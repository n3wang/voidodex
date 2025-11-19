package io.github.n3wang.voidcodex.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.n3wang.voidcodex.VoidCodexGame;
import io.github.n3wang.voidcodex.model.*;

/**
 * Combat screen with boarding and weapons options, plus shield management.
 */
public class CombatScreen extends GameScreen {
    private Table mainTable;
    private Table playerShipTable;
    private Table enemyShipTable;
    private Table combatActionsTable;
    private Label playerShieldLabel;
    private Label enemyShieldLabel;
    private Label statusLabel;

    public CombatScreen(VoidCodexGame game) {
        super(game);
        createUI();
        initializeCombat();
    }

    private void createUI() {
        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(10f);

        // Title
        Label title = new Label("COMBAT", game.getSkin(), "subtitle");
        mainTable.add(title).colspan(2).padBottom(10f).row();

        // Player and Enemy ship info side by side
        Table shipsTable = new Table();

        // Player ship panel
        playerShipTable = new Table();
        playerShipTable.setBackground(game.getDrawable("default-round"));
        playerShipTable.pad(10f);
        updatePlayerShipInfo();
        shipsTable.add(playerShipTable).size(400f, 300f).padRight(10f);

        // Enemy ship panel
        enemyShipTable = new Table();
        enemyShipTable.setBackground(game.getDrawable("default-round"));
        enemyShipTable.pad(10f);
        updateEnemyShipInfo();
        shipsTable.add(enemyShipTable).size(400f, 300f);

        mainTable.add(shipsTable).colspan(2).padBottom(10f).row();

        // Combat mode selection
        Table modeTable = new Table();
        modeTable.setBackground(game.getDrawable("default-round"));
        modeTable.pad(10f);

        Label modeLabel = new Label("Combat Mode:", game.getSkin(), "subtitle");
        modeTable.add(modeLabel).padRight(10f);

        CombatState combatState = game.getGameState().getCombatState();
        
        TextButton weaponsButton = new TextButton("Weapons", game.getSkin());
        weaponsButton.setChecked(combatState.getMode() == CombatMode.WEAPONS);
        weaponsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                combatState.setMode(CombatMode.WEAPONS);
                updateCombatActions();
            }
        });
        modeTable.add(weaponsButton).padRight(10f);

        TextButton boardingButton = new TextButton("Boarding", game.getSkin());
        boardingButton.setChecked(combatState.getMode() == CombatMode.BOARDING);
        boardingButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                combatState.setMode(CombatMode.BOARDING);
                updateCombatActions();
            }
        });
        modeTable.add(boardingButton);

        mainTable.add(modeTable).colspan(2).fillX().padBottom(10f).row();

        // Combat actions
        combatActionsTable = new Table();
        combatActionsTable.setBackground(game.getDrawable("default-round"));
        combatActionsTable.pad(10f);
        updateCombatActions();
        mainTable.add(combatActionsTable).colspan(2).fillX().padBottom(10f).row();

        // Status label
        statusLabel = new Label("", game.getSkin());
        statusLabel.setColor(Color.YELLOW);
        mainTable.add(statusLabel).colspan(2).padBottom(10f).row();

        // Back button
        TextButton backButton = new TextButton("Flee Combat", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getGameState().getCombatState().endCombat();
                game.setScreen(new ShipScreen(game));
            }
        });
        mainTable.add(backButton).colspan(2);

        stage.addActor(mainTable);
    }

    private void initializeCombat() {
        CombatState combatState = game.getGameState().getCombatState();
        Ship playerShip = game.getGameState().getCurrentShip();
        
        // Calculate max shields based on shield room power
        Room shieldRoom = playerShip.getRooms().stream()
                .filter(r -> r.getType() == RoomType.SHIELDS)
                .findFirst()
                .orElse(null);
        
        if (shieldRoom != null) {
            int maxShields = shieldRoom.getPowerLevel() * 2; // 2 shield per power
            combatState.setMaxPlayerShields(maxShields);
            combatState.setPlayerShields(maxShields);
        }
        
        // Enemy shields
        combatState.setMaxEnemyShields(4);
        combatState.setEnemyShields(4);
    }

    private void updatePlayerShipInfo() {
        playerShipTable.clear();
        Ship ship = game.getGameState().getCurrentShip();
        CombatState combatState = game.getGameState().getCombatState();

        Label title = new Label("Your Ship: " + ship.getName(), game.getSkin(), "subtitle");
        playerShipTable.add(title).padBottom(10f).row();

        Label hullLabel = new Label("Hull: " + ship.getCurrentHull() + "/" + ship.getMaxHull(), game.getSkin());
        playerShipTable.add(hullLabel).left().row();

        playerShieldLabel = new Label("Shields: " + combatState.getPlayerShields() + "/" + combatState.getMaxPlayerShields(), game.getSkin());
        playerShieldLabel.setColor(Color.CYAN);
        playerShipTable.add(playerShieldLabel).left().padTop(5f).row();

        // Show weapon power
        Room weaponRoom = ship.getRooms().stream()
                .filter(r -> r.getType() == RoomType.WEAPONS)
                .findFirst()
                .orElse(null);
        if (weaponRoom != null) {
            Label weaponLabel = new Label("Weapon Power: " + weaponRoom.getPowerLevel() + "/" + weaponRoom.getMaxPower(), game.getSkin());
            playerShipTable.add(weaponLabel).left().padTop(5f).row();
        }

        // Crew count
        Label crewLabel = new Label("Crew: " + ship.getCrew().size(), game.getSkin());
        playerShipTable.add(crewLabel).left().padTop(5f).row();
    }

    private void updateEnemyShipInfo() {
        enemyShipTable.clear();
        CombatState combatState = game.getGameState().getCombatState();
        Ship enemyShip = combatState.getEnemyShip();

        Label title = new Label("Enemy Ship", game.getSkin(), "subtitle");
        enemyShipTable.add(title).padBottom(10f).row();

        Label hullLabel = new Label("Hull: " + enemyShip.getCurrentHull() + "/" + enemyShip.getMaxHull(), game.getSkin());
        enemyShipTable.add(hullLabel).left().row();

        enemyShieldLabel = new Label("Shields: " + combatState.getEnemyShields() + "/" + combatState.getMaxEnemyShields(), game.getSkin());
        enemyShieldLabel.setColor(Color.CYAN);
        enemyShipTable.add(enemyShieldLabel).left().padTop(5f).row();

        Label crewLabel = new Label("Crew: " + enemyShip.getCrew().size(), game.getSkin());
        enemyShipTable.add(crewLabel).left().padTop(5f).row();
    }

    private void updateCombatActions() {
        combatActionsTable.clear();
        CombatState combatState = game.getGameState().getCombatState();
        Ship playerShip = game.getGameState().getCurrentShip();

        if (combatState.getMode() == CombatMode.WEAPONS) {
            Label title = new Label("Weapons Mode", game.getSkin(), "subtitle");
            combatActionsTable.add(title).padBottom(10f).row();

            // Fire weapons button
            Room weaponRoom = playerShip.getRooms().stream()
                    .filter(r -> r.getType() == RoomType.WEAPONS)
                    .findFirst()
                    .orElse(null);

            if (weaponRoom != null && weaponRoom.getPowerLevel() > 0) {
                TextButton fireButton = new TextButton("Fire Weapons (" + weaponRoom.getPowerLevel() + " damage)", game.getSkin());
                fireButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        fireWeapons();
                    }
                });
                combatActionsTable.add(fireButton).padRight(10f);
            } else {
                Label noPowerLabel = new Label("Weapons need power! Assign power to weapons room.", game.getSkin());
                noPowerLabel.setColor(Color.RED);
                combatActionsTable.add(noPowerLabel).row();
            }

            // Shield management
            combatActionsTable.row().padTop(10f);
            Label shieldTitle = new Label("Shield Management:", game.getSkin());
            combatActionsTable.add(shieldTitle).left().row();

            Room shieldRoom = playerShip.getRooms().stream()
                    .filter(r -> r.getType() == RoomType.SHIELDS)
                    .findFirst()
                    .orElse(null);

            if (shieldRoom != null) {
                TextButton rechargeButton = new TextButton("Recharge Shields (+" + shieldRoom.getPowerLevel() + ")", game.getSkin());
                rechargeButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        rechargeShields();
                    }
                });
                combatActionsTable.add(rechargeButton).padRight(10f);
            }

        } else { // BOARDING mode
            Label title = new Label("Boarding Mode", game.getSkin(), "subtitle");
            combatActionsTable.add(title).padBottom(10f).row();

            Label descLabel = new Label("Send crew to board enemy ship", game.getSkin());
            combatActionsTable.add(descLabel).left().row();

            TextButton boardButton = new TextButton("Board Enemy Ship", game.getSkin());
            boardButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    boardEnemyShip();
                }
            });
            combatActionsTable.add(boardButton).padTop(10f);
        }

        // End turn button
        combatActionsTable.row().padTop(10f);
        TextButton endTurnButton = new TextButton("End Turn", game.getSkin());
        endTurnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                endTurn();
            }
        });
        combatActionsTable.add(endTurnButton);
    }

    private void fireWeapons() {
        Ship playerShip = game.getGameState().getCurrentShip();
        CombatState combatState = game.getGameState().getCombatState();
        Ship enemyShip = combatState.getEnemyShip();

        Room weaponRoom = playerShip.getRooms().stream()
                .filter(r -> r.getType() == RoomType.WEAPONS)
                .findFirst()
                .orElse(null);

        if (weaponRoom == null || weaponRoom.getPowerLevel() == 0) {
            statusLabel.setText("Weapons have no power!");
            return;
        }

        int damage = weaponRoom.getPowerLevel();

        // Check enemy shields first
        if (combatState.getEnemyShields() > 0) {
            int shieldDamage = Math.min(damage, combatState.getEnemyShields());
            combatState.setEnemyShields(combatState.getEnemyShields() - shieldDamage);
            damage -= shieldDamage;
            statusLabel.setText("Enemy shields absorbed " + shieldDamage + " damage!");
        }

        // Apply remaining damage to hull
        if (damage > 0) {
            enemyShip.setCurrentHull(enemyShip.getCurrentHull() - damage);
            statusLabel.setText("Dealt " + damage + " damage to enemy hull!");
        }

        if (enemyShip.getCurrentHull() <= 0) {
            statusLabel.setText("Enemy ship destroyed! Victory!");
            combatState.endCombat();
        }

        updateEnemyShipInfo();
    }

    private void rechargeShields() {
        Ship playerShip = game.getGameState().getCurrentShip();
        CombatState combatState = game.getGameState().getCombatState();

        Room shieldRoom = playerShip.getRooms().stream()
                .filter(r -> r.getType() == RoomType.SHIELDS)
                .findFirst()
                .orElse(null);

        if (shieldRoom == null || shieldRoom.getPowerLevel() == 0) {
            statusLabel.setText("Shields have no power!");
            return;
        }

        int recharge = shieldRoom.getPowerLevel();
        int newShields = Math.min(combatState.getPlayerShields() + recharge, combatState.getMaxPlayerShields());
        combatState.setPlayerShields(newShields);
        statusLabel.setText("Shields recharged to " + newShields + "!");
        updatePlayerShipInfo();
    }

    private void boardEnemyShip() {
        Ship playerShip = game.getGameState().getCurrentShip();
        CombatState combatState = game.getGameState().getCombatState();
        Ship enemyShip = combatState.getEnemyShip();

        if (playerShip.getCrew().isEmpty()) {
            statusLabel.setText("No crew to board!");
            return;
        }

        // Simple boarding: crew damage enemy
        int crewDamage = playerShip.getCrew().size() * 2;
        enemyShip.setCurrentHull(enemyShip.getCurrentHull() - crewDamage);
        statusLabel.setText("Boarding party dealt " + crewDamage + " damage!");

        if (enemyShip.getCurrentHull() <= 0) {
            statusLabel.setText("Enemy ship destroyed! Victory!");
            combatState.endCombat();
        }

        updateEnemyShipInfo();
    }

    private void endTurn() {
        CombatState combatState = game.getGameState().getCombatState();
        combatState.nextTurn();

        if (!combatState.isPlayerTurn()) {
            // Enemy turn - simple AI
            performEnemyTurn();
            combatState.nextTurn();
        }

        statusLabel.setText("Turn ended. " + (combatState.isPlayerTurn() ? "Your turn!" : "Enemy turn!"));
    }

    private void performEnemyTurn() {
        CombatState combatState = game.getGameState().getCombatState();
        Ship playerShip = game.getGameState().getCurrentShip();
        Ship enemyShip = combatState.getEnemyShip();

        // Simple enemy AI: attack player
        int enemyDamage = 2; // Base enemy damage

        // Check player shields
        if (combatState.getPlayerShields() > 0) {
            int shieldDamage = Math.min(enemyDamage, combatState.getPlayerShields());
            combatState.setPlayerShields(combatState.getPlayerShields() - shieldDamage);
            enemyDamage -= shieldDamage;
        }

        // Apply remaining damage to hull
        if (enemyDamage > 0) {
            playerShip.setCurrentHull(playerShip.getCurrentHull() - enemyDamage);
            statusLabel.setText("Enemy dealt " + enemyDamage + " damage to your hull!");
        } else {
            statusLabel.setText("Your shields absorbed the enemy attack!");
        }

        if (playerShip.getCurrentHull() <= 0) {
            statusLabel.setText("Your ship is destroyed! Defeat!");
            combatState.endCombat();
        }

        updatePlayerShipInfo();
    }
}
