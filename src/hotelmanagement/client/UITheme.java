package hotelmanagement.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * Modern, refined desktop design system for the Hotel Management System.
 * Focuses on high-legibility typography, clear hierarchy, balanced spacing,
 * and professional enterprise aesthetics without visual clutter or generic tropes.
 */
public final class UITheme {

    // --- Color Palette ---
    public static final Color BG_APP = new Color(0xF8, 0xFA, 0xFC);         // Slate 50
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SUBTLE = new Color(0xF1, 0xF5, 0xF9);       // Slate 100
    public static final Color BG_HOVER = new Color(0xEA, 0xEE, 0xF5);

    public static final Color BORDER = new Color(0xE2, 0xE8, 0xF0);          // Slate 200
    public static final Color BORDER_INPUT = new Color(0xCB, 0xD5, 0xE1);    // Slate 300
    public static final Color BORDER_FOCUS = new Color(0x5E, 0xAE, 0x84);    // #5eae84

    public static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);    // Slate 900
    public static final Color TEXT_SECONDARY = new Color(0x47, 0x55, 0x69);  // Slate 600
    public static final Color TEXT_MUTED = new Color(0x94, 0xA3, 0xB8);      // Slate 400

    public static final Color PRIMARY = new Color(0x5E, 0xAE, 0x84);         // #5eae84
    public static final Color PRIMARY_HOVER = new Color(0x4D, 0x98, 0x72);   // Darker shade for hover
    public static final Color PRIMARY_TEXT = Color.WHITE;

    public static final Color SUCCESS = new Color(0x05, 0x96, 0x69);         // Emerald 600
    public static final Color SUCCESS_BG = new Color(0xEC, 0xFD, 0xF5);
    public static final Color SUCCESS_TEXT = new Color(0x06, 0x5F, 0x46);

    public static final Color WARNING = new Color(0xD9, 0x77, 0x06);         // Amber 600
    public static final Color WARNING_BG = new Color(0xFF, 0xFB, 0xEB);
    public static final Color WARNING_TEXT = new Color(0x92, 0x40, 0x0E);

    public static final Color DANGER = new Color(0xDC, 0x26, 0x26);          // Red 600
    public static final Color DANGER_BG = new Color(0xFE, 0xF2, 0xF2);
    public static final Color DANGER_TEXT = new Color(0x99, 0x1B, 0x1B);

    public static final Color INFO_BG = new Color(0xEF, 0xF8, 0xF3);          // Soft #5eae84 tint
    public static final Color INFO_TEXT = new Color(0x2D, 0x6E, 0x4B);        // Deep sage tone

    // --- Fonts ---
    private static final String FONT_NAME = "Segoe UI";
    public static final Font FONT_TITLE = new Font(FONT_NAME, Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font(FONT_NAME, Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font(FONT_NAME, Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font(FONT_NAME, Font.BOLD, 13);
    public static final Font FONT_CAPTION = new Font(FONT_NAME, Font.PLAIN, 12);
    public static final Font FONT_BADGE = new Font(FONT_NAME, Font.BOLD, 11);
    public static final Font FONT_STAT_NUM = new Font(FONT_NAME, Font.BOLD, 26);

    private UITheme() {}

    /** Applies global rendering hints and UI defaults. */
    public static void init() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        UIManager.put("Panel.background", BG_APP);
        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Label.font", FONT_REGULAR);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.font", FONT_REGULAR);
        UIManager.put("PasswordField.font", FONT_REGULAR);
        UIManager.put("ComboBox.font", FONT_REGULAR);
        UIManager.put("Table.font", FONT_REGULAR);
        UIManager.put("TableHeader.font", FONT_BOLD);
        UIManager.put("TabbedPane.font", FONT_SUBHEADER);
        UIManager.put("TabbedPane.background", BG_APP);
        UIManager.put("TabbedPane.foreground", TEXT_SECONDARY);
        UIManager.put("TabbedPane.selectedForeground", PRIMARY);
    }

    /** Creates a primary action button (filled blue with smooth hover). */
    public static JButton createPrimaryButton(String text) {
        return new ModernButton(text, PRIMARY, PRIMARY_HOVER, PRIMARY_TEXT, null);
    }

    /** Creates a secondary outline button (clean white with border). */
    public static JButton createSecondaryButton(String text) {
        return new ModernButton(text, BG_CARD, BG_SUBTLE, TEXT_PRIMARY, BORDER_INPUT);
    }

    /** Creates a subtle action button (for refresh, reset, etc.). */
    public static JButton createGhostButton(String text) {
        return new ModernButton(text, BG_SUBTLE, BG_HOVER, TEXT_SECONDARY, BORDER);
    }

    /** Creates a destructive / cancel button. */
    public static JButton createDangerButton(String text) {
        return new ModernButton(text, DANGER_BG, new Color(0xFE, 0xE2, 0xE2), DANGER_TEXT, new Color(0xFE, 0xCA, 0xCA));
    }

    /** Creates a success / positive action button. */
    public static JButton createSuccessButton(String text) {
        return new ModernButton(text, SUCCESS_BG, new Color(0xD1, 0xFA, 0xE5), SUCCESS_TEXT, new Color(0xA7, 0xF3, 0xD0));
    }

    /** Creates a styled text field with rounded border and focus glow. */
    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        styleInput(field);
        return field;
    }

    public static JTextField createTextField(String initialText, int columns) {
        JTextField field = new JTextField(initialText, columns);
        styleInput(field);
        return field;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        styleInput(field);
        return field;
    }

    /** Applies uniform styling and focus animations to an input field. */
    public static void styleInput(JTextField field) {
        field.setFont(FONT_REGULAR);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_CARD);
        field.setCaretColor(PRIMARY);
        field.setMargin(new Insets(6, 10, 6, 10));

        Border normalBorder = BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_INPUT, 6, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8));
        Border focusBorder = BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_FOCUS, 6, 2),
                BorderFactory.createEmptyBorder(4, 7, 4, 7));

        field.setBorder(normalBorder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(focusBorder);
                field.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(normalBorder);
                field.repaint();
            }
        });
    }

    /** Styles a JComboBox with uniform font and padding. */
    public static <T> void styleComboBox(JComboBox<T> box) {
        box.setFont(FONT_REGULAR);
        box.setBackground(BG_CARD);
        box.setForeground(TEXT_PRIMARY);
        box.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_INPUT, 6, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
    }

    /** Creates a clean white card panel with a subtle border. */
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER, 8, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return card;
    }

    /** Applies modern table styling with custom header, row height, and badges. */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(38);
        table.setShowGrid(true);
        table.setGridColor(new Color(0xF1, 0xF5, 0xF9));
        table.setSelectionBackground(new Color(0xEF, 0xF8, 0xF3));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setBackground(BG_CARD);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(0xF8, 0xFA, 0xFC));
        header.setForeground(TEXT_SECONDARY);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFocus, r, c);
                l.setFont(FONT_BOLD);
                l.setForeground(TEXT_SECONDARY);
                l.setBackground(new Color(0xF8, 0xFA, 0xFC));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                return l;
            }
        };
        header.setDefaultRenderer(headerRenderer);

        // Smart cell renderer with zebra striping and status pills
        TableCellRenderer cellRenderer = new ModernTableCellRenderer();
        table.setDefaultRenderer(Object.class, cellRenderer);
    }

    /** Wraps a component in a styled JScrollPane. */
    public static JScrollPane createScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(new RoundedLineBorder(BORDER, 8, 1));
        sp.getViewport().setBackground(BG_CARD);
        sp.setBackground(BG_CARD);
        return sp;
    }

    // --- Custom Components ---

    /** Modern button with rounded edges, custom colors, and mouse transitions. */
    public static class ModernButton extends JButton {
        private static final long serialVersionUID = 1L;

        private final Color bgColor;
        private final Color hoverColor;
        private final Color borderColor;
        private boolean isHovered = false;

        public ModernButton(String text, Color bg, Color hover, Color textCol, Color border) {
            super(text);
            this.bgColor = bg;
            this.hoverColor = hover;
            this.borderColor = border;

            setFont(FONT_BOLD);
            setForeground(textCol);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(7, 14, 7, 14));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            Color fill = isHovered ? hoverColor : bgColor;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, width, height, 8, 8);

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Smart table cell renderer with zebra striping and clean status pills. */
    public static class ModernTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        private final JPanel badgePanel = new JPanel();
        private final JLabel badgeLabel = new JLabel();

        public ModernTableCellRenderer() {
            badgePanel.setOpaque(false);
            badgePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 6));
            badgeLabel.setFont(FONT_BADGE);
            badgePanel.add(badgeLabel);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            String text = value == null ? "" : value.toString();

            // Render status values as modern pill badges
            if (isStatusValue(text)) {
                setupBadge(text, isSelected);
                return badgePanel;
            }

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setFont(FONT_REGULAR);
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            // Numbers / currency formatting: align right for money
            if (text.startsWith("Rs. ")) {
                label.setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? BG_CARD : new Color(0xF8, 0xFA, 0xFC));
                label.setForeground(TEXT_PRIMARY);
            } else {
                label.setBackground(new Color(0xEF, 0xF8, 0xF3));
                label.setForeground(TEXT_PRIMARY);
            }

            return label;
        }

        private boolean isStatusValue(String s) {
            return s.equals("AVAILABLE") || s.equals("BOOKED") || s.equals("OCCUPIED")
                    || s.equals("MAINTENANCE") || s.equals("CONFIRMED") || s.equals("CHECKED_IN")
                    || s.equals("CHECKED_OUT") || s.equals("CANCELLED") || s.equals("PAID")
                    || s.equals("Registered") || s.equals("Walk-in");
        }

        private void setupBadge(String status, boolean isSelected) {
            Color bg;
            Color fg;

            switch (status) {
                case "AVAILABLE":
                case "CHECKED_IN":
                case "PAID":
                case "Registered":
                    bg = SUCCESS_BG;
                    fg = SUCCESS_TEXT;
                    break;
                case "BOOKED":
                case "CONFIRMED":
                    bg = INFO_BG;
                    fg = INFO_TEXT;
                    break;
                case "MAINTENANCE":
                case "OCCUPIED":
                    bg = WARNING_BG;
                    fg = WARNING_TEXT;
                    break;
                case "CANCELLED":
                    bg = DANGER_BG;
                    fg = DANGER_TEXT;
                    break;
                default:
                    bg = BG_SUBTLE;
                    fg = TEXT_SECONDARY;
                    break;
            }

            badgeLabel.setText("  " + status + "  ");
            badgeLabel.setForeground(fg);
            badgeLabel.setOpaque(true);
            badgeLabel.setBackground(bg);
            badgeLabel.setBorder(new RoundedLineBorder(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 60), 12, 1));
            badgePanel.setBackground(isSelected ? new Color(0xEF, 0xF8, 0xF3) : BG_CARD);
        }
    }

    /** Rounded line border utility. */
    public static class RoundedLineBorder implements Border {
        private final Color color;
        private final int radius;
        private final int thickness;

        public RoundedLineBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            for (int i = 0; i < thickness; i++) {
                g2.drawRoundRect(x + i, y + i, width - 1 - (i * 2), height - 1 - (i * 2), radius, radius);
            }
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 2, thickness + 4, thickness + 2, thickness + 4);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
