package io.github.n3wang.voidcodex.model;

/**
 * Manages game time state (pause, speed).
 */
public class GameTimeState {
    private boolean paused;
    private float timeScale; // 0.5f = slow, 1.0f = normal, 2.0f = fast, 4.0f = very fast
    private boolean commandMode;

    public GameTimeState() {
        this.paused = false;
        this.timeScale = 1.0f;
        this.commandMode = false;
    }

    public void togglePause() {
        paused = !paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setTimeScale(float scale) {
        this.timeScale = Math.max(0.5f, Math.min(4.0f, scale));
    }

    public float getTimeScale() {
        return paused ? 0f : timeScale;
    }

    public void toggleCommandMode() {
        commandMode = !commandMode;
    }

    public boolean isCommandMode() {
        return commandMode;
    }
}

