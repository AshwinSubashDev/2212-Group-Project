/**
 * ExamineOverlayPanel is an overlay used to show a focused "examine" view for a single item.
 * It displays a preview on the left and detailed text / properties on the right, and
 * exposes callbacks for rotate / inspect / use / close actions back to the controller.
 *
 * 
 *
 * @author Rian K
 */
package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Main overlay panel that hosts the examine UI.
 */
public class ExamineOverlayPanel extends JPanel {

    /**
     * Small value object representing a labelled property shown on the right-hand side.
     * Each property can optionally be highlighted to draw extra attention in the UI.
     */
    public static class ExamineProperty {
        /**
         * Creates a new examine property.
         *
         * @param l the label to show (e.g., "Type", "Rarity")
         * @param v the value associated with the label
         * @param h whether this property should be drawn in a highlighted style
         */
        public final String label, value; public final boolean highlight;
        public ExamineProperty(String l, String v, boolean h) { label = l; value = v; highlight = h; }
    }

    /**
     * Listener interface used by the controller to react to user actions in the overlay.
     */
    public interface Listener {
        /**
         * Called when the player asks to rotate the examined item.
         */
        void onRotate();

        /**
         * Called when the player wants to inspect additional details for the item.
         */
        void onInspect();

        /**
         * Called when the player chooses to use the examined item.
         */
        void onUse();

        /**
         * Called when the overlay should be closed and control returns to the main game.
         */
        void onClose();
    }

    /**
     * Simple visual preview component drawn inside the left-hand frame.
     * By default it renders a subtle grid with a key icon.
     */
    public static class KeyPreviewPanel extends JPanel {
        /**
         * Creates the preview panel and configures its base appearance.
         */
        public KeyPreviewPanel() { setOpaque(true); setBackground(new Color(0x020617)); }

        /**
         * Custom paint routine that draws the grid background and centered key icon.
         *
         * @param g the graphics context to draw with
         */
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth(), h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0x94A3B8)); g2.setComposite(AlphaComposite.SrcOver.derive(0.25f));
            int s = 8;
            for (int x = 0; x < w; x += s) g2.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += s) g2.drawLine(0, y, w, y);
            g2.setComposite(AlphaComposite.SrcOver);
            String key = "🗝️";
            g2.setFont(g2.getFont().deriveFont(Math.min(w, h) * 0.4f));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(key), th = fm.getAscent();
            int x = (w - tw) / 2;
            int y = (h + th) / 2 - fm.getDescent();
            g2.setColor(new Color(0xFBBF24));
            g2.drawString(key, x, y);
            g2.dispose();
        }
    }

    /**
     * Creates a new examine overlay for the given item and content.
     *
     * @param itemName       the display name of the item being examined
     * @param previewContent the component to show in the left preview frame
     * @param shortDesc      a short description of the item
     * @param lore           optional lore / flavour text shown in a scrollable area
     * @param props          additional labelled properties to show in a grid
     * @param listener       callback listener for user actions in this overlay
     */
    public ExamineOverlayPanel(
            String itemName,
            JComponent previewContent,
            String shortDesc,
            String lore,
            List<ExamineProperty> props,
            Listener listener
    ) {
        setLayout(new BorderLayout());
        setBackground(new Color(0x020617));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(0x020617));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xFBBF24)),
                new EmptyBorder(16, 16, 16, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        JPanel left = buildLeft(previewContent, listener);
        JPanel right = buildRight(itemName, shortDesc, lore, props);

        gbc.gridx = 0; gbc.weightx = 0.9; gbc.insets = new Insets(0,0,0,0);
        card.add(left, gbc);
        gbc.gridx = 1; gbc.weightx = 1.1; gbc.insets = new Insets(0,16,0,0);
        card.add(right, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        JButton close = new JButton("×");
        close.addActionListener(e -> { if (listener != null) listener.onClose(); });
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRight.setOpaque(false); topRight.add(close);
        wrapper.add(topRight, BorderLayout.NORTH);
        wrapper.add(card, BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
    }

    /**
     * Builds the left-hand side of the overlay, including the preview frame and action buttons.
     *
     * @param preview  the preview component to embed
     * @param listener the listener that will receive button callbacks
     * @return a configured panel for the left column
     */
    private JPanel buildLeft(JComponent preview, Listener listener) {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JPanel frame = new JPanel(new BorderLayout());
        frame.setBackground(new Color(0x020617));
        frame.setBorder(BorderFactory.createLineBorder(new Color(0xFBBF24)));
        frame.setPreferredSize(new Dimension(260, 260));
        frame.add(preview, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttons.setOpaque(false);
        JButton rotate = new JButton("Rotate");
        JButton inspect = new JButton("Inspect details");
        JButton use = new JButton("Use item");
        rotate.addActionListener(e -> { if (listener != null) listener.onRotate(); });
        inspect.addActionListener(e -> { if (listener != null) listener.onInspect(); });
        use.addActionListener(e -> { if (listener != null) listener.onUse(); });
        buttons.add(rotate); buttons.add(inspect); buttons.add(use);

        JLabel tip = new JLabel("While examining, the main game is paused until this overlay is closed.");
        tip.setForeground(new Color(0x9CA3AF));
        tip.setFont(tip.getFont().deriveFont(11f));

        col.add(frame);
        col.add(Box.createVerticalStrut(8));
        col.add(buttons);
        col.add(Box.createVerticalStrut(4));
        col.add(tip);
        return col;
    }

    /**
     * Builds the right-hand side of the overlay, including title, description, lore and properties.
     *
     * @param itemName  the item name to display as a heading
     * @param shortDesc a short description of the item
     * @param lore      optional lore text; may be {@code null} or empty
     * @param props     list of properties to render in a grid; may be empty
     * @return a configured panel for the right column
     */
    private JPanel buildRight(String itemName, String shortDesc, String lore, List<ExamineProperty> props) {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("EXAMINE");
        label.setForeground(new Color(0xFBBF24));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 10f));
        JLabel name = new JLabel(itemName);
        name.setForeground(Color.WHITE);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 18f));

        JTextArea shortArea = new JTextArea(shortDesc);
        shortArea.setWrapStyleWord(true);
        shortArea.setLineWrap(true);
        shortArea.setEditable(false);
        shortArea.setBackground(new Color(0x020617));
        shortArea.setForeground(new Color(0xE5E7EB));
        shortArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        col.add(label);
        col.add(name);
        col.add(Box.createVerticalStrut(8));
        col.add(shortArea);
        col.add(Box.createVerticalStrut(8));

        if (lore != null && !lore.isEmpty()) {
            JLabel loreLbl = new JLabel("Lore Notes");
            loreLbl.setForeground(new Color(0xFBBF24));
            loreLbl.setFont(loreLbl.getFont().deriveFont(Font.BOLD, 10f));
            JTextArea loreArea = new JTextArea(lore);
            loreArea.setWrapStyleWord(true);
            loreArea.setLineWrap(true);
            loreArea.setEditable(false);
            loreArea.setBackground(new Color(0x020617));
            loreArea.setForeground(new Color(0xE5E7EB));
            loreArea.setBorder(new EmptyBorder(8, 8, 8, 8));
            JScrollPane loreScroll = new JScrollPane(loreArea);
            loreScroll.setPreferredSize(new Dimension(0, 120));
            loreScroll.setBorder(BorderFactory.createEmptyBorder());
            col.add(loreLbl);
            col.add(Box.createVerticalStrut(4));
            col.add(loreScroll);
            col.add(Box.createVerticalStrut(8));
        }

        if (props != null && !props.isEmpty()) {
            JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
            grid.setOpaque(false);
            for (ExamineProperty p : props) {
                JPanel tile = new JPanel();
                tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
                tile.setBorder(new EmptyBorder(8, 8, 8, 8));
                if (p.highlight) {
                    tile.setBackground(new Color(0x1D4ED8));
                    tile.setOpaque(true);
                } else {
                    tile.setOpaque(false);
                }
                JLabel pl = new JLabel(p.label);
                pl.setForeground(new Color(0x9CA3AF));
                pl.setFont(pl.getFont().deriveFont(10f));
                JLabel pv = new JLabel(p.value);
                pv.setForeground(Color.WHITE);
                pv.setFont(pv.getFont().deriveFont(Font.BOLD, 12f));
                tile.add(pl); tile.add(pv);
                grid.add(tile);
            }
            col.add(grid);
        }

        return col;
    }
}
