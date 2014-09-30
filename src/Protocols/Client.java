/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author Sozos Assias
 */
public class Client {
    //Creates and returns the socket
    public SSLSocket createSocket(String url, int port, String truststore, String trustpass) {

        String string;
        try {
            System.out.println("Creating socket.");
            SSLContext ctx;
            KeyStore ks;
            //Passphrase = the password to the truststore
            char[] passphrase = trustpass.toCharArray();
            //Get TrustManagerFactory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            //Set security to TLS
            ctx = SSLContext.getInstance("TLS");
            //Get the keystore
            ks = KeyStore.getInstance("JKS");
            //Load the keystore's file using the passphrase and initialize the factory using the keys you got from the keystore
            ks.load(new FileInputStream(truststore), passphrase);
            tmf.init(ks);
            //initialize the context using the keys you got from TMF
            ctx.init(null, tmf.getTrustManagers(), null);
            //create the factory and then use the factory to create the socket
            SSLSocketFactory factory = ctx.getSocketFactory();
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
