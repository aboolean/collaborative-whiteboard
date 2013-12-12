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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import data.WhiteLine;


/*
 * GUI testing strategy: Specified below is the expected behavior of the
 * client's GUI window. All behavior below was tested and confirmed to be
 * functional.
 * 
 * Run the file. Client is prompted for an IP address and port to connect to. If
 * an invalid address or port is entered, a popup informs the client of the
 * error and allows them to reenter the information.
 * 
 * Upon successful connection, the client is prompted for a username. If an
 * invalid username is submitted (either not matching the predined naming
 * convention, or already in use), the user is assigned a preset username.
 * Otherwise, the client is assigned the submitted name. This name is displayed
 * in the bottom left of the GUI window.
 * 
 * The GUI opens, displaying editing tools and the canvas. The window is not
 * resizable, and terminates the program when exited.
 * 
 * If the client tries to draw on the canvas before selecting a board, they will
 * be prompted to choose a board first. The Create New Board button prompts the
 * client for a new board name, and displays the newly created board in the
 * Whiteboards table. Duplicate or blank names are acceptable.
 * 
 * The Delete Current Board button asks the client to confirm that they want to
 * delete their currently selected board, and then deletes it from the list. The
 * boards in the Whiteboards list have their list numbers updated. Other users
 * on the same board will have their canvas cleared, and upon trying to draw
 * again, will receive a popup informing them that their current board has been
 * deleted. Clicking on a board selects that board, clearing the canvas of any
 * strokes, and reloading the canvas with the strokes on the new board.
 * 
 * The Board Editors list displays the usernames of all clients who are also
 * editing the same board. This list includes the client's own username. The
 * rows in this list are unselectable.
 * 
 * The Stroke Thickness slider enables the client to choose a stroke thickness
 * between 1 and 10. Selecting a larger number results in thicker strokes. Only
 * integer values can be selected here.
 * 
 * The color palette enables the client to choose a new color to draw with.
 * Clicking on a cell changes the current color. The More Colors button opens a
 * JColorChooser which enables the client to choose from even more colors.
 * 
 * The Erase button sets the drawing color to white. The eraser's thickness is
 * adjusted by the thickness slider. Toggling the eraser off preserves the
 * user's previously selected color.
 * 
 * The Clear button makes the canvas completely white for all users.
 * 
 * Another client's strokes appear on the canvas of the client in real-time.
 * Both clients are able to draw at the same time. If both clients draw over the
 * same area, their strokes are able to interleave with one another.
 * 
 */


@SuppressWarnings("serial")
public class WhiteboardGUI extends JFrame {

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
	private Socket acquiredSocket;
	private PrintWriter acquiredOut;
	private BufferedReader acquiredIn;
	private final ArrayList<ClientBoard> clientBoards = new ArrayList<ClientBoard>();
	private int lastSelection = 0;
	// begin with no board selected
	private ClientBoard currentBoard = null;

	public WhiteboardGUI() throws UnknownHostException, IOException {

		this.setResizable(false);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Prompt User for Address and Begin Connection
		startConnection();
		socket = acquiredSocket;
		out = acquiredOut;
		in = acquiredIn;

		// username init
		// prompt client, send to server, receive confirmation
		String username = JOptionPane.showInputDialog("Request Username:");
		if (username != null && username.matches("[A-Za-z]([A-Za-z0-9]?)+")) {
			out.println("user_req " + username);
		} else {
			out.println("user_req");
		}
		String you_are = in.readLine();
		if (you_are != null
				&& you_are.matches("you_are [A-Za-z]([A-Za-z0-9]?)+"))
			currentUser = new JLabel("Your Username: " + you_are.substring(8));
		else
			throw new RuntimeException("Unkown message received from server.");

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
		allBoards.setRowSelectionAllowed(true);
		allBoards.setPreferredScrollableViewportSize(new Dimension(0, 200));
		allBoards.setFillsViewportHeight(true);
		JScrollPane allBoardsScroll = new JScrollPane(allBoards);

		// Add a listener which sends a SEL message to the server when a board
		// in the list is selected.
		allBoards.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (!e.getValueIsAdjusting()) {
							if (allBoards.getSelectedRow() > -1) {
								lastSelection = allBoards.getSelectedRow();
								currentBoard = clientBoards.get(lastSelection);
								canvas.clear();
								out.println("select "
										+ String.valueOf(currentBoard.getID()));
							}
						}

					}

				});
		allBoards.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		this.add(allBoardsScroll, c);

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
				if (newBoard.matches("[^\n\r]+")) {
					out.println("board_req " + newBoard);
				} else {
					out.println("board_req");
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
				int option = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to delete this board?",
						"Delete Current Board", JOptionPane.YES_NO_OPTION);
				if (currentBoard != null && option == JOptionPane.OK_OPTION) {
					allBoards.clearSelection();
					out.println("del " + String.valueOf(currentBoard.getID()));
					currentBoard = null;
					canvas.clear();
				}
			}

		});
		c.gridx = 0;
		c.gridy = 3;
		this.add(deleteBoard, c);

		c.insets = padding;
		c.fill = GridBagConstraints.NONE;

		boardEditorsLabel = new JLabel("Board Editors");
		c.gridx = 0;
		c.gridy = 4;
		this.add(boardEditorsLabel, c);

		// Create table listing all Users active on this whiteboard.
		boardEditors = new JTable(tableModelEditors);
		boardEditors.setRowSelectionAllowed(false);
		boardEditors.setPreferredScrollableViewportSize(new Dimension(0, 100));
		boardEditors.setFillsViewportHeight(true);
		JScrollPane boardEditorsScroll = new JScrollPane(boardEditors);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 5;
		this.add(boardEditorsScroll, c);

		// Slider which adjusts the stroke thickness.
		sliderLabel = new JLabel("Stroke Thickness");
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 6;
		this.add(sliderLabel, c);

		thicknessSlider = new JSlider(1, 10, 3);
		thicknessSlider.setMajorTickSpacing(9);
		thicknessSlider.setMinorTickSpacing(1);
		thicknessSlider.setSnapToTicks(true);
		thicknessSlider.setPaintTicks(true);
		thicknessSlider.setPaintLabels(true);
		thicknessSlider.addChangeListener(new ChangeListener() {
		    @Override
		    public void stateChanged(ChangeEvent e) {
		        JSlider source = (JSlider) e.getSource();
		        if (!source.getValueIsAdjusting()) {
		            thickness = source.getValue();
		        }
		    }
		});
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 7;
		this.add(thicknessSlider, c);

		// Display current username.
		c.gridx = 0;
		c.gridy = 8;
		this.add(currentUser, c);

		// Create the ClientView that acts as a canvas for the client to draw
		// on.
		canvas = new ClientView();
		addDrawingController(canvas);
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 8;
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
		c.gridy = 8;
		c.insets = new Insets(10, 50, 20, 50);
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
		c.gridy = 8;
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
		c.gridy = 8;
		this.add(eraseToggle, c);

		// Button which clears the whiteboard and sends a BRD_CLR message.
		clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				out.println("board_clear " + currentBoard.getID());
			}
		});
		c.gridx = 4;
		c.gridy = 8;
		this.add(clear, c);

		SwingWorker<Void, String> incomingMessageThread = new SwingWorker<Void, String>() {

			@Override
			protected Void doInBackground() {
				try {
					for (String line = in.readLine(); line != null; line = in
							.readLine()) {
						publish(line);
					}

				} catch (IOException e) {
					// Connection Interrupted; handled in finally clause
				} finally {
					JOptionPane
							.showMessageDialog(
									new JFrame(),
									"Server connection lost. Please restart application.",
									"Connection Lost",
									JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
				return null;
			}

			// GUI changes in event dispatch thread only
			@Override
			protected void process(java.util.List<String> messages) {
				for (String msg : messages) {
					handleMessage(msg);
				}
			}
		};

		incomingMessageThread.execute();

		// sends the initialization BRD_ALL request to receive all the currently
		// active boards; prompts server to send a stream of BRD_INFO messages
		out.println("board_all");

		this.pack();

	}

	/**
	 * Called by a background thread to handle messages received from the
	 * server.
	 * 
	 * @param msg
	 *            a formatted string message from server
	 * @return a WhiteLine object or null
	 * @throws UnsupportedOperationException
	 *             unrecognized command received
	 */
	private void handleMessage(String msg) {

		// STROKE
		if (msg.matches("stroke \\d+ ([1-9]|10) \\d+ \\d+ \\d+ \\d+ \\d{1,3} \\d{1,3} \\d{1,3}")) {
			String[] t = msg.split(" ");
			int r = Integer.parseInt(t[7]), g = Integer.parseInt(t[8]), b = Integer
					.parseInt(t[9]); // RGB values
			Color color = new Color(r, g, b);
			int thickness = Integer.parseInt(t[2]);
			int x1 = Integer.parseInt(t[3]), y1 = Integer.parseInt(t[4]);
			int x2 = Integer.parseInt(t[5]), y2 = Integer.parseInt(t[6]);
			if (Integer.parseInt(t[1]) == currentBoard.getID()) {
				canvas.drawLine(new WhiteLine(x1, y1, x2, y2, color, thickness));
				canvas.push();
			}
		}
		// BRD_CLR
		else if (msg.matches("board_clear \\d+")) {
			int boardID = Integer.parseInt(msg.split(" ")[1]);
			if (boardID == currentBoard.getID()) {
				canvas.clear();
			}
		}
		// BRD_DEL
		else if (msg.matches("del \\d+")) {
			deleteWhiteboard(Integer.parseInt(msg.split(" ")[1]));
		}
		// BRD_USERS
		else if (msg.matches("board_users \\d+( [A-Za-z][A-Za-z0-9]*)*")) {
			msg = msg.substring(12); // trim command
			int id = Integer.parseInt(msg.substring(0, msg.indexOf(" ")));
			if (msg.matches("board_users \\d+")) { // no users
				if (id == currentBoard.getID())
					updateUsers("");
			} else {
				msg = msg.substring(msg.indexOf(" ") + 1); // trim id off
				if (id == currentBoard.getID())
					updateUsers(msg);
			}
		}
		// BRD_INFO
		else if (msg.matches("board \\d+( [^\r\n]+)?")) {
			if (msg.matches("board \\d+")) { // nameless
				addWhiteboard("", Integer.parseInt(msg.substring(6)));
			} else {
				// remove command and extract ID + name
				msg = msg.substring(6);
				int id = Integer.parseInt(msg.substring(0, msg.indexOf(" ")));
				String name = msg.substring(msg.indexOf(" "));
				addWhiteboard(name, id);
			}
		} else {
			throw new UnsupportedOperationException(
					"Unrecognized command received from server.");
		}
	}


	/**
	 * Called when a BRD_INFO message has been received. Adds this new board to
	 * the list of all active boards.
	 * 
	 * @param name
	 *            the name of the new board
	 * @param id_num
	 *            the unique identification number of the new board
	 */
	private void addWhiteboard(String name, int id_num) {
		ClientBoard clientBoard = new ClientBoard(name, id_num);
		clientBoards.add(clientBoard);
		int row = tableModelWhiteboards.getRowCount();
		tableModelWhiteboards.addRow(new Object[] { String.format("%03d", row)
				+ " - " + name });
	}

	/**
	 * Called when a BRD_DEL message has been received. Deletes this board from
	 * the list of active boards.
	 * 
	 * @param boardID
	 *            the identification number of the board to be deleted
	 */
	private void deleteWhiteboard(int boardID) {
		boolean success = false;
		for (int i = 0; i < clientBoards.size(); i++) {
			if (boardID == clientBoards.get(i).getID()) {
				success = clientBoards.remove(clientBoards.get(i));
				break;
			}
		}
		if (success) {
			// re-populate board list
			tableModelWhiteboards.setRowCount(0);
			for (int i = 0; i < clientBoards.size(); i++) {
				tableModelWhiteboards.addRow(new Object[] { String.format(
						"%03d", i) + " - " + clientBoards.get(i).getName() });
			}
			// currently editing deleted board
			if (currentBoard != null && currentBoard.getID() == boardID) {
				canvas.clear();
				allBoards.clearSelection();
				JOptionPane
						.showMessageDialog(
								new JFrame(),
								"The board you were editing was deleted by another\nuser. Please select another board to continue.",
								"Board Deleted", JOptionPane.ERROR_MESSAGE);
				currentBoard = null;
			}
		}
	}

	/**
	 * Called when a BRD_USERS message has been received. Updates the list of
	 * active board editors with the usernames of all active users.
	 * 
	 * @param editors
	 *            an alphabetized, space-delimited list of editors for the
	 *            current board
	 */
	private void updateUsers(String editors) {
		if (editors == null || editors.equals("")) {
			tableModelEditors.setRowCount(0);
			return;
		}
		String[] msg = editors.split(" ");
		tableModelEditors.setRowCount(0);
		for (int i = 0; i < msg.length; i++) {
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

	private void startConnection() {
		String addressInput = "0.0.0.0";
		String portInput = "55000";

		while (true) {
			// IP in format [0,255].[0,255].[0,255].[0,255]
			String addressPattern = "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
			// Port in range [49152,65535]
			String portPattern = "(49(1(5[2-9]|[6-9][0-9])|[2-9][0-9]{2})|5[0-9]{4}|6([0-4][0-9]{3}|5([0-4][0-9]{2}|5([0-2][0-9]|3[0-5]))))";

			JTextField addressField = new JTextField();
			JTextField portField = new JTextField();
			Object[] message = { "IP Address:", addressField, "Port:",
					portField };

			int buttonPressed = JOptionPane.showConfirmDialog(null, message,
					"Connect", JOptionPane.OK_CANCEL_OPTION);

			if (buttonPressed == JOptionPane.OK_OPTION) {
				addressInput = addressField.getText();
				portInput = portField.getText();
				if (addressInput == null
						|| !addressInput.matches(addressPattern)) {
					JOptionPane.showMessageDialog(new JFrame(),
							"An invalid IP address was entered.",
							"Incorrect Address", JOptionPane.ERROR_MESSAGE);
					continue;
				} else if (portInput == null || !portInput.matches(portPattern)) {
					JOptionPane
							.showMessageDialog(
									new JFrame(),
									"An invalid port number was entered. Please re-enter a number within the range [49152,65535].",
									"Incorrect Port", JOptionPane.ERROR_MESSAGE);
					continue;
				} // else correct
			} else {
				System.exit(0);
			}

			try {
				acquiredSocket = new Socket(
						InetAddress.getByName(addressInput),
						Integer.valueOf(portInput));
				acquiredIn = new BufferedReader(new InputStreamReader(
						acquiredSocket.getInputStream()));
				acquiredOut = new PrintWriter(acquiredSocket.getOutputStream(),
						true);
				break;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(new JFrame(),
						"The specified destination was unreachable.",
						"Connection Error", JOptionPane.ERROR_MESSAGE);
				continue;
			}

		}
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
			lastX = e.getX();
			lastY = e.getY();
		}

		/**
		 * When mouse moves while a button is pressed down, draw a line segment.
		 */
		public void mouseDragged(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();

			if (currentBoard == null) {
				JOptionPane.showMessageDialog(null,
						"Please select a board before drawing.");
			} else if (canvas.inBounds(x, y) && canvas.inBounds(lastX, lastY)) {
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