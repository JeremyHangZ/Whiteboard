package org.a2.wb_client;

import org.a2.common.RemoteOperations;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Entrance for Whiteboard Client to build connect with Server
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public class JoinWhiteboard {
    // Each client should hold below features before connection.
    static String username;
    static int port;
    static String serverIP;

    // Main function, start connection
    public static void main(String[] args) {
        // check arguments in correct format
        if (args.length != 3){
            System.out.println("Please enter arguments in format (<serverIPAddress> <serverPort> username).");
            return;

        }else {
            // Set the server ip address
            try {
                if (args[0].equals("localhost")){
                    serverIP = InetAddress.getLocalHost().getHostAddress();
                }else {
                    serverIP = args[0];
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

            // Set server port and client id
            try {
                port = Integer.parseInt(args[1]);
                username = args[2];
            }catch (Exception e){
                System.out.println("Please enter arguments in format (<serverIPAddress> <serverPort> username).");
                return;
            }
        }

        // Start connecting to server
        try {
            Registry registry = LocateRegistry.getRegistry(serverIP, port);
            RemoteOperations whiteboard = (RemoteOperations) registry.lookup("WhiteboardService");
            new ManageClientGUI(whiteboard);

        } catch (Exception e) {
            System.out.println("Cannot connect to whiteboard server, try again :)");
        }
    }
}
