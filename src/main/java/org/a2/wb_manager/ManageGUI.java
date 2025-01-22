package org.a2.wb_manager;

import org.a2.common.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class implement all remote methods that are shared to all RMI clients.
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public class ManageGUI extends UnicastRemoteObject implements RemoteOperations {

    ServerWB wbPanel;
    ServerGUI gui;

    // constructor
    protected ManageGUI() throws RemoteException {
        // start a server gui
        gui = new ServerGUI();
        wbPanel = gui.wbPanel;
    }

    @Override
    public CopyOnWriteArrayList<WbShape> getShapes() throws RemoteException {
        return wbPanel.shapeList;
    }

    @Override
    public CopyOnWriteArrayList<WbLine> getDraws() throws RemoteException {
        return wbPanel.lineList;
    }

    @Override
    public CopyOnWriteArrayList<WbText> getTexts() throws RemoteException {
        return wbPanel.textList;
    }

    @Override
    public CopyOnWriteArrayList<String> getUserList() throws RemoteException {
        return ManageUser.names;
    }

    @Override
    public CopyOnWriteArrayList<String> getChatHist() throws RemoteException {
        return ManageUser.chatHist;
    }

    @Override
    public void addRect() throws RemoteException {
        gui.btnRect.doClick();
    }

    @Override
    public void addOval() throws RemoteException {
        gui.btnCircle.doClick();
    }

    @Override
    public void addLine() throws RemoteException {
        gui.btnLine.doClick();
    }

    @Override
    public void register(ReplyReceiver r, String username) throws RemoteException {
        // check whether username is distinct
        if (ManageUser.names.contains(username)){
            r.duplicateName();
        }else{
            // ask manager to answer user's request of join
            if (gui.showPermissionDialog(username)){
                ManageUser.clients.put(username,r);
                ManageUser.names.add(username);
                gui.updateUserList();

                // update all other users gui user list
                for (ReplyReceiver receiver : ManageUser.clients.values()){
                    if (receiver != r){
                        try {
                            receiver.updateUserList(ManageUser.names);
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                // reply client (join success)
                r.loginStatus(true);
            }else {
                // request denied
                r.loginStatus(false);
            }
        }
    }

    @Override
    public void quit(String username) throws RemoteException {
        ManageUser.clients.remove(username);
        ManageUser.names.remove(username);
        gui.updateUserList();
        ManageUser.updateValidUser();
    }

    @Override
    public void pressMouse(String mode, MouseEvent event) throws RemoteException {
        // Ask Server to perform press action in request mode
        String currMode = "shape";
        if (wbPanel.drawing){
            currMode = "draw";
        } else if (wbPanel.erasing) {
            currMode = "erase";
        } else if (wbPanel.texting) {
            currMode = "text";
        }

        if (mode.equals("shape")){
            gui.shapeMode();
            wbPanel.mousePressed(event);
        } else if (mode.equals("text")) {
            gui.textMode();
            wbPanel.mousePressed(event);
        }

        // return to original mode
        switch (currMode) {
            case "draw" -> gui.drawMode();
            case "erase" -> gui.eraseMode();
            case "text" -> gui.textMode();
            default -> gui.shapeMode();
        }
    }

    @Override
    public void dragMouse(String mode, MouseEvent event) throws RemoteException {
        // Ask Server to perform drag action in request mode
        String currMode = "shape";
        if (wbPanel.drawing){
            currMode = "draw";
        } else if (wbPanel.erasing) {
            currMode = "erase";
        } else if (wbPanel.texting) {
            currMode = "text";
        }

        if (mode.equals("shape")){
            gui.shapeMode();
            wbPanel.mouseDragged(event);
        } else if (mode.equals("text")) {
            gui.textMode();
            wbPanel.mouseDragged(event);
        }

        // return to original mode
        switch (currMode) {
            case "draw" -> gui.drawMode();
            case "erase" -> gui.eraseMode();
            case "text" -> gui.textMode();
            default -> gui.shapeMode();
        }
    }

    @Override
    public void releaseMouse(MouseEvent event) throws RemoteException {
        wbPanel.mouseReleased(event);
    }


    @Override
    public void setShapeColor(WbShape shape, Color color) throws RemoteException {
        wbPanel.shapeList.get(findIndex(shape)).shapeColor = color;
        wbPanel.repaint();
    }

    @Override
    public void setFillColor(WbShape shape, Color color) throws RemoteException {
        wbPanel.shapeList.get(findIndex(shape)).fillColor = color;
        wbPanel.repaint();
    }

    @Override
    public void setTextColor(WbShape shape, Color color) throws RemoteException {
        wbPanel.shapeList.get(findIndex(shape)).textColor = color;
        wbPanel.repaint();
    }

    @Override
    public void setText(WbShape shape, String text) throws RemoteException {
        for (WbShape s : wbPanel.shapeList){
            if (s.equals(shape)){
                s.text = text;
                wbPanel.repaint();
                break;
            }
        }
    }

    @Override
    public void deleteShape(WbShape shape) throws RemoteException {
        wbPanel.shapeList.remove(shape);
        wbPanel.repaint();
    }

    @Override
    public void addDraw(WbLine line) throws RemoteException {
        wbPanel.lineList.add(line);
        wbPanel.repaint();
    }

    @Override
    public void eraseDraw(Ellipse2D eraser) throws RemoteException {
        wbPanel.lineList.removeIf(l -> l.withinEraser(eraser));
        wbPanel.repaint();
    }

    @Override
    public void addText(WbText text) throws RemoteException {
        wbPanel.textList.add(text);
        wbPanel.repaint();
    }

    @Override
    public void setTextContent(WbText text, String s) throws RemoteException {
        for (WbText t : wbPanel.textList){
            if (t.equals(text)){
                t.text = s;
                wbPanel.repaint();
                break;
            }
        }
    }

    @Override
    public void setTextColor2(WbText text, Color color) throws RemoteException {
        for (WbText t : wbPanel.textList){
            if (t.equals(text)){
                t.color = color;
                wbPanel.repaint();
                break;
            }
        }
    }

    @Override
    public void deleteText(WbText text) throws RemoteException {
        wbPanel.textList.remove(text);
        wbPanel.repaint();
    }

    @Override
    public void sendMessage(String s) throws RemoteException {
        ManageUser.chatHist.add(s);
        gui.updateChatHist();
        ManageUser.updateChatHist();
    }

    /**
     * find the index of request target in the shape list
     * @param shape the target
     * @return the index of target, -1 if cant find any
     */
    private int findIndex(WbShape shape){
        for (int i = 0; i < wbPanel.shapeList.size(); i++) {
            if (wbPanel.shapeList.get(i).equals(shape)){
                return i;
            }
        }
        return -1;
    }


}

