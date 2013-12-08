package ui;

import canvas.Canvas;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class WhiteboardGUI extends JFrame implements ChangeListener {

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
    // need to add +/- buttons
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
    private final JTable colorPalette; // needs to be a grid of colors
                                       // eventually...
    private final JButton moreColors;
    private final JToggleButton eraseToggle;
    private final JButton clear;
    private final Canvas canvas;

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

    @SuppressWarnings("serial")
    public WhiteboardGUI() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        Insets padding = new Insets(10, 10, 10, 10);

        c.insets = padding;

        whiteboardsLabel = new JLabel("Whiteboards");
        c.gridx = 0;
        c.gridy = 0;
        this.add(whiteboardsLabel, c);
        tableModelWhiteboards.addRow(new Object[] { "01 - trojanHorse" });
        tableModelWhiteboards.addRow(new Object[] { "02 - twoStrokeEngine" });

        allBoards = new JTable(tableModelWhiteboards);
        allBoards.setPreferredSize(new Dimension(0, 200));
        allBoards.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        System.out.println("I can has dis board?");
                        // send message to Server
                    }

                });
        allBoards.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        this.add(allBoards, c);

        c.fill = GridBagConstraints.NONE;

        boardEditorsLabel = new JLabel("Board Editors");
        c.gridx = 0;
        c.gridy = 2;
        this.add(boardEditorsLabel, c);

        tableModelEditors.addRow(new Object[] { "apollo" });
        tableModelEditors.addRow(new Object[] { "zeus" });
        tableModelEditors.addRow(new Object[] { "athena" });
        tableModelEditors.addRow(new Object[] { "juno" });

        boardEditors = new JTable(tableModelEditors);
        boardEditors.setRowSelectionAllowed(false);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 3;
        this.add(boardEditors, c);

        sliderLabel = new JLabel("Stroke Thickness");
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 4;
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
        c.gridy = 5;
        this.add(thicknessSlider, c);

        canvas = new Canvas(800, 600);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 6;
        c.gridwidth = 4;
        this.add(canvas, c);

        DefaultTableModel model = new DefaultTableModel(2, 7) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        colorPalette = new JTable(model);
        colorPalette.setGridColor(Color.BLACK);
        colorPalette.setRowHeight(20);
        for (int i = 0; i < 7; i++) {
            colorPalette.getColumnModel().getColumn(i).setPreferredWidth(20);
            colorPalette.getColumnModel().getColumn(i)
                    .setCellRenderer(new ColorRenderer());
        }

        colorPalette.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        int row = colorPalette.getSelectedRow();
                        int col = colorPalette.getSelectedColumn();
                        drawingColor = colors[row][col];
                        canvas.setColor(drawingColor);
                        System.out.println(drawingColor);
                        // send message to server
                    }

                });

        c.gridx = 1;
        c.gridy = 6;
        c.gridheight = 1;
        c.gridwidth = 1;
        this.add(colorPalette, c);

        c.insets = new Insets(0, 50, 0, 50);

        moreColors = new JButton("More Colors");
        c.gridx = 2;
        c.gridy = 6;
        this.add(moreColors, c);

        eraseToggle = new JToggleButton("Erase");
        eraseToggle.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                if (eraseToggle.isSelected())
                    eraseMode = true;
                else
                    eraseMode = false;
                canvas.setErase(eraseMode);
            }
        });
        c.gridx = 3;
        c.gridy = 6;
        this.add(eraseToggle, c);

        clear = new JButton("Clear");
        clear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                canvas.fillWithWhite();
                // send message to Server
            }

        });
        c.gridx = 4;
        c.gridy = 6;
        this.add(clear, c);

        this.pack();

    }

    /*
     * Listen to the thickness slider.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            thickness = source.getValue();
            canvas.setThickness(thickness);
            // send message to Server
        }
    }

    /*
     * Received message adding a Whiteboard to the list.
     */
    private void addWhiteboard() {

    }

    /*
     * Received message adding a User to the list.
     */
    private void addUser() {

    }

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

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                WhiteboardGUI main = new WhiteboardGUI();

                main.setVisible(true);
            }
        });
    }

}