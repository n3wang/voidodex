# Screenshot System + Automation - Quick Start

## 🚀 One-Time Setup (5 minutes)

```bash
# Run the setup script
setup_screenshot_system.bat

# Restart your terminal
# Then test in Claude Code:
claude code
> "List my debug screenshots"
> "Start the game"  # New! Full automation
```

Done! ✅

## 🎮 NEW: Full Game Automation

I can now **control the entire game** automatically:
- ✅ Build, start, stop, restart the game
- ✅ Navigate to any screen
- ✅ Simulate clicks and keyboard input
- ✅ Run automated test scenarios
- ✅ **Zero user input required!**

## 📸 Daily Usage

### Capture Screenshots

**In Game:**
- Auto-captures each screen on first visit
- Press **F12** for manual capture
- Screenshots saved to `debug_screenshots/`

### Review with Claude

**In Claude Code (this terminal!):**

```
You: "Show me the latest screenshot"
You: "Check for new screenshots"
You: "Compare screenshot A and B"
You: "Review all screenshots for bugs"
```

That's it! I'll automatically access and analyze them.

## 🎯 Common Tasks

| Task                  | Command                      | Automation Level      |
| --------------------- | ---------------------------- | --------------------- |
| Manual testing        | Press **F12** in game        | 👤 Manual              |
| Review screenshots    | "Show latest screenshot"     | 🤖 Automated           |
| **Auto-test feature** | **"Test energy blocks"**     | 🚀 **Fully Automated** |
| **Fix + verify**      | **"Fix the bug and verify"** | 🚀 **Fully Automated** |
| **Full QA pass**      | **"Test all screens"**       | 🚀 **Fully Automated** |

## 🚀 Automation Examples

### Before (Manual)
```
You: [Run game manually]
You: [Click through UI]
You: [Press F12]
You: [Tell me to check screenshot]
Me: [Analyze and suggest fix]
You: [Edit code]
You: [Rebuild and rerun game]
You: [Test again]
```

### Now (Automated)
```
You: "The energy blocks aren't coloring - investigate and fix"

Me: [Starts game automatically]
    [Tests feature]
    [Captures screenshots]
    [Identifies bug]
    [Fixes code]
    [Rebuilds]
    [Verifies fix]
    "✓ Fixed! Energy blocks now color correctly."
```

**You literally did nothing but ask!**

## 🐛 Troubleshooting

**MCP server not working?**
```bash
cd mcp-screenshot-server
npm install
node test.js
```

**No screenshots found?**
- Check `debug_screenshots/` folder exists
- Run game and press F12
- Check `DebugScreenshotManager.java` has `DEBUG_MODE = true`

**Need to reconfigure?**
```bash
cd mcp-screenshot-server
install.bat
```

## 📖 Full Documentation

- **SCREENSHOT_CLAUDE_INTEGRATION.md** - Complete guide
- **mcp-screenshot-server/README.md** - MCP server details
- **DEBUG_SCREENSHOTS.md** - Screenshot system details

## 💡 Pro Tips

### With Automation
1. **Full auto-fix:** "Fix the energy block bug" - I'll test, fix, and verify
2. **Auto-testing:** "Run the crew movement test" - automated scenario execution
3. **Hands-free QA:** "Test all screens and report issues" - complete audit
4. **Smart rebuilds:** "Restart with latest changes" - rebuild + restart + test

### Manual Mode (Still Available!)
1. **Continuous review:** Say "Watch for new screenshots" and I'll monitor
2. **Before/after:** Capture before fixing, after fixing, then compare
3. **Multiple screens:** I can filter by screen name: "Show latest from ShipScreen"
4. **Direct questions:** "The energy blocks aren't coloring - check the latest screenshot"

## 🎮 Automation Commands

| What to Say                | What Happens                              |
| -------------------------- | ----------------------------------------- |
| "Start the game"           | Game launches automatically               |
| "Restart the game"         | Game restarts (useful after code changes) |
| "Rebuild and restart"      | Full rebuild + restart                    |
| "Test energy blocks"       | Runs automated test scenario              |
| "Test all screens"         | Navigates and captures all screens        |
| "Click the shields system" | Simulates click at that location          |
| "Navigate to ship screen"  | Switches to ship screen                   |
| "Is the game running?"     | Checks game status                        |

---

**First time?** Run: `setup_screenshot_system.bat`

**Already set up?** Just talk to me about screenshots naturally!
