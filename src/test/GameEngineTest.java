package test;

import model.*;
import java.util.Arrays;

/**
 * GameEngine Tests
 */
public class GameEngineTest {
    
    private static int passed = 0;
    private static int failed = 0;
    
    public static void main(String[] args) {
        System.out.println("=== GameEngine Tests ===\n");
        
        // Core commands
        testPickUp();
        testDrop();
        testUse();
        testGo();
        testTalk();
        testExamine();
        testGive();
        
        // Advanced features
        testUseEffects();
        testFailureCases();
        testGameEnd();
        testSaveLoad();
        
        // Starting location tests (user code)
        testGetStartingLocationReturnsCorrect();
        testFindStartingLocationFallback();
        testCurrentLocationNullHandling();
        
        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total: " + (passed + failed));
    }
    //Maine Functions
    
    static void testPickUp() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        int t = state.getTurnsTaken();
        String r = engine.handlePickUp("TestKey");
        test("PickUp works + turn", r.contains("picked up") && state.getTurnsTaken() == t + 1);
    }
    
    static void testDrop() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        state.getInventory().getItemNames().add("TestKey");
        state.getLocations().get("TestRoom").getItems().remove("TestKey");
        int t = state.getTurnsTaken();
        String r = engine.handleDrop("TestKey");
        test("Drop works + turn", r.contains("dropped") && state.getTurnsTaken() == t + 1);
    }
    
    static void testUse() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        state.getInventory().getItemNames().add("Potion");
        int t = state.getTurnsTaken();
        engine.handleUse("Potion", null);
        test("Use single item + turn", state.getTurnsTaken() == t + 1);
        
        state.getInventory().getItemNames().add("Key");
        state.getInventory().getItemNames().add("Lock");
        t = state.getTurnsTaken();
        engine.handleUse("Key", "Lock");
        test("Use two items + turn", state.getTurnsTaken() == t + 1);
    }
    
    static void testGo() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        int t = state.getTurnsTaken();
        engine.handleGo("north");
        test("Go changes location + turn", 
             "NorthRoom".equals(state.getCurrentLocationName()) && state.getTurnsTaken() == t + 1);
    }
    
    static void testTalk() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        int t = state.getTurnsTaken();
        engine.handleTalk("TestNPC");
        test("Talk with dialogue + turn", state.getTurnsTaken() == t + 1);
        
        model.Character npc = state.getCharacters().get("TestNPC");
        while (npc.hasMoreDialogue()) npc.nextDialogue();
        t = state.getTurnsTaken();
        engine.handleTalk("TestNPC");
        test("Talk no dialogue = no turn", state.getTurnsTaken() == t);
    }
    
    static void testExamine() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        String r = engine.handleExamine("TestKey");
        test("Examine in location works", !r.contains("don't see"));
        
        int t = state.getTurnsTaken();
        r = engine.handleExamine("HiddenItem");
        test("Examine not accessible fails", r.contains("don't see") && state.getTurnsTaken() == t);
    }
    
    static void testGive() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        state.getInventory().getItemNames().add("WantedItem");
        int t = state.getTurnsTaken();
        engine.handleGive("TestNPC", "WantedItem");
        test("Give + reward + turn", 
             state.getInventory().getItemNames().contains("RewardItem") && state.getTurnsTaken() == t + 1);
    }
    
    static void testUseEffects() {
        // Unlock connection
        GameState s1 = createTestState();
        GameEngine e1 = new GameEngine(s1);
        s1.getInventory().getItemNames().add("UnlockKey");
        e1.handleUse("UnlockKey", null);
        test("Use unlocks connection", s1.getLocations().get("TestRoom").getConnections().containsKey("secret"));
        
        // Reveal items
        GameState s2 = createTestState();
        GameEngine e2 = new GameEngine(s2);
        s2.getInventory().getItemNames().add("RevealItem");
        e2.handleUse("RevealItem", null);
        test("Use reveals items", s2.getLocations().get("TestRoom").getItems().contains("HiddenGem"));
    }
    
    static void testFailureCases() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        int t = state.getTurnsTaken();
        
        engine.handlePickUp("FakeItem");
        engine.handleDrop("FakeItem");
        engine.handleGo("west");
        
        Item rock = new Item("Rock", "A rock", true);
        state.getItems().put("Rock", rock);
        state.getInventory().getItemNames().add("Rock");
        engine.handleUse("Rock", null);
        
        test("All failures = no turns used", state.getTurnsTaken() == t);
    }
    
    static void testGameEnd() {
        GameState s1 = createTestState();
        GameEngine e1 = new GameEngine(s1);
        s1.getInventory().getItemNames().add("EndGameItem");
        e1.handleUse("EndGameItem", null);
        test("endsGame flag works", s1.isGameOver());
        
        GameState s2 = createTestState();
        GameEngine e2 = new GameEngine(s2);
        s2.getInventory().getItemNames().add("WinItem");
        e2.handleUse("WinItem", null);
        test("winsGame flag works", s2.isGameOver());
        
        GameState s3 = createTestState();
        GameEngine e3 = new GameEngine(s3);
        s3.setTurnLimit(2);
        e3.handlePickUp("TestKey");
        e3.handleGo("north");
        test("Turn limit ends game", s3.isGameOver());
    }
    
    static void testSaveLoad() {
        GameState state = createTestState();
        GameEngine engine = new GameEngine(state);
        state.getInventory().getItemNames().add("TestKey");
        state.setTurnsTaken(5);
        
        java.io.File f = new java.io.File("test_save.json");
        engine.handleSaveGame(f);
        engine.loadSavedGame(f);
        f.delete();
        
        test("Save/Load preserves state", 
             engine.getGameState().getTurnsTaken() == 5 && 
             engine.getGameState().getInventory().getItemNames().contains("TestKey"));
    }
    
    // === YOUR CODE BELOW (UNCHANGED) ===

    static void testGetStartingLocationReturnsCorrect() {
        GameState state = new GameState();
        
        Location room1 = new Location("Room1", "First room");
        Location room2 = new Location("Room2", "Second room");
        room2.setStartingLocation(true);
        Location room3 = new Location("Room3", "Third room");
        
        state.getLocations().put("Room1", room1);
        state.getLocations().put("Room2", room2);
        state.getLocations().put("Room3", room3);
        
        String startLoc = state.getStartingLocation(state.getLocations());
        
        test("getStartingLocation returns correct starting location", "Room2".equals(startLoc));
    }
    
    static void testFindStartingLocationFallback() {
        GameState state = new GameState();
        
        Location room1 = new Location("Room1", "First room");
        Location room2 = new Location("StartRoom", "Starting room");
        room2.setStartingLocation(true);
        
        state.getLocations().put("Room1", room1);
        state.getLocations().put("StartRoom", room2);
        
        Location found = state.findStartingLocation();
        
        boolean foundCorrect = found != null && "StartRoom".equals(found.getName());
        
        test("findStartingLocation returns starting location", foundCorrect);
    }
    
    static void testCurrentLocationNullHandling() {
        GameState state = new GameState();
        
        Location room1 = new Location("DefaultRoom", "A default room");
        state.getLocations().put("DefaultRoom", room1);
        state.setCurrentLocationName(null);
        
        String currentLocName = state.getCurrentLocationName();
        
        if (currentLocName == null || currentLocName.isEmpty()) {
            currentLocName = state.getStartingLocation(state.getLocations());
            if (currentLocName == null || currentLocName.isEmpty()) {
                Location startLoc = state.findStartingLocation();
                if (startLoc != null) {
                    currentLocName = startLoc.getName();
                }
            }
        }
        
        boolean handled = currentLocName != null && !currentLocName.isEmpty();
        
        test("Null currentLocation handled by fallback chain", handled);
    }
    
    static GameState createTestState() {
        GameState state = new GameState();
        
        Location room = new Location("TestRoom", "A test room.");
        room.getItems().add("TestKey");
        room.getCharacters().add("TestNPC");
        room.getConnections().put("north", "NorthRoom");
        state.getLocations().put("TestRoom", room);
        
        Location northRoom = new Location("NorthRoom", "North room.");
        northRoom.getConnections().put("south", "TestRoom");
        state.getLocations().put("NorthRoom", northRoom);
        
        state.setCurrentLocationName("TestRoom");
        
        state.getItems().put("TestKey", new Item("TestKey", "A test key.", true));
        Item potion = new Item("Potion", "A test potion.", true);
        potion.setReusable(false);
        state.getItems().put("Potion", potion);
        state.getItems().put("HiddenItem", new Item("HiddenItem", "A hidden item.", true));
        Item endItem = new Item("EndGameItem", "Ends the game.", true);
        endItem.setReusable(false);
        state.getItems().put("EndGameItem", endItem);
        state.getItems().put("Key", new Item("Key", "A small key.", true));
        Item lockItem = new Item("Lock", "A locked box.", true);
        lockItem.setReusable(true);
        state.getItems().put("Lock", lockItem);
        state.getItems().put("UnlockKey", new Item("UnlockKey", "A magic key.", true));
        state.getItems().put("RevealItem", new Item("RevealItem", "Reveals hidden things.", true));
        state.getItems().put("WantedItem", new Item("WantedItem", "Something the NPC wants.", true));
        state.getItems().put("RewardItem", new Item("RewardItem", "A reward from the NPC.", true));
        state.getItems().put("WinItem", new Item("WinItem", "The winning item!", true));
        
        model.Character npc = new model.Character();
        npc.setName("TestNPC");
        npc.setDescription("A test NPC.");
        npc.setDialogue(Arrays.asList("Hello!", "Goodbye!"));
        state.getCharacters().put("TestNPC", npc);
        
        state.getUseRules().add(new UseRule("Potion", "You drink the potion.", Arrays.asList()));
        UseRule endRule = new UseRule("EndGameItem", "Game ends!", Arrays.asList());
        endRule.setEndsGame(true);
        state.getUseRules().add(endRule);
        state.getUseRules().add(new UseRule("Key", "Lock", "You unlock the box!", Arrays.asList("OpenBox")));
        UseRule unlockRule = new UseRule("UnlockKey", "A secret passage opens!", Arrays.asList());
        java.util.Map<String, String> newConnections = new java.util.HashMap<>();
        newConnections.put("secret", "SecretRoom");
        unlockRule.setNewConnections(newConnections);
        state.getUseRules().add(unlockRule);
        UseRule revealRule = new UseRule("RevealItem", "Hidden treasures appear!", Arrays.asList());
        revealRule.setRevealItems(Arrays.asList("HiddenGem"));
        state.getUseRules().add(revealRule);
        UseRule winRule = new UseRule("WinItem", "You win!", Arrays.asList());
        winRule.setWinsGame(true);
        state.getUseRules().add(winRule);
        
        GiveRule giveRule = new GiveRule();
        giveRule.setCharacterName("TestNPC");
        giveRule.setItemOrAttribute("WantedItem");
        giveRule.setResponseMessage("Thank you! Here's a reward.");
        giveRule.setRewardItems(Arrays.asList("RewardItem"));
        state.getGiveRules().add(giveRule);
        
        state.setInventory(new Inventory());
        return state;
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
}
