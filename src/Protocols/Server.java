/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

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

    private final HashMap<Integer, Communication> threads = new HashMap<Integer, Communication>();

    WriteQueue que;

    /**
     *
     * Returns a hashmap with all the server-client threads.
     *
     * @return the threads
     */
    public synchronized HashMap<Integer, Communication> getThreads() {
        return threads;
    }
    
    int port;
    int locality = 0;
    String keystore;
    String keystorePass;
    String keypass;
    int hashTail = 0;

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
     */
    public Server(int port, int locality, String keystore, String keystorePass, String keypass) {
        this.port = port;
        this.locality = locality; //if 1 server is local, 0 means remote
        this.keypass = keypass;
        this.keystore = keystore;
        this.keystorePass = keystorePass;
        Calendar cal = Calendar.getInstance();
        que = new WriteQueue(cal.getTimeInMillis());
    }

    @Override
    public synchronized void run() {
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
                int placed = 0; //0 means communication thread cannot find place in hashmap, 1 means it can
                for (int i = 0; i < threads.size(); i++) {
                    if (getThreads().get(i).isInterrupted() && placed == 0) {
                        getThreads().put(i, new Communication((SSLSocket) sslserversocket.accept(), que));
                        getThreads().get(i).start();
                        placed = 1;
                    }
                }
                if (placed == 0) {
                    getThreads().put(hashTail, new Communication((SSLSocket) sslserversocket.accept(), que));
                    getThreads().get(hashTail).start();
                    hashTail++;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public WriteQueue getTheQueue() {
        return que;
    }
}
