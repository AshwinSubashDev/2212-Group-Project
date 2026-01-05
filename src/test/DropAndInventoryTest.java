package test;

import model.*;
import java.util.*;

/**
 *  Drop & Inventory Tests
 */
public class DropAndInventoryTest {
    
    private static int passed = 0;
    private static int failed = 0;
    
    public static void main(String[] args) {
        System.out.println("=== Drop & Inventory Tests (Simplified) ===\n");
        
        // Inventory Tests
        testInventoryConstructors();
        testInventoryOperations();
        
        // Drop Tests  
        testDropSuccess();
        testDropFailures();
        
        // Integration Tests
        testDropIntegration();
        testDropPickUpCycle();
        
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
    
    //  INVENTORY TESTS 
    
    static void testInventoryConstructors() {
        // Default
        Inventory inv1 = new Inventory();
        test("Inventory default - not null and empty", 
             inv1.getItemNames() != null && inv1.getItemNames().isEmpty());
        
        // With collection
        Inventory inv2 = new Inventory(Arrays.asList("A", "B"));
        test("Inventory with items - contains all", 
             inv2.getItemNames().size() == 2 && 
             inv2.getItemNames().contains("A") && 
             inv2.getItemNames().contains("B"));
        
        // Null collection
        Inventory inv3 = new Inventory(null);
        test("Inventory null collection - safe empty list", 
             inv3.getItemNames() != null && inv3.getItemNames().isEmpty());
    }
    
    static void testInventoryOperations() {
        Inventory inv = new Inventory();
        
        inv.getItemNames().add("Key");
        boolean added = inv.getItemNames().contains("Key");
        
        inv.getItemNames().remove("Key");
        boolean removed = !inv.getItemNames().contains("Key");
        
        test("Inventory add/remove works", added && removed);
    }
    
    // DROP TESTS 
    
    static void testDropSuccess() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        
        state.getInventory().getItemNames().add("TestKey");
        Location loc = state.getLocations().get(state.getCurrentLocationName());
        
        int turnsBefore = state.getTurnsTaken();
        boolean inInvBefore = state.getInventory().getItemNames().contains("TestKey");
        boolean notInLocBefore = !loc.getItems().contains("TestKey");
        
        String result = engine.handleDrop("TestKey");
        
        int turnsAfter = state.getTurnsTaken();
        boolean notInInvAfter = !state.getInventory().getItemNames().contains("TestKey");
        boolean inLocAfter = loc.getItems().contains("TestKey");
        boolean msgOk = result.contains("dropped");
        
        // Verify ALL effects
        test("Drop success - message correct", msgOk);
        test("Drop success - removed from inventory", inInvBefore && notInInvAfter);
        test("Drop success - added to location", notInLocBefore && inLocAfter);
        test("Drop success - turn incremented", turnsAfter == turnsBefore + 1);
    }
    
    static void testDropFailures() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        
        // Test null
        int t1 = state.getTurnsTaken();
        String r1 = engine.handleDrop(null);
        test("Drop null - fails without turn", 
             r1.contains("must specify") && state.getTurnsTaken() == t1);
        
        // Test empty
        int t2 = state.getTurnsTaken();
        String r2 = engine.handleDrop("");
        test("Drop empty - fails without turn", 
             r2.contains("must specify") && state.getTurnsTaken() == t2);
        
        // Test non-existent
        int t3 = state.getTurnsTaken();
        String r3 = engine.handleDrop("FakeItem");
        test("Drop non-existent - fails without turn", 
             r3.contains("don't have") && state.getTurnsTaken() == t3);
    }
    
    // INTEGRATION TESTS
    
    static void testDropIntegration() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        Location loc = state.getLocations().get(state.getCurrentLocationName());
        
        // Add 3 items and drop all
        state.getInventory().getItemNames().add("Item1");
        state.getInventory().getItemNames().add("Item2");
        state.getInventory().getItemNames().add("Item3");
        
        engine.handleDrop("Item1");
        engine.handleDrop("Item2");
        engine.handleDrop("Item3");
        
        boolean invEmpty = state.getInventory().getItemNames().isEmpty();
        boolean allInLoc = loc.getItems().contains("Item1") && 
                           loc.getItems().contains("Item2") && 
                           loc.getItems().contains("Item3");
        
        test("Multiple drops - inventory empty", invEmpty);
        test("Multiple drops - all in location", allInLoc);
    }
    
    static void testDropPickUpCycle() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        
        // Start with item in inventory
        state.getInventory().getItemNames().add("CycleItem");
        
        // Drop
        engine.handleDrop("CycleItem");
        boolean dropped = !state.getInventory().getItemNames().contains("CycleItem");
        
        // Pick up
        engine.handlePickUp("CycleItem");
        boolean pickedUp = state.getInventory().getItemNames().contains("CycleItem");
        
        test("Drop/PickUp cycle works", dropped && pickedUp);
    }
    
    //  HELPER 
    
    static GameState createTestState() {
        GameState state = new GameState();
        
        Location room = new Location("TestRoom", "A room");
        state.getLocations().put("TestRoom", room);
        state.setCurrentLocationName("TestRoom");
        
        // Items
        state.getItems().put("TestKey", new Item("TestKey", "Key", true));
        state.getItems().put("Item1", new Item("Item1", "1", true));
        state.getItems().put("Item2", new Item("Item2", "2", true));
        state.getItems().put("Item3", new Item("Item3", "3", true));
        state.getItems().put("CycleItem", new Item("CycleItem", "Cycle", true));
        
        state.setInventory(new Inventory());
        state.setUseRules(new ArrayList<>());
        state.setGiveRules(new ArrayList<>());
        
        return state;
    }
}
