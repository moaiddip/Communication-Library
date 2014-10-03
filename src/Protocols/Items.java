/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

/**
 *
 * @author Sozos
 */
//Holds all the relevant information regarding a query.
//newOrOld: a boolean to keep track of if the query is new or old. new = true(default)
//answered: a boolean to keep track of if the query is answered. not answered=false (default)
//message: the query
//reply: the answer to the query
//synchronized methods are read/written by different threads
public class Items {

    boolean newOrOld = true;
    boolean answered = false;//testing: true, default: false
    String message;
    String reply;
    String address;

    //sets the query of a client and the clients ip address in the items instance

    public void create(String message, String address) {
        this.message = message;
        this.address = address;
    }

    //checks if the query is answered, synchronized because it can be accessed
    //and changed by multiple threads
    
    public Boolean isAnswered() {
        synchronized (this) {
            return answered;
        }
    }

    //Gets the query

    public String getMsg() {
        return message;
    }

    //Gets the answer to the query

    public String getReply() {
        synchronized (this) {
            return reply;
        }
    }

    //Makes a new query old so that it can be replaced in the hashmap

    public void makeOld() {
        synchronized (this) {
            newOrOld = !newOrOld;
        }
    }

    //sets the reply message and sets the answered boolean to true (answered)

    public void setReply(String message) {
        synchronized (this) {
            reply = message;
            answered = true;
        }
    }

    //returns if the message is new or old
    //a message is only set to old once the reply has been sent back
    //to the client ONLY

    public Boolean getState() {
        synchronized (this) {
            return newOrOld;
        }
    }

    //returns the ip address of the client who issued the query

    public String getAddress() {
        return address;
    }
}
