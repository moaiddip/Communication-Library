/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Queue.WriteQueue;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * The class used to create an SSLServerSocket and listen to connection
 * requests. This is the server.
 *
 * @author Sozos Assias
 */
public class Server extends Thread {

    private final HashMap<Integer, ConnectionHandler> threads = new HashMap<Integer, ConnectionHandler>();

    private final WriteQueue que;

    /**
     *
     * Returns a hashmap with all the server-client threads.
     *
     * @return the threads
     */
    public synchronized HashMap<Integer, ConnectionHandler> getThreads() {
        return threads;
    }

    private final int port;
    private final int locality;
    private final String keystore;
    private final String keystorePass;
    private final String keypass;
    private int hashTail = 0;
    private final String logoutCmd;
    private final String exitCmd;

    /**
     * Creates an SSLServerSocket and then it creates a loop that creates new
     * threads of the Communication class, every time a new connection is
     * established.
     *
     * @param port The port that the server should listen to, as an int.
     * @param locality An int, either 0 or 1, indicating if the server should
     * run remotely and locally or only locally respectively.
     * @param keystore The path to and the name of a keystore.
     * @param keystorePass The password of the keystore.
     * @param keypass The password of the private key in the keystore.
     * @param logoutCmd The logout command. If null the server will not attempt
     * to logout automatically.
     */
    public Server(int port, int locality, String keystore, String keystorePass, String keypass, String logoutCmd, String exitCmd) {
        this.port = port;
        this.locality = locality; //if 1 server is local, 0 means remote
        this.keypass = keypass;
        this.keystore = keystore;
        this.keystorePass = keystorePass;
        Calendar cal = Calendar.getInstance();
        que = new WriteQueue(cal.getTimeInMillis());
        this.logoutCmd = logoutCmd;
        this.exitCmd=exitCmd;
    }

    @Override
    public void run() {
        boolean listening = true;
        try {
//            Calendar cal = Calendar.getInstance();
//            que = new WriteQueue(cal.getTimeInMillis());
            System.out.println("Creating Server Socket.");
            //Password to the keystore
            char[] passphrase = keystorePass.toCharArray();
            //Load the keystore class and then load the keystore file using the password
            KeyStore store = KeyStore.getInstance("JKS");
            store.load(new FileInputStream(keystore), passphrase);
            //KeyManagerFactory initiated to get the key from the keystore using the key's password
            KeyManagerFactory factory
                    = KeyManagerFactory.getInstance("SunX509");
            factory.init(store, keypass.toCharArray());
            //Set security to TSL and initiate using the key we just recovered
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(factory.getKeyManagers(), null, null);
            //Create a socket factory and then create a socket using the factory
            SSLServerSocketFactory socketfactory = context.getServerSocketFactory();
            SSLServerSocket sslserversocket;
            if (locality == 0) {
                sslserversocket
                        = (SSLServerSocket) socketfactory.createServerSocket(port, 100); //port, no of max clients
            } else {
                sslserversocket
                        = (SSLServerSocket) socketfactory.createServerSocket(port, 100, InetAddress.getByName(null));
            }
            System.out.println("Server Socket created. Listening.");
            //Creates the que and listens to the socket.

            while (listening) {

                SSLSocket socket;
                if ((socket = (SSLSocket) sslserversocket.accept()).isConnected()) {
                    placeInThreads(socket);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private synchronized void placeInThreads(SSLSocket socket) {
        int placed = 0; //0 means communication thread cannot find place in hashmap, 1 means it can
        for (int i = 0; i < threads.size(); i++) {
            if (getThreads().get(i).isInterrupted() && placed == 0) {
                getThreads().put(i, new ConnectionHandler(socket, que, logoutCmd, exitCmd));
                getThreads().get(i).start();
                placed = 1;
            }
        }
        if (placed == 0) {
            getThreads().put(hashTail, new ConnectionHandler(socket, que, logoutCmd, exitCmd));
            getThreads().get(hashTail).start();
            hashTail++;
        }
    }

    public WriteQueue getTheQueue() {
        return que;
    }
}
