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
    
    private void updateCrewMovement(float delta) {
        Ship ship = game.getGameState().getCurrentShip();
        boolean needsUpdate = false;
        
        for (Crew crew : ship.getCrew()) {
            if (crew.isMoving()) {
                // Movement speed: 2 tiles per second = 0.5 seconds per tile
                crew.setMovementProgress(crew.getMovementProgress() + crew.getMovementSpeed() * delta);
                
                if (crew.getMovementProgress() >= 1.0f) {
                    // Reached next room in path
                    crew.setCurrentRoomX(crew.getNextRoomX());
                    crew.setCurrentRoomY(crew.getNextRoomY());
                    crew.setMovementProgress(0.0f);
                    
                    // Check if reached final destination
                    if (crew.getCurrentRoomX() == crew.getTargetRoomX() && 
                        crew.getCurrentRoomY() == crew.getTargetRoomY()) {
                        // Reached destination, assign quarter position
                        crew.setMoving(false);
                        needsUpdate = true;
                    } else {
                        // Continue to next room in path
                        List<int[]> path = Pathfinding.findPath(ship,
                                crew.getCurrentRoomX(), crew.getCurrentRoomY(),
                                crew.getTargetRoomX(), crew.getTargetRoomY());
                        
                        if (!path.isEmpty()) {
                            int[] nextStep = path.get(0);
                            crew.setNextRoomX(nextStep[0]);
                            crew.setNextRoomY(nextStep[1]);
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
            Table portrait = createCrewPortrait(crew);
            leftPanel.add(portrait).fillX().padBottom(3f).row();
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

        // Make clickable - LEFT CLICK = SELECT
        portrait.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Only handle left clicks for selection
                if (event.getButton() == Input.Buttons.LEFT) {
                    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                        // Shift-left-click: toggle selection
                        if (selectedCrew.contains(crew)) {
                            selectedCrew.remove(crew);
                        } else {
                            selectedCrew.add(crew);
                        }
                    } else {
                        // Regular left-click: single select
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

    private Button createRoomButton(Room room, Ship ship) {
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

        // Crew indicator - show crew sprites in quarters (max 4 crew per room)
        List<Crew> crewInRoom = ship.getCrew().stream()
                .filter(c -> c.getCurrentRoomX() == room.getX() && c.getCurrentRoomY() == room.getY())
                .limit(4) // Max 4 crew per room
                .toList();
        
        // Create a 2x2 grid for crew positions (quarters)
        Table crewGrid = new Table();
        for (int quarter = 0; quarter < 4; quarter++) {
            final int quarterPos = quarter;
            Crew crewInQuarter = crewInRoom.stream()
                    .filter(c -> c.getQuarterPosition() == quarterPos)
                    .findFirst()
                    .orElse(null);
            
            if (crewInQuarter != null) {
                int crewIndex = ship.getCrew().indexOf(crewInQuarter);
                Texture crewTexture = PixelArtGenerator.generateCrewSprite(crewIndex);
                Image crewImage = new Image(new TextureRegion(crewTexture));
                crewImage.setSize(20f, 20f);
                if (selectedCrew.contains(crewInQuarter)) {
                    crewImage.setColor(Color.YELLOW); // Highlight selected
                }
                crewGrid.add(crewImage).size(20f, 20f).pad(2f);
            } else {
                // Empty quarter
                Table emptyQuarter = new Table();
                crewGrid.add(emptyQuarter).size(20f, 20f).pad(2f);
            }
            
            // New row after every 2 quarters (top row: 0,1 bottom row: 2,3)
            if (quarter == 1) {
                crewGrid.row();
            }
        }
        roomTable.add(crewGrid).row();

        Button button = new Button(roomTable, game.getSkin());
        
        // Handle clicks - LEFT CLICK = SELECT, RIGHT CLICK = MOVE/ACTION
        final List<Crew> crewInRoomFinal = new ArrayList<>(crewInRoom);
        final Room roomFinal = room;
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Handle left click for selection or movement
                if (event.getButton() == Input.Buttons.LEFT) {
                    // If crew is selected and moving, clicking a room should move them
                    if (!selectedCrew.isEmpty() && roomFinal.getType() != RoomType.EMPTY) {
                        // Check if any selected crew is moving
                        boolean anyMoving = selectedCrew.stream().anyMatch(Crew::isMoving);
                        
                        if (!anyMoving) {
                            // No crew moving, check if room has space and move them
                            List<Crew> currentCrewInRoom = ship.getCrew().stream()
                                    .filter(c -> c.getCurrentRoomX() == roomFinal.getX() && 
                                                c.getCurrentRoomY() == roomFinal.getY())
                                    .toList();
                            
                            int availableSpots = 4 - currentCrewInRoom.size();
                            if (availableSpots > 0) {
                                int crewToMove = Math.min(selectedCrew.size(), availableSpots);
                                
                                // Move crew to available quarters
                                int moved = 0;
                                for (Crew crew : selectedCrew) {
                                    if (moved >= crewToMove) break;
                                    
                                    // Find an available quarter (0-3)
                                    int availableQuarter = -1;
                                    for (int q = 0; q < 4; q++) {
                                        final int quarter = q;
                                        boolean quarterOccupied = currentCrewInRoom.stream()
                                                .anyMatch(c -> c.getQuarterPosition() == quarter);
                                        if (!quarterOccupied) {
                                            availableQuarter = quarter;
                                            break;
                                        }
                                    }
                                    
                                    if (availableQuarter >= 0) {
                                        // Use pathfinding to move crew
                                        List<int[]> path = Pathfinding.findPath(ship, 
                                                crew.getCurrentRoomX(), crew.getCurrentRoomY(),
                                                roomFinal.getX(), roomFinal.getY());
                                        
                                        if (!path.isEmpty()) {
                                            // Set target and start movement
                                            crew.setTargetRoomX(roomFinal.getX());
                                            crew.setTargetRoomY(roomFinal.getY());
                                            crew.setMoving(true);
                                            crew.setMovementProgress(0.0f);
                                            
                                            // Set first step in path
                                            int[] firstStep = path.get(0);
                                            crew.setNextRoomX(firstStep[0]);
                                            crew.setNextRoomY(firstStep[1]);
                                            
                                            // Store quarter for when they arrive
                                            crew.setQuarterPosition(availableQuarter);
                                            moved++;
                                            
                                            // Update currentCrewInRoom for next iteration
                                            currentCrewInRoom = ship.getCrew().stream()
                                                    .filter(c -> c.getCurrentRoomX() == roomFinal.getX() && 
                                                                c.getCurrentRoomY() == roomFinal.getY())
                                                    .toList();
                                        }
                                    }
                                }
                                
                                updateCrewPortraits();
                                updateShipGrid();
                                return;
                            }
                        }
                    }
                    
                    // LEFT CLICK: Select crew in room (if not moving them)
                    if (!crewInRoomFinal.isEmpty()) {
                        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                            // Shift-left-click: add to selection
                            for (Crew crew : crewInRoomFinal) {
                                if (!selectedCrew.contains(crew)) {
                                    selectedCrew.add(crew);
                                }
                            }
                        } else {
                            // Regular left-click: select first crew in room
                            selectedCrew.clear();
                            selectedCrew.add(crewInRoomFinal.get(0));
                        }
                        updateCrewPortraits();
                        updateShipGrid();
                    }
                }
            }
            
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Handle right click for movement (button 1 = right mouse button)
                if (button == 1) {
                    // RIGHT CLICK: Move selected crew to this room (or action/attack)
                    if (!selectedCrew.isEmpty() && roomFinal.getType() != RoomType.EMPTY) {
                        // Check if room has space (max 4 crew)
                        List<Crew> currentCrewInRoom = ship.getCrew().stream()
                                .filter(c -> c.getCurrentRoomX() == roomFinal.getX() && 
                                            c.getCurrentRoomY() == roomFinal.getY())
                                .toList();
                        
                        int availableSpots = 4 - currentCrewInRoom.size();
                        if (availableSpots > 0) {
                            int crewToMove = Math.min(selectedCrew.size(), availableSpots);
                            
                            // Move crew to available quarters
                            int moved = 0;
                            for (Crew crew : selectedCrew) {
                                if (moved >= crewToMove) break;
                                
                                // Find an available quarter (0-3)
                                int availableQuarter = -1;
                                for (int q = 0; q < 4; q++) {
                                    final int quarter = q;
                                    boolean quarterOccupied = currentCrewInRoom.stream()
                                            .anyMatch(c -> c.getQuarterPosition() == quarter);
                                    if (!quarterOccupied) {
                                        availableQuarter = quarter;
                                        break;
                                    }
                                }
                                
                                if (availableQuarter >= 0) {
                                    // Use pathfinding to move crew
                                    List<int[]> path = Pathfinding.findPath(ship, 
                                            crew.getCurrentRoomX(), crew.getCurrentRoomY(),
                                            roomFinal.getX(), roomFinal.getY());
                                    
                                    if (!path.isEmpty()) {
                                        // Set target and start movement
                                        crew.setTargetRoomX(roomFinal.getX());
                                        crew.setTargetRoomY(roomFinal.getY());
                                        crew.setMoving(true);
                                        crew.setMovementProgress(0.0f);
                                        
                                        // Set first step in path
                                        int[] firstStep = path.get(0);
                                        crew.setNextRoomX(firstStep[0]);
                                        crew.setNextRoomY(firstStep[1]);
                                        
                                        // Store path for this crew (we'll need to track it)
                                        crew.setQuarterPosition(availableQuarter); // Store quarter for when they arrive
                                        moved++;
                                        
                                        // Update currentCrewInRoom for next iteration
                                        currentCrewInRoom = ship.getCrew().stream()
                                                .filter(c -> c.getCurrentRoomX() == roomFinal.getX() && 
                                                            c.getCurrentRoomY() == roomFinal.getY())
                                                .toList();
                                    }
                                }
                            }
                            
                            // Don't clear selection - allow multiple moves
                            updateCrewPortraits();
                            updateShipGrid();
                        }
                        return true; // Consume the event
                    }
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });

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

