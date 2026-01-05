package test;

import model.*;
import java.util.*;

/**
 * GiveRule Unit Tests 
 * Tests core functionality and integration with GameEngine
 */
public class GiveRuleTest {
    
    private static int passed = 0;
    private static int failed = 0;
    
    public static void main(String[] args) {
        System.out.println("=== GiveRule Tests ===\n");
        
        // Unit Tests
        testConstructors();
        testGettersAndSetters();
        testNullHandling();
        
        // Integration Tests
        testGiveRuleWithGameEngine();
        testRewardApplication();
        testEndsGameFlag();
        
        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total: " + (passed + failed));
    }
    
    static void test(String name, boolean condition) {
        if (condition) {
            System.out.println("✓ PASS: " + name);
            passed++;
        } else {
            System.out.println("✗ FAIL: " + name);
            failed++;
        }
    }
    
    //  UNIT TESTS
    
    static void testConstructors() {
        // Default constructor
        GiveRule rule1 = new GiveRule();
        test("Default constructor - rewardItems not null", rule1.getRewardItems() != null);
        test("Default constructor - endsGame is false", !rule1.isEndsGame());
        
        // Full constructor
        List<String> rewards = Arrays.asList("Gold", "Ring");
        GiveRule rule2 = new GiveRule("Wizard", "Wand", "Thanks!", rewards, true);
        test("Full constructor - sets all fields", 
             "Wizard".equals(rule2.getCharacterName()) &&
             "Wand".equals(rule2.getItemOrAttribute()) &&
             rule2.getRewardItems().size() == 2 &&
             rule2.isEndsGame());
    }
    
    static void testGettersAndSetters() {
        GiveRule rule = new GiveRule();
        
        rule.setCharacterName("Guard");
        rule.setItemOrAttribute("Key");
        rule.setResponseMessage("Thanks!");
        rule.setEndsGame(true);
        
        test("Setters and getters work correctly",
             "Guard".equals(rule.getCharacterName()) &&
             "Key".equals(rule.getItemOrAttribute()) &&
             "Thanks!".equals(rule.getResponseMessage()) &&
             rule.isEndsGame());
    }
    
    static void testNullHandling() {
        GiveRule rule = new GiveRule();
        
        // Setting null rewards should create empty list, not null
        rule.setRewardItems(null);
        
        test("Null rewards converted to empty list", 
             rule.getRewardItems() != null && rule.getRewardItems().isEmpty());
    }
    
    // INTEGRATION TESTS 
    
    static void testGiveRuleWithGameEngine() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        
        // Add item to inventory
        state.getInventory().getItemNames().add("WantedItem");
        
        // Give item to NPC
        String result = engine.handleGive("TestNPC", "WantedItem");
        
        // Verify rule matched and item removed
        boolean ruleMatched = result.contains("Thank you");
        boolean itemRemoved = !state.getInventory().getItemNames().contains("WantedItem");
        
        test("GiveRule matches in GameEngine", ruleMatched);
        test("Item removed from inventory after give", itemRemoved);
    }
    
    static void testRewardApplication() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        
        state.getInventory().getItemNames().add("WantedItem");
        
        boolean noRewardBefore = !state.getInventory().getItemNames().contains("RewardItem");
        engine.handleGive("TestNPC", "WantedItem");
        boolean hasRewardAfter = state.getInventory().getItemNames().contains("RewardItem");
        
        test("Reward added to inventory after successful give", noRewardBefore && hasRewardAfter);
    }
    
    static void testEndsGameFlag() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        
        // Add ending rule
        GiveRule endingRule = new GiveRule("TestNPC", "EndItem", "Victory!", null, true);
        state.getGiveRules().add(endingRule);
        
        // Add item
        state.getItems().put("EndItem", new Item("EndItem", "Ends game", true));
        state.getInventory().getItemNames().add("EndItem");
        
        boolean gameNotOverBefore = !state.isGameOver();
        engine.handleGive("TestNPC", "EndItem");
        boolean gameOverAfter = state.isGameOver();
        
        test("endsGame flag ends the game", gameNotOverBefore && gameOverAfter);
    }
    
    // HELPER
    
    static GameState createTestState() {
        GameState state = new GameState();
        
        // Location with NPC
        Location room = new Location("TestRoom", "A room");
        room.getCharacters().add("TestNPC");
        state.getLocations().put("TestRoom", room);
        state.setCurrentLocationName("TestRoom");
        
        // Character
        model.Character npc = new model.Character();
        npc.setName("TestNPC");
        state.getCharacters().put("TestNPC", npc);
        
        // Items
        state.getItems().put("WantedItem", new Item("WantedItem", "Wanted", true));
        state.getItems().put("RewardItem", new Item("RewardItem", "Reward", true));
        
        // GiveRule: TestNPC wants WantedItem, gives RewardItem
        List<String> rewards = new ArrayList<>();
        rewards.add("RewardItem");
        GiveRule rule = new GiveRule("TestNPC", "WantedItem", "Thank you!", rewards, false);
        List<GiveRule> giveRules = new ArrayList<>();
        giveRules.add(rule);
        state.setGiveRules(giveRules);
        
        // Initialize
        state.setInventory(new Inventory());
        state.setUseRules(new ArrayList<>());
        
        return state;
    }
}
