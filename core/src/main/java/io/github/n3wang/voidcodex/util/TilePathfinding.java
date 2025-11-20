package io.github.n3wang.voidcodex.util;

import io.github.n3wang.voidcodex.model.Room;
import io.github.n3wang.voidcodex.model.RoomType;
import io.github.n3wang.voidcodex.model.Ship;

import java.util.ArrayList;
import java.util.List;

/**
 * Pathfinding for tile-based movement within and between rooms.
 * Each room is 2x2 tiles, and movement is tile-to-tile (horizontal/vertical only).
 */
public class TilePathfinding {
    
    /**
     * Find path from start tile to target tile.
     * Coordinates: (roomX, roomY, tileX, tileY) where tileX and tileY are 0-1.
     * Returns list of tile coordinates (roomX, roomY, tileX, tileY) in order.
     */
    public static List<int[]> findPath(Ship ship, 
            int startRoomX, int startRoomY, int startTileX, int startTileY,
            int targetRoomX, int targetRoomY, int targetTileX, int targetTileY) {
        
        List<int[]> path = new ArrayList<>();
        
        // If already at destination, return empty path
        if (startRoomX == targetRoomX && startRoomY == targetRoomY && 
            startTileX == targetTileX && startTileY == targetTileY) {
            return path;
        }
        
        // If same room, find path within room
        if (startRoomX == targetRoomX && startRoomY == targetRoomY) {
            return findPathWithinRoom(ship, startRoomX, startRoomY, startTileX, startTileY, targetTileX, targetTileY);
        }
        
        // Different rooms - need to move between rooms
        // Strategy: Move to exit tile of current room, then move room-to-room, then to target tile
        
        Room startRoom = ship.getRoom(startRoomX, startRoomY);
        Room targetRoom = ship.getRoom(targetRoomX, targetRoomY);
        
        if (startRoom == null || startRoom.getType() == RoomType.EMPTY ||
            targetRoom == null || targetRoom.getType() == RoomType.EMPTY) {
            return path; // Invalid rooms
        }
        
        // Find path to adjacent room first (simple: move horizontally/vertically between rooms)
        // For now, use simple pathfinding: move to room edge, then to next room
        
        // Move within start room to edge closest to target room
        int exitTileX = startTileX;
        int exitTileY = startTileY;
        
        // Determine which edge to exit from based on target room direction
        if (targetRoomX > startRoomX) {
            // Target is to the right, exit from right edge
            exitTileX = 1;
        } else if (targetRoomX < startRoomX) {
            // Target is to the left, exit from left edge
            exitTileX = 0;
        }
        
        if (targetRoomY > startRoomY) {
            // Target is below, exit from bottom edge
            exitTileY = 1;
        } else if (targetRoomY < startRoomY) {
            // Target is above, exit from top edge
            exitTileY = 0;
        }
        
        // Path within start room to exit
        if (startTileX != exitTileX || startTileY != exitTileY) {
            List<int[]> pathToExit = findPathWithinRoom(ship, startRoomX, startRoomY, startTileX, startTileY, exitTileX, exitTileY);
            path.addAll(pathToExit);
        }
        
        // Move room-to-room (horizontal/vertical only)
        int currentRoomX = startRoomX;
        int currentRoomY = startRoomY;
        int currentTileX = exitTileX;
        int currentTileY = exitTileY;
        
        // Move horizontally first
        while (currentRoomX != targetRoomX) {
            if (currentRoomX < targetRoomX) {
                currentRoomX++;
            } else {
                currentRoomX--;
            }
            
            Room nextRoom = ship.getRoom(currentRoomX, currentRoomY);
            if (nextRoom == null || nextRoom.getType() == RoomType.EMPTY) {
                return new ArrayList<>(); // Path blocked
            }
            
            // Enter next room from opposite side
            int enterTileX = (currentRoomX > startRoomX) ? 0 : 1;
            path.add(new int[]{currentRoomX, currentRoomY, enterTileX, currentTileY});
            currentTileX = enterTileX;
        }
        
        // Then move vertically
        while (currentRoomY != targetRoomY) {
            if (currentRoomY < targetRoomY) {
                currentRoomY++;
            } else {
                currentRoomY--;
            }
            
            Room nextRoom = ship.getRoom(currentRoomX, currentRoomY);
            if (nextRoom == null || nextRoom.getType() == RoomType.EMPTY) {
                return new ArrayList<>(); // Path blocked
            }
            
            // Enter next room from opposite side
            int enterTileY = (currentRoomY > startRoomY) ? 0 : 1;
            path.add(new int[]{currentRoomX, currentRoomY, currentTileX, enterTileY});
            currentTileY = enterTileY;
        }
        
        // Now move within target room to target tile
        if (currentTileX != targetTileX || currentTileY != targetTileY) {
            List<int[]> pathToTarget = findPathWithinRoom(ship, targetRoomX, targetRoomY, currentTileX, currentTileY, targetTileX, targetTileY);
            path.addAll(pathToTarget);
        }
        
        return path;
    }
    
    /**
     * Find path within a single room (2x2 grid).
     */
    private static List<int[]> findPathWithinRoom(Ship ship, int roomX, int roomY, 
            int startTileX, int startTileY, int targetTileX, int targetTileY) {
        List<int[]> path = new ArrayList<>();
        
        Room room = ship.getRoom(roomX, roomY);
        if (room == null) {
            return path;
        }
        
        int currentTileX = startTileX;
        int currentTileY = startTileY;
        
        // Move horizontally first
        while (currentTileX != targetTileX) {
            if (currentTileX < targetTileX) {
                currentTileX++;
            } else {
                currentTileX--;
            }
            
            // Check if tile is occupied
            if (!room.isTileEmpty(currentTileX, currentTileY)) {
                // Try alternative path (move vertically first)
                return findPathWithinRoomVerticalFirst(ship, roomX, roomY, startTileX, startTileY, targetTileX, targetTileY);
            }
            
            path.add(new int[]{roomX, roomY, currentTileX, currentTileY});
        }
        
        // Then move vertically
        while (currentTileY != targetTileY) {
            if (currentTileY < targetTileY) {
                currentTileY++;
            } else {
                currentTileY--;
            }
            
            // Check if tile is occupied
            if (!room.isTileEmpty(currentTileX, currentTileY)) {
                return new ArrayList<>(); // Path blocked
            }
            
            path.add(new int[]{roomX, roomY, currentTileX, currentTileY});
        }
        
        return path;
    }
    
    /**
     * Alternative pathfinding within room: move vertically first, then horizontally.
     */
    private static List<int[]> findPathWithinRoomVerticalFirst(Ship ship, int roomX, int roomY,
            int startTileX, int startTileY, int targetTileX, int targetTileY) {
        List<int[]> path = new ArrayList<>();
        
        Room room = ship.getRoom(roomX, roomY);
        if (room == null) {
            return path;
        }
        
        int currentTileX = startTileX;
        int currentTileY = startTileY;
        
        // Move vertically first
        while (currentTileY != targetTileY) {
            if (currentTileY < targetTileY) {
                currentTileY++;
            } else {
                currentTileY--;
            }
            
            // Check if tile is occupied
            if (!room.isTileEmpty(currentTileX, currentTileY)) {
                return new ArrayList<>(); // Path blocked
            }
            
            path.add(new int[]{roomX, roomY, currentTileX, currentTileY});
        }
        
        // Then move horizontally
        while (currentTileX != targetTileX) {
            if (currentTileX < targetTileX) {
                currentTileX++;
            } else {
                currentTileX--;
            }
            
            // Check if tile is occupied
            if (!room.isTileEmpty(currentTileX, currentTileY)) {
                return new ArrayList<>(); // Path blocked
            }
            
            path.add(new int[]{roomX, roomY, currentTileX, currentTileY});
        }
        
        return path;
    }
}

