package io.github.n3wang.voidcodex.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import io.github.n3wang.voidcodex.VoidCodexGame;

/**
 * Merchant screen for trading and resource management.
 * Placeholder for development.
 */
public class MerchantScreen extends GameScreen {
    public MerchantScreen(VoidCodexGame game) {
        super(game);
        createUI();
    }

    private void createUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(20f);

        Label title = new Label("MERCHANT STATION", game.getSkin(), "subtitle");
        title.setAlignment(Align.center);
        mainTable.add(title).padBottom(20f).row();

        Table contentTable = new Table();
        contentTable.setBackground(game.getDrawable("default-round"));
        contentTable.pad(20f);

        Label desc = new Label("Merchant Screen - Under Development\n\n" +
                "This screen will allow you to:\n" +
                "- Buy and sell resources\n" +
                "- Purchase ship upgrades\n" +
                "- Hire crew members\n" +
                "- Trade scrap for fuel and supplies", game.getSkin());
        desc.setAlignment(Align.center);
        desc.setWrap(true);
        contentTable.add(desc).width(600f).row();

        mainTable.add(contentTable).padBottom(20f).row();

        // Resources display
        Table resourcesTable = new Table();
        resourcesTable.setBackground(game.getDrawable("default-round"));
        resourcesTable.pad(10f);

        var ship = game.getGameState().getCurrentShip();
        Label resources = new Label("Scrap: " + ship.getScrap() + " | Fuel: " + ship.getFuel(), game.getSkin());
        resourcesTable.add(resources).row();

        mainTable.add(resourcesTable).padBottom(20f).row();

        // Back button
        TextButton backButton = new TextButton("Back to Scenario Selection", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new ScenarioSelectionScreen(game));
            }
        });
        mainTable.add(backButton);

        stage.addActor(mainTable);
    }
}

