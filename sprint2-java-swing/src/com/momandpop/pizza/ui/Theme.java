package com.momandpop.pizza.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * Shared design system providing custom-painted dark espresso, warm stone, and golden amber
 * Swing components matching the Mom &amp; Pop Pizzeria aesthetic.
 */
public final class Theme {

    /** Deep obsidian-espresso application backdrop (#0F0D0B). */
    public static final Color BG_APP = new Color(15, 13, 11);

    /** Elevated card top gradient (#1E1A17). */
    public static final Color BG_CARD = new Color(30, 26, 23);

    /** Elevated card bottom gradient (#161311). */
    public static final Color BG_CARD_ALT = new Color(22, 19, 17);

    /** Dark recessed input field fill (#110E0C). */
    public static final Color BG_INPUT = new Color(17, 14, 12);

    /** Top navigation header background (#14110F). */
    public static final Color BG_HEADER = new Color(20, 17, 15);

    /** Warm buttery amber accent (#FDE68A). */
    public static final Color ACCENT_AMBER = new Color(253, 230, 138);

    /** Bright amber hover highlight (#FEF08A). */
    public static final Color ACCENT_AMBER_HOVER = new Color(254, 240, 138);

    /** Deep bronze/gold accent border (#B48332). */
    public static final Color ACCENT_GOLD = new Color(180, 131, 50);

    /** Subtle warm gold card outline (#6B532B). */
    public static final Color BORDER_GOLD_SOFT = new Color(107, 83, 43);

    /** Primary warm alabaster text (#F5F5F4). */
    public static final Color TEXT_PRIMARY = new Color(245, 245, 244);

    /** Secondary muted stone text (#A8A29E). */
    public static final Color TEXT_MUTED = new Color(168, 162, 158);

    /** Dark charcoal ink used on amber buttons (#1C1917). */
    public static final Color TEXT_ON_AMBER = new Color(28, 25, 23);

    /** Stone border (#44403C). */
    public static final Color BORDER_STONE = new Color(68, 64, 60);

    /** Subtle card divider (#302B27). */
    public static final Color BORDER_SUBTLE = new Color(48, 43, 39);

    /** Emerald confirmation green (#34D399). */
    public static final Color STATUS_SUCCESS = new Color(52, 211, 153);

    /** Subtle emerald border (#10B981). */
    public static final Color BORDER_EMERALD = new Color(16, 185, 129);

    /** Deep emerald card tint (#0A2E23). */
    public static final Color BG_EMERALD_TINT = new Color(10, 46, 35);

    /** Rose error accent (#FB7185). */
    public static final Color STATUS_ERROR = new Color(251, 113, 133);

    /** Deep rose danger background (#3F111B). */
    public static final Color BG_DANGER = new Color(63, 17, 27);

    /** Deep rose hover background (#581524). */
    public static final Color BG_DANGER_HOVER = new Color(88, 21, 36);

    /** Rose border (#9F1239). */
    public static final Color BORDER_DANGER = new Color(159, 18, 57);

    /** Cream thermal paper background (#FDFBF7). */
    public static final Color RECEIPT_PAPER = new Color(253, 251, 247);

    /** Receipt dark ink (#1C1917). */
    public static final Color RECEIPT_INK = new Color(28, 25, 23);

    /** Large serif display hero font. */
    public static final Font FONT_HERO = new Font("Georgia", Font.BOLD, 32);

    /** Primary serif section title font. */
    public static final Font FONT_TITLE = new Font("Georgia", Font.BOLD, 20);

    /** Brand header serif font. */
    public static final Font FONT_BRAND = new Font("Georgia", Font.BOLD, 22);

    /** Secondary sans-serif heading font. */
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 14);

    /** Standard UI body font. */
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);

    /** Bold uppercase micro-label font. */
    public static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 11);

    /** Monospaced font for prices and receipts. */
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);

    /** Bold monospaced font for financial totals. */
    public static final Font FONT_MONO_BOLD = new Font("Monospaced", Font.BOLD, 13);

    /**
     * Private constructor to prevent instantiation.
     */
    private Theme() {
    }

    /**
     * Creates a sleek dark-espresso rounded card panel with an optional header title.
     *
     * @param title optional section header title (may be null)
     * @return styled {@link RoundedCardPanel}
     */
    public static RoundedCardPanel createCardPanel(String title) {
        return new RoundedCardPanel(null, title, BORDER_SUBTLE, false);
    }

    /**
     * Creates a rounded card panel with a subtle golden outline and optional eyebrow tag.
     *
     * @param title optional section header title (may be null)
     * @return styled {@link RoundedCardPanel}
     */
    public static RoundedCardPanel createAmberCardPanel(String title) {
        return new RoundedCardPanel(null, title, BORDER_GOLD_SOFT, false);
    }

    /**
     * Creates a rounded card panel with both a small gold eyebrow tag and a serif title.
     *
     * @param eyebrow small uppercase tag above the title
     * @param title   main section title
     * @param amber   whether to outline with warm gold border
     * @return styled {@link RoundedCardPanel}
     */
    public static RoundedCardPanel createTitledCard(String eyebrow, String title, boolean amber) {
        return new RoundedCardPanel(eyebrow, title, amber ? BORDER_GOLD_SOFT : BORDER_SUBTLE, false);
    }

    /**
     * Creates an atmospheric hero card with subtle radial hearth glow behind the text.
     *
     * @return styled hero {@link RoundedCardPanel}
     */
    public static RoundedCardPanel createHeroCardPanel() {
        return new RoundedCardPanel(null, null, BORDER_GOLD_SOFT, true);
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
        return new StyledButton(text, new Color(36, 32, 29), new Color(54, 48, 43), BORDER_STONE, TEXT_PRIMARY);
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
     * Creates a rounded {@link JTextField} with tall padding and amber focus border.
     *
     * @param columns preferred column count
     * @return styled {@link JTextField}
     */
    public static JTextField createTextField(int columns) {
        return new RoundedTextField(columns);
    }

    /**
     * Creates a rounded {@link JPasswordField} with tall padding and amber focus border.
     *
     * @param columns preferred column count
     * @return styled {@link JPasswordField}
     */
    public static JPasswordField createPasswordField(int columns) {
        return new RoundedPasswordField(columns);
    }

    /**
     * Wraps a field component underneath an uppercase muted stone label for clean modern forms.
     *
     * @param labelText uppercase field caption
     * @param field     input component
     * @return vertical stacked {@link JPanel}
     */
    public static JPanel createStackedInput(String labelText, JComponent field) {
        JPanel stack = new JPanel(new BorderLayout(0, 5));
        stack.setOpaque(false);
        stack.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = createFieldLabel(labelText);
        stack.add(lbl, BorderLayout.NORTH);
        stack.add(field, BorderLayout.CENTER);
        if (field instanceof JTextField) {
            stack.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        }
        return stack;
    }

    /**
     * Applies slim dark rounded scrollbars and removes default borders from a {@link JScrollPane}.
     *
     * @param scroll target scroll pane
     */
    public static void styleScrollPane(JScrollPane scroll) {
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));
        scroll.getViewport().setBackground(BG_INPUT);
        scroll.setBackground(BG_APP);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
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
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_SUBTLE);
        table.setSelectionBackground(new Color(68, 52, 24));
        table.setSelectionForeground(ACCENT_AMBER);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 36));
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
     * Enables high-quality font and shape anti-aliasing on a {@link Graphics2D} context.
     *
     * @param g2 target graphics context
     */
    public static void enableAntialiasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /**
     * Custom anti-aliased rounded card panel with modern header bar instead of boxy {@code TitledBorder}.
     */
    public static class RoundedCardPanel extends JPanel {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Border stroke color. */
        private final Color borderColor;

        /** Enables warm radial oven glow behind hero sections. */
        private final boolean heroGlow;

        /** Inner body panel exposed when a header is installed. */
        private JPanel bodyContainer;

        /**
         * Constructs a {@code RoundedCardPanel}.
         *
         * @param eyebrow     optional small uppercase gold label
         * @param title       optional heading text
         * @param borderColor outline stroke color
         * @param heroGlow    true to paint a subtle warm radial hearth glow
         */
        public RoundedCardPanel(String eyebrow, String title, Color borderColor, boolean heroGlow) {
            this.borderColor = borderColor;
            this.heroGlow = heroGlow;
            setOpaque(false);

            boolean hasHeader = (eyebrow != null && !eyebrow.isEmpty()) || (title != null && !title.isEmpty());
            if (hasHeader) {
                super.setLayout(new BorderLayout(0, 12));
                setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

                JPanel headerBox = new JPanel();
                headerBox.setLayout(new BoxLayout(headerBox, BoxLayout.Y_AXIS));
                headerBox.setOpaque(false);
                headerBox.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                        BorderFactory.createEmptyBorder(0, 0, 8, 0)
                ));
                if (eyebrow != null && !eyebrow.isEmpty()) {
                    JLabel eye = new JLabel(eyebrow.toUpperCase());
                    eye.setFont(new Font("Monospaced", Font.BOLD, 10));
                    eye.setForeground(ACCENT_AMBER);
                    headerBox.add(eye);
                    headerBox.add(Box.createVerticalStrut(2));
                }
                if (title != null && !title.isEmpty()) {
                    JLabel ttl = new JLabel(title);
                    ttl.setFont(FONT_SUBTITLE);
                    ttl.setForeground(TEXT_PRIMARY);
                    headerBox.add(ttl);
                }
                super.add(headerBox, BorderLayout.NORTH);

                bodyContainer = new JPanel();
                bodyContainer.setOpaque(false);
                super.add(bodyContainer, BorderLayout.CENTER);
            } else {
                setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
            }
        }

        /**
         * Returns the inner content container when a header is present, or {@code this} otherwise.
         *
         * @return content panel
         */
        public JPanel getContentPane() {
            return bodyContainer != null ? bodyContainer : this;
        }

        /**
         * Paints the smooth rounded espresso background, optional hearth glow, and crisp border.
         *
         * @param g Swing graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);
            int w = getWidth();
            int h = getHeight();

            GradientPaint gp = new GradientPaint(0, 0, BG_CARD, 0, h, BG_CARD_ALT);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 20, 20);

            if (heroGlow && w > 20 && h > 20) {
                RadialGradientPaint rgp = new RadialGradientPaint(
                        new Point2D.Float(w / 2.0f, h * 0.45f),
                        Math.max(w * 0.55f, 120f),
                        new float[]{0.0f, 1.0f},
                        new Color[]{new Color(145, 78, 22, 62), new Color(20, 17, 15, 0)}
                );
                g2.setPaint(rgp);
                g2.fillRoundRect(1, 1, w - 3, h - 3, 20, 20);
            }

            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Rounded single-line text input field with amber focus ring.
     */
    public static class RoundedTextField extends JTextField {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Tracks keyboard focus state for highlight border. */
        private boolean focused;

        /**
         * Constructs a {@code RoundedTextField}.
         *
         * @param columns preferred columns
         */
        public RoundedTextField(int columns) {
            super(columns);
            setOpaque(false);
            setFont(FONT_BODY);
            setForeground(TEXT_PRIMARY);
            setCaretColor(ACCENT_AMBER);
            setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
            setPreferredSize(new Dimension(Math.max(110, columns * 11 + 24), 38));
            setMinimumSize(new Dimension(60, 38));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
        }

        /**
         * Paints the rounded recessed background and focus highlight ring.
         *
         * @param g Swing graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(isEnabled() ? BG_INPUT : new Color(25, 22, 20));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(focused ? ACCENT_AMBER : BORDER_STONE);
            g2.setStroke(new BasicStroke(focused ? 1.5f : 1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Rounded password input field with amber focus ring.
     */
    public static class RoundedPasswordField extends JPasswordField {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Tracks keyboard focus state for highlight border. */
        private boolean focused;

        /**
         * Constructs a {@code RoundedPasswordField}.
         *
         * @param columns preferred columns
         */
        public RoundedPasswordField(int columns) {
            super(columns);
            setOpaque(false);
            setFont(FONT_BODY);
            setForeground(TEXT_PRIMARY);
            setCaretColor(ACCENT_AMBER);
            setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
            setPreferredSize(new Dimension(Math.max(110, columns * 11 + 24), 38));
            setMinimumSize(new Dimension(60, 38));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
        }

        /**
         * Paints the rounded recessed background and focus highlight ring.
         *
         * @param g Swing graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(BG_INPUT);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(focused ? ACCENT_AMBER : BORDER_STONE);
            g2.setStroke(new BasicStroke(focused ? 1.5f : 1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
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
            setMargin(new Insets(9, 16, 9, 16));
            setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
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
            enableAntialiasing(g2);
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
     * Custom-painted {@link JToggleButton} styled as a tactile dark pill that glows golden amber when selected.
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
            setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
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
            enableAntialiasing(g2);
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
     * Full-width horizontal toggle row displaying item name on the left and price modifier on the right,
     * highlighting with a colored border when selected (matching Figure 8 topping rows).
     */
    public static class MenuRowToggle extends JToggleButton {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Display name of the topping. */
        private final String itemName;

        /** Price modifier label rendered on the right. */
        private final String priceText;

        /** Active border highlight color (emerald for veggies, rose/amber for meats). */
        private final Color activeBorderColor;

        /** Active subtle background fill color. */
        private final Color activeBgColor;

        /** Tracks hover rollover state. */
        private boolean hovered;

        /**
         * Constructs a {@code MenuRowToggle}.
         *
         * @param itemName          topping name
         * @param priceText         formatted price label (e.g. "+$1.75")
         * @param activeBorderColor border highlight when selected
         * @param activeBgColor     subtle fill tint when selected
         */
        public MenuRowToggle(String itemName, String priceText, Color activeBorderColor, Color activeBgColor) {
            super("");
            this.itemName = itemName;
            this.priceText = priceText;
            this.activeBorderColor = activeBorderColor;
            this.activeBgColor = activeBgColor;
            setActionCommand(itemName);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setPreferredSize(new Dimension(220, 38));
            setMinimumSize(new Dimension(180, 36));
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
         * Paints the horizontal card container, left name, and right monospace price text.
         *
         * @param g Swing graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);
            int w = getWidth();
            int h = getHeight();
            boolean sel = isSelected();

            g2.setColor(sel ? activeBgColor : (hovered ? new Color(36, 31, 28) : BG_INPUT));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);

            g2.setColor(sel ? activeBorderColor : BORDER_SUBTLE);
            g2.setStroke(new BasicStroke(sel ? 1.6f : 1.0f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            int textY = (h + g2.getFontMetrics(FONT_BODY).getAscent() - g2.getFontMetrics(FONT_BODY).getDescent()) / 2;
            g2.setFont(sel ? new Font("SansSerif", Font.BOLD, 12) : new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(sel ? TEXT_PRIMARY : new Color(214, 211, 209));
            String prefix = sel ? "✓ " : "";
            g2.drawString(prefix + itemName, 12, textY);

            g2.setFont(FONT_MONO_BOLD);
            g2.setColor(sel ? ACCENT_AMBER : TEXT_MUTED);
            int priceWidth = g2.getFontMetrics().stringWidth(priceText);
            g2.drawString(priceText, w - priceWidth - 12, textY);
            g2.dispose();
        }
    }

    /**
     * Slim dark-stone scrollbar UI eliminating retro Metal arrow buttons.
     */
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        /**
         * Constructs the {@code ModernScrollBarUI}.
         */
        ModernScrollBarUI() {
        }

        /**
         * Configures dark track and stone thumb colors.
         */
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(75, 68, 63);
            this.trackColor = BG_APP;
        }

        /**
         * Returns an invisible zero-sized decrease button.
         *
         * @param orientation scrollbar orientation
         * @return empty button
         */
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        /**
         * Returns an invisible zero-sized increase button.
         *
         * @param orientation scrollbar orientation
         * @return empty button
         */
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        /**
         * Paints a slim rounded scrollbar thumb.
         *
         * @param g           graphics context
         * @param c           scrollbar component
         * @param thumbBounds thumb rectangle
         */
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);
            g2.setColor(isDragging ? ACCENT_AMBER : thumbColor);
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1,
                    thumbBounds.width - 2, thumbBounds.height - 2, 8, 8);
            g2.dispose();
        }

        /**
         * Creates an empty zero-size button to hide scrollbar arrows.
         *
         * @return zero-dimension {@link JButton}
         */
        private JButton zeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
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
