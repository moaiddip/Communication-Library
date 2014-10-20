/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Accessed by the Communication class to query a command. The first half of the
 * realtime queue.
 *
 * Implemented automatically. Only the method ReturnMap() should be used.
 *
 * @author Sozos Assias
 */
public final class WriteQueue {

    long firstTime;
    long secondTime;
    int replaceInt=1;

    public WriteQueue(long curTime) {
        this.firstTime = curTime/1000;
        secondTime = firstTime;
        CalculateTime time = new CalculateTime();
        time.run();
        Replace replace = new Replace();
        replace.run();
    }
    int hashTail = 0;
    static HashMap<Integer, Item> items = new HashMap<>();
    static HashMap<Integer, Item> secondList = new HashMap<>();
    int totalQueries = 0;
    Calendar cal;

    //puts a new message in the queue

    /**
     * Creates an instance of the Item class, puts the message and ip address of
     * the client that queried the message in the instance created, then it puts
     * the instance in a hashmap. When 5 (NOT FINAL, NEEDS STRESS TESTING) Item
     * class instances are created, the entries are copied into another hashmap
     * to be used by the Server team to process the queries. Implemented
     * automatically, should NOT be called.
     *
     * @param message The query in a string.
     * @param address The ip address of the client with the query.
     * @return Returns the instance of the Item class created.
     */
    public Item putMsg(String message, String address) {
        //looks for an old message to replace
        cal = Calendar.getInstance();
        firstTime = secondTime;
        secondTime = cal.getTimeInMillis()/1000;
        int placed = 0;
        for (int i = 0; i < items.size(); i++) {
            if (placed == 0) {
                if (items.get(i).getState() == false) {
                    items.get(i).create(message, address);
                    totalQueries++;
                    placed = 1;
                    //prepareQueries();
                    System.out.println(i);
                    return items.get(i);
                }
            }

        }
        //creates new entry
        items.put(hashTail, new Item());
        items.get(hashTail).create(message, address);
        hashTail++;
        totalQueries++;
        //prepareQueries();
        return items.get(hashTail - 1);
    }

    /**
     * Returns an Item instance at a given position in the hashmap. Should not
     * be called.
     *
     * @param pos The position in the hashmap as an int.
     * @return An Item class instance.
     */
    public Item getObject(int pos) {
        return items.get(pos);

    }

    /**
     * Returns the static hashmap that has the queries that need to be
     * processed. Should be used by the Servers group. The method is already
     * synchronized.
     *
     * @return A hashmap(Integer, Item).
     */
    public HashMap returnMap() {
        synchronized (secondList) {
            return secondList;
        }
    }

    /**
     * Puts the new unaswered queries in the second hashmap that should be
     * handled by the second half of the realtime queue. Reduces how much the
     * waiting time for an answer might vary. Should not be called.
     */
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
                                    placed = 1;
                                }
                            }
                        }
                        if (placed == 0) {
                            secondList.put(secondList.size() + 1, items.get(i));
                        }
                    }
                }
                secondList.notify();
            }
        }
    }

    public class CalculateTime implements Runnable {

        @Override
        public void run() {
            while (true) {
                Calendar cal2 = Calendar.getInstance();
                long past = cal2.getTimeInMillis()/1000;

                if (((secondTime - past) % 3 <1) && replaceInt > 1) {
                    replaceInt--;
                }
                else if (firstTime - secondTime < 3 && firstTime != secondTime) {
                    replaceInt++;
                }
            }
        }
    }

    public class Replace implements Runnable {

        @Override
        public void run() {
            synchronized (items) {
                if (totalQueries == replaceInt) {
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
                                            placed = 1;
                                        }
                                    }
                                }
                                if (placed == 0) {
                                    secondList.put(secondList.size() + 1, items.get(i));
                                }
                            }
                        }
                        secondList.notify();
                    }
                }
            }
        }
    }

}
