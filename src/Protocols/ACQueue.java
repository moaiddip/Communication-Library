/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The queue used by the Arduino and the Client. Even though some things are
 * static, because the Arduino and the client are not supposed to be used On the
 * same project at the same time, it does not matter.
 *
 * @author Sozos
 */
public class ACQueue {

    /**
     * Returns a boolean showing whether a new command was added to the queue.
     *
     * @return the hasAddedCommands
     */
    public Boolean getHasAddedCommands() {
        return hasAddedCommands.get();
    }

    /**
     * Sets the boolean showing whether a new command was added to the queue.
     *
     * @param aHasAddedCommands the hasAddedCommands to set
     */
    public void setHasAddedCommands(Boolean aHasAddedCommands) {
        hasAddedCommands.set(aHasAddedCommands);
    }

    /**
     * Returns the list with all the commands queued by the arduino to the
     * server or by the server to the client.
     *
     * @return the items
     */
    public synchronized HashMap<Integer, String> getItems() {
        return items;
    }
    int hashTail = 0;
    private final HashMap<Integer, String> items = new HashMap<>();
    private final AtomicBoolean hasAddedCommands = new AtomicBoolean(false);

    /**
     * Puts a message in the queue.
     *
     * @param message The message you want to put.
     * @return Returns the message (optional).
     */
    public synchronized String putMsg(String message) {
        //looks for an old message to replace
        for (int i = 0; i < getItems().size(); i++) {
            if (getItems().get(i) == null) {
                getItems().replace(i, message);
                System.out.println("Replacing command in the Arduino/Client queue");
                hasAddedCommands.set(true);
                return getItems().get(i);
            }
        }
        //creates new entry
        getItems().put(hashTail, message);
        hashTail++;
        System.out.println("Putting new command in the Arduino/Client queue");
        hasAddedCommands.set(true);
        return getItems().get(hashTail - 1);
    }
}
