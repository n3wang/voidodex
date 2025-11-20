package io.github.n3wang.voidcodex.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import io.github.n3wang.voidcodex.model.Crew;
import io.github.n3wang.voidcodex.model.Room;
import io.github.n3wang.voidcodex.model.RoomType;
import io.github.n3wang.voidcodex.model.Ship;
import io.github.n3wang.voidcodex.util.PixelArtGenerator;

import java.util.List;

/**
 * Tile-based rendering actor for ship grid.
 * Each room is 2x2 tiles, and the ship grid is rendered as a tile map.
 */
public class ShipTileMapActor extends Actor {
    private Ship ship;
    private float tileSize = 60f; // Size of each tile in pixels
    private float roomTileSize = 28f; // Size of tiles within a room (2x2)
    private List<Crew> selectedCrew;
    private TileClickHandler clickHandler;
    
    public interface TileClickHandler {
        void onTileClick(int roomX, int roomY, int tileX, int tileY, int button);
    }
    
    public ShipTileMapActor(Ship ship, List<Crew> selectedCrew, TileClickHandler clickHandler) {
        this.ship = ship;
        this.selectedCrew = selectedCrew;
        this.clickHandler = clickHandler;
        
        // Set actor size based on ship grid
        setSize(ship.getGridWidth() * tileSize, ship.getGridHeight() * tileSize);
        
        // Set visible color to white so textures render properly
        setColor(Color.WHITE);
        
        // Make sure the actor can receive touch events
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        
        // Add input listener for tile clicks
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // x and y are already in local coordinates relative to this actor
                // In Scene2D, Y=0 is at bottom
                // Debug: log to verify clicks are received
                // Gdx.app.log("TileMap", "Click received at " + x + "," + y + " button=" + button);
                handleTileClick(x, y, button);
                return true; // Consume the event
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                // Also handle on touchUp as backup
                handleTileClick(x, y, button);
            }
        });
    }
    
    private void handleTileClick(float localX, float localY, int button) {
        if (ship == null || clickHandler == null) {
            // Debug: log if handler is null
            // Gdx.app.log("TileMap", "Handler is null or ship is null");
            return;
        }
        
        // Debug: log click
        // Gdx.app.log("TileMap", "Click at local " + localX + "," + localY + " button=" + button);
        
        // localX and localY are already in local coordinates (relative to this actor)
        // In Scene2D, Y=0 is at bottom, so no flipping needed
        
        // Calculate which room and tile was clicked
        int roomX = (int) (localX / tileSize);
        int roomY = (int) (localY / tileSize);
        
        // Clamp to valid room coordinates
        if (roomX < 0 || roomX >= ship.getGridWidth() || 
            roomY < 0 || roomY >= ship.getGridHeight()) {
            return;
        }
        
        // Calculate tile position within room (2x2 grid)
        float roomLocalX = localX - (roomX * tileSize);
        float roomLocalY = localY - (roomY * tileSize);
        
        // Account for room padding/margin
        float roomPadding = (tileSize - (roomTileSize * 2)) / 2;
        float tileLocalX = roomLocalX - roomPadding;
        float tileLocalY = roomLocalY - roomPadding;
        
        int tileX = -1;
        int tileY = -1;
        
        // Determine which tile within the room (0-1 for 2x2 grid)
        if (tileLocalX >= 0 && tileLocalX < roomTileSize) {
            tileX = 0;
        } else if (tileLocalX >= roomTileSize && tileLocalX < roomTileSize * 2) {
            tileX = 1;
        }
        
        // Y=0 is at bottom in Scene2D, so tileY 0 is bottom row
        if (tileLocalY >= 0 && tileLocalY < roomTileSize) {
            tileY = 0; // Bottom row in room
        } else if (tileLocalY >= roomTileSize && tileLocalY < roomTileSize * 2) {
            tileY = 1; // Top row in room
        }
        
        // If clicked within a valid tile, notify handler
        if (tileX >= 0 && tileX < 2 && tileY >= 0 && tileY < 2) {
            clickHandler.onTileClick(roomX, roomY, tileX, tileY, button);
        } else {
            // Clicked on room but not on a specific tile - still notify handler
            clickHandler.onTileClick(roomX, roomY, -1, -1, button);
        }
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (ship == null) return;
        
        Color originalColor = batch.getColor();
        Color actorColor = getColor();
        float finalAlpha = actorColor.a * parentAlpha;
        batch.setColor(actorColor.r, actorColor.g, actorColor.b, finalAlpha);
        
        // Render each room
        // Note: In Scene2D, Y=0 is at bottom, so we render from bottom to top
        for (int roomY = 0; roomY < ship.getGridHeight(); roomY++) {
            for (int roomX = 0; roomX < ship.getGridWidth(); roomX++) {
                Room room = ship.getRoom(roomX, roomY);
                float roomScreenX = getX() + (roomX * tileSize);
                // Y=0 is at bottom in Scene2D, so render normally
                float roomScreenY = getY() + (roomY * tileSize);
                
                if (room != null && room.getType() != RoomType.EMPTY) {
                    drawRoom(batch, room, roomScreenX, roomScreenY, finalAlpha);
                } else {
                    // Draw empty space
                    drawEmptySpace(batch, roomScreenX, roomScreenY, finalAlpha);
                }
            }
        }
        
        batch.setColor(originalColor);
    }
    
    private void drawRoom(Batch batch, Room room, float x, float y, float alpha) {
        // Draw room background FIRST (so tiles appear on top)
        Texture roomTexture = PixelArtGenerator.generateRoomSprite(room.getType());
        TextureRegion roomRegion = new TextureRegion(roomTexture);
        
        // Room background (full tile size) - ensure it's visible
        Color roomColor = Color.WHITE;
        if (room.getHealth() < room.getMaxHealth() * 0.5f) {
            roomColor = new Color(1f, 0.5f, 0.5f, 1f); // Light red for damaged
        }
        
        batch.setColor(roomColor.r, roomColor.g, roomColor.b, alpha);
        batch.draw(roomRegion, x, y, tileSize, tileSize);
        
        // Draw room border/outline
        batch.setColor(0.4f, 0.4f, 0.4f, alpha);
        drawRectOutline(batch, x, y, tileSize, tileSize);
        
        // Draw 2x2 tile grid within room
        float roomPadding = (tileSize - (roomTileSize * 2)) / 2;
        float tileStartX = x + roomPadding;
        float tileStartY = y + roomPadding;
        
        // Render tiles (tileY 0 is bottom, 1 is top in room coords, matching Scene2D Y=0 at bottom)
        for (int tileY = 0; tileY < 2; tileY++) {
            for (int tileX = 0; tileX < 2; tileX++) {
                float tileXPos = tileStartX + (tileX * roomTileSize);
                // Y=0 is at bottom, so render normally
                float tileYPos = tileStartY + (tileY * roomTileSize);
                
                // Draw tile background - use a semi-transparent overlay so room shows through
                // Make it lighter and more transparent so the room texture is visible
                batch.setColor(0.5f, 0.5f, 0.6f, alpha * 0.2f); // Very transparent
                Texture tileBgTexture = PixelArtGenerator.generateRoomSprite(RoomType.EMPTY);
                batch.draw(tileBgTexture, tileXPos, tileYPos, roomTileSize, roomTileSize);
                
                // Draw tile border - make it subtle
                batch.setColor(0.4f, 0.4f, 0.4f, alpha * 0.5f);
                drawRectOutline(batch, tileXPos, tileYPos, roomTileSize, roomTileSize);
                
                // Draw crew if present
                Crew crewAtTile = room.getCrewAtTile(tileX, tileY);
                if (crewAtTile != null) {
                    drawCrew(batch, crewAtTile, tileXPos, tileYPos, roomTileSize, alpha);
                }
            }
        }
        
        // Draw room hazards (fire, breach) as overlay
        if (room.hasFire()) {
            Texture fireTexture = PixelArtGenerator.generateFireSprite();
            batch.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
            batch.draw(fireTexture, x + tileSize - 20, y + tileSize - 20, 20, 20);
        }
        if (room.hasBreach()) {
            Texture breachTexture = PixelArtGenerator.generateBreachSprite();
            batch.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
            batch.draw(breachTexture, x + 5, y + tileSize - 20, 20, 20);
        }
        
        // Draw room info (power, health) as text overlay
        // Note: For text, you'd need a BitmapFont - for now, we'll skip it
        // or use a label actor overlay
    }
    
    private void drawCrew(Batch batch, Crew crew, float x, float y, float size, float alpha) {
        // Find crew index for sprite
        int crewIndex = ship.getCrew().indexOf(crew);
        Texture crewTexture = PixelArtGenerator.generateCrewSprite(crewIndex);
        
        // Always draw crew sprite in white - selection is shown by highlight border
        batch.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
        batch.draw(crewTexture, x + 2, y + 2, size - 4, size - 4);
        
        // Draw selection highlight - use semi-transparent yellow
        if (selectedCrew != null && selectedCrew.contains(crew)) {
            // Use lower alpha (0.3) for highlight so crew is still visible
            batch.setColor(Color.YELLOW.r, Color.YELLOW.g, Color.YELLOW.b, alpha * 0.3f);
            drawRect(batch, x, y, size, size);
            // Also draw a thin border for better visibility
            batch.setColor(Color.YELLOW.r, Color.YELLOW.g, Color.YELLOW.b, alpha * 0.7f);
            drawRectOutline(batch, x, y, size, size);
        }
    }
    
    private void drawEmptySpace(Batch batch, float x, float y, float alpha) {
        // Draw empty/void space - use a visible gray color
        batch.setColor(0.3f, 0.3f, 0.3f, alpha);
        Texture emptyTexture = PixelArtGenerator.generateRoomSprite(RoomType.EMPTY);
        batch.draw(emptyTexture, x, y, tileSize, tileSize);
        
        // Draw border
        batch.setColor(0.2f, 0.2f, 0.2f, alpha);
        drawRectOutline(batch, x, y, tileSize, tileSize);
    }
    
    private void drawRect(Batch batch, float x, float y, float width, float height) {
        // Draw filled rectangle using a simple texture
        Texture fillTexture = PixelArtGenerator.generateRoomSprite(RoomType.EMPTY);
        batch.draw(fillTexture, x, y, width, height);
    }
    
    private void drawRectOutline(Batch batch, float x, float y, float width, float height) {
        // Draw rectangle outline using lines
        float lineWidth = 2f;
        Texture lineTexture = PixelArtGenerator.generateRoomSprite(RoomType.EMPTY);
        // Top
        batch.draw(lineTexture, x, y + height - lineWidth, width, lineWidth);
        // Bottom
        batch.draw(lineTexture, x, y, width, lineWidth);
        // Left
        batch.draw(lineTexture, x, y, lineWidth, height);
        // Right
        batch.draw(lineTexture, x + width - lineWidth, y, lineWidth, height);
    }
    
    public void setShip(Ship ship) {
        this.ship = ship;
        if (ship != null) {
            setSize(ship.getGridWidth() * tileSize, ship.getGridHeight() * tileSize);
        }
    }
    
    public void setSelectedCrew(List<Crew> selectedCrew) {
        this.selectedCrew = selectedCrew;
    }
    
    public float getTileSize() {
        return tileSize;
    }
    
    public void setTileSize(float tileSize) {
        this.tileSize = tileSize;
        this.roomTileSize = tileSize * 0.47f; // Slightly less than half to allow padding
        if (ship != null) {
            setSize(ship.getGridWidth() * tileSize, ship.getGridHeight() * tileSize);
        }
    }
}

