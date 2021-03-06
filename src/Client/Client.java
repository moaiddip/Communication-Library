/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Queue.SimpleQueue;
import Server.ConnectionHandler;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * This is the class used to create a socket for the client and communicate with the server. It is a thread.
 *
 * @author Sozos Assias
 */
public class Client extends Thread {

    private final int device; // 1 for android device, 0 for computer app, 2 for webbrowser
    private final String url; // holds ip address
    private final int port;
    private String truststore; //Location of keystore
    private String trustpass; // keystore password
    private InputStream trustStoreStream;
    private String command; // default command
    private String reply = null;
    private final AtomicBoolean query = new AtomicBoolean(false);
    private final SimpleQueue cQue = new SimpleQueue(); //arduino client queue
    private final AtomicBoolean quit = new AtomicBoolean(false);
    private final AtomicBoolean working = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final AtomicBoolean conError = new AtomicBoolean(false);
    private PrintWriter out;
    private BufferedReader in;
    private SSLSocket sslsocket;
    private final String exitCmd = "isAboutToExit";
    private final String replyCmd = "isReply";
    private final int timeOut = 15;
    private Socket socket;

    /**
     * The first constructor for computers.
     *
     * @param url The ip address of the server
     * @param port The port of the server
     * @param truststore The truststore location and name. eg.
     * "c:\blabla\keystore.jks"
     * @param trustpass The truststore pass.
     */
    public Client(String url, int port, String truststore, String trustpass) {
        device = 0;
        this.url = url;
        this.port = port;
        this.truststore = truststore;
        this.trustpass = trustpass;
    }

    /**
     * The constructor for android devices.
     *
     * @param url The ip address of the server
     * @param port The port of the server
     * @param truststore The input stream of the truststore.
     * @param trustpass The truststore pass.
     */
    public Client(String url, int port, InputStream truststore, String trustpass) {
        device = 1;
        this.url = url;
        this.port = port;
        this.trustStoreStream = truststore;
        this.trustpass = trustpass;
    }
    /**
     * Creates a plain socket, used for applications running on the same computer as the server.
     * @param url The url (or in this case 127.0.0.1)
     * @param port The port
     */
    public Client(String url, int port) {
        device = 2;
        this.url = url;
        this.port = port;
    }

    /**
     * Creates an socket and creates 2 threads to send and receive commands.
     *
     *
     */
    @Override
    public void run() {
        try {
            System.out.println("Creating socket.");
            if (device == 0 || device == 1) {
                SSLContext context;
                KeyStore keystore;
                //Passphrase = the password to the truststore
                char[] passphrase = trustpass.toCharArray();
                //Get TrustManagerFactory
                TrustManagerFactory trustfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                //Set security to TLS
                context = SSLContext.getInstance("TLS");
                //Get the keystore
                //keystore = KeyStore.getInstance("BKS");
                //Load the keystore's file using the passphrase and initialize the factory using the keys you got from the keystore
                //InputStream trustStoreStream = context.getResources().openRawResource(R.raw.server);
                if (device == 0) {
                    keystore = KeyStore.getInstance("JKS");
                    keystore.load(new FileInputStream(truststore), passphrase);
                } else {
                    keystore = KeyStore.getInstance("BKS"); //bouncy castle security library for android
                    keystore.load(trustStoreStream, passphrase);
                }
                //Load the keystore's file using the passphrase and initialize the factory using the keys you got from the keystore

                trustfactory.init(keystore);
                //initialize the context using the keys you got from TMF
                context.init(null, trustfactory.getTrustManagers(), null);
                //create the factory and then use the factory to create the socket
                SSLSocketFactory factory = context.getSocketFactory();
                sslsocket = (SSLSocket) factory.createSocket(url, port);
                System.out.println("Socket created.");
                out
                        = new PrintWriter(sslsocket.getOutputStream(), true);
                in = new BufferedReader(
                        new InputStreamReader(sslsocket.getInputStream()));
            } else {
                socket = new Socket(url, port);
                System.out.println("Socket created.");
                out
                        = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
            }
            new Reader(in).start();
            new Writer(out).start();
            conError.set(false);
            System.out.println("Created I/O stream threads.");

        } catch (Exception ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * The reader thread. Reads everything sent from the server and decides
     * whether they go in the queue or set as the answer to a command previously
     * sent.
     */
    class Reader extends Thread {

        BufferedReader in;

        Reader(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            while (!quit.get()) {

                String input;
                try {
                    while ((input = in.readLine()) != null) {

                        if (input.contains(replyCmd)) {
                            input = input.replace(replyCmd, "");
                            reply = input;
                            setFinished(true);
                            setWorking(false);
                        } else {
                            cQue.putCmd(input);
                        }

                    }

                } catch (Exception ex) {
                    System.out.println("Cannot read from the socket. Closing.");
                    conError.set(true);
                    quit.set(true);
                    try {
                        closeCommunication();
                    } catch (IOException ex1) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
            try {
                closeCommunication();
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * The writer thread. Sends commands to the server, one at a time and lets
     * the reader thread know that a command was sent.
     */
    class Writer extends Thread {

        PrintWriter out;

        Writer(PrintWriter out) {
            this.out = out;
        }

        @Override
        public void run() {
            while (!quit.get()) {
                if (query.get()) {
                    setQuery(false);
                    setWorking(true);
                    setFinished(false);
                    out.println(command);
                }
            }
        }
    }

    /**
     * Sets the atomic boolean that is responsible for keeping the threads
     * alive.
     */
    public void quitCommunication() {
        setCommand(exitCmd);
        quit.set(true);
    }
    /**
     * Closes communication with the server
     * @throws IOException 
     */
    public void closeCommunication() throws IOException {
        if(sslsocket!=null && !sslsocket.isClosed()){
            sslsocket.close();
        }
        else if (socket!=null && !socket.isClosed()){
            socket.close();
        }
        if (in!=null){
            in.close();
        }
        if (out!=null){
            out.close();
        }
        System.out.println("Closed the socket.");

    }

    /**
     * If this is true then a command is about to be sent to the server.
     *
     * @return an atomic boolean.
     */
    public Boolean hasQuery() {
        return query.get();
    }

    /**
     * Sets the atomic boolean stating whether a command is about to be sent.
     *
     * @param aQuery
     */
    public void setQuery(Boolean aQuery) {
        query.set(aQuery);
    }

    /**
     * If this is true then a command was sent and is being processed.
     *
     * @return an atomic boolean
     */
    public Boolean isWorking() {
        return working.get();
    }

    /**
     * Sets the atomic boolean that states whether a command is being processed.
     *
     * @param aWorking
     */
    public void setWorking(Boolean aWorking) {
        working.set(aWorking);
    }

    /**
     * Sets the next message to be sent and sets the query atomic boolean to
     * true if false.
     *
     * @param aCommand The command you wish to send.
     */
    public void setCommand(String aCommand) {
        Calendar cal = Calendar.getInstance();
        long time = cal.getTimeInMillis() / 1000;
        while (query.get() || working.get()) {
            if (cal.getTimeInMillis() /1000 - time >= timeOut) {
                return;
            }
        }
        command = aCommand;
        query.set(true);
    }

    /**
     * Gets the message to be sent.
     *
     * @return The message as a string.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets an atomic boolean indicating if a command or message has been
     * answered.
     *
     * @return An atomic boolean. True means it has been asnwered.
     */
    public Boolean isFinished() {
        return finished.get();
    }

    /**
     * Sets the atomic boolean indicating if a command or message has been
     * answered.
     *
     * @param aFinished The state as a boolean. True: finished
     */
    public void setFinished(Boolean aFinished) {
        finished.set(aFinished);
    }

    /**
     * Returns the reply to a message.
     *
     * @return The reply as a string.
     */
    public String getReply() {
        Calendar cal = Calendar.getInstance();
        long time = cal.getTimeInMillis() / 1000;
        while (!finished.get()){
            if (cal.getTimeInMillis() /1000 - time >= timeOut) {
                return null;
            }
        }
        finished.set(false);
        String respond = reply;
        reply = null;
        return respond;
    }
    /**
     * Returns the SimpleQueue instance that push messages are saved in.
     * @return SimpleQueue instance
     */
    public SimpleQueue getClientQueue() {
        return cQue;
    }
    /**
     * A boolean indicating if the connection is still alive
     * @return true: crashed false: alive
     */
    public Boolean getConError() {
        return conError.get();
    }
}
