/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 *
 * @author Sozos Assias
 */
public class Server extends Thread {

    int port;
    int locality=0;
    String keystore;
    String keystorePass;
    String keypass;
    //The main server, creates the serversocket and then create a multithreaded
    //listener, needs a port, an int indicating if the server should only be run locally
    //the name of the keystore, the pass of the keystore and the pass of the key
    public Server(int port, int locality, String keystore, String keystorePass, String keypass) {
        this.port = port;
        this.locality=locality;
        this.keypass=keypass;
        this.keystore=keystore;
        this.keystorePass=keystorePass;
    }

    @Override
    public void run() {
        boolean listening = true;
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
            SSLServerSocketFactory socketfactory = context.getServerSocketFactory();   
            SSLServerSocket sslserversocket;
            if (locality==0){
            sslserversocket
                    = (SSLServerSocket) socketfactory.createServerSocket(port);
            }
            else{
                sslserversocket
                    = (SSLServerSocket) socketfactory.createServerSocket(port,0, InetAddress.getByName(null));
            }
            System.out.println("Server Socket created. Listening.");
            //Creates the que and listens to the socket.
            WriteQueue que = new WriteQueue();
            while (listening) {
                new Communication((SSLSocket)sslserversocket.accept(), que).start();
            }
        } catch (Exception ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
