/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

/**
 * The class used by the client to send and receive messages.
 * @author Sozos Assias
 */
public class ClientSend {



    public ClientSend() {
    }

    /**
     * Sends a message to the other end of an established connection and awaits for an answer.
     * It is recommended that this method is called from a thread other than the main thread.
     * @param message The message to send.
     * @param sslsocket An already established connection using SSLSocket.
     * @return Returns a String with the reply from the server.
     */
    public String send(String message, SSLSocket sslsocket) {
        try {
            //Creates I/O
            PrintWriter out
                    = new PrintWriter(sslsocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(sslsocket.getInputStream()));
            System.out.println("Created I/O.");
            System.out.println("Sending message.");
            //Sends message
            out.println(message);
            System.out.println("Waiting for a reply.");
            //Waits for a reply and returns it to the caller class
            while ((message = in.readLine()) != null) {
                System.out.println("Reply received.");
                return message;
                //System.out.println(msg);
            }
        } catch (Exception ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
        //If an error occurs it will return null.
        return null;
    }

}
