/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.security.SecureRandom;

/**
 * Used to create a single session key for a client.
 * NOT YET FINAL.
 * @author Sozos
 */
public class KeyGenerator {

    //Creates a one session only key.
    //The first byte can never be 0.
    /**
     * Generates a random byte array to be used as a single session key by the client.
     * Should be saved in the database and erased when a connection is dropped.
     * The first byte should never be zero. When the first byte is zero, the session key
     * has not been created yet, so the user hasn't logged in. 
     * @return A byte[16] array.
     */
    public byte[] returnKey() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[16];
        while (bytes[0] == 0x0) {
            random.nextBytes(bytes);
        }
        return bytes;
    }
}
