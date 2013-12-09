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

    private Color color = Color.BLACK;
    private java.awt.BasicStroke thickness = new BasicStroke(3);
    public final int Y_SIZE = 600, X_SIZE = 800;
    private Image buffer;
    private boolean eraseMode = false;

    public ClientView() {
        this.setPreferredSize(new Dimension(X_SIZE, Y_SIZE));
        addDrawingController();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setThickness(int n) {
        this.thickness = new BasicStroke(n);
    }

    public void setErase(boolean e) {
        this.eraseMode = e;
    }

    @Override
    public void paintComponent(Graphics g) {
        // If this is the first time paintComponent() is being called,
        // make our drawing buffer.
        if (buffer == null) {
            buffer = createImage(X_SIZE, Y_SIZE);
            clear();
        }

        // Copy the drawing buffer to the screen.
        g.drawImage(buffer, 0, 0, null);
    }
    
    /*
     * Make the drawing buffer entirely white.
     */
    public void clear() {
        final Graphics2D g = (Graphics2D) buffer.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        this.repaint();
        // send CLEAR message
    }

    private void drawLine(WhiteLine line) {
        Graphics2D g = (Graphics2D) buffer.getGraphics();

        g.setStroke(line.getThickness());
        g.setColor(line.getColor());
        g.drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());

        // IMPORTANT! every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
    }

    /*
     * Add the mouse listener that supports the user's freehand drawing.
     */
    private void addDrawingController() {
        DrawingController controller = new DrawingController();
        addMouseListener(controller);
        addMouseMotionListener(controller);
    }

    /*
     * DrawingController handles the user's freehand drawing.
     */
    private class DrawingController implements MouseListener,
            MouseMotionListener {
        // store the coordinates of the last mouse event, so we can
        // draw a line segment from that last point to the point of the next
        // mouse event.
        private int lastX, lastY;

        public void DrawingController() {

        }

        /*
         * When mouse button is pressed down, start drawing.
         */
        public void mousePressed(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
        }

        /*
         * When mouse moves while a button is pressed down, draw a line segment.
         */
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            Color strokeColor;
            int strokeThick;
            if (eraseMode) {
                strokeColor = Color.white;
                strokeThick = 10;
            } else {
                strokeColor = color;
                strokeThick = (int) thickness.getLineWidth();
            }
            WhiteLine line = new WhiteLine(lastX, lastY, x, y, strokeColor, strokeThick);
            drawLine(line);
            // send STROKE message with line
            lastX = x;
            lastY = y;
        }

        public void mouseMoved(MouseEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }
}
