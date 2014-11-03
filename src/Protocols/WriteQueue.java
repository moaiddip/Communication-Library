/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

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
     * Returns an atomic boolean responsible to show if a new command was added.
     *
     * @return the hasAddedCommands
     */
    public Boolean getHasAddedCommands() {
        return hasAddedCommands.get();
    }

    /**
     * Sets the atomic boolean.
     *
     * @param aHasAddedCommands the hasAddedCommands to set
     */
    public void setHasAddedCommands(Boolean aHasAddedCommands) {
        hasAddedCommands.compareAndSet(!aHasAddedCommands, aHasAddedCommands);
    }

    long firstTime;
    long secondTime;
    int replaceInt = 1;

    public WriteQueue(long curTime) {
        this.firstTime = curTime / 1000;
        secondTime = firstTime;
        initialize();
    }
    int hashTail = 0;
    HashMap<Integer, Item> items = new HashMap<>();
    HashMap<Integer, Item> secondList = new HashMap<>();
    private final AtomicBoolean hasAddedCommands = new AtomicBoolean(false);
    int totalQueries = 0;
    Calendar cal;

    public void initialize() {
        Replace replace = new Replace();
        replace.start();
        CalculateTime time = new CalculateTime();
        time.start();
        
    }

    //puts a new message in the queue
    /**
     * Creates an instance of the Item class, puts the message and ip address of
     * the client that queried the message in the instance created, then it puts
     * the instance in a hashmap.
     *
     * @param message The query in a string.
     * @param address The ip address of the client with the query.
     * @param userPrio The priority of the user who issued the command.
     * @return Returns the instance of the Item class created.
     */
    public synchronized Item putMsg(String message, String address, int userPrio) {
        //looks for an old message to replace
        System.out.println("Putting command in queue: " + message + " from: " + address + " with prio: " + userPrio);
        cal = Calendar.getInstance();
        firstTime = secondTime;
        secondTime = cal.getTimeInMillis() / 1000;
        if (firstTime - secondTime < 3 && firstTime != secondTime) {
            replaceInt++;
            System.out.println("Current replaceInt:" + replaceInt);
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getState() == false) {
                items.get(i).create(message, address, userPrio);
                totalQueries++;
                System.out.println("Total queries: " + totalQueries);
                return items.get(i);
            }
        }
        //creates new entry
        items.put(hashTail, new Item());
        items.get(hashTail).create(message, address, userPrio);
        hashTail++;
        totalQueries++;
        System.out.println("Total queries: " + totalQueries);
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
    public synchronized HashMap returnMap() {
        return secondList;
    }

    /**
     * Calculates when should the queries be copied onto the second hashmap to
     * begin processing.
     */
    public class CalculateTime extends Thread {

        @Override
        public synchronized void run() {
            while (true) {
                Calendar cal2 = Calendar.getInstance();
                long past = cal2.getTimeInMillis() / 1000;

                if (((secondTime - past) % 3 < 1) && replaceInt > 1) {
                    replaceInt--;
                    System.out.println("Current replaceInt:" + replaceInt);
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
        public synchronized void run() {
            while (true) {
                
                try {
                    this.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(WriteQueue.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (totalQueries == replaceInt) {
                    System.out.println("Putting commands in the ReadQueue.");
                    totalQueries = 0;
                    for (int i = 0; i < items.size(); i++) {
                        if (secondList.isEmpty()) {
                            secondList.put(0, items.get(i));
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
                    hasAddedCommands.set(true);
                }
            }
        }

    }
}
