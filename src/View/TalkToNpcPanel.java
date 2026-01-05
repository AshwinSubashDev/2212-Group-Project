/**
 * TalkToNpcPanel implements a simple branching dialogue UI for talking to an NPC.
 * It shows the NPC name, their current line, and a list of choices the player can click.
 *
 * 
 *
 * @author Rian K
 */
package View;

import model.GameState;
import model.Inventory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;

/**
 * Swing panel that manages the conversation flow with one NPC using a small dialogue graph.
 */
public class TalkToNpcPanel extends JPanel {

    private final GameState gameState;
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private String nodeId = "initial";

    private JLabel locLbl, turnLbl, npcNameLbl, msgLbl;
    private JTextArea npcText;
    private JPanel choicesPanel;
    private JCheckBox gossipChk, mapChk;

    /**
     * Small enum describing what extra side-effect a choice should trigger.
     */
    private enum Act { NONE, CLOSE, USE_ITEM }

    /**
     * Represents a single dialogue choice the player can pick.
     */
    private static class Choice {
        /**
         * Creates a new choice.
         *
         * @param t    text shown on the button
         * @param n    id of the next node to jump to (may be {@code null} for no jump)
         * @param req  optional required inventory item; if missing, the choice is disabled
         * @param show optional item name that will be "shown" to the NPC
         * @param a    extra action to perform when this choice is picked
         */
        final String text, nextId, requiredItem, itemToShow;
        final Act act;
        Choice(String t, String n, String req, String show, Act a) {
            text = t; nextId = n; requiredItem = req; itemToShow = show; act = a;
        }
    }

    /**
     * Represents a single node in the dialogue graph: the NPC line and its available choices.
     */
    private static class Node {
        /**
         * Creates a new dialogue node.
         *
         * @param id    unique identifier for this node
         * @param npc   name of the NPC speaking
         * @param text  line of dialogue to show
         * @param cs    list of choices reachable from this node
         */
        final String id, npc, text;
        final java.util.List<Choice> choices;
        Node(String id, String npc, String text, java.util.List<Choice> cs) {
            this.id = id; this.npc = npc; this.text = text; this.choices = cs;
        }
    }

    /**
     * Creates the talk-to-NPC panel and initialises its dialogue nodes and UI.
     *
     * @param gameState the shared game state used to read / update turns and inventory
     */
    public TalkToNpcPanel(GameState gameState) {
        this.gameState = gameState;
        initNodes();
        initUi();
        refresh();
    }

    /**
     * Populates the small dialogue graph used by this panel.
     * Nodes and choices are hard-coded here for simplicity.
     */
    private void initNodes() {
        nodes.put("initial", new Node(
                "initial", "Prof. Rivera",
                "\"I might help you, but what do you need?\"",
                Arrays.asList(
                        new Choice("Ask about the missing USB drive.", "ask-usb", null, null, Act.NONE),
                        new Choice("Show item: Campus Map.", "show-map", null, "Campus Map", Act.USE_ITEM),
                        new Choice("Persuade (needs: 'Campus Gossip' note).", "persuade", "Campus Gossip", null, Act.NONE),
                        new Choice("Goodbye.", null, null, null, Act.CLOSE)
                )
        ));

        nodes.put("ask-usb", new Node(
                "ask-usb", "Prof. Rivera",
                "\"The USB drive? I saw someone near the library with it yesterday. Check the computer lab.\"",
                Arrays.asList(
                        new Choice("Thank them and continue.", "initial", null, null, Act.NONE),
                        new Choice("Press for more details.", "press-usb", null, null, Act.NONE)
                )
        ));

        nodes.put("press-usb", new Node(
                "press-usb", "Prof. Rivera",
                "\"Listen, I have office hours. Come back then and maybe I will remember more.\"",
                Collections.singletonList(
                        new Choice("Fine, I will come back later.", "initial", null, null, Act.NONE)
                )
        ));

        nodes.put("show-map", new Node(
                "show-map", "Prof. Rivera",
                "\"Ah, you have the campus map. See this mark? That's where I last saw the student with the USB.\"",
                Arrays.asList(
                        new Choice("Note the location and continue.", "initial", null, null, Act.NONE),
                        new Choice("Ask about the marked location.", "ask-location", null, null, Act.NONE)
                )
        ));

        nodes.put("ask-location", new Node(
                "ask-location", "Prof. Rivera",
                "\"It's an old study room behind the engineering labs. Not many students know about it.\"",
                Arrays.asList(
                        new Choice("Thank them and leave.", null, null, null, Act.CLOSE),
                        new Choice("Ask why they were there.", "why-there", null, null, Act.NONE)
                )
        ));

        nodes.put("why-there", new Node(
                "why-there", "Prof. Rivera",
                "\"Faculty sometimes use it for quiet work. But lately, I have seen students sneaking in.\"",
                Collections.singletonList(
                        new Choice("This might be important.", "initial", null, null, Act.NONE)
                )
        ));

        nodes.put("persuade", new Node(
                "persuade", "Prof. Rivera",
                "\"Oh, you know about that? Alright, I will tell you more. The student who took it is in your dorm. Room 237.\"",
                Arrays.asList(
                        new Choice("This is very helpful, thank you!", "initial", null, null, Act.NONE),
                        new Choice("I will check it out.", null, null, null, Act.CLOSE)
                )
        ));
    }

    /**
     * Builds the overall layout of the panel and attaches it to Swing containers.
     */
    private void initUi() {
        setLayout(new BorderLayout());
        setBackground(new Color(0xE5E7EB));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildScene(), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(buildActionBar(), BorderLayout.NORTH);
        bottom.add(buildDebug(), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Builds the coloured top bar showing the location, turn count and NPC name.
     *
     * @return the configured top-bar component
     */
    private JComponent buildTopBar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(new Color(0x4F46E5));
        p.setBorder(new EmptyBorder(8, 16, 8, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        JLabel pin = new JLabel("📍"); pin.setForeground(Color.WHITE);
        locLbl = new JLabel(); locLbl.setForeground(Color.WHITE);
        locLbl.setFont(locLbl.getFont().deriveFont(Font.BOLD));
        left.add(pin); left.add(new JLabel("Location:")); left.add(locLbl);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        turnLbl = new JLabel();
        turnLbl.setForeground(Color.WHITE);
        right.add(turnLbl);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    /**
     * Builds the central scene area containing the card with NPC text and choices.
     *
     * @return the configured scene component
     */
    private JComponent buildScene() {
        JPanel scene = new JPanel(new GridBagLayout());
        scene.setBackground(new Color(0xBFDBFE));
        scene.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 2, new Color(0x020617)));
        scene.add(buildCard());
        return scene;
    }

    /**
     * Builds the inner card that shows the NPC's current line and the list of choices.
     *
     * @return a configured card panel
     */
    private JPanel buildCard() {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setOpaque(true);
        card.setBackground(new Color(0x111827));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel portrait = new JLabel("🎓");
        portrait.setFont(portrait.getFont().deriveFont(42f));
        JLabel nameLabel = new JLabel("Talking to:");
        nameLabel.setForeground(new Color(0x9CA3AF));
        nameLabel.setFont(nameLabel.getFont().deriveFont(10f));
        npcNameLbl = new JLabel();
        npcNameLbl.setForeground(Color.WHITE);
        npcNameLbl.setFont(npcNameLbl.getFont().deriveFont(Font.BOLD, 18f));
        left.add(portrait);
        left.add(Box.createVerticalStrut(8));
        left.add(nameLabel);
        left.add(npcNameLbl);

        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setOpaque(false);
        npcText = new JTextArea();
        npcText.setWrapStyleWord(true);
        npcText.setLineWrap(true);
        npcText.setEditable(false);
        npcText.setBackground(new Color(0x111827));
        npcText.setForeground(new Color(0xE5E7EB));
        npcText.setBorder(new EmptyBorder(8, 8, 8, 8));
        choicesPanel = new JPanel();
        choicesPanel.setOpaque(false);
        choicesPanel.setLayout(new BoxLayout(choicesPanel, BoxLayout.Y_AXIS));

        msgLbl = new JLabel(" ");
        msgLbl.setForeground(new Color(0xB91C1C));

        JPanel rightWrap = new JPanel(new BorderLayout());
        rightWrap.setOpaque(false);
        rightWrap.add(right, BorderLayout.CENTER);
        rightWrap.add(msgLbl, BorderLayout.SOUTH);

        right.add(npcText, BorderLayout.CENTER);
        right.add(choicesPanel, BorderLayout.SOUTH);

        card.add(left, BorderLayout.WEST);
        card.add(rightWrap, BorderLayout.CENTER);
        return card;
    }

    /**
     * Builds a small auxiliary action bar that sits above the panel (Look, Talk, Use).
     *
     * @return the configured action bar component
     */
    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setBackground(new Color(0x020617));
        bar.setBorder(BorderFactory.createMatteBorder(2, 2, 0, 2, new Color(0x020617)));
        JLabel lbl = new JLabel("Actions:"); lbl.setForeground(new Color(0xFBBF24));
        bar.add(lbl);

        bar.add(makeActionBtn("Look"));
        bar.add(new JLabel("•"));
        JButton talk = makeActionBtn("Talk");
        talk.addActionListener(e -> { nodeId = "initial"; setVisible(true); refresh(); });
        bar.add(talk);
        bar.add(new JLabel("•"));
        bar.add(makeActionBtn("Use"));
        bar.add(new JLabel("•"));
        bar.add(makeActionBtn("Leave"));

        return bar;
    }

    /**
     * Convenience helper for creating small, styled buttons in the action bar.
     *
     * @param text the text to place on the button
     * @return the configured button
     */
    private JButton makeActionBtn(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(4, 8, 4, 8));
        return b;
    }

    /**
     * Builds a small debug panel that lets the player toggle whether certain items are owned.
     *
     * @return the configured debug component
     */
    private JComponent buildDebug() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(8, 16, 12, 16));

        JLabel title = new JLabel("Debug: Inventory Flags");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 11f));
        title.setForeground(new Color(0x111827));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        gossipChk = new JCheckBox("Has 'Campus Gossip' note");
        mapChk = new JCheckBox("Has Campus Map");
        gossipChk.setOpaque(false); mapChk.setOpaque(false);

        gossipChk.addActionListener(e -> { toggleItem("Campus Gossip", gossipChk.isSelected()); refresh(); });
        mapChk.addActionListener(e -> { toggleItem("Campus Map", mapChk.isSelected()); refresh(); });

        row.add(gossipChk); row.add(mapChk);

        JLabel hint = new JLabel("Toggle items to enable/disable the Persuade option.");
        hint.setFont(hint.getFont().deriveFont(11f));

        p.add(title); p.add(row); p.add(hint);
        return p;
    }

    /**
     * Refreshes the UI labels and choice buttons to match the current dialogue node
     * and inventory contents.
     */
    private void refresh() {
        locLbl.setText(gameState.getCurrentLocationName());
        turnLbl.setText("Turns: " + gameState.getTurnsTaken());
        gossipChk.setSelected(hasItem("Campus Gossip"));
        mapChk.setSelected(hasItem("Campus Map"));

        Node n = nodes.get(nodeId);
        if (n == null) return;

        npcNameLbl.setText(n.npc);
        npcText.setText(n.text);
        msgLbl.setText(" ");

        choicesPanel.removeAll();
        for (Choice c : n.choices) {
            JButton btn = new JButton(c.text);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            boolean disabled = c.requiredItem != null && !hasItem(c.requiredItem);
            btn.setEnabled(!disabled);
            btn.addActionListener(e -> handleChoice(c));
            choicesPanel.add(btn);
        }
        revalidate(); repaint();
    }

    /**
     * Handles a choice being clicked: updates turn count, checks requirements,
     * applies any special action and moves to the next node if specified.
     *
     * @param c the chosen dialogue option
     */
    private void handleChoice(Choice c) {
        gameState.setTurnsTaken(gameState.getTurnsTaken() + 1);
        turnLbl.setText("Turns: " + gameState.getTurnsTaken());
        msgLbl.setText(" ");

        if (c.requiredItem != null && !hasItem(c.requiredItem)) {
            msgLbl.setText("You need " + c.requiredItem + " to do that.");
            return;
        }

        if (c.act == Act.CLOSE) { setVisible(false); return; }

        if (c.act == Act.USE_ITEM && c.itemToShow != null) {
            Node n = nodes.get(nodeId);
            String npc = n != null ? n.npc : "them";
            msgLbl.setText("You show " + c.itemToShow + " to " + npc + ".");
        }

        if (c.nextId != null && nodes.containsKey(c.nextId)) {
            nodeId = c.nextId;
            refresh();
        }
    }

    /**
     * Helper that checks if the player currently owns an item with the given name.
     *
     * @param name the display name of the item to check
     * @return {@code true} if the item is present in the inventory; {@code false} otherwise
     */
    private boolean hasItem(String name) {
        Inventory inv = gameState.getInventory();
        return inv != null && inv.getItemNames() != null && inv.getItemNames().contains(name);
    }

    /**
     * Adds or removes an item from the inventory list based on a toggle.
     *
     * @param name       the item name to add/remove
     * @param shouldHave {@code true} to ensure the item is present, {@code false} to remove it
     */
    private void toggleItem(String name, boolean shouldHave) {
        Inventory inv = gameState.getInventory();
        if (inv == null) return;
        java.util.List<String> list = inv.getItemNames();
        if (list == null) return;
        if (shouldHave) {
            if (!list.contains(name)) list.add(name);
        } else {
            list.remove(name);
        }
    }
}
