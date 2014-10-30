/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import static Protocols.SerialClass.input;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * This is the class used to create an SSLSocket for the client.
 *
 * @author Sozos Assias
 */
public class Client extends Thread {

    
    int phone;
    String url;
    int port;
    String truststore;
    String trustpass;
    InputStream trustStore;
    static String message="no_command";
    static String reply;
    private static AtomicBoolean query = new AtomicBoolean(false);

    private AtomicBoolean quit = new AtomicBoolean(false);
    private static AtomicBoolean working = new AtomicBoolean(false);

    public Client(String url, int port, String truststore, String trustpass) {
        phone = 0;
        this.url = url;
        this.port = port;
        this.truststore = truststore;
        this.trustpass = trustpass;
    }

    public Client(String url, int port, InputStream truststore, String trustpass) {
        phone = 1;
        this.url = url;
        this.port = port;
        this.trustStore = truststore;
        this.trustpass = trustpass;
    }

    /**
     * Creates and returns an SSLSocket.
     *
     * @return An SSLSocket or null in case of an error.
     */
    @Override
    public void run(){
        String string;
        try {
            System.out.println("Creating socket.");
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
            if (phone == 0) {
                keystore = KeyStore.getInstance("JKS");
                keystore.load(new FileInputStream(truststore), passphrase);
            } else {
                keystore = KeyStore.getInstance("BKS");
                keystore.load(trustStore, passphrase);
            }
            //Load the keystore's file using the passphrase and initialize the factory using the keys you got from the keystore
            
            trustfactory.init(keystore);
            //initialize the context using the keys you got from TMF
            context.init(null, trustfactory.getTrustManagers(), null);
            //create the factory and then use the factory to create the socket
            SSLSocketFactory factory = context.getSocketFactory();
            SSLSocket sslsocket = (SSLSocket) factory.createSocket(url, port);
            System.out.println("Socket created.");
            PrintWriter out
                    = new PrintWriter(sslsocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(sslsocket.getInputStream()));
            new Reader(in).start();
            new Writer(out).start();
            
        } catch (Exception ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    class Reader extends Thread{
        BufferedReader in;
        Reader(BufferedReader in){
            this.in=in;
        }
        @Override
        public void run(){
            while(!getQuit().get()){
                try {
                ACQueue cQue =new ACQueue();
                String inputz;
                while ((inputz = input.readLine()) != null) {
                    
                    System.out.println("Received: " + inputz);

                    String[] parts = inputz.split("_");
                    String[] parts2 = message.split("_");
                    if ((parts[0].equals(parts2[0])|| parts[0].equals("error"))) {
                        reply=inputz;
                        message="no_command";
                        setWorking(false);
                    }
                    else{
                        cQue.putMsg(inputz);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        }
    }
    class Writer extends Thread{
        PrintWriter out;
        Writer(PrintWriter out){
            this.out=out;
        }
        @Override
        public void run(){
            while(!getQuit().get()){
                if(query.get()){
                    System.out.println(query.get());
                    setQuery(false);
                    System.out.println(query.get());
                    setWorking(true);
                    out.println(message);
                }
            }
            
        }
    }
    public AtomicBoolean getQuit() {
        return quit;
    }

    public void setQuit(Boolean aQuit) {
        quit.compareAndSet(!aQuit, aQuit);
    }
    public static AtomicBoolean getQuery() {
        return query;
    }

    public static void setQuery(Boolean aQuery) {
        query.compareAndSet(!aQuery, aQuery);
    }

    public static AtomicBoolean getWorking() {
        return working;
    }

    public static void setWorking(Boolean aWorking) {
        working.compareAndSet(!aWorking, aWorking);
    }
    public void setMessage(String aMessage) {
        message = aMessage;
        query.compareAndSet(false, true);
    }
    public static String getMessage(){
        return message;
    }
}
