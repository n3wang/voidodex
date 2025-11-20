package io.github.n3wang.voidcodex.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import io.github.n3wang.voidcodex.VoidCodexGame;
import io.github.n3wang.voidcodex.screens.ShipScreenNew;
import io.github.n3wang.voidcodex.model.CodexEntry;
import io.github.n3wang.voidcodex.util.CodexManager;

import java.util.List;

/**
 * Screen for reading Void Codex pages before hyperspace jumps.
 */
public class CodexScreen extends GameScreen {
    private List<CodexEntry> currentPages;
    private int currentPageIndex;
    private Label titleLabel;
    private Label contentLabel;
    private Label hintLabel;
    private TextButton nextButton;
    private TextButton previousButton;
    private TextButton doneButton;

    public CodexScreen(VoidCodexGame game) {
        super(game);
        
        // Get codex pages for current biome
        var sector = game.getGameState().getCurrentSector();
        if (sector != null) {
            currentPages = CodexManager.getCodexPagesForBiome(sector.getBiome().getType());
        } else {
            currentPages = CodexManager.getAllEntries().subList(0, Math.min(2, CodexManager.getAllEntries().size()));
        }
        
        currentPageIndex = 0;
        createUI();
        updatePageDisplay();
    }

    private void createUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(20f);

        // Title
        Label screenTitle = new Label("VOID CODEX", game.getSkin(), "subtitle");
        screenTitle.setAlignment(Align.center);
        mainTable.add(screenTitle).colspan(3).padBottom(20f).row();

        // Codex page display area
        Table pageTable = new Table();
        pageTable.setBackground(game.getDrawable("default-round"));
        pageTable.pad(20f);

        titleLabel = new Label("", game.getSkin(), "subtitle");
        titleLabel.setAlignment(Align.center);
        titleLabel.setWrap(true);
        pageTable.add(titleLabel).fillX().padBottom(15f).row();

        contentLabel = new Label("", game.getSkin());
        contentLabel.setAlignment(Align.topLeft);
        contentLabel.setWrap(true);
        pageTable.add(contentLabel).fill().expand().padBottom(15f).row();

        hintLabel = new Label("", game.getSkin());
        hintLabel.setAlignment(Align.center);
        hintLabel.setWrap(true);
        hintLabel.setColor(Color.YELLOW);
        hintLabel.setVisible(false);
        pageTable.add(hintLabel).fillX().row();

        ScrollPane scrollPane = new ScrollPane(pageTable, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        mainTable.add(scrollPane).colspan(3).fill().expand().padBottom(20f).row();

        // Navigation buttons
        Table buttonTable = new Table();

        previousButton = new TextButton("Previous", game.getSkin());
        previousButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentPageIndex > 0) {
                    currentPageIndex--;
                    updatePageDisplay();
                }
            }
        });
        buttonTable.add(previousButton).padRight(10f);

        Label pageInfo = new Label("", game.getSkin());
        pageInfo.setAlignment(Align.center);
        buttonTable.add(pageInfo).expandX();

        nextButton = new TextButton("Next", game.getSkin());
        nextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentPageIndex < currentPages.size() - 1) {
                    currentPageIndex++;
                    updatePageDisplay();
                }
            }
        });
        buttonTable.add(nextButton).padLeft(10f);

        mainTable.add(buttonTable).fillX().padBottom(10f).row();

        // Done button
        doneButton = new TextButton("Done Reading", game.getSkin());
        doneButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Mark codex as read
                var sector = game.getGameState().getCurrentSector();
                if (sector != null) {
                    sector.setCodexRead(true);
                }
                // Return to ship screen
                game.setScreen(new ShipScreenNew(game));
            }
        });
        mainTable.add(doneButton).fillX();

        stage.addActor(mainTable);
    }

    private void updatePageDisplay() {
        if (currentPages.isEmpty()) {
            titleLabel.setText("No Entries");
            contentLabel.setText("The Codex is empty.");
            return;
        }

        CodexEntry entry = currentPages.get(currentPageIndex);
        titleLabel.setText(entry.getTitle());
        contentLabel.setText(entry.getContent());

        // Show biome hint if available
        if (entry.revealsBiomeInfo() && entry.getBiomeHint() != null) {
            hintLabel.setText("Biome Knowledge: " + entry.getBiomeHint());
            hintLabel.setVisible(true);
        } else {
            hintLabel.setVisible(false);
        }

        // Update button states
        previousButton.setDisabled(currentPageIndex == 0);
        nextButton.setDisabled(currentPageIndex >= currentPages.size() - 1);
    }
}

