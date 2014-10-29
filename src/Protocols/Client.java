/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
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
public class Client {

    int phone;
    String url;
    int port;
    String truststore;
    String trustpass;
    InputStream trustStore;

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
    public SSLSocket createSocket() {

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
            return sslsocket;
        } catch (Exception ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
        //If an error occurs, it returns null
        return null;
    }
}
