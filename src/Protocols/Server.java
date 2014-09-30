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
    //listener, needs a port
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
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keystore), passphrase);
            //KeyManagerFactory initiated to get the key from the keystore using the key's password
            KeyManagerFactory kmf
                    = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keypass.toCharArray());
            //Set security to TSL and initiate using the key we just recovered
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);
            //Create a socket factory and then create a socket using the factory
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();   
            SSLServerSocket sslserversocket;
            if (locality==0){
            sslserversocket
                    = (SSLServerSocket) ssf.createServerSocket(port);
            }
            else{
                sslserversocket
                    = (SSLServerSocket) ssf.createServerSocket(port,0, InetAddress.getByName(null));
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
