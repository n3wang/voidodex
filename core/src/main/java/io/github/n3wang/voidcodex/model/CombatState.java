package io.github.n3wang.voidcodex.model;

/**
 * Represents the current combat state.
 */
public class CombatState {
    private Ship enemyShip;
    private boolean inCombat;
    private CombatMode mode; // BOARDING or WEAPONS
    private int playerShields;
    private int maxPlayerShields;
    private int enemyShields;
    private int maxEnemyShields;
    private boolean playerTurn;
    
    public CombatState() {
        this.inCombat = false;
        this.mode = CombatMode.WEAPONS;
        this.playerShields = 0;
        this.maxPlayerShields = 0;
        this.enemyShields = 0;
        this.maxEnemyShields = 0;
        this.playerTurn = true;
    }
    
    public void startCombat(Ship enemyShip) {
        this.enemyShip = enemyShip;
        this.inCombat = true;
        this.mode = CombatMode.WEAPONS;
        this.playerTurn = true;
        // Initialize shields based on shield room power
        // This will be set by the combat screen
    }
    
    public void endCombat() {
        this.inCombat = false;
        this.enemyShip = null;
    }
    
    // Getters and setters
    public Ship getEnemyShip() { return enemyShip; }
    public boolean isInCombat() { return inCombat; }
    public CombatMode getMode() { return mode; }
    public void setMode(CombatMode mode) { this.mode = mode; }
    public int getPlayerShields() { return playerShields; }
    public void setPlayerShields(int shields) { this.playerShields = Math.max(0, Math.min(shields, maxPlayerShields)); }
    public int getMaxPlayerShields() { return maxPlayerShields; }
    public void setMaxPlayerShields(int max) { this.maxPlayerShields = max; }
    public int getEnemyShields() { return enemyShields; }
    public void setEnemyShields(int shields) { this.enemyShields = Math.max(0, Math.min(shields, maxEnemyShields)); }
    public int getMaxEnemyShields() { return maxEnemyShields; }
    public void setMaxEnemyShields(int max) { this.maxEnemyShields = max; }
    public boolean isPlayerTurn() { return playerTurn; }
    public void setPlayerTurn(boolean turn) { this.playerTurn = turn; }
    public void nextTurn() { this.playerTurn = !this.playerTurn; }
}

