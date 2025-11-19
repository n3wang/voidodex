package io.github.n3wang.voidcodex;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.github.n3wang.voidcodex.screens.MainMenuScreen;
import io.github.n3wang.voidcodex.screens.ShipScreen;

/**
 * Main game class that manages screens and global resources.
 */
public class VoidCodexGame extends Game {
    private Skin skin;
    private GameState gameState;

    @Override
    public void create() {
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        gameState = new GameState();
        
        // Create a simple default-round drawable
        createDefaultRoundDrawable();
        
        // Start with main menu
        setScreen(new MainMenuScreen(this));
    }

    private Drawable defaultRoundDrawable;
    
    private void createDefaultRoundDrawable() {
        // Create the drawable once and cache it
        if (defaultRoundDrawable == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 1f)); // Dark gray
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            defaultRoundDrawable = new TextureRegionDrawable(new TextureRegion(texture));
            skin.add("default-round", defaultRoundDrawable);
        }
    }

    /**
     * Gets a drawable, creating default-round if it doesn't exist.
     */
    public Drawable getDrawable(String name) {
        if ("default-round".equals(name)) {
            // Always ensure it's created
            createDefaultRoundDrawable();
            // Return the cached drawable or get from skin
            return defaultRoundDrawable != null ? defaultRoundDrawable : skin.getDrawable(name);
        }
        return skin.getDrawable(name);
    }

    public Skin getSkin() {
        return skin;
    }

    public GameState getGameState() {
        return gameState;
    }

    @Override
    public void dispose() {
        if (skin != null) {
            skin.dispose();
        }
        if (gameState != null) {
            gameState.dispose();
        }
    }
}

