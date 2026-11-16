package com.momandpop.pizza.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

/**
 * Shared visual theme constants and Swing component factory helpers preserving the
 * Mom &amp; Pop Pizzeria dark stone and warm amber palette.
 */
public final class Theme {

    /** Primary application dark espresso background (#14110F). */
    public static final Color BG_APP = new Color(20, 17, 15);

    /** Elevated card background (#201C19). */
    public static final Color BG_CARD = new Color(32, 28, 25);

    /** Input field dark background (#171412). */
    public static final Color BG_INPUT = new Color(23, 20, 18);

    /** Warm amber accent color (#FDE68A). */
    public static final Color ACCENT_AMBER = new Color(253, 230, 138);

    /** Deeper gold accent for borders and badges (#D97706). */
    public static final Color ACCENT_GOLD = new Color(217, 119, 6);

    /** Primary light stone text (#F5F5F4). */
    public static final Color TEXT_PRIMARY = new Color(245, 245, 244);

    /** Secondary muted stone text (#A8A29E). */
    public static final Color TEXT_MUTED = new Color(168, 162, 158);

    /** Dark ink text used on amber buttons (#1C1917). */
    public static final Color TEXT_ON_AMBER = new Color(28, 25, 23);

    /** Subtle card border color (#44403C). */
    public static final Color BORDER_STONE = new Color(68, 64, 60);

    /** Success green accent (#34D399). */
    public static final Color STATUS_SUCCESS = new Color(52, 211, 153);

    /** Error rose accent (#FB7185). */
    public static final Color STATUS_ERROR = new Color(251, 113, 133);

    /** Receipt paper cream background (#FDFBF7). */
    public static final Color RECEIPT_PAPER = new Color(253, 251, 247);

    /** Receipt dark ink text (#1C1917). */
    public static final Color RECEIPT_INK = new Color(28, 25, 23);

    /** Main serif heading font. */
    public static final Font FONT_TITLE = new Font("Serif", Font.BOLD, 22);

    /** Section heading font. */
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 15);

    /** Standard UI body font. */
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);

    /** Bold UI label font. */
    public static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 12);

    /** Monospaced font for prices, receipt lines, and tabular numbers. */
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 13);

    /** Bold monospaced font for financial totals. */
    public static final Font FONT_MONO_BOLD = new Font("Monospaced", Font.BOLD, 14);

    /**
     * Private constructor to prevent instantiation.
     */
    private Theme() {
    }

    /**
     * Creates a styled dark-stone card panel with optional titled padding.
     *
     * @param title optional section header title (may be null)
     * @return styled {@link JPanel}
     */
    public static JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        Border line = BorderFactory.createLineBorder(BORDER_STONE, 1);
        Border padding = BorderFactory.createEmptyBorder(14, 16, 14, 16);
        if (title != null && !title.isEmpty()) {
            Border titled = BorderFactory.createTitledBorder(
                    line,
                    " " + title + " ",
                    0,
                    0,
                    FONT_LABEL,
                    ACCENT_AMBER
            );
            panel.setBorder(BorderFactory.createCompoundBorder(titled, padding));
        } else {
            panel.setBorder(BorderFactory.createCompoundBorder(line, padding));
        }
        return panel;
    }

    /**
     * Creates a high-emphasis amber primary action button.
     *
     * @param text button caption
     * @return styled {@link JButton}
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(ACCENT_AMBER);
        btn.setForeground(TEXT_ON_AMBER);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_GOLD, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a secondary stone-bordered button.
     *
     * @param text button caption
     * @return styled {@link JButton}
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_LABEL);
        btn.setBackground(new Color(41, 37, 36));
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_STONE, 1),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a styled dark-theme {@link JTextField}.
     *
     * @param columns preferred column count
     * @return styled {@link JTextField}
     */
    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT_BODY);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_AMBER);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_STONE, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    /**
     * Creates a styled dark-theme {@link JPasswordField}.
     *
     * @param columns preferred column count
     * @return styled {@link JPasswordField}
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(FONT_BODY);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_AMBER);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_STONE, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    /**
     * Creates an uppercase muted field label.
     *
     * @param text label text
     * @return styled {@link JLabel}
     */
    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    /**
     * Formats a numeric dollar value as {@code $X.XX}.
     *
     * @param amount amount in USD
     * @return formatted currency string
     */
    public static String formatCurrency(double amount) {
        return String.format("$%.2f", amount);
    }
}
