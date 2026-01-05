package model;

import java.io.*;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Game engine that handles command execution and state management
 * Processes pick-up, use, and save commands 
 * Requires: Gson library for JSON serialization
 */
public class GameEngine {
    private GameState gameState;
    private Gson gson;
    
    /**
     * Constructor
     * @param gameState the current game state
     */
    public GameEngine(GameState gameState) {
        this.gameState = gameState;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

     /**
     * Handles 'Start New Game' command. Game data is loaded from the specified starting file
     * If file is not found or can not be read, game is exited and error message is printed
     */

    public void startGame(){
        try(FileReader reader = new FileReader("data/game.json")){
            GameState loadedGameState = gson.fromJson(reader, GameState.class);
                
            if(loadedGameState == null){
                System.out.println("Error: Game data is null");
                System.exit(0);
            }

            this.gameState = loadedGameState; //Setting loaded game state as current state
            
            // Validate loaded game state
            validateGameState();
            
        } catch (IOException e) {
            System.out.println("Error loading game: " + e.getMessage());
            System.exit(0);
        } catch(JsonSyntaxException e){
            System.out.println("Error reading game file: " + e.getMessage());
            System.exit(0);
        }
    }
    
    /**
     * Validates the loaded game state to ensure all required data is present and valid
     */
    private void validateGameState() {
        // Check that locations exist
        if (gameState.getLocations() == null || gameState.getLocations().isEmpty()) {
            System.out.println("Error: No locations defined in game data");
            System.exit(0);
        }
        
        // Check that starting location exists
        String startLoc = gameState.getCurrentLocationName();
        if (startLoc == null || !gameState.getLocations().containsKey(startLoc)) {
            System.out.println("Error: Starting location '" + startLoc + "' not found");
            System.exit(0);
        }
        
        // Validate all location connections point to existing locations
        for (Map.Entry<String, Location> entry : gameState.getLocations().entrySet()) {
            Location loc = entry.getValue();
            if (loc.getConnections() != null) {
                for (Map.Entry<String, String> conn : loc.getConnections().entrySet()) {
                    if (!gameState.getLocations().containsKey(conn.getValue())) {
                        System.out.println("Warning: Location '" + entry.getKey() + 
                            "' has connection to non-existent location '" + conn.getValue() + "'");
                    }
                }
            }
            
            // Validate items in locations exist in items map
            if (loc.getItems() != null) {
                for (String itemName : loc.getItems()) {
                    if (gameState.getItems() == null || !gameState.getItems().containsKey(itemName)) {
                        System.out.println("Warning: Item '" + itemName + "' in location '" + 
                            entry.getKey() + "' not found in items");
                    }
                }
            }
            
            // Validate characters in locations exist in characters map
            if (loc.getCharacters() != null) {
                for (String charName : loc.getCharacters()) {
                    if (gameState.getCharacters() == null || !gameState.getCharacters().containsKey(charName)) {
                        System.out.println("Warning: Character '" + charName + "' in location '" + 
                            entry.getKey() + "' not found in characters");
                    }
                }
            }
        }
        
        // Initialize inventory if null
        if (gameState.getInventory() == null) {
            gameState.setInventory(new Inventory());
        }
    }

    /**
     * Legacy 'Go' method for UI.java compatibility
     * @param currentLocation the name of the current location
     * @param direction the direction the player wants to move in 
     * @return true/false depending on whether player can move to that location
     */
    public boolean goToLocation(String currentLocation, String direction){
        String result = handleGo(direction);
        gameState.setGameMessage(result);
        return !result.contains("invalid") && !result.equals("cannot") && !result.equals("does not") && !result.equals("locked");
        
    }
    
    /**
     * Gets the result message for a go command
     * @param direction the direction to move
     * @return result message
     */
    public String handleGo(String direction) {
        Location currentLocation = gameState.getLocations().get(gameState.getCurrentLocationName());
        if (currentLocation == null) {
            return "Current location is invalid.";
        }
        
        String destinationName = currentLocation.getConnections().get(direction);
        if (destinationName == null) {
            return "You cannot go that way.";
        }
        
        Location destination = gameState.getLocations().get(destinationName);
        if (destination == null) {
            return "That location does not exist.";
        }
        
        // Check if exit is locked. If user has unlockItem in inventory then unlock exit and allow movement
        if (currentLocation.isExitLocked(direction)) {
            String unlockItem = currentLocation.getLockedExits().get(direction);
            if (!gameState.getInventory().getItemNames().contains(unlockItem)){
                return "That exit is locked. You need " + unlockItem + " to enter.";
            } else {
                currentLocation.unlockExit(direction);
            }
        }
        
        gameState.setCurrentLocationName(destinationName);
        gameState.incMoves();
        String locationDescription = destination.getDisplayDescription();
        destination.setVisited(true);
        
        // Check for ending location
        if (destination.isEndingLocation()) {
            gameState.setGameOver(true);
            return destination.getDescription() + "\n\n*** THE END ***";
        }
        
        return "You move to " + destinationName + ".\n" + locationDescription;
    }

    /**
     * Handles 'Examine' command. Displays the object/character description and contained items if applicable 
     * @param objectName name of object being examined
     * @return description message or error message
     */
    public String handleExamine(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return "You must specify something to examine.";
        }
        
        Location currentLocation = gameState.getLocations().get(gameState.getCurrentLocationName());
        if (currentLocation == null) {
            return "Current location is invalid.";
        }
        
        Inventory inventory = gameState.getInventory();
        StringBuilder result = new StringBuilder();
        
        // Check if it's an item
        if (gameState.getItems().containsKey(objectName)) {
            // SPEC REQUIREMENT: Item must be in current location or inventory
            boolean inRoom = currentLocation.getItems().contains(objectName);
            boolean inInventory = inventory.getItemNames().contains(objectName);
            
            if (!inRoom && !inInventory) {
                return "You don't see that here.";
            }
            
            Item item = gameState.getItems().get(objectName);
            result.append(item.getDescription());
            
            // Reveal contained items
            if (item.getContainedItems() != null && !item.getContainedItems().isEmpty()) {
                List<String> foundItems = item.getContainedItems();
                
                result.append("\n\nYou find: ");
                for (String itemName : foundItems) {
                    currentLocation.getItems().add(itemName);
                    result.append(itemName).append(", ");
                }
                result.setLength(result.length() - 2); // Remove trailing comma
                item.getContainedItems().clear();
            }
            
            item.setExamined(true);
            gameState.incMoves();
            return result.toString();
        }
        
        // Check if it's a character
        if (gameState.getCharacters().containsKey(objectName)) {
            // SPEC REQUIREMENT: Character must be in current location
            if (!currentLocation.getCharacters().contains(objectName)) {
                return "You don't see that here.";
            }
            
            Character character = gameState.getCharacters().get(objectName);
            gameState.incMoves();
            return character.getDescription();
        }
        
        return "You don't see that here.";
    }
    
    /**
     * Legacy examine method for UI.java compatibility
     * @param objectName name of object being examined
     * @return true/false based on whether examine was successful
     */
    public boolean examine(String objectName) {
        String result = handleExamine(objectName);
        gameState.setGameMessage(result);
        return !result.equals("You don't see that here.") && !result.equals("You must specify something to examine.");
    }
    
    // ================================================================
    // Drop Item
    /**
     * Drop item from player's inventory into the current location
     * @param itemName The name of the item to drop
     * @return message indicating result
     */
    public String handleDrop(String itemName) {
        if (itemName == null || itemName.isEmpty()) {
            return "You must specify an item to drop.";
        }
        
        Inventory inventory = gameState.getInventory();
        
        // Check if item is in inventory
        if (!inventory.getItemNames().contains(itemName)) {
            return "You don't have that item.";
        }
        
        // Get current location
        Location currentLocation = gameState.getLocations().get(gameState.getCurrentLocationName());
        if (currentLocation == null) {
            return "Current location is invalid.";
        }
        
        // Remove from inventory and add to location
        inventory.getItemNames().remove(itemName);
        currentLocation.getItems().add(itemName);
        
        gameState.incMoves();
        return "You dropped the " + itemName + ".";
    }
    
    /**
     * Legacy dropItem method for UI.java compatibility
     * @param itemId The ID of the item to drop
     * @return true if successful, false otherwise
     */
    public boolean dropItem(String itemId) {
        String result = handleDrop(itemId);
        gameState.setGameMessage(result);
        return result.startsWith("You dropped");
    }

    /**
     * Handles the "Pick Up" command to add an item to the player's inventory
     * @param itemName the name of the item to pick up
     * @return a message indicating the result of the action
     */
    public String handlePickUp(String itemName) {
        // Validates that item exists
        if (itemName == null || itemName.isEmpty()) {
            return "You must specify an item to pick up.";
        }
        
        Item item = gameState.getItems().get(itemName);
        if (item == null) {
            return "That item does not exist in this game world.";
        }

        // Checks if item is in current location
        Location currentLocation = gameState.getLocations().get(gameState.getCurrentLocationName());
        if (currentLocation == null) {
            return "Current location is invalid.";
        }

        if (!currentLocation.getItems().contains(itemName)) {
            return "That item is not in your current location.";
        }

        // Checks if item can be picked up
        if (!item.isPickable()) {
            return "You cannot pick up this item";
        }

        // If all good, removes item from location and adds it to inventory
        currentLocation.getItems().remove(itemName);
        gameState.getInventory().getItemNames().add(itemName);
        
        // Increment turn counter for successful pickup
        gameState.incMoves();

        return "You picked up the " + itemName + "!";
    }

    /**
     * Handles the "Use" command to use item(s)
     * Supports both single-item use and two-item use 
     * @param itemNameOne the name of the first item to use
     * @param itemNameTwo the name of the second item (null for single-item use)
     * @return a message describing what happened
     */
    public String handleUse(String itemNameOne, String itemNameTwo) {
        // Validates first item
        if (itemNameOne == null || itemNameOne.isEmpty()) {
            return "You must specify an item to use.";
        }

        Item itemOne = gameState.getItems().get(itemNameOne);
        if (itemOne == null) {
            return "That item does not exist.";
        }

        Location currentLocation = gameState.getLocations().get(gameState.getCurrentLocationName());
        if (currentLocation == null) {
            return "Current location is invalid.";
        }

        Inventory inventory = gameState.getInventory();
        boolean itemOneInInventory = inventory.getItemNames().contains(itemNameOne);
        boolean item1InLocation = currentLocation.getItems().contains(itemNameOne);

        if (!itemOneInInventory && !item1InLocation) {
            return itemNameOne + " is not available.";
        }

        // Single-item Use
        if (itemNameTwo == null || itemNameTwo.isEmpty()) {
            return handleSingleItemUse(itemNameOne, itemOne, currentLocation, inventory);
        }

        // Two-item Use
        Item itemTwo = gameState.getItems().get(itemNameTwo);
        if (itemTwo == null) {
            return "That target item does not exist.";
        }

        boolean itemTwoInInventory = inventory.getItemNames().contains(itemNameTwo);
        boolean itemTwoInLocation = currentLocation.getItems().contains(itemNameTwo);

        if (!itemTwoInInventory && !itemTwoInLocation) {
            return itemNameTwo + " is not available.";
        }

        return handleTwoItemUse(itemNameOne, itemNameTwo, currentLocation, inventory);
    }

    /**
     * Handles single-item use case
     */
    private String handleSingleItemUse(String itemName, Item item, Location location, Inventory inventory) {
        UseRule matchingRule = findUseRule(itemName, item, null, null);

        if (matchingRule == null) {
            return "You cannot use " + itemName + " in that way.";
        }

        // Removes the item (unless reusable)
        boolean inInventory = inventory.getItemNames().contains(itemName);
        if (inInventory && !item.isReusable()) {
            inventory.getItemNames().remove(itemName);
        } else if (!inInventory && !item.isReusable()) {
            location.getItems().remove(itemName);
        }

        // Adds resulting items
        if (matchingRule.getResultingItems() != null) {
            if (inInventory) {
                inventory.getItemNames().addAll(matchingRule.getResultingItems());
            } else {
                location.getItems().addAll(matchingRule.getResultingItems());
            }
        }
        
        // Apply rule modifications
        applyRuleEffects(matchingRule, location);
        
        // Increment turn counter for successful use
        gameState.incMoves();

        return matchingRule.getResultDescription();
    }

    /**
     * Handles two-item use case
     */
    private String handleTwoItemUse(String itemNameOne, String itemNameTwo, Location location, Inventory inventory) {
        Item itemOne = gameState.getItems().get(itemNameOne);
        Item itemTwo = gameState.getItems().get(itemNameTwo);

        UseRule matchingRule = findUseRule(itemNameOne, itemOne, itemNameTwo, itemTwo);

        if (matchingRule == null) {
            return "You cannot use " + itemNameOne + " with " + itemNameTwo + ".";
        }

        // Determines where results should go
        boolean itemOneInInventory = inventory.getItemNames().contains(itemNameOne);
        boolean itemTwoInInventory = inventory.getItemNames().contains(itemNameTwo);
        boolean resultsToInventory = (itemOneInInventory && itemTwoInInventory);

        // Removes both items (unless reusable)
        if (itemOneInInventory && !itemOne.isReusable()) {
            inventory.getItemNames().remove(itemNameOne);
        } else if (!itemOneInInventory && !itemOne.isReusable()) {
            location.getItems().remove(itemNameOne);
        }

        if (itemTwoInInventory && !itemTwo.isReusable()) {
            inventory.getItemNames().remove(itemNameTwo);
        } else if (!itemTwoInInventory && !itemTwo.isReusable()) {
            location.getItems().remove(itemNameTwo);
        }

        // Adds resulting items
        if (matchingRule.getResultingItems() != null) {
            if (resultsToInventory) {
                inventory.getItemNames().addAll(matchingRule.getResultingItems());
            } else {
                location.getItems().addAll(matchingRule.getResultingItems());
            }
        }
        
        // Apply rule modifications
        applyRuleEffects(matchingRule, location);
        
        // Increment turn counter for successful use
        gameState.incMoves();

        return matchingRule.getResultDescription();
    }
    
    /**
     * Applies all effects from a UseRule (new connections, reveal items, game ending, etc.)
     * @param rule the UseRule to apply effects from
     * @param location the current location
     */
    private void applyRuleEffects(UseRule rule, Location location) {
        // Add new connections (unlock exits)
        if (rule.getNewConnections() != null) {
            for (Map.Entry<String, String> entry : rule.getNewConnections().entrySet()) {
                location.getConnections().put(entry.getKey(), entry.getValue());
            }
        }
        
        // Reveal hidden items
        if (rule.getRevealItems() != null) {
            for (String hiddenItem : rule.getRevealItems()) {
                location.getItems().add(hiddenItem);
            }
        }
        
        // Apply score change (if game uses scoring)
        if (rule.getScoreChange() != 0) {
            // gameState.adjustScore(rule.getScoreChange()); // Uncomment if score system exists
        }
        
        // Check for game ending conditions
        if (rule.isEndsGame()) {
            gameState.setGameOver(true);
        }
        
        // Check for game winning conditions
        if (rule.isWinsGame()) {
            gameState.setGameOver(true);
            gameState.setGameMessage("Congratulations! You have won the game!");
        }
    }

    /**
     * Finds a matching use rule for the given item(s)
     * Supports matching by item name or attribute
     * @param nameOne name of first item
     * @param itemOne Item object for first item
     * @param nameTwo name of second item (null for single-item use)
     * @param itemTwo Item object for second item (null for single-item use)
     * @return matching UseRule or null if no rule found
     */
    private UseRule findUseRule(String nameOne, Item itemOne, String nameTwo, Item itemTwo) {
        for (UseRule rule : gameState.getUseRules()) {
            if (nameTwo == null) {  // For single-item use
                if (rule.getTargetItem() != null) {
                    continue; // Skips rules that expect a target
                }
                if (itemOrAttributeMatches(nameOne, itemOne, rule.getItemUsed())) {
                    return rule;
                }
            } else {  // For two-item use, checks both orderings
                if (rule.getTargetItem() == null) {
                    continue; // Skips single-item rules
                }
                // Check: itemOne used with itemTwo
                if (itemOrAttributeMatches(nameOne, itemOne, rule.getItemUsed()) &&
                    itemOrAttributeMatches(nameTwo, itemTwo, rule.getTargetItem())) {
                    return rule;
                }
                // Check: itemTwo used with itemOne (reverse order)
                if (itemOrAttributeMatches(nameTwo, itemTwo, rule.getItemUsed()) &&
                    itemOrAttributeMatches(nameOne, itemOne, rule.getTargetItem())) {
                    return rule;
                }
            }
        }

        return null;
    }

    /**
     * Checks if an item matches a rule specification by name or attribute
     * @param itemName the name of the item
     * @param item the Item object
     * @param matchingSpecification the rule specification (item name or attribute)
     * @return true if the item matches the specification
     */
    private boolean itemOrAttributeMatches(String itemName, Item item, String matchingSpecification) {
        if (matchingSpecification == null) {
            return false;
        }
        // Matches by exact name
        if (itemName.equalsIgnoreCase(matchingSpecification)) {
            return true;
        }
        // Matches by attribute (if item has no attributes, this returns false)
        if (item != null && item.getAttributes() != null) {
            for (String attribute : item.getAttributes()) {
                if (attribute.equalsIgnoreCase(matchingSpecification)) {
                    return true;
                }
            }
        }

        return false;
    }

    // ========================================
    // GIVE ITEM 
    /**
     * Give an item from player's inventory to a character
     * Uses GiveRules to determine character response
     * @param characterName The name of the character to give item to
     * @param itemName The name of the item to give
     * @return message indicating result
     */
    public String handleGive(String characterName, String itemName) {
        if (characterName == null || characterName.isEmpty()) {
            return "You must specify who to give the item to.";
        }
        if (itemName == null || itemName.isEmpty()) {
            return "You must specify an item to give.";
        }
        
        Inventory inventory = gameState.getInventory();
        
        // Check if item is in inventory
        if (!inventory.getItemNames().contains(itemName)) {
            return "You don't have that item.";
        }
        
        // Check if character exists in current location
        Location currentLocation = gameState.getLocations().get(gameState.getCurrentLocationName());
        if (currentLocation == null || !currentLocation.getCharacters().contains(characterName)) {
            return "There is no one named " + characterName + " here.";
        }
        
        Character character = gameState.getCharacters().get(characterName);
        if (character == null) {
            return "Character not found.";
        }
        
        Item item = gameState.getItems().get(itemName);
        
        // Find matching GiveRule
        GiveRule matchingRule = findGiveRule(characterName, itemName, item);
        
        if (matchingRule == null) {
            return character.getName() + " has no need for the " + itemName + ".";
        }
        
        // Remove item from inventory
        inventory.getItemNames().remove(itemName);
        
        // Add reward items to inventory
        if (matchingRule.getRewardItems() != null) {
            inventory.getItemNames().addAll(matchingRule.getRewardItems());
        }
        
        gameState.incMoves();
        
        // Check if this ends the game
        if (matchingRule.isEndsGame()) {
            gameState.setGameOver(true);
        }
        
        return matchingRule.getResponseMessage();
    }
    
    /**
     * Find a matching GiveRule for character and item
     */
    private GiveRule findGiveRule(String characterName, String itemName, Item item) {
        for (GiveRule rule : gameState.getGiveRules()) {
            if (!rule.getCharacterName().equalsIgnoreCase(characterName)) {
                continue;
            }
            
            String wanted = rule.getItemOrAttribute();
            // Check by item name
            if (itemName.equalsIgnoreCase(wanted)) {
                return rule;
            }
            // Check by attribute
            if (item != null && item.getAttributes() != null) {
                for (String attr : item.getAttributes()) {
                    if (attr.equalsIgnoreCase(wanted)) {
                        return rule;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Legacy giveItem method for UI.java compatibility
     */
    public boolean giveItem(String characterName, String itemName) {
        String result = handleGive(characterName, itemName);
        gameState.setGameMessage(result);
        return !result.contains("no need") && !result.contains("don't have") && !result.contains("not found");
    }
    
   /**
    * Method for combining items to create a new item 
    * @param item1 the name of the first item selected
    * @param item2 the name of the second item selected
    * @return string indicating result
    */
    public String handleCombine(String item1, String item2){
        Inventory inventory = gameState.getInventory();

        if(inventory.getItemNames().isEmpty()){
            return "No items to combine.";
        }

        Item firstItem = gameState.getItems().get(item1);
        Item secondItem = gameState.getItems().get(item2);

        if(firstItem == null || secondItem == null){
            return "Invalid selection.";
        }

        String target1 = firstItem.combinableWith().get("target");
        String target2 = secondItem.combinableWith().get("target");

        if(target1 == null || target2 == null){
            return "Item target not found.";
        }

        /* Checking that both the first and second selected items are combinable with each other. 
            So item1 has item2 listed as its "target" and item2 has item1 listed as its "target"  */
        if(target1.equals(item2) && target2.equals(item1)){
            String combinedItemName = firstItem.combinableWith().get("result"); 

            if(combinedItemName == null){
                return "Resulting item not found.";
            }
            
            //Removing items used for combination
            inventory.getItemNames().remove(item1);
            inventory.getItemNames().remove(item2);
            
            //Adding newly combined item to inventory
            inventory.getItemNames().add(combinedItemName);

            return "Combined successfully! New item acquired: " + combinedItemName;
        } else{
            return "Items are not combinable with each other.";
        }
    }

    /**
     * Legacy combine method for UI.java compatibility
     */
    public boolean combineItems(String characterName, String itemName) {
        String result = handleCombine(characterName, itemName);
        gameState.setGameMessage(result);
        return !result.contains("not") && !result.contains("Invalid") && !result.contains("no");
    }

    /**
     * Handles the "Save Game" command to persist the current game state to a JSON file
     * Serializes the entire GameState object to JSON format for later loading
     * @param saveFile the file to save to
     * @return a message indicating the result of the save operation
     */
    public String handleSaveGame(File saveFile) {
        if (saveFile == null) {
            return "Invalid save file path.";
        }

        try {
            // Creates parent directories if needed
            File parentDirectory = saveFile.getParentFile();
            if (parentDirectory != null && !parentDirectory.exists()) {
                if (!parentDirectory.mkdirs()) {
                    return "Failed to create save directory.";
                }
            }

            // Serializes gameState to JSON with pretty printing for readability
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(gameState);
            
            // Writes JSON to file
            try (FileWriter writer = new FileWriter(saveFile)) {
                writer.write(json);
            }

            return "Game saved successfully!";

        } catch (IOException e) {
            return "Error saving game: " + e.getMessage();
        }
    }

    /**
     * Handles loading saved game data from a JSON file
     * @param savedFile the saved game file 
     * @return true or false depending on success or failure
     */
    public boolean loadSavedGame(File savedFile){
        if(savedFile == null || !savedFile.exists()){
            return false;
        }
        //Reading saved game file 
        try(FileReader reader = new FileReader(savedFile);){
            GameState loadedGameState = gson.fromJson(reader, GameState.class);
                
            if(loadedGameState == null){
                return false;
            }

            this.gameState = loadedGameState; //Setting loaded game state as current state
            return true;
        } catch (IOException e) {
            System.out.println("Error loading game: " + e.getMessage());
            return false;
        } catch(JsonSyntaxException e){
            System.out.println("Error reading game file: " + e.getMessage());
            return false;
        }
    }

    
    /**
     * Gets the current game state
     * @return the GameState object
     */
    public GameState getGameState() {
        return gameState;
    }
    /**
     * Sets the game state
     * @param gameState the GameState object
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    
    // ========================================
    // TALK TO CHARACTER
    /**
     * Talk to a character and get their next dialogue line
     * @param characterName The name of the character to talk to
     * @return the character's dialogue response
     */
    public String handleTalk(String characterName) {
        if (characterName == null || characterName.isEmpty()) {
            return "You must specify who to talk to.";
        }
        
        // Check if character is in current location
        Location currentLocation = gameState.getLocations().get(gameState.getCurrentLocationName());
        if (currentLocation == null || !currentLocation.getCharacters().contains(characterName)) {
            return "There is no one named " + characterName + " here.";
        }
        
        Character character = gameState.getCharacters().get(characterName);
        if (character == null) {
            return "Character not found.";
        }
        
        // SPEC REQUIREMENT: Only increment turns if character has dialogue to say
        if (!character.hasMoreDialogue()) {
            // Don't consume a turn if character has nothing new to say
            return character.getName() + " has nothing more to say.";
        }
        
        gameState.incMoves();
        return character.getName() + " says: \"" + character.nextDialogue() + "\"";
    }
    
    /**
     * Legacy talkToCharacter method for UI.java compatibility
     */
    public void talkToCharacter(String characterName) {
        String result = handleTalk(characterName);
        gameState.setGameMessage(result);
    }
    
    // ========================================
    // REQUIRES TARGET (for two-item use detection)
    /**
     * Checks if an item requires a target (second item) to be used
     * @param itemName The name of the item to check
     * @return true if the item requires a target, false otherwise
     */
    public boolean requiresTarget(String itemName) {
        if (itemName == null) return false;
        
        Item item = gameState.getItems().get(itemName);
        if (item == null) return false;
        
        // Check if there's a single-item rule for this item
        for (UseRule rule : gameState.getUseRules()) {
            if (rule.getTargetItem() == null || rule.getTargetItem().isEmpty()) {
                // This is a single-item rule
                if (itemOrAttributeMatches(itemName, item, rule.getItemUsed())) {
                    return false; // Has a single-item rule, doesn't require target
                }
            }
        }
        
        // Check if there's a two-item rule for this item
        for (UseRule rule : gameState.getUseRules()) {
            if (rule.getTargetItem() != null && !rule.getTargetItem().isEmpty()) {
                if (itemOrAttributeMatches(itemName, item, rule.getItemUsed()) ||
                    itemOrAttributeMatches(itemName, item, rule.getTargetItem())) {
                    return true; // Has a two-item rule
                }
            }
        }
        
        return false; // No rules found, default to not requiring target
    }
    
    /**
     * Increments the turn counter (alias for GameState.incMoves)
     */
    public void incrementTurnsTaken() {
        gameState.incMoves();
    }
}

