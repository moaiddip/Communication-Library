/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

/**
 *
 * @author Sozos Assias
 */
public class Communication extends Thread {

    SSLSocket sslsocket;
    WriteQueue que;

    //Communication between a client and the server happens here
    //Gets the socket linked to that client and the queue from the main server class
    public Communication(SSLSocket sslsocket, WriteQueue que) {
        this.sslsocket = sslsocket;
        this.que = que;
    }

    @Override
    public void run() {

        Boolean listener = true;
        try {
            //Create I/O for the socket
            PrintWriter out
                    = new PrintWriter(sslsocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(sslsocket.getInputStream()));
            System.out.println("Initialized I/O.");
            String string;
            //Gets the ip address of the client.
            String remoteSocketAddress = sslsocket.getRemoteSocketAddress().toString();
            //System.out.println(remoteSocketAddress);
            
            //Listens in a loop until asked to exit
            while (listener) {
                //Get string from the input stream
                while ((string = in.readLine()) != null) {
                    System.out.println("Received query.");
                    //exit is the quit command, replies with ok

                    if ("exit".equals(string)) {
                        listener = false;
                        out.println("ok");
                    } else {
                        //Puts message in queue and waits until the message is
                        //Answered, then it sends the answer back to the client

                        System.out.println("Putting query in queue.");
                        Items object;
                        synchronized (que) {
                            object = que.putMsg(string, remoteSocketAddress);
                        }
                        synchronized (object) {
                            while (object.isAnswered() == false) {
                                object.wait();
                            }
                        }
                        System.out.println("Answer processed, preparing to reply.");

                        string = object.getReply();
                        

                        System.out.println("Replying.");
                        out.println(string);
                        object.makeOld();
                    }

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sslsocket.close();
            } catch (IOException ex) {
                Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
