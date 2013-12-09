package client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import data.WhiteLine;

public class ClientView extends JPanel {

    public final int Y_SIZE = 600, X_SIZE = 800;
    private Image buffer;

    /**
     * Constructs the ClientView object with dimensions specified by the final
     * X_SIZE and Y_SIZE fields.
     */
    public ClientView() {
        this.setPreferredSize(new Dimension(X_SIZE, Y_SIZE));
        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent arg0) {
            }

            @Override
            public void mouseMoved(MouseEvent arg0) {
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }
        });
    }

    /**
     * Method called by Swing internally to paint from the buffer to the screen.
     */
    @Override
    public void paintComponent(Graphics g) {
        if (buffer == null) {
            buffer = createImage(X_SIZE, Y_SIZE);
            clear();
        }
        g.drawImage(buffer, 0, 0, null);
    }

    /**
     * Repaints the ClientView onscreen.
     */
    public void push() {
        this.repaint();
    }

    /**
     * Make the drawing buffer entirely white.
     */
    public void clear() {
        final Graphics2D g = (Graphics2D) buffer.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Draws a line as specified by the input to the buffer. Does not repaint
     * the component on screen. Call push() to repaint.
     * 
     * @param line
     *            WhiteLine containing information for the line to be drawn.
     */
    public void drawLine(WhiteLine line) {
        Graphics2D g = (Graphics2D) buffer.getGraphics();

        g.setStroke(line.getThickness());
        g.setColor(line.getColor());
        g.drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
    }
}
