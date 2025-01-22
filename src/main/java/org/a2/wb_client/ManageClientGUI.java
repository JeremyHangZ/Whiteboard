package org.a2.wb_client;

import org.a2.common.*;

import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This Class is a Manager of client gui, force client to register to server before start using.
 * This Class also defines all remote operations that can be called from server.
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public class ManageClientGUI extends UnicastRemoteObject implements ReplyReceiver {
    RemoteOperations wbService;
    ClientWB wbPanel;
    ClientGUI clientGUI;
    volatile Boolean validUser = false;

    // Constructor
    protected ManageClientGUI(RemoteOperations wbService) throws RemoteException {
        super();
        this.wbService = wbService;
        this.wbPanel = new ClientWB(wbService);
        System.out.println("Waiting permission from Manager");

        wbService.register(this, JoinWhiteboard.username); // register client to server

        // self-spin until receive permission from server
        while (!validUser) {
            Thread.onSpinWait();
        }
        System.out.println("Access Permitted :)");

        // open Gui
        this.clientGUI = new ClientGUI(wbService,wbPanel);
    }

    @Override
    public void updateShape(CopyOnWriteArrayList<WbShape> shapes) throws RemoteException {
        wbPanel.shapeList = shapes;
        clientGUI.repaint();
    }

    @Override
    public void updateLine(CopyOnWriteArrayList<WbLine> lines) throws RemoteException {
        wbPanel.lineList = lines;
        clientGUI.repaint();
    }

    @Override
    public void updateText(CopyOnWriteArrayList<WbText> texts) throws RemoteException {
        wbPanel.textList = texts;
        clientGUI.repaint();
    }

    @Override
    public void duplicateName() throws RemoteException {
        System.out.println("Username was in use, please try other names.");
        System.exit(0);
    }

    @Override
    public void loginStatus(Boolean status) throws RemoteException {
        if (status){
            validUser = true;
        }else {
            System.out.println("Access Denied :(");
            System.exit(0);
        }
    }

    @Override
    public void beKickedOut() throws RemoteException {
        System.out.println("You are kicked out by whiteboard manager :(");

        // Use thread to prevent blocking server
        new Thread(() -> {
            JOptionPane.showMessageDialog(clientGUI,
                    "You are kicked out by whiteboard manager :( ", "Confirm leave :)", JOptionPane.INFORMATION_MESSAGE);
            clientGUI.dispose();
            System.exit(0);
        }).start();
    }

    @Override
    public void updateUserList(CopyOnWriteArrayList<String> userList) throws RemoteException {
        clientGUI.updateUserList(userList);
    }

    @Override
    public void updateChatHist(CopyOnWriteArrayList<String> messageList) throws RemoteException {
        clientGUI.updateChatHist(messageList);
    }

    @Override
    public void serverClosed() throws RemoteException {
        System.out.println("Server terminated by Manager, services ended :)");
        JOptionPane.showMessageDialog(clientGUI,
                "Whiteboard terminate by Manager, Press Yes to leave :) ", "Confirm leave :)", JOptionPane.INFORMATION_MESSAGE);
        clientGUI.dispose();
        System.exit(0);
    }
}
