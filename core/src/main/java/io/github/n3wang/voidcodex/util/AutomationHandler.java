package io.github.n3wang.voidcodex.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import io.github.n3wang.voidcodex.VoidCodexGame;
import io.github.n3wang.voidcodex.screens.*;

/**
 * Handles automation commands from the MCP server.
 * Reads commands from command_queue.json and executes them.
 */
public class AutomationHandler {
    private static final String COMMAND_QUEUE = "debug_screenshots/command_queue.json";
    private static final String STATUS_FILE = "debug_screenshots/game_status.json";
    private static long lastCommandTimestamp = 0;
    private static VoidCodexGame game;
    private static boolean enabled = true;

    /**
     * Initialize automation handler
     */
    public static void init(VoidCodexGame gameInstance) {
        game = gameInstance;
        Gdx.app.log("AutomationHandler", "Automation enabled - listening for MCP commands");
    }

    /**
     * Update - call this every frame to check for commands
     */
    public static void update(String currentScreen) {
        if (!enabled || game == null) return;

        // Write current status
        writeStatus(currentScreen);

        // Check for commands
        try {
            FileHandle commandFile = Gdx.files.local(COMMAND_QUEUE);
            if (!commandFile.exists()) return;

            String jsonString = commandFile.readString();
            if (jsonString == null || jsonString.trim().isEmpty()) return;

            Json json = new Json();
            JsonValue commandData = new com.badlogic.gdx.utils.JsonReader().parse(jsonString);

            long timestamp = commandData.getLong("timestamp", 0);

            // Only process if it's a new command
            if (timestamp <= lastCommandTimestamp) return;

            lastCommandTimestamp = timestamp;

            String command = commandData.getString("command", "");
            JsonValue params = commandData.get("params");

            Gdx.app.log("AutomationHandler", "Executing command: " + command);

            executeCommand(command, params);

            // Delete command file after execution
            commandFile.delete();

        } catch (Exception e) {
            Gdx.app.error("AutomationHandler", "Error processing command", e);
        }
    }

    /**
     * Execute a command
     */
    private static void executeCommand(String command, JsonValue params) {
        switch (command) {
            case "navigate":
                handleNavigate(params);
                break;

            case "click":
                handleClick(params);
                break;

            case "press_key":
                handlePressKey(params);
                break;

            case "capture":
                handleCapture(params);
                break;

            case "wait":
                // Wait is handled by MCP server, not by game
                break;

            default:
                Gdx.app.log("AutomationHandler", "Unknown command: " + command);
        }
    }

    /**
     * Navigate to a screen
     */
    private static void handleNavigate(JsonValue params) {
        if (params == null) return;

        String screenName = params.getString("screen", params.getString("target", ""));
        Gdx.app.log("AutomationHandler", "Navigating to: " + screenName);

        Gdx.app.postRunnable(() -> {
            try {
                switch (screenName) {
                    case "MainMenu":
                    case "MainMenuScreen":
                        game.setScreen(new MainMenuScreen(game));
                        break;

                    case "ShipScreen":
                    case "ShipScreenNew":
                        game.setScreen(new ShipScreenNew(game));
                        break;

                    case "CodexScreen":
                        game.setScreen(new CodexScreen(game));
                        break;

                    case "ScenarioSelection":
                    case "ScenarioSelectionScreen":
                        game.setScreen(new ScenarioSelectionScreen(game));
                        break;

                    default:
                        Gdx.app.log("AutomationHandler", "Unknown screen: " + screenName);
                }
            } catch (Exception e) {
                Gdx.app.error("AutomationHandler", "Failed to navigate to " + screenName, e);
            }
        });
    }

    /**
     * Simulate a click at coordinates
     */
    private static void handleClick(JsonValue params) {
        if (params == null) return;

        int x = params.getInt("x", 0);
        int y = params.getInt("y", 0);
        String button = params.getString("button", "left");

        int buttonCode = button.equals("right") ? Input.Buttons.RIGHT : Input.Buttons.LEFT;

        Gdx.app.log("AutomationHandler", String.format("Clicking at (%d, %d) with %s button", x, y, button));

        Gdx.app.postRunnable(() -> {
            try {
                // Simulate touch down and up
                Gdx.input.getInputProcessor().touchDown(x, y, 0, buttonCode);
                Gdx.input.getInputProcessor().touchUp(x, y, 0, buttonCode);
            } catch (Exception e) {
                Gdx.app.error("AutomationHandler", "Failed to simulate click", e);
            }
        });
    }

    /**
     * Simulate a key press
     */
    private static void handlePressKey(JsonValue params) {
        if (params == null) return;

        String keyName = params.getString("key", "");
        int keyCode = getKeyCode(keyName);

        Gdx.app.log("AutomationHandler", "Pressing key: " + keyName);

        Gdx.app.postRunnable(() -> {
            try {
                Gdx.input.getInputProcessor().keyDown(keyCode);
                // Small delay then key up
                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        Gdx.app.postRunnable(() -> {
                            Gdx.input.getInputProcessor().keyUp(keyCode);
                        });
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }).start();
            } catch (Exception e) {
                Gdx.app.error("AutomationHandler", "Failed to simulate key press", e);
            }
        });
    }

    /**
     * Capture a screenshot with a specific name
     */
    private static void handleCapture(JsonValue params) {
        if (params == null) return;

        String name = params.getString("name", "automated");
        Gdx.app.log("AutomationHandler", "Capturing screenshot: " + name);

        // Force a screenshot with custom name
        Gdx.app.postRunnable(() -> {
            DebugScreenshotManager.captureWithName(name);
        });
    }

    /**
     * Write current game status to file
     */
    private static void writeStatus(String currentScreen) {
        try {
            FileHandle statusFile = Gdx.files.local(STATUS_FILE);

            // Create status JSON
            String status = String.format(
                "{\"currentScreen\":\"%s\",\"timestamp\":%d,\"fps\":%d}",
                currentScreen,
                System.currentTimeMillis(),
                Gdx.graphics.getFramesPerSecond()
            );

            statusFile.writeString(status, false);

        } catch (Exception e) {
            // Silently fail - status writing is not critical
        }
    }

    /**
     * Get LibGDX key code from key name
     */
    private static int getKeyCode(String keyName) {
        switch (keyName.toUpperCase()) {
            case "F12": return Input.Keys.F12;
            case "SPACE": return Input.Keys.SPACE;
            case "ENTER": return Input.Keys.ENTER;
            case "ESC": case "ESCAPE": return Input.Keys.ESCAPE;
            case "LEFT": return Input.Keys.LEFT;
            case "RIGHT": return Input.Keys.RIGHT;
            case "UP": return Input.Keys.UP;
            case "DOWN": return Input.Keys.DOWN;
            case "W": return Input.Keys.W;
            case "A": return Input.Keys.A;
            case "S": return Input.Keys.S;
            case "D": return Input.Keys.D;
            default: return Input.Keys.UNKNOWN;
        }
    }

    /**
     * Enable or disable automation
     */
    public static void setEnabled(boolean enabled) {
        AutomationHandler.enabled = enabled;
        Gdx.app.log("AutomationHandler", "Automation " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Check if automation is enabled
     */
    public static boolean isEnabled() {
        return enabled;
    }
}
