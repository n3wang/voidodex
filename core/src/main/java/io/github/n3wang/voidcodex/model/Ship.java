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
    private int gridWidth;
    private int gridHeight;

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
    }

    public static Ship createStarterShip() {
        Ship ship = new Ship("Void Runner", 30, 8, 8, 4);
        
        // Create a simple starter layout
        // Row 0: Bridge, Medbay, Empty, Empty
        ship.addRoom(new Room(0, 0, RoomType.BRIDGE));
        ship.addRoom(new Room(1, 0, RoomType.MEDBAY));
        ship.addRoom(new Room(2, 0, RoomType.EMPTY));
        ship.addRoom(new Room(3, 0, RoomType.EMPTY));
        
        // Row 1: Shields, Weapons, Engines, Oxygen
        ship.addRoom(new Room(0, 1, RoomType.SHIELDS));
        ship.addRoom(new Room(1, 1, RoomType.WEAPONS));
        ship.addRoom(new Room(2, 1, RoomType.ENGINES));
        ship.addRoom(new Room(3, 1, RoomType.OXYGEN));
        
        // Row 2: Empty, Empty, Empty, Empty
        for (int x = 0; x < 4; x++) {
            ship.addRoom(new Room(x, 2, RoomType.EMPTY));
        }
        
        // Row 3: Empty, Empty, Empty, Empty
        for (int x = 0; x < 4; x++) {
            ship.addRoom(new Room(x, 3, RoomType.EMPTY));
        }
        
        // Add starter crew
        ship.addCrew(new Crew("Captain", CrewRole.CAPTAIN));
        ship.addCrew(new Crew("Engineer", CrewRole.ENGINEER));
        
        return ship;
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void addCrew(Crew crewMember) {
        crew.add(crewMember);
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
    public int getGridWidth() { return gridWidth; }
    public int getGridHeight() { return gridHeight; }
}

