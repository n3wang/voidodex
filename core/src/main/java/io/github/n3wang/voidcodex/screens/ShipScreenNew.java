package io.github.n3wang.voidcodex.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
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
    private Table centerPanel; // Player ship grid
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
        }
        
        // Always render (even when paused)
        super.render(delta);
        
        // Update UI
        updateTopBar();
    }
    
    /**
     * Handle crew selection logic
     */
    private void handleCrewSelection(Crew crew) {
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
     * Handle tile click - RIGHT CLICK = SELECT, LEFT CLICK = MOVE
     */
    private void handleTileClick(Room room, int tileX, int tileY, int button) {
        Ship ship = game.getGameState().getCurrentShip();
        Crew crewAtTile = room.getCrewAtTile(tileX, tileY);
        
        if (button == Input.Buttons.RIGHT) {
            // RIGHT CLICK: Select crew at tile
            if (crewAtTile != null) {
                handleCrewSelection(crewAtTile);
            }
        } else if (button == Input.Buttons.LEFT) {
            // LEFT CLICK: Move selected crew to this tile
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
        mainTable.pad(5f);

        // Top Bar
        createTopBar();
        mainTable.add(topBar).fillX().padBottom(5f).row();

        // Main content area
        Table contentTable = new Table();

        // Left Panel - Crew Portraits
        createLeftPanel();
        contentTable.add(leftPanel).size(150f, 500f).padRight(5f);

        // Center Panel - Player Ship Grid
        createCenterPanel();
        contentTable.add(centerPanel).expand().fill().padRight(5f);

        // Right Panel - Enemy Ship Grid (only in combat)
        createRightPanel();
        if (game.getGameState().isInCombat()) {
            contentTable.add(rightPanel).size(300f, 500f);
        }

        mainTable.add(contentTable).expand().fill().padBottom(5f).row();

        // Bottom Panel - Weapons and Systems
        createBottomPanel();
        mainTable.add(bottomPanel).fillX();

        stage.addActor(mainTable);
        setupKeyboardInput();
    }

    private void createTopBar() {
        topBar = new Table();
        topBar.setBackground(game.getDrawable("default-round"));
        topBar.pad(5f);

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

        // Crew sprite
        int crewIndex = game.getGameState().getCurrentShip().getCrew().indexOf(crew);
        Texture crewTexture = PixelArtGenerator.generateCrewSprite(crewIndex);
        Image crewImage = new Image(new TextureRegion(crewTexture));
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

        // Make clickable - RIGHT CLICK = SELECT
        portrait.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // clicked() is typically called for left clicks, but check button anyway
                if (event.getButton() == Input.Buttons.RIGHT) {
                    handleCrewSelection(crew);
                }
            }
            
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Handle right click in touchDown (this is more reliable for right clicks)
                if (button == Input.Buttons.RIGHT) {
                    handleCrewSelection(crew);
                    return true; // Consume right click
                }
                // For left clicks, return false so clicked() can handle it
                return super.touchDown(event, x, y, pointer, button);
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

        updateShipGrid();
    }

    private void updateShipGrid() {
        centerPanel.clearChildren();
        Label title = new Label("YOUR SHIP", game.getSkin(), "subtitle");
        centerPanel.add(title).padBottom(10f).row();

        Ship ship = game.getGameState().getCurrentShip();
        Table grid = new Table();

        for (int y = 0; y < ship.getGridHeight(); y++) {
            for (int x = 0; x < ship.getGridWidth(); x++) {
                Room room = ship.getRoom(x, y);
                if (room != null) {
                    Button roomButton = createRoomButton(room, ship);
                    grid.add(roomButton).size(120f, 120f).pad(2f); // Larger rooms
                } else {
                    Table empty = new Table();
                    empty.setBackground(game.getDrawable("default-round"));
                    grid.add(empty).size(120f, 120f).pad(2f);
                }
            }
            grid.row();
        }

        centerPanel.add(grid);
    }

    private Button createRoomButton(final Room room, Ship ship) {
        Table roomTable = new Table();
        
        // Use pixel art sprite for room background
        Texture roomTexture = PixelArtGenerator.generateRoomSprite(room.getType());
        TextureRegion region = new TextureRegion(roomTexture);
        roomTable.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(region));
        
        if (room.getHealth() < room.getMaxHealth() * 0.5f) {
            roomTable.setColor(Color.RED); // Damaged - tint red
        }

        // Room type
        Label typeLabel = new Label(room.getType().getDisplayName(), game.getSkin());
        typeLabel.setFontScale(0.5f);
        typeLabel.setAlignment(Align.center);
        roomTable.add(typeLabel).row();

        // Power level
        if (room.getMaxPower() > 0) {
            Label powerLabel = new Label("P:" + room.getPowerLevel() + "/" + room.getMaxPower(), game.getSkin());
            powerLabel.setFontScale(0.5f);
            roomTable.add(powerLabel).row();
        }

        // Health
        Label healthLabel = new Label("H:" + room.getHealth(), game.getSkin());
        healthLabel.setFontScale(0.5f);
        roomTable.add(healthLabel).row();

        // Hazards - use pixel art sprites
        if (room.hasFire()) {
            Image fireImage = new Image(new TextureRegion(PixelArtGenerator.generateFireSprite()));
            fireImage.setSize(20f, 20f);
            roomTable.add(fireImage).row();
        }
        if (room.hasBreach()) {
            Image breachImage = new Image(new TextureRegion(PixelArtGenerator.generateBreachSprite()));
            breachImage.setSize(20f, 20f);
            roomTable.add(breachImage).row();
        }

        // Crew indicator - show 2x2 grid of tiles with crew (clickable tiles)
        Table tileGrid = new Table();
        for (int tileY = 0; tileY < 2; tileY++) {
            for (int tileX = 0; tileX < 2; tileX++) {
                final int finalTileX = tileX;
                final int finalTileY = tileY;
                Crew crewAtTile = room.getCrewAtTile(tileX, tileY);
                
                Table tileCell = new Table();
                tileCell.setBackground(game.getDrawable("default-round"));
                tileCell.setColor(Color.DARK_GRAY);
                
                if (crewAtTile != null) {
                    int crewIndex = ship.getCrew().indexOf(crewAtTile);
                    Texture crewTexture = PixelArtGenerator.generateCrewSprite(crewIndex);
                    Image crewImage = new Image(new TextureRegion(crewTexture));
                    crewImage.setSize(12f, 12f);
                    if (selectedCrew.contains(crewAtTile)) {
                        crewImage.setColor(Color.YELLOW); // Highlight selected
                        tileCell.setColor(Color.YELLOW);
                    }
                    tileCell.add(crewImage).size(12f, 12f);
                }
                
                // Make each tile clickable
                Button tileButton = new Button(tileCell, game.getSkin());
                tileButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        // clicked() is typically called for left clicks
                        // Default to left button if not set
                        int button = event.getButton();
                        if (button == -1) {
                            button = Input.Buttons.LEFT; // Default to left if not set
                        }
                        handleTileClick(room, finalTileX, finalTileY, button);
                    }
                    
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        // Handle right click in touchDown (more reliable for right clicks)
                        if (button == Input.Buttons.RIGHT) {
                            handleTileClick(room, finalTileX, finalTileY, Input.Buttons.RIGHT);
                            return true; // Consume right click
                        }
                        // For left clicks, return false so clicked() can handle it
                        return super.touchDown(event, x, y, pointer, button);
                    }
                });
                
                tileGrid.add(tileButton).size(12f, 12f).pad(1f);
            }
            tileGrid.row();
        }
        roomTable.add(tileGrid).row();

        Button button = new Button(roomTable, game.getSkin());

        return button;
    }

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
        systemsTable.setBackground(game.getDrawable("default-round"));
        systemsTable.pad(5f);

        Label title = new Label("SYSTEMS POWER", game.getSkin(), "subtitle");
        systemsTable.add(title).colspan(2).padBottom(5f).row();

        Ship ship = game.getGameState().getCurrentShip();
        List<Room> poweredRooms = ship.getRooms().stream()
                .filter(r -> r.getType() != RoomType.EMPTY && r.getMaxPower() > 0)
                .toList();

        for (Room room : poweredRooms) {
            Table systemRow = createSystemPowerRow(room, ship);
            systemsTable.add(systemRow).fillX().padBottom(2f).row();
        }

        parent.add(systemsTable).fillX();
    }

    private Table createSystemPowerRow(Room room, Ship ship) {
        Table row = new Table();
        row.setBackground(game.getDrawable("default-round"));
        row.pad(3f);

        // System name
        Label nameLabel = new Label(room.getType().getDisplayName(), game.getSkin());
        nameLabel.setFontScale(0.75f);
        row.add(nameLabel).width(100f).left();

        // Power boxes stacked vertically (bottom to top) - use pixel art
        Table powerBoxes = new Table();
        // Stack from bottom to top - iterate in reverse
        for (int i = room.getMaxPower() - 1; i >= 0; i--) {
            boolean isPowered = i < room.getPowerLevel();
            Texture powerBoxTexture = PixelArtGenerator.generatePowerBoxSprite(isPowered);
            
            final Room roomRef = room;

            // Create button with image
            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
            style.imageUp = new TextureRegionDrawable(new TextureRegion(powerBoxTexture));
            ImageButton boxButton = new ImageButton(style);
            boxButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (event.getButton() == Input.Buttons.LEFT) {
                        // Left click: add power
                        ship.addPowerToRoom(roomRef);
                    } else if (event.getButton() == Input.Buttons.RIGHT) {
                        // Right click: remove power
                        ship.removePowerFromRoom(roomRef);
                    }
                    createBottomPanel();
                    updateShipGrid();
                    updateTopBar();
                }
            });

            powerBoxes.add(boxButton).size(18f, 18f).pad(1f).row(); // Stack vertically
        }

        row.add(powerBoxes).padLeft(5f);

        // Power level
        Label powerLevelLabel = new Label("[" + room.getPowerLevel() + "/" + room.getMaxPower() + "]", game.getSkin());
        powerLevelLabel.setFontScale(0.7f);
        row.add(powerLevelLabel).width(50f).right().padLeft(5f);

        return row;
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

