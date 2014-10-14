/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.security.SecureRandom;

/**
 *
 * @author Sozos
 */
public class KeyGenerator {

    //Creates a one session only key.
    //The first byte can never be 0.

    public byte[] returnKey() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[16];
        while (bytes[0] == 0x0) {
            random.nextBytes(bytes);
        }
        return bytes;
    }
}
