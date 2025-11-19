package io.github.n3wang.voidcodex.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a crew member with skills and stats.
 */
public class Crew {
    private String name;
    private CrewRole role;
    private int currentRoomX;
    private int currentRoomY;
    private int health;
    private int maxHealth;
    private Map<Skill, Integer> skills; // Skill -> XP
    private boolean isMoving;
    private int targetRoomX;
    private int targetRoomY;

    public Crew(String name, CrewRole role) {
        this.name = name;
        this.role = role;
        this.health = 100;
        this.maxHealth = 100;
        this.skills = new HashMap<>();
        this.currentRoomX = 0;
        this.currentRoomY = 0;
        this.isMoving = false;
        
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
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = Math.max(0, Math.min(health, maxHealth)); }
    public int getMaxHealth() { return maxHealth; }
    public boolean isMoving() { return isMoving; }
    public void setMoving(boolean moving) { this.isMoving = moving; }
    public int getTargetRoomX() { return targetRoomX; }
    public void setTargetRoomX(int x) { this.targetRoomX = x; }
    public int getTargetRoomY() { return targetRoomY; }
    public void setTargetRoomY(int y) { this.targetRoomY = y; }
    public Map<Skill, Integer> getSkills() { return skills; }
}

