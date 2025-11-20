# Automated Testing & Control with Claude Code

## 🎮 Full Game Automation

The enhanced MCP server now gives Claude Code (me!) **complete control** over your game, enabling fully automated testing and debugging **without any user input**.

## 🚀 What I Can Do Automatically

### Build & Launch Control
- ✅ Build the game (with or without clean)
- ✅ Start the game
- ✅ Stop the game
- ✅ Restart the game (with optional rebuild)
- ✅ Navigate to specific screens on launch

### In-Game Control
- ✅ Navigate between screens
- ✅ Simulate clicks (left/right button)
- ✅ Press keyboard keys
- ✅ Capture screenshots at any time
- ✅ Execute predefined test scenarios
- ✅ Monitor game status in real-time

## 📋 Example Workflows

### Workflow 1: Automated Bug Investigation

**You:** "The energy blocks aren't coloring properly - investigate and fix it"

**Me:**
```
1. [Uses restart_game with rebuild=true]
   "Restarting game with latest code..."

2. [Uses start_game with target_screen="ShipScreen"]
   "Game started, navigating to Ship Screen..."

3. [Uses game_command to capture initial state]
   "Capturing initial state..."

4. [Uses game_command to click energy system]
   "Adding power to shields system..."

5. [Uses get_latest_screenshot]
   "Analyzing screenshot... I can see the blocks aren't changing color.
    The issue is in ShipScreenNew.java:896..."

6. [Makes code changes]
   "Fixed! The problem was updateBottomPanel() wasn't being called..."

7. [Uses restart_game with rebuild=true]
   "Rebuilding and restarting to verify fix..."

8. [Uses automate_scenario with scenario="test_energy_blocks"]
   "Running automated test scenario..."

9. [Uses compare_screenshots]
   "Comparing before/after... ✓ Fix confirmed! Energy blocks now
    properly display light grey when powered."
```

**You did nothing!** I handled everything automatically.

### Workflow 2: Screen Navigation Test

**You:** "Test all screens and capture screenshots"

**Me:**
```
[Uses start_game]
[Uses automate_scenario with scenario="navigate_all_screens"]
[Uses list_screenshots]

"Completed! Captured screenshots of:
- Main Menu
- Ship Screen
- Codex Screen
- All screens loaded successfully with no errors."
```

### Workflow 3: Continuous Testing

**You:** "Monitor the game and alert me of any UI issues"

**Me:**
```
[Uses start_game]
[Sets up watch loop]

*Every 30 seconds:*
[Uses watch_screenshots]
[Uses get_latest_screenshot if new]
[Analyzes for issues]

"All clear... All clear...
⚠️  ALERT: Detected text overlap in Codex screen
Screenshot captured: CodexScreen_2025-01-20_14-30.png
Issue: Title overlaps with back button at coordinates (50, 680)"
```

## 🛠️ Available MCP Tools

### Game Control

#### `build_game`
Build the game using Gradle.
```
You: "Build the game"
Me: [Uses build_game]
```

**Options:**
- `clean`: Run clean before build (default: false)

#### `start_game`
Start the game, optionally navigating to a specific screen.
```
You: "Start the game on the ship screen"
Me: [Uses start_game with target_screen="ShipScreen"]
```

**Options:**
- `target_screen`: Screen to navigate to after start
- `wait_for_start`: Wait for game to fully load (default: true)

#### `stop_game`
Stop the currently running game.
```
You: "Stop the game"
Me: [Uses stop_game]
```

#### `restart_game`
Restart the game, optionally rebuilding first.
```
You: "Restart the game with latest changes"
Me: [Uses restart_game with rebuild=true]
```

**Options:**
- `rebuild`: Rebuild before restarting (default: false)
- `target_screen`: Screen to navigate to after restart

### Game Automation

#### `game_command`
Send individual commands to the running game.
```
You: "Click the shields system"
Me: [Uses game_command with command="click", params={x: 100, y: 600}]
```

**Commands:**
- `navigate`: Change screens
  - `params`: `{ screen: "ShipScreen" }`
- `click`: Simulate mouse click
  - `params`: `{ x: 100, y: 200, button: "left" }`
- `press_key`: Simulate keyboard input
  - `params`: `{ key: "F12" }`
- `capture`: Take screenshot
  - `params`: `{ name: "custom_name" }`
- `wait`: Pause (handled by MCP server)
  - `params`: `{ ms: 1000 }`

#### `automate_scenario`
Run predefined test scenarios.
```
You: "Test the energy blocks automatically"
Me: [Uses automate_scenario with scenario="test_energy_blocks"]
```

**Built-in Scenarios:**

**`test_energy_blocks`**
1. Navigate to Ship Screen
2. Capture initial state
3. Click first energy system
4. Capture powered state
5. Click second energy system
6. Capture fully powered state

**`navigate_all_screens`**
1. Visit Main Menu → capture
2. Visit Ship Screen → capture
3. Visit Codex Screen → capture
4. Returns to main menu

**`test_crew_movement`**
1. Navigate to Ship Screen
2. Select crew member
3. Command movement
4. Capture movement progress
5. Verify arrival

**Options:**
- `capture_steps`: Capture screenshot at each step (default: true)

#### `get_game_status`
Get current game status.
```
You: "Is the game running?"
Me: [Uses get_game_status]
    "Game status:
     - Running: true
     - PID: 12345
     - Current Screen: ShipScreenNew
     - FPS: 60"
```

## 💡 Example Conversations

### Example 1: Complete Automated Fix

```
You: The crew movement looks weird, can you check it?

Me: I'll investigate by running the game and testing crew movement.

[Uses restart_game with rebuild=false, target_screen="ShipScreen"]

Game started, navigating to Ship Screen...

[Uses automate_scenario with scenario="test_crew_movement"]

Running crew movement test...

[Uses get_latest_screenshot]

I can see the issue - the crew members are teleporting instead of
smoothly moving. Looking at the movement code...

[Reads ShipScreenNew.java]

The problem is in updateCrewMovement() at line 217. The movement speed
is set but the interpolation isn't being applied correctly.

[Makes fix]

Fixed! The issue was that crew.getMovementProgress() wasn't being used
for rendering interpolation.

[Uses restart_game with rebuild=true]

Rebuilding and restarting...

[Uses automate_scenario with scenario="test_crew_movement"]

[Uses compare_screenshots with before/after]

✓ Verified! Crew now moves smoothly across tiles. The movement animation
shows proper interpolation between positions.
```

### Example 2: Comprehensive Screen Audit

```
You: Audit all screens for visual issues

Me: I'll run through all screens and analyze each one.

[Uses start_game]
[Uses automate_scenario with scenario="navigate_all_screens"]

Auditing screens...

[For each screenshot, uses get_screenshot and analyzes]

**Audit Results:**

✓ Main Menu: Clean, no issues
⚠️  Ship Screen: Energy block coloring issue (already noted)
✗ Codex Screen: Title text overlaps back button
✓ Scenario Selection: No issues

Would you like me to fix the Codex Screen overlap?
```

### Example 3: Performance Testing

```
You: Test if the game runs smoothly with multiple crew members moving

Me: I'll set up a performance test.

[Uses restart_game]
[Uses game_command to spawn multiple crew movements]
[Uses get_game_status repeatedly to monitor FPS]

Running performance test...

Results:
- 1 crew moving: 60 FPS ✓
- 2 crew moving: 60 FPS ✓
- 5 crew moving: 58 FPS ⚠️
- 10 crew moving: 45 FPS ✗

Performance degrades with 5+ crew members. The bottleneck is in
TilePathfinding.findPath() which is called every frame.

Recommendation: Cache pathfinding results or only recalculate when
crew reaches waypoints.
```

## 🔧 Technical Details

### How It Works

```
┌─────────────────────┐
│   Claude Code CLI   │ (You talk to me)
│   (You're here!)    │
└──────────┬──────────┘
           │ MCP Protocol
           ▼
┌─────────────────────┐
│   MCP Server        │ (Node.js)
│   - build_game      │
│   - start_game      │
│   - game_command    │
│   - automate_scenario│
└──────────┬──────────┘
           │ Writes to file
           ▼
┌─────────────────────────────────┐
│  command_queue.json             │
│  { command: "click",            │
│    params: { x: 100, y: 200 }}  │
└──────────┬──────────────────────┘
           │ Read every frame
           ▼
┌─────────────────────────────────┐
│  Game (LibGDX)                  │
│  AutomationHandler.java         │
│  - Reads commands               │
│  - Executes actions             │
│  - Writes status                │
└─────────────────────────────────┘
```

### Command Queue

Commands are passed via `debug_screenshots/command_queue.json`:

```json
{
  "command": "click",
  "params": {
    "x": 100,
    "y": 200,
    "button": "left"
  },
  "timestamp": 1705756800000
}
```

The game reads this file every frame, executes new commands, then deletes the file.

### Game Status

The game writes status to `debug_screenshots/game_status.json`:

```json
{
  "currentScreen": "ShipScreenNew",
  "timestamp": 1705756800000,
  "fps": 60
}
```

This allows the MCP server to know the current game state.

## 🎯 Advanced Usage

### Creating Custom Scenarios

Edit `mcp-screenshot-server/index.js` and add to the `scenarios` object:

```javascript
custom_scenario: [
  { command: 'navigate', params: { screen: 'ShipScreen' } },
  { command: 'wait', params: { ms: 2000 } },
  { command: 'click', params: { x: 100, y: 600 } },
  { command: 'capture', params: { name: 'step1' } },
  // ... more steps
],
```

Then use it:
```
You: "Run custom_scenario"
Me: [Uses automate_scenario with scenario="custom_scenario"]
```

### Chaining Commands

You can ask me to perform complex multi-step operations:

```
You: "Build the game, restart it, test all screens, and report any issues"

Me: [Automatically uses:]
   1. build_game
   2. restart_game
   3. automate_scenario (navigate_all_screens)
   4. Analyzes each screenshot
   5. Generates report
```

### Continuous Integration

Use the MCP server in CI/CD:

```yaml
# .github/workflows/visual-test.yml
- name: Visual Testing
  run: |
    # Start MCP server
    node mcp-screenshot-server/index.js &

    # Run automated tests via Claude Code
    claude code --prompt "Start the game, run all test scenarios, and report any visual regressions"
```

## 🚫 Limitations

- **Timing:** Some operations need delays for UI to settle
- **Coordinates:** Click coordinates are absolute and may need adjustment for different resolutions
- **Complex interactions:** Some UI interactions (drag, multi-touch) not yet supported
- **Game crashes:** If game crashes, MCP server loses control

## 🛡️ Safety

- **Process management:** MCP server tracks game PID and can always kill it
- **Timeouts:** Build commands have 5-minute timeout
- **Error handling:** Failed commands don't crash the system
- **File locking:** Commands use timestamps to avoid race conditions

## 📚 Related Documentation

- **SCREENSHOT_CLAUDE_INTEGRATION.md** - Screenshot viewing system
- **DEBUG_SCREENSHOTS.md** - Screenshot capture system
- **mcp-screenshot-server/README.md** - MCP server details

---

## 🚀 Quick Start

**Already set up?** Just start talking:

```
You: "Restart the game and test the energy blocks"
```

**Not set up?** Run:

```bash
setup_screenshot_system.bat
```

Then restart Claude Code and you're ready!

---

**The future is here**: Fully automated game testing and debugging with natural language! 🤖
