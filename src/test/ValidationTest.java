package model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Validation tests: ensure the implemented data models and engine behaviour
 * faithfully reflect the requirements modelling. Tests are designed to be
 * deterministic, in-memory, and UI-agnostic.
 */
public class ValidationTest {

    private GameState gameState;
    private GameEngine engine;

    @BeforeEach
    public void setup() {
        // Build a deterministic validation world
        gameState = ValidationWorldBuilder.buildValidationWorld();
        engine = new GameEngine(gameState);
    }

    @Test
    public void testDataIntegrityAndDefaults() {
        // Basic sanity checks on the in-memory data structures
        assertNotNull(gameState.getLocations(), "Locations map should be initialized");
        assertNotNull(gameState.getItems(), "Items map should be initialized");
        assertNotNull(gameState.getInventory(), "Inventory should be initialized");
        assertNotNull(gameState.getCurrentLocationName(), "Current location name should be set");
        assertNotNull(gameState.getUseRules(), "UseRules list should be initialized");
        assertNotNull(gameState.getGiveRules(), "GiveRules list should be initialized");
        // Ensure starting and ending flags exist
        Location start = gameState.getLocations().get("A");
        Location end = gameState.getLocations().get("B");
        assertNotNull(start, "Starting location A should exist");
        assertNotNull(end, "Ending location B should exist");
        assertTrue(start.isStartingLocation(), "A should be marked as starting location");
        assertTrue(end.isEndingLocation(), "B should be marked as ending location");
    }

    @Test
    public void testLockedExitRequiresKeyThenUnlocksAndEndGame() {
        // From A, north exit is locked; try to go north without key
        String firstAttempt = engine.handleGo("north");
        assertTrue(firstAttempt.toLowerCase().contains("locked"), "Move should be blocked by lock if key missing");
        // Pick up the Key
        String pickup = engine.handlePickUp("Key");
        assertEquals("You picked up the Key!", pickup);
        // Now attempt to go north; should unlock and move to B
        boolean moved = engine.goToLocation("A", "north");
        assertTrue(moved, "After obtaining Key, movement should succeed through unlocked exit");
        assertEquals("B", engine.getGameState().getCurrentLocationName(), "Current location should be B after unlock move");
        // B is ending location; game should be over
        assertTrue(engine.getGameState().isGameOver(), "Reaching ending location should end the game");
        String endMsg = engine.getGameState().getGameMessage();
        assertNotNull(endMsg);
        assertTrue(endMsg.toLowerCase().contains("the end"), "Ending message should indicate end-of-game");
    }

    @Test
    public void testSaveAndLoadConsistency_inMemorySerialization() throws Exception {
        // Take a small action to create non-default state
        engine.handlePickUp("Key"); // pick up key from A
        // Snapshot before save (via in-test deep copy)
        GameState before = deepCopyGameState(engine.getGameState());
        // Save to temp file
        File tmp = File.createTempFile("validation_gamestate", ".json");
        tmp.deleteOnExit();
        String saveResult = engine.handleSaveGame(tmp);
        assertEquals("Game saved successfully!", saveResult);

        // Mutate the in-memory state to ensure load restores it
        engine.getGameState().setCurrentLocationName("A");
        engine.getGameState().setTurnsTaken(5);

        // Load back the saved state
        boolean loaded = engine.loadSavedGame(tmp);
        assertTrue(loaded, "Loading saved game should succeed");
        // Deep copy after load
        GameState after = deepCopyGameState(engine.getGameState());
        // Validate snapshot equality for critical fields
        assertEquals(before.getCurrentLocationName(), after.getCurrentLocationName(),
                "Current location should match snapshot after load");
        assertEquals(before.getTurnsTaken(), after.getTurnsTaken(),
                "Turns taken should match snapshot after load");
        assertEquals(before.getInventory().getItemNames(), after.getInventory().getItemNames(),
                "Inventory contents should match snapshot after load");
    }

    @Test
    public void testInvalidMoveEdgeCase() {
        // From A, only north connection exists; test an invalid direction
        String result = engine.handleGo("south");
        assertEquals("You cannot go that way.", result, "Invalid direction should yield standard message");
    }

    @Test
    public void testEndToEndSequenceValidation() {
        // Pick up key, unlock, move to B, verify end state
        String pickup = engine.handlePickUp("Key");
        assertEquals("You picked up the Key!", pickup);
        boolean moved = engine.goToLocation("A", "north");
        assertTrue(moved, "Move to B should succeed after unlocking");
        assertEquals("B", engine.getGameState().getCurrentLocationName());
        assertTrue(engine.getGameState().isGameOver(), "End state should trigger game over");
        String endMsg = engine.getGameState().getGameMessage();
        assertTrue(endMsg.contains("*** THE END ***"), "End state should include end banner in message");
    }

    // Helper: deep-copy a GameState using Gson (generic, test-only utility)
    private GameState deepCopyGameState(GameState src) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        String json = gson.toJson(src);
        return gson.fromJson(json, GameState.class);
    }
}
