package org.a2.wb_manager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Entrance to start a whiteboard server, register RMI to local machine
 *
 * @author Sihang Zhang 1114039 sihangz@student.unimelb.edu.au
 */
public class CreateWhiteboard {
    static int port;
    static String username;

    public static void main(String[] args) throws RemoteException {
        // check validity of user input
        if (args.length != 3){
            System.out.println("Please enter arguments in format (<serverIPAddress> <serverPort> username).");
            return;


        }else {
            if (!args[0].equals("localhost")){
                System.out.println("Cannot find specified Whiteboard Server.");
                return;
            }
            try {
                port = Integer.parseInt(args[1]);
                username = args[2] + " (Manager)";
            }catch (Exception e){
                System.out.println("Please enter arguments in format (<serverIPAddress> <serverPort> username).");
                return;
            }
        }

        // start register service to registry
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            ManageUser.names.add(username);
            ManageGUI manageGUI = new ManageGUI();
            registry.rebind("WhiteboardService", manageGUI);

            System.out.println("Server started :)");
        } catch (Exception e) {
            System.out.println("Cannot connect to whiteboard server, try again.");
        }
    }
}
