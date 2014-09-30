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
 *
 * @author Sozos Assias
 */
public class Sender {



    public Sender() {
    }
    //Send method gets the message you want to send and the destination and
    //sends the message
    public String send(String msg, SSLSocket sslsocket) {
        try {
            //Creates I/O
            PrintWriter out
                    = new PrintWriter(sslsocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(sslsocket.getInputStream()));
            System.out.println("Created I/O.");
            System.out.println("Sending message.");
            //Sends message
            out.println(msg);
            System.out.println("Waiting for a reply.");
            //Waits for a reply and returns it to the caller class
            while ((msg = in.readLine()) != null) {
                System.out.println("Reply received.");
                return msg;
                //System.out.println(msg);
            }
        } catch (Exception ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
        //If an error occurs it will return null.
        return null;
    }

}
