# VoidCodex Screenshot MCP Server

MCP (Model Context Protocol) server that gives Claude Code direct access to your debug screenshots for automatic analysis and review.

## What This Does

- 🔌 Integrates directly with Claude Code CLI (the one you're using right now!)
- 📸 Exposes your debug screenshots as tools Claude can use
- 🤖 Enables automated screenshot review without leaving your workflow
- 👁️ Can watch for new screenshots in real-time

## Quick Setup

### 1. Install Dependencies

```bash
cd mcp-screenshot-server
npm install
```

### 2. Configure Claude Code

Add this to your Claude Code configuration file:

**Windows:** `%APPDATA%\Claude\claude_desktop_config.json`
**Mac/Linux:** `~/.config/Claude/claude_desktop_config.json`

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

⚠️ **Important:** Replace the path with your actual project path!

### 3. Restart Claude Code

Close and reopen your terminal, then restart Claude Code:

```bash
claude code
```

### 4. Test It!

In Claude Code, just ask me:

```
List my debug screenshots
```

Or:

```
Show me the latest screenshot and analyze it for UI issues
```

## Available Tools

Once connected, I can use these tools automatically:

### `list_screenshots`
Lists all available screenshots with metadata
```
You: "List all screenshots from ShipScreen"
```

### `get_latest_screenshot`
Gets the most recent screenshot for analysis
```
You: "Show me the latest screenshot"
```

### `get_screenshot`
Gets a specific screenshot by filename
```
You: "Analyze ShipScreenNew_manual_2025-01-20_14-30-00.png"
```

### `watch_screenshots`
Checks for new screenshots since last check
```
You: "Check for new screenshots"
```

### `compare_screenshots`
Compares two screenshots side-by-side
```
You: "Compare the before and after screenshots"
```

## Example Workflow

### Automatic Bug Review

1. **Run your game and capture screenshots:**
   ```bash
   gradlew desktop:run
   # Press F12 when you see the energy block issue
   ```

2. **Ask Claude to review:**
   ```
   You: "Check for new screenshots and analyze them for UI bugs"

   Claude: *uses watch_screenshots tool*
   Claude: *uses get_latest_screenshot tool*
   Claude: "I can see the energy blocks issue. The blocks aren't
           changing color because..."
   ```

3. **Make fixes, capture again, compare:**
   ```
   You: "Compare the before and after screenshots"

   Claude: *uses compare_screenshots tool*
   Claude: "Great! The energy blocks are now properly colored..."
   ```

### Continuous Monitoring

In one terminal:
```bash
# Run your game
gradlew desktop:run
```

In Claude Code terminal:
```
You: "Watch for new screenshots and review them automatically"

Claude: *periodically checks with watch_screenshots*
Claude: *analyzes each new screenshot*
Claude: *provides feedback on UI issues*
```

## Configuration

### Change Screenshot Directory

Edit `index.js` line 28:
```javascript
const SCREENSHOT_DIR = path.join(__dirname, '..', 'your_custom_folder');
```

### Add Custom Tools

You can extend the server by adding new tools in `index.js`. See the MCP SDK docs: https://github.com/anthropics/model-context-protocol

## Troubleshooting

**"Server not found" error:**
- Check the path in `claude_desktop_config.json` is correct
- Make sure you ran `npm install` in the mcp-screenshot-server folder
- Restart Claude Code completely

**"No screenshots found":**
- Make sure you ran the game with debug mode enabled
- Check `debug_screenshots/` folder exists and has .png files
- The folder should be at the same level as mcp-screenshot-server/

**MCP server not loading:**
- Check Node.js is installed: `node --version`
- Check the server manually: `node index.js` (should print "Server running")
- Check Claude Code logs for errors

## Advanced Usage

### Auto-Review on Game Run

Create a wrapper script that:
1. Starts the game
2. Tells Claude to watch for screenshots
3. Provides real-time feedback

```bash
# Start game in background
gradlew desktop:run &

# Tell Claude to watch
echo "Watch for new screenshots and review them" | claude code
```

### Integration with Git Hooks

Add to `.git/hooks/pre-commit`:
```bash
#!/bin/bash
# Capture screenshots before commit for documentation
# ... game screenshot capture logic ...

# Ask Claude to review
echo "Review recent screenshots for any issues before commit" | claude code
```

## How It Works

1. **MCP Server:** Node.js server that exposes screenshot tools
2. **Claude Code:** Connects to MCP server via stdio transport
3. **When you ask about screenshots:** I automatically use the appropriate tool
4. **Images returned:** I can see and analyze them directly

The beauty is **you don't need to manually send me file paths** - I can discover and analyze screenshots automatically!

## Learn More

- [Model Context Protocol](https://github.com/anthropics/model-context-protocol)
- [Claude Code Documentation](https://github.com/anthropics/claude-code)
- [MCP SDK](https://github.com/anthropics/mcp-sdk)

---

**Pro Tip:** You can keep this server running alongside other MCP servers (like file system, git, etc.) and I'll have access to all of them simultaneously!
