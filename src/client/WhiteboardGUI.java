package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import server.WhiteboardServer;
import data.User;
import data.WhiteLine;

@SuppressWarnings("serial")
public class WhiteboardGUI extends JFrame implements ChangeListener {

    int i = 0;

    private static final long serialVersionUID = 1L;

    /*
     * GUI-related fields.
     */
    private final JLabel whiteboardsLabel;
    private final JTable allBoards;
    private final DefaultTableModel tableModelWhiteboards = new DefaultTableModel(
            new Object[][] {}, new Object[] { "" }) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JButton addBoard;
    private final JButton deleteBoard;
    private final JLabel boardEditorsLabel;
    private final JTable boardEditors;
    private final DefaultTableModel tableModelEditors = new DefaultTableModel(
            new Object[][] {}, new Object[] { "" }) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JLabel sliderLabel;
    private final JSlider thicknessSlider;
    private final JLabel currentUser;
    private final JTable colorPalette;
    private final JButton moreColors;
    private final JColorChooser colorChooser;
    private final JToggleButton eraseToggle;
    private final JButton clear;
    private final ClientView canvas;

    /*
     * Drawing-related fields.
     */
    Color[][] colors = {
            { Color.RED, new Color(255, 128, 0), Color.YELLOW,
                    new Color(128, 255, 0), Color.GREEN,
                    new Color(44, 145, 19), Color.BLACK },
            { Color.CYAN, new Color(0, 128, 255), Color.BLUE,
                    new Color(128, 0, 255), Color.PINK, Color.MAGENTA,
                    Color.GRAY } };
    private int thickness = 3;
    private Color drawingColor = Color.BLACK;
    private boolean eraseMode = false;

    /*
     * Communication-related fields.
     */
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final ArrayList<ClientBoard> clientBoards = new ArrayList<ClientBoard>();
    private int lastSelection = 0;
    // begin with no board selected
    private ClientBoard currentBoard = null;

    public WhiteboardGUI() throws UnknownHostException, IOException {

        String ipAddress = JOptionPane
                .showInputDialog("Connect to IP Address:");

        socket = new Socket(InetAddress.getByName(ipAddress), 55000);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    for (String line = in.readLine(); line != null; line = in
                            .readLine()) {
                        // for now, just print the line -- will handle eventually
                        System.out.println(line);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                }
            }
        });
        thread.start();

        // set up the layout
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        Insets padding = new Insets(10, 10, 10, 10);
        c.insets = padding;

        whiteboardsLabel = new JLabel("Whiteboards");
        c.gridx = 0;
        c.gridy = 0;
        this.add(whiteboardsLabel, c);

        // Create table containing all active boards.

        allBoards = new JTable(tableModelWhiteboards);
        allBoards.setPreferredSize(new Dimension(0, 200));

        // sends the initialization BRD_ALL request to receive all the currently
        // active boards; prompts server to send a stream of BRD_INFO messages
        out.println("board_all");

        // Add a listener which sends a SEL message to the server when a board
        // in the list is selected.
        allBoards.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if (allBoards.getSelectedRow() > 0) {
                            lastSelection = allBoards.getSelectedRow();
                        }
                        ClientBoard cb = clientBoards.get(lastSelection);
                        currentBoard = cb;
                        out.println("SEL " + cb.getID());
                    }

                });
        allBoards.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        this.add(allBoards, c);

        c.insets = new Insets(0, 10, 0, 0);
        c.fill = GridBagConstraints.NONE;

        // Button which sends a BRD_REQ message to instantiate a new whiteboard.
        addBoard = new JButton("Create New Board");
        addBoard.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // new board's name
                String newBoard = JOptionPane
                        .showInputDialog("New board name:");
                if (newBoard == null | newBoard.equals("")) {
                    out.println("board_req");
                } else if (newBoard.matches("[A-Za-z]([A-Za-z0-9]?)+")) {
                    out.println("board_req " + newBoard);
                }
            }

        });
        c.gridx = 0;
        c.gridy = 2;
        this.add(addBoard, c);

        // Button which sends a BRD_DEL message to delete the currently selected
        // board.
        deleteBoard = new JButton("Delete Current Board");
        deleteBoard.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int n = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to delete this board?",
                        "Delete Current Board", JOptionPane.YES_NO_OPTION);
                if (n == 0) {
                    out.println("board " + currentBoard.getID());
                    deleteWhiteboard("board " + currentBoard.getID());
                    currentBoard = null;
                }
            }

        });
        c.gridy = 3;
        this.add(deleteBoard, c);

        c.insets = padding;
        c.fill = GridBagConstraints.NONE;

        boardEditorsLabel = new JLabel("Board Editors");
        c.gridx = 0;
        c.gridy = 3;
        this.add(boardEditorsLabel, c);

        // Create table listing all Users active on this whiteboard.
        tableModelEditors.addRow(new Object[] { "apollo" });
        tableModelEditors.addRow(new Object[] { "zeus" });
        tableModelEditors.addRow(new Object[] { "athena" });
        tableModelEditors.addRow(new Object[] { "juno" });

        boardEditors = new JTable(tableModelEditors);
        boardEditors.setRowSelectionAllowed(false);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 4;
        this.add(boardEditors, c);

        // Slider which adjusts the stroke thickness.
        sliderLabel = new JLabel("Stroke Thickness");
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 5;
        this.add(sliderLabel, c);

        thicknessSlider = new JSlider(1, 10, 3);
        thicknessSlider.setMajorTickSpacing(9);
        thicknessSlider.setMinorTickSpacing(1);
        thicknessSlider.setSnapToTicks(true);
        thicknessSlider.setPaintTicks(true);
        thicknessSlider.setPaintLabels(true);
        thicknessSlider.addChangeListener(this);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 6;
        this.add(thicknessSlider, c);

        // prompt the client for a username, and then instantiate the User
        // object
        String username = JOptionPane.showInputDialog("Username:");
        if (username == null || username.equals("")) {
            out.println("user_req");
        } else if (username.matches("[A-Za-z]([A-Za-z0-9]?)+")) {
            out.println("user_req " + username);
        }
        currentUser = new JLabel("Your Username: " + username);

        c.gridx = 0;
        c.gridy = 7;
        this.add(currentUser, c);

        // Create the ClientView that acts as a canvas for the client to draw
        // on.
        canvas = new ClientView();
        addDrawingController(canvas);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 7;
        c.gridwidth = 4;
        this.add(canvas, c);

        // Create a grid of selectable colors.
        DefaultTableModel model = new DefaultTableModel(2, 7) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        colorPalette = new JTable(model);
        colorPalette.setGridColor(Color.BLACK);
        colorPalette.setRowHeight(20);
        colorPalette.setCellSelectionEnabled(true);
        colorPalette.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < 7; i++) {
            colorPalette.getColumnModel().getColumn(i).setPreferredWidth(20);
            colorPalette.getColumnModel().getColumn(i)
                    .setCellRenderer(new ColorRenderer());
        }

        colorPalette.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent me) {

                Point point = me.getPoint();
                JTable table = (JTable) me.getSource();
                int row = table.rowAtPoint(point);
                int col = table.columnAtPoint(point);
                drawingColor = colors[row][col];
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
            }

        });

        c.gridx = 1;
        c.gridy = 7;
        c.gridheight = 1;
        c.gridwidth = 1;
        this.add(colorPalette, c);

        c.insets = new Insets(0, 50, 0, 50);

        // Button which opens a JColorChooser dialog to select more
        // colors.
        moreColors = new JButton("More Colors");
        colorChooser = new JColorChooser();
        moreColors.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Color color = colorChooser.showDialog(null, "More Colors",
                        Color.WHITE);
                drawingColor = color;
            }

        });
        c.gridx = 2;
        c.gridy = 7;
        this.add(moreColors, c);

        // Button which toggles between drawing and erasing (drawing
        // with thick, white strokes).
        eraseToggle = new JToggleButton("Erase");
        eraseToggle.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                if (eraseToggle.isSelected())
                    eraseMode = true;
                else
                    eraseMode = false;
            }
        });
        c.gridx = 3;
        c.gridy = 7;
        this.add(eraseToggle, c);

        // Button which clears the whiteboard and sends a BRD_CLR message.
        clear = new JButton("Clear");
        clear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                canvas.clear();
                canvas.push();
                out.println("board_clear " + currentBoard.getID());
            }
        });
        c.gridx = 4;
        c.gridy = 7;
        this.add(clear, c);

        this.pack();

    }

    /**
     * Listens to the thickness slider and adjusts the thickness field
     * accordingly.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            thickness = source.getValue();
        }
    }

    /**
     * Called when a BRD_INFO message has been received. Adds this new board to
     * the list of all active boards.
     */
    private void addWhiteboard(String BRD_INFO) {
        String[] msg = BRD_INFO.split(" ");
        ClientBoard clientBoard = new ClientBoard(msg[2],
                Integer.parseInt(msg[1]));
        clientBoards.add(clientBoard);
        int row = tableModelWhiteboards.getRowCount();
        tableModelWhiteboards.addRow(new Object[] { "0"
                + String.valueOf(row + 1) + " - " + msg[2] });
    }

    /**
     * Called when a BRD_DEL message has been received. Deletes this board from
     * the list of active boards.
     */
    private void deleteWhiteboard(String BRD_DEL) {
        String[] msg = BRD_DEL.split(" ");
        int id = Integer.parseInt(msg[1]);
        boolean success = false;
        for (int i = 0; i < clientBoards.size(); i++) {
            if (id == clientBoards.get(i).getID()) {
                success = clientBoards.remove(clientBoards.get(i));
            }
        }
        if (success) {
            tableModelWhiteboards.setRowCount(0);
            int size = clientBoards.size();
            for (int i = 0; i < size; i++) {
                addWhiteboard(clientBoards.get(i).toString());
            }
        }
    }

    /**
     * Called when a BRD_USERS message has been received. Updates the list of
     * active board editors with the usernames of all active users.
     */
    private void addUsers(String BRD_USERS) {
        String[] msg = BRD_USERS.split(" ");
        tableModelEditors.setRowCount(0);
        for (int i = 2; i < msg.length; i++) {
            tableModelEditors.addRow(new Object[] { msg[i] });
        }
    }

    /**
     * Custom TableCellRenderer which colors the colorPalette JTable according
     * to the colors 2D Array.
     */
    public class ColorRenderer extends JLabel implements TableCellRenderer {

        public ColorRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object obj, boolean isSelected, boolean hasFocus, int row,
                int col) {
            setBackground(colors[row][col]);
            return this;
        }
    }

    /**
     * Add the mouse listener that supports the user's freehand drawing.
     */
    private void addDrawingController(ClientView cv) {
        DrawingController controller = new DrawingController();
        cv.addMouseListener(controller);
        cv.addMouseMotionListener(controller);
    }

    /**
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

        /**
         * When mouse button is pressed down, start drawing.
         */
        public void mousePressed(MouseEvent e) {
            if (currentBoard == null) {
                JOptionPane.showMessageDialog(null,
                        "Please select a board before drawing.");
            } else {
                lastX = e.getX();
                lastY = e.getY();
            }
        }

        /**
         * When mouse moves while a button is pressed down, draw a line segment.
         */
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            if (canvas.inBounds(x, y) && canvas.inBounds(lastX, lastY)) {
                Color strokeColor;
                int strokeThick = (int) thickness;
                if (eraseMode) {
                    strokeColor = Color.white;
                } else {
                    strokeColor = drawingColor;
                }
                WhiteLine line = new WhiteLine(lastX, lastY, x, y, strokeColor,
                        strokeThick);
                canvas.drawLine(line);
                canvas.push();
                // send STROKE message
                out.println("stroke " + currentBoard.getID() + " "
                        + String.valueOf(strokeThick) + " " + lastX + " "
                        + lastY + " " + x + " " + y + " "
                        + strokeColor.getRed() + " " + strokeColor.getGreen()
                        + " " + strokeColor.getBlue());
            }
            lastX = x;
            lastY = y;
        }

        public void mouseMoved(MouseEvent e) {
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

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                WhiteboardGUI main;
                try {
                    main = new WhiteboardGUI();
                    main.setVisible(true);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

}