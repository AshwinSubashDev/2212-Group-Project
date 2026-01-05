package model;

import java.util.*;

/**
 * Represents a location in the game world
 * Contains references to items and characters by name to keep serialization simple
 */
public class Location {
    private String name;                       // Name of the location
    private String description;                // Description shown to the player
    private List<String> items;                // Item names present at the location
    private List<String> characters;           // Character names present at the location
    private Map<String, String> connections;   // Mapped sub-locations (e.g., "north" -> "kitchen")
    private String imagePath;                  // Optional image file for the location
    private boolean isStartingLocation;        // Whether this is the starting point
    private boolean isEndingLocation;          // Whether this is the ending point
    private Map<String, String> lockedExits;   // direction -> required item/key
    private boolean visited;                   // Player has been here before
    private boolean isDark;                    // Requires light source
    private String revisitDescription;         // Alternative description after first visit
    private String imageCaption;               // Caption/tooltip for the location image

    /**
     * Creates an empty location with default lists and flags
     */
    public Location() {
        this.items = new ArrayList<>();
        this.characters = new ArrayList<>();
        this.connections = new LinkedHashMap<>();
        this.isStartingLocation = false;
        this.isEndingLocation = false;
        this.lockedExits = new HashMap<>();
        this.visited = false;
        this.isDark = false;
    }

    /**
     * Creates a location with a name and description
     * @param name        the location's name
     * @param description the location's description
     */
    public Location(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    /**
     * Gets the name of the location
     * @return the location name
     */
    public String getName() {
        return name;
    }
    /**
     * Sets the name of the location
     * @param name the new location name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the location
     * @return the location description
     */
    public String getDescription() {
        return description;
    }
    /**
     * Sets the description of the location
     * @param description the new location description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the list of item names present at this location
     * @return a list of item names
     */
    public List<String> getItems() {
        return items;
    }
    /**
     * Sets the list of item names present at this location
     * @param items a list of item names
     */
    public void setItems(List<String> items) {
        this.items = items;
    }

    /**
     * Gets the list of character names present at this location
     * @return a list of character names
     */
    public List<String> getCharacters() {
        return characters;
    }
    /**
     * Sets the list of character names present at this location
     * @param characters a list of character names
     */
    public void setCharacters(List<String> characters) {
        this.characters = characters;
    }

    /**
     * Gets the location connection map
     * Keys represent directions or labels, and values represent destination location names
     * @return a map of connections
     */
    public Map<String, String> getConnections() {
        return connections;
    }
    /**
     * Sets the connection map for this location
     * @param connections a map where keys are identifiers and values are linked location names
     */
    public void setConnections(Map<String, String> connections) {
        this.connections = connections;
    }

    /**
     * Gets the image path for the location
     * @return the image path or filename
     */
    public String getImagePath() {
        return imagePath;
    }
    /**
     * Sets the image path for the location
     * @param imagePath a path or filename to the location's image
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Checks whether this is the starting location
     * @return true if starting location, false otherwise
     */
    public boolean isStartingLocation() {
        return isStartingLocation;
    }
    /**
     * Sets whether this is the starting location
     * @param startingLocation true if starting location, false otherwise
     */
    public void setStartingLocation(boolean startingLocation) {
        isStartingLocation = startingLocation;
    }

    /**
     * Checks whether this is the ending location
     * @return true if ending location, false otherwise
     */
    public boolean isEndingLocation() {
        return isEndingLocation;
    }
    /**
     * Sets whether this is the ending location
     * @param endingLocation true if ending location, false otherwise
     */
    public void setEndingLocation(boolean endingLocation) {
        isEndingLocation = endingLocation;
    }
    
    public Map<String, String> getLockedExits() { return lockedExits; }
    public void setLockedExits(Map<String, String> lockedExits) {
        this.lockedExits = lockedExits != null ? lockedExits : new HashMap<>();
    }
    
    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }
    
    public boolean isDark() { return isDark; }
    public void setDark(boolean dark) { this.isDark = dark; }
    
    // Check if exit is locked
    public boolean isExitLocked(String direction) {
        if (lockedExits == null) return false;
        return lockedExits.containsKey(direction);
    }
    
    // Unlock an exit
    public void unlockExit(String direction) {
        if (lockedExits != null) lockedExits.remove(direction);
    }
    
    // Add connection
    public void addConnection(String direction, String destination) {
        if (connections == null) connections = new LinkedHashMap<>();
        connections.put(direction, destination);
    }
    
    // Remove connection
    public void removeConnection(String direction) {
        if (connections != null) connections.remove(direction);
    }
    
    // Check if has exit
    public boolean hasExit(String direction) {
        if (connections == null) return false;
        return connections.containsKey(direction);
    }
    
    // Get destination
    public String getDestination(String direction) {
        if (connections == null) return null;
        return connections.get(direction);
    }
    
    // Check if item at location
    public boolean hasItem(String itemName) {
        if (items == null) return false;
        return items.contains(itemName);
    }
    
    // Add item
    public void addItem(String itemName) {
        if (items == null) items = new ArrayList<>();
        if (!items.contains(itemName)) items.add(itemName);
    }
    
    // Remove item
    public boolean removeItem(String itemName) {
        if (items == null) return false;
        return items.remove(itemName);
    }
    
    /**
     * Checks if a character with the given name is present
     * @param charName name of the character
     * @return true if present, false otherwise
     */
    public boolean hasCharacter(String charName) {
        if (characters == null) return false;
        return characters.contains(charName);
    }
    
    // Connection Validity
    
    /**
     * Validates that a connection label points to a non-null, non-empty destination
     * Useful when loading from JSON to ensure data integrity
     * @param direction the direction/label to check
     * @return true if connection exists and has valid destination
     */
    public boolean isConnectionValid(String direction) {
        return connections != null && 
               connections.containsKey(direction) && 
               connections.get(direction) != null &&
               !connections.get(direction).isEmpty();
    }
    
    // Revisit Description
    
    /**
     * Gets the revisit description (shown after first visit)
     * Falls back to main description if not set
     * @return revisit description or main description
     */
    public String getRevisitDescription() {
        return revisitDescription != null ? revisitDescription : description;
    }
    
    public void setRevisitDescription(String revisitDescription) {
        this.revisitDescription = revisitDescription;
    }
    
    /**
     * Gets the appropriate description based on visited status
     * @return revisit description if visited, main description otherwise
     */
    public String getDisplayDescription() {
        return visited ? getRevisitDescription() : description;
    }
    
    // Check If All Exits Locked
    
    /**
     * Checks if ALL exits from this location are locked
     * Useful for triggering puzzles (eg: sealed chamber)
     * @return true if every exit is locked
     */
    public boolean isAllExitsLocked() {
        if (connections == null || connections.isEmpty()) return false;
        if (lockedExits == null || lockedExits.isEmpty()) return false;
        return connections.keySet().equals(lockedExits.keySet());
    }
    
    // Get All Exit Labels
    
    /**
     * Gets all exit labels (directions) from this location
     * Includes both locked and unlocked exits
     * @return set of all exit direction labels
     */
    public Set<String> getAllExitLabels() {
        return connections != null ? connections.keySet() : Collections.emptySet();
    }
    
    /**
     * Gets only unlocked exit labels
     * @return set of unlocked exit directions
     */
    public Set<String> getUnlockedExitLabels() {
        if (connections == null) return Collections.emptySet();
        Set<String> unlocked = new HashSet<>(connections.keySet());
        if (lockedExits != null) unlocked.removeAll(lockedExits.keySet());
        return unlocked;
    }
    
    // Image Caption
    
    /**
     * Gets the image caption 
     * @return the image caption
     */
    public String getImageCaption() { return imageCaption; }
    
    public void setImageCaption(String imageCaption) { 
        this.imageCaption = imageCaption; 
    }
}
