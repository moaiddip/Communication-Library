/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

/**
 * Holds all the relevant information in regards to a query.
 * @author Sozos
 */

public class Item {
    /**
     * 
     * newOrOld: a boolean to keep track of if the query is new or old. new = true(default)
     * answered: a boolean to keep track of if the query is answered. not answered=false (default)
     * message: the query
     * reply: the answer to the query
     * synchronized methods are read/written by different threads
     */
    boolean newOrOld = true;
    boolean answered = false;//testing: true, default: false
    String message;
    String reply;
    String address;

    //sets the query of a client and the clients ip address in the items instance
    /**
     * Sets the query and ip address of an Item instance. Should be used when the instance is first created.
     * @param message Requires a String with the message.
     * @param address Requires a String with an ip address.
     */
    public void create(String message, String address) {
        this.message = message;
        this.address = address;
        if(message.contains("test")){
            answered=true;
        }
        if (answered){
            reply=message;
        }
    }

    /**
     * Checks if an item instance is answered. This action is already synchronized.
     * @return A boolean. False = Not answered True = Answered
     */
    public Boolean isAnswered() {
        synchronized (this) {
            return answered;
        }
    }
    /**
     * Returns the message/query from an item instance. Synchronization not required.
     * @return A string with a message. (Presumably from a client)
     */
    public String getMsg() {
        return message;
    }
    /**
     * Returns the answer to a query. This method is already synchronized.
     * @return A string with the answer to a query.
     */
    public String getReply() {
        synchronized (this) {
            return reply;
        }
    }
    /**
     * Makes a query old so that it can be replaced in the WriteQueue.
     * Implemented automatically, should not be called. This method is already synchronized.
     */
    public void makeOld() {
        synchronized (this) {
            newOrOld = !newOrOld;
        }
    }

    /**
     * Sets the answer for a processed query and changes the answered boolean to true.
     * This method is already synchronized.
     * @param message The answer to a query. 
     */
    public void setReply(String message) {
        synchronized (this) {
            reply = message;
            answered = true;
        }
    }
    /**
     * Returns a boolean indicating if a query is old or new.
     * A message is only set to old once the reply has been sent to the client.
     * @return A boolean, false = old; true = new;
     */
    public Boolean getState() {
        synchronized (this) {
            return newOrOld;
        }
    }
    /**
     * Returns the ip address of the client that issued the query.
     * @return A string with an ip address.
     */
    public String getAddress() {
        return address;
    }
}
