/**
 * InventoryPanel renders the player's inventory for the adventure game.
 * It shows a filterable list of items on the left and details / actions on the right.
 *
 * 
 *
 * @author Rian K
 */
package View;

import model.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Swing panel responsible for presenting and interacting with the player's inventory.
 */
public class InventoryPanel extends JPanel {

    /**
     * Simple model object used by the panel to represent an inventory item for display.
     */
    private static class Item {
        /**
         * Item type used for filtering and display.
         */
        enum Type { KEY_ITEM, CONSUMABLE, EQUIPMENT, MISC }

        /**
         * Creates a new item instance.
         *
         * @param id  internal identifier used by the game
         * @param n   display name shown to the player
         * @param d   description text shown in the detail pane
         * @param t   item type (for filtering)
         * @param q   starting quantity
         * @param e   whether the item starts equipped
         */
        final String id, name, desc; final Type type;
        int qty; boolean equipped;
        Item(String id, String n, String d, Type t, int q, boolean e) {
            this.id = id; name = n; desc = d; type = t; qty = q; equipped = e;
        }

        /**
         * Returns the name so that the JList renders the item nicely.
         *
         * @return the item name
         */
        public String toString() { return name; }
    }

    private final GameState gameState;
    private final List<Item> items = new ArrayList<>();
    private String filter = "All";

    private JLabel locLbl, turnLbl, countLbl, selNameLbl, selTypeLbl, msgLbl;
    private DefaultListModel<Item> listModel;
    private JList<Item> list;
    private JTextArea descArea;

    /**
     * Creates a new inventory panel bound to the given game state.
     * The panel seeds some starting items and initialises its Swing UI.
     *
     * @param gameState the shared game state backing this panel
     */
    public InventoryPanel(GameState gameState) {
        this.gameState = gameState;
        seedItems();
        initUi();
        refreshHeader();
        applyFilter();
    }

    /**
     * Adds some sample items into the local list.
     * In a full game this data would typically come from the model.
     */
    private void seedItems() {
        items.add(new Item("rusty-key", "Rusty Key",
                "An old iron key covered in rust. It looks like it belongs to a locked room on campus.",
                Item.Type.KEY_ITEM, 1, false));
        items.add(new Item("campus-map", "Campus Map",
                "A folded map of the university grounds. A few locations are circled in red ink.",
                Item.Type.KEY_ITEM, 1, false));
        items.add(new Item("energy-drink", "Energy Drink",
                "A can of highly caffeinated mystery liquid. Restores a bit of stamina.",
                Item.Type.CONSUMABLE, 2, false));
        items.add(new Item("lab-coat", "Lab Coat",
                "A white lab coat with ink stains. Smells faintly of coffee and marker ink.",
                Item.Type.EQUIPMENT, 1, true));
        items.add(new Item("sticky-note", "Sticky Note",
                "A crumpled sticky note with a half-erased phone number and \"DON'T FORGET\" scribbled on it.",
                Item.Type.MISC, 3, false));
    }

    /**
     * Builds the overall layout for the inventory panel and hooks up listeners.
     */
    private void initUi() {
        setLayout(new BorderLayout(0, 8));
        setBackground(new Color(0x020617));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    /**
     * Builds the coloured header strip showing location, turn count and item count.
     *
     * @return the configured header component
     */
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x111827));
        header.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel("🎒");
        icon.setFont(icon.getFont().deriveFont(20f));
        JLabel title = new JLabel("Inventory");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        locLbl = new JLabel();
        locLbl.setForeground(new Color(0x9CA3AF));
        locLbl.setFont(locLbl.getFont().deriveFont(11f));
        left.add(icon); left.add(title); left.add(locLbl);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        countLbl = new JLabel();
        countLbl.setForeground(new Color(0xE5E7EB));
        countLbl.setFont(countLbl.getFont().deriveFont(11f));
        turnLbl = new JLabel();
        turnLbl.setForeground(new Color(0xE5E7EB));
        turnLbl.setFont(turnLbl.getFont().deriveFont(11f));
        right.add(countLbl); right.add(turnLbl);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /**
     * Builds the main content area, which is split into list (left) and details (right).
     *
     * @return the configured main component
     */
    private JComponent buildMain() {
        JPanel main = new JPanel(new GridLayout(1, 2, 12, 0));
        main.setOpaque(false);
        main.add(buildLeft());
        main.add(buildRight());
        return main;
    }

    /**
     * Builds the left side of the panel, containing filters and the item list.
     *
     * @return the configured left component
     */
    private JComponent buildLeft() {
        JPanel col = new JPanel(new BorderLayout(0, 8));
        col.setOpaque(false);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filters.setOpaque(false);
        String[] names = {"All", "Key Item", "Consumable", "Equipment", "Misc"};
        for (String f : names) {
            JButton b = new JButton(f);
            b.setFont(b.getFont().deriveFont(10f));
            b.setFocusPainted(false);
            b.addActionListener(e -> { filter = f; applyFilter(); });
            filters.add(b);
        }

        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer((jl, value, idx, sel, focus) -> {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(true);
            p.setBorder(new EmptyBorder(6, 8, 6, 8));
            p.setBackground(sel ? new Color(0x1F2937) : new Color(0x020617));
            JLabel name = new JLabel(value.name + (value.qty > 1 ? " x" + value.qty : ""));
            name.setForeground(Color.WHITE);
            name.setFont(name.getFont().deriveFont(Font.BOLD, 12f));
            JLabel type = new JLabel(value.type.name());
            type.setForeground(new Color(0x9CA3AF));
            type.setFont(type.getFont().deriveFont(10f));
            p.add(name, BorderLayout.CENTER);
            p.add(type, BorderLayout.SOUTH);
            return p;
        });
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateDetail();
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0x111827)));

        col.add(filters, BorderLayout.NORTH);
        col.add(scroll, BorderLayout.CENTER);
        return col;
    }

    /**
     * Builds the right side of the panel, containing selected item details and action buttons.
     *
     * @return the configured right component
     */
    private JComponent buildRight() {
        JPanel col = new JPanel(new BorderLayout(0, 8));
        col.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel("Selected Item");
        lbl.setForeground(new Color(0x9CA3AF));
        lbl.setFont(lbl.getFont().deriveFont(10f));
        selNameLbl = new JLabel("No item selected");
        selNameLbl.setForeground(Color.WHITE);
        selNameLbl.setFont(selNameLbl.getFont().deriveFont(Font.BOLD, 14f));
        left.add(lbl); left.add(selNameLbl);

        selTypeLbl = new JLabel();
        selTypeLbl.setForeground(new Color(0xE5E7EB));
        selTypeLbl.setOpaque(true);
        selTypeLbl.setBackground(new Color(0x020617));
        selTypeLbl.setBorder(new EmptyBorder(4, 8, 4, 8));
        selTypeLbl.setFont(selTypeLbl.getFont().deriveFont(10f));

        header.add(left, BorderLayout.WEST);
        header.add(selTypeLbl, BorderLayout.EAST);

        descArea = new JTextArea();
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setBackground(new Color(0x020617));
        descArea.setForeground(new Color(0xE5E7EB));
        descArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(0x111827)));
        descScroll.setPreferredSize(new Dimension(0, 140));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton useBtn = new JButton("Use");
        JButton equipBtn = new JButton("Equip / Unequip");
        JButton dropBtn = new JButton("Drop");
        useBtn.addActionListener(e -> handleUse());
        equipBtn.addActionListener(e -> handleEquip());
        dropBtn.addActionListener(e -> handleDrop());
        actions.add(useBtn); actions.add(equipBtn); actions.add(dropBtn);

        msgLbl = new JLabel("Select an item to see details.");
        msgLbl.setForeground(new Color(0x9CA3AF));
        msgLbl.setFont(msgLbl.getFont().deriveFont(11f));

        col.add(header, BorderLayout.NORTH);
        col.add(descScroll, BorderLayout.CENTER);
        col.add(actions, BorderLayout.SOUTH);
        col.add(msgLbl, BorderLayout.PAGE_END);
        return col;
    }

    /**
     * Builds a small footer row with debug / hint text.
     *
     * @return the configured footer component
     */
    private JComponent buildFooter() {
        JLabel hint = new JLabel("Tip: Use, equip, or drop items to affect the investigation and your stats.");
        hint.setForeground(new Color(0x6B7280));
        hint.setFont(hint.getFont().deriveFont(11f));

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 0, 0, 0));
        p.add(hint, BorderLayout.WEST);
        return p;
    }

    /**
     * Refreshes the header labels using the current values from the game state.
     */
    private void refreshHeader() {
        locLbl.setText(" – " + gameState.getCurrentLocationName());
        turnLbl.setText("Turns: " + gameState.getTurnsTaken());
        countLbl.setText("Items: " + items.size());
    }

    /**
     * Applies the current filter to the backing item list and updates the JList model.
     */
    private void applyFilter() {
        listModel.clear();
        for (Item i : items) {
            if ("All".equals(filter)) {
                listModel.addElement(i);
            } else if ("Key Item".equals(filter) && i.type == Item.Type.KEY_ITEM) {
                listModel.addElement(i);
            } else if ("Consumable".equals(filter) && i.type == Item.Type.CONSUMABLE) {
                listModel.addElement(i);
            } else if ("Equipment".equals(filter) && i.type == Item.Type.EQUIPMENT) {
                listModel.addElement(i);
            } else if ("Misc".equals(filter) && i.type == Item.Type.MISC) {
                listModel.addElement(i);
            }
        }
        countLbl.setText("Items: " + listModel.size());
        updateDetail();
    }

    /**
     * Updates the detail pane to reflect the currently selected item in the list.
     * If nothing is selected, a neutral helper message is shown.
     */
    private void updateDetail() {
        Item i = list.getSelectedValue();
        if (i == null) {
            selNameLbl.setText("No item selected");
            selTypeLbl.setText("");
            descArea.setText("");
            msgLbl.setText("Select an item to see details.");
            return;
        }
        selNameLbl.setText(i.name);
        selTypeLbl.setText(i.type.name());
        descArea.setText(i.desc);
        msgLbl.setText(" ");
    }

    /**
     * Handles the "Use" button being pressed for the currently selected item.
     * This method updates the game state turns and shows appropriate feedback text.
     */
    private void handleUse() {
        Item i = list.getSelectedValue();
        if (i == null) {
            msgLbl.setText("Select an item to use.");
            return;
        }
        gameState.setTurnsTaken(gameState.getTurnsTaken() + 1);
        refreshHeader();
        if (i.type == Item.Type.CONSUMABLE && i.qty > 0) {
            i.qty--;
            if (i.qty == 0) {
                items.remove(i);
            }
            applyFilter();
            msgLbl.setText("You use " + i.name + ".");
        } else {
            msgLbl.setText("You cannot use that item right now.");
        }
    }

    /**
     * Handles the "Equip/Unequip" button being pressed for the current item.
     * For simplicity this just toggles a flag and updates the message label.
     */
    private void handleEquip() {
        Item i = list.getSelectedValue();
        if (i == null) {
            msgLbl.setText("Select an item to equip or unequip.");
            return;
        }
        if (i.type != Item.Type.EQUIPMENT) {
            msgLbl.setText("Only equipment items can be equipped.");
            return;
        }
        i.equipped = !i.equipped;
        msgLbl.setText(i.equipped ? "You equip " + i.name + "." : "You unequip " + i.name + ".");
        repaint();
    }

    /**
     * Handles the "Drop" action. Equipped items cannot be dropped.
     */
    private void handleDrop() {
        Item i = list.getSelectedValue();
        if (i == null) return;
        gameState.setTurnsTaken(gameState.getTurnsTaken() + 1);
        refreshHeader();
        if (i.equipped) {
            msgLbl.setText("You cannot drop an equipped item.");
            return;
        }
        items.remove(i);
        applyFilter();
        msgLbl.setText(i.name + " dropped.");
    }
}
