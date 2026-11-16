package com.momandpop.pizza.ui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive mouse-drawable signature pad panel used during final checkout authorization.
 */
public class SignatureCanvasPanel extends JPanel {

    /** Collection of freehand strokes drawn by the customer. */
    private final List<List<Point>> strokes;

    /** The active stroke currently being dragged by the mouse. */
    private List<Point> currentStroke;

    /**
     * Constructs an interactive {@code SignatureCanvasPanel} ready for mouse signatures.
     */
    public SignatureCanvasPanel() {
        this.strokes = new ArrayList<>();
        setBackground(Theme.BG_INPUT);
        setPreferredSize(new Dimension(340, 85));
        setMinimumSize(new Dimension(240, 80));
        setBorder(BorderFactory.createLineBorder(Theme.BORDER_STONE, 1));
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                currentStroke = new ArrayList<>();
                currentStroke.add(e.getPoint());
                strokes.add(currentStroke);
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentStroke != null) {
                    currentStroke.add(e.getPoint());
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                currentStroke = null;
            }
        };

        addMouseListener(listener);
        addMouseMotionListener(listener);
    }

    /**
     * Clears all drawn signature strokes from the canvas.
     */
    public void clearSignature() {
        strokes.clear();
        currentStroke = null;
        repaint();
    }

    /**
     * Returns whether at least one stroke has been drawn on the canvas.
     *
     * @return {@code true} if a drawn signature exists
     */
    public boolean hasSignature() {
        return !strokes.isEmpty();
    }

    /**
     * Paints the signature line prompt and any freehand customer strokes.
     *
     * @param g Swing graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Theme.BORDER_STONE);
        int baselineY = getHeight() - 18;
        g2.drawLine(14, baselineY, getWidth() - 14, baselineY);

        if (strokes.isEmpty()) {
            g2.setColor(Theme.TEXT_MUTED);
            g2.setFont(Theme.FONT_BODY);
            g2.drawString("Draw signature here with mouse...", 16, baselineY - 6);
        } else {
            g2.setColor(Theme.ACCENT_AMBER);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (List<Point> stroke : strokes) {
                for (int i = 1; i < stroke.size(); i++) {
                    Point p1 = stroke.get(i - 1);
                    Point p2 = stroke.get(i);
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
        g2.dispose();
    }
}
