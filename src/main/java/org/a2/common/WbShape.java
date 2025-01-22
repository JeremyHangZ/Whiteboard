package org.a2.common;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RectangularShape;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This Class defines all the shapes that can be added to whiteboard
 * Shapes include rectangle, circle, oval, and line
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public class WbShape implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Define basic features of a whiteboard shape
    public Shape shape;
    public String text; // text within shape
    public Color shapeColor; // shape border color
    public Color textColor;
    public Color fillColor; // shape fill color
    public Boolean selected = false; // check if shape is selected by user
    public ArrayList<Shape> controlPoints; // assign control points to shape
    public int selectedControl = -1; // check if any control point of shape is selected

    // constructor
    public WbShape(Shape shape) {
        this.shape = shape;
        this.shapeColor = Color.black;
        this.textColor = Color.BLACK;
        this.setControlPoint();
    }

    // constructor
    public WbShape(WbShape s) {
        this.shape = s.shape;
        this.text = s.text;
        this.shapeColor = s.shapeColor;
        this.textColor = s.textColor;
        this.fillColor = s.fillColor;
        this.selected = s.selected;
        this.controlPoints = s.controlPoints;
        this.selectedControl = s.selectedControl;
    }

    /**
     * Draw the shape features to whiteboard
     * @param g the whiteboard graphics
     */
    public void drawShape(Graphics2D g) {
        g.setColor(shapeColor);

        // Handle rectangular shapes
        if (shape instanceof RectangularShape rect) {
            g.draw(shape);

            // fill shape with color
            if (fillColor != null){
                g.setColor(fillColor);
                g.fill(shape);
            }

            // Inserting text into shape, centralized alignment
            if (text != null && !text.isEmpty()){
                g.setColor(textColor);
                FontMetrics metrics = g.getFontMetrics();
                int textX = (int) (rect.getX() + (rect.getWidth() - metrics.stringWidth(text)) / 2);
                int textY = (int) (rect.getY() + ((rect.getHeight() - metrics.getHeight()) / 2) + metrics.getAscent());
                g.drawString(text, textX, textY);
            }

        // Handle Line shape
        } else if (shape instanceof Line2D line) {
            if (fillColor != null){
                g.setColor(fillColor);
            }
            g.drawLine((int) line.getX1(), (int) line.getY1(), (int) line.getX2(), (int) line.getY2());
        }

        // if shape is selected, present all control points
        if (selected) {
            for (Shape s : controlPoints) {
                g.setColor(Color.blue);
                g.fill(s);
            }
        }
    }

    /**
     * This method assign control points to shape according to its type
     */
    private void setControlPoint() {
        this.controlPoints = new ArrayList<>();

        // rectangular shape has 8 control points
        if (shape instanceof RectangularShape) {
            int size = 8;
            int x = (int) (((RectangularShape) shape).getX() - size / 2);
            int y = (int) (((RectangularShape) shape).getY() - size / 2);
            int w = (int) (((RectangularShape) shape).getWidth());
            int h = (int) (((RectangularShape) shape).getHeight());

            // Define control points
            int[][] points = {
                    {x, y}, {x + w / 2, y}, {x + w, y}, // top row
                    {x, y + h / 2}, {x + w, y + h / 2}, // middle row
                    {x, y + h}, {x + w / 2, y + h}, {x + w, y + h}  // bottom row
            };

            // Add control points according to rect shape position
            for (int[] point : points) {
                controlPoints.add(new Rectangle(point[0], point[1], size, size));
            }

        // Line shape has two control points
        } else if (shape instanceof Line2D line) {
            int size = 8;
            int x1 = (int) (line.getX1() - size / 2);
            int y1 = (int) (line.getY1() - size / 2);
            int x2 = (int) (line.getX2() - size / 2);
            int y2 = (int) (line.getY2() - size / 2);

            // Add control point according to line position
            controlPoints.add(new Rectangle(x1, y1, size, size));
            controlPoints.add(new Rectangle(x2, y2, size, size));
        }
    }

    /**
     * This method update shape position to new (x,y)
     * @param x new X position
     * @param y new Y position
     */
    public void moveBy(int x, int y){
        // update rectangular shape
        if (shape instanceof RectangularShape){
            int newX = (int) (((RectangularShape) shape).getX() + x);
            int newY = (int) (((RectangularShape) shape).getY() + y);
            if (shape instanceof Rectangle){
                shape = new Rectangle(newX,newY,((Rectangle) shape).width, ((Rectangle) shape).height);
            } else if (shape instanceof Ellipse2D) {
                shape = new Ellipse2D.Double(newX,newY,((Ellipse2D) shape).getWidth(), ((Ellipse2D) shape).getHeight());
            }
            // reset control points as position changed
            this.setControlPoint();

        // update Line shape
        } else if (shape instanceof Line2D line) {
            double newX1 = line.getX1() + x;
            double newY1 = line.getY1() + y;
            double newX2 = line.getX2() + x;
            double newY2 = line.getY2() + y;
            shape = new Line2D.Double(newX1, newY1, newX2, newY2);
            // reset control points as position changed
            this.setControlPoint();
        }
    }

    /**
     * This method change the size of selected shape
     * @param x End x-position of selected control point
     * @param y End y-position of selected control point
     */
    public void adjustShape(int x, int y) {
        // Logic to update rectangular shape
        if (shape instanceof RectangularShape rect){
            int newX = (int) rect.getX();
            int newY = (int) rect.getY();
            int newWidth = (int) rect.getWidth();
            int newHeight = (int) rect.getHeight();

            // Logic to update size differentiated by control point
            switch (selectedControl) {
                case 0: // Top-left
                    newX = x;
                    newY = y;
                    newWidth += (int) (rect.getX() - x);
                    newHeight += (int) (rect.getY() - y);
                    break;
                case 1: // Top-middle
                    newY = y;
                    newHeight += (int) (rect.getY() - y);
                    break;
                case 2: // Top-right
                    newY = y;
                    newWidth = x - newX;
                    newHeight += (int) (rect.getY() - y);
                    break;
                case 3: // Middle-left
                    newX = x;
                    newWidth += (int) (rect.getX() - x);
                    break;
                case 4:
                    newWidth = x - newX;
                    break;
                case 5: // Bottom-left
                    newX = x;
                    newWidth += (int) (rect.getX() - x);
                    newHeight = y - newY;
                    break;
                case 6: // Bottom-middle
                    newHeight = y - newY;
                    break;
                case 7: // Bottom-right
                    newWidth = x - newX;
                    newHeight = y - newY;
                    break;
            }

            // creat new shape to determined location with determined size
            if (shape instanceof Rectangle) {
                shape = new Rectangle(newX, newY, newWidth, newHeight);
            } else if (shape instanceof Ellipse2D) {
                shape = new Ellipse2D.Double(newX, newY, newWidth, newHeight);
            }

        // Logic to update line shape
        }else if (shape instanceof Line2D line) {
            double newX1 = line.getX1();
            double newY1 = line.getY1();
            double newX2 = line.getX2();
            double newY2 = line.getY2();

            switch (selectedControl) {
                case 0: // Start point control
                    newX1 = x;
                    newY1 = y;
                    break;
                case 1: // End point control
                    newX2 = x;
                    newY2 = y;
                    break;
            }
            // creat new line with new specified features
            shape = new Line2D.Double(newX1, newY1, newX2, newY2);
        }

        // update control points of shape as position changed
        setControlPoint();
    }

    /**
     * This method check if a mouse click position is close enough to a line shape
     * @param line the line shape object
     * @param mouseX X-position of mouse click event
     * @param mouseY Y-position of mouse click event
     * @return true if close enough, else false
     */
    public boolean isClickNearLine(Line2D line, double mouseX, double mouseY) {
        // Calculate the shortest distance mouse click position to given line shape
        double distance = line.ptSegDist(mouseX, mouseY);

        // if distance with in 10 grid size, return true
        return distance <= 10;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WbShape wbShape = (WbShape) o;

        if (this.shape instanceof Line2D thisLine && wbShape.shape instanceof Line2D) {
            Line2D otherLine = (Line2D) wbShape.shape;
            return thisLine.getP1().equals(otherLine.getP1()) && thisLine.getP2().equals(otherLine.getP2());
        }

        if (!shape.equals(wbShape.shape)) return false;
        if (!Objects.equals(text, wbShape.text)) return false;
        if (!Objects.equals(shapeColor, wbShape.shapeColor)) return false;
        if (!Objects.equals(textColor, wbShape.textColor)) return false;
        if (!Objects.equals(fillColor, wbShape.fillColor)) return false;

        return controlPoints.equals(wbShape.controlPoints);
    }

    @Override
    public int hashCode() {
        int result = shape.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (shapeColor != null ? shapeColor.hashCode() : 0);
        result = 31 * result + (textColor != null ? textColor.hashCode() : 0);
        result = 31 * result + (fillColor != null ? fillColor.hashCode() : 0);
        result = 31 * result + controlPoints.hashCode();
        result = 31 * result + selectedControl;
        return result;
    }
}
