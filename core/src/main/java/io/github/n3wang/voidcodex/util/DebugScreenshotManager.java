package io.github.n3wang.voidcodex.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Debug utility for automatically capturing screenshots of different screens.
 * Features:
 * - Auto-capture on screen changes
 * - Manual capture via hotkey (F12)
 * - Organized folder structure with timestamps
 * - Screen name tracking to avoid duplicate captures
 */
public class DebugScreenshotManager {
    private static boolean DEBUG_MODE = true; // Toggle this to enable/disable
    private static final String SCREENSHOT_DIR = "debug_screenshots/";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");

    private static String currentScreenName = "";
    private static Set<String> capturedScreens = new HashSet<>();
    private static boolean manualCaptureRequested = false;

    /**
     * Call this at the start of each screen's render() method
     */
    public static void update(String screenName) {
        if (!DEBUG_MODE) return;

        // Check for manual screenshot hotkey (F12)
        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            manualCaptureRequested = true;
            Gdx.app.log("DebugScreenshot", "Manual screenshot requested");
        }

        // Auto-capture on screen change (first time visiting a screen)
        if (!screenName.equals(currentScreenName)) {
            currentScreenName = screenName;
            if (!capturedScreens.contains(screenName)) {
                capturedScreens.add(screenName);
                captureScreenshot(screenName, "auto");
                Gdx.app.log("DebugScreenshot", "Auto-captured: " + screenName);
            }
        }

        // Manual capture
        if (manualCaptureRequested) {
            captureScreenshot(currentScreenName, "manual");
            manualCaptureRequested = false;
            Gdx.app.log("DebugScreenshot", "Manual capture saved: " + currentScreenName);
        }
    }

    /**
     * Capture a screenshot and save it to the debug folder
     */
    private static void captureScreenshot(String screenName, String type) {
        try {
            // Create screenshot directory if it doesn't exist
            FileHandle screenshotDir = Gdx.files.local(SCREENSHOT_DIR);
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            // Generate filename with timestamp
            String timestamp = dateFormat.format(new Date());
            String filename = String.format("%s_%s_%s.png", screenName, type, timestamp);
            FileHandle file = Gdx.files.local(SCREENSHOT_DIR + filename);

            // Capture the screen
            byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0,
                Gdx.graphics.getBackBufferWidth(),
                Gdx.graphics.getBackBufferHeight(),
                true);

            // Create pixmap and save
            Pixmap pixmap = new Pixmap(
                Gdx.graphics.getBackBufferWidth(),
                Gdx.graphics.getBackBufferHeight(),
                Pixmap.Format.RGBA8888
            );
            BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
            PixmapIO.writePNG(file, pixmap);
            pixmap.dispose();

            Gdx.app.log("DebugScreenshot", "Screenshot saved: " + file.path());

            // Also create a "latest" symlink for easy access
            if (type.equals("manual")) {
                FileHandle latestFile = Gdx.files.local(SCREENSHOT_DIR + screenName + "_LATEST.png");
                // For desktop, we'll just copy it (symlinks are OS-specific)
                file.copyTo(latestFile);
            }

        } catch (Exception e) {
            Gdx.app.error("DebugScreenshot", "Failed to capture screenshot", e);
        }
    }

    /**
     * Enable or disable debug mode at runtime
     */
    public static void setDebugMode(boolean enabled) {
        DEBUG_MODE = enabled;
        if (enabled) {
            Gdx.app.log("DebugScreenshot", "Debug screenshot mode ENABLED (Press F12 for manual capture)");
        } else {
            Gdx.app.log("DebugScreenshot", "Debug screenshot mode DISABLED");
        }
    }

    /**
     * Check if debug mode is enabled
     */
    public static boolean isDebugMode() {
        return DEBUG_MODE;
    }

    /**
     * Clear the captured screens set to allow re-capturing auto screenshots
     */
    public static void resetCapturedScreens() {
        capturedScreens.clear();
        Gdx.app.log("DebugScreenshot", "Captured screens list reset");
    }

    /**
     * Get the screenshot directory path
     */
    public static String getScreenshotDirectory() {
        return Gdx.files.local(SCREENSHOT_DIR).path();
    }
}
