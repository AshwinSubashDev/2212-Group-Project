package model;

import java.util.*;

/**
 * Represents a non-playable character (NPC) in the game
 * Characters may have dialogue lines, a list of desired item names, and items they currently possess
 * They can also be marked as discovered once interacted with by the player.
 */
public class Character {
    private String name;                 // The character's name
    private String description;          // A description of the character
    private List<String> dialogue;       // Dialogue lines the character can say
    private List<String> itemsWanted;    // Names of items the character wants
    private List<String> itemsPossessed; // Items the character currently holds
    private boolean discovered;          // Whether the character has been discovered
    private int dialogueIndex = 0;       // Current dialogue progression index
    private String imagePath;            // Path to character image
    private Integer[] dimensions;        // Image dimensions [x, y, width, height]

    /**
     * Default constructor initializing lists and setting the discovered state to true
     */
    public Character() {
        this.dialogue = new ArrayList<>();
        this.itemsWanted = new ArrayList<>();
        this.itemsPossessed = new ArrayList<>();
        this.discovered = true;
    }

    /**
     * Creates a character with a name and description
     * @param name the character's name
     * @param description a description of the character
     */
    public Character(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    /**
     * Gets the character's name
     * @return the name of the character
     */
    public String getName() {
        return name;
    }
    /**
     * Sets the character's name
     * @param name the new name for the character
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the character
     * @return the character's description
     */
    public String getDescription() {
        return description;
    }
    /**
     * Sets the character's description
     * @param description the new description for the character
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the list of dialogue lines the character can say
     * @return a list of dialogue strings
     */
    public List<String> getDialogue() {
        return dialogue;
    }
    /**
     * Sets the list of dialogue lines for the character
     * @param dialogue a list of dialogue strings
     */
    public void setDialogue(List<String> dialogue) {
        this.dialogue = dialogue;
    }

    /**
     * Gets the list of items the character wants.
     *
     * @return a list of item names desired by the character
     */
    public List<String> getItemsWanted() {
        return itemsWanted;
    }
    /**
     * Sets the list of items the character wants
     * @param itemsWanted a list of desired item names
     */
    public void setItemsWanted(List<String> itemsWanted) {
        this.itemsWanted = itemsWanted;
    }

    /**
     * Gets the list of items the character currently possesses
     * @return a list of item names the character has
     */
    public List<String> getItemsPossessed() {
        return itemsPossessed;
    }
    /**
     * Sets the list of items the character possesses
     * @param itemsPossessed a list of item names the character holds
     */
    public void setItemsPossessed(List<String> itemsPossessed) {
        this.itemsPossessed = itemsPossessed;
    }

    /**
     * Checks whether the character has been discovered
     * @return true if the character has been discovered, false otherwise
     */
    public boolean isDiscovered() {
        return discovered;
    }
    /**
     * Sets whether the character has been discovered
     * @param discovered true if the character is discovered, false otherwise
     */
    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    /**
     * Gets the next dialogue line from the character
     * Progresses through dialogue sequentially
     * @return the next dialogue line, or a default message if all dialogue has been shown
     */
    public String nextDialogue() {
        if (dialogue == null || dialogue.isEmpty()) {
            return "...";
        }
        if (dialogueIndex >= dialogue.size()) {
            return "They have nothing more to say.";
        }
        return dialogue.get(dialogueIndex++);
    }

    /**
     * Resets the dialogue progression to the beginning
     */
    public void resetDialogue() {
        this.dialogueIndex = 0;
    }

    /**
     * Gets the current dialogue index
     * @return the current dialogue progression index
     */
    public int getDialogueIndex() {
        return dialogueIndex;
    }

    /**
     * Checks if more dialogue is available
     * @return true if there are unread dialogue lines
     */
    public boolean hasMoreDialogue() {
        if (dialogue == null || dialogue.isEmpty()) return false;
        return dialogueIndex < dialogue.size();
    }

    // Check if character wants this item
    public boolean wantsItem(String itemName) {
        if (itemsWanted == null) return false;
        return itemsWanted.contains(itemName);
    }
    
    // Add item to possessed
    public void addItem(String itemName) {
        if (itemsPossessed == null) itemsPossessed = new ArrayList<>();
        itemsPossessed.add(itemName);
    }
    
    // Remove item from possessed
    public boolean removeItem(String itemName) {
        if (itemsPossessed == null) return false;
        return itemsPossessed.remove(itemName);
    }

    // Check if character has item
    public boolean hasItem(String itemName) {
        if (itemsPossessed == null) return false;
        return itemsPossessed.contains(itemName);
    }

    /**
     * Give an item to this character and get response
     * @param itemName item being given
     * @return response text
     */
    public String giveItem(String itemName) {
        if (wantsItem(itemName)) {
            addItem(itemName);
            return name + " accepts the " + itemName + ".";
        }
        return name + " doesn't want that.";
    }

    private transient Map<String, Runnable> eventTriggers;  // Event hooks
    
    /**
     * Gets the image path for this character
     * @return the image path
     */
    public String getImagePath() {
        return imagePath;
    }
    
    /**
     * Sets the image path for this character
     * @param imagePath the image path
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    /**
     * Gets the image dimensions [x, y, width, height]
     * @return the dimensions array
     */
    public Integer[] getDimensions() {
        return dimensions;
    }
    
    /**
     * Sets the image dimensions
     * @param dimensions array of [x, y, width, height]
     */
    public void setDimensions(Integer[] dimensions) {
        this.dimensions = dimensions;
    }

    public void addTrigger(String key, Runnable action) {
        if (eventTriggers == null) eventTriggers = new HashMap<>();
        eventTriggers.put(key, action);
    }

    public boolean runTrigger(String key) {
        if (eventTriggers == null) return false;
        Runnable action = eventTriggers.get(key);
        if (action != null) {
            action.run();
            return true;
        }
        return false;
    }
}
