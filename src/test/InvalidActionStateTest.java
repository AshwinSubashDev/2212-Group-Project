import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class InvalidActionStateTest {

    @Test
    public void invalidCommandDoesNotChangeGameState() {
        GameState state = GameState.createDefaultGame();
        GameEngine engine = new GameEngine(state);

        Location startLocation = state.getCurrentLocation();
        int startTurns = state.getTurns();
        int startInventorySize = state.getInventory().getItems().size();

        engine.processCommand("use banana on spaceship");

        assertEquals(startLocation, state.getCurrentLocation(),
                "Location should not change after invalid command");

        assertEquals(startTurns, state.getTurns(),
                "Turns should not increment after invalid command");

        assertEquals(startInventorySize,
                state.getInventory().getItems().size(),
                "Inventory should be unchanged after invalid command");
    }
}