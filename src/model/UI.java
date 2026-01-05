package model; 

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.*;

/**
 * User Interface that handles displaying game play.
 * It creates and arranges UI elements according to user actions. 
 * Utilizes GameEngine class to handle game logic such as validating movement between locations and giving items to characters
 */

public class UI {
    private final GameEngine gameEngine;  

    private JTextArea description;           // Text area for main game frame
    private JTextArea inventDescription;     // Text area for inventory 

    private JLabel turnCounterLabel;     // JLabel used to display turn counter 
    private Location currLocation;       // Current location 
    private String currentLocName;       // Name of current location 
    private CardLayout cardLayout;       // CardLayout used to store locations 
    
    private Map<String, JLayeredPane> locationPanels = new HashMap<>(); // HashMap to hold location JPanels for easier access when updating 
    private Map<String, JLabel> locationBackgrounds = new HashMap<>();  // HashMap to hold background JPanels for locations 

    private JPanel outerlayer, movementPanel, actionPanel, textArea;

    private JFrame frame, inventFrame;
    private JLayeredPane inventPane, layeredPane;

    private JButton north, south, east, west;
    private JButton menuButton, inventoryButton, talk, give, use, examine, pickUp;

    // Integers indicating layers. Used for JLayeredPane to organize view 
    public static final Integer BG_LAYER = 0;
    public static final Integer ITEM_LAYER = 1;
    public static final Integer CHAR_LAYER = 2;
    public static final Integer UI_LAYER = 3;

    private actionMode currentMode = actionMode.DEFAULT;   // enum to indicate current action. set to default if no action is being performed

    private String firstItem = null;       // String to hold name of first selected JLabel 
    private String secondItem = null;      // String to hold name of second selected JLabel 

    // enum to handle different action commands. Action mode is set to default when no action has been selected 
    public enum actionMode{
        DEFAULT,    
        COMBINE,
        USE,
        GIVE,
        TALK,
        DROP,
        EXAMINE,
        PICKUP
    }

    public UI(GameEngine gameEngine){
        this.gameEngine = gameEngine;

        mainMenu("start"); // Displays starting page first 
    }

    /**
     * Creates text areas for main menu and main frame 
     * @return empty JTextArea 
     */
    public JTextArea createTextArea(){
        description = new JTextArea();
        description.setBounds(60,450,900,100);
        description.setEditable(false);
        description.setBackground(Color.black);
        description.setForeground(Color.white);
        description.setFont(new Font("Serif",Font.PLAIN, 20));
        description.setLineWrap(true);
        return description;
    }

    /**
     * Creates and displays main menu screen. 
     * Takes a parameter 'option' that indicates whether starting screen or menu screen should be shown. 
     * Both screens use similar buttons/actions, so depending on the value of option the screens are set accordingly
     * @param option string indicating what frame layout should look like. If option = 'start', starting screen is shown, if option = 'menu', menu screen is shown 
     */
    public void mainMenu(String option){
        JFrame menuFrame = new JFrame("New Game");
        menuFrame.setSize(1000,600);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.getContentPane().setBackground(Color.black);
        menuFrame.setLayout(null);

        menuFrame.add(createTextArea());

        // Starting Screen 
        if(option.equals("start")){
            JTextArea projectTitle = new JTextArea("Island Escape");
            projectTitle.setBounds(420,150,210,40);
            projectTitle.setEditable(false);
            projectTitle.setBackground(Color.white);
            projectTitle.setForeground(Color.black);
            projectTitle.setFont(new Font("Serif", Font.BOLD, 35));
            displayText("Welcome to 'Island Escape'! \n In this game you take on the role of an adventurer who was ship wrecked on a mysterious island.\n You have no way of getting home, \n but you've heard there's a powerful witch somewhere on this island who might be able to help.");


            JButton newGame = new JButton("Start New Game");
            newGame.setBounds(450, 250, 150, 40);

            newGame.addActionListener((ActionEvent e) -> {
                menuFrame.setVisible(false);
                gameEngine.startGame();
                createMainFrame();
            });

            JButton savedGame = new JButton("Load Saved Game");
            savedGame.setBounds(450, 290, 150, 40);

            savedGame.addActionListener((ActionEvent e) -> {
                File savedgame = new File("data/savedGame.json");
                if (gameEngine.loadSavedGame(savedgame) == true){
                    menuFrame.setVisible(false);
                    createMainFrame();
                } else {
                    displayText("No saved games found.");
                }
            });

            // Adding buttons to frame 
            menuFrame.add(newGame);
            menuFrame.add(projectTitle);
            menuFrame.add(savedGame);
        }
        
        // Menu Screen 
        if(option.equals("menu")){
            menuFrame.setVisible(true);
            frame.setVisible(false);

            // Button to save current game. Writes to specified file
            JButton saveGame = new JButton("Save Game");
            saveGame.setBounds(450, 290, 150, 40);

            saveGame.addActionListener((ActionEvent e) -> {
                File saveFile = new File("data/savedGame.json");
                displayText(gameEngine.handleSaveGame(saveFile));
            });

            // Button to resume game play 
            JButton resume = new JButton("Resume Game");
            resume.setBounds(450, 250, 150, 40);
            resume.addActionListener((ActionEvent e) -> {
                    frame.setVisible(true);
                    menuFrame.setVisible(false);
            });
            
            menuFrame.add(resume);
            menuFrame.add(saveGame);
        }

        // Ending Screen
        if(option.equals("ending")){
            menuFrame.setVisible(true);
            frame.setVisible(false);

            // Ending message 
            JTextArea endingText = new JTextArea("Congratulations!! \n You successfully escaped the island. \n Select what you would like to do next");
            endingText.setLineWrap(true);
            endingText.setFont(new Font("Serif", Font.PLAIN, 30));
            endingText.setBounds(200,150,500,110);        
            endingText.setBackground(Color.white);
            endingText.setEditable(false);
            endingText.setForeground(Color.black);

            menuFrame.add(endingText);

            // Adding replay game button 
            JButton replayGame = new JButton("Play Again");
            replayGame.setBounds(255, 330, 150, 40);

            replayGame.addActionListener((ActionEvent e) -> {
                menuFrame.setVisible(false);
                gameEngine.startGame();
                createMainFrame();
            });

            menuFrame.add(replayGame);

        }

        JButton quitGame = new JButton("Quit");
        quitGame.setBounds(450, 330, 150, 40);
        
        menuFrame.add(quitGame);

        // Adding background panel and image to frame 
        JPanel bgPanel = new JPanel();
        bgPanel.setBounds(50,50,900,400);
        bgPanel.setLayout(null);

        menuFrame.add(bgPanel);

        JLabel bgLabel = new JLabel();
        bgLabel.setBounds(0,0,900,400);
        ImageIcon bgIcon = new ImageIcon("images/island.jpg");
        
        bgLabel.setIcon(bgIcon);
        
        bgPanel.add(bgLabel); 
        menuFrame.setVisible(true);

        quitGame.addActionListener((ActionEvent e) -> {
            int choice = JOptionPane.showConfirmDialog(
                menuFrame,
                "Are you sure you want to quit the game?",
                "Quit Game",
                JOptionPane.YES_NO_OPTION
            );
        
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
                }
            });
    }

    /**
     * Setting up locations in CardLayout 
     * @param outerlayer JPanel for location 
     * @param cardLayout CardLayout containing all location panels 
     * @param locations map containing all locations and directions 
     */
    public void setLocations(JPanel outerlayer, CardLayout cardLayout, Map<String, Location> locations){
        for(String name : locations.keySet()){
            String imagePath = locations.get(name).getImagePath();
            JLayeredPane lp = createLocation(imagePath,locations, name);
            outerlayer.add(lp,name);
            locationPanels.put(name, lp); // Adding location panels to hashmap for easier lookup when updating 
        }

        // Getting starting Location
        currentLocName = gameEngine.getGameState().getCurrentLocationName();
        if (currentLocName == null || currentLocName.isEmpty()) {
            Location startLoc = gameEngine.getGameState().findStartingLocation();
            if (startLoc != null) {
                currentLocName = startLoc.getName();
            }
        }

        currLocation = locations.get(currentLocName);
        cardLayout.show(outerlayer, currentLocName);
    }
    
    /**
     * Creates items to be placed in each location 
     * @param imagePath string containing image path for object
     * @param dimensions integer array containing values for x,y coordinates, width and height 
     * @param itemName name of item being created 
     * @return JLabel representing item
     */
    private JLabel createObjects(String imagePath, Integer[] dimensions, String itemName){
        JLabel item = new JLabel(new ImageIcon(imagePath));
        item.setBounds(dimensions[0],dimensions[1],dimensions[2],dimensions[3]);
        item.setName(itemName);

        // Adding mouse listener to each item to handle various action commands 
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentMode == actionMode.EXAMINE) {
                    currentMode = actionMode.DEFAULT;
                    gameEngine.examine(itemName);
                    displayText(gameEngine.getGameState().getGameMessage());
                    updateTurnCounter(gameEngine.getGameState().getTurnsTaken());

                }

                if(currentMode == actionMode.PICKUP){
                    currentMode = actionMode.DEFAULT;
                    String result = gameEngine.handlePickUp(itemName);
                    if(result.equals("You picked up the " + itemName + "!")){
                        item.setVisible(false); // Removing item from scene if picked up 
                    }
                    displayText(result);
                    updateTurnCounter(gameEngine.getGameState().getTurnsTaken());

                }

                if(currentMode == actionMode.USE){
                    handleInventoryClick(item);
                    e.consume();
                }
            }
        });

        return item;
    }

    /**
     * Creating characters to be added to locations 
     * @param imagePath string containing image path for character
     * @param dimensions integer array containing coordinates and dimensions for image
     * @param characterName name of character being added
     * @return JLabel representing character 
     */
    private JLabel createCharacter(String imagePath, Integer[] dimensions, String characterName){
        JLabel character = new JLabel(new ImageIcon(imagePath));

        character.setBounds(dimensions[0],dimensions[1],dimensions[2],dimensions[3]);
        character.setName(characterName);

        // Adding mouse listener to characters to handle various action commands 
        character.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentMode == actionMode.TALK) {
                    currentMode = actionMode.DEFAULT;
                    gameEngine.talkToCharacter(characterName);
                    displayText(gameEngine.getGameState().getGameMessage());
                    updateTurnCounter(gameEngine.getGameState().getTurnsTaken());

                } 
                if (currentMode == actionMode.EXAMINE){
                    currentMode = actionMode.DEFAULT;
                    gameEngine.examine(characterName);
                    displayText(gameEngine.getGameState().getGameMessage());
                    updateTurnCounter(gameEngine.getGameState().getTurnsTaken());

                }

                if(currentMode == actionMode.GIVE){
                    handleInventoryClick(character);
                }
            }
        });
        return character;
    }
    
    /**
     * Creating complete location panel including items and characters
     * @param imagePath string containing location image path 
     * @param locations map of all locations and directions to be added 
     * @param name name of location 
     * @return JLayeredPane of location 
     */
    private JLayeredPane createLocation(String imagePath, Map<String, Location> locations, String name){
        JLayeredPane lp = new JLayeredPane();
        lp.setBounds(0,0,1000,450);
        lp.setLayout(null);
        lp.setName(name);

        // Setting background image for location 
        JLabel bg = new JLabel(new ImageIcon(imagePath));
        bg.setBounds(0,0,1000,450);
        lp.add(bg,BG_LAYER);

        locationBackgrounds.put(name, bg); // Adding background to hashmap for easier lookup when updating 

        // Adding items to location 
        for(String item:locations.get(name).getItems()){
            Map<String,Item> itemList = gameEngine.getGameState().getItems();
            Item items = itemList.get(item);
            
            String itemImage = items.getImagePath();
            Integer[] itemDimensions = items.getDimensions();
            String itemName = items.getName();
            lp.add(createObjects(itemImage, itemDimensions, itemName), ITEM_LAYER);
        }

        // Adding characters to location 
        for(String character : locations.get(name).getCharacters()){
            Map<String, Character> characterList = gameEngine.getGameState().getCharacters();
            Character characters = characterList.get(character);
            String characterName = characters.getName();
            String charImagePath = characters.getImagePath();
            Integer[] charDimensions = characters.getDimensions();
            lp.add(createCharacter(charImagePath, charDimensions, characterName), CHAR_LAYER);
        }

        // Adding mouse listener to background. If user doesn't click on valid item/character, set action mode to default and continue game play
        lp.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    currentMode = actionMode.DEFAULT;
                    displayText("Invalid selection");
                }
            });

        return lp;
    }

    /**
     * Creating main game frame. This displays locations, actions, and game messages 
     */
    private void createMainFrame() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
    
        // ----- Create LAYERED PANE -----
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);  
        layeredPane.setPreferredSize(new Dimension(1000,600));
    
        // ----- BACKGROUND (outerlayer) -----
        cardLayout = new CardLayout();
        outerlayer = new JPanel(cardLayout);
        outerlayer.setBounds(0,0,1000,600);
        outerlayer.setOpaque(false);
        setLocations(outerlayer, cardLayout, gameEngine.getGameState().getLocations());

        layeredPane.add(outerlayer, BG_LAYER);

        // --- CREATING INVENTORY ---
        createInventory();

        // ----- MOVEMENT PANEL -----
        movementPanel = new JPanel(null);
        movementPanel.setBounds(0, 0, 1000, 600);
        movementPanel.setOpaque(false);
    
        // Movement buttons to change locations 
        north = new JButton();
        north.setBounds(500,10,30,45);
        south = new JButton();
        south.setBounds(500,400,30,45);
        east = new JButton();
        east.setBounds(950,225,45,30);
        west = new JButton();
        west.setBounds(10,225,45,30);
    
        north.setIcon(new ImageIcon("images/north.png"));
        south.setIcon(new ImageIcon("images/south.png"));
        west.setIcon(new ImageIcon("images/west.png"));
        east.setIcon(new ImageIcon("images/east.png"));
    
        // If location is accessible then frame is updated to show new location
        north.addActionListener(e -> {
            if(gameEngine.goToLocation(currentLocName,"north")){
                showLocation(gameEngine.getGameState().getCurrentLocationName());
                displayText(gameEngine.getGameState().getGameMessage());
                updateTurnCounter(gameEngine.getGameState().getTurnsTaken());

            }
        });
            
        south.addActionListener(e -> {
            if(gameEngine.goToLocation(currentLocName,"south")){
                showLocation(gameEngine.getGameState().getCurrentLocationName());
                displayText(gameEngine.getGameState().getGameMessage());
                updateTurnCounter(gameEngine.getGameState().getTurnsTaken());

            }
        });

        east.addActionListener(e -> {
            if(gameEngine.goToLocation(currentLocName,"east")){
                showLocation(gameEngine.getGameState().getCurrentLocationName());
                displayText(gameEngine.getGameState().getGameMessage());
                updateTurnCounter(gameEngine.getGameState().getTurnsTaken());

            }
        });

        west.addActionListener(e -> {
            if(gameEngine.goToLocation(currentLocName,"west")){
                showLocation(gameEngine.getGameState().getCurrentLocationName());

                displayText(gameEngine.getGameState().getGameMessage());
                updateTurnCounter(gameEngine.getGameState().getTurnsTaken());

            }
        });

        movementPanel.add(north);
        movementPanel.add(south);
        movementPanel.add(east);
        movementPanel.add(west);
    
        layeredPane.add(movementPanel, UI_LAYER); 
    
        // --- SETTINGS PANEL ----

        JPanel settingsPanel = new JPanel();
        settingsPanel.setPreferredSize(new Dimension(1000,70));
        settingsPanel.setBackground(Color.BLACK);

        inventoryButton = new JButton(new ImageIcon("images/inventory.png"));
        menuButton = new JButton(new ImageIcon("images/menu.png"));

        menuButton.addActionListener(e -> mainMenu("menu"));
        inventoryButton.addActionListener(e -> displayInventory());

        settingsPanel.add(menuButton);
        settingsPanel.add(inventoryButton);
    
        // ----- ACTION PANEL -----
        actionPanel = new JPanel();
        actionPanel.setPreferredSize(new Dimension(1000,50));
        examine = new JButton("Examine");
        actionPanel.add(examine);
    
        // Handling 'examine' command
        examine.addActionListener(e -> {
            currentMode = actionMode.EXAMINE;
            displayText("Click on an item or character in the scene to examine it.");
            updateTurnCounter(gameEngine.getGameState().getTurnsTaken());
            
        });

        pickUp = new JButton("Pick Up");
        actionPanel.add(pickUp);
    
        // Handling 'pick up' command
        pickUp.addActionListener(e -> {
            currentMode = actionMode.PICKUP;

            displayText("Click on an item in the scene to pick it up.");
            updateTurnCounter(gameEngine.getGameState().getTurnsTaken());

            
        });

        talk = new JButton("Talk");
        actionPanel.add(talk);
    
        // Handling 'talk' command
        talk.addActionListener(e -> {
            currentMode = actionMode.TALK;

            displayText("Select character to talk too.");
            updateTurnCounter(gameEngine.getGameState().getTurnsTaken());

            
        });

        // --- CREATING TURN COUNTER ---
        int turnCount = gameEngine.getGameState().getTurnsTaken();
        turnCounterLabel = new JLabel("Turns: " + turnCount);
        actionPanel.add(turnCounterLabel);

        // ----- TEXT PANEL -----
        textArea = new JPanel(); // Background for text 
        textArea.setBounds(0,450,1000,200);
        textArea.setBackground(Color.BLACK);

        textArea.add(createTextArea()); 
        layeredPane.add(textArea, CHAR_LAYER);
    
        // ----- ADD TO FRAME -----
        frame.add(layeredPane, BorderLayout.CENTER);
        frame.add(actionPanel, BorderLayout.SOUTH);
        frame.add(settingsPanel, BorderLayout.NORTH);
    
        frame.pack();     // sizes the layeredPane correctly

        // displaying the starting location 
        showLocation(currentLocName);
        displayText(currLocation.getDescription());
        
        frame.setVisible(true);
    }

    /**
     * Displays game messages on screen 
     * @param text text to be displayed 
     */
    public void displayText(String text){
        description.setText(text);
    }

    /**
     * Clears text area
     */
    public void clearDisplayText(){
        description.setText("");
    }

    /**
     * Displays inventory messages on screen 
     * @param text text to be displayed 
     */
    public void inventoryText(String text){
        inventDescription.setText(text);
    }

    /**
     * Clear inventory text area 
     */
    public void clearInventoryText(){
        inventDescription.setText("");
    }

    /**
     * Creating inventory frame. When opened inventory items and actions are shown 
     */
    private void createInventory(){
        // ---- CREATING INVENTORY FRAME ----
        inventFrame = new JFrame();
        inventFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        inventFrame.setBounds(0,0,1000,600);

        inventPane = new JLayeredPane();
        inventPane.setBounds(0,0,1000,600);
        inventPane.setLayout(null);

        JPanel bgPanel = new JPanel();
        bgPanel.setBounds(0,0,1000,450);
        bgPanel.setBackground(Color.BLACK);

        JLabel bgImage = new JLabel(new ImageIcon("images/invent.png"));
        bgImage.setBounds(0,70,1000,450);
        
        bgPanel.add(bgImage);

        // Adding mouse listener to background panel. If user clicks on background instead of inventory item, set action mode to default
        bgPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentMode = actionMode.DEFAULT;

                inventoryText("Invalid selection.");
            }
        });
        
        inventPane.add(bgPanel,0);
        

        // --- ADDING TEXT PANEL ---
        inventDescription = new JTextArea();
        inventDescription.setBounds(0,450,1000,100);
        inventDescription.setEditable(false);
        inventDescription.setBackground(Color.black);
        inventDescription.setForeground(Color.white);
        inventDescription.setFont(new Font("Serif",Font.PLAIN, 20));
        inventDescription.setLineWrap(true);

        inventPane.add(inventDescription,3);

        // --- ADDING INVENTORY ACTIONS ---
        JPanel actionsPanel = new JPanel();
        actionsPanel.setSize(1000,70);

        JButton exit = new JButton("Exit");
        actionsPanel.add(exit);

        // Handling 'exit' command
        exit.addActionListener(e -> {
            frame.setVisible(true);
            inventFrame.setVisible(false);
            
        });

        JButton combine = new JButton("Combine");
        actionsPanel.add(combine);
    
        // Handling 'combine' command
        combine.addActionListener(e -> {
            currentMode = actionMode.COMBINE;

            firstItem = null;
            secondItem = null;
            inventoryText("Select first item to combine");
        });

        JButton drop = new JButton("Drop Item");
        actionsPanel.add(drop);
    
        // Handling 'drop' command
        drop.addActionListener(e -> {
            currentMode = actionMode.DROP;
            firstItem = null;
            inventoryText("Select item to drop.");
        });

        use = new JButton("Use Item");
        actionsPanel.add(use);
    
        // Handling 'use' command
        use.addActionListener(e -> {
            currentMode = actionMode.USE;

            firstItem = null;
            secondItem = null;
            inventoryText("Select item to use.");
        });

        give = new JButton("Give Item");
        actionsPanel.add(give);
    
        // Handling 'give' command
        give.addActionListener(e -> {
            currentMode = actionMode.GIVE;

            firstItem = null;
            secondItem = null;
            inventoryText("Select item to give.");
            
        });

        examine = new JButton("Examine");
        actionsPanel.add(examine);
    
        // Handling 'examine' command
        examine.addActionListener(e -> {
            currentMode = actionMode.EXAMINE;
            inventoryText("Select item to examine.");
            
        });

        inventFrame.add(actionsPanel,BorderLayout.NORTH);
        inventFrame.add(inventPane,BorderLayout.CENTER);
        
        inventFrame.setVisible(false); // Inventory frame is set to not visible until it is opened
    }

    /**
     * Displays the updated turn counter on screen 
     * @param turns integer indicating total number of turns taken
     */
    public void updateTurnCounter(int turns){
        turnCounterLabel.setText("Turns: " + turns);
    }

    /**
     * Adds items to the inventory frame 
     * @param inventory inventory being updated 
     */
    public void updateInventory(Inventory inventory){
        // Removing old items by clearing the layer containing item labels
        Component[] comps = inventPane.getComponents();
        for (Component c : comps) {
            int itemLayer = inventPane.getLayer(c);
            if (itemLayer == 2) { 
                inventPane.remove(c);
            }
        }

        // positioning for inventory items (ensures they align with background image)
        int x = 195;
        int y = 40;
        int spacing = 75;

        // Adding new items to inventory 
        for (String itemName : inventory.getItemNames()) {
            Item item = gameEngine.getGameState().getItems().get(itemName);

            JLabel itemLabel = createInventItems(item);
            itemLabel.setBounds(x, y, 64, 64);
            inventPane.add(itemLabel, Integer.valueOf(2)); // Adding items to layer 2
            
            x += spacing;
            if (x > 700) {  // Shift item to next row
                x = 50;
                y += spacing;
            }
        }
        // Updating inventory display to reflect changes 
        inventPane.revalidate();
        inventPane.repaint();
    }

    /**
     * Method to create inventory items. Sets item pictures and mouse listener 
     * @param item Item to be added 
     * @return JLabel containing item image and name 
     */
    private JLabel createInventItems(Item item){
        String imagePath = item.getImagePath();
        JLabel inventItem = new JLabel(new ImageIcon(imagePath));
        inventItem.setName(item.getName());

        // Adding mouse listener to handle when user clicks on item JLabel 
        inventItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(currentMode == actionMode.EXAMINE){
                    currentMode = actionMode.DEFAULT;
                    inventoryText(item.getName() + ": " + item.getDescription());
                    updateTurnCounter(gameEngine.getGameState().getTurnsTaken());
                } else {
                    handleInventoryClick(inventItem); 
                    e.consume();
                }
                
            }
        });

        return inventItem;
    }
 
    /**
     * Displays inventory 
     * Sets inventory frame to visible and main frame to not visible. Also updates inventory to reflect any changes made 
     */
    public void displayInventory(){
        updateInventory(gameEngine.getGameState().getInventory());
        inventFrame.setVisible(true);
        frame.setVisible(false);
    }

    /**
     * Handles inventory actions such as drop, combine, use, etc.
     * @param itemLabel item JLabel that was clicked
     */
    private void handleInventoryClick(JLabel itemLabel){
        String clickedName = itemLabel.getName(); 

        switch (currentMode) {
            case USE :
                if (firstItem == null) {
                    firstItem = clickedName;
                    
                    // Closing inventory frame to display main frame for user to select where to use the item
                    frame.setVisible(true);
                    inventFrame.setVisible(false);
                    displayText("Select target to use item on.");
                    return;
                }
                
                // Selecting second item
                if (secondItem == null) {
                    // Prevent selecting the same item twice
                    if (clickedName.equals(firstItem)) {
                        displayText("You must select two different items.");
                        currentMode = actionMode.DEFAULT;
                        return;
                    }
                    
                    secondItem = clickedName;
                    
                    // Displaying the result of handleUse
                    displayText(gameEngine.handleUse(firstItem,secondItem));
                    updateInventory(gameEngine.getGameState().getInventory());
                    updateScene(gameEngine.getGameState().getCurrentLocation());
                    updateTurnCounter(gameEngine.getGameState().getTurnsTaken());
                    clearInventoryText();
                    
                    checkGameEnds(); // Checking if use action ended game
                    
                }
                
                // Resetting variables
                currentMode = actionMode.DEFAULT;
                firstItem = null;
                secondItem = null;
                break;
            case COMBINE :
                if (firstItem == null) {
                    firstItem = clickedName;
                    inventoryText("Select second item to combine.");
                    return;
                }

                // Selecting second item
                if (secondItem == null) {
                    // Prevent duplicate selection
                    if (clickedName.equals(firstItem)) {
                        inventoryText("You must select two different items.");
                        currentMode = actionMode.DEFAULT;
                        return;
                    }

                    secondItem = clickedName;
                    
                    // Printing message regarding result of combination  
                    if(gameEngine.combineItems(firstItem, secondItem)){
                        updateInventory(gameEngine.getGameState().getInventory());
                        updateTurnCounter(gameEngine.getGameState().getTurnsTaken());
                    }
                }
                inventoryText(gameEngine.getGameState().getGameMessage());

                currentMode = actionMode.DEFAULT;
                firstItem = null;
                secondItem = null;
                break;
            
            case DROP :
                firstItem = clickedName;

                if(gameEngine.dropItem(firstItem)){
                    updateInventory(gameEngine.getGameState().getInventory());
                    updateScene(gameEngine.getGameState().getCurrentLocation());
                    updateTurnCounter(gameEngine.getGameState().getTurnsTaken());
                    inventoryText(gameEngine.getGameState().getGameMessage());
                }
                
                currentMode = actionMode.DEFAULT;
                firstItem = null;
            
                break; 

            case GIVE : 
                if(firstItem == null){
                    firstItem = clickedName; // Holds item name
    
                    frame.setVisible(true);
                    inventFrame.setVisible(false);
                    displayText("Select target to use item on.");
                    clearInventoryText();
                    return;
                }
    
                if(secondItem == null){
                    if(clickedName.equals(firstItem)){
                        displayText("You must select two different items.");
                        currentMode = actionMode.DEFAULT;
                        return;
                    }
    
                    secondItem = clickedName; // Holds character name
    
                    if(gameEngine.giveItem(secondItem, firstItem)){ // Updating inventory if give was successful
                        updateInventory(gameEngine.getGameState().getInventory());
                        checkGameEnds(); // Checking if give action ended game
                    }
                    updateTurnCounter(gameEngine.getGameState().getTurnsTaken());
                    displayText(gameEngine.getGameState().getGameMessage());

                }
                
                currentMode = actionMode.DEFAULT;
                firstItem = null;
                secondItem = null;
                break;
            
            case DEFAULT :
                inventoryText("Select action first");
                break;
                
            default : throw new AssertionError();
        }
    }

    /**
     * Updates locations when changes are made, e.g. adding/removing items 
     * @param loc location that is being updated
     */
    public void updateScene(Location loc){
        // Getting location panel to update 
        JLayeredPane layer = locationPanels.get(loc.getName());
        updateLocationLayer(layer, loc);
    
        layer.revalidate();
        layer.repaint();
    }

    /**
     * Checks whether game has ended. If yes, then shows ending scene 
     */
    public void checkGameEnds(){
        if(gameEngine.getGameState().isGameOver()){
            mainMenu("ending");
        }
    }

    /**
     * Helper method to update location layer 
     * @param lp location JLayeredPane that needs to be updated
     * @param loc location that is being updated 
     */
    private void updateLocationLayer(JLayeredPane lp, Location loc) {
        // Removing all old components, excluding the background image
        JLabel bg = locationBackgrounds.get(loc.getName());
        for (Component c : lp.getComponents()) {
            if (c != bg) { 
                lp.remove(c);
            }
        }
    
        // Adding items again based on updated location information 
        Map<String, Item> allItems = gameEngine.getGameState().getItems();
        for (String itemName : loc.getItems()) {
            Item item = allItems.get(itemName);
            JLabel itemLabel = createObjects(item.getImagePath(), item.getDimensions(), itemName);
            lp.add(itemLabel, ITEM_LAYER);
        }
    
        // Adding characters again based on updated location information 
        Map<String, Character> allChars = gameEngine.getGameState().getCharacters();
        for (String charName : loc.getCharacters()) {
            Character character = allChars.get(charName);
            JLabel charLabel = createCharacter(character.getImagePath(), character.getDimensions(), charName);
            lp.add(charLabel, CHAR_LAYER);
        }

        // Adding updated location panel to hash map 
        locationPanels.put(loc.getName(), lp);
    }
    
    /**
     * Shows current scene. Used for transitioning between panels in cardLayout
     * @param locationName name of current location
     */
    public void showLocation(String locationName) {
        cardLayout.show(outerlayer, locationName);
    }
}
