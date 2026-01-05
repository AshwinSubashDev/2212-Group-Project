package model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Lightweight integration tests for the GameEngine and related state
 * objects using an in-memory, self-contained test world.
 * Focuses on end-to-end style scenarios without UI dependencies.
 */
public class IntegrationTest {

    private GameState gameState;
    private GameEngine engine;

    /**
     * Helper to create a richer world for integration tests:
     * - Two locations: A (start) and B (end)
     * - A -> B via "north" is locked by default and requires a Key
     * - A contains a Key item that can unlock the exit
     * - B is an ending location to exercise end-game behavior
     */
    private void buildMinimalWorld() {
        gameState = new GameState();
        // Locations
        Location locA = new Location("A", "Starting location A. A locked north exit awaits.");
        Location locB = new Location("B", "Destination B. An ending location.");
        locB.setEndingLocation(true);
        locA.setStartingLocation(true);
        // Connections (north exists but is locked by default)
        locA.getConnections().put("north", "B");
        // Locked exit requires Key
        locA.setLockedExits(Collections.singletonMap("north", "Key"));
        // Item: Key in location A required to unlock
        Item key = new Item("Key", "A small rusty key.", true);
        // Ensure item is simple and present in location
        locA.getItems().add("Key");

        // Add to gameState
        Map<String, Location> locations = new LinkedHashMap<>();
        locations.put(locA.getName(), locA);
        locations.put(locB.getName(), locB);
        gameState.setLocations(locations);

        // Items
        Map<String, Item> items = new LinkedHashMap<>();
        items.put("Key", key);
        gameState.setItems(items);

        // Characters (none)
        Map<String, Character> chars = new LinkedHashMap<>();
        gameState.setCharacters(chars);

        // Inventory (empty)
        gameState.setInventory(new Inventory());
        gameState.setCurrentLocationName("A");
        gameState.setTurnsTaken(0);
        gameState.setGameOver(false);
        gameState.setGameMessage("");

        // No use/give rules for this basic path
        gameState.setUseRules(new ArrayList<>());
        gameState.setGiveRules(new ArrayList<>());
    }

    @BeforeEach
    public void setup() {
        buildMinimalWorld();
        engine = new GameEngine(gameState);
    }

    @Test
    public void testGoToUnlockedPathUpdatesLocationAndTurns() {
        // Act: move north from A to B (will unlock then move)
        boolean moved = engine.goToLocation("A", "north");
        // Assert: move occurred and state updated
        assertTrue(moved, "Expected movement to succeed after unlocking");
        assertEquals("B", engine.getGameState().getCurrentLocationName(), "Current location should be B after move");
        assertEquals(1, engine.getGameState().getTurnsTaken(), "Turns should increment after a successful move");
        // Message should reflect destination
        String msg = engine.getGameState().getGameMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("You move to B") || msg.contains("You move to B."), "Message should indicate movement to B");
    }

    @Test
    public void testSaveAndLoadConsistency_preservesLocationAndTurns() throws Exception {
        // Move to trigger some state (unlock then move to B)
        engine.goToLocation("A", "north"); // Now at B, turns=1
        assertEquals("B", engine.getGameState().getCurrentLocationName());
        assertEquals(1, engine.getGameState().getTurnsTaken());

        // Save to temp file
        File tmp = File.createTempFile("gamestate", ".json");
        tmp.deleteOnExit();
        String saveResult = engine.handleSaveGame(tmp);
        assertEquals("Game saved successfully!", saveResult);

        // Do some more moves to change state
        // Move back to A is not possible in this minimal world (no back connection),
        // so simulate additional internal state change for test variety
        engine.getGameState().setCurrentLocationName("A");
        engine.getGameState().setTurnsTaken(2);
        assertEquals("A", engine.getGameState().getCurrentLocationName());
        assertEquals(2, engine.getGameState().getTurnsTaken());

        // Load the saved state back
        boolean loaded = engine.loadSavedGame(tmp);
        assertTrue(loaded, "Load should succeed from saved file");
        // Verify state matches pre-save snapshot
        assertEquals("B", engine.getGameState().getCurrentLocationName(), "Location should be restored to B after load");
        assertEquals(1, engine.getGameState().getTurnsTaken(), "Turns should be restored to 1 after load");
        // Also verify that the loaded location is indeed B
        assertEquals("B", engine.getGameState().getCurrentLocationName(), "Loaded location should be B");
    }

    @Test
    public void testLockedExitRequiresKeyThenUnlocksAndEndsGame() {
        // Initially, moving north should be blocked because exit is locked and Key not in inventory
        String firstMove = engine.handleGo("north");
        // Expect a locked message mentioning Key
        assertNotNull(firstMove);
        assertTrue(firstMove.toLowerCase().contains("locked"), "Expected a locked exit message when key not possessed");
        assertTrue(firstMove.contains("Key"), "Message should indicate the required item (Key)");

        // Pick up the Key from A
        String pickup = engine.handlePickUp("Key");
        assertEquals("You picked up the Key!", pickup);
        // After pickup, attempting to go north should unlock and move to B
        boolean moved = engine.goToLocation("A", "north");
        assertTrue(moved, "Should move to B after unlocking with Key");
        assertEquals("B", engine.getGameState().getCurrentLocationName(), "Current location should be B after unlock move");
        assertTrue(engine.getGameState().isGameOver(), "Destination B is an ending location; game should be over");
        String endMsg = engine.getGameState().getGameMessage();
        assertNotNull(endMsg);
        // The message should include the end banner
        assertTrue(endMsg.contains("*** THE END ***"));
    }

    @Test
    public void testInvalidMoveEdgeCase() {
        // From A there is only a north connection; test invalid direction
        String result = engine.handleGo("south");
        assertEquals("You cannot go that way.", result, "Should inform when direction is invalid/not connected");
    }
}
