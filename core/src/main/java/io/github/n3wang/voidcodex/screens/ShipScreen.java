package io.github.n3wang.voidcodex.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import io.github.n3wang.voidcodex.VoidCodexGame;
import io.github.n3wang.voidcodex.model.*;

import java.util.List;

/**
 * Main ship management screen showing the ship grid, crew, and systems.
 */
public class ShipScreen extends GameScreen {
    private Table mainTable;
    private Table shipGridTable;
    private Table crewTable;
    private Table infoTable;
    private Table powerManagementTable;
    private Crew selectedCrew; // Selected crew for movement

    public ShipScreen(VoidCodexGame game) {
        super(game);
        selectedCrew = null;
        createUI();
    }

    private void createUI() {
        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(10f);

        // Top bar: Ship info and navigation
        Table topBar = new Table();
        topBar.setBackground(game.getDrawable("default-round"));
        topBar.pad(5f);

        Ship ship = game.getGameState().getCurrentShip();
        Sector sector = game.getGameState().getCurrentSector();

        Label shipNameLabel = new Label("Ship: " + ship.getName(), game.getSkin());
        Label hullLabel = new Label("Hull: " + ship.getCurrentHull() + "/" + ship.getMaxHull(), game.getSkin());
        topBarPowerLabel = new Label("Power: " + ship.getAvailablePower() + "/" + ship.getMaxPower() + " (Used: " + ship.getUsedPower() + ")", game.getSkin());
        Label sectorLabel = new Label("Sector: " + (sector != null ? sector.getBiome().getName() : "Unknown"), game.getSkin());

        topBar.add(shipNameLabel).padRight(20f);
        topBar.add(hullLabel).padRight(20f);
        topBar.add(topBarPowerLabel).padRight(20f);
        topBar.add(sectorLabel).expandX().right();

        mainTable.add(topBar).fillX().padBottom(10f).row();

        // Main content area
        Table contentTable = new Table();

        // Left: Ship grid
        shipGridTable = new Table();
        shipGridTable.setBackground(game.getDrawable("default-round"));
        shipGridTable.pad(10f);
        updateShipGrid();
        contentTable.add(shipGridTable).size(600f, 400f).padRight(10f);

        // Right: Crew and info
        Table rightPanel = new Table();
        
        // Crew panel
        crewTable = new Table();
        crewTable.setBackground(game.getDrawable("default-round"));
        crewTable.pad(10f);
        updateCrewList();
        rightPanel.add(crewTable).fillX().padBottom(10f).row();

        // Info panel
        infoTable = new Table();
        infoTable.setBackground(game.getDrawable("default-round"));
        infoTable.pad(10f);
        updateInfoPanel();
        rightPanel.add(infoTable).fillX().row();

        contentTable.add(rightPanel).size(300f, 400f);

        mainTable.add(contentTable).expand().fill().row();

        // Bottom: Power management and action buttons
        Table bottomTable = new Table();
        
        // Left: Action buttons
        Table buttonTable = new Table();
        Table buttonTable = new Table();
        
        TextButton codexButton = new TextButton("Read Codex", game.getSkin());
        codexButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new CodexScreen(game));
            }
        });
        buttonTable.add(codexButton).padRight(10f);

        TextButton jumpButton = new TextButton("Hyperspace Jump", game.getSkin());
        jumpButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                performHyperspaceJump();
            }
        });
        buttonTable.add(jumpButton).padRight(10f);
        
        TextButton combatButton = new TextButton("Start Combat", game.getSkin());
        combatButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startCombat();
            }
        });
        buttonTable.add(combatButton).padRight(10f);

        TextButton menuButton = new TextButton("Main Menu", game.getSkin());
        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        buttonTable.add(menuButton);
        
        bottomTable.add(buttonTable).expandX().left();
        
        // Right: Power Management Panel
        powerManagementTable = new Table();
        powerManagementTable.setBackground(game.getDrawable("default-round"));
        powerManagementTable.pad(10f);
        updatePowerManagement();
        bottomTable.add(powerManagementTable).size(400f, 200f).padLeft(10f);

        mainTable.add(bottomTable).fillX();

        stage.addActor(mainTable);
    }

    private void updateShipGrid() {
        shipGridTable.clear();
        shipGridTable.add(new Label("Ship Layout", game.getSkin(), "subtitle")).colspan(4).padBottom(10f).row();

        Ship ship = game.getGameState().getCurrentShip();
        int gridWidth = ship.getGridWidth();
        int gridHeight = ship.getGridHeight();

        // Create grid of room buttons
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                Room room = ship.getRoom(x, y);
                if (room != null) {
                    Button roomButton = createRoomButton(room, ship);
                    shipGridTable.add(roomButton).size(70f, 70f).pad(2f);
                } else {
                    // Empty cell
                    Table emptyCell = new Table();
                    emptyCell.setBackground(game.getDrawable("default-round"));
                    shipGridTable.add(emptyCell).size(70f, 70f).pad(2f);
                }
            }
            shipGridTable.row();
        }
    }

    private Button createRoomButton(Room room, Ship ship) {
        Table roomTable = new Table();
        roomTable.setBackground(game.getDrawable("default-round"));
        
        // Color based on room type
        Color roomColor = getRoomColor(room.getType());
        roomTable.setColor(roomColor);
        
        // Room type label
        Label typeLabel = new Label(room.getType().getDisplayName(), game.getSkin());
        typeLabel.setFontScale(0.7f);
        typeLabel.setAlignment(Align.center);
        roomTable.add(typeLabel).row();
        
        // Power level
        if (room.getMaxPower() > 0) {
            Label powerLabel = new Label("P:" + room.getPowerLevel() + "/" + room.getMaxPower(), game.getSkin());
            powerLabel.setFontScale(0.6f);
            roomTable.add(powerLabel).row();
        }
        
        // Health
        Label healthLabel = new Label("H:" + room.getHealth(), game.getSkin());
        healthLabel.setFontScale(0.6f);
        roomTable.add(healthLabel).row();
        
        // Crew indicator
        List<Crew> crewInRoom = ship.getCrew().stream()
                .filter(c -> c.getCurrentRoomX() == room.getX() && c.getCurrentRoomY() == room.getY())
                .toList();
        if (!crewInRoom.isEmpty()) {
            Label crewLabel = new Label("C:" + crewInRoom.size(), game.getSkin());
            crewLabel.setFontScale(0.6f);
            crewLabel.setColor(Color.YELLOW);
            roomTable.add(crewLabel);
        }
        
        Button button = new Button(roomTable, game.getSkin());
        
        // Highlight if selected crew can move here
        if (selectedCrew != null) {
            roomTable.setColor(roomColor.r * 1.5f, roomColor.g * 1.5f, roomColor.b * 1.5f, 1f);
        }
        
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (selectedCrew != null) {
                    // Move selected crew to this room
                    moveCrewToRoom(selectedCrew, room);
                    selectedCrew = null;
                    updateShipGrid();
                    updateCrewList();
                } else {
                    showRoomDetails(room);
                }
            }
        });
        
        return button;
    }

    private Color getRoomColor(RoomType type) {
        switch (type) {
            case BRIDGE: return Color.CYAN;
            case MEDBAY: return Color.GREEN;
            case SHIELDS: return Color.BLUE;
            case WEAPONS: return Color.RED;
            case ENGINES: return Color.ORANGE;
            case OXYGEN: return Color.WHITE;
            case SENSORS: return Color.PURPLE;
            case DOORS: return Color.GRAY;
            default: return Color.DARK_GRAY;
        }
    }

    private void updateCrewList() {
        crewTable.clear();
        crewTable.add(new Label("Crew (Click to select, then click room to move)", game.getSkin(), "subtitle")).padBottom(10f).row();

        Ship ship = game.getGameState().getCurrentShip();
        for (Crew crew : ship.getCrew()) {
            Table crewRow = new Table();
            if (selectedCrew == crew) {
                crewRow.setBackground(game.getDrawable("default-round"));
                crewRow.setColor(Color.YELLOW);
            } else {
                crewRow.setBackground(game.getDrawable("default-round"));
            }
            crewRow.pad(5f);

            Label nameLabel = new Label(crew.getName() + " (" + crew.getRole().getDisplayName() + ")", game.getSkin());
            crewRow.add(nameLabel).left().expandX().row();

            Label healthLabel = new Label("Health: " + crew.getHealth() + "/" + crew.getMaxHealth(), game.getSkin());
            healthLabel.setFontScale(0.8f);
            crewRow.add(healthLabel).left().row();

            Label locationLabel = new Label("Location: (" + crew.getCurrentRoomX() + "," + crew.getCurrentRoomY() + ")", game.getSkin());
            locationLabel.setFontScale(0.7f);
            crewRow.add(locationLabel).left();

            TextButton selectButton = new TextButton(selectedCrew == crew ? "Selected" : "Select", game.getSkin());
            selectButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (selectedCrew == crew) {
                        selectedCrew = null; // Deselect
                    } else {
                        selectedCrew = crew; // Select
                    }
                    updateCrewList();
                    updateShipGrid();
                }
            });
            crewRow.add(selectButton).padLeft(10f);

            crewTable.add(crewRow).fillX().padBottom(5f).row();
        }
    }
    
    private void moveCrewToRoom(Crew crew, Room room) {
        crew.setCurrentRoomX(room.getX());
        crew.setCurrentRoomY(room.getY());
        showMessage(crew.getName() + " moved to " + room.getType().getDisplayName());
    }
    
    private void startCombat() {
        // Create a simple enemy ship
        Ship enemyShip = Ship.createStarterShip();
        enemyShip.setCurrentHull(20);
        enemyShip.setAvailablePower(6);
        
        // Start combat
        game.getGameState().getCombatState().startCombat(enemyShip);
        game.setScreen(new CombatScreen(game));
    }

    private void updateInfoPanel() {
        infoTable.clear();
        infoTable.add(new Label("Ship Status", game.getSkin(), "subtitle")).padBottom(10f).row();

        Sector sector = game.getGameState().getCurrentSector();
        if (sector != null) {
            Label biomeLabel = new Label("Biome: " + sector.getBiome().getName(), game.getSkin());
            infoTable.add(biomeLabel).left().row();

            // Removed codex status display
        }
    }

    private void showRoomDetails(Room room) {
        // Could show a dialog with room details
        System.out.println("Room: " + room.getType().getDisplayName() + " at (" + room.getX() + "," + room.getY() + ")");
    }

    private void showMessage(String message) {
        // Simple message display - could be improved with a dialog
        System.out.println(message);
    }

    private void performHyperspaceJump() {
        // Advance to next sector
        game.getGameState().advanceSector();
        
        // Generate new sector
        Sector newSector = new Sector(
            game.getGameState().getCurrentSectorIndex(),
            io.github.n3wang.voidcodex.util.BiomeGenerator.generateRandomBiome()
        );
        game.getGameState().setCurrentSector(newSector);
        
        // Update UI
        updateShipGrid();
        updateInfoPanel();
    }
    
    private void updatePowerManagement() {
        powerManagementTable.clear();
        Ship ship = game.getGameState().getCurrentShip();
        
        // Title
        Label title = new Label("SYSTEMS POWER", game.getSkin(), "subtitle");
        powerManagementTable.add(title).colspan(3).padBottom(5f).row();
        
        // Available power display with visual bar
        Table powerBarTable = new Table();
        Label powerLabel = new Label("Power: " + ship.getAvailablePower() + "/" + ship.getMaxPower(), game.getSkin());
        powerLabel.setColor(Color.YELLOW);
        powerBarTable.add(powerLabel).padRight(10f);
        
        // Visual power bar
        Table powerBar = new Table();
        powerBar.setBackground(game.getDrawable("default-round"));
        powerBar.setColor(Color.DARK_GRAY);
        int usedPower = ship.getUsedPower();
        for (int i = 0; i < ship.getMaxPower(); i++) {
            Table powerSegment = new Table();
            powerSegment.setBackground(game.getDrawable("default-round"));
            if (i < usedPower) {
                powerSegment.setColor(Color.GREEN);
            } else if (i < usedPower + ship.getAvailablePower()) {
                powerSegment.setColor(Color.YELLOW);
            } else {
                powerSegment.setColor(Color.DARK_GRAY);
            }
            powerBar.add(powerSegment).size(15f, 15f).pad(1f);
        }
        powerBarTable.add(powerBar);
        powerManagementTable.add(powerBarTable).colspan(3).padBottom(10f).row();
        
        // Get all rooms that can have power (excluding empty rooms)
        List<Room> poweredRooms = ship.getRooms().stream()
                .filter(r -> r.getType() != RoomType.EMPTY && r.getMaxPower() > 0)
                .toList();
        
        // Create power controls for each system in two columns
        int halfSize = (poweredRooms.size() + 1) / 2;
        Table leftColumn = new Table();
        Table rightColumn = new Table();
        
        for (int i = 0; i < poweredRooms.size(); i++) {
            Room room = poweredRooms.get(i);
            Table systemRow = createSystemPowerRow(room, ship);
            
            if (i < halfSize) {
                leftColumn.add(systemRow).fillX().padBottom(3f).row();
            } else {
                rightColumn.add(systemRow).fillX().padBottom(3f).row();
            }
        }
        
        powerManagementTable.add(leftColumn).expandX().fillX().padRight(5f);
        powerManagementTable.add(rightColumn).expandX().fillX().padLeft(5f);
    }
    
    private Table createSystemPowerRow(Room room, Ship ship) {
        Table systemRow = new Table();
        systemRow.setBackground(game.getDrawable("default-round"));
        systemRow.pad(5f);
        
        // System name with icon representation
        Label systemName = new Label(room.getType().getDisplayName(), game.getSkin());
        systemName.setFontScale(0.75f);
        systemRow.add(systemName).width(70f).left();
        
        // Power boxes
        Table powerBoxes = new Table();
        for (int i = 0; i < room.getMaxPower(); i++) {
            Table powerBox = new Table();
            powerBox.setBackground(game.getDrawable("default-round"));
            
            if (i < room.getPowerLevel()) {
                // Powered box - bright green
                powerBox.setColor(Color.GREEN);
            } else {
                // Unpowered box - dark gray
                powerBox.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
            }
            
            final int powerIndex = i;
            final Room roomRef = room;
            
            // Make boxes clickable
            Button boxButton = new Button(powerBox, game.getSkin());
            boxButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (powerIndex < roomRef.getPowerLevel()) {
                        // Remove power
                        ship.removePowerFromRoom(roomRef);
                    } else {
                        // Add power
                        ship.addPowerToRoom(roomRef);
                    }
                    updatePowerManagement();
                    updateShipGrid();
                    updateTopBarPower();
                }
            });
            
            powerBoxes.add(boxButton).size(18f, 18f).pad(1f);
        }
        
        systemRow.add(powerBoxes).padLeft(5f);
        
        // Current power level display
        Label powerLevelLabel = new Label("[" + room.getPowerLevel() + "/" + room.getMaxPower() + "]", game.getSkin());
        powerLevelLabel.setFontScale(0.65f);
        powerLevelLabel.setColor(room.getPowerLevel() > 0 ? Color.WHITE : Color.GRAY);
        systemRow.add(powerLevelLabel).width(50f).right().padLeft(5f);
        
        return systemRow;
    }
    
    private Label topBarPowerLabel;
    
    private void updateTopBarPower() {
        if (topBarPowerLabel != null) {
            Ship ship = game.getGameState().getCurrentShip();
            topBarPowerLabel.setText("Power: " + ship.getAvailablePower() + "/" + ship.getMaxPower() + " (Used: " + ship.getUsedPower() + ")");
        }
    }
}

