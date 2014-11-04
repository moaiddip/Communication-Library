/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

/**
 * Holds all the relevant information in regards to a query.
 *
 * @author Sozos
 */
public class Item {

    /**
     *
     * newOrOld: a boolean to keep track of if the query is new or old. new =
     * true(default) answered: a boolean to keep track of if the query is
     * answered. not answered=false (default) message: the query reply: the
     * answer to the query synchronized methods are read/written by different
     * threads
     */
    boolean newOrOld = true;
    boolean answered = false;//testing: true, default: false
    String message;
    String reply;
    String address;
    private String user = null;
    private int userPrio = -1;
    private int priority = -1;

    //sets the query of a client and the clients ip address in the items instance
    /**
     * Sets the query and ip address of an Item instance. Should be used when
     * the instance is first created.
     *
     * @param message Requires a String with the message.
     * @param address Requires a String with an ip address.
     * @param userPrio
     */
    public void create(String message, String address, int userPrio) {
        this.message = message;
        this.address = address;
        this.userPrio = userPrio;
        System.out.println(message+" received in the Item class.");
        if (message.contains("test")) {
            answered = true;
        }
        if (answered) {
            reply = message;
        }
    }

    /**
     * Checks if an item instance is answered. This action is already
     * synchronized.
     *
     * @return A boolean. False = Not answered True = Answered
     */
    public synchronized Boolean isAnswered() {
            return answered;
    }

    /**
     * Returns the message/query from an item instance. Synchronization not
     * required.
     *
     * @return A string with a message. (Presumably from a client)
     */
    public String getMsg() {
        return message;
    }

    /**
     * Returns the answer to a query. This method is already synchronized.
     *
     * @return A string with the answer to a query.
     */
    public synchronized String getReply() {
            return reply;
    }

    /**
     * Makes a query old so that it can be replaced in the WriteQueue.
     * Implemented automatically, should not be called. This method is already
     * synchronized.
     */
    public synchronized void makeOld() {
            newOrOld = !newOrOld;
    }

    /**
     * Sets the answer for a processed query and changes the answered boolean to
     * true. This method is already synchronized.
     *
     * @param message The answer to a query.
     */
    public synchronized void setReply(String message) {
            reply = message;
            answered = true;
            notify();
    }

    /**
     * Returns a boolean indicating if a query is old or new. A message is only
     * set to old once the reply has been sent to the client.
     *
     * @return A boolean, false = old; true = new;
     */
    public synchronized Boolean getState() {
            return newOrOld;
    }

    /**
     * Returns the ip address of the client that issued the query.
     *
     * @return A string with an ip address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the priority of the user.
     *
     * @return the userPrio
     */
    public synchronized int getUserPrio() {
            return userPrio;
    }

    /**
     * Sets the priority of a user. User priority needs to be set by the server
     * when a user logs in.
     *
     * @param userPrio the userPrio to set
     */
    public synchronized void setUserPrio(int userPrio) {
            this.userPrio = userPrio;
    }

    /**
     * Returns the priority of the command.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority of the command.
     *
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Returns the user that issued this command.
     *
     * @return the user
     */
    public synchronized String getUser() {
            return user;
    }

    /**
     * Sets the user that issued this command. User needs to be set by the
     * server when a user logs in.
     *
     * @param user the user to set
     */
    public synchronized void setUser(String user) {
            this.user = user;
    }
}
