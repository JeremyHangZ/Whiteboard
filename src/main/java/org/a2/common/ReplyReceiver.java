package org.a2.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This Interface list all remote operations that a whiteboard user provide to server
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public interface ReplyReceiver extends Remote {
    // Update whiteboard shapes received from Server
    void updateShape(CopyOnWriteArrayList<WbShape> shapes) throws RemoteException;

    // Update whiteboard free draws received from Server
    void updateLine(CopyOnWriteArrayList<WbLine> lines) throws RemoteException;

    // Update whiteboard texts received from Server
    void updateText(CopyOnWriteArrayList<WbText> texts) throws RemoteException;

    // Check if username is already in use
    void duplicateName() throws RemoteException;

    // Check Login status
    void loginStatus(Boolean status) throws RemoteException;

    // Receive inform from Sever if user was kicked out
    void beKickedOut() throws RemoteException;

    // Receive valid user list from server
    void updateUserList(CopyOnWriteArrayList<String> userList) throws RemoteException;

    // Receive the latest Chat history from Server
    void updateChatHist(CopyOnWriteArrayList<String> messageList) throws RemoteException;

    // Close Gui if Server closed
    void serverClosed() throws RemoteException;
}
