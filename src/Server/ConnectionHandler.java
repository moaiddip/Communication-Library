/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Queue.Item;
import Queue.WriteQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

/**
 * This is the class used by the Server class to handle communication with each
 * client.
 *
 * @author Sozos Assias
 */
public class ConnectionHandler extends Thread {

    private BufferedReader in;
    private PrintWriter out;
    private String remoteSocketAddress;
    private final SSLSocket sslsocket;
    private final WriteQueue que;
    private String user = null;
    private int userPrio = -1;
    private final String logoutCmd;
    private final String exitCmd;

    /**
     * Handles communication with each client. It extends Thread. Implemented
     * automatically in the Server class. Should not be called.
     *
     * @param sslsocket Requires an sslsocket with an established connection.
     * @param que Requires a shared instance of the WriteQueue class.
     * @param logoutCmd The default logout command, if null it will not attempt
     * to logout
     * @param exitCmd The command used to close the socket.
     */
    public ConnectionHandler(SSLSocket sslsocket, WriteQueue que, String logoutCmd, String exitCmd) {
        this.sslsocket = sslsocket;
        this.que = que;
        this.logoutCmd = logoutCmd;
        this.exitCmd = exitCmd;
        //init(server, threads, hashTail);

    }

    public synchronized int init(Server server, HashMap<Integer, ConnectionHandler> threads, int hashTail) {
        try {
            out = new PrintWriter(sslsocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
            remoteSocketAddress = sslsocket.getRemoteSocketAddress().toString();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        int placed = 0; //0 means communication thread cannot find place in hashmap, 1 means it can
        for (int i = 0; i < threads.size(); i++) {
            if (placed == 0 && threads.get(i) != null) {
                if (!threads.get(i).isAlive()) {
                    threads.put(i, this);
                    placed = 1;
                    System.out.println("Placed client in the existing spot no.:"+i+".");
                }
            }
        }
        if (placed == 0) {
            threads.put(hashTail, this);
            System.out.println("Placed client thread in the new spot no.:"+hashTail+".");
            hashTail++;
        }
        return hashTail;
    }

    @Override
    public void run() {
        System.out.println("Connection with client " + remoteSocketAddress + " initialized successfully.");
        Boolean listener = true;
        try {
            String string;
            //System.out.println(remoteSocketAddress);
            //Listens in a loop until asked to exit
            while (listener) {
                //Get string from the input stream
                while ((string = in.readLine()) != null) {
                    System.out.println("Received query.");
                    //exit is the quit command, replies with ok

                    if (exitCmd.equals(string)) {
                        listener = false;
                        out.println("ok");
                    } else {
                        //Puts message in queue and waits until the message is
                        //Answered, then it sends the answer back to the client
                        Item item;
                        item = que.putCmd(string, remoteSocketAddress, userPrio);
                        if (userPrio != -1) {
                            item.setUser(getUser());
                            item.setUserPrio(userPrio);
                        }
                        synchronized (item) {
                            while (item.isAnswered() == false) {
                                item.wait();
                            }
                        }
                        System.out.println("Answer processed, preparing to reply.");

                        string = item.getReply();
                        if (getUser() == null) {
                            user = item.getUser();
                            userPrio = item.getUserPrio();
                        }

                        System.out.println("Replying.");
                        out.println(string);
                        item.makeOld();
                    }

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                Item item;
                if (user != null && logoutCmd != null) {
                    item = que.putCmd(logoutCmd, remoteSocketAddress, userPrio);
                    item.setUser(getUser());
                }

                sslsocket.close();
                in.close();
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Returns the user that occupies this thread.
     *
     * @return the user as a string. If it's null then there is no user logged
     * in.
     */
    public synchronized String getUser() {
        return user; //Sync problem.
    }

    /**
     * Sends a status update to this client.
     *
     * @param status The status update as a string.
     */
    public void sendUpdate(String status) {
        out.println(status);
        System.out.println("Sending status update: " + status);
    }
}
