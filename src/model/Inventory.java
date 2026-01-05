package model;

import java.util.*;

/**
 * Represents the player's inventory
 * Stores item names to keep referencing simple
 */
public class Inventory {
    private List<String> itemNames; // Names of items currently in the player's inventory

    /**
     * Creates an empty inventory
     */
    public Inventory() {
        this.itemNames = new ArrayList<>();
    }

    /**
     * Creates an inventory initialized with a collection of item names
     * @param items a collection of item names to add to the inventory (can be null)
     */
    public Inventory(Collection<String> items) {
        this();
        if (items != null) {
            this.itemNames.addAll(items);
        }
    }

    /**
     * Gets the list of item names in the inventory
     * @return a list of item names
     */
    public List<String> getItemNames() {
        return itemNames;
    }
    /**
     * Sets the list of item names in the inventory
     * @param itemNames a list of item names
     */
    public void setItemNames(List<String> itemNames) {
        this.itemNames = itemNames;
    }
}
