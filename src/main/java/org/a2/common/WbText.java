package org.a2.common;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * This Class defines a text object that can be added to whiteboard
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public class WbText implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Defines the features of a whiteboard text
    public String text;
    public int x;
    public int y;
    public Color color;
    public int textWidth;
    public int textHeight;

    // constructor
    public WbText(String text, int x, int y, Color color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    /**
     * Draw the text to whiteboard
     * @param g the graphics of whiteboard
     */
    public void drawText(Graphics2D g){
        g.setColor(color);
        // Get the text height and width to determine position
        FontMetrics metrics = g.getFontMetrics();
        textWidth = metrics.stringWidth(text);
        textHeight = metrics.getHeight();
        int dx = x - textWidth / 2;
        int dy = y - textHeight / 2 + metrics.getAscent();  // 基线位置
        g.drawString(text, dx, dy);
    }

    /**
     * This method check whether a mouse click position is on the text.
     * @param x X-position of mouse event
     * @param y Y-position of mouse event
     * @return true if click on the text. false otherwise.
     */
    public Boolean contain(int x, int y){
        return (x > this.x - textWidth / 2) && (x < this.x + textWidth / 2) &&
                (y > this.y - textHeight / 2) && (y < this.y + textHeight / 2);
    }

    /**
     * Update text Position
     * @param dx change of x-value
     * @param dy change of y-value
     */
    public void moveBy(int dx, int dy){
        x = x + dx;
        y = y + dy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WbText wbText = (WbText) o;

        if (x != wbText.x) return false;
        if (y != wbText.y) return false;
        if (textWidth != wbText.textWidth) return false;
        if (textHeight != wbText.textHeight) return false;
        if (!Objects.equals(text, wbText.text)) return false;
        return Objects.equals(color, wbText.color);
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + textWidth;
        result = 31 * result + textHeight;
        return result;
    }
}
