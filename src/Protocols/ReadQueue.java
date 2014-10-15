/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An example of the second half of the realtime queue implementation
 * Needs to be implemented by the Servers group.
 * Accessed by the server part that connects to the database to process queries.
 * @author Sozos
 */
public class ReadQueue {

    HashMap<Integer, Item> items;

    /**
     * Receives a reference of WriteQueue and creates a reference of the second hashmap in the WriteQueue.
     * The second hashmap is the one used to process queries.
     */
    public ReadQueue() {
        WriteQueue que = new WriteQueue();
        items = que.returnMap();
    }
    /**
     * Gets the next item on the list.
     * First in, First out implementation.
     * @return An Item instance.
     */
    public Item getNext() { //First come, first served queue; Will change to give priority.
        synchronized (items) {
            while (items.isEmpty()) {
                try {
                    items.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ReadQueue.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getState() && !items.get(i).isAnswered()) {
                    return items.get(i);

                }

            }

        }
        return null; //Error has occured.
        //que priorities: 1-Low (For scheduled tasks-reoccuring) 2-Normal 3-High 4-Realtime
    }
}
