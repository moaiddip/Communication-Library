/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Accessed by the Communication class to query a command. The first half of the
 * realtime queue.
 *
 * Implemented automatically. Only the method ReturnMap() should be used.
 *
 * @author Sozos Assias
 */
public final class WriteQueue {

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

    long firstTime;
    long secondTime;
    int replaceInt = 1;

    public WriteQueue(long curTime) {
        this.firstTime = curTime / 1000;
        secondTime = firstTime;
        CalculateTime time = new CalculateTime();
        time.start();
        Replace replace = new Replace();
        replace.start();
    }
    int hashTail = 0;
    static HashMap<Integer, Item> items = new HashMap<>();
    static HashMap<Integer, Item> secondList = new HashMap<>();
    private static AtomicBoolean hasAddedCommands = new AtomicBoolean(false);
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

        synchronized (items) {
            cal = Calendar.getInstance();
            firstTime = secondTime;
            secondTime = cal.getTimeInMillis() / 1000;
            int placed = 0;
            if (firstTime - secondTime < 3 && firstTime != secondTime) {
                replaceInt++;
                System.out.println("Current replaceInt:" + replaceInt);
            }
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
     * Calculates when should the queries be copied onto the second hashmap to
     * begin processing.
     */
    public class CalculateTime extends Thread {

        @Override
        public void run() {
            while (true) {
                synchronized (items) {
                    Calendar cal2 = Calendar.getInstance();
                    long past = cal2.getTimeInMillis() / 1000;

                    if (((secondTime - past) % 3 < 1) && replaceInt > 1) {
                        replaceInt--;
                        System.out.println("Current replaceInt:" + replaceInt);
                    }
                }
            }
        }
    }

    /**
     * Puts the new unaswered queries in the second hashmap that should be
     * handled by the second half of the realtime queue. Reduces how much the
     * waiting time for an answer might vary. Should not be called.
     */
    public class Replace extends Thread {

        @Override
        public void run() {
            while (true) {
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
                            getHasAddedCommands().set(true);
                            secondList.notify();
                        }
                    }
                }
            }
        }

    }
}
