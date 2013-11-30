package ui;

import canvas.Canvas;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("unused")
public class WhiteboardGUI extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private final JLabel whiteboardsLabel;
    private final JTable allBoards;
    private final DefaultTableModel tableModelWhiteboards = new DefaultTableModel(new Object[][] {}, new Object[] {""});
    // need to add +/- buttons
    private final JLabel boardNameLabel;
    private final JTextField boardName;
    private final JLabel boardEditorsLabel;
    private final JTable boardEditors;
    private final DefaultTableModel tableModelEditors = new DefaultTableModel(new Object[][] {}, new Object[] {""});
    private final JTable colorPalette; // needs to be a grid of colors eventually...
    private final JButton moreColors;
    private final JButton eraseToggle;
    private final JButton clear;
    private final Canvas canvas;

    public WhiteboardGUI() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        Insets padding = new Insets(10,10,10,10);
        
        c.insets = padding;
        
        whiteboardsLabel = new JLabel("Whiteboards");
        c.gridx = 0;
        c.gridy = 0;
        this.add(whiteboardsLabel, c);
        
        tableModelWhiteboards.addRow(new Object[] {"01 - trojanHorse"});
        tableModelWhiteboards.addRow(new Object[] {"02 - twoStrokeEngine"});
        
        allBoards = new JTable(tableModelWhiteboards);
        allBoards.setPreferredSize(new Dimension(0, 200));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        this.add(allBoards, c);
        
        boardNameLabel = new JLabel("Board Name");
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 2;
        this.add(boardNameLabel, c);
        
        boardName = new JTextField(15);
        c.gridx = 0;
        c.gridy = 3;
        this.add(boardName, c);
        
        boardEditorsLabel = new JLabel("Board Editors");
        c.gridx = 0;
        c.gridy = 4;
        this.add(boardEditorsLabel, c);
        
        tableModelEditors.addRow(new Object[] {"apollo"});
        tableModelEditors.addRow(new Object[] {"zeus"});
        tableModelEditors.addRow(new Object[] {"athena"});
        tableModelEditors.addRow(new Object[] {"juno"});
        
        boardEditors = new JTable(tableModelEditors);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 5;
        this.add(boardEditors, c);
        
        canvas = new Canvas(800,600);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 6;
        c.gridwidth = 4;
        this.add(canvas, c);
        
        
        colorPalette = new JTable(2,14);
        MatteBorder border = new MatteBorder(0,0,0,0, Color.BLACK);
        colorPalette.setBorder(border);
        
        for(int i=0; i<14; i++){
            colorPalette.getColumnModel().getColumn(i).setPreferredWidth(16);
        }
        
        c.gridx = 1;
        c.gridy = 6;
        c.gridheight = 1;
        c.gridwidth = 1;
        this.add(colorPalette, c);
        
        c.insets = new Insets(0,50,0,50);
        
        moreColors = new JButton("More Colors");
        c.gridx = 2;
        c.gridy = 6;
        this.add(moreColors, c);
        
        eraseToggle = new JButton("Erase");
        c.gridx = 3;
        c.gridy = 6;
        this.add(eraseToggle, c);
        
        clear = new JButton("Clear");
        c.gridx = 4;
        c.gridy = 6;
        this.add(clear, c);
        
        this.pack();
        
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
