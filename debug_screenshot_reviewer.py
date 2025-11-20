#!/usr/bin/env python3
"""
Debug Screenshot Reviewer for Claude
=====================================
This script automatically sends screenshots to Claude for review and debugging.

Setup:
1. Install: pip install anthropic
2. Set environment variable: ANTHROPIC_API_KEY=your_key_here
3. Run: python debug_screenshot_reviewer.py

Usage:
- Reviews all screenshots in debug_screenshots/ folder
- Creates a report with Claude's analysis
- Can watch for new screenshots in real-time
"""

import os
import sys
import base64
import time
from pathlib import Path
from datetime import datetime

try:
    import anthropic
except ImportError:
    print("ERROR: anthropic package not installed")
    print("Install with: pip install anthropic")
    sys.exit(1)

# Configuration
SCREENSHOT_DIR = "debug_screenshots"
REPORT_DIR = "debug_reports"
API_KEY = os.environ.get("ANTHROPIC_API_KEY")

def encode_image(image_path):
    """Encode image to base64 for Claude API"""
    with open(image_path, "rb") as f:
        return base64.standard_b64encode(f.read()).decode("utf-8")

def get_image_media_type(image_path):
    """Get media type based on file extension"""
    ext = Path(image_path).suffix.lower()
    media_types = {
        ".png": "image/png",
        ".jpg": "image/jpeg",
        ".jpeg": "image/jpeg",
        ".gif": "image/gif",
        ".webp": "image/webp"
    }
    return media_types.get(ext, "image/png")

def analyze_screenshot(client, image_path, prompt=None):
    """Send screenshot to Claude for analysis"""
    if prompt is None:
        prompt = """Analyze this game screenshot for:
1. UI/UX issues (alignment, visibility, colors)
2. Visual bugs or glitches
3. Layout problems
4. Suggested improvements
5. Any elements that look broken or incorrect

Be specific and actionable in your feedback."""

    print(f"Analyzing: {image_path}")

    try:
        image_data = encode_image(image_path)
        media_type = get_image_media_type(image_path)

        message = client.messages.create(
            model="claude-3-5-sonnet-20241022",
            max_tokens=1024,
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "image",
                            "source": {
                                "type": "base64",
                                "media_type": media_type,
                                "data": image_data,
                            },
                        },
                        {
                            "type": "text",
                            "text": prompt
                        }
                    ],
                }
            ],
        )

        return message.content[0].text

    except Exception as e:
        return f"ERROR analyzing screenshot: {str(e)}"

def review_all_screenshots(watch_mode=False):
    """Review all screenshots in the debug folder"""

    if not API_KEY:
        print("ERROR: ANTHROPIC_API_KEY environment variable not set")
        print("Set it with: export ANTHROPIC_API_KEY=your_key_here")
        return

    client = anthropic.Anthropic(api_key=API_KEY)

    # Create report directory
    os.makedirs(REPORT_DIR, exist_ok=True)

    # Find all screenshots
    screenshot_path = Path(SCREENSHOT_DIR)
    if not screenshot_path.exists():
        print(f"Screenshot directory '{SCREENSHOT_DIR}' does not exist")
        print("Run your game with debug mode to generate screenshots")
        return

    screenshots = sorted(screenshot_path.glob("*.png"))

    if not screenshots:
        print(f"No screenshots found in '{SCREENSHOT_DIR}'")
        return

    print(f"Found {len(screenshots)} screenshots")
    print("=" * 60)

    # Create report
    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    report_file = Path(REPORT_DIR) / f"review_{timestamp}.md"

    with open(report_file, "w") as report:
        report.write(f"# Debug Screenshot Review\n")
        report.write(f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")

        for screenshot in screenshots:
            print(f"\n📸 Reviewing: {screenshot.name}")

            # Analyze with Claude
            analysis = analyze_screenshot(client, str(screenshot))

            # Write to report
            report.write(f"## {screenshot.name}\n\n")
            report.write(f"![Screenshot]({screenshot.absolute()})\n\n")
            report.write(f"### Claude's Analysis:\n\n")
            report.write(analysis)
            report.write("\n\n---\n\n")

            # Print summary
            print(f"✓ Analysis complete")
            print(f"Preview: {analysis[:100]}...")

    print("\n" + "=" * 60)
    print(f"✓ Review complete! Report saved to: {report_file}")
    print(f"View with: cat {report_file}")

def watch_for_new_screenshots():
    """Watch for new screenshots and analyze them in real-time"""
    print("👁️  Watching for new screenshots... (Press Ctrl+C to stop)")

    if not API_KEY:
        print("ERROR: ANTHROPIC_API_KEY not set")
        return

    client = anthropic.Anthropic(api_key=API_KEY)
    analyzed = set()

    screenshot_path = Path(SCREENSHOT_DIR)
    screenshot_path.mkdir(exist_ok=True)

    try:
        while True:
            screenshots = set(screenshot_path.glob("*.png"))
            new_screenshots = screenshots - analyzed

            for screenshot in new_screenshots:
                print(f"\n🆕 New screenshot detected: {screenshot.name}")
                analysis = analyze_screenshot(client, str(screenshot))
                print(f"\n📝 Claude's Analysis:\n{analysis}\n")
                analyzed.add(screenshot)

            time.sleep(2)  # Check every 2 seconds

    except KeyboardInterrupt:
        print("\n\n👋 Stopped watching")

if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="Review game screenshots with Claude")
    parser.add_argument("--watch", "-w", action="store_true",
                       help="Watch for new screenshots and analyze them in real-time")
    parser.add_argument("--screen", "-s", type=str,
                       help="Analyze only screenshots matching this screen name")

    args = parser.parse_args()

    if args.watch:
        watch_for_new_screenshots()
    else:
        review_all_screenshots()
