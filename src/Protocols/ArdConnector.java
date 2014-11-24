/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import gnu.io.CommPortIdentifier;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the arduino server that sends commands to the arduino and waits for a
 * response.
 *
 * @author Sozos Assias
 */
public class ArdConnector extends Thread {

    ACQueue ac = new ACQueue();
    public String port = "COM3";
    private String reply = null;
    private String command = "no_command!";
    private final AtomicBoolean query = new AtomicBoolean(false);
    private final AtomicBoolean quit = new AtomicBoolean(false);
    private final AtomicBoolean working = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private boolean problem=true;
    private String divider = "_";
    private String defaultCommand = "no_command!";
    /**
     * Sets the port that the software will communicate with.
     *
     * @param port The port name as a string. eg. "COM3" (which is the default
     * on my pc)
     * @param divider Used for syntax
     * @param defaultCommand Restricted command
     */
    public ArdConnector(String port, String divider, String defaultCommand){
        this.port = port;
        this.divider = divider;
        this.defaultCommand = defaultCommand;
        command = defaultCommand;
        
    }

    /**
     * Initializes the connection and waits for a command, sends the command and
     * waits for an answer. If there is no answer, it sends the command once
     * again. If there is no answer again, it sets the response to no_answer.
     */
    @Override
    public void run() {
        try {
            SerialClass obj = new SerialClass();
            

            while (!quit.get()) {
                CommPortIdentifier portId = null;
                Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
                while (portEnum.hasMoreElements()) {
                    CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                    if (currPortId.getName().equals(port)) {
                        if(problem){                            
                            obj.initialize(this, ac, currPortId, divider);
                            ArdConnector.sleep(2000);
                        }
                        problem=false;
                        break;
                    }else{
                        problem=true;
                        obj.close();
                    }

                }
                if (query.get()&&!problem) {
                    setQuery(false);
                    setWorking(true);
                    setFinished(false);
                    obj.writeData(command);
                    Calendar cal = Calendar.getInstance();
                    long time = cal.getTimeInMillis() / 1000;
                    while ((cal.getTimeInMillis() / 1000) - time > 3 || !isWorking());
                    if (!isWorking()) {
                        System.out.println("Did not receive an answer from the arduino, trying again.");
                        obj.writeData(command);
                    }
                    time = cal.getTimeInMillis() / 1000;
                    while ((cal.getTimeInMillis() / 1000) - time > 3 || !isWorking());
                    if (!isWorking()) {
                        System.out.println("Did not receive an answer from the arduino, stopping.");
                        setReply("no_answer!");
                        setWorking(false);
                        setFinished(true);
                    }
                }
            }
            obj.close();
        } catch (Exception e) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * This method stops the listening loops, which ultimately shuts it down.
     */
    public void quitCommunication() {
        quit.set(true);
    }

    /**
     * Changes the query phase after a command has been sent to the arduino. A
     * false value means that there is no command waiting to be sent.
     *
     * @param newValue The new value for the phase.
     */
    public void setQuery(Boolean newValue) {
        query.set(newValue);
    }

    /**
     * Changes the status of a command to "being processed" and "not
     * processing". Used when a command is sent to indicate if the command is
     * being processed by the arduino.
     *
     * @param newValue The value of the working boolean. True=processing.
     * False=done/not processing.
     */
    public void setWorking(Boolean newValue) {
        working.set(newValue);
    }

    /**
     * Gets the state of the arduino, which is split into two. True: Processing
     * a command. False: Not processing a command.
     *
     * @return A boolean indicating the state.
     */
    public Boolean isWorking() {
        return working.get();
    }

    /**
     * Returns the answer from the arduino.
     *
     * @return A string with the answer.
     */
    public String getReply() {
        while(!finished.get());
        finished.set(false);
        String answer = this.reply;
        this.reply = null;
        return answer;
    }

    /**
     * Sets the answer, used by the SerialClass to edit the answer when received
     * from the arduino.
     *
     * @param aReply The answer received from the arduino. 
     */
    public void setReply(String aReply) {
        reply = aReply;
    }

    /**
     * Sets a new command to be processed and sets the query phase to true if it
     * is false, indicating that there is a command waiting.
     *
     * @param aCommand The command to be processed by the arduino.
     */
    public void setCommand(String aCommand) {
        while(query.get() || working.get());
        command = aCommand;
        query.set(true);
    }

    /**
     * Gets the command set by the server.
     *
     * @return The command.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Sets the command variable to the default command. Default command =
     * no_command!
     */
    public void setCommandDefault() {
        command = defaultCommand;
        
    }

    /**
     * Returns a boolean showing if a task has finished.
     *
     * @return
     */
    public Boolean isFinished() {
        return finished.get();
    }

    /**
     * Sets the boolean that shows if a task is finished.
     *
     * @param aFinished True: finished False: not finished.
     */
    public void setFinished(Boolean aFinished) {
            finished.set(aFinished);
    }

    public ACQueue getArduinoQueue() {
        return ac;
    }

}
