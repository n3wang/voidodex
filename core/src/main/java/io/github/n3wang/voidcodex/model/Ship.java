package io.github.n3wang.voidcodex.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player's ship with rooms, systems, and crew.
 */
public class Ship {
    private String name;
    private int maxHull;
    private int currentHull;
    private int maxPower;
    private int availablePower;
    private List<Room> rooms;
    private List<Crew> crew;
    private List<Weapon> weapons;
    private int gridWidth;
    private int gridHeight;
    private int shields;
    private int maxShields;
    private int scrap;
    private int fuel;

    public Ship(String name, int maxHull, int maxPower, int gridWidth, int gridHeight) {
        this.name = name;
        this.maxHull = maxHull;
        this.currentHull = maxHull;
        this.maxPower = maxPower;
        this.availablePower = maxPower;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.rooms = new ArrayList<>();
        this.crew = new ArrayList<>();
        this.weapons = new ArrayList<>();
        this.shields = 0;
        this.maxShields = 0;
        this.scrap = 0;
        this.fuel = 100;
    }

    public static Ship createStarterShip() {
        Ship ship = new Ship("Void Runner", 30, 8, 3, 3);
        
        // Create 3x3 grid layout - systems distributed around
        // Row 0: Bridge, Shields, Weapons
        ship.addRoom(new Room(0, 0, RoomType.BRIDGE));
        ship.addRoom(new Room(1, 0, RoomType.SHIELDS));
        ship.addRoom(new Room(2, 0, RoomType.WEAPONS));
        
        // Row 1: Engines, Medbay, Oxygen
        ship.addRoom(new Room(0, 1, RoomType.ENGINES));
        ship.addRoom(new Room(1, 1, RoomType.MEDBAY));
        ship.addRoom(new Room(2, 1, RoomType.OXYGEN));
        
        // Row 2: Sensors, Doors, Empty
        ship.addRoom(new Room(0, 2, RoomType.SENSORS));
        ship.addRoom(new Room(1, 2, RoomType.DOORS));
        ship.addRoom(new Room(2, 2, RoomType.EMPTY));
        
        // Add starter crew
        ship.addCrew(new Crew("Captain", CrewRole.CAPTAIN));
        ship.addCrew(new Crew("Engineer", CrewRole.ENGINEER));
        
        // Add starter weapons
        ship.addWeapon(new Weapon("Basic Laser", WeaponType.LASER, 10, 1, 1));
        ship.addWeapon(new Weapon("Missile Launcher", WeaponType.MISSILE, 20, 2, 2));
        
        // Initialize shields
        ship.maxShields = 4;
        ship.shields = 0;
        
        return ship;
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void addCrew(Crew crewMember) {
        crew.add(crewMember);
    }

    public void addWeapon(Weapon weapon) {
        weapons.add(weapon);
    }

    public Room getRoom(int x, int y) {
        return rooms.stream()
                .filter(r -> r.getX() == x && r.getY() == y)
                .findFirst()
                .orElse(null);
    }

    // Getters and setters
    public String getName() { return name; }
    public int getMaxHull() { return maxHull; }
    public int getCurrentHull() { return currentHull; }
    public void setCurrentHull(int hull) { this.currentHull = Math.max(0, Math.min(hull, maxHull)); }
    public int getMaxPower() { return maxPower; }
    public int getAvailablePower() { return availablePower; }
    public void setAvailablePower(int power) { this.availablePower = Math.max(0, Math.min(power, maxPower)); }
    
    /**
     * Try to add power to a room. Returns true if successful.
     */
    public boolean addPowerToRoom(Room room) {
        if (availablePower > 0 && room.getPowerLevel() < room.getMaxPower()) {
            room.setPowerLevel(room.getPowerLevel() + 1);
            availablePower--;
            return true;
        }
        return false;
    }
    
    /**
     * Remove power from a room. Returns true if successful.
     */
    public boolean removePowerFromRoom(Room room) {
        if (room.getPowerLevel() > 0) {
            room.setPowerLevel(room.getPowerLevel() - 1);
            availablePower++;
            return true;
        }
        return false;
    }
    
    /**
     * Get total used power across all rooms.
     */
    public int getUsedPower() {
        return rooms.stream()
                .mapToInt(Room::getPowerLevel)
                .sum();
    }
    
    public List<Room> getRooms() { return rooms; }
    public List<Crew> getCrew() { return crew; }
    public List<Weapon> getWeapons() { return weapons; }
    public int getGridWidth() { return gridWidth; }
    public int getGridHeight() { return gridHeight; }
    public int getShields() { return shields; }
    public void setShields(int shields) { this.shields = Math.max(0, Math.min(shields, maxShields)); }
    public int getMaxShields() { return maxShields; }
    public void setMaxShields(int max) { this.maxShields = max; }
    public int getScrap() { return scrap; }
    public void setScrap(int scrap) { this.scrap = scrap; }
    public int getFuel() { return fuel; }
    public void setFuel(int fuel) { this.fuel = fuel; }
}

