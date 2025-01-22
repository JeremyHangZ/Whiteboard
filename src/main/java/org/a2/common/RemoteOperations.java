package org.a2.common;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * This Interface list all services that a Whiteboard Server provide to its users.
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public interface RemoteOperations extends Remote {
    // Get all shapes from whiteboard
    CopyOnWriteArrayList<WbShape> getShapes() throws RemoteException;

    // Get all free draws from whiteboard
    CopyOnWriteArrayList<WbLine> getDraws() throws RemoteException;

    // Get all whiteboard text from whiteboard
    CopyOnWriteArrayList<WbText> getTexts() throws RemoteException;

    // Get current valid users' names from server
    CopyOnWriteArrayList<String> getUserList() throws RemoteException;

    // Get all chat box history from server
    CopyOnWriteArrayList<String> getChatHist() throws RemoteException;

    // Add a new rectangle to whiteboard
    void addRect() throws RemoteException;

    // Add a new circle/oval to whiteboard
    void addOval() throws RemoteException;

    // Add a new line to whiteboard
    void addLine() throws RemoteException;

    // Register a client to Server
    void register(ReplyReceiver r, String username) throws RemoteException;

    // Client ask for leave
    void quit(String username) throws RemoteException;

    // Monitoring mouse press event at server
    void pressMouse(String mode, MouseEvent event) throws RemoteException;

    // Monitoring mouse drag event at server
    void dragMouse(String mode, MouseEvent event) throws RemoteException;

    // Monitoring mouse release event at server
    void releaseMouse(MouseEvent event) throws RemoteException;

    // Change a specified shapes border color
    void setShapeColor(WbShape shape, Color color) throws RemoteException;

    // Change a specified shapes fill color
    void setFillColor(WbShape shape, Color color) throws RemoteException;

    // Change color of text within shape
    void setTextColor(WbShape shape, Color color) throws RemoteException;

    // Change text within Shape
    void setText(WbShape shape, String text) throws RemoteException;

    // Delete a specified shape from whiteboard
    void deleteShape(WbShape shape) throws RemoteException;

    // Add a new free draw to server
    void addDraw(WbLine line) throws RemoteException;

    // Erase free draw in selected area
    void eraseDraw(Ellipse2D eraser) throws RemoteException;

    // Add a whiteboard free text
    void addText(WbText text) throws RemoteException;

    // Change a specified whiteboard text's content
    void setTextContent(WbText text, String s) throws RemoteException;

    // Set a color for a whiteboard text
    void setTextColor2(WbText text, Color color) throws RemoteException;

    // Delete a specified whiteboard text
    void deleteText(WbText text) throws RemoteException;

    // Send a message to whiteboard communication box
    void sendMessage(String s) throws RemoteException;
}
