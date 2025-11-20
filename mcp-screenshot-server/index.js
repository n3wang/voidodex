#!/usr/bin/env node

/**
 * VoidCodex Screenshot MCP Server
 *
 * Provides Claude Code with direct access to debug screenshots
 * for automatic review and analysis.
 *
 * Tools provided:
 * - list_screenshots: List all available screenshots
 * - get_latest_screenshot: Get the most recent screenshot
 * - get_screenshot: Get a specific screenshot by name
 * - watch_screenshots: Get info about new screenshots since last check
 */

import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from '@modelcontextprotocol/sdk/types.js';
import fs from 'fs/promises';
import path from 'path';
import { fileURLToPath } from 'url';
import { spawn, exec } from 'child_process';
import { promisify } from 'util';

const execAsync = promisify(exec);

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Screenshot directory (relative to project root)
const SCREENSHOT_DIR = path.join(__dirname, '..', 'debug_screenshots');
const PROJECT_ROOT = path.join(__dirname, '..');

// Track last seen screenshots for watch functionality
let lastSeenScreenshots = new Set();

// Track running game process
let gameProcess = null;

// Command queue for game automation
const commandQueue = path.join(PROJECT_ROOT, 'debug_screenshots', 'command_queue.json');

/**
 * Get all screenshots in the directory
 */
async function getScreenshots() {
  try {
    await fs.mkdir(SCREENSHOT_DIR, { recursive: true });
    const files = await fs.readdir(SCREENSHOT_DIR);

    const screenshots = await Promise.all(
      files
        .filter(f => f.endsWith('.png') || f.endsWith('.jpg') || f.endsWith('.jpeg'))
        .map(async (file) => {
          const filePath = path.join(SCREENSHOT_DIR, file);
          const stats = await fs.stat(filePath);

          // Parse screen name and type from filename
          // Format: ScreenName_type_timestamp.png
          const parts = file.replace(/\.(png|jpg|jpeg)$/i, '').split('_');
          const screenName = parts[0] || 'Unknown';
          const type = parts[1] || 'unknown';

          return {
            name: file,
            path: filePath,
            screenName,
            type,
            size: stats.size,
            modified: stats.mtime,
            modifiedMs: stats.mtimeMs
          };
        })
    );

    // Sort by modification time (newest first)
    return screenshots.sort((a, b) => b.modifiedMs - a.modifiedMs);
  } catch (error) {
    console.error('Error reading screenshots:', error);
    return [];
  }
}

/**
 * Read screenshot as base64
 */
async function readScreenshotAsBase64(filePath) {
  const buffer = await fs.readFile(filePath);
  return buffer.toString('base64');
}

/**
 * Get file extension to determine media type
 */
function getMediaType(filename) {
  const ext = path.extname(filename).toLowerCase();
  const types = {
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
  };
  return types[ext] || 'image/png';
}

/**
 * Create the MCP server
 */
const server = new Server(
  {
    name: 'voidcodex-screenshot-server',
    version: '1.0.0',
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

/**
 * List available tools
 */
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: 'list_screenshots',
        description: 'List all available debug screenshots from the VoidCodex game. Returns metadata including screen name, type (auto/manual), and timestamp.',
        inputSchema: {
          type: 'object',
          properties: {
            screen_filter: {
              type: 'string',
              description: 'Optional: filter by screen name (e.g., "ShipScreen")',
            },
            limit: {
              type: 'number',
              description: 'Optional: limit number of results (default: 10)',
            },
          },
        },
      },
      {
        name: 'get_latest_screenshot',
        description: 'Get the most recent screenshot. Returns the image data that Claude can analyze.',
        inputSchema: {
          type: 'object',
          properties: {
            screen_name: {
              type: 'string',
              description: 'Optional: get latest from specific screen (e.g., "ShipScreenNew")',
            },
          },
        },
      },
      {
        name: 'get_screenshot',
        description: 'Get a specific screenshot by filename. Returns the image data that Claude can analyze.',
        inputSchema: {
          type: 'object',
          properties: {
            filename: {
              type: 'string',
              description: 'The screenshot filename (e.g., "ShipScreenNew_manual_2025-01-20_14-30-00.png")',
            },
          },
          required: ['filename'],
        },
      },
      {
        name: 'watch_screenshots',
        description: 'Check for new screenshots since last check. Useful for monitoring during development.',
        inputSchema: {
          type: 'object',
          properties: {},
        },
      },
      {
        name: 'compare_screenshots',
        description: 'Compare two screenshots side by side (returns both images for Claude to analyze).',
        inputSchema: {
          type: 'object',
          properties: {
            filename1: {
              type: 'string',
              description: 'First screenshot filename',
            },
            filename2: {
              type: 'string',
              description: 'Second screenshot filename',
            },
          },
          required: ['filename1', 'filename2'],
        },
      },
      {
        name: 'build_game',
        description: 'Build the game using Gradle. Returns build output.',
        inputSchema: {
          type: 'object',
          properties: {
            clean: {
              type: 'boolean',
              description: 'Whether to run clean before build (default: false)',
            },
          },
        },
      },
      {
        name: 'start_game',
        description: 'Start the game. Can specify which screen to navigate to after launch.',
        inputSchema: {
          type: 'object',
          properties: {
            target_screen: {
              type: 'string',
              description: 'Optional: screen to navigate to (e.g., "ShipScreen", "CodexScreen")',
            },
            wait_for_start: {
              type: 'boolean',
              description: 'Wait for game to fully start before returning (default: true)',
            },
          },
        },
      },
      {
        name: 'stop_game',
        description: 'Stop the currently running game.',
        inputSchema: {
          type: 'object',
          properties: {},
        },
      },
      {
        name: 'restart_game',
        description: 'Restart the game. Optionally rebuild before restarting.',
        inputSchema: {
          type: 'object',
          properties: {
            rebuild: {
              type: 'boolean',
              description: 'Whether to rebuild before restarting (default: false)',
            },
            target_screen: {
              type: 'string',
              description: 'Optional: screen to navigate to after restart',
            },
          },
        },
      },
      {
        name: 'game_command',
        description: 'Send a command to the running game (navigate, click, input text, etc.).',
        inputSchema: {
          type: 'object',
          properties: {
            command: {
              type: 'string',
              description: 'Command type: "navigate", "click", "press_key", "wait", "capture"',
            },
            params: {
              type: 'object',
              description: 'Command parameters (varies by command type)',
            },
          },
          required: ['command'],
        },
      },
      {
        name: 'automate_scenario',
        description: 'Run an automated scenario (sequence of actions) to capture specific game state.',
        inputSchema: {
          type: 'object',
          properties: {
            scenario: {
              type: 'string',
              description: 'Scenario name: "test_energy_blocks", "navigate_all_screens", "test_crew_movement", etc.',
            },
            capture_steps: {
              type: 'boolean',
              description: 'Capture screenshot at each step (default: true)',
            },
          },
          required: ['scenario'],
        },
      },
      {
        name: 'get_game_status',
        description: 'Get current status of the game (running, stopped, screen name, etc.).',
        inputSchema: {
          type: 'object',
          properties: {},
        },
      },
    ],
  };
});

/**
 * Handle tool calls
 */
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
    switch (name) {
      case 'list_screenshots': {
        let screenshots = await getScreenshots();

        // Apply screen filter if provided
        if (args.screen_filter) {
          screenshots = screenshots.filter(s =>
            s.screenName.toLowerCase().includes(args.screen_filter.toLowerCase())
          );
        }

        // Apply limit
        const limit = args.limit || 10;
        screenshots = screenshots.slice(0, limit);

        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(screenshots.map(s => ({
                filename: s.name,
                screenName: s.screenName,
                type: s.type,
                size: `${(s.size / 1024).toFixed(2)} KB`,
                modified: s.modified.toISOString(),
              })), null, 2),
            },
          ],
        };
      }

      case 'get_latest_screenshot': {
        let screenshots = await getScreenshots();

        // Filter by screen name if provided
        if (args.screen_name) {
          screenshots = screenshots.filter(s =>
            s.screenName.toLowerCase() === args.screen_name.toLowerCase()
          );
        }

        if (screenshots.length === 0) {
          return {
            content: [
              {
                type: 'text',
                text: 'No screenshots found',
              },
            ],
          };
        }

        const latest = screenshots[0];
        const imageData = await readScreenshotAsBase64(latest.path);
        const mediaType = getMediaType(latest.name);

        return {
          content: [
            {
              type: 'text',
              text: `Latest screenshot: ${latest.name} (${latest.screenName}, ${latest.type}, ${new Date(latest.modified).toLocaleString()})`,
            },
            {
              type: 'image',
              data: imageData,
              mimeType: mediaType,
            },
          ],
        };
      }

      case 'get_screenshot': {
        const filename = args.filename;
        const filePath = path.join(SCREENSHOT_DIR, filename);

        try {
          const imageData = await readScreenshotAsBase64(filePath);
          const mediaType = getMediaType(filename);

          return {
            content: [
              {
                type: 'text',
                text: `Screenshot: ${filename}`,
              },
              {
                type: 'image',
                data: imageData,
                mimeType: mediaType,
              },
            ],
          };
        } catch (error) {
          return {
            content: [
              {
                type: 'text',
                text: `Error: Screenshot "${filename}" not found`,
              },
            ],
          };
        }
      }

      case 'watch_screenshots': {
        const currentScreenshots = await getScreenshots();
        const currentSet = new Set(currentScreenshots.map(s => s.name));

        // Find new screenshots
        const newScreenshots = currentScreenshots.filter(s => !lastSeenScreenshots.has(s.name));

        // Update last seen
        lastSeenScreenshots = currentSet;

        if (newScreenshots.length === 0) {
          return {
            content: [
              {
                type: 'text',
                text: 'No new screenshots since last check',
              },
            ],
          };
        }

        return {
          content: [
            {
              type: 'text',
              text: `Found ${newScreenshots.length} new screenshot(s):\n` +
                    JSON.stringify(newScreenshots.map(s => ({
                      filename: s.name,
                      screenName: s.screenName,
                      type: s.type,
                      modified: s.modified.toISOString(),
                    })), null, 2),
            },
          ],
        };
      }

      case 'compare_screenshots': {
        const file1 = args.filename1;
        const file2 = args.filename2;

        try {
          const image1 = await readScreenshotAsBase64(path.join(SCREENSHOT_DIR, file1));
          const image2 = await readScreenshotAsBase64(path.join(SCREENSHOT_DIR, file2));

          return {
            content: [
              {
                type: 'text',
                text: `Comparing screenshots:\n1. ${file1}\n2. ${file2}`,
              },
              {
                type: 'image',
                data: image1,
                mimeType: getMediaType(file1),
              },
              {
                type: 'image',
                data: image2,
                mimeType: getMediaType(file2),
              },
            ],
          };
        } catch (error) {
          return {
            content: [
              {
                type: 'text',
                text: `Error comparing screenshots: ${error.message}`,
              },
            ],
          };
        }
      }

      case 'build_game': {
        const clean = args.clean || false;
        const buildCommand = clean ? 'gradlew.bat clean desktop:build' : 'gradlew.bat desktop:build';

        try {
          const result = await execAsync(buildCommand, {
            cwd: PROJECT_ROOT,
            timeout: 300000, // 5 minutes
          });

          return {
            content: [
              {
                type: 'text',
                text: `Build ${clean ? '(clean) ' : ''}completed successfully.\n\nOutput:\n${result.stdout.slice(-500)}`,
              },
            ],
          };
        } catch (error) {
          return {
            content: [
              {
                type: 'text',
                text: `Build failed:\n${error.stderr || error.message}`,
              },
            ],
          };
        }
      }

      case 'start_game': {
        if (gameProcess && !gameProcess.killed) {
          return {
            content: [
              {
                type: 'text',
                text: 'Game is already running. Use stop_game first or use restart_game.',
              },
            ],
          };
        }

        const waitForStart = args.wait_for_start !== false;

        try {
          gameProcess = spawn('gradlew.bat', ['desktop:run'], {
            cwd: PROJECT_ROOT,
            detached: false,
            stdio: ['ignore', 'pipe', 'pipe'],
          });

          let output = '';
          gameProcess.stdout.on('data', (data) => {
            output += data.toString();
          });

          gameProcess.stderr.on('data', (data) => {
            output += data.toString();
          });

          // Write target screen to command queue if specified
          if (args.target_screen) {
            await fs.writeFile(commandQueue, JSON.stringify({
              command: 'navigate',
              target: args.target_screen,
              timestamp: Date.now(),
            }));
          }

          if (waitForStart) {
            // Wait up to 30 seconds for game to start
            await new Promise((resolve) => setTimeout(resolve, 10000));
          }

          return {
            content: [
              {
                type: 'text',
                text: `Game started (PID: ${gameProcess.pid})${args.target_screen ? `\nNavigating to: ${args.target_screen}` : ''}`,
              },
            ],
          };
        } catch (error) {
          return {
            content: [
              {
                type: 'text',
                text: `Failed to start game: ${error.message}`,
              },
            ],
          };
        }
      }

      case 'stop_game': {
        if (!gameProcess || gameProcess.killed) {
          return {
            content: [
              {
                type: 'text',
                text: 'No game process running.',
              },
            ],
          };
        }

        try {
          // On Windows, use taskkill to ensure all child processes are killed
          if (process.platform === 'win32') {
            await execAsync(`taskkill /pid ${gameProcess.pid} /T /F`);
          } else {
            gameProcess.kill('SIGTERM');
          }

          gameProcess = null;

          return {
            content: [
              {
                type: 'text',
                text: 'Game stopped successfully.',
              },
            ],
          };
        } catch (error) {
          return {
            content: [
              {
                type: 'text',
                text: `Failed to stop game: ${error.message}`,
              },
            ],
          };
        }
      }

      case 'restart_game': {
        const rebuild = args.rebuild || false;
        const targetScreen = args.target_screen;

        let result = 'Restarting game...\n';

        // Stop existing game
        if (gameProcess && !gameProcess.killed) {
          try {
            if (process.platform === 'win32') {
              await execAsync(`taskkill /pid ${gameProcess.pid} /T /F`);
            } else {
              gameProcess.kill('SIGTERM');
            }
            gameProcess = null;
            await new Promise(resolve => setTimeout(resolve, 2000));
            result += 'Stopped existing game.\n';
          } catch (error) {
            result += `Warning: Failed to stop cleanly: ${error.message}\n`;
          }
        }

        // Rebuild if requested
        if (rebuild) {
          try {
            result += 'Building game...\n';
            const buildResult = await execAsync('gradlew.bat desktop:build', {
              cwd: PROJECT_ROOT,
              timeout: 300000,
            });
            result += 'Build completed.\n';
          } catch (error) {
            return {
              content: [
                {
                  type: 'text',
                  text: `Failed to rebuild: ${error.message}`,
                },
              ],
            };
          }
        }

        // Start game
        gameProcess = spawn('gradlew.bat', ['desktop:run'], {
          cwd: PROJECT_ROOT,
          detached: false,
          stdio: ['ignore', 'pipe', 'pipe'],
        });

        // Write target screen to command queue if specified
        if (targetScreen) {
          await fs.writeFile(commandQueue, JSON.stringify({
            command: 'navigate',
            target: targetScreen,
            timestamp: Date.now(),
          }));
        }

        await new Promise(resolve => setTimeout(resolve, 10000));

        result += `Game started (PID: ${gameProcess.pid})`;
        if (targetScreen) {
          result += `\nNavigating to: ${targetScreen}`;
        }

        return {
          content: [
            {
              type: 'text',
              text: result,
            },
          ],
        };
      }

      case 'game_command': {
        const { command, params } = args;

        // Write command to queue file for game to read
        await fs.writeFile(commandQueue, JSON.stringify({
          command,
          params: params || {},
          timestamp: Date.now(),
        }));

        return {
          content: [
            {
              type: 'text',
              text: `Command sent: ${command}${params ? `\nParams: ${JSON.stringify(params)}` : ''}`,
            },
          ],
        };
      }

      case 'automate_scenario': {
        const { scenario, capture_steps } = args;
        const captureSteps = capture_steps !== false;

        // Define scenarios
        const scenarios = {
          test_energy_blocks: [
            { command: 'navigate', params: { screen: 'ShipScreen' } },
            { command: 'wait', params: { ms: 2000 } },
            { command: 'capture', params: { name: 'energy_blocks_initial' } },
            { command: 'click', params: { x: 100, y: 600 } }, // Click first system
            { command: 'wait', params: { ms: 500 } },
            { command: 'capture', params: { name: 'energy_blocks_powered_1' } },
            { command: 'click', params: { x: 160, y: 600 } }, // Click second system
            { command: 'wait', params: { ms: 500 } },
            { command: 'capture', params: { name: 'energy_blocks_powered_2' } },
          ],
          navigate_all_screens: [
            { command: 'navigate', params: { screen: 'MainMenu' } },
            { command: 'capture', params: { name: 'main_menu' } },
            { command: 'navigate', params: { screen: 'ShipScreen' } },
            { command: 'capture', params: { name: 'ship_screen' } },
            { command: 'navigate', params: { screen: 'CodexScreen' } },
            { command: 'capture', params: { name: 'codex_screen' } },
          ],
          test_crew_movement: [
            { command: 'navigate', params: { screen: 'ShipScreen' } },
            { command: 'wait', params: { ms: 2000 } },
            { command: 'capture', params: { name: 'crew_initial' } },
            { command: 'click', params: { x: 300, y: 300 } }, // Select crew
            { command: 'wait', params: { ms: 500 } },
            { command: 'click', params: { x: 400, y: 300, button: 'right' } }, // Move crew
            { command: 'wait', params: { ms: 2000 } },
            { command: 'capture', params: { name: 'crew_moving' } },
            { command: 'wait', params: { ms: 3000 } },
            { command: 'capture', params: { name: 'crew_arrived' } },
          ],
        };

        const scenarioSteps = scenarios[scenario];
        if (!scenarioSteps) {
          return {
            content: [
              {
                type: 'text',
                text: `Unknown scenario: ${scenario}\nAvailable: ${Object.keys(scenarios).join(', ')}`,
              },
            ],
          };
        }

        // Execute scenario steps
        let result = `Executing scenario: ${scenario}\n\n`;
        for (let i = 0; i < scenarioSteps.length; i++) {
          const step = scenarioSteps[i];
          result += `Step ${i + 1}: ${step.command}\n`;

          // Write command to queue
          await fs.writeFile(commandQueue, JSON.stringify({
            ...step,
            timestamp: Date.now(),
          }));

          // Wait for step to complete
          if (step.command === 'wait') {
            await new Promise(resolve => setTimeout(resolve, step.params.ms));
          } else {
            await new Promise(resolve => setTimeout(resolve, 1000));
          }
        }

        result += '\nScenario completed.';

        return {
          content: [
            {
              type: 'text',
              text: result,
            },
          ],
        };
      }

      case 'get_game_status': {
        const isRunning = gameProcess && !gameProcess.killed;

        let status = {
          running: isRunning,
          pid: isRunning ? gameProcess.pid : null,
        };

        // Try to read current screen from a status file
        try {
          const statusFile = path.join(SCREENSHOT_DIR, 'game_status.json');
          const statusData = await fs.readFile(statusFile, 'utf8');
          const gameStatus = JSON.parse(statusData);
          status = { ...status, ...gameStatus };
        } catch (error) {
          // Status file doesn't exist yet
        }

        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(status, null, 2),
            },
          ],
        };
      }

      default:
        return {
          content: [
            {
              type: 'text',
              text: `Unknown tool: ${name}`,
            },
          ],
          isError: true,
        };
    }
  } catch (error) {
    return {
      content: [
        {
          type: 'text',
          text: `Error: ${error.message}`,
        },
      ],
      isError: true,
    };
  }
});

/**
 * Start the server
 */
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error('VoidCodex Screenshot MCP Server running on stdio');
}

main().catch((error) => {
  console.error('Server error:', error);
  process.exit(1);
});
