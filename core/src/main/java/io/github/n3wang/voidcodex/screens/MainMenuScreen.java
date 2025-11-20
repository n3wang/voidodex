package io.github.n3wang.voidcodex.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import io.github.n3wang.voidcodex.VoidCodexGame;
import io.github.n3wang.voidcodex.screens.ShipScreenNew;
import io.github.n3wang.voidcodex.model.Ship;
import io.github.n3wang.voidcodex.model.Sector;
import io.github.n3wang.voidcodex.model.Biome;
import io.github.n3wang.voidcodex.util.BiomeGenerator;

/**
 * Main menu screen.
 */
public class MainMenuScreen extends GameScreen {
    public MainMenuScreen(VoidCodexGame game) {
        super(game);
        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Label title = new Label("VOID CODEX", game.getSkin(), "subtitle");
        title.setAlignment(Align.center);
        table.add(title).padBottom(40f).row();

        TextButton newGameButton = new TextButton("New Game", game.getSkin());
        newGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startNewGame();
            }
        });
        table.add(newGameButton).width(200f).padBottom(10f).row();

        TextButton exitButton = new TextButton("Exit", game.getSkin());
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.exit(0);
            }
        });
        table.add(exitButton).width(200f).row();

        stage.addActor(table);
    }

    private void startNewGame() {
        // Initialize a new game
        Ship starterShip = Ship.createStarterShip();
        game.getGameState().setCurrentShip(starterShip);
        
        // Generate first sector
        Biome firstBiome = BiomeGenerator.generateRandomBiome();
        Sector firstSector = new Sector(0, firstBiome);
        game.getGameState().setCurrentSector(firstSector);
        
        // Go to ship screen
        game.setScreen(new ShipScreenNew(game));
    }
}

