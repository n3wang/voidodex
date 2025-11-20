# Screenshot + Claude Code Integration

Complete setup for automatic screenshot review using Claude Code CLI with MCP server.

## 🎯 What This Enables

Instead of manually sending me screenshot paths, I can **automatically**:
- 📋 List all your debug screenshots
- 👀 View and analyze the latest screenshot
- 🔍 Compare before/after screenshots
- ⚡ Watch for new screenshots in real-time
- 🐛 Automatically review UI bugs as you capture them

**All without leaving Claude Code!**

## 📦 Components

### 1. Screenshot Capture (Java)
- **`DebugScreenshotManager.java`** - Captures screenshots in your game
- Press **F12** for manual capture
- Auto-captures each screen on first visit
- Saves to `debug_screenshots/`

### 2. MCP Server (Node.js)
- **`mcp-screenshot-server/`** - Exposes screenshots as tools for Claude
- Connects to Claude Code via Model Context Protocol
- Provides tools for listing, viewing, and comparing screenshots

### 3. Claude Code Integration
- You talk to me normally in Claude Code
- I automatically use the MCP server tools when you ask about screenshots
- No need to manually send file paths!

## 🚀 Quick Setup

### Step 1: Install MCP Server

```bash
cd mcp-screenshot-server
install.bat
```

This will:
- Install Node.js dependencies
- Configure Claude Code automatically
- Create the config file if needed

### Step 2: Restart Claude Code

Close your terminal and restart:

```bash
claude code
```

### Step 3: Test It!

In Claude Code, ask me:

```
List my debug screenshots
```

If I respond with a list of screenshots, it's working! 🎉

## 💬 How to Use

### Basic Commands

Just talk to me naturally:

```
You: "Show me the latest screenshot"
→ I'll use get_latest_screenshot tool and analyze it

You: "List all screenshots from the ship screen"
→ I'll use list_screenshots with a filter

You: "Compare the last two screenshots"
→ I'll use compare_screenshots

You: "Are there any new screenshots?"
→ I'll use watch_screenshots
```

### Development Workflow

**1. Normal Development:**
```bash
# Terminal 1: Run your game
gradlew desktop:run

# Terminal 2: Claude Code
claude code

# In Claude Code:
You: "Watch for new screenshots and review them for bugs"
```

Now as you play and press F12, I'll automatically review each screenshot!

**2. Bug Investigation:**
```
# Capture screenshot when you see the bug
[Press F12 in game]

# In Claude Code:
You: "Check the latest screenshot - the energy blocks aren't coloring properly"

Me: [Uses get_latest_screenshot, analyzes it]
    "I can see the issue. Looking at the screenshot, the energy blocks
     are all dark grey even though the system shows power level > 0.
     The problem is in ShipScreenNew.java:896..."
```

**3. Before/After Comparison:**
```
# Capture before fix
[F12 in game]

# Make your code changes

# Capture after fix
[F12 in game]

# Compare
You: "Compare the before and after screenshots"

Me: [Uses compare_screenshots]
    "Great! The energy blocks are now properly colored in the second
     screenshot. The powered blocks show as light grey (0.9f) as expected..."
```

## 🛠️ Available Commands

| What You Say | What I Do |
|--------------|-----------|
| "List screenshots" | Show all available screenshots |
| "Show latest screenshot" | Display and analyze most recent |
| "Show latest ship screen" | Display latest from specific screen |
| "Get screenshot X" | Show specific screenshot by name |
| "Check for new screenshots" | Find screenshots since last check |
| "Compare X and Y" | Show both screenshots side-by-side |
| "Review all screenshots" | Analyze each screenshot for issues |
| "Watch for screenshots" | Monitor for new captures |

## 🔧 Configuration

### Claude Code Config Location

The MCP server is configured in:
- **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`
- **Mac/Linux:** `~/.config/Claude/claude_desktop_config.json`

Should look like:
```json
{
  "mcpServers": {
    "voidcodex-screenshots": {
      "command": "node",
      "args": ["E:\\Documents\\GitHub\\voidodex\\mcp-screenshot-server\\index.js"]
    }
  }
}
```

### Multiple MCP Servers

You can have multiple MCP servers! For example:

```json
{
  "mcpServers": {
    "voidcodex-screenshots": {
      "command": "node",
      "args": ["E:\\Documents\\GitHub\\voidodex\\mcp-screenshot-server\\index.js"]
    },
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "E:\\Documents\\GitHub\\voidodex"]
    },
    "git": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-git"]
    }
  }
}
```

Then I can access screenshots, files, AND git simultaneously!

## 📝 Example Session

Here's a real conversation flow:

```
You: Hey Claude, I'm seeing an issue with the energy blocks not changing color

Me: Let me check your latest screenshot. [Uses get_latest_screenshot]

    I can see the ship screen with the energy systems at the bottom.
    You're right - all the power blocks appear to be the same dark grey
    color (0.15f, 0.15f, 0.15f) even though some systems show they have
    power allocated.

    The issue is in ShipScreenNew.java:896. The block color is based on
    `currentPowerLevel`, but this variable is captured at panel creation
    time and never updated. When you change power allocation, the panel
    is recreated but not properly replaced in the UI.

    Would you like me to fix this?

You: Yes please!

Me: [Makes the fix, explains changes]

    Fixed! The issue was that createBottomPanel() wasn't updating the
    existing panel. I've created an updateBottomPanel() method that
    clears and rebuilds the panel contents in-place.

    Try it now - press F12 when you see the powered systems.

You: [F12 in game after testing]
    Check the new screenshot - is it fixed?

Me: [Uses get_latest_screenshot]

    Perfect! The energy blocks are now showing the correct colors:
    - Unpowered: Dark grey (0.15f)
    - Powered: Light grey/white (0.9f)

    The fix is working correctly. You can see the shields system has
    3 blocks lit up, matching its power level.

You: Awesome! Can you compare the before and after?

Me: [Uses compare_screenshots with the two images]

    Here's the comparison:

    BEFORE: All blocks dark grey regardless of power level
    AFTER: Powered blocks are bright, unpowered are dark

    The visual distinction is much clearer now. Users will be able
    to see at a glance which systems are receiving power.
```

## 🐛 Troubleshooting

### "Unknown tool: list_screenshots"

The MCP server isn't connected. Check:
1. Did you run `install.bat`?
2. Is the path in `claude_desktop_config.json` correct?
3. Did you restart Claude Code?

Test manually:
```bash
node mcp-screenshot-server/index.js
# Should print: "VoidCodex Screenshot MCP Server running on stdio"
# Press Ctrl+C to stop
```

### "No screenshots found"

1. Make sure debug mode is enabled in `DebugScreenshotManager.java`
2. Run your game and press F12
3. Check `debug_screenshots/` folder exists and has .png files

### MCP Server Won't Start

```bash
# Check Node.js installed
node --version

# Reinstall dependencies
cd mcp-screenshot-server
npm install

# Test
node test.js
```

## 🎓 How It Works

```
┌─────────────────┐
│   Your Game     │
│  (LibGDX/Java)  │
└────────┬────────┘
         │ F12 pressed
         ▼
┌─────────────────────────┐
│ DebugScreenshotManager  │
│  Captures & saves PNG   │
└────────┬────────────────┘
         │
         ▼
┌──────────────────────┐
│  debug_screenshots/  │
│   screenshot.png     │
└────────┬─────────────┘
         │
         │ MCP Server watches
         ▼
┌──────────────────────────┐
│  MCP Screenshot Server   │
│  (Node.js)               │
│  Exposes tools:          │
│  - list_screenshots      │
│  - get_latest           │
│  - compare_screenshots   │
└────────┬─────────────────┘
         │ stdio transport
         ▼
┌──────────────────────────┐
│     Claude Code CLI      │
│  (You're here!)          │
└────────┬─────────────────┘
         │
         ▼
┌──────────────────────────┐
│        Claude            │
│  (Me! I can see          │
│   your screenshots       │
│   and analyze them)      │
└──────────────────────────┘
```

When you say "show me the latest screenshot":
1. I call the `get_latest_screenshot` tool
2. MCP server reads the image from `debug_screenshots/`
3. Returns it as base64-encoded data
4. I receive it and can analyze the image
5. I provide feedback on what I see

## 🚀 Advanced Usage

### Auto-Review Script

Create `auto_review.bat`:
```bash
@echo off
start "VoidCodex Game" gradlew desktop:run
timeout /t 5
claude code --prompt "Watch for new screenshots and review each one for UI/UX issues"
```

### CI/CD Integration

```yaml
# .github/workflows/screenshot-review.yml
name: Screenshot Review

on: [pull_request]

jobs:
  review:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Run game and capture screenshots
        run: ./gradlew desktop:run --headless
      - name: Review with Claude
        env:
          ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }}
        run: |
          claude code --prompt "Review all screenshots in debug_screenshots/ for visual regressions"
```

### Custom Watch Loop

```python
# watch_and_review.py
import subprocess
import time

while True:
    result = subprocess.run([
        'claude', 'code', '--prompt',
        'Check for new screenshots and review them'
    ], capture_output=True, text=True)

    print(result.stdout)
    time.sleep(10)  # Check every 10 seconds
```

## 📚 Learn More

- [Model Context Protocol](https://github.com/anthropics/model-context-protocol)
- [Claude Code](https://github.com/anthropics/claude-code)
- [MCP Servers](https://github.com/anthropics/mcp-servers)

---

**Ready to get started?**

```bash
cd mcp-screenshot-server
install.bat
```

Then restart Claude Code and ask me: **"List my debug screenshots"**

Happy debugging! 🐛✨
