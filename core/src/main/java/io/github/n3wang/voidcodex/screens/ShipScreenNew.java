package io.github.n3wang.voidcodex.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import io.github.n3wang.voidcodex.VoidCodexGame;
import io.github.n3wang.voidcodex.model.*;
import io.github.n3wang.voidcodex.util.PixelArtGenerator;
import io.github.n3wang.voidcodex.util.Pathfinding;
import io.github.n3wang.voidcodex.util.TilePathfinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Void Wars-style ship management and combat screen.
 */
public class ShipScreenNew extends GameScreen {
    // UI Components
    private Table mainTable;
    private Table topBar;
    private Table leftPanel; // Crew portraits
    private Table centerPanel; // Player ship grid container
    private ShipTileMapActor shipTileMap; // Tile-based ship rendering
    private Table rightPanel; // Enemy ship grid (combat)
    private Table bottomPanel; // Weapons and systems

    // Top bar components
    private Label hullLabel;
    private ProgressBar hullBar;
    private Label shieldLabel;
    private ProgressBar shieldBar;
    private Label scrapLabel;
    private Label fuelLabel;
    private Label powerLabel;
    private Label timerLabel; // Debug timer
    private TextButton pauseButton;
    private TextButton slowButton;
    private TextButton fastButton;

    // Game time tracking
    private float gameTime = 0f;

    // State
    private List<Crew> selectedCrew;
    private Weapon selectedWeapon;

    public ShipScreenNew(VoidCodexGame game) {
        super(game);
        selectedCrew = new ArrayList<>();
        createUI();
    }

    @Override
    public void render(float delta) {
        GameTimeState timeState = game.getGameState().getTimeState();

        // Update game time (only if not paused)
        if (!timeState.isPaused()) {
            float timeScale = timeState.getTimeScale();
            float scaledDelta = delta * timeScale;
            gameTime += scaledDelta;

            // Update crew movement (time-based, 0.5 seconds per tile)
            updateCrewMovement(scaledDelta);

            // Update weapon charges
            updateWeaponCharges(scaledDelta);

            // Update oxygen system
            updateOxygenSystem(scaledDelta);
            
            // Update repair systems
            updateRepairSystems(scaledDelta);
        }

        // Always render (even when paused)
        super.render(delta);

        // Update UI
        updateTopBar();

        // Update tile map position (in case UI moved) - only update position, not z-order
        if (shipTileMap != null && centerPanel != null) {
            centerPanel.layout();
            // Convert centerPanel coordinates to stage coordinates
            Vector2 stagePos = centerPanel.localToStageCoordinates(new Vector2(0, 0));
            float mapX = stagePos.x + 10; // Padding from left edge of centerPanel
            float mapY = stagePos.y + 40; // Below title
            shipTileMap.setPosition(mapX, mapY);
            // Don't call toFront() every frame - it causes z-ordering conflicts
        }
    }

    /**
     * Handle crew selection logic
     */
    private void handleCrewSelection(Crew crew) {
        // Debug: log selection
        Gdx.app.log("ShipScreen", "Selecting crew: " + crew.getName());

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            // Shift-right-click: toggle selection
            if (selectedCrew.contains(crew)) {
                selectedCrew.remove(crew);
            } else {
                selectedCrew.add(crew);
            }
        } else {
            // Regular right-click: single select
            if (selectedCrew.contains(crew) && selectedCrew.size() == 1) {
                selectedCrew.clear(); // Deselect if already selected
            } else {
                selectedCrew.clear();
                selectedCrew.add(crew);
            }
        }
        updateCrewPortraits();
        updateShipGrid(); // Update ship grid to show selected crew
    }

    /**
     * Handle tile click - LEFT CLICK = SELECT, RIGHT CLICK = MOVE
     */
    private void handleTileClick(Room room, int tileX, int tileY, int button) {
        Ship ship = game.getGameState().getCurrentShip();
        Crew crewAtTile = room.getCrewAtTile(tileX, tileY);

        if (button == Input.Buttons.LEFT) {
            // LEFT CLICK: Select crew at tile
            if (crewAtTile != null) {
                handleCrewSelection(crewAtTile);
            }
        } else if (button == Input.Buttons.RIGHT) {
            // RIGHT CLICK: Move selected crew to this tile
            if (!selectedCrew.isEmpty() && room.getType() != RoomType.EMPTY) {
                // Check if tile is empty
                if (crewAtTile == null) {
                    // Check if any selected crew is moving
                    boolean anyMoving = selectedCrew.stream().anyMatch(Crew::isMoving);

                    if (!anyMoving) {
                        // Move first selected crew to this tile
                        Crew crew = selectedCrew.get(0);

                        // Remove crew from current tile (but don't remove from room yet - let movement handle it)
                        Room currentRoom = ship.getRoom(crew.getCurrentRoomX(), crew.getCurrentRoomY());

                        // Use tile-based pathfinding
                        List<int[]> path = TilePathfinding.findPath(ship,
                                crew.getCurrentRoomX(), crew.getCurrentRoomY(),
                                crew.getCurrentTileX(), crew.getCurrentTileY(),
                                room.getX(), room.getY(), tileX, tileY);

                        if (!path.isEmpty()) {
                            // Remove from current tile before starting movement
                            if (currentRoom != null) {
                                currentRoom.removeCrewFromTile(crew.getCurrentTileX(), crew.getCurrentTileY());
                            }

                            // Set target and start movement
                            crew.setTargetRoomX(room.getX());
                            crew.setTargetRoomY(room.getY());
                            crew.setTargetTileX(tileX);
                            crew.setTargetTileY(tileY);
                            crew.setMoving(true);
                            crew.setMovementProgress(0.0f);

                            // Set first step in path
                            int[] firstStep = path.get(0);
                            crew.setNextRoomX(firstStep[0]);
                            crew.setNextRoomY(firstStep[1]);
                            crew.setNextTileX(firstStep[2]);
                            crew.setNextTileY(firstStep[3]);

                            updateCrewPortraits();
                            updateShipGrid();
                        } else {
                            // Path blocked, place crew directly if same room and adjacent tile
                            if (crew.getCurrentRoomX() == room.getX() &&
                                crew.getCurrentRoomY() == room.getY()) {
                                // Check if it's an adjacent tile (horizontal or vertical only)
                                int dx = Math.abs(crew.getCurrentTileX() - tileX);
                                int dy = Math.abs(crew.getCurrentTileY() - tileY);
                                if ((dx == 1 && dy == 0) || (dx == 0 && dy == 1)) {
                                    // Adjacent tile, move directly
                                    if (currentRoom != null) {
                                        currentRoom.removeCrewFromTile(crew.getCurrentTileX(), crew.getCurrentTileY());
                                    }
                                    room.setCrewAtTile(tileX, tileY, crew);
                                    crew.setCurrentTileX(tileX);
                                    crew.setCurrentTileY(tileY);
                                    updateShipGrid();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateCrewMovement(float delta) {
        Ship ship = game.getGameState().getCurrentShip();
        boolean needsUpdate = false;

        for (Crew crew : ship.getCrew()) {
            if (crew.isMoving()) {
                // Movement speed: 2 tiles per second = 0.5 seconds per tile
                crew.setMovementProgress(crew.getMovementProgress() + crew.getMovementSpeed() * delta);

                if (crew.getMovementProgress() >= 1.0f) {
                    // Reached next tile in path
                    Room currentRoom = ship.getRoom(crew.getCurrentRoomX(), crew.getCurrentRoomY());
                    if (currentRoom != null) {
                        // Remove from current tile
                        currentRoom.removeCrewFromTile(crew.getCurrentTileX(), crew.getCurrentTileY());
                    }

                    // Move to next tile
                    crew.setCurrentRoomX(crew.getNextRoomX());
                    crew.setCurrentRoomY(crew.getNextRoomY());
                    crew.setCurrentTileX(crew.getNextTileX());
                    crew.setCurrentTileY(crew.getNextTileY());
                    crew.setMovementProgress(0.0f);

                    // Place crew in new tile
                    Room nextRoom = ship.getRoom(crew.getCurrentRoomX(), crew.getCurrentRoomY());
                    if (nextRoom != null) {
                        nextRoom.setCrewAtTile(crew.getCurrentTileX(), crew.getCurrentTileY(), crew);
                    }

                    // Check if reached final destination
                    if (crew.getCurrentRoomX() == crew.getTargetRoomX() &&
                        crew.getCurrentRoomY() == crew.getTargetRoomY() &&
                        crew.getCurrentTileX() == crew.getTargetTileX() &&
                        crew.getCurrentTileY() == crew.getTargetTileY()) {
                        // Reached destination
                        crew.setMoving(false);
                        needsUpdate = true;
                    } else {
                        // Continue to next tile in path
                        List<int[]> path = TilePathfinding.findPath(ship,
                                crew.getCurrentRoomX(), crew.getCurrentRoomY(),
                                crew.getCurrentTileX(), crew.getCurrentTileY(),
                                crew.getTargetRoomX(), crew.getTargetRoomY(),
                                crew.getTargetTileX(), crew.getTargetTileY());

                        if (!path.isEmpty()) {
                            int[] nextStep = path.get(0);
                            crew.setNextRoomX(nextStep[0]);
                            crew.setNextRoomY(nextStep[1]);
                            crew.setNextTileX(nextStep[2]);
                            crew.setNextTileY(nextStep[3]);
                        } else {
                            // Path blocked, stop movement
                            crew.setMoving(false);
                        }
                        needsUpdate = true;
                    }
                }
            }
        }

        if (needsUpdate) {
            updateShipGrid();
        }
    }

    private void createUI() {
        mainTable = new Table();
        mainTable.setFillParent(true);
        // Minimal padding to ensure top bar has space
        mainTable.pad(5f);
        // Make sure mainTable doesn't block input to child actors
        mainTable.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.childrenOnly);

        // Top Bar - give it more space and ensure it's visible
        createTopBar();
        mainTable.add(topBar).fillX().height(60f).padBottom(5f).row();

        // Main content area - use expandY() but don't fill to allow bottom panel space
        Table contentTable = new Table();

        // Left Panel - Crew Portraits
        createLeftPanel();
        contentTable.add(leftPanel).size(150f, 400f).padRight(5f);

        // Center Panel - Player Ship Grid
        createCenterPanel();
        contentTable.add(centerPanel).expand().fill().padRight(5f);

        // Right Panel - Enemy Ship Grid (only in combat)
        createRightPanel();
        if (game.getGameState().isInCombat()) {
            contentTable.add(rightPanel).size(300f, 400f);
        }

        // Give content area explicit height to leave room for top and bottom
        // Total: 5 (top pad) + 60 (top bar) + 5 (gap) + 400 (content) + 5 (gap) + 180 (bottom) + 5 (bottom pad) = 660f
        mainTable.add(contentTable).expand().fill().height(400f).padBottom(5f).row();

        // Bottom Panel - Weapons and Systems
        createBottomPanel();
        mainTable.add(bottomPanel).fillX().height(180f);

        stage.addActor(mainTable);
        setupKeyboardInput();

        // Create tile map after UI is set up
        updateShipGrid();
    }

    private void createTopBar() {
        topBar = new Table();
        topBar.setBackground(game.getDrawable("default-round"));
        topBar.pad(8f);
        // Ensure top bar has adequate height and is visible
        topBar.setHeight(60f);
        topBar.setVisible(true);

        Ship ship = game.getGameState().getCurrentShip();

        // Hull
        Label hullText = new Label("Hull:", game.getSkin());
        topBar.add(hullText).padRight(5f);

        hullBar = new ProgressBar(0, ship.getMaxHull(), 1, false, game.getSkin());
        hullBar.setValue(ship.getCurrentHull());
        hullBar.setWidth(100f);
        topBar.add(hullBar).padRight(10f);

        hullLabel = new Label(ship.getCurrentHull() + "/" + ship.getMaxHull(), game.getSkin());
        topBar.add(hullLabel).padRight(15f);

        // Shields
        Label shieldText = new Label("Shields:", game.getSkin());
        topBar.add(shieldText).padRight(5f);

        shieldBar = new ProgressBar(0, ship.getMaxShields(), 1, false, game.getSkin());
        shieldBar.setValue(ship.getShields());
        shieldBar.setWidth(80f);
        topBar.add(shieldBar).padRight(10f);

        shieldLabel = new Label(ship.getShields() + "/" + ship.getMaxShields(), game.getSkin());
        shieldLabel.setColor(Color.CYAN);
        topBar.add(shieldLabel).padRight(15f);

        // Resources
        scrapLabel = new Label("Scrap: " + ship.getScrap(), game.getSkin());
        topBar.add(scrapLabel).padRight(10f);

        fuelLabel = new Label("Fuel: " + ship.getFuel(), game.getSkin());
        topBar.add(fuelLabel).padRight(10f);

        powerLabel = new Label("Energy: " + ship.getAvailablePower() + "/" + ship.getMaxPower() + " (Used: " + ship.getUsedPower() + ")", game.getSkin());
        powerLabel.setColor(Color.YELLOW);
        topBar.add(powerLabel).padRight(15f);

        // Debug timer
        timerLabel = new Label("Time: 0.00s", game.getSkin());
        timerLabel.setColor(Color.CYAN);
        topBar.add(timerLabel).padRight(15f);

        // Time controls
        pauseButton = new TextButton("PAUSE", game.getSkin());
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.getGameState().getTimeState().togglePause();
                updateTimeButtons();
            }
        });
        topBar.add(pauseButton).padRight(5f);

        slowButton = new TextButton("SLOW", game.getSkin());
        slowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.getGameState().getTimeState().setTimeScale(0.5f);
                updateTimeButtons();
            }
        });
        topBar.add(slowButton).padRight(5f);

        fastButton = new TextButton("FAST", game.getSkin());
        fastButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.getGameState().getTimeState().setTimeScale(2.0f);
                updateTimeButtons();
            }
        });
        topBar.add(fastButton).padRight(15f);

        // Warp button
        TextButton warpButton = new TextButton("WARP", game.getSkin());
        warpButton.setColor(Color.CYAN);
        warpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                performWarp();
            }
        });
        topBar.add(warpButton).padRight(10f);

        // Info/Description button
        TextButton infoButton = new TextButton("INFO", game.getSkin());
        infoButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showInfoDialog();
            }
        });
        topBar.add(infoButton).padRight(10f);

        // Back to scenario selection
        TextButton backButton = new TextButton("BACK", game.getSkin());
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new io.github.n3wang.voidcodex.screens.ScenarioSelectionScreen(game));
            }
        });
        topBar.add(backButton);

        updateTimeButtons();
    }

    private void performWarp() {
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
        updateTopBar();
    }

    private void showInfoDialog() {
        // Create a simple info dialog
        Dialog dialog = new Dialog("Ship Information", game.getSkin()) {
            @Override
            protected void result(Object object) {
                hide();
            }
        };

        Ship ship = game.getGameState().getCurrentShip();
        Sector sector = game.getGameState().getCurrentSector();

        String info = "Ship: " + ship.getName() + "\n";
        info += "Hull: " + ship.getCurrentHull() + "/" + ship.getMaxHull() + "\n";
        info += "Shields: " + ship.getShields() + "/" + ship.getMaxShields() + "\n";
        info += "Power: " + ship.getAvailablePower() + "/" + ship.getMaxPower() + "\n";
        info += "Crew: " + ship.getCrew().size() + "\n";
        if (sector != null) {
            info += "Sector: " + sector.getBiome().getName() + "\n";
            info += "Biome: " + sector.getBiome().getDescription();
        }

        dialog.text(info);
        dialog.button("OK", true);
        dialog.show(stage);
    }

    private void updateTimeButtons() {
        GameTimeState timeState = game.getGameState().getTimeState();
        if (timeState.isPaused()) {
            pauseButton.setText("RESUME");
            pauseButton.setColor(Color.RED);
        } else {
            pauseButton.setText("PAUSE");
            pauseButton.setColor(Color.WHITE);
        }

        slowButton.setChecked(timeState.getTimeScale() == 0.5f);
        fastButton.setChecked(timeState.getTimeScale() == 2.0f);
    }

    private void createLeftPanel() {
        leftPanel = new Table();
        leftPanel.setBackground(game.getDrawable("default-round"));
        leftPanel.pad(5f);

        Label title = new Label("CREW", game.getSkin(), "subtitle");
        leftPanel.add(title).padBottom(5f).row();

        updateCrewPortraits();
    }

    private void updateCrewPortraits() {
        leftPanel.clearChildren();
        Label title = new Label("CREW", game.getSkin(), "subtitle");
        leftPanel.add(title).padBottom(5f).row();

        Ship ship = game.getGameState().getCurrentShip();
        for (Crew crew : ship.getCrew()) {
            // Only show crew that are in a room (not moving between rooms)
            if (!crew.isMoving() || ship.getRoom(crew.getCurrentRoomX(), crew.getCurrentRoomY()) != null) {
                Table portrait = createCrewPortrait(crew);
                leftPanel.add(portrait).fillX().padBottom(3f).row();
            }
        }
    }

    private Table createCrewPortrait(Crew crew) {
        Table portrait = new Table();
        boolean isSelected = selectedCrew.contains(crew);

        portrait.setBackground(game.getDrawable("default-round"));
        if (isSelected) {
            portrait.setColor(Color.YELLOW);
        }
        portrait.pad(5f);

        // Crew profile picture (headshot)
        Texture crewProfile = PixelArtGenerator.generateCrewProfile(crew.getRole());
        Image crewImage = new Image(new TextureRegion(crewProfile));
        crewImage.setSize(32f, 32f);
        if (isSelected) {
            crewImage.setColor(Color.YELLOW);
        }
        portrait.add(crewImage).size(32f, 32f).padBottom(3f).row();

        // Crew name
        Label nameLabel = new Label(crew.getName(), game.getSkin());
        nameLabel.setFontScale(0.8f);
        portrait.add(nameLabel).left().row();

        // Role
        Label roleLabel = new Label(crew.getRole().getDisplayName(), game.getSkin());
        roleLabel.setFontScale(0.7f);
        roleLabel.setColor(Color.GRAY);
        portrait.add(roleLabel).left().row();

        // Health bar
        ProgressBar healthBar = new ProgressBar(0, crew.getMaxHealth(), 1, false, game.getSkin());
        healthBar.setValue(crew.getHealth());
        healthBar.setWidth(120f);
        portrait.add(healthBar).left().padTop(3f).row();

        // Make clickable - LEFT CLICK = SELECT
        portrait.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Handle left click for selection
                if (button == Input.Buttons.LEFT) {
                    handleCrewSelection(crew);
                    return true; // Consume left click
                }
                return false; // Don't consume right clicks
            }
        });

        return portrait;
    }

    private void createCenterPanel() {
        centerPanel = new Table();
        centerPanel.setBackground(game.getDrawable("default-round"));
        centerPanel.pad(10f);

        Label title = new Label("YOUR SHIP", game.getSkin(), "subtitle");
        centerPanel.add(title).padBottom(10f).row();

        // Create placeholder for tile map (will be added to stage separately)
        Ship ship = game.getGameState().getCurrentShip();
        // Don't add a placeholder - the tile map actor will be added directly to stage
        // Just add empty space to reserve the area
        centerPanel.add().size(ship.getGridWidth() * 60f, ship.getGridHeight() * 60f);

        // Don't create tile map here - it will be created after UI is laid out
    }

    private void updateShipGrid() {
        Ship ship = game.getGameState().getCurrentShip();

        // Create or update tile-based ship map
        if (shipTileMap == null) {
            shipTileMap = new ShipTileMapActor(ship, selectedCrew, (roomX, roomY, tileX, tileY, button) -> {
                handleTileMapClick(roomX, roomY, tileX, tileY, button);
            });

            // Position it to match the center panel's map placeholder
            // We'll position it after the UI is laid out
            if (centerPanel != null) {
                centerPanel.layout(); // Force layout
                // Convert centerPanel coordinates to stage coordinates
                Vector2 stagePos = centerPanel.localToStageCoordinates(new Vector2(0, 0));
                // Position map below the title (title is ~30px high, plus 10px padding)
                float mapX = stagePos.x + 10; // Padding from left edge of centerPanel
                float mapY = stagePos.y + 40; // Below title (30px title + 10px padding)
                shipTileMap.setPosition(mapX, mapY);
            }

            // Add directly to stage so it can receive input
            stage.addActor(shipTileMap);

            // Make sure it's touchable and visible
            shipTileMap.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
            shipTileMap.setVisible(true);
            
            // shipTileMap is added after mainTable, so it's naturally on top for input
            // The top bar is in a different area (top of screen), so shipTileMap won't cover it
            // shipTileMap only covers the center panel area where the ship grid is displayed
        } else {
            shipTileMap.setShip(ship);
            shipTileMap.setSelectedCrew(selectedCrew);

            // Update position in case UI moved
            if (centerPanel != null) {
                centerPanel.layout();
                Vector2 stagePos = centerPanel.localToStageCoordinates(new Vector2(0, 0));
                float mapX = stagePos.x + 10;
                float mapY = stagePos.y + 40;
                shipTileMap.setPosition(mapX, mapY);
                shipTileMap.setVisible(true);
                // Don't call toFront() - it causes z-ordering conflicts
            }
        }
    }

    /**
     * Handle clicks on the tile map
     */
    private void handleTileMapClick(int roomX, int roomY, int tileX, int tileY, int button) {
        Ship ship = game.getGameState().getCurrentShip();
        Room room = ship.getRoom(roomX, roomY);

        if (room == null || room.getType() == RoomType.EMPTY) {
            return;
        }

        if (tileX < 0 || tileY < 0) {
            // Clicked on room but not on a specific tile - allow selection of crew in room
            // Find first crew in this room and select them
            List<Crew> crewInRoom = ship.getCrew().stream()
                    .filter(c -> c.getCurrentRoomX() == roomX && c.getCurrentRoomY() == roomY)
                    .filter(c -> !c.isMoving())
                    .toList();
            if (!crewInRoom.isEmpty() && button == Input.Buttons.LEFT) {
                handleCrewSelection(crewInRoom.get(0));
            }
            return;
        }

        // Use existing tile click handler
        handleTileClick(room, tileX, tileY, button);
    }

    // createRoomButton removed - now using tile-based rendering

    private void createRightPanel() {
        rightPanel = new Table();
        rightPanel.setBackground(game.getDrawable("default-round"));
        rightPanel.pad(10f);

        if (game.getGameState().isInCombat()) {
            updateEnemyShipGrid();
        } else {
            Label label = new Label("No Enemy", game.getSkin());
            rightPanel.add(label);
        }
    }

    private void updateEnemyShipGrid() {
        rightPanel.clearChildren();

        Label title = new Label("ENEMY SHIP", game.getSkin(), "subtitle");
        rightPanel.add(title).padBottom(10f).row();

        CombatState combatState = game.getGameState().getCombatState();
        Ship enemyShip = combatState.getEnemyShip();

        // Enemy ship info
        Label hullLabel = new Label("Hull: " + enemyShip.getCurrentHull() + "/" + enemyShip.getMaxHull(), game.getSkin());
        rightPanel.add(hullLabel).left().row();

        Label shieldLabel = new Label("Shields: " + combatState.getEnemyShields() + "/" + combatState.getMaxEnemyShields(), game.getSkin());
        rightPanel.add(shieldLabel).left().row();

        // Enemy ship grid (miniaturized)
        Table grid = new Table();
        for (int y = 0; y < enemyShip.getGridHeight(); y++) {
            for (int x = 0; x < enemyShip.getGridWidth(); x++) {
                Room room = enemyShip.getRoom(x, y);
                if (room != null && room.getType() != RoomType.EMPTY) {
                    Button roomButton = createEnemyRoomButton(room, enemyShip);
                    grid.add(roomButton).size(40f, 40f).pad(1f);
                }
            }
            grid.row();
        }
        rightPanel.add(grid).padTop(10f);
    }

    private Button createEnemyRoomButton(Room room, Ship enemyShip) {
        Table roomTable = new Table();
        roomTable.setBackground(game.getDrawable("default-round"));
        roomTable.setColor(Color.RED);

        Label typeLabel = new Label(room.getType().getDisplayName().substring(0, Math.min(3, room.getType().getDisplayName().length())), game.getSkin());
        typeLabel.setFontScale(0.5f);
        roomTable.add(typeLabel);

        Button button = new Button(roomTable, game.getSkin());

        // Weapon targeting
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedWeapon != null && selectedWeapon.isCharged()) {
                    fireWeaponAtTarget(selectedWeapon, room, enemyShip);
                }
            }
        });

        return button;
    }

    private void createBottomPanel() {
        // If panel already exists, just update its contents instead of recreating
        if (bottomPanel != null) {
            updateBottomPanel();
            return;
        }

        // Initial creation only
        bottomPanel = new Table();
        bottomPanel.setBackground(game.getDrawable("default-round"));
        bottomPanel.pad(10f);

        // Left: Systems power management
        Table leftSide = new Table();
        createSystemsPowerSection(leftSide);
        bottomPanel.add(leftSide).expandX().fillX().padRight(10f);

        // Right: Weapons section
        Table rightSide = new Table();
        createWeaponsSection(rightSide);
        bottomPanel.add(rightSide).size(400f, 200f);
    }

    private void updateBottomPanel() {
        if (bottomPanel == null) return;

        // Clear existing content
        bottomPanel.clearChildren();

        // Rebuild content with updated data
        Table leftSide = new Table();
        createSystemsPowerSection(leftSide);
        bottomPanel.add(leftSide).expandX().fillX().padRight(10f);

        Table rightSide = new Table();
        createWeaponsSection(rightSide);
        bottomPanel.add(rightSide).size(400f, 200f);
    }

    private void createWeaponsSection(Table parent) {
        Table weaponsTable = new Table();
        weaponsTable.setBackground(game.getDrawable("default-round"));
        weaponsTable.pad(5f);

        Label title = new Label("WEAPONS", game.getSkin(), "subtitle");
        weaponsTable.add(title).padBottom(5f).row();

        Ship ship = game.getGameState().getCurrentShip();
        for (Weapon weapon : ship.getWeapons()) {
            Table weaponRow = createWeaponRow(weapon, ship);
            weaponsTable.add(weaponRow).fillX().padBottom(3f).row();
        }

        parent.add(weaponsTable).fillX();
    }

    private Table createWeaponRow(Weapon weapon, Ship ship) {
        Table row = new Table();
        row.setBackground(game.getDrawable("default-round"));
        row.pad(5f);

        if (weapon.isSelected()) {
            row.setColor(Color.YELLOW);
        }

        // Weapon icon
        Texture weaponIcon = PixelArtGenerator.generateWeaponIcon();
        Image weaponImage = new Image(new TextureRegion(weaponIcon));
        weaponImage.setSize(24f, 24f);
        row.add(weaponImage).size(24f, 24f).padRight(5f);

        // Weapon name
        Label nameLabel = new Label(weapon.getName(), game.getSkin());
        nameLabel.setFontScale(0.8f);
        row.add(nameLabel).width(100f).left();

        // Charge bar
        ProgressBar chargeBar = new ProgressBar(0, weapon.getMaxCharge(), 1, false, game.getSkin());
        chargeBar.setValue(weapon.getCurrentCharge());
        chargeBar.setWidth(150f);
        row.add(chargeBar).padLeft(10f);

        // Charge text
        Label chargeLabel = new Label(weapon.getCurrentCharge() + "/" + weapon.getMaxCharge(), game.getSkin());
        chargeLabel.setFontScale(0.7f);
        row.add(chargeLabel).padLeft(5f);

        // Power required
        Label powerLabel = new Label("Power: " + weapon.getPowerRequired(), game.getSkin());
        powerLabel.setFontScale(0.7f);
        row.add(powerLabel).padLeft(10f);

        // Select button
        TextButton selectButton = new TextButton(weapon.isSelected() ? "SELECTED" : "SELECT", game.getSkin());
        selectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Deselect all weapons
                for (Weapon w : ship.getWeapons()) {
                    w.setSelected(false);
                }
                weapon.setSelected(selectedWeapon == weapon ? false : true);
                selectedWeapon = weapon.isSelected() ? weapon : null;
                createBottomPanel(); // Refresh
            }
        });
        row.add(selectButton).padLeft(10f);

        // Autofire toggle
        TextButton autoButton = new TextButton(weapon.isAutoFire() ? "AUTO ON" : "AUTO OFF", game.getSkin());
        autoButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                weapon.setAutoFire(!weapon.isAutoFire());
                createBottomPanel();
            }
        });
        row.add(autoButton).padLeft(5f);

        return row;
    }

    private void createSystemsPowerSection(Table parent) {
        Table systemsTable = new Table();
        systemsTable.pad(5f);

        // Total energy display - show available power
        Ship ship = game.getGameState().getCurrentShip();
        int availablePower = ship.getAvailablePower();
        int maxPower = ship.getMaxPower();
        int usedPower = ship.getUsedPower();
        Label totalEnergyLabel = new Label("Total Energy: " + availablePower + "/" + maxPower + " (Used: " + usedPower + ")", game.getSkin());
        totalEnergyLabel.setFontScale(0.8f);
        systemsTable.add(totalEnergyLabel).colspan(10).padBottom(5f).row();

        List<Room> poweredRooms = ship.getRooms().stream()
                .filter(r -> r.getType() != RoomType.EMPTY && r.getMaxPower() > 0)
                .toList();

        // Arrange systems horizontally like in the image
        for (Room room : poweredRooms) {
            Table systemPanel = createSystemPowerPanel(room, ship);
            systemsTable.add(systemPanel).size(60f, 120f).pad(2f);
        }

        parent.add(systemsTable).fillX();
    }

    private Table createSystemPowerPanel(Room room, Ship ship) {
        // Create dark gray panel with rivets (like in the image)
        Table panel = new Table();
        panel.setBackground(game.getDrawable("default-round"));
        panel.setColor(0.3f, 0.3f, 0.3f, 1f); // Dark gray
        panel.pad(4f);

        // Vertical layout: indicator light, meter, icon
        panel.defaults().fillX().pad(2f);

        // Get current power level (read fresh each time)
        final int currentPowerLevel = room.getPowerLevel();

        // Green indicator light at top (small square)
        Table indicatorLight = new Table();
        indicatorLight.setBackground(game.getDrawable("default-round"));
        if (currentPowerLevel > 0) {
            indicatorLight.setColor(Color.GREEN);
        } else {
            indicatorLight.setColor(0.1f, 0.1f, 0.1f, 1f); // Dark when off
        }
        panel.add(indicatorLight).size(8f, 8f).row();

        // Vertical power blocks (greyish-white blocks stacked)
        Table powerBlocks = new Table();
        powerBlocks.defaults().size(16f, 5f).pad(1f);
        // Show up to 10 blocks
        int maxBlocks = 10;
        for (int i = maxBlocks - 1; i >= 0; i--) {
            Table block = new Table();
            block.setBackground(game.getDrawable("default-round"));
            if (i < currentPowerLevel) {
                // Powered block - greyish white
                block.setColor(0.9f, 0.9f, 0.9f, 1f);
            } else {
                // Unpowered block - dark grey
                block.setColor(0.15f, 0.15f, 0.15f, 1f);
            }
            powerBlocks.add(block).row();
        }
        panel.add(powerBlocks).size(18f, 80f).row();

        // System icon at bottom
        Texture iconTexture = PixelArtGenerator.generateSystemIcon(room.getType());
        Image iconImage = new Image(new TextureRegion(iconTexture));
        iconImage.setColor(Color.WHITE);
        panel.add(iconImage).size(20f, 20f).row();

        // Make entire panel clickable for power allocation
        final Room roomRef = room;
        panel.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    // Left click: add power (if available) - no max limit check
                    if (ship.getAvailablePower() > 0) {
                        ship.addPowerToRoom(roomRef);
                        createBottomPanel();
                        updateShipGrid();
                        updateTopBar();
                    }
                    return true;
                } else if (button == Input.Buttons.RIGHT) {
                    // Right click: remove power
                    if (roomRef.getPowerLevel() > 0) {
                        ship.removePowerFromRoom(roomRef);
                        createBottomPanel();
                        updateShipGrid();
                        updateTopBar();
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                // Show tooltip on hover
                showSystemTooltip(roomRef, event.getStageX(), event.getStageY());
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // Hide tooltip
                hideSystemTooltip();
            }
        });

        return panel;
    }

    private Table tooltipTable;

    private void showSystemTooltip(Room room, float x, float y) {
        if (tooltipTable != null) {
            tooltipTable.remove();
        }

        String description = getSystemDescription(room.getType());
        tooltipTable = new Table();
        tooltipTable.setBackground(game.getDrawable("default-round"));
        tooltipTable.setColor(0.2f, 0.2f, 0.2f, 0.9f);
        tooltipTable.pad(5f);

        Label tooltipLabel = new Label(room.getType().getDisplayName() + "\n" + description + "\nPower: " + room.getPowerLevel() + "/" + room.getMaxPower(), game.getSkin());
        tooltipLabel.setFontScale(0.7f);
        tooltipLabel.setWrap(true);
        tooltipTable.add(tooltipLabel).width(150f);
        tooltipTable.pack();
        tooltipTable.setPosition(x + 10, y + 10);
        stage.addActor(tooltipTable);
        tooltipTable.toFront();
    }

    private void hideSystemTooltip() {
        if (tooltipTable != null) {
            tooltipTable.remove();
            tooltipTable = null;
        }
    }

    private String getSystemDescription(RoomType type) {
        switch (type) {
            case BRIDGE:
                return "Ship navigation and command center.";
            case SHIELDS:
                return "Generates protective energy barriers.";
            case WEAPONS:
                return "Controls ship weapon systems.";
            case ENGINES:
                return "Provides propulsion and speed.";
            case MEDBAY:
                return "Heals and treats crew members.";
            case OXYGEN:
                return "Maintains breathable atmosphere.";
            case SENSORS:
                return "Detects enemies and hazards.";
            case DOORS:
                return "Controls internal door systems.";
            default:
                return "Ship system.";
        }
    }

    private void updateTopBar() {
        Ship ship = game.getGameState().getCurrentShip();
        hullBar.setValue(ship.getCurrentHull());
        hullLabel.setText(ship.getCurrentHull() + "/" + ship.getMaxHull());
        shieldBar.setValue(ship.getShields());
        shieldLabel.setText(ship.getShields() + "/" + ship.getMaxShields());
        scrapLabel.setText("Scrap: " + ship.getScrap());
        fuelLabel.setText("Fuel: " + ship.getFuel());
        powerLabel.setText("Energy: " + ship.getAvailablePower() + "/" + ship.getMaxPower() + " (Used: " + ship.getUsedPower() + ")");

        // Update debug timer
        if (timerLabel != null) {
            timerLabel.setText(String.format("Time: %.2fs", gameTime));
        }
    }

    private void updateOxygenSystem(float delta) {
        Ship ship = game.getGameState().getCurrentShip();

        // Find oxygen room
        Room oxygenRoom = ship.getRooms().stream()
                .filter(r -> r.getType() == RoomType.OXYGEN)
                .findFirst()
                .orElse(null);

        if (oxygenRoom == null) return;

        // If oxygen system is powered, recover oxygen in all tiles where oxygen is low
        if (oxygenRoom.getPowerLevel() > 0) {
            float fillRate = 0.8f * delta; // Fill rate per second (0.8 = 80% per second per power level)

            for (Room room : ship.getRooms()) {
                if (room.getType() == RoomType.EMPTY) continue;

                for (int tileX = 0; tileX < 2; tileX++) {
                    for (int tileY = 0; tileY < 2; tileY++) {
                        float currentOxygen = room.getTileOxygen(tileX, tileY);
                        // Only fill if oxygen is low (< 1.0)
                        if (currentOxygen < 1.0f) {
                            float newOxygen = Math.min(1.0f, currentOxygen + fillRate * oxygenRoom.getPowerLevel());
                            room.setTileOxygen(tileX, tileY, newOxygen);
                        }
                    }
                }
            }
        } else {
            // If oxygen system is not powered, slowly drain oxygen
            float drainRate = 0.1f * delta; // Drain rate per second (0.1 = 10% per second)

            for (Room room : ship.getRooms()) {
                if (room.getType() == RoomType.EMPTY) continue;

                for (int tileX = 0; tileX < 2; tileX++) {
                    for (int tileY = 0; tileY < 2; tileY++) {
                        float currentOxygen = room.getTileOxygen(tileX, tileY);
                        if (currentOxygen > 0.0f) {
                            float newOxygen = Math.max(0.0f, currentOxygen - drainRate);
                            room.setTileOxygen(tileX, tileY, newOxygen);
                        }
                    }
                }
            }
        }

        // Update ship grid to show oxygen changes
        updateShipGrid();
    }

    private void updateRepairSystems(float delta) {
        Ship ship = game.getGameState().getCurrentShip();

        // Repair systems based on crew in each room
        for (Room room : ship.getRooms()) {
            // Debug: log room health status with percentage
            if (room.getType() != RoomType.EMPTY && room.getHealth() < room.getMaxHealth()) {
                float healthPercent = (float)room.getHealth() / (float)room.getMaxHealth() * 100f;
                Gdx.app.debug("Repair", String.format("Room %s needs repair: %d/%d (%.1f%%)", 
                    room.getType().getDisplayName(), room.getHealth(), room.getMaxHealth(), healthPercent));
            }
            if (room.getType() == RoomType.EMPTY) continue;
            
            // Check if room needs repair (health < maxHealth)
            if (room.getHealth() < room.getMaxHealth()) {
                // Calculate total repair rate from all crew in this room
                float totalRepairRate = 0.0f;
                boolean hasRepairingCrew = false;
                List<Crew> repairingCrew = new ArrayList<>();
                
                for (int tileX = 0; tileX < 2; tileX++) {
                    for (int tileY = 0; tileY < 2; tileY++) {
                        Crew crew = room.getCrewAtTile(tileX, tileY);
                        if (crew != null && !crew.isMoving()) {
                            // Get repair stat (Engineering skill level)
                            int engineeringLevel = crew.getSkillLevel(Skill.ENGINEERING);
                            int engineeringXP = crew.getSkillXP(Skill.ENGINEERING);
                            
                            // Base repair rate: 20% per second for level 1, 10% for level 0
                            // A trained Engineer (level 1 = 100 XP) repairs 0% to 100% in 5 seconds = 20% per second
                            // Level 0 (10 XP) still repairs but slower: 10% per second = 10 seconds
                            // Even with 0 XP, allow basic repair at 5% per second
                            float repairRate = 0.05f; // Base repair rate for anyone
                            if (engineeringXP > 0) {
                                repairRate = 0.10f + (0.10f * engineeringLevel);
                            }
                            
                            totalRepairRate += repairRate;
                            hasRepairingCrew = true;
                            repairingCrew.add(crew);
                            
                            // Log when crew starts repairing
                            if (room.getHealth() < room.getMaxHealth()) {
                                Gdx.app.log("Repair", String.format("%s in %s: Engineering Level=%d (XP=%d), Repair Rate=%.2f%%/s", 
                                    crew.getName(), room.getType().getDisplayName(), engineeringLevel, engineeringXP, repairRate * 100f));
                            }
                        }
                    }
                }
                
                // Apply repair if crew is present
                if (hasRepairingCrew && totalRepairRate > 0.0f) {
                    int oldHealth = room.getHealth();
                    float oldHealthPercent = (float)oldHealth / (float)room.getMaxHealth() * 100f;
                    
                    // Repair rate is percentage per second
                    // Calculate repair amount in health points per second
                    float repairAmountPerSecond = (totalRepairRate * room.getMaxHealth());
                    float repairAmount = repairAmountPerSecond * delta;
                    
                    // Use fractional health accumulation to handle small increments
                    boolean healthIncreased = room.addFractionalHealth(repairAmount);
                    int newHealth = room.getHealth();
                    float newHealthPercent = (float)newHealth / (float)room.getMaxHealth() * 100f;
                    
                    // Log repair activity with percentage (log every frame when repairing, or when health increases)
                    if (healthIncreased || (oldHealth < room.getMaxHealth() && Math.random() < 0.1f)) {
                        Gdx.app.log("Repair", String.format("%s: %s repairing %s - Health: %d/%d (%.1f%%) -> %d/%d (%.1f%%) [Rate: %.2f HP/s, Delta: %.3f, Amount: %.4f]", 
                            room.getType().getDisplayName(),
                            repairingCrew.stream().map(Crew::getName).reduce((a, b) -> a + ", " + b).orElse("Unknown"),
                            room.getType().getDisplayName(),
                            oldHealth, room.getMaxHealth(), oldHealthPercent,
                            newHealth, room.getMaxHealth(), newHealthPercent,
                            repairAmountPerSecond, delta, repairAmount));
                    }
                }
            }
        }
        
        // Update ship grid to show repair progress
        updateShipGrid();
    }

    private void updateWeaponCharges(float delta) {
        Ship ship = game.getGameState().getCurrentShip();
        for (Weapon weapon : ship.getWeapons()) {
            // Check if weapon has power
            Room weaponRoom = ship.getRooms().stream()
                    .filter(r -> r.getType() == RoomType.WEAPONS)
                    .findFirst()
                    .orElse(null);

            if (weaponRoom != null && weaponRoom.getPowerLevel() >= weapon.getPowerRequired()) {
                weapon.charge(1); // Charge 1 per second
            }
        }
    }

    private void fireWeaponAtTarget(Weapon weapon, Room targetRoom, Ship enemyShip) {
        CombatState combatState = game.getGameState().getCombatState();

        int damage = weapon.getDamage();

        // Check shields
        if (combatState.getEnemyShields() > 0) {
            int shieldDamage = Math.min(damage, combatState.getEnemyShields());
            combatState.setEnemyShields(combatState.getEnemyShields() - shieldDamage);
            damage -= shieldDamage;
        }

        // Apply hull damage
        if (damage > 0) {
            enemyShip.setCurrentHull(enemyShip.getCurrentHull() - damage);
            targetRoom.setHealth(targetRoom.getHealth() - damage);

            // Chance for fire/breach
            if (Math.random() < 0.2) {
                targetRoom.setFire(true);
            }
        }

        weapon.fire();
        updateEnemyShipGrid();
    }


    // Handle keyboard input via InputListener
    private void setupKeyboardInput() {
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    game.getGameState().getTimeState().togglePause();
                    updateTimeButtons();
                    return true;
                }
                return false;
            }
        });
    }
}

