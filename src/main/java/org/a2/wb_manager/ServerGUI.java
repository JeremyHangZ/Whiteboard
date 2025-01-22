package org.a2.wb_manager;

import org.a2.common.WbShape;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/**
 * This is the main gui frame that contain all components at server side
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public class ServerGUI extends JFrame {

    ServerWB wbPanel;

    // tool Panel
    public JButton btnRect;
    public JButton btnCircle;
    public JButton btnLine;
    public JButton btnShape;
    public JButton btnDraw;
    public JButton btnEraser;
    public JButton btnText;

    // right Panel
    public static JTextArea jtaUserList;
    public JTextField jtfUsername;
    public JButton btnRemove;
    public JTextArea jtaChatHist;
    public JTextField jtfMessage;
    public JButton btnSend;

    // constructor
    public ServerGUI() {
        // create whiteboard component
        this.wbPanel = new ServerWB();

        // create file control menu for manager
        setFileMenu();

        setTitle("Whiteboard System: " + CreateWhiteboard.username); // title of the window
        setSize(800, 650); // window size
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // closing window logic
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // double check close operation
                int confirmed = JOptionPane.showConfirmDialog(ServerGUI.this,
                        "Are you sure to terminate Whiteboard System ?", "Termination :)",
                        JOptionPane.YES_NO_OPTION);

                if (confirmed == JOptionPane.YES_OPTION) {
                    ManageUser.terminateWb();
                    System.exit(0);
                }
            }
        });

        JPanel mainPanel = mainPanel();

        add(mainPanel);

        // set visibility
        setVisible(true);

        // add a rectangle
        btnRect.addActionListener(e -> {
            wbPanel.addShape(new WbShape(new Rectangle(50, 50, 50, 50)));
            repaint();
        });

        // add a circle
        btnCircle.addActionListener(e -> {
            wbPanel.addShape(new WbShape(new Ellipse2D.Double(50, 200, 50, 50)));
            repaint();
        });

        // add a line shape
        btnLine.addActionListener(e -> {
            wbPanel.addShape(new WbShape(new Line2D.Double(50, 350, 50, 450)));
            repaint();
        });

        // swap to shape mode
        btnShape.addActionListener(e -> shapeMode());

        // swap to draw mode
        btnDraw.addActionListener(e -> {
            drawMode();
            wbPanel.drawingColor = JColorChooser.showDialog(
                    this, "Choose the color of graphics", Color.black);
        });

        // swap to eraser mode
        btnEraser.addActionListener(e -> {
            eraseMode();
            showEraserSizeDialog();
        });

        // swap to text mode
        btnText.addActionListener(e -> textMode());

        // Kick out a specified client from whiteboard
        btnRemove.addActionListener(e -> {
            String userid = jtfUsername.getText();
            if (userid != null) {
                ManageUser.kickOut(userid);
                updateUserList();
            }
            jtfUsername.setText(null);
        });

        // add a message to chat box
        btnSend.addActionListener(e -> {
            String message = jtfMessage.getText();
            if (message != null) {
                ManageUser.chatHist.add(CreateWhiteboard.username + ": " + message);
                updateChatHist();
                ManageUser.updateChatHist();
            }
            jtfMessage.setText(null);
        });
    }

    /**
     * Set file control menu for manager
     */
    private void setFileMenu(){
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem newWhiteboard = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsPNG = new JMenuItem("Save As...");
        JMenuItem closeItem = new JMenuItem("Close");

        // create a new whiteboard
        newWhiteboard.addActionListener(e -> wbPanel.newBoard());

        // save all objects in whiteboard into a .dat file
        saveItem.addActionListener(e -> {
            String filepath = chooseFileLocation(this);
            if (filepath != null){
                wbPanel.saveBoard(filepath);
            }
        });

        // open a saved .dat file
        openItem.addActionListener(e -> {
            String filepath = chooseFileLocation(this);
            if (filepath != null){
                wbPanel.openBoard(filepath);
            }
        });

        // save whiteboard to png file
        saveAsPNG.addActionListener(e -> {
            String filepath = chooseFileLocation(this);
            if (filepath != null){
                wbPanel.saveBoardAsImage(wbPanel, filepath);
            }
        });

        // close whiteboard, kick all users out
        closeItem.addActionListener(e -> this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        fileMenu.add(newWhiteboard);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsPNG);
        fileMenu.add(new JSeparator()); // Visually separate the "Close" option
        fileMenu.add(closeItem);

        // Add "File" menu to the menu bar
        menuBar.add(fileMenu);

        // Configure the JFrame
        this.setJMenuBar(menuBar);
    }

    /**
     * Allow manager to choose a filepath from local machine
     * @param parent who the window belong to
     * @return a string as filepath
     */
    public String chooseFileLocation(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");

        // File instructions
        FileNameExtensionFilter filter = new FileNameExtensionFilter("DAT files (*.dat) | PNG", "dat","png");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(parent);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    /**
     * The design of the whole gui
     * @return a GUI panel
     */
    private JPanel mainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel leftPanel = toolPanel();
        leftPanel.setBackground(Color.black);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 1.0;
        mainPanel.add(leftPanel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        mainPanel.add(wbPanel, gbc);

        JPanel rightPanel = createRightPanel();
        rightPanel.setBackground(Color.black);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 2;
        gbc.weightx = 0;
        mainPanel.add(rightPanel, gbc);

        return mainPanel;
    }

    /**
     * The left panel of GUI, can swap modes or choose shape type
     * @return the tool panel
     */
    private JPanel toolPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.DARK_GRAY);

        JLabel label = new JLabel("Tools");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);

        JPanel modePanel = new JPanel();
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
        modePanel.setBackground(Color.DARK_GRAY);

        JLabel modeLabel = new JLabel("Modes");
        modeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        modeLabel.setForeground(Color.CYAN);
        modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        modePanel.add(modeLabel);

        btnShape = new JButton("Shape Mode");
        btnDraw = new JButton("Draw Mode");
        btnEraser = new JButton("Erase Mode");
        btnText = new JButton("Text Mode");
        JButton[] modeButtons = {btnShape, btnDraw, btnEraser, btnText};
        for (JButton btn : modeButtons) {
            btn.setMaximumSize(new Dimension(150, 40));
            btn.setMinimumSize(new Dimension(100, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            modePanel.add(btn);
            modePanel.add(Box.createRigidArea(new Dimension(0, 5)));  // 添加间距
        }
        panel.add(modePanel);

        JPanel shapePanel = new JPanel();
        shapePanel.setLayout(new BoxLayout(shapePanel, BoxLayout.Y_AXIS));
        shapePanel.setBackground(Color.DARK_GRAY);

        JLabel shapeLabel = new JLabel("Shapes");
        shapeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        shapeLabel.setForeground(Color.CYAN);
        shapeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        shapePanel.add(shapeLabel);

        btnRect = new JButton("Rectangle");
        btnCircle = new JButton("Oval");
        btnLine = new JButton("Line");
        JButton[] shapeButtons = {btnRect, btnCircle, btnLine};
        for (JButton btn : shapeButtons) {
            btn.setMaximumSize(new Dimension(150, 40));
            btn.setMinimumSize(new Dimension(100, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            shapePanel.add(btn);
            shapePanel.add(Box.createRigidArea(new Dimension(0, 5)));  // 添加间距
        }
        panel.add(shapePanel);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * The design of right panel of server GUI
     * @return the right panel
     */
    public JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel statusLabel = new JLabel("User List");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(statusLabel);

        jtaUserList = new JTextArea(8, 20);
        jtaUserList.setForeground(Color.WHITE);
        jtaUserList.setBackground(Color.DARK_GRAY);
        jtaUserList.setLineWrap(true);
        jtaUserList.setWrapStyleWord(true);
        jtaUserList.setEditable(false);

        JScrollPane userListScrollPane = new JScrollPane(jtaUserList);
        userListScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        userListScrollPane.setMinimumSize(new Dimension(200, 150));
        userListScrollPane.setPreferredSize(new Dimension(200, 150));
        panel.add(userListScrollPane);

        JLabel removePart = new JLabel("Enter id to remove");
        removePart.setFont(new Font("Arial", Font.BOLD, 14));
        removePart.setForeground(Color.WHITE);
        removePart.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(removePart);

        jtfUsername = new JTextField();
        jtfUsername.setBackground(Color.lightGray);
        updateUserList();
        jtfUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, jtfUsername.getPreferredSize().height));
        jtfUsername.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(jtfUsername);

        btnRemove = new JButton("Confirm Remove");
        btnRemove.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnRemove.getPreferredSize().height));
        btnRemove.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(btnRemove);

        JLabel chatPart = new JLabel("Chat History");
        chatPart.setFont(new Font("Arial", Font.BOLD, 18));
        chatPart.setForeground(Color.WHITE);
        chatPart.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(chatPart);

        jtaChatHist = new JTextArea(8, 20);
        jtaChatHist.setLineWrap(true);
        jtaChatHist.setWrapStyleWord(true);
        jtaChatHist.setEditable(false);
        jtaChatHist.setBackground(Color.DARK_GRAY);
        jtaChatHist.setForeground(Color.WHITE);

        JScrollPane chatHistScrollPane = new JScrollPane(jtaChatHist);
        chatHistScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(chatHistScrollPane);

        JLabel message = new JLabel("Enter message to send");
        message.setForeground(Color.WHITE);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(message);

        jtfMessage = new JTextField();
        jtfMessage.setBackground(Color.lightGray);
        jtfMessage.setMaximumSize(new Dimension(Integer.MAX_VALUE, jtfMessage.getPreferredSize().height));
        jtfMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(jtfMessage);

        btnSend = new JButton("send message");
        btnSend.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnSend.getPreferredSize().height));
        btnSend.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(btnSend);

        return panel;
    }

    /**
     * show the latest user list on right panel
     */
    public static void updateUserList() {
        StringBuilder users = new StringBuilder();
        for (String s : ManageUser.names) {
            users.append(s).append("\n");
        }
        jtaUserList.setText(String.valueOf(users));
    }

    /**
     * show the latest chat histories on right panel
     */
    public void updateChatHist() {
        StringBuilder messages = new StringBuilder();
        for (String s : ManageUser.chatHist) {
            messages.append(s).append("\n\n");
        }
        jtaChatHist.setText(String.valueOf(messages));
    }

    /**
     * Show request from user and decide whether share board to that user
     * @param username client id
     * @return true if permitted, false otherwise
     */
    Boolean showPermissionDialog(String username) {
        int confirmed = JOptionPane.showConfirmDialog(this,
                username + " wants to share your whiteboard.", "Login Request",
                JOptionPane.YES_NO_OPTION);

        return confirmed == JOptionPane.YES_OPTION;
    }

    /**
     * Allow manager to chooser the size of the eraser
     */
    private void showEraserSizeDialog() {
        JDialog eraserDialog = new JDialog(this, "Adjust eraser size", true);
        eraserDialog.setTitle("Adjust Eraser Size");
        eraserDialog.setSize(300, 120);
        eraserDialog.setLayout(new FlowLayout());

        JSlider eraserSizeSlider = new JSlider(JSlider.HORIZONTAL, 10, 50, 10);
        eraserSizeSlider.setMajorTickSpacing(4);
        eraserSizeSlider.setMinorTickSpacing(1);
        eraserSizeSlider.setPaintTicks(true);
        eraserSizeSlider.setPaintLabels(true);

        // confirm changes
        JButton setButton = new JButton("Set Size");
        setButton.addActionListener(e -> {
            wbPanel.eraserSize = eraserSizeSlider.getValue();
            eraserDialog.dispose();
        });

        eraserDialog.add(eraserSizeSlider);
        eraserDialog.add(setButton);
        eraserDialog.setLocationRelativeTo(this);  // Center the dialog
        eraserDialog.setVisible(true);
    }

    // swap to shape mode
    public void shapeMode() {
        wbPanel.erasing = false;
        wbPanel.drawing = false;
        wbPanel.texting = false;
    }

    // swap to draw mode
    public void drawMode() {
        wbPanel.drawing = true;
        wbPanel.erasing = false;
        wbPanel.texting = false;
    }

    // swap to eraser mode
    public void eraseMode() {
        wbPanel.drawing = false;
        wbPanel.erasing = true;
        wbPanel.texting = false;
    }

    // swap to text mode
    public void textMode() {
        wbPanel.drawing = false;
        wbPanel.erasing = false;
        wbPanel.texting = true;
    }
}
