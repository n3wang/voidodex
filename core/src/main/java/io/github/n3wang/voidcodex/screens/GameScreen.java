package io.github.n3wang.voidcodex.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.n3wang.voidcodex.VoidCodexGame;

/**
 * Base class for all game screens.
 */
public abstract class GameScreen implements Screen {
    protected final VoidCodexGame game;
    protected Stage stage;
    protected static final int VIEWPORT_WIDTH = 1280;
    protected static final int VIEWPORT_HEIGHT = 720;

    public GameScreen(VoidCodexGame game) {
        this.game = game;
        this.stage = new Stage(new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
    }

    /**
     * Override this to provide a custom name for debug screenshots.
     * Defaults to the class name.
     */
    protected String getScreenName() {
        return getClass().getSimpleName();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Debug screenshot manager - auto-capture on first render, manual via F12
        io.github.n3wang.voidcodex.util.DebugScreenshotManager.update(getScreenName());

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Override if needed
    }

    @Override
    public void resume() {
        // Override if needed
    }

    @Override
    public void hide() {
        // Override if needed
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}

