package model;

import java.util.*;

/**
 * A serializable snapshot of the entire game state used for Save/Load
 */
public class GameState {
    private Map<String, Location> locations;     // All locations in the game indexed by name
    private Map<String, Item> items;             // All items in the game indexed by name
    private Map<String, Character> characters;   // All characters in the game indexed by name
    private Inventory inventory;                 // Player's inventory state
    private String currentLocationName;          // Name of the player's current location
    private int turnsTaken;                      // Number of turns taken so far
    private Integer turnLimit;                   // Maximum number of turns allowed (nullable)
    private List<UseRule> useRules;              // Rules defining item interactions
    private List<GiveRule> giveRules;            // Rules defining character gift interactions
    private String gameMessage;                  // Current game message to display
    private boolean gameOver;                    // Whether the game has ended

    /**
     * Creates an empty game state with initialized collections
     */
    public GameState() {
        this.locations = new LinkedHashMap<>();
        this.items = new LinkedHashMap<>();
        this.characters = new LinkedHashMap<>();
        this.inventory = new Inventory();
        this.turnsTaken = 0;
        this.useRules = new ArrayList<>();
        this.giveRules = new ArrayList<>();
        this.gameMessage = "";
        this.gameOver = false;
    }

    /**
     * Gets all locations in the game
     * @return a map of location names to Location objects
     */
    public Map<String, Location> getLocations() {
        return locations;
    }
    /**
     * Sets all locations in the game
     * @param locations a map of location names to Location objects
     */
    public void setLocations(Map<String, Location> locations) {
        this.locations = locations;
    }

    /**
     * Gets all items in the game
     * @return a map of item names to Item objects
     */
    public Map<String, Item> getItems() {
        return items;
    }
    /**
     * Sets all items in the game
     * @param items a map of item names to Item objects
     */
    public void setItems(Map<String, Item> items) {
        this.items = items;
    }

    /**
     * Gets all characters in the game
     * @return a map of character names to Character objects
     */
    public Map<String, Character> getCharacters() {
        return characters;
    }
    /**
     * Sets all characters in the game
     * @param characters a map of character names to Character objects
     */
    public void setCharacters(Map<String, Character> characters) {
        this.characters = characters;
    }

    /**
     * Gets the player's inventory
     * @return the inventory object
     */
    public Inventory getInventory() {
        return inventory;
    }
    /**
     * Sets the player's inventory
     * @param inventory the inventory object
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Gets the name of the player's current location
     * @return the name of the current location
     */
    public String getCurrentLocationName() {
        return currentLocationName;
    }
    /**
     * Sets the player's current location name
     * @param currentLocationName the name of the location
     */
    public void setCurrentLocationName(String currentLocationName) {
        this.currentLocationName = currentLocationName;
    }

    /**
     * Gets the number of turns taken so far
     * @return the number of turns used
     */
    public int getTurnsTaken() {
        return turnsTaken;
    }
    /**
     * Sets the number of turns taken so far
     * @param turnsTaken the number of turns used
     */
    public void setTurnsTaken(int turnsTaken) {
        this.turnsTaken = turnsTaken;
    }

    /**
     * Gets the turn limit for the game
     * @return the turn limit, or null if unlimited
     */
    public Integer getTurnLimit() {
        return turnLimit;
    }
    /**
     * Sets the turn limit for the game
     * @param turnLimit the maximum number of turns allowed, or null for unlimited
     */
    public void setTurnLimit(Integer turnLimit) {
        this.turnLimit = turnLimit;
    }

    /**
     * Gets the list of use rules for this game
     * @return a list of UseRule objects
     */
    public List<UseRule> getUseRules() {
        return useRules;
    }
    /**
     * Sets the list of use rules for this game
     * @param useRules a list of UseRule objects (null creates an empty list)
     */
    public void setUseRules(List<UseRule> useRules) {
        if (useRules != null) {
            this.useRules = useRules;
        } else {
            this.useRules = new ArrayList<>();
        }
    }

    /**
     * Gets the list of give rules for this game
     * @return a list of GiveRule objects
     */
    public List<GiveRule> getGiveRules() {
        return giveRules;
    }

    /**
     * Sets the list of give rules for this game
     * @param giveRules a list of GiveRule objects (null creates an empty list)
     */
    public void setGiveRules(List<GiveRule> giveRules) {
        if (giveRules != null) {
            this.giveRules = giveRules;
        } else {
            this.giveRules = new ArrayList<>();
        }
    }

    /**
     * Gets the current game message
     * @return the game message
     */
    public String getGameMessage() {
        return gameMessage;
    }

    /**
     * Sets the current game message
     * @param gameMessage the message to display
     */
    public void setGameMessage(String gameMessage) {
        this.gameMessage = gameMessage;
    }

    /**
     * Checks if the game is over
     * @return true if game over, false otherwise
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Sets the game over state
     * @param gameOver true if game should end, false otherwise
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * Increments the turn counter by 1
     */
    public void incMoves() {
        this.turnsTaken++;
    }
    
    // Helper Methods
    
    // Get current location object (null-safe)
    public Location getCurrentLocation() {
        if (currentLocationName == null || locations == null) return null;
        return locations.get(currentLocationName);
    }
    
    // Get location by name (null-safe)
    public Location getLocation(String name) {
        if (name == null || locations == null) return null;
        return locations.get(name);
    }
    
    // Get item by name (null-safe)
    public Item getItem(String name) {
        if (name == null || items == null) return null;
        return items.get(name);
    }
    
    // Get character by name (null-safe)
    public Character getCharacter(String name) {
        if (name == null || characters == null) return null;
        return characters.get(name);
    }
    
    // Check if turn limit reached
    public boolean isTurnLimitReached() {
        if (turnLimit == null) return false;
        return turnsTaken >= turnLimit;
    }
    
    // Find starting location
    public Location findStartingLocation() {
        if (locations == null || locations.isEmpty()) return null;
        for (Location loc : locations.values()) {
            if (loc.isStartingLocation()) return loc;
        }
        return locations.values().iterator().next();
    }
}
