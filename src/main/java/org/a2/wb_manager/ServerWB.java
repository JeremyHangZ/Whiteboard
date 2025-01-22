package org.a2.wb_manager;

import org.a2.common.ReplyReceiver;
import org.a2.common.WbLine;
import org.a2.common.WbShape;
import org.a2.common.WbText;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.io.*;
import java.rmi.RemoteException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This Class contains the full functions' definitions of a server whiteboard
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public class ServerWB extends JPanel implements MouseListener, MouseMotionListener {

    // Store all components within whiteboard
    CopyOnWriteArrayList<WbShape> shapeList = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<WbLine> lineList = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<WbText> textList = new CopyOnWriteArrayList<>();

    // Current mode
    Boolean drawing = false;
    Boolean erasing = false;
    Boolean texting = false;
    Boolean dragging = false;

    // other features
    Ellipse2D eraser;
    int eraserSize;
    int startX, startY; // mouse start position
    JPopupMenu shapePopup;
    JPopupMenu textPopup;
    Color drawingColor = Color.BLACK; // initial free draw line color

    // current manager's target
    WbShape target;
    WbText textTarget;

    // constructor
    public ServerWB() {
        shapePopup();
        textPopup();

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

        for (WbLine l : lineList) {
            l.drawLine(g2d);
        }

        for (WbText t : textList){
            t.drawText(g2d);
        }

        for (ReplyReceiver r : ManageUser.clients.values()) {
            try {
                r.updateShape(shapeList);
                r.updateLine(lineList);
                r.updateText(textList);
            } catch (RemoteException ignored) {
                ManageUser.remove(r);
            }
        }
    }

    /**
     *  Popup options when right-click on a shape
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
     * Popup options when right-click on a text
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
     * Creat a new whiteboard by clear all components in lists
     */
    void newBoard(){
        shapeList = new CopyOnWriteArrayList<>();
        lineList = new CopyOnWriteArrayList<>();
        textList = new CopyOnWriteArrayList<>();
        repaint();
    }

    /**
     * Save all components on the whiteboard to local
     * @param filepath the destination of saved file
     */
    void saveBoard(String filepath){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            oos.writeObject(shapeList);
            oos.writeObject(lineList);
            oos.writeObject(textList);
            JOptionPane.showMessageDialog(this,
                    "Successfully save whiteboard objects ", "Operation Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e){
            JOptionPane.showMessageDialog(this,
                    "Save operation failed, try again :) ", "Error", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Open a saved dat file to reload past whiteboard
     * @param filepath the source file location
     */
    void openBoard(String filepath){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
            shapeList = (CopyOnWriteArrayList<WbShape>) ois.readObject();
            lineList = (CopyOnWriteArrayList<WbLine>) ois.readObject();
            textList = (CopyOnWriteArrayList<WbText>) ois.readObject();
            repaint();
        } catch (Exception e){
            JOptionPane.showMessageDialog(this,
                    "Cannot read this file as whiteboard, try other files :) ", "Error", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Save whiteboard as a Png file
     * @param boardPanel the whiteboard to be saved
     * @param filepath the destination of saved file
     */
    public void saveBoardAsImage(JPanel boardPanel, String filepath) {
        // Create a BufferedImage which has the same size as whiteboard
        BufferedImage image = new BufferedImage(boardPanel.getWidth(), boardPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = image.createGraphics();

        // paint whiteboard components to image
        boardPanel.paint(g2);

        g2.dispose();

        // save buffered image as png
        try {
            ImageIO.write(image, "png", new File(filepath));
            JOptionPane.showMessageDialog(this,
                    "Successfully save whiteboard to png", "Operation Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Cannot save whiteboard to png, check again :)", "Error", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Add a shape to whiteboard
     * @param shape shape
     */
    public void addShape(WbShape shape) {
        shapeList.add(shape);
    }

    /**
     * change the color a specified shape
     */
    public void addShapeColor() {
        target.shapeColor = JColorChooser.showDialog
                (this, "Set the border color:", Color.black);
        repaint();
    }

    /**
     * change the fill color of specified shape
     */
    public void addFillColor() {
        target.fillColor = JColorChooser.showDialog
                (this, "Fill the shape with color:", Color.black);
        repaint();
    }

    /**
     * change the color of text within a shape
     */
    public void addTextColor() {
        target.textColor = JColorChooser.showDialog
                (this, "Set the text color:", Color.black);
        repaint();
    }

    /**
     * Delete a shape from whiteboard
     */
    public void deleteShape() {
        shapeList.remove(target);
        repaint();
    }

    /**
     * change the content of text within a shape
     */
    public void setTextContent(){
        textTarget.text = JOptionPane.showInputDialog(
                this, "Enter text:", textTarget.text);
        repaint();
    }

    /**
     * change the color of text within a shape
     */
    public void setTextColor(){
        textTarget.color = JColorChooser.showDialog
                (this, "Set the text color:", Color.black);
        repaint();
    }

    /**
     * delete a whiteboard text object
     */
    public void deleteTextItem() {
        textList.remove(textTarget);
        repaint();
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        // Shape Mode
        if (! drawing && ! erasing && ! texting) {

            // double-click a shape to insert text
            if (target != null && e.getClickCount() == 2) {
                String text = JOptionPane.showInputDialog(
                        this, "Enter text for rectangle:", target.text);
                if (text != null) {
                    target.text = text;
                    repaint();
                }

            // right-click a shape to show popup options
            } else if (target != null && SwingUtilities.isRightMouseButton(e)) {
                shapePopup.show(this, e.getX(), e.getY());
            }

        // Text Mode
        } else if (texting) {
            // right-click a shape to show popup options
            if (textTarget != null && SwingUtilities.isRightMouseButton(e)) {
                textPopup.show(this, e.getX(), e.getY());

            // Click empty space to insert new text object
            }else if (textTarget == null){
                String text = JOptionPane.showInputDialog(
                        this, "Enter text:", null);
                if (text == null){
                    return;
                }
                textList.add(new WbText(text,e.getX(),e.getY(),Color.BLACK));
                repaint();
            }
        }
    }


    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // Shape Mode
        if (!drawing && !erasing && ! texting) {
            if (target != null) {
                // check if any control point is selected
                target.selectedControl = -1;
                for (int i = 0; i < target.controlPoints.size(); i++) {
                    if (target.controlPoints.get(i) != null && target.controlPoints.get(i).contains(x, y)) {
                        target.selectedControl = i;
                        dragging = true;
                        break;
                    }
                }
            }

            // check if any shape is selected
            if (!dragging) {
                target = null;
                for (WbShape s : shapeList) {
                    if (s.shape instanceof RectangularShape) {
                        s.selected = s.shape.contains(x, y);
                    } else if (s.shape instanceof Line2D) {
                        s.selected = s.isClickNearLine((Line2D) s.shape, x, y);
                    }
                    startX = x;
                    startY = y;
                    if (s.selected) {
                        target = s;
                        dragging = true;
                    }
                }
            }
            repaint();

        // Draw Mode
        } else if (drawing) {
            startX = x;
            startY = y;
            dragging = true;

        // Text Mode
        } else if (texting){
            textTarget = null;
            // check if any text object is selected
            for (WbText t : textList) {
                if (t.contain(x,y)){
                    textTarget = t;
                    dragging = true;
                }
                startX = x;
                startY = y;
            }

        // Erase Mode
        } else {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragging = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Shape Mode logic
        if (!drawing && !erasing && ! texting) {
            if (dragging) {
                int x = e.getX();
                int y = e.getY();

                // drag control point to resize shapes
                if (target != null && target.selectedControl >= 0) {
                    target.adjustShape(x, y);
                    repaint();

                // check if any shape is selected to be moved
                } else {
                    for (WbShape s : shapeList) {
                        if (s.selected) {
                            int dx = x - startX;
                            int dy = y - startY;
                            s.moveBy(dx, dy);
                            startX = x;
                            startY = y;
                            repaint();
                        }
                    }
                }
            }

        // Draw Mode
        } else if (drawing){
            // continuously add free draw line to whiteboard
            if (dragging) {
                int x = e.getX();
                int y = e.getY();
                lineList.add(new WbLine(new Point(startX, startY), new Point(x, y),
                        drawingColor, 0));
                // update x,y value for next draw
                startX = x;
                startY = y;
                repaint();
            }

        // text mode
        }else if (texting){
            // check if any text is selected to be moved
            if (dragging) {
                int x = e.getX();
                int y = e.getY();
                int dx = x - startX;
                int dy = y - startY;
                textTarget.moveBy(dx,dy);
                startX = x;
                startY = y;
                repaint();
            }

        // Eraser Mode
        } else{
            // check if any free draw lines within eraser area, remove draws if yes
            if (dragging) {
                eraser = new Ellipse2D.Double(
                        e.getX() - (double) eraserSize / 2,
                        e.getY() - (double) eraserSize / 2,
                        eraserSize,
                        eraserSize);
                lineList.removeIf(l -> l.withinEraser(eraser));
                repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}
}
