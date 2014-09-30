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
    int count = 0;
    static HashMap<Integer, Items> items = new HashMap<>();
    static HashMap<Integer, Items> secondList = new HashMap<>();
    int more = 0;
    //int[] prio = {0, 0, 0, 0};
    //int[] prioVal = {1, 2, 5, 10};

    //puts a new message in the queue
    public Items putMsg(String msg) {
        //looks for an old message to replace
        int found = 0;
        for (int i = 0; i < items.size(); i++) {
            if (found == 0) {
                if (items.get(i).getState() == false) {
                    items.get(i).create(msg);
                    more++;
                    found = 1;
                    replace();
                    System.out.println(i);
                    return items.get(i);
                }
            }

        }
        //creates new entry
        items.put(count, new Items());
        items.get(count).create(msg);
        count++;
        more++;
        replace();
        return items.get(count - 1);
    }

    //checks if a message at position pos was processed.
    public Items getObject(int pos) {
        return items.get(pos);

    }

    public HashMap returnMap() {
        synchronized (secondList) {
            return secondList;
        }
    }

    public void replace() {
        if (more == 5) {
            System.out.println("Duplicating list.");
            more = 0;
            synchronized (secondList) {
                for (int i = 0; i < items.size(); i++) {
                    Boolean done = false;
                    if (secondList.isEmpty()) {
                        secondList.put(secondList.size() + 1, items.get(i));
                    } else {
                        int found = 0;
                        for (int j = 0; j < items.size(); j++) {
                            if (found == 0) {
                                if (items.get(i).getState() == false) {
                                    secondList.replace(j, items.get(i));
                                    done = true;
                                    found=1;
                                }
                            }
                        }

                        if (!done) {
                            secondList.put(secondList.size() + 1, items.get(i));
                        }
                    }
                }
            }
        }
    }

}
