package org.a2.common;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;
import java.io.Serial;
import java.io.Serializable;

/**
 * This Class defines free draw line that can be added to whiteboard
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public class WbLine implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // defines basic feature of a free-draw line
    Point startPoint;
    Point endPoint;
    Color color;
    int size;

    // Constructor
    public WbLine(Point startPoint, Point endPoint, Color color, int size) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.color = color;
        this.size = size;
    }

    /**
     * Draw the line to whiteboard
     * @param g the graphics
     */
    public void drawLine(Graphics2D g){
        g.setColor(color);
        g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
    }

    /**
     * Check if any lines within a specified eraser area.
     * @param eraser A ellipse circle act as eraser
     * @return true if within, false not in
     */
    public boolean withinEraser(Ellipse2D eraser){
        return eraser.contains(startPoint.x, startPoint.y) || eraser.contains(endPoint.x, endPoint.y);
    }
}
