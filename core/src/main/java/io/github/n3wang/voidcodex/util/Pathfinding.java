package io.github.n3wang.voidcodex.util;

import io.github.n3wang.voidcodex.model.Room;
import io.github.n3wang.voidcodex.model.RoomType;
import io.github.n3wang.voidcodex.model.Ship;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple pathfinding for crew movement (horizontal/vertical only).
 */
public class Pathfinding {
    
    /**
     * Find path from start to target using horizontal/vertical movement only (no diagonal).
     * Returns list of room coordinates (x, y) in order.
     */
    public static List<int[]> findPath(Ship ship, int startX, int startY, int targetX, int targetY) {
        // If already at destination, return empty path
        if (startX == targetX && startY == targetY) {
            return new ArrayList<>();
        }
        
        List<int[]> path = new ArrayList<>();
        
        // Simple pathfinding: move horizontally first, then vertically (no diagonal)
        int currentX = startX;
        int currentY = startY;
        
        // Move horizontally first
        while (currentX != targetX) {
            if (currentX < targetX) {
                currentX++;
            } else {
                currentX--;
            }
            
            // Check if room exists and is not empty
            Room room = ship.getRoom(currentX, currentY);
            if (room != null && room.getType() != RoomType.EMPTY) {
                path.add(new int[]{currentX, currentY});
            } else {
                // Can't move through empty rooms, try vertical first
                return findPathVerticalFirst(ship, startX, startY, targetX, targetY);
            }
        }
        
        // Then move vertically (no diagonal movement)
        while (currentY != targetY) {
            if (currentY < targetY) {
                currentY++;
            } else {
                currentY--;
            }
            
            // Check if room exists and is not empty
            Room room = ship.getRoom(currentX, currentY);
            if (room != null && room.getType() != RoomType.EMPTY) {
                path.add(new int[]{currentX, currentY});
            } else {
                // Path blocked, return empty path
                return new ArrayList<>();
            }
        }
        
        return path;
    }
    
    /**
     * Alternative pathfinding: move vertically first, then horizontally.
     */
    private static List<int[]> findPathVerticalFirst(Ship ship, int startX, int startY, int targetX, int targetY) {
        List<int[]> path = new ArrayList<>();
        
        int currentX = startX;
        int currentY = startY;
        
        // Move vertically first
        while (currentY != targetY) {
            if (currentY < targetY) {
                currentY++;
            } else {
                currentY--;
            }
            
            Room room = ship.getRoom(currentX, currentY);
            if (room != null && room.getType() != RoomType.EMPTY) {
                path.add(new int[]{currentX, currentY});
            } else {
                return new ArrayList<>(); // Path blocked
            }
        }
        
        // Then move horizontally
        while (currentX != targetX) {
            if (currentX < targetX) {
                currentX++;
            } else {
                currentX--;
            }
            
            Room room = ship.getRoom(currentX, currentY);
            if (room != null && room.getType() != RoomType.EMPTY) {
                path.add(new int[]{currentX, currentY});
            } else {
                return new ArrayList<>(); // Path blocked
            }
        }
        
        return path;
    }
}

