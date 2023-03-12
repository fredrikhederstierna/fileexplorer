package fileexplorer;

import static java.lang.System.out;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Class for reading binary file data
 */
class FileDataReader
{
  static byte[] readFile(String filename) throws IOException {
    File file = new File(filename);
    FileInputStream fis = new FileInputStream(file);
    // Temporary max file size array 1MByte
    byte[] data = new byte[1024];
    int flen = fis.read(data);
    fis.close();
    if (flen < 0) return new String("<empty file>").getBytes();
    // Limit array length to actual file size
    byte[] fdata = new byte[flen];
    System.arraycopy(data, 0, fdata, 0, flen);
    return fdata;
  }
}

/**
 * Simple panel showing a text
 */
class FileTextPanel extends JPanel
{
  private FileExplorer parent;

  private JTextArea txaFileText;

  public FileTextPanel(FileExplorer parent) {
    this.parent = parent;

    setBackground(Color.white);
    setForeground(Color.black);

    txaFileText = new JTextArea();
    txaFileText.setAlignmentX(JTextArea.LEFT_ALIGNMENT);

    add(txaFileText);
  }

  public void setText(String data) {
    this.txaFileText.setText(new String(data));
  }
}

//--------------------------------
class FileExplorer extends JFrame implements ActionListener
{
  private String root;
  private FileFilter fileFilter;
  private FileSystemModel fileSystemModel;

  private JPanel panMainUp;
  private JTextField txfRootPath;
  private JButton btnSetRoot;

  private JPanel panMainCenter;
  private JScrollPane panFiles;
  private JTreeTable treeTable;

  private JScrollPane panText;
  private FileTextPanel panFileText;

  private TitledBorder titledBorder;

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == btnSetRoot) {
      this.root = txfRootPath.getText();

      // string must end with /
      if ((this.root.length() == 0) ||
          !(String.valueOf(this.root.charAt(this.root.length() - 1)).equals(File.separator))) {
        // add ending '/'
        this.root += File.separator;
        txfRootPath.setText(this.root);
        txfRootPath.repaint();
      }

      // update tree
      fileSystemModel = new FileSystemModel(this.root, this.fileFilter);
      treeTable.setModel(fileSystemModel);
      treeTable.repaint();
      treeTable.invalidate();  
      treeTable.revalidate();         
    }
  }

  private void loadFile(String filename) {

    if (filename == null) {
      JOptionPane.showMessageDialog(null, "Load filename was null.");
      return;
    }

    byte[] data = new String("<not loaded>").getBytes();

    try {
      out.println("Load filename: " + filename);
      data = FileDataReader.readFile(filename);

    } catch (IOException ioe) {
      out.println("File IO error message: " + ioe.getMessage());
    }

    panFileText.setText(new String(data));
  }

  /**
   * Class to handle table mouse clicks
   */
  private class TableMouseAdapter extends MouseAdapter {

    public void mouseClicked(MouseEvent e) {

      // check if double-click
      if (e.getClickCount() == 2) {
        
        int row = treeTable.rowAtPoint(e.getPoint());
        int col = treeTable.columnAtPoint(e.getPoint());

        // if chosing a file in first file column
        if (col == 0) {

          Object obj = treeTable.getValueAt(row,col);
          File file = (File)obj;

          if (FileExplorer.this.fileFilter.accept(file)) {

            String filename = file.toString();
            out.println("TableMouseAdapter.mouseClicked(row=" + row + ",col=" + col + "): " + filename);

            loadFile(filename);
            
            titledBorder.setTitle(filename);

            panMainCenter.repaint();
            panMainCenter.invalidate();  
            panMainCenter.revalidate();
          }
        }
      }
    }
  }

  /**
   * Simple file explorer class
   */
  public FileExplorer() {

    this.root = File.separator;
    this.fileFilter = new FileNameExtensionFilter("Text file", "txt", "log");

    setLocation(100,100);
    setSize(800,500);
    setTitle("FileExplorer");

    addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e) { out.println("Exit FileExplorer."); System.exit(0); }} );

    this.getContentPane().setLayout(new BorderLayout());

    panMainCenter = new JPanel(new GridLayout(1,2));
    panMainCenter.setBorder(BorderFactory.createLineBorder(Color.black));
    this.add(panMainCenter, BorderLayout.CENTER);

    panMainUp = new JPanel(new BorderLayout());
    txfRootPath = new JTextField(this.root);
    panMainUp.add(txfRootPath, BorderLayout.CENTER);
    btnSetRoot = new JButton("Set root path");
    panMainUp.add(btnSetRoot, BorderLayout.EAST);
    btnSetRoot.addActionListener(this);
    panMainUp.add(new JLabel("Root path: "), BorderLayout.WEST);
    this.add(panMainUp, BorderLayout.NORTH);

    fileSystemModel = new FileSystemModel(this.root, this.fileFilter);
    treeTable = new JTreeTable(fileSystemModel);
    panFiles = new JScrollPane(treeTable);
    panMainCenter.add(panFiles);

    treeTable.addMouseListener(new TableMouseAdapter());

    panFileText = new FileTextPanel(this);
    panText = new JScrollPane(panFileText);
    Border border = BorderFactory.createLineBorder(Color.black);
    titledBorder = BorderFactory.createTitledBorder(border, "filename");
    panText.setBorder(titledBorder);
    panMainCenter.add(panText);

    setVisible(true);
  }

  /**
   * Main
   */
  public static void main(String[] args) {
    out.println("Starting FileExplorer...");
    FileExplorer fileExplorer = new FileExplorer();
  }
}
