package io.github.n3wang.voidcodex.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a crew member with skills and stats.
 */
public class Crew {
    private String name;
    private CrewRole role;
    private int currentRoomX; // Room position on ship grid
    private int currentRoomY; // Room position on ship grid
    private int currentTileX; // Tile position within room (0-1)
    private int currentTileY; // Tile position within room (0-1)
    private int health;
    private int maxHealth;
    private Map<Skill, Integer> skills; // Skill -> XP
    private boolean isMoving;
    private int targetRoomX; // Target room position
    private int targetRoomY; // Target room position
    private int targetTileX; // Target tile position within room (0-1)
    private int targetTileY; // Target tile position within room (0-1)
    private float movementSpeed; // Tiles per second
    private float movementProgress; // 0.0 to 1.0, progress to next tile
    private int nextRoomX; // Next room in path
    private int nextRoomY; // Next room in path
    private int nextTileX; // Next tile in path (0-1)
    private int nextTileY; // Next tile in path (0-1)

    public Crew(String name, CrewRole role) {
        this.name = name;
        this.role = role;
        this.health = 100;
        this.maxHealth = 100;
        this.skills = new HashMap<>();
        this.currentRoomX = 0;
        this.currentRoomY = 0;
        this.currentTileX = 0;
        this.currentTileY = 0;
        this.isMoving = false;
        this.movementSpeed = 2.0f; // 2 tiles per second = 0.5 seconds per tile
        this.movementProgress = 0.0f;
        
        // Initialize skills with base XP
        for (Skill skill : Skill.values()) {
            skills.put(skill, 0);
        }
        
        // Give role-based starting XP
        for (Skill skill : role.getPrimarySkills()) {
            skills.put(skill, 10);
        }
    }

    public void gainXP(Skill skill, int amount) {
        int currentXP = skills.getOrDefault(skill, 0);
        skills.put(skill, currentXP + amount);
    }

    public int getSkillLevel(Skill skill) {
        int xp = skills.getOrDefault(skill, 0);
        // Level = XP / 100 (simple progression)
        return xp / 100;
    }

    public int getSkillXP(Skill skill) {
        return skills.getOrDefault(skill, 0);
    }

    // Getters and setters
    public String getName() { return name; }
    public CrewRole getRole() { return role; }
    public int getCurrentRoomX() { return currentRoomX; }
    public void setCurrentRoomX(int x) { this.currentRoomX = x; }
    public int getCurrentRoomY() { return currentRoomY; }
    public void setCurrentRoomY(int y) { this.currentRoomY = y; }
    public int getCurrentTileX() { return currentTileX; }
    public void setCurrentTileX(int x) { this.currentTileX = Math.max(0, Math.min(1, x)); }
    public int getCurrentTileY() { return currentTileY; }
    public void setCurrentTileY(int y) { this.currentTileY = Math.max(0, Math.min(1, y)); }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = Math.max(0, Math.min(health, maxHealth)); }
    public int getMaxHealth() { return maxHealth; }
    public boolean isMoving() { return isMoving; }
    public void setMoving(boolean moving) { this.isMoving = moving; }
    public int getTargetRoomX() { return targetRoomX; }
    public void setTargetRoomX(int x) { this.targetRoomX = x; }
    public int getTargetRoomY() { return targetRoomY; }
    public void setTargetRoomY(int y) { this.targetRoomY = y; }
    public int getTargetTileX() { return targetTileX; }
    public void setTargetTileX(int x) { this.targetTileX = Math.max(0, Math.min(1, x)); }
    public int getTargetTileY() { return targetTileY; }
    public void setTargetTileY(int y) { this.targetTileY = Math.max(0, Math.min(1, y)); }
    @Deprecated
    public int getQuarterPosition() { return 0; } // Deprecated - use tile positions instead
    @Deprecated
    public void setQuarterPosition(int quarter) { } // Deprecated - use tile positions instead
    public float getMovementSpeed() { return movementSpeed; }
    public void setMovementSpeed(float speed) { this.movementSpeed = Math.max(0.1f, speed); }
    public float getMovementProgress() { return movementProgress; }
    public void setMovementProgress(float progress) { this.movementProgress = Math.max(0f, Math.min(1f, progress)); }
    public int getNextRoomX() { return nextRoomX; }
    public void setNextRoomX(int x) { this.nextRoomX = x; }
    public int getNextRoomY() { return nextRoomY; }
    public void setNextRoomY(int y) { this.nextRoomY = y; }
    public int getNextTileX() { return nextTileX; }
    public void setNextTileX(int x) { this.nextTileX = Math.max(0, Math.min(1, x)); }
    public int getNextTileY() { return nextTileY; }
    public void setNextTileY(int y) { this.nextTileY = Math.max(0, Math.min(1, y)); }
    public Map<Skill, Integer> getSkills() { return skills; }
    
    /**
     * Update movement progress. Returns true if crew reached destination.
     */
    public boolean updateMovement(float deltaTime) {
        if (!isMoving) return false;
        
        movementProgress += movementSpeed * deltaTime;
        if (movementProgress >= 1.0f) {
            // Reached next room
            currentRoomX = nextRoomX;
            currentRoomY = nextRoomY;
            movementProgress = 0.0f;
            
            // Check if reached final destination
            if (currentRoomX == targetRoomX && currentRoomY == targetRoomY) {
                isMoving = false;
                return true; // Reached destination
            }
        }
        return false; // Still moving
    }
}

