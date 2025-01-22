package org.a2.wb_client;

import org.a2.common.RemoteOperations;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This Class act as the graphical user interface frame at client side
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public class ClientGUI extends JFrame {
    RemoteOperations wbService;
    ClientWB clientWB;

    // tool Panel
    public JButton btnRect;
    public JButton btnCircle;
    public JButton btnLine;
    public JButton btnShape;
    public JButton btnDraw;
    public JButton btnEraser;
    public JButton btnText;

    // Peer interact Panel
    public JTextArea jtaUserList;
    public JTextArea jtaChatHist;
    public JTextField jtfMessage;
    public JButton btnSend;

    // Constructor
    public ClientGUI(RemoteOperations wbService, ClientWB clientWB) throws RemoteException {
        this.clientWB = clientWB;
        this.wbService = wbService;

        // Initialize whiteboard content
        clientWB.shapeList = wbService.getShapes();
        clientWB.lineList = wbService.getDraws();
        clientWB.textList = wbService.getTexts();

        setTitle("Whiteboard System: " + JoinWhiteboard.username + " (User)"); // title of the window
        setSize(800, 650); // window size
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);  // 不执行任何标准关闭操作

        // customize window closing event
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Double check exit operation
                int confirmed = JOptionPane.showConfirmDialog(ClientGUI.this,
                        "Are you sure to leave whiteboard? ", "Confirm leave :)",
                        JOptionPane.YES_NO_OPTION);

                // process exit
                if (confirmed == JOptionPane.YES_OPTION) {
                    try {
                        wbService.quit(JoinWhiteboard.username);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    dispose(); // release resource
                    System.exit(0);  // End program
                }
            }
        });

        // initialize gui contents
        JPanel mainPanel = mainPanel();
        updateUserList(wbService.getUserList());
        updateChatHist(wbService.getChatHist());
        add(mainPanel);

        // set visibility
        setVisible(true);

        // add rectangle shape
        btnRect.addActionListener(e -> {
            try {
                wbService.addRect();
            } catch (RemoteException ex) {
                System.out.println("Server Terminated, service ended :)");
                System.exit(0);
            }
        });

        // add circle shape
        btnCircle.addActionListener(e -> {
            try {
                wbService.addOval();
            } catch (RemoteException ex) {
                System.out.println("Server Terminated, service ended :)");
                System.exit(0);
            }
        });

        // add Line shape
        btnLine.addActionListener(e -> {
            try {
                wbService.addLine();
            } catch (RemoteException ex) {
                System.out.println("Server Terminated, service ended :)");
                System.exit(0);
            }
        });

        // Swap to shape mode
        btnShape.addActionListener(e -> shapeMode());

        // Swap to Free draw mode
        btnDraw.addActionListener(e -> {
            drawMode();
            clientWB.drawingColor = JColorChooser.showDialog(
                    this, "Choose the color of graphics", Color.black);
        });

        // swap to eraser mode
        btnEraser.addActionListener(e -> {
            eraseMode();
            showEraserSizeDialog();
        });

        // swap to texting mode
        btnText.addActionListener(e -> textMode());

        // send message to chat box
        btnSend.addActionListener(e -> {
            String message = jtfMessage.getText();
            if (message != null) {
                message = JoinWhiteboard.username + ": " + message;
                try {
                    wbService.sendMessage(message);
                } catch (RemoteException ex) {
                    System.out.println("Server Terminated, service ended :)");
                    System.exit(0);
                }
            }
            jtfMessage.setText(null);
        });
    }

    /**
     * Show the latest list of valid users
     * @param userList a list of usernames that is currently using whiteboard
     */
    public void updateUserList(CopyOnWriteArrayList<String> userList) {
        StringBuilder users = new StringBuilder();
        for (String s : userList) {
            users.append(s).append("\n");
        }
        jtaUserList.setText(String.valueOf(users));
    }

    /**
     * Show the latest list of chat history
     * @param messageList a list of chat message
     */
    public void updateChatHist(CopyOnWriteArrayList<String> messageList) {
        StringBuilder messages = new StringBuilder();
        for (String s : messageList) {
            messages.append(s).append("\n\n");
        }
        jtaChatHist.setText(String.valueOf(messages));
    }

    /**
     * @return the main gui panel
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
        mainPanel.add(clientWB, gbc);

        JPanel rightPanel = createRightPanel();
        rightPanel.setBackground(Color.black);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 2;
        gbc.weightx = 0;
        mainPanel.add(rightPanel, gbc);

        return mainPanel;
    }

    /**
     * @return The left panel within main panel
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

        // swap mode buttons
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
        btnText = new JButton(("Text Mode"));
        JButton[] modeButtons = {btnShape, btnDraw, btnEraser, btnText};
        for (JButton btn : modeButtons) {
            btn.setMaximumSize(new Dimension(150, 40));
            btn.setMinimumSize(new Dimension(100, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            modePanel.add(btn);
            modePanel.add(Box.createRigidArea(new Dimension(0, 5)));  // add empty space
        }
        panel.add(modePanel);

        // swap shape buttons
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
     * @return The right panel within main panel
     */
    public JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel statusLabel = new JLabel("User List");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(statusLabel);

        // Used to show valid users
        jtaUserList = new JTextArea(8, 20);
        jtaUserList.setForeground(Color.WHITE);
        jtaUserList.setBackground(Color.DARK_GRAY);
        jtaUserList.setLineWrap(true);  // 启用自动换行
        jtaUserList.setWrapStyleWord(true);
        jtaUserList.setEditable(false);

        // encapsulate textarea into scrollPanel.
        JScrollPane userListScrollPane = new JScrollPane(jtaUserList);
        userListScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        userListScrollPane.setMinimumSize(new Dimension(200, 150));
        userListScrollPane.setPreferredSize(new Dimension(200, 150));
        panel.add(userListScrollPane);

        JLabel chatPart = new JLabel("Chat History");
        chatPart.setFont(new Font("Arial", Font.BOLD, 18));
        chatPart.setForeground(Color.WHITE);
        chatPart.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(chatPart);

        jtaChatHist = new JTextArea(8, 20);
        jtaChatHist.setLineWrap(true);  // 启用自动换行
        jtaChatHist.setWrapStyleWord(true);
        jtaChatHist.setEditable(false);
        jtaChatHist.setForeground(Color.WHITE);
        jtaChatHist.setBackground(Color.DARK_GRAY);
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
     * This Method call a dialog to adjust eraser size
     */
    private void showEraserSizeDialog() {
        // Dialog view
        JDialog eraserDialog = new JDialog(this, "Adjust eraser size", true);
        eraserDialog.setTitle("Adjust Eraser Size");
        eraserDialog.setSize(300, 120);
        eraserDialog.setLayout(new FlowLayout());

        // Set dialog basic features
        JSlider eraserSizeSlider = new JSlider(JSlider.HORIZONTAL, 10, 50, 10);
        eraserSizeSlider.setMajorTickSpacing(4);
        eraserSizeSlider.setMinorTickSpacing(1);
        eraserSizeSlider.setPaintTicks(true);
        eraserSizeSlider.setPaintLabels(true);

        // Confirm change
        JButton setButton = new JButton("Set Size");
        setButton.addActionListener(e -> {
            clientWB.eraserSize = eraserSizeSlider.getValue();
            eraserDialog.dispose();
        });

        eraserDialog.add(eraserSizeSlider);
        eraserDialog.add(setButton);
        eraserDialog.setLocationRelativeTo(this);  // Center the dialog
        eraserDialog.setVisible(true);
    }

    /**
     * Change whiteboard to shape Mode, disable all other function
     */
    public void shapeMode() {
        clientWB.erasing = false;
        clientWB.drawing = false;
        clientWB.texting = false;
    }

    /**
     * Change whiteboard to draw Mode, disable all other function
     */
    public void drawMode() {
        clientWB.drawing = true;
        clientWB.erasing = false;
        clientWB.texting = false;
    }

    /**
     * Change whiteboard to eraser Mode, disable all other function
     */
    public void eraseMode() {
        clientWB.drawing = false;
        clientWB.erasing = true;
        clientWB.texting = false;
    }

    /**
     * Change whiteboard to text Mode, disable all other function
     */
    public void textMode() {
        clientWB.drawing = false;
        clientWB.erasing = false;
        clientWB.texting = true;
    }
}
