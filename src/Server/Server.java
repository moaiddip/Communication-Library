/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Queue.WriteQueue;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
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

    private final ConcurrentHashMap<Integer, ConnectionHandler> threads = new ConcurrentHashMap<Integer, ConnectionHandler>();

    private final WriteQueue que;

    /**
     *
     * Returns a hashmap with all the server-client threads.
     *
     * @return the threads
     */
    public synchronized ConcurrentHashMap<Integer, ConnectionHandler> getThreads() {
        return threads;
    }

    private final int portSSL;
    private final int locality;
    private final String keystore;
    private final String keystorePass;
    private final String keypass;
    private final String logoutCmd;
    private final boolean listening = true;
    private ServerSocket serverSocket;
    private SSLServerSocket sslserversocket;
    private final int port;

    /**
     * Creates an SSLServerSocket and then it creates a loop that creates new
     * threads of the Communication class, every time a new connection is
     * established.
     *
     * @param portSSL The port that the SSL server should listen to, as an int.
     * @param port The port the not secured server should listen to, as an int.
     * @param locality An int, either 0 or 1, indicating if the server should
     * run remotely and locally or only locally respectively.
     * @param keystore The path to and the name of a keystore.
     * @param keystorePass The password of the keystore.
     * @param keypass The password of the private key in the keystore.
     * @param logoutCmd The logout command. If null the server will not attempt
     * to logout automatically.
     */
    public Server(int portSSL, int port, int locality, String keystore, String keystorePass, String keypass, String logoutCmd) {
        this.portSSL = portSSL;
        this.port = port;
        this.locality = locality; //if 1 server is local, 0 means remote
        this.keypass = keypass;
        this.keystore = keystore;
        this.keystorePass = keystorePass;
        Calendar cal = Calendar.getInstance();
        que = new WriteQueue(cal.getTimeInMillis());
        this.logoutCmd = logoutCmd;
    }

    @Override
    public void run() {

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
            
            if (locality == 0) {
                sslserversocket
                        = (SSLServerSocket) socketfactory.createServerSocket(portSSL, 100); //port, no of max clients
            } else {
                sslserversocket
                        = (SSLServerSocket) socketfactory.createServerSocket(portSSL, 100, InetAddress.getByName(null));      
            }
            serverSocket = new ServerSocket(port, 100, InetAddress.getByName(null));
            System.out.println("Server Socket created. Listening.");
            //Creates the que and listens to the socket.
            new Thread(new Listener()).start();
            new Thread(new SSLListener()).start();
            
        } catch (Exception ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public WriteQueue getTheQueue() {
        return que;
    }

    class SSLListener extends Thread {

        @Override
        public void run() {
            while (listening) {
                try {
                    ConnectionHandler ch = new ConnectionHandler((SSLSocket) sslserversocket.accept(), que, logoutCmd);
                    ch.init(getThreads());
                    ch.start();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    class Listener extends Thread {

        @Override
        public void run() {
            while (listening) {
                try {
                    ConnectionHandler ch = new ConnectionHandler((Socket) serverSocket.accept(), que, logoutCmd);
                    ch.init(getThreads());
                    ch.start();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
