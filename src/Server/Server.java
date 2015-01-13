/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Queue.WriteQueue;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final AtomicBoolean listening = new AtomicBoolean(true);
    private ServerSocket serverSocket;
    private final int port;
    private ExecutorService executor = null;
    private final AtomicBoolean terminated = new AtomicBoolean(false);

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
     */
    public Server(int port, int locality, String keystore, String keystorePass, String keypass) {
        this.port = port;
        this.locality = locality; //1: Testing 0: Not testing
        this.keypass = keypass;
        this.keystore = keystore;
        this.keystorePass = keystorePass;
        Calendar cal = Calendar.getInstance();
        que = new WriteQueue(cal.getTimeInMillis());
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
            while (listening.get()) {
                Socket socket = serverSocket.accept();
                ConnectionHandler ch;
                if (socket.getInetAddress().isLoopbackAddress() && locality==0) {
                    ch = new ConnectionHandler((Socket) socket, que);
                } else {
                    SSLSocket SslSock = (SSLSocket) socketFactory.createSocket(socket, socket.getInetAddress().toString(), socket.getPort(), true);
                    SslSock.setUseClientMode(false);
                    ch = new ConnectionHandler((SSLSocket) SslSock, que);
                }
                ch.init(getThreads());
                executor.submit(ch);
            }
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(serverSocket!=null || !serverSocket.isClosed()){
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            terminated.set(true);
        }

    }

    public WriteQueue getTheQueue() {
        return que;
    }
    public boolean quit(){
        listening.set(false);
        while(!terminated.get());
        return true;
    }
}
