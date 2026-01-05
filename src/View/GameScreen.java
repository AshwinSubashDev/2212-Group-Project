package View;

import controller.GameController;
import model.GameState;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GameScreen extends JFrame {

    private GameController controller;

    // Left side: room items (with action bar on top)
    private JPanel leftContainer = new JPanel(new BorderLayout());
    private JPanel roomPanel = new JPanel(new FlowLayout());
    private JScrollPane roomScrollPane;
    // Right side: inventory
    private JPanel inventoryPanel = new JPanel(new FlowLayout());
    private JScrollPane inventoryScrollPane;
    private JTextArea messageArea = new JTextArea(5, 40);

    private JButton pickUpButton = new JButton("Pick Up");
    private JButton useButton = new JButton("Use");
    private JButton optionsButton = new JButton("Options");

    private JPopupMenu optionsMenu = new JPopupMenu();
    private JMenuItem saveGameItem = new JMenuItem("Save Game");

    // Selected items
    private String selectedRoomItem = null;
    private String selectedInventoryItem = null;

    public GameScreen() {
        setupUI();
    }

    private void setupUI() {
        setTitle("Adventure Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Build left side (room items under actions)
        leftContainer.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        // Action bar on top of left pane
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionBar.add(pickUpButton);
        actionBar.add(useButton);
        actionBar.add(optionsButton);
        leftContainer.add(actionBar, BorderLayout.NORTH);
        // Room items area (scrollable)
        roomScrollPane = new JScrollPane(roomPanel);
        roomScrollPane.setBorder(BorderFactory.createTitledBorder("Room Items"));
        leftContainer.add(roomScrollPane, BorderLayout.CENTER);
        // Right: Inventory as its own panel, scrollable
        inventoryScrollPane = new JScrollPane(inventoryPanel);
        inventoryScrollPane.setBorder(BorderFactory.createTitledBorder("Inventory"));
        // Assemble into a split layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftContainer, inventoryScrollPane);
        splitPane.setDividerLocation(480);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        // Bottom: messages
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Controls are already placed in the left actionBar above; keep any additional controls here if needed
        bottomPanel.add(new JLabel(" "), BorderLayout.NORTH);

        JScrollPane msgScroll = new JScrollPane(messageArea);
        messageArea.setEditable(false);
        bottomPanel.add(msgScroll, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // Options menu
        optionsMenu.add(saveGameItem);
        optionsButton.addActionListener(e -> {
            optionsMenu.show(optionsButton, 0, optionsButton.getHeight());
        });

        saveGameItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int ret = chooser.showSaveDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (controller != null) controller.onSaveGameRequested(file);
            }
        });

        // PICK UP BUTTON
        pickUpButton.addActionListener(e -> {
            if (selectedRoomItem == null) {
                showMessage("Select a room item to pick up.");
                return;
            }
            if (controller != null) controller.onPickUpItem(selectedRoomItem);
            selectedRoomItem = null;
        });

        useButton.addActionListener(e -> {
            if (selectedInventoryItem == null) {
                showMessage("Select an inventory item to use.");
                return;
            }
            controller.onUseItem(selectedInventoryItem, selectedRoomItem);
            selectedInventoryItem = null;
            selectedRoomItem = null;
        });

        pack();
        setLocationRelativeTo(null);
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    public void update(GameState state) {
        // Room items
        roomPanel.removeAll();
        for (String itemName : state.getLocations()
                .get(state.getCurrentLocationName())
                .getItems()) {

            JButton btn = new JButton(itemName);
            btn.addActionListener(e -> {
                selectedRoomItem = itemName;
                showMessage("Selected room item: " + itemName);

                // If an inventory item is pending use, trigger use now
                if (controller != null) controller.onRoomItemSelected(itemName);
            });
            roomPanel.add(btn);
        }

        // Inventory
        inventoryPanel.removeAll();
        for (String itemName : state.getInventory().getItemNames()) {
            JButton btn = new JButton(itemName);
            btn.addActionListener(e -> {
                selectedInventoryItem = itemName;
                showMessage("Selected inventory item: " + itemName);
                // DO NOT auto-use here
            });
            inventoryPanel.add(btn);
        }

        revalidate();
        repaint();
    }

    public void showMessage(String text) {
        messageArea.append(text + "\n");
    }

    // Public API to reset UI selections when an incompatible two-item use occurs
    public void resetSelections() {
        selectedRoomItem = null;
        selectedInventoryItem = null;
        // Optionally clear or refresh UI indicators here
    }
}
