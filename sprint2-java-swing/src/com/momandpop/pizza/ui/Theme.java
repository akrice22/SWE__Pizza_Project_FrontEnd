package com.momandpop.pizza.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Shared visual theme constants and custom-painted Swing components matching the
 * Mom &amp; Pop Pizzeria dark stone, espresso, and warm amber aesthetic.
 */
public final class Theme {

    /** Deep espresso background (#12100E). */
    public static final Color BG_APP = new Color(18, 16, 14);

    /** Elevated card top gradient (#211D1A). */
    public static final Color BG_CARD = new Color(33, 29, 26);

    /** Elevated card bottom gradient (#181512). */
    public static final Color BG_CARD_ALT = new Color(24, 21, 18);

    /** Dark recessed input background (#13100E). */
    public static final Color BG_INPUT = new Color(19, 16, 14);

    /** Top header espresso background (#191512). */
    public static final Color BG_HEADER = new Color(25, 21, 18);

    /** Warm golden amber accent (#FDE68A). */
    public static final Color ACCENT_AMBER = new Color(253, 230, 138);

    /** Bright hover amber accent (#FEF08A). */
    public static final Color ACCENT_AMBER_HOVER = new Color(254, 240, 138);

    /** Deeper gold border accent (#D97706). */
    public static final Color ACCENT_GOLD = new Color(217, 119, 6);

    /** Primary light stone text (#F5F5F4). */
    public static final Color TEXT_PRIMARY = new Color(245, 245, 244);

    /** Secondary muted stone text (#A8A29E). */
    public static final Color TEXT_MUTED = new Color(168, 162, 158);

    /** Dark ink text used on amber buttons (#1C1917). */
    public static final Color TEXT_ON_AMBER = new Color(28, 25, 23);

    /** Warm stone border (#44403C). */
    public static final Color BORDER_STONE = new Color(68, 64, 60);

    /** Subtle divider border (#302B27). */
    public static final Color BORDER_SUBTLE = new Color(48, 43, 39);

    /** Emerald success accent (#34D399). */
    public static final Color STATUS_SUCCESS = new Color(52, 211, 153);

    /** Deep emerald card tint (#064E3B). */
    public static final Color BG_SUCCESS_TINT = new Color(10, 48, 36);

    /** Rose error accent (#FB7185). */
    public static final Color STATUS_ERROR = new Color(251, 113, 133);

    /** Deep rose danger background (#3F111B). */
    public static final Color BG_DANGER = new Color(63, 17, 27);

    /** Deep rose hover background (#581524). */
    public static final Color BG_DANGER_HOVER = new Color(88, 21, 36);

    /** Rose danger border (#9F1239). */
    public static final Color BORDER_DANGER = new Color(159, 18, 57);

    /** Receipt paper cream background (#FDFBF7). */
    public static final Color RECEIPT_PAPER = new Color(253, 251, 247);

    /** Receipt dark ink text (#1C1917). */
    public static final Color RECEIPT_INK = new Color(28, 25, 23);

    /** Display serif heading font. */
    public static final Font FONT_HERO = new Font("Serif", Font.BOLD, 28);

    /** Primary serif section title font. */
    public static final Font FONT_TITLE = new Font("Serif", Font.BOLD, 20);

    /** Secondary sans-serif heading font. */
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 15);

    /** Standard UI body font. */
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);

    /** Bold uppercase label font. */
    public static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 11);

    /** Monospaced font for prices and receipt lines. */
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);

    /** Bold monospaced font for prices and KPI totals. */
    public static final Font FONT_MONO_BOLD = new Font("Monospaced", Font.BOLD, 13);

    /**
     * Private constructor to prevent instantiation.
     */
    private Theme() {
    }

    /**
     * Creates a custom anti-aliased rounded card panel with optional header title.
     *
     * @param title optional section header title (may be null)
     * @return styled {@link RoundedCardPanel}
     */
    public static RoundedCardPanel createCardPanel(String title) {
        return new RoundedCardPanel(title, BORDER_STONE);
    }

    /**
     * Creates a rounded card panel with a golden amber highlight border.
     *
     * @param title optional section header title
     * @return styled {@link RoundedCardPanel}
     */
    public static RoundedCardPanel createAmberCardPanel(String title) {
        return new RoundedCardPanel(title, new Color(140, 112, 58));
    }

    /**
     * Creates a high-emphasis amber primary button with hover highlight.
     *
     * @param text button caption
     * @return styled {@link JButton}
     */
    public static JButton createPrimaryButton(String text) {
        return new StyledButton(text, ACCENT_AMBER, ACCENT_AMBER_HOVER, ACCENT_GOLD, TEXT_ON_AMBER);
    }

    /**
     * Creates a secondary dark-stone button with subtle border and hover effect.
     *
     * @param text button caption
     * @return styled {@link JButton}
     */
    public static JButton createSecondaryButton(String text) {
        return new StyledButton(text, new Color(41, 37, 36), new Color(58, 52, 48), BORDER_STONE, TEXT_PRIMARY);
    }

    /**
     * Creates a rose-tinted danger or lock button matching the management terminal controls.
     *
     * @param text button caption
     * @return styled {@link JButton}
     */
    public static JButton createDangerButton(String text) {
        return new StyledButton(text, BG_DANGER, BG_DANGER_HOVER, BORDER_DANGER, STATUS_ERROR);
    }

    /**
     * Creates a sleek selectable toggle pill button that glows amber when selected.
     *
     * @param text button caption
     * @return styled {@link TogglePillButton}
     */
    public static TogglePillButton createTogglePill(String text) {
        return new TogglePillButton(text, false);
    }

    /**
     * Creates a dark-themed {@link JTextField} with amber focus border highlighting.
     *
     * @param columns preferred column count
     * @return styled {@link JTextField}
     */
    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        styleInputComponent(field);
        return field;
    }

    /**
     * Creates a dark-themed {@link JPasswordField} with amber focus border highlighting.
     *
     * @param columns preferred column count
     * @return styled {@link JPasswordField}
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        styleInputComponent(field);
        return field;
    }

    /**
     * Applies consistent dark background, padding, and focus-ring border to text inputs.
     *
     * @param field input text component
     */
    private static void styleInputComponent(JTextField field) {
        field.setFont(FONT_BODY);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_AMBER);
        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_STONE, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        );
        Border focused = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_AMBER, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        );
        field.setBorder(normal);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(focused);
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(normal);
            }
        });
    }

    /**
     * Styles a {@link JComboBox} to match the dark espresso theme.
     *
     * @param combo target combo box
     */
    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(FONT_BODY);
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_STONE, 1));
    }

    /**
     * Styles a {@link JScrollPane} with subtle stone border and dark viewport background.
     *
     * @param scroll target scroll pane
     */
    public static void styleScrollPane(JScrollPane scroll) {
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));
        scroll.getViewport().setBackground(BG_INPUT);
        scroll.setBackground(BG_APP);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
    }

    /**
     * Applies alternating dark-stone row styling and amber headers to a {@link JTable}.
     *
     * @param table target table component
     */
    public static void styleTable(JTable table) {
        table.setBackground(BG_INPUT);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_SUBTLE);
        table.setSelectionBackground(new Color(74, 58, 28));
        table.setSelectionForeground(ACCENT_AMBER);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 34));
        header.setDefaultRenderer(new DarkHeaderRenderer());

        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    /**
     * Creates a muted uppercase field label.
     *
     * @param text caption text
     * @return styled {@link JLabel}
     */
    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    /**
     * Formats a monetary amount in USD as {@code $X.XX}.
     *
     * @param amount value in dollars
     * @return formatted currency string
     */
    public static String formatCurrency(double amount) {
        return String.format("$%.2f", amount);
    }

    /**
     * Custom anti-aliased rounded card panel with subtle gradient fill and optional title bar.
     */
    public static class RoundedCardPanel extends JPanel {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Border highlight color. */
        private final Color borderColor;

        /**
         * Constructs a {@code RoundedCardPanel}.
         *
         * @param title       optional uppercase title (may be null)
         * @param borderColor card outline color
         */
        public RoundedCardPanel(String title, Color borderColor) {
            this.borderColor = borderColor;
            setOpaque(false);
            if (title != null && !title.isEmpty()) {
                Border empty = BorderFactory.createEmptyBorder(14, 16, 14, 16);
                Border titled = BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(4, 6, 4, 6),
                        title.toUpperCase(),
                        0,
                        0,
                        FONT_LABEL,
                        ACCENT_AMBER
                );
                setBorder(BorderFactory.createCompoundBorder(titled, empty));
            } else {
                setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
            }
        }

        /**
         * Paints the anti-aliased dark espresso gradient background and rounded border.
         *
         * @param g Swing graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, BG_CARD, 0, h, BG_CARD_ALT);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Custom-painted rounded {@link JButton} supporting dark/amber styling and rollover states.
     */
    public static class StyledButton extends JButton {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Normal background fill color. */
        private final Color normalBg;

        /** Hover rollover background fill color. */
        private final Color hoverBg;

        /** Outline stroke color. */
        private final Color borderCol;

        /** Tracks mouse hover state. */
        private boolean hovered;

        /**
         * Constructs a custom {@code StyledButton}.
         *
         * @param text      button caption
         * @param normalBg  resting fill color
         * @param hoverBg   rollover fill color
         * @param borderCol border stroke color
         * @param fg        label foreground color
         */
        public StyledButton(String text, Color normalBg, Color hoverBg, Color borderCol, Color fg) {
            super(text);
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.borderCol = borderCol;
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setForeground(fg);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setMargin(new Insets(8, 16, 8, 16));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        /**
         * Paints the anti-aliased rounded button capsule and border.
         *
         * @param g Swing graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color fill = !isEnabled() ? BG_INPUT : (hovered ? hoverBg : normalBg);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(borderCol);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Custom-painted {@link JToggleButton} styled as a tactile dark pill that highlights golden amber when selected.
     */
    public static class TogglePillButton extends JToggleButton {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Tracks hover rollover state. */
        private boolean hovered;

        /**
         * Constructs a {@code TogglePillButton}.
         *
         * @param text     button label
         * @param selected initial selection state
         */
        public TogglePillButton(String text, boolean selected) {
            super(text, selected);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        /**
         * Paints the pill background, golden selection state, and foreground text color.
         *
         * @param g Swing graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (isSelected()) {
                g2.setColor(hovered ? ACCENT_AMBER_HOVER : ACCENT_AMBER);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
                g2.setColor(ACCENT_GOLD);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
                setForeground(TEXT_ON_AMBER);
            } else {
                g2.setColor(hovered ? new Color(41, 37, 36) : BG_INPUT);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
                g2.setColor(BORDER_STONE);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
                setForeground(TEXT_PRIMARY);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Table header cell renderer matching dark stone headers with amber uppercase captions.
     */
    private static class DarkHeaderRenderer extends DefaultTableCellRenderer {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs the {@code DarkHeaderRenderer}.
         */
        DarkHeaderRenderer() {
        }

        /**
         * Renders each table column header cell with dark background and amber text.
         *
         * @param table      source table
         * @param value      header value
         * @param isSelected selection flag
         * @param hasFocus   focus flag
         * @param row        row index
         * @param column     column index
         * @return styled component
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(BG_HEADER);
            setForeground(ACCENT_AMBER);
            setFont(FONT_LABEL);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_STONE),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            return this;
        }
    }

    /**
     * Table body cell renderer applying alternating espresso row stripes and clean padding.
     */
    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs the {@code AlternatingRowRenderer}.
         */
        AlternatingRowRenderer() {
        }

        /**
         * Renders table body cells with alternating dark rows.
         *
         * @param table      source table
         * @param value      cell value
         * @param isSelected selection flag
         * @param hasFocus   focus flag
         * @param row        row index
         * @param column     column index
         * @return styled component
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? BG_INPUT : BG_CARD_ALT);
                setForeground(TEXT_PRIMARY);
            }
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            return this;
        }
    }
}
