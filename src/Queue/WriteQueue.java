/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Queue;

import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
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
        hasAddedCommands.set(aHasAddedCommands);
    }

    private long firstTime;
    private long secondTime;
    private int replaceInt = 1;

    public WriteQueue(long curTime) {
        this.firstTime = curTime / 1000;
        secondTime = firstTime;
        initialize();
    }
    private final ConcurrentHashMap<Integer, Item> items = new ConcurrentHashMap<Integer, Item>();
    private final ConcurrentHashMap<Integer, Item> secondList = new ConcurrentHashMap<Integer, Item>();
    private final AtomicBoolean hasAddedCommands = new AtomicBoolean(false);
    private int totalQueries = 0;
    private Calendar cal;
    private long refresh = 3;

    public void initialize() {
        DynamicFun dFun = new DynamicFun();
        dFun.start();
        //CalculateTime time = new CalculateTime();
        //time.start();

    }

    //puts a new message in the queue
    /**
     * Creates an instance of the Item class, puts the command and ip address of
     * the client that queried the command in the instance created, then it puts
     * the instance in a hashmap.
     *
     * @param command The query in a string.
     * @param address The ip address of the client with the query.
     * @param userPrio The priority of the user who issued the command.
     * @return Returns the instance of the Item class created.
     */
    public synchronized Item putCmd(String command, String address, int userPrio) {
        //looks for an old command to replace
        System.out.println("Putting command in queue: " + command + " from: " + address + " with prio: " + userPrio);
        cal = Calendar.getInstance();
        firstTime = secondTime;
        secondTime = cal.getTimeInMillis() / 1000;
        if (firstTime - secondTime < refresh && firstTime != secondTime) {
            replaceInt++;
            System.out.println("Current replaceInt:" + replaceInt);
        }
        if (items.isEmpty()) {
            items.put(0, new Item());
            items.get(0).create(command, address, userPrio);
            totalQueries++;
            System.out.println("A command has been added at position:" + 0);
            return items.get(0);
        } else {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) == null) {
                    items.put(i, new Item());
                    items.get(i).create(command, address, userPrio);
                    totalQueries++;
                    System.out.println("A command has been added at position:" + i);
                    System.out.println("Total queries: " + totalQueries);
                    return items.get(i);
                } else if (items.get(i).getState() == false) {
                    items.put(i, new Item());
                    items.get(i).create(command, address, userPrio);
                    totalQueries++;
                    System.out.println("A command has been replaced at position:" + i);
                    System.out.println("Total queries: " + totalQueries);
                    return items.get(i);
                } else if (items.get(i).getCmd()==null){
                    items.get(i).create(command, address, userPrio);
                    totalQueries++;
                    System.out.println("A command has been replaced at position:" + i);
                    System.out.println("Total queries: " + totalQueries);
                    return items.get(i);
                }
            }
        }
        items.put(items.size(), new Item());
        items.get(items.size() - 1).create(command, address, userPrio);
        totalQueries++;
        System.out.println("A command has been added at position:" + (items.size() - 1));
        System.out.println("Total queries: " + totalQueries);
        return items.get(items.size() - 1);
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
    public ConcurrentHashMap getMap() {
        return secondList;
    }

    /**
     * Puts the new unaswered queries in the second hashmap that should be
     * handled by the second half of the realtime queue. Reduces how much the
     * waiting time for an answer might vary. Should not be called.
     */
    public class DynamicFun extends Thread {

        @Override
        public synchronized void run() {
            while (true) {
                checkState();
            }
        }
    }
    public synchronized void checkState(){
                if (totalQueries >= replaceInt) {
                    totalQueries = 0;
                    replace();
                }
                else{
                    calcTime();
                }
    }

    private synchronized void replace() {

            System.out.println("Putting commands in the ReadQueue.");            
            int copied = 0;
            for (int i = 0; i < items.size(); i++) {
                if (secondList.isEmpty()) {
                    secondList.put(0, items.get(i));
                    items.put(i, new Item());
                    System.out.println("Added in ReadQueue at: " + 0);
                    copied++;
                } else {
                    boolean foundSpot = false;
                    for (int j = 0; j < secondList.size(); j++) {
                        if (secondList.get(j) == null) {
                            System.out.println("Added in ReadQueue at: " + j);
                            secondList.put(j, items.get(i));
                            items.put(i, new Item());
                            foundSpot = true;
                            copied++;
                        } else if (secondList.get(j).getState() == false) {
                            System.out.println("Replaced in ReadQueue at: " + j);
                            secondList.put(j, items.get(i));
                            items.put(i, new Item());
                            foundSpot = true;
                            copied++;
                        }
                    }
                    if (!foundSpot) {
                        System.out.println("Item number: " + secondList.size() + " is false.");
                        System.out.println("Added in ReadQueue at: " + secondList.size());
                        secondList.put(secondList.size(), items.get(i));
                        items.put(i, new Item());
                        copied++;
                    }
                }

            }
            System.out.println(copied+" commands have been added in the ReadQueue. ReadQueue's current size: "+secondList.size());
            System.out.println("Note: You should strive to copy as many commands as possible without any previous commands still in the ReadQueue.");
            hasAddedCommands.set(true);
        
    }

    private synchronized void calcTime() {
        Calendar cal2 = Calendar.getInstance();
        long past = cal2.getTimeInMillis() / 1000;

        if (((secondTime - past) / refresh < 1) && replaceInt > 1) {
            replaceInt--;
            System.out.println("Current replaceInt:" + replaceInt);
        }
    }

    /**
     * Gets this thing
     *
     * @return long
     */
    public synchronized long getRefreshRate() {
        return refresh;
    }

    /**
     * Returns this thing.
     *
     * @param refresh long
     */
    public synchronized void setRefreshRate(long refresh) {
        this.refresh = refresh;
    }
}
