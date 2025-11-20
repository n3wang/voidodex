# 🤖 VoidCodex Claude Code Automation System

## What We Built

A complete **AI-powered game development assistant** that integrates directly with Claude Code CLI to provide:

### 1. 📸 Automatic Screenshot Capture
- Press **F12** in-game for manual capture
- Auto-captures each screen on first visit
- Organized in `debug_screenshots/` folder

### 2. 🔌 MCP Server Integration
- **Model Context Protocol** server connects Claude Code to your game
- I can access, view, and analyze screenshots automatically
- No need to manually send me file paths!

### 3. 🎮 Full Game Control (NEW!)
- **Build/start/stop/restart** the game automatically
- **Navigate** between screens without user input
- **Simulate clicks and keyboard input**
- **Run automated test scenarios**
- **Complete end-to-end testing** - all automated!

## Files Created

### Core Components

**Java (Game-side):**
- `core/src/main/java/io/github/n3wang/voidcodex/util/`
  - `DebugScreenshotManager.java` - Screenshot capture (F12 + auto)
  - `AutomationHandler.java` - **NEW!** MCP command executor

**Node.js (MCP Server):**
- `mcp-screenshot-server/`
  - `index.js` - **Enhanced** MCP server with game control
  - `package.json` - Dependencies
  - `test.js` - Test script
  - `README.md` - MCP server documentation

**Setup Scripts:**
- `setup_screenshot_system.bat` - One-click setup
- `mcp-screenshot-server/install.bat` - MCP server installer
- `review_screenshots.bat` - Batch screenshot review (legacy)

**Documentation:**
- `AUTOMATED_TESTING.md` - **NEW!** Complete automation guide
- `SCREENSHOT_CLAUDE_INTEGRATION.md` - MCP integration guide
- `QUICKSTART_SCREENSHOTS.md` - **Updated** Quick reference
- `DEBUG_SCREENSHOTS.md` - Screenshot system details
- `README_AUTOMATION_SYSTEM.md` - This file!

## How It Works

```
                    ┌─────────────────────┐
                    │   YOU (Developer)   │
                    └──────────┬──────────┘
                               │
                    "Test energy blocks"
                               │
                               ▼
┌──────────────────────────────────────────────────────────┐
│                  Claude Code CLI (Me!)                    │
│  - Understands natural language requests                  │
│  - Automatically uses MCP tools                           │
│  - Can see and analyze images                            │
└──────────────────────┬───────────────────────────────────┘
                       │ MCP Protocol
                       ▼
┌──────────────────────────────────────────────────────────┐
│              MCP Screenshot Server (Node.js)              │
│                                                           │
│  Screenshot Tools:          Game Control Tools:          │
│  - list_screenshots         - build_game                 │
│  - get_latest_screenshot    - start_game                 │
│  - get_screenshot           - stop_game                  │
│  - watch_screenshots        - restart_game               │
│  - compare_screenshots      - game_command               │
│                             - automate_scenario          │
│                             - get_game_status            │
└──────────────────────┬───────────────────────────────────┘
                       │
        ┌──────────────┴───────────────┐
        │                              │
        ▼                              ▼
┌──────────────────┐      ┌──────────────────────────┐
│  Screenshots/    │      │  Command Queue           │
│  Status Files    │      │  (JSON files)            │
└──────────────────┘      └──────────┬───────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────┐
│                    Your Game (LibGDX)                     │
│                                                           │
│  Every Frame:                                            │
│  1. AutomationHandler.update() - Read command queue     │
│  2. Execute commands (navigate, click, capture)         │
│  3. Write status to file                                │
│  4. DebugScreenshotManager.update() - F12 capture      │
└──────────────────────────────────────────────────────────┘
```

## Quick Start

### Installation
```bash
# One command to set up everything:
setup_screenshot_system.bat
```

This will:
1. ✅ Install Node.js dependencies
2. ✅ Configure Claude Code
3. ✅ Test the setup
4. ✅ Optionally build and run the game

### First Use

**Restart your terminal**, then:

```bash
claude code
```

Try these commands:

```
You: "List my debug screenshots"
→ I'll show all captured screenshots

You: "Start the game"
→ Game launches automatically

You: "Test all screens"
→ Automated navigation + screenshot capture

You: "The energy blocks aren't coloring - investigate"
→ Full automated test + analysis + fix suggestions
```

## Example Workflows

### Workflow 1: Manual Testing (Original)
```
1. Run game manually
2. Navigate to bug location
3. Press F12 to capture
4. Tell me: "Check latest screenshot"
5. I analyze and suggest fixes
```

### Workflow 2: Semi-Automated (Screenshot viewing)
```
You: "Show me the latest ship screen screenshot"
Me: [Uses get_latest_screenshot automatically]
    [Analyzes image]
    "I can see the energy blocks aren't coloring..."
```

### Workflow 3: Fully Automated (NEW!)
```
You: "Test the energy blocks feature"
Me: [Uses start_game]
    [Uses automate_scenario: test_energy_blocks]
    [Uses get_screenshot for each step]
    [Analyzes all screenshots]
    "Test complete. Found issue: blocks not coloring when powered.
     The bug is in ShipScreenNew.java:896..."
```

### Workflow 4: Complete Auto-Fix (NEW!)
```
You: "The energy blocks don't color - fix it"

Me: [Uses restart_game with rebuild]
    [Uses automate_scenario: test_energy_blocks]
    [Analyzes screenshots]
    "Confirmed bug. Investigating code..."

    [Reads ShipScreenNew.java]
    "Found issue in createBottomPanel()..."

    [Applies fix via Edit tool]
    "Fix applied. Testing..."

    [Uses restart_game with rebuild=true]
    [Uses automate_scenario again]
    [Compares before/after screenshots]

    "✓ FIXED! Energy blocks now color correctly.
     Before: All blocks dark grey
     After: Powered blocks light grey (0.9f)"
```

## What Makes This Special

### 🌟 Zero User Input Required
Traditional testing:
- You manually run the game
- You manually navigate UI
- You manually capture screenshots
- You manually send me the paths

With automation:
- Just tell me what to test
- I do everything automatically
- You get comprehensive results

### 🌟 Natural Language Control
No need to learn commands or syntax:

❌ **Bad (traditional):**
```bash
./gradlew desktop:run
# wait...
# click UI...
# press F12...
# take note of filename...
claude code
> "Analyze debug_screenshots/ShipScreen_manual_2025-01-20_14-30-00.png"
```

✅ **Good (with automation):**
```bash
claude code
> "Test the ship screen"
```

### 🌟 Intelligent Automation
I'm not just executing scripts - I can:
- **Adapt** test strategies based on what I see
- **Investigate** unexpected issues
- **Suggest** additional tests
- **Compare** results intelligently
- **Fix** code and verify automatically

## MCP Tools Reference

### Screenshot Tools
| Tool | Purpose | Example |
|------|---------|---------|
| `list_screenshots` | List all screenshots | "Show me all screenshots" |
| `get_latest_screenshot` | Get most recent | "Show latest screenshot" |
| `get_screenshot` | Get specific file | "Show ShipScreen_auto_*.png" |
| `watch_screenshots` | Check for new ones | "Any new screenshots?" |
| `compare_screenshots` | Side-by-side comparison | "Compare before and after" |

### Game Control Tools (NEW!)
| Tool | Purpose | Example |
|------|---------|---------|
| `build_game` | Build with Gradle | "Build the game" |
| `start_game` | Launch game | "Start the game on ship screen" |
| `stop_game` | Stop running game | "Stop the game" |
| `restart_game` | Restart (optional rebuild) | "Restart with latest changes" |
| `game_command` | Send single command | "Click at (100, 200)" |
| `automate_scenario` | Run test scenario | "Run energy blocks test" |
| `get_game_status` | Check game state | "Is the game running?" |

## Built-in Test Scenarios

### `test_energy_blocks`
Tests energy system power allocation UI:
1. Navigate to Ship Screen
2. Capture initial (no power)
3. Add power to system 1 → capture
4. Add power to system 2 → capture
5. Compare states

### `navigate_all_screens`
Comprehensive screen tour:
1. Main Menu → capture
2. Ship Screen → capture
3. Codex Screen → capture
4. Return to menu

### `test_crew_movement`
Tests crew movement mechanics:
1. Navigate to Ship Screen
2. Select crew member
3. Command movement
4. Capture during movement
5. Verify arrival
6. Compare start/end positions

## Configuration

### Enable/Disable Debug Mode

**In game code:**
```java
DebugScreenshotManager.setDebugMode(false); // Disable
```

**Or edit** `DebugScreenshotManager.java:20`:
```java
private static boolean DEBUG_MODE = false;
```

### Enable/Disable Automation

**In game code:**
```java
AutomationHandler.setEnabled(false); // Disable
```

Useful for production builds where you don't want automation.

### Add Custom Scenarios

Edit `mcp-screenshot-server/index.js`, find the `scenarios` object, and add:

```javascript
my_custom_test: [
  { command: 'navigate', params: { screen: 'MyScreen' } },
  { command: 'wait', params: { ms: 1000 } },
  { command: 'click', params: { x: 200, y: 300 } },
  { command: 'capture', params: { name: 'custom_step1' } },
  // ... more steps
],
```

Then use: "Run my_custom_test scenario"

## Troubleshooting

### MCP Server Not Working
```bash
cd mcp-screenshot-server
node test.js
```

Check:
- Node.js installed? `node --version`
- Dependencies installed? `npm install`
- Config file correct? Check `%APPDATA%\Claude\claude_desktop_config.json`

### Game Not Starting
Check:
- Java installed? `java -version`
- Gradle working? `gradlew --version`
- Build successful? `gradlew desktop:build`

### Commands Not Executing
Check:
- AutomationHandler initialized? See logs for "Automation enabled"
- Command queue file writable? Check `debug_screenshots/` folder
- Game window has focus?

### Screenshots Not Captured
Check:
- Debug mode enabled? See `DebugScreenshotManager.DEBUG_MODE`
- Screenshots folder exists? `debug_screenshots/` should be created automatically
- Permissions? Ensure write access to project folder

## Performance Notes

- Screenshot capture: ~50ms (negligible)
- Automation overhead: <1ms per frame (reads JSON file)
- Game control: Instant (uses LibGDX input processor)
- MCP server: Low memory (~50MB)

## Security Considerations

- **Local only:** MCP server uses stdio, no network exposure
- **Game control:** Only works with your local game instance
- **File access:** Limited to project directory
- **No secrets:** No API keys or credentials needed

## Future Enhancements

Potential additions:
- 🎯 Visual regression testing (auto-compare with baseline images)
- 🎮 Headless mode support (run game without window)
- 📊 Performance profiling integration
- 🐛 Automatic bug report generation
- 🧪 Property-based testing scenarios
- 📹 Video capture for complex interactions
- 🌐 Multi-game instance testing

## Credits

Built using:
- [Model Context Protocol (MCP)](https://github.com/anthropics/model-context-protocol)
- [Claude Code](https://github.com/anthropics/claude-code)
- [LibGDX](https://libgdx.com/)
- [Gradle](https://gradle.org/)

## License

Part of VoidCodex game project.

---

## 🚀 Ready to Start?

```bash
setup_screenshot_system.bat
```

Then restart Claude Code and say: **"Start the game"**

Happy automated testing! 🤖✨
