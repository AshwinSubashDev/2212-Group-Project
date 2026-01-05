package model;

import java.util.*;

/**
 * Lightweight helper to construct reusable in-memory test worlds for integration tests.
 * This avoids any reliance on external data files and keeps tests fast and deterministic.
 */
public class TestWorldBuilder {
    public static GameState buildSimpleTwoLocationWorld() {
        GameState gs = new GameState();
        Location a = new Location("A", "Location A");
        Location b = new Location("B", "Location B");
        b.setEndingLocation(false);
        a.setStartingLocation(true);
        a.getConnections().put("north", "B");
        Map<String, Location> locations = new LinkedHashMap<>();
        locations.put(a.getName(), a);
        locations.put(b.getName(), b);
        gs.setLocations(locations);
        gs.setItems(new LinkedHashMap<>());
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
