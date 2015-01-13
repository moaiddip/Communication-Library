/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Queue.Item;
import Queue.DynamicQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private SSLSocket sslsocket;
    private final DynamicQueue que;
    private String user = null;
    private int userPrio = -1;
    private final String exitCmd = "isAboutToExit";
    private final String replyCmd = "isReply";
    private final AtomicBoolean terminated = new AtomicBoolean(false);
    private Socket socket;
    private final int mode;

    /**
     * Handles communication with each client. It extends Thread. Implemented
     * automatically in the Server class. Should not be called.
     *
     * @param sslsocket Requires an sslsocket with an established connection.
     * @param que Requires a shared instance of the WriteQueue class.
     */
    public ConnectionHandler(SSLSocket sslsocket, DynamicQueue que) {
        this.sslsocket = sslsocket;
        this.que = que;
        mode = 0;
    }

    public ConnectionHandler(Socket socket, DynamicQueue que) {
        this.socket = socket;
        this.que = que;
        mode = 1;
    }

    public synchronized void init(ConcurrentHashMap<Integer, ConnectionHandler> threads) {
        try {
            if (mode == 0) {
                out = new PrintWriter(sslsocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
                remoteSocketAddress = sslsocket.getRemoteSocketAddress().toString();
            } else {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                remoteSocketAddress = socket.getRemoteSocketAddress().toString();
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (threads.isEmpty()) {
            threads.put(0, this);

        } else {
            boolean foundSpot = false;
            for (int i = 0; i < threads.size(); i++) {
                if (threads.get(i) == null) {
                    threads.put(i, this);
                    foundSpot = true;
                } else if (!threads.get(i).isAlive()) {
                    threads.put(i, this);
                    foundSpot = true;
                }
            }
            if (!foundSpot) {
                threads.put(threads.size(), this);
            }
        }
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
                    //exit is the quit command, replies with ok

                    if (exitCmd.equals(string)) {
                        out.print(exitCmd);
                        listener = false;
                        break;
                    } else {
                        //Puts message in queue and waits until the message is
                        //Answered, then it sends the answer back to the client
                        Item item;
                        item = que.putCmd(string, remoteSocketAddress, userPrio);
                        if (user != null) {
                            item.setUser(getUser());
                            item.setUserPrio(userPrio);
                        }
                        synchronized (item) {
                            while (item.isAnswered() == false) {
                                item.wait();
                            }
                        }

                        string = item.getReply() + replyCmd;
                        if (item.getUserChanged()) {
                            user = item.getUser();
                            userPrio = item.getUserPrio();
                        }
                        out.println(string);
                        item.makeOld();
                    }

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println("Quiting ConnectionHandler thread with ip: "+remoteSocketAddress);
            terminated.set(true);
            try {
                if (sslsocket != null) {
                    sslsocket.close();
                } else if (socket != null) {
                    socket.close();
                }
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
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
        return user;
    }

    /**
     * Sends a status update to this client.
     *
     * @param status The status update as a string.
     */
    public void sendUpdate(String status) {
        if (!terminated.get()) {
            out.println(status);
        }
    }
}
