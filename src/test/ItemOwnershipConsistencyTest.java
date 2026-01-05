import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ItemOwnershipConsistencyTest {

    @Test
    public void itemCannotExistInInventoryAndLocation() {
        GameState state = GameState.createDefaultGame();
        GameEngine engine = new GameEngine(state);

        Location location = state.getCurrentLocation();
        Item item = location.getItems().get(0);

        engine.processCommand("pick up " + item.getName());

        assertTrue(state.getInventory().getItems().contains(item),
                "Item should be in inventory after pickup");

        assertFalse(location.getItems().contains(item),
                "Item should be removed from location after pickup");
    }
}