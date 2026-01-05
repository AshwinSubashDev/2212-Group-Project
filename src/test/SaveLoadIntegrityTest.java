import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.File;

public class SaveLoadIntegrityTest {

    @Test
    public void saveAndLoadPreservesGameState() {
        GameState state = GameState.createDefaultGame();
        GameEngine engine = new GameEngine(state);

        engine.processCommand("move east");
        engine.processCommand("pick up key");

        int turnsBeforeSave = state.getTurns();
        String locationBeforeSave = state.getCurrentLocation().getName();
        int inventoryBeforeSave = state.getInventory().getItems().size();

        String filename = "test_save.json";
        engine.saveGame(filename);

        GameState loadedState = GameEngine.loadGame(filename);

        assertEquals(turnsBeforeSave, loadedState.getTurns());
        assertEquals(locationBeforeSave, loadedState.getCurrentLocation().getName());
        assertEquals(inventoryBeforeSave,
                loadedState.getInventory().getItems().size());

        new File(filename).delete();
    }
}