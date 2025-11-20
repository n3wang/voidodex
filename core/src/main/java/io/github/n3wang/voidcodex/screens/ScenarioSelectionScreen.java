package io.github.n3wang.voidcodex.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import io.github.n3wang.voidcodex.VoidCodexGame;
import io.github.n3wang.voidcodex.model.Ship;
import io.github.n3wang.voidcodex.model.Sector;
import io.github.n3wang.voidcodex.model.Biome;
import io.github.n3wang.voidcodex.util.BiomeGenerator;

/**
 * Scenario selection screen for development/testing different game scenarios.
 */
public class ScenarioSelectionScreen extends GameScreen {
    public ScenarioSelectionScreen(VoidCodexGame game) {
        super(game);
        createUI();
    }

    private void createUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(20f);

        Label title = new Label("VOID CODEX - SCENARIO SELECTION", game.getSkin(), "subtitle");
        title.setAlignment(Align.center);
        mainTable.add(title).colspan(2).padBottom(30f).row();

        // Left column: Scenarios
        Table scenariosTable = new Table();
        scenariosTable.setBackground(game.getDrawable("default-round"));
        scenariosTable.pad(15f);

        Label scenariosTitle = new Label("SCENARIOS", game.getSkin(), "subtitle");
        scenariosTable.add(scenariosTitle).padBottom(15f).row();

        // Ship Management Scenario
        TextButton shipButton = new TextButton("Ship Management", game.getSkin());
        shipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startShipScenario();
            }
        });
        scenariosTable.add(shipButton).width(250f).padBottom(10f).row();

        // Combat Scenario
        TextButton combatButton = new TextButton("Combat Scenario", game.getSkin());
        combatButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startCombatScenario();
            }
        });
        scenariosTable.add(combatButton).width(250f).padBottom(10f).row();

        // Merchant Scenario
        TextButton merchantButton = new TextButton("Merchant Scenario", game.getSkin());
        merchantButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startMerchantScenario();
            }
        });
        scenariosTable.add(merchantButton).width(250f).padBottom(10f).row();

        // Codex Reading Scenario
        TextButton codexButton = new TextButton("Codex Reading", game.getSkin());
        codexButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startCodexScenario();
            }
        });
        scenariosTable.add(codexButton).width(250f).padBottom(10f).row();

        mainTable.add(scenariosTable).size(300f, 400f).padRight(20f);

        // Right column: Descriptions
        Table descriptionsTable = new Table();
        descriptionsTable.setBackground(game.getDrawable("default-round"));
        descriptionsTable.pad(15f);

        Label descTitle = new Label("DESCRIPTIONS", game.getSkin(), "subtitle");
        descriptionsTable.add(descTitle).padBottom(15f).row();

        Label shipDesc = new Label("Ship Management:\n" +
                "Test ship systems, power management,\n" +
                "crew movement, and ship layout.", game.getSkin());
        shipDesc.setWrap(true);
        descriptionsTable.add(shipDesc).width(350f).padBottom(20f).row();

        Label combatDesc = new Label("Combat Scenario:\n" +
                "Test combat mechanics, weapon targeting,\n" +
                "shields, and boarding actions.", game.getSkin());
        combatDesc.setWrap(true);
        descriptionsTable.add(combatDesc).width(350f).padBottom(20f).row();

        Label merchantDesc = new Label("Merchant Scenario:\n" +
                "Test trading, resource management,\n" +
                "and merchant interactions.", game.getSkin());
        merchantDesc.setWrap(true);
        descriptionsTable.add(merchantDesc).width(350f).padBottom(20f).row();

        Label codexDesc = new Label("Codex Reading:\n" +
                "Test codex system, lore reading,\n" +
                "and biome information.", game.getSkin());
        codexDesc.setWrap(true);
        descriptionsTable.add(codexDesc).width(350f).row();

        mainTable.add(descriptionsTable).size(400f, 400f).row();

        // Bottom: Exit button
        TextButton exitButton = new TextButton("Exit", game.getSkin());
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.exit(0);
            }
        });
        mainTable.add(exitButton).colspan(2).padTop(20f);

        stage.addActor(mainTable);
    }

    private void startShipScenario() {
        // Initialize ship
        Ship starterShip = Ship.createStarterShip();
        game.getGameState().setCurrentShip(starterShip);
        
        // Generate sector
        Biome biome = BiomeGenerator.generateRandomBiome();
        Sector sector = new Sector(0, biome);
        game.getGameState().setCurrentSector(sector);
        
        // Go to ship screen
        game.setScreen(new ShipScreenNew(game));
    }

    private void startCombatScenario() {
        // Initialize player ship
        Ship playerShip = Ship.createStarterShip();
        game.getGameState().setCurrentShip(playerShip);
        
        // Generate sector
        Biome biome = BiomeGenerator.generateRandomBiome();
        Sector sector = new Sector(0, biome);
        game.getGameState().setCurrentSector(sector);
        
        // Create enemy ship with different layout
        Ship enemyShip = createEnemyShip();
        enemyShip.setCurrentHull(25);
        enemyShip.setShields(2);
        enemyShip.setMaxShields(3);
        
        // Add enemy crew
        enemyShip.addCrew(new io.github.n3wang.voidcodex.model.Crew("Enemy Captain", io.github.n3wang.voidcodex.model.CrewRole.CAPTAIN));
        enemyShip.addCrew(new io.github.n3wang.voidcodex.model.Crew("Enemy Gunner", io.github.n3wang.voidcodex.model.CrewRole.SOLDIER));
        
        // Start combat
        game.getGameState().getCombatState().startCombat(playerShip, enemyShip);
        
        // Go to ship screen (which will show combat)
        game.setScreen(new ShipScreenNew(game));
    }
    
    private Ship createEnemyShip() {
        Ship enemyShip = new Ship("Pirate Vessel", 25, 6, 3, 3);
        
        // Enemy ship layout (3x3 grid)
        // Row 0: Bridge, Shields, Weapons
        enemyShip.addRoom(new io.github.n3wang.voidcodex.model.Room(0, 0, io.github.n3wang.voidcodex.model.RoomType.BRIDGE));
        enemyShip.addRoom(new io.github.n3wang.voidcodex.model.Room(1, 0, io.github.n3wang.voidcodex.model.RoomType.SHIELDS));
        enemyShip.addRoom(new io.github.n3wang.voidcodex.model.Room(2, 0, io.github.n3wang.voidcodex.model.RoomType.WEAPONS));
        
        // Row 1: Engines, Medbay, Oxygen
        enemyShip.addRoom(new io.github.n3wang.voidcodex.model.Room(0, 1, io.github.n3wang.voidcodex.model.RoomType.ENGINES));
        enemyShip.addRoom(new io.github.n3wang.voidcodex.model.Room(1, 1, io.github.n3wang.voidcodex.model.RoomType.MEDBAY));
        enemyShip.addRoom(new io.github.n3wang.voidcodex.model.Room(2, 1, io.github.n3wang.voidcodex.model.RoomType.OXYGEN));
        
        // Row 2: Empty, Empty, Empty
        enemyShip.addRoom(new io.github.n3wang.voidcodex.model.Room(0, 2, io.github.n3wang.voidcodex.model.RoomType.EMPTY));
        enemyShip.addRoom(new io.github.n3wang.voidcodex.model.Room(1, 2, io.github.n3wang.voidcodex.model.RoomType.EMPTY));
        enemyShip.addRoom(new io.github.n3wang.voidcodex.model.Room(2, 2, io.github.n3wang.voidcodex.model.RoomType.EMPTY));
        
        // Add enemy weapons
        enemyShip.addWeapon(new io.github.n3wang.voidcodex.model.Weapon("Enemy Laser", io.github.n3wang.voidcodex.model.WeaponType.LASER, 8, 1, 1));
        
        return enemyShip;
    }

    private void startMerchantScenario() {
        // Initialize ship
        Ship starterShip = Ship.createStarterShip();
        starterShip.setScrap(100); // Give some scrap for trading
        game.getGameState().setCurrentShip(starterShip);
        
        // Generate sector
        Biome biome = BiomeGenerator.generateRandomBiome();
        Sector sector = new Sector(0, biome);
        game.getGameState().setCurrentSector(sector);
        
        // Go to merchant screen
        game.setScreen(new MerchantScreen(game));
    }

    private void startCodexScenario() {
        // Initialize ship
        Ship starterShip = Ship.createStarterShip();
        game.getGameState().setCurrentShip(starterShip);
        
        // Generate sector
        Biome biome = BiomeGenerator.generateRandomBiome();
        Sector sector = new Sector(0, biome);
        game.getGameState().setCurrentSector(sector);
        
        // Go to codex screen
        game.setScreen(new CodexScreen(game));
    }
}

