package model;

import java.util.*;

/**
 * Validation-oriented world builder.
 * Builds deterministic in-memory game state designed to validate
 * requirements modelling (data integrity, movement rules, persistence, etc.)
 * without relying on external data files.
 */
public class ValidationWorldBuilder {

    /**
     * Builds a modest but representative world with:
     * - Two locations: A (start) and B (end)
     * - A -> B via "north" (locked by default, requires Key)
     * - A contains a Key item (pickable)
     * - B is an ending location
     * - No NPCs in this minimal validation world; tests can extend for NPCs.
     * @return a GameState prepared for validation tests
     */
    public static GameState buildValidationWorld() {
        GameState gs = new GameState();

        // Locations
        Location a = new Location("A", "Starting location A. A locked north exit awaits.");
        Location b = new Location("B", "Ending location B. You have reached the end.");
        b.setEndingLocation(true);
        a.setStartingLocation(true);

        // Connections
        a.getConnections().put("north", "B");
        // Locked exit requires Key
        a.setLockedExits(Collections.singletonMap("north", "Key"));

        // Items
        Item key = new Item("Key", "A small rusty key.", true);
        a.getItems().add("Key"); // place Key in A

        // Add to state
        Map<String, Location> locations = new LinkedHashMap<>();
        locations.put(a.getName(), a);
        locations.put(b.getName(), b);
        gs.setLocations(locations);

        Map<String, Item> items = new LinkedHashMap<>();
        items.put("Key", key);
        gs.setItems(items);

        // No characters for this basic validation world
        gs.setCharacters(new LinkedHashMap<>());
        gs.setInventory(new Inventory());
        gs.setCurrentLocationName("A");
        gs.setTurnsTaken(0);
        gs.setUseRules(new ArrayList<>());
        gs.setGiveRules(new ArrayList<>());
        gs.setGameMessage("");
        gs.setGameOver(false);
        return gs;
    }
}
