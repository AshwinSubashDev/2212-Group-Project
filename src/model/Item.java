package model;

import java.util.*;

/**
 * Represents an item (object) in the game world
 */
public class Item {
    private String name;                        // The item's name
    private String description;                 // Description shown to the player
    private boolean isPickable;                 // Whether the item can be picked up
    private List<String> attributes;            // Attribute descriptors (e.g., "sharp", "heavy")
    private List<String> containedItems;        // Names of items hidden inside this item
    private boolean examined;                   // Whether the item has been examined
    private boolean reusable;                   // Whether using the item consumes it
    private String imagePath;                   // Optional image filename/path
    private Map<String,String> combinableWith;  // Contains names of all items this item can be combined with and their resulting item
    private boolean hidden;                     // Item is concealed until revealed
    private boolean isContainer;                // This item can hold other items
    private boolean locked;                     // Container is locked
    private String keyItem;                     // Item name that unlocks this container
    private Integer[] dimensions;               // Contains the dimensions of the image. Includes x-coordinate, y-coordinate, width and height


    /**
     * Creates an item with default values
     * Attributes list and contained item list are initialized empty
     * Item is pickable and not reusable by default
     */
    public Item() {
        this.attributes = new ArrayList<>();
        this.containedItems = new ArrayList<>();
        this.examined = false;
        this.isPickable = true;
        this.reusable = false;
        this.hidden = false;
        this.isContainer = false;
        this.locked = false;
    }

    /**
     * Creates an item with a name, description, and pickable state
     * @param name        the item's name
     * @param description the item's description
     * @param pickable    whether the item can be picked up
     */
    public Item(String name, String description, boolean pickable) {
        this();
        this.name = name;
        this.description = description;
        this.isPickable = pickable;
    }

    /**
     * Gets the item's name
     * @return the name of the item
     */
    public String getName() {
        return name;
    }
    /**
     * Sets the item's name
     * @param name the new item name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the item's description
     * @return the item description
     */
    public String getDescription() {
        return description;
    }
    /**
     * Sets the item's description
     * @param description the new item description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Checks whether the item can be picked up
     * @return true if pickable, false otherwise
     */
    public boolean isPickable() {
        return isPickable;
    }
    /**
     * Sets whether the item can be picked up
     * @param pickable true if pickable, false otherwise
     */
    public void setPickable(boolean pickable) {
        this.isPickable = pickable;
    }

    /**
     * Gets the attribute descriptors for the item
     * @return a list of attribute strings
     */
    public List<String> getAttributes() {
        return attributes;
    }
    /**
     * Sets the attribute descriptors for the item
     * @param attributes a list of attribute strings (null creates a new empty list)
     */
    public void setAttributes(List<String> attributes) {
        if (attributes != null) {
            this.attributes = attributes;
        } else {
            this.attributes = new ArrayList<>();
        }
    }

    /**
     * Gets the names of items contained within this item
     * @return a list of contained item names
     */
    public List<String> getContainedItems() {
        return containedItems;
    }
    /**
     * Sets the list of items contained within this item
     * @param containedItems a list of contained item names
     */
    public void setContainedItems(List<String> containedItems) {
        this.containedItems = containedItems;
    }

    /**
     * Checks whether the item has been examined
     * @return true if examined, false otherwise
     */
    public boolean isExamined() {
        return examined;
    }
    /**
     * Sets whether the item has been examined
     * @param examined true if examined, false otherwise
     */
    public void setExamined(boolean examined) {
        this.examined = examined;
    }

    /**
     * Checks whether the item is reusable
     * @return true if reusable, false if consumed on use
     */
    public boolean isReusable() {
        return reusable;
    }
    /**
     * Sets whether the item is reusable
     * @param reusable true if reusable, false otherwise
     */
    public void setReusable(boolean reusable) {
        this.reusable = reusable;
    }

    /**
     * Gets the path to the item's image
     * @return the image path or filename
     */
    public String getImagePath() {
        return imagePath;
    }
    /**
     * Sets the path to the item's image
     * @param imagePath the image path or filename
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Sets the dimensions of the image for the item
     * @param dimensions integer array that contains the x-coordinate, y-coordinate, width and height of the item image
     */
    public void setDimensions(Integer[] dimensions){
        this.dimensions = dimensions;
    }

    /**
     * Gets the image dimensions for the item
     * @return integer array containing the placement and size of the item image
     */
    public Integer[] getDimensions(){
        return dimensions;
    }

    /**
     * Gets all the combinable items for this item
     * Each entry maps a target item name (key) to its resulting item name (value)
     * @return a map of the name of all combinable items and their result 
     */
    public Map<String,String> combinableWith() {
        return combinableWith;
    }

    /**
     * Sets the combinable items map
     * @param combinableWith map of combinable items
     */
    public void setCombinableWith(Map<String,String> combinableWith) {
        this.combinableWith = combinableWith;
    }
    
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    
    public boolean isContainer() { return isContainer; }
    public void setContainer(boolean container) { this.isContainer = container; }
    
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    
    public String getKeyItem() { return keyItem; }
    public void setKeyItem(String keyItem) { this.keyItem = keyItem; }
    
    // Check if item has specific attribute
    public boolean hasAttribute(String attr) {
        if (attributes == null || attr == null) return false;
        for (String a : attributes) {
            if (a.equalsIgnoreCase(attr)) return true;
        }
        return false;
    }
    
    // Add attribute
    public void addAttribute(String attr) {
        if (attributes == null) attributes = new ArrayList<>();
        if (!hasAttribute(attr)) attributes.add(attr);
    }
    
    // Add item to container
    public void addContainedItem(String itemName) {
        if (containedItems == null) containedItems = new ArrayList<>();
        containedItems.add(itemName);
    }
    
    // Remove item from container
    public boolean removeContainedItem(String itemName) {
        if (containedItems == null) return false;
        return containedItems.remove(itemName);
    }
    
    // Check if can combine with target
    public boolean canCombineWith(String targetName) {
        if (combinableWith == null) return false;
        return combinableWith.containsKey(targetName);
    }
    
    // Get combination result
    public String getCombinationResult(String targetName) {
        if (combinableWith == null) return null;
        return combinableWith.get(targetName);
    }
}
