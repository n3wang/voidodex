# Debug Screenshot System

Automatically capture and analyze game screenshots with Claude for debugging.

## Features

✨ **Auto-capture** - Takes screenshot when you first visit each screen
🎮 **Manual capture** - Press `F12` anytime to capture current screen
📁 **Organized** - Screenshots saved with timestamps in `debug_screenshots/`
🤖 **AI Review** - Automated Claude analysis for bugs and improvements

## Quick Start

### 1. Enable Debug Mode (Already Done!)

The `DebugScreenshotManager` is already integrated into your screens. Just run your game!

### 2. Capture Screenshots

**Automatic:** Screenshots are captured the first time you visit each screen
**Manual:** Press `F12` anytime during gameplay

Screenshots saved to: `debug_screenshots/`

### 3. Review with Claude

#### Option A: Batch Review (All screenshots at once)
```bash
# Setup (first time only)
pip install anthropic
export ANTHROPIC_API_KEY=your_api_key_here

# Run review
python debug_screenshot_reviewer.py
```

View the generated report in `debug_reports/review_TIMESTAMP.md`

#### Option B: Watch Mode (Real-time analysis)
```bash
python debug_screenshot_reviewer.py --watch
```

This will watch for new screenshots and analyze them immediately!

#### Option C: Manual Review with Claude Code
```bash
# Just tell Claude to look at them
claude code
> "Check the screenshots in debug_screenshots/ for any UI issues"
```

## Configuration

### Enable/Disable Debug Mode

In your code:
```java
// Enable
DebugScreenshotManager.setDebugMode(true);

// Disable
DebugScreenshotManager.setDebugMode(false);
```

Or edit `DebugScreenshotManager.java` line 20:
```java
private static boolean DEBUG_MODE = false; // Set to false to disable
```

### Change Screenshot Directory

Edit `DebugScreenshotManager.java` line 21:
```java
private static final String SCREENSHOT_DIR = "your_folder/";
```

### Reset Auto-Captures

To re-capture screens that were already captured:
```java
DebugScreenshotManager.resetCapturedScreens();
```

## Adding to Other Screens

Add this line to any screen's `render()` method:

```java
@Override
public void render(float delta) {
    DebugScreenshotManager.update("ScreenName");

    // ... rest of your render code
}
```

Example screens to add:
- `CodexScreen` → "CodexScreen"
- `MainMenuScreen` → "MainMenu"
- `CombatScreen` → "CombatScreen"

## Keyboard Shortcuts

| Key | Action |
|-----|--------|
| `F12` | Capture screenshot manually |

## File Structure

```
voidodex/
├── debug_screenshots/          # Screenshots saved here
│   ├── ShipScreen_auto_2025-01-20_14-30-00.png
│   ├── ShipScreen_manual_2025-01-20_14-35-22.png
│   └── ShipScreen_LATEST.png  # Latest manual capture
│
├── debug_reports/              # Claude analysis reports
│   └── review_2025-01-20_14-40-00.md
│
└── debug_screenshot_reviewer.py  # Review script
```

## Example Workflow

1. **Development:**
   ```bash
   # Run your game
   ./gradlew desktop:run

   # Navigate through screens (auto-captured)
   # Press F12 when you see something wrong
   ```

2. **Review:**
   ```bash
   # Get Claude's analysis
   python debug_screenshot_reviewer.py

   # Or use watch mode while developing
   python debug_screenshot_reviewer.py --watch
   ```

3. **Fix Issues:**
   ```bash
   # Review the report
   cat debug_reports/review_*.md

   # Make fixes
   # Capture again with F12
   # Compare
   ```

## Tips

💡 **Before major changes:** Capture all screens as "before" reference
💡 **After bug fixes:** Capture again to verify
💡 **Watch mode:** Great for rapid iteration - see Claude's feedback instantly
💡 **Latest files:** Manual captures create `_LATEST.png` for quick comparison

## Troubleshooting

**No screenshots appearing?**
- Check that `DEBUG_MODE = true` in `DebugScreenshotManager.java`
- Look for log messages: "Auto-captured: ScreenName"
- Check the `debug_screenshots/` folder exists

**Python script not working?**
- Install anthropic: `pip install anthropic`
- Set API key: `export ANTHROPIC_API_KEY=sk-...`
- Check Python 3 is installed: `python --version`

**F12 not working?**
- Make sure the game window has focus
- Check logs for "Manual screenshot requested"
- Try a different key by editing `Input.Keys.F12` in the code

## Advanced: Custom Analysis Prompts

Edit `debug_screenshot_reviewer.py` and modify the prompt:

```python
prompt = """Analyze this UI specifically for:
1. Color contrast issues
2. Text readability
3. Button placement
4. Energy block visibility (your current issue!)
"""

analyze_screenshot(client, image_path, prompt)
```

## Getting Your Claude API Key

1. Go to: https://console.anthropic.com/
2. Sign in/sign up
3. Go to API Keys section
4. Create a new key
5. Set it: `export ANTHROPIC_API_KEY=your_key_here`

---

**Need help?** Just ask Claude Code! Send the screenshot path:
```
Check this screenshot for the energy block issue: debug_screenshots/ShipScreen_LATEST.png
```
