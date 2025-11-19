package io.github.n3wang.voidcodex;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private VoidCodexGame game;

    @Override
    public void create() {
        game = new VoidCodexGame();
        game.create();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        game.render();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        game.resize(width, height);
    }

    @Override
    public void pause() {
        game.pause();
    }

    @Override
    public void resume() {
        game.resume();
    }

    @Override
    public void dispose() {
        game.dispose();
    }
}