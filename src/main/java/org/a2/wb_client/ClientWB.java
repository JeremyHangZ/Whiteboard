package org.a2.wb_client;

import org.a2.common.RemoteOperations;
import org.a2.common.WbLine;
import org.a2.common.WbShape;
import org.a2.common.WbText;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RectangularShape;
import java.rmi.RemoteException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This Class contains all whiteboard definition and operations at client side
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public class ClientWB extends JPanel implements MouseListener, MouseMotionListener {
    // define RMI service
    RemoteOperations wbService;

    // List of whiteboard components
    CopyOnWriteArrayList<WbShape> shapeList = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<WbLine> lineList = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<WbText> textList = new CopyOnWriteArrayList<>();

    // All whiteboard features
    JPopupMenu shapePopup;
    JPopupMenu textPopup;
    Boolean drawing = false;
    Boolean erasing = false;
    Boolean texting = false;
    int startX,startY; // start position of free draw
    Boolean dragging = false;
    Color drawingColor = Color.BLACK; // default free draw color
    Ellipse2D eraser;
    int eraserSize = 20; // default eraser size

    // current target selected by user
    WbShape target;
    WbText textTarget;

    // constructor
    public ClientWB(RemoteOperations wbService) {
        this.wbService = wbService;

        // add popup components and use when needed
        shapePopup();
        textPopup();

        // handle mouse operations
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        for (WbShape s : shapeList) {
            s.drawShape(g2d);
        }

        for (WbLine l : lineList){
            l.drawLine(g2d);
        }

        for (WbText t : textList){
            t.drawText(g2d);
        }
    }

    /**
     * Define Popup component when right-click a shape
     */
    private void shapePopup(){
        shapePopup = new JPopupMenu();
        JMenuItem shapeColor = new JMenuItem("Border Color");
        JMenuItem fillColor = new JMenuItem("Fill Color");
        JMenuItem textColor = new JMenuItem("Text Color");
        JMenuItem deleteItem = new JMenuItem("Delete Shape");

        shapeColor.addActionListener(e -> addShapeColor());
        fillColor.addActionListener(e -> addFillColor());
        textColor.addActionListener(e -> addTextColor());
        deleteItem.addActionListener(e -> deleteShape());

        shapePopup.add(shapeColor);
        shapePopup.add(fillColor);
        shapePopup.add(textColor);
        shapePopup.add(deleteItem);
    }

    /**
     * Define Popup options when right-click a text
     */
    private void textPopup(){
        textPopup = new JPopupMenu();
        JMenuItem setText = new JMenuItem("Set text");
        JMenuItem setColor = new JMenuItem("Set color");
        JMenuItem deleteText = new JMenuItem("Delete text");

        setText.addActionListener(e -> setTextContent());
        setColor.addActionListener(e -> setTextColor());
        deleteText.addActionListener(e -> deleteTextItem());

        textPopup.add(setText);
        textPopup.add(setColor);
        textPopup.add(deleteText);
    }

    /**
     * Call server service to remove a text from whiteboard
     */
    private void deleteTextItem() {
        try {
            wbService.deleteText(textTarget);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Call server service to set a color to specified text
     */
    private void setTextColor() {
        try {
            wbService.setTextColor2(textTarget,
                    JColorChooser.showDialog(this, "Fill the shape with color:", Color.black));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Call server service to set a text with new content
     */
    private void setTextContent() {
        String text = JOptionPane.showInputDialog(
                this, "Enter text:", textTarget.text);
        if (text == null){
            return;
        }
        try {
            wbService.setTextContent(textTarget, text);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Call server service to remove a shape from whiteboard
     */
    private void deleteShape() {
        try {
            wbService.deleteShape(target);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Call server service to change the color of text within a shape
     */
    private void addTextColor() {
        try {
            wbService.setTextColor(target,
                    JColorChooser.showDialog(this, "Set the text color:", Color.black));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Call server service to fill a shape with specified color
     */
    private void addFillColor() {
        try {
            wbService.setFillColor(target,
                    JColorChooser.showDialog(this, "Fill the shape with color:", Color.black));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Call server service to change a shape's broder color
     */
    private void addShapeColor() {
        try {
            wbService.setShapeColor(target,
                    JColorChooser.showDialog(this, "Set the border color:", Color.black));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to find a shape target according to mouse click event
     * @param x x-value of mouse click
     * @param y y-value of mouse click
     * @return the shape if click within it
     */
    public WbShape findTarget(int x, int y) {
        target = null;
        for (WbShape s : shapeList) {
            if (s.shape instanceof RectangularShape){
                s.selected = s.shape.contains(x, y);
            } else if (s.shape instanceof Line2D) {
                s.selected = s.isClickNearLine((Line2D) s.shape, x, y);
            }
            if (s.selected){
                target = s;
                break;
            }
        }
        return target;
    }

    /**
     * This method is used to find a text target according to mouse click event
     * @param x x-value of mouse click
     * @param y y-value of mouse click
     * @return the text if click within it
     */
    public WbText findTextTarget(int x, int y) {
        textTarget = null;
        for (WbText t : textList) {
            if (t.contain(x,y)){
                textTarget = t;
                break;
            }
        }
        return textTarget;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Shape Mode
        if (!drawing && !erasing && !texting) {

            // check if any shape is selected
            target = findTarget(e.getX(),e.getY());

            // If right click on shape, show popup options
            if (target != null && SwingUtilities.isRightMouseButton(e)){
                shapePopup.show(this, e.getX(), e.getY());

            // Double click to insert text into shape
            } else if (target != null && e.getClickCount() == 2) {
                String text = JOptionPane.showInputDialog(this, "Enter text for rectangle:", target.text);
                try {
                    wbService.setText(target,text);
                    repaint();
                } catch (RemoteException ex) {
                    System.out.println("Server Terminated, service ended :)");
                    System.exit(0);
                }
            }

        // Texting Mode
        } else if (texting) {

            // check if any text is selected
            textTarget = findTextTarget(e.getX(),e.getY());

            // If right click on text, show popup options
            if (textTarget != null && SwingUtilities.isRightMouseButton(e)){
                textPopup.show(this, e.getX(), e.getY());

            // Click on empty space to inserting text
            }else if (textTarget == null){
                String text = JOptionPane.showInputDialog(
                        this, "Enter text:", null);
                if (text == null){
                    return;
                }

                try {
                    wbService.addText(new WbText(text,e.getX(),e.getY(),Color.BLACK));
                } catch (RemoteException ex) {
                    System.out.println("Server Terminated, service ended :)");
                    System.exit(0);
                }
            }

        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // Shape Mode
        if (! drawing && ! erasing && !texting) {
            // Ask server to monitor mouse operation
            try {
                wbService.pressMouse("shape", e);
                repaint();
            } catch (RemoteException ex) {
                System.out.println("Server Terminated, service ended :)");
                System.exit(0);
            }

        // Draw Mode
        } else if (drawing) {
            startX = x;
            startY = y;
            dragging = true;
        // Text Mode
        }else if (texting){
            try {
                wbService.pressMouse("text",e);
                repaint();
            } catch (RemoteException ex) {
                System.out.println("Server Terminated, service ended :)");
                System.exit(0);
            }
        // Eraser Mode
        }else {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Simulate release actions of server
        try {
            wbService.releaseMouse(e);
            repaint();
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Shape Mode
        if (! drawing && ! erasing && !texting) {
            // Client ask server to perform drag operation
            try {
                wbService.dragMouse("shape", e);
                repaint();
            } catch (RemoteException ex) {
                System.out.println("Server Terminated, service ended :)");
                System.exit(0);
            }

        // Draw Mode
        } else if (drawing){
            if (dragging) {
                int x = e.getX();
                int y = e.getY();

                // Add free draw line to whiteboard
                try {
                    wbService.addDraw(new WbLine(
                            new Point(startX, startY),
                            new Point(x, y),
                            drawingColor,0));

                } catch (RemoteException ex) {
                    System.out.println("Server Terminated, service ended :)");
                    System.exit(0);
                }

                // Update latest mouse position for next free draw line
                startX = x;
                startY = y;
                repaint();
            }

        // Text Mode
        } else if (texting) {
            // Ask server to move text's position
            try {
                wbService.dragMouse("text", e);
                repaint();
            } catch (RemoteException ex) {
                System.out.println("Server Terminated, service ended :)");
                System.exit(0);
            }

        // Erase Mode
        } else {
            if (dragging) {
                // update eraser's position
                eraser = new Ellipse2D.Double(
                        e.getX() - (double) eraserSize / 2,
                        e.getY() - (double) eraserSize / 2,
                        eraserSize,
                        eraserSize);

                // Ask server to remove all free draw lines within eraser
                try {
                    wbService.eraseDraw(eraser);
                } catch (RemoteException ex) {
                    System.out.println("Server Terminated, service ended :)");
                    System.exit(0);
                }
                repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }


    @Override
    public void mouseExited(MouseEvent e) {

    }
}

