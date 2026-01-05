package model;

import java.util.*;

/**
 * Represents a rule defining what happens when item(s) are used together
 * Supports single-item use as well as two-item interactions with attribute matching
 */
public class UseRule {

    private String itemUsed;              // Name or attribute of the primary item being used
    private String targetItem;            // Name or attribute of the secondary item (null for single-item rules)
    private String resultDescription;     // Message displayed when the rule is successfully triggered
    private List<String> resultingItems;  // Names of any items produced as a result of the interaction
    private Map<String, String> newConnections; // New location connections unlocked (label → destination)
    private String ruleId;                // Unique rule identifier
    private String requiredLocation;      // Must be at this location to use
    private String requiredFlag;          // Game flag that must be set
    private String setFlag;               // Flag to set after use
    private List<String> revealItems;     // Items to unhide after use
    private int scoreChange;              // Score points gained
    private boolean endsGame;             // Using ends the game
    private boolean winsGame;             // Using wins the game

    /**
     * Creates an empty UseRule with no predefined data
     * Initializes an empty resultingItems list
     */
    public UseRule() {
        this.resultingItems = new ArrayList<>();
        this.revealItems = new ArrayList<>();
        this.scoreChange = 0;
        this.endsGame = false;
        this.winsGame = false;
    }

    /**
     * Constructor for a single-item use rule
     * @param itemUsed          name or attribute of the item being used
     * @param resultDescription description of the result when the rule triggers
     * @param resultingItems    list of items created by this action (may be null)
     */
    public UseRule(String itemUsed, String resultDescription, List<String> resultingItems) {
        this();
        this.itemUsed = itemUsed;
        this.targetItem = null;
        this.resultDescription = resultDescription;

        if (resultingItems != null) {
            this.resultingItems = resultingItems;
        }
    }

    /**
     * Constructor for a two-item use rule
     * @param itemUsed          name or attribute of the first item
     * @param targetItem        name or attribute of the second item
     * @param resultDescription description of the result when the rule triggers
     * @param resultingItems    list of items created by this interaction (may be null)
     */
    public UseRule(String itemUsed, String targetItem, String resultDescription, List<String> resultingItems) {
        this();
        this.itemUsed = itemUsed;
        this.targetItem = targetItem;
        this.resultDescription = resultDescription;

        if (resultingItems != null) {
            this.resultingItems = resultingItems;
        }
    }

    /**
     * Gets the name or attribute of the primary item for this rule
     * @return item name or attribute
     */
    public String getItemUsed() {
        return itemUsed;
    }
    /**
     * Sets the name or attribute of the primary item for this rule
     * @param itemUsed item name or attribute
     */
    public void setItemUsed(String itemUsed) {
        this.itemUsed = itemUsed;
    }

    /**
     * Gets the name or attribute of the target item
     * This may be null for single-item rules
     * @return target item name or attribute, or null
     */
    public String getTargetItem() {
        return targetItem;
    }
    /**
     * Sets the name or attribute of the target item
     * @param targetItem target item name or attribute
     */
    public void setTargetItem(String targetItem) {
        this.targetItem = targetItem;
    }

    /**
     * Gets the message shown when this rule is triggered
     * @return result description text
     */
    public String getResultDescription() {
        return resultDescription;
    }
    /**
     * Sets the message shown when this rule is triggered
     * @param resultDescription result description text
     */
    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }

    /**
     * Gets the list of item names produced by this rule
     * @return list of resulting item names
     */
    public List<String> getResultingItems() {
        return resultingItems;
    }
    /**
     * Sets the list of items that are produced when this rule triggers
     * @param resultingItems list of resulting item names (null creates an empty list)
     */
    public void setResultingItems(List<String> resultingItems) {
        if (resultingItems != null) {
            this.resultingItems = resultingItems;
        } else {
            this.resultingItems = new ArrayList<>();
        }
    }

    /**
     * Gets the map of new connections unlocked by this rule
     * @return map of connection labels to destination location names
     */
    public Map<String, String> getNewConnections() {
        return newConnections;
    }

    /**
     * Sets the map of new connections to unlock when this rule triggers
     * @param newConnections map of connection labels to destination location names
     */
    public void setNewConnections(Map<String, String> newConnections) {
        this.newConnections = newConnections;
    }
    
    public String getRuleId() { return ruleId; }
    public void setRuleId(String id) { this.ruleId = id; }
    
    public String getRequiredLocation() { return requiredLocation; }
    public void setRequiredLocation(String location) { this.requiredLocation = location; }
    
    public String getRequiredFlag() { return requiredFlag; }
    public void setRequiredFlag(String flag) { this.requiredFlag = flag; }
    
    public String getSetFlag() { return setFlag; }
    public void setSetFlag(String flag) { this.setFlag = flag; }
    
    public List<String> getRevealItems() { return revealItems; }
    public void setRevealItems(List<String> items) {
        this.revealItems = items != null ? items : new ArrayList<>();
    }
    
    public int getScoreChange() { return scoreChange; }
    public void setScoreChange(int score) { this.scoreChange = score; }
    
    public boolean isEndsGame() { return endsGame; }
    public void setEndsGame(boolean ends) { this.endsGame = ends; }
    
    public boolean isWinsGame() { return winsGame; }
    public void setWinsGame(boolean wins) { this.winsGame = wins; }
    
    // Check if single-item rule
    public boolean isSingleItemRule() {
        return targetItem == null || targetItem.isEmpty();
    }
    
    // Check if two-item rule
    public boolean isTwoItemRule() {
        return targetItem != null && !targetItem.isEmpty();
    }
    
    // Add resulting item
    public void addResultingItem(String item) {
        if (resultingItems == null) resultingItems = new ArrayList<>();
        resultingItems.add(item);
    }
    
    // Add connection
    public void addConnection(String direction, String destination) {
        if (newConnections == null) newConnections = new HashMap<>();
        newConnections.put(direction, destination);
    }
}
