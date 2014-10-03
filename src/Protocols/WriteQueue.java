/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.util.HashMap;

/**
 *
 * @author Sozos Assias
 */
public final class WriteQueue {

    public WriteQueue() {

    }
    int hashTail = 0;
    static HashMap<Integer, Items> items = new HashMap<>();
    static HashMap<Integer, Items> secondList = new HashMap<>();
    int totalQueries = 0;
    //int[] prio = {0, 0, 0, 0};
    //int[] prioVal = {1, 2, 5, 10};

    //puts a new message in the queue
    public Items putMsg(String message, String address) {
        //looks for an old message to replace
        int placed = 0;
        for (int i = 0; i < items.size(); i++) {
            if (placed == 0) {
                if (items.get(i).getState() == false) {
                    items.get(i).create(message, address);
                    totalQueries++;
                    placed = 1;
                    prepareQueries();
                    System.out.println(i);
                    return items.get(i);
                }
            }

        }
        //creates new entry
        items.put(hashTail, new Items());
        items.get(hashTail).create(message, address);
        hashTail++;
        totalQueries++;
        prepareQueries();
        return items.get(hashTail - 1);
    }

    //returns an Items instance at the given position in the hashmap
    public Items getObject(int pos) {
        return items.get(pos);

    }
    //returns the static list, synchronized because it is accessed by multiple threads
    public HashMap returnMap() {
        synchronized (secondList) {
            return secondList;
        }
    }
    //puts the new unanswered queries in a second hashmap that is
    //handled by the other half of the queue
    //one queue is responsible for queueing the queries
    //the other is responsible for processing them
    //this is done to reduce how much the waiting time might vary
    //between processing queues
    public void prepareQueries() {
        if (totalQueries == 5) {
            System.out.println("Preparing queries to be processed.");
            totalQueries = 0;
            synchronized (secondList) {
                for (int i = 0; i < items.size(); i++) {
                    if (secondList.isEmpty()) {
                        secondList.put(secondList.size() + 1, items.get(i));
                    } else {
                        int placed = 0;
                        for (int j = 0; j < items.size(); j++) {
                            if (placed == 0) {
                                if (items.get(i).getState() == false) {
                                    secondList.replace(j, items.get(i));
                                    placed=1;
                                }
                            }
                        }
                        if (placed==0) {
                            secondList.put(secondList.size() + 1, items.get(i));
                        }
                    }
                }
                secondList.notify();
            }
        }
    }

}
