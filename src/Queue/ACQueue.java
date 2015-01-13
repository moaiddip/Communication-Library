/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//1.8
package Queue;

import java.util.concurrent.ConcurrentHashMap;
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
    public ConcurrentHashMap<Integer, String> getItems() {
        return items;
    }

    private final ConcurrentHashMap<Integer, String> items = new ConcurrentHashMap<Integer, String>();
    private final AtomicBoolean hasAddedCommands = new AtomicBoolean(false);

    /**
     * Puts a message in the queue.
     *
     * @param command The message you want to put.
     * @return Returns the message (optional).
     */
    public synchronized String putCmd(String command) {
        //looks for an old message to replace
        if (items.isEmpty()) {
            items.put(0, command);
            return items.get(0);
        } else {
            for (int i = 0; i < getItems().size(); i++) {
                if (getItems().get(i)==null){
                    getItems().put(i, command);
                    hasAddedCommands.set(true);
                    return getItems().get(i);
                }
                else if ("".equals(getItems().get(i))) { //all "empty" entries should be "".
                    getItems().put(i, command);
                    hasAddedCommands.set(true);
                    return getItems().get(i);
                }
            }
        }
        //creates new entry
        getItems().put(items.size(), command);
        hasAddedCommands.set(true);
        return getItems().get(items.size() - 1);
    }
}
