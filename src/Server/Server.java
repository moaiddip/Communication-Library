/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Queue.WriteQueue;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

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

    private final int locality;
    private final String keystore;
    private final String keystorePass;
    private final String keypass;
    private final String logoutCmd;
    private final boolean listening = true;
    private ServerSocket serverSocket;
    private final int port;
    private ExecutorService executor = null;

    /**
     * Creates an SSLServerSocket and then it creates a loop that creates new
     * threads of the Communication class, every time a new connection is
     * established.
     *
     * @param port The port the server should listen to, as an int.
     * @param locality If this is 1 then loopback addresses will also be encrypted.
     * @param keystore The path to and the name of a keystore.
     * @param keystorePass The password of the keystore.
     * @param keypass The password of the private key in the keystore.
     * @param logoutCmd The logout command. If null the server will not attempt
     * to logout automatically.
     */
    public Server(int port, int locality, String keystore, String keystorePass, String keypass, String logoutCmd) {
        this.port = port;
        this.locality = locality; //1: Testing 0: Not testing
        this.keypass = keypass;
        this.keystore = keystore;
        this.keystorePass = keystorePass;
        Calendar cal = Calendar.getInstance();
        que = new WriteQueue(cal.getTimeInMillis());
        this.logoutCmd = logoutCmd;
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {

        try {
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
            SSLSocketFactory socketFactory = context.getSocketFactory();
            serverSocket = new ServerSocket(port, 100);
            System.out.println("Server Socket created. Listening.");
            while (listening) {
                Socket socket = serverSocket.accept();
                ConnectionHandler ch = null;
                if (socket.getInetAddress().isLoopbackAddress() && locality==0) {
                    ch = new ConnectionHandler((Socket) serverSocket.accept(), que, logoutCmd);
                } else {
                    SSLSocket SslSock = (SSLSocket) socketFactory.createSocket(socket, socket.getInetAddress().toString(), socket.getPort(), true);
                    SslSock.setUseClientMode(false);
                    ch = new ConnectionHandler((SSLSocket) SslSock, que, logoutCmd);
                }
                ch.init(getThreads());
                executor.submit(ch);
            }
        } catch (Exception ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public WriteQueue getTheQueue() {
        return que;
    }
}
