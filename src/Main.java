import javax.swing.*;
import model.*;

/**
 * Main class to run game. Initializes GameState, GameEngine and UI
 */

public class Main {
    /** 
     * @param args 
     */
    public static void main(String[] args) {
        GameState gameState = new GameState();
        GameEngine gameEngine = new GameEngine(gameState);

        // Start the game
        SwingUtilities.invokeLater(() -> {
            UI ui = new UI(gameEngine);
        });
    }
}
