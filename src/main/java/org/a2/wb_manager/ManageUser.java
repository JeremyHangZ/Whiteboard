package org.a2.wb_manager;

import org.a2.common.ReplyReceiver;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class is used to manage all users-related information and operation
 *
 *  @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public class ManageUser {

    // Store all user instances and names and chat histories
    static ConcurrentHashMap<String, ReplyReceiver> clients = new ConcurrentHashMap<>();
    static CopyOnWriteArrayList<String> names = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<String> chatHist = new CopyOnWriteArrayList<>();

    /**
     * Kick a user out from whiteboard
     * @param username specified user to be kicked
     */
    static void kickOut(String username){
        if (clients.containsKey(username) && !Objects.equals(username, CreateWhiteboard.username)){
            try {
                // inform user
                clients.get(username).beKickedOut();
            } catch (RemoteException ignored) {
            // remove user information form server
            }finally {
                clients.remove(username);
                names.remove(username);
                updateValidUser();
            }
        }
    }

    /**
     * Remove a client from whiteboard version 2
     * @param replyReceiver the clients remote interface instance
     */
    static void remove(ReplyReceiver replyReceiver){
        Iterator<ConcurrentHashMap.Entry<String, ReplyReceiver>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            ConcurrentHashMap.Entry<String, ReplyReceiver> entry = iterator.next();
            String id = entry.getKey();
            ReplyReceiver r = entry.getValue();
            if (r.equals(replyReceiver)) {
                iterator.remove();
                names.remove(id);
                updateValidUser();
                ServerGUI.updateUserList();
                break;
            }
        }
    }

    /**
     * send the latest valid user list to all users
     */
    static void updateValidUser(){
        for (ReplyReceiver r : clients.values()){
            try {
                r.updateUserList(names);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * send the latest chat histories to all users
     */
    static void updateChatHist(){
        for (ReplyReceiver r : clients.values()){
            try {
                r.updateChatHist(chatHist);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Server terminating logic, inform all user
     */
    static void terminateWb(){
        for (ReplyReceiver r : clients.values()) {
            new Thread(() -> {
                try {
                    r.serverClosed();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
}
