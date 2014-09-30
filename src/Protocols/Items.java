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
public class Items {
    boolean newOrOld=true;
    boolean answered=false;//testing: true, default: false
    String message;
    String reply;
    public void create(String message){
        this.message=message;
    }
    public synchronized Boolean isAnswered(){
        return answered;
    }
    public String getMsg(){
        return message;
    }
    public synchronized String getReply(){
        return reply;
    }
    public synchronized void makeOld(){
        newOrOld=!newOrOld;
    }
    public synchronized void putReply(String message){
        reply=message;
        answered=true;
    }
    public synchronized Boolean getState(){
        return newOrOld;
    }
}
