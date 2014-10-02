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
 *
 * @author Sozos
 */
public class ReadQueue {

    HashMap<Integer, Items> items;

    //Gets the reference of the static queue from the other half of the queue implementation
    public ReadQueue() {
        WriteQueue que = new WriteQueue();
        items = que.returnMap();
    }

    public Items getNext() { //First come, first served queue; Will change to give priority.
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
