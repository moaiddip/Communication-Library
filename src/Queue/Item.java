/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Queue;

/**
 * Holds all the relevant information in regards to a query.
 *
 * @author Sozos
 */
public class Item {

    /**
     *
     * newOrOld: a boolean to keep track of if the query is new or old. new =
 true(default) answered: a boolean to keep track of if the query is
 answered. not answered=false (default) command: the query reply: the
 answer to the query synchronized methods are read/written by different
 threads
     */
    private boolean newOrOld = true;
    private boolean answered = false;//testing: true, default: false
    private String command;
    private String reply;
    private String address;
    private String user = null;
    private int userPrio = -1;
    private int priority = 0;
    private String authsessionkey;
    private String sessionkey;
    private int aging=0;
    private boolean copied = false;

    //sets the query of a client and the clients ip address in the items instance
    /**
     * Sets the query and ip address of an Item instance. Should be used when
     * the instance is first created.
     *
     * @param command Requires a String with the command.
     * @param address Requires a String with an ip address.
     * @param userPrio
     */
    public void create(String command, String address, int userPrio) {
        this.command = command;
        this.address = address;
        this.userPrio = userPrio;
//       
        System.out.println(command+" received in the Item class.");
        if (command.contains("test")) {
            answered = true;
            reply = command;
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
     * Returns the command/query from an item instance. Synchronization not
     * required.
     *
     * @return A string with a command. (Presumably from a client)
     */
    public String getCmd() {
        return command;
    }
    
    /**
     * gets the age for priority queues that use aging
     * 
     * 
     */
    public synchronized int  getAging() {
        return aging;
    }
    /**
     * sets the age for priority queues that use aging
     * 
     * @param aging an int
     */
    public synchronized void setAging(int aging) {
       this.aging = aging;
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
     * Returns a boolean indicating if a query is old or new. A command is only
 set to old once the reply has been sent to the client.
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
    
    
    public synchronized void setAuthSessionKey(String authsessionkey) {
            this.authsessionkey = authsessionkey;
    }
    
    
   public synchronized String getAuthSessionKey() {
            return authsessionkey;
    }
   
   public synchronized void setSessionKey(String sessionkey) {
            this.sessionkey = sessionkey;
    }
    
    
   public synchronized String getSessionKey() {
            return sessionkey;
    }
   public Boolean isCopied(){
       return copied;
   }
   public void setCopied(boolean copied){
       this.copied=copied;
   }
}
