package model;

import java.util.*;

/**
 * Represents a rule defining what happens when an item is given to a character
 * Used by the Give command to determine NPC reactions and rewards
 */
public class GiveRule {
    private String characterName;           // Name of the character receiving the item
    private String itemOrAttribute;         // Name or attribute of the item being given
    private String responseMessage;         // Message displayed when item is given
    private List<String> rewardItems;       // Items given to player in return
    private boolean endsGame;               // Whether this action ends the game

    /**
     * Creates an empty GiveRule with initialized collections
     */
    public GiveRule() {
        this.rewardItems = new ArrayList<>();
        this.endsGame = false;
    }

    /**
     * Creates a GiveRule with all parameters
     * 
     * @param characterName the name of the character
     * @param itemOrAttribute the item name or attribute the character wants
     * @param responseMessage the message shown when item is given
     * @param rewardItems list of items given as reward it also can be  null
     * @param endsGame whether this action ends the game
     */
    public GiveRule(String characterName, String itemOrAttribute, String responseMessage, 
                    List<String> rewardItems, boolean endsGame) {
        this();
        this.characterName = characterName;
        this.itemOrAttribute = itemOrAttribute;
        this.responseMessage = responseMessage;
        if (rewardItems != null) {
            this.rewardItems = rewardItems;
        }
        this.endsGame = endsGame;
    }

    /**
     * Gets the character name for this rule
     * @return the character's name
     */
    public String getCharacterName() {
        return characterName;
    }

    /**
     * Sets the character name for this rule
     * @param characterName the character's name
     */
    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    /**
     * Gets the item name or attribute this rule matches
     * @return item name or attribute
     */
    public String getItemOrAttribute() {
        return itemOrAttribute;
    }

    /**
     * Sets the item name or attribute this rule matches
     * @param itemOrAttribute item name or attribute
     */
    public void setItemOrAttribute(String itemOrAttribute) {
        this.itemOrAttribute = itemOrAttribute;
    }

    /**
     * Gets the response message shown when item is given o the character
     * @return the response message
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * Sets the response message shown when item is given
     * @param responseMessage the response message
     */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * Gets the list of reward items given to the player
     * @return list of reward item names
     */
    public List<String> getRewardItems() {
        return rewardItems;
    }

    /**
     * Sets the list of reward items given to the player
     * @param rewardItems list of reward item names (null creates empty list)
     */
    public void setRewardItems(List<String> rewardItems) {
        if (rewardItems != null) {
            this.rewardItems = rewardItems;
        } else {
            this.rewardItems = new ArrayList<>();
        }
    }

    /**
     * Checks if giving this item ends the game
     * @return true if game ends, false otherwise
     */
    public boolean isEndsGame() {
        return endsGame;
    }

    /**
     * Sets whether giving this item ends the game
     * @param endsGame true if game should end, false otherwise
     */
    public void setEndsGame(boolean endsGame) {
        this.endsGame = endsGame;
    }
}
