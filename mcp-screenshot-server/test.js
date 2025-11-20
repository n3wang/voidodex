#!/usr/bin/env node

/**
 * Test script for VoidCodex Screenshot MCP Server
 * Run this to verify the server can access screenshots
 */

import fs from 'fs/promises';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const SCREENSHOT_DIR = path.join(__dirname, '..', 'debug_screenshots');

async function test() {
  console.log('🧪 Testing VoidCodex Screenshot MCP Server\n');

  // Test 1: Check if screenshots directory exists
  console.log('1. Checking screenshot directory...');
  try {
    await fs.access(SCREENSHOT_DIR);
    console.log('   ✓ Directory exists:', SCREENSHOT_DIR);
  } catch (error) {
    console.log('   ✗ Directory not found:', SCREENSHOT_DIR);
    console.log('   Creating directory...');
    await fs.mkdir(SCREENSHOT_DIR, { recursive: true });
    console.log('   ✓ Directory created');
  }

  // Test 2: List screenshots
  console.log('\n2. Listing screenshots...');
  try {
    const files = await fs.readdir(SCREENSHOT_DIR);
    const screenshots = files.filter(f =>
      f.endsWith('.png') || f.endsWith('.jpg') || f.endsWith('.jpeg')
    );

    if (screenshots.length === 0) {
      console.log('   ⚠ No screenshots found');
      console.log('   Run your game and press F12 to capture some!');
    } else {
      console.log(`   ✓ Found ${screenshots.length} screenshot(s):`);
      for (const screenshot of screenshots) {
        const stats = await fs.stat(path.join(SCREENSHOT_DIR, screenshot));
        const sizeKB = (stats.size / 1024).toFixed(2);
        console.log(`     - ${screenshot} (${sizeKB} KB)`);
      }
    }
  } catch (error) {
    console.log('   ✗ Error listing screenshots:', error.message);
  }

  // Test 3: Check Node modules
  console.log('\n3. Checking dependencies...');
  try {
    await fs.access(path.join(__dirname, 'node_modules'));
    console.log('   ✓ Dependencies installed');
  } catch (error) {
    console.log('   ✗ Dependencies not installed');
    console.log('   Run: npm install');
  }

  // Test 4: Try to import the MCP SDK
  console.log('\n4. Testing MCP SDK...');
  try {
    await import('@modelcontextprotocol/sdk/server/index.js');
    console.log('   ✓ MCP SDK available');
  } catch (error) {
    console.log('   ✗ MCP SDK not available:', error.message);
    console.log('   Run: npm install');
  }

  console.log('\n✅ Test complete!\n');
  console.log('Next steps:');
  console.log('  1. Run: install.bat (to configure Claude Code)');
  console.log('  2. Restart Claude Code');
  console.log('  3. Ask Claude: "List my debug screenshots"');
}

test().catch(console.error);
