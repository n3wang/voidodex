package io.github.n3wang.voidcodex.model;

/**
 * Represents a ship weapon.
 */
public class Weapon {
    private String name;
    private int maxCharge;
    private int currentCharge;
    private int powerRequired;
    private int damage;
    private boolean autoFire;
    private boolean selected;
    private WeaponType type;

    public Weapon(String name, WeaponType type, int maxCharge, int powerRequired, int damage) {
        this.name = name;
        this.type = type;
        this.maxCharge = maxCharge;
        this.currentCharge = 0;
        this.powerRequired = powerRequired;
        this.damage = damage;
        this.autoFire = false;
        this.selected = false;
    }

    public boolean isCharged() {
        return currentCharge >= maxCharge;
    }

    public void charge(int amount) {
        currentCharge = Math.min(currentCharge + amount, maxCharge);
    }

    public void fire() {
        if (isCharged()) {
            currentCharge = 0;
        }
    }

    // Getters and setters
    public String getName() { return name; }
    public int getMaxCharge() { return maxCharge; }
    public int getCurrentCharge() { return currentCharge; }
    public int getPowerRequired() { return powerRequired; }
    public int getDamage() { return damage; }
    public boolean isAutoFire() { return autoFire; }
    public void setAutoFire(boolean autoFire) { this.autoFire = autoFire; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
    public WeaponType getType() { return type; }
}

