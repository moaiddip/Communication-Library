/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Laying the groundwork
 * @author Sozos
 */
public class ACQueue {

    /**
     * @return the hasAddedCommands
     */
    public static AtomicBoolean getHasAddedCommands() {
        return hasAddedCommands;
    }

    /**
     * @param aHasAddedCommands the hasAddedCommands to set
     */
    public static void setHasAddedCommands(AtomicBoolean aHasAddedCommands) {
        hasAddedCommands = aHasAddedCommands;
    }

    /**
     * @return the items
     */
    public static HashMap<Integer, String> getItems() {
        return items;
    }
    int hashTail = 0;
    private static HashMap<Integer, String> items = new HashMap<>();
    private static AtomicBoolean hasAddedCommands = new AtomicBoolean(false);
    public String putMsg(String message) {
        //looks for an old message to replace
        synchronized (getItems()) {
            int placed = 0;
            for (int i = 0; i < getItems().size(); i++) {
                if (placed == 0) {
                    if (getItems().get(i)== null) {
                        getItems().replace(i, message);
                        placed = 1;
                        System.out.println("Replacing command in the Arduino/Client queue");
                        getHasAddedCommands().set(true);
                        return getItems().get(i);
                    }
                }

            }
            //creates new entry
            getItems().put(hashTail, message);
            hashTail++;
            System.out.println("Putting new command in the Arduino/Client queue");
            getHasAddedCommands().set(true);
            return getItems().get(hashTail - 1);
        }
    }
}
