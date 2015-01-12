/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Arduino;

import Server.ConnectionHandler;
import Queue.ACQueue;
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

    private final ACQueue ac = new ACQueue();
    private String port;
    private String reply = null;
    private String command;
    private final AtomicBoolean query = new AtomicBoolean(false);
    private final AtomicBoolean quit = new AtomicBoolean(false);
    private final AtomicBoolean working = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private boolean problem = false;
    private final String defaultCommand;
    private final String[] rCmds;
    private final int retryTime = 3;
    private final int timeOut = 15;

    /**
     * Sets the port that the software will communicate with.
     *
     * @param port The port name as a string. eg. "COM3" (which is the default
     * on my pc)
     * @param defaultCommand Restricted command
     * @param rCmds 0: divider 1: error command 2: autocheck 3: end of autocheck
     */
    public ArdConnector(String port, String defaultCommand, String[] rCmds) {
        this.port = port;
        this.defaultCommand = defaultCommand;
        command = defaultCommand;
        this.rCmds = rCmds;
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
            simpleInit(obj);
            while (!quit.get()) {
                restart(obj);
                if (query.get() && !problem) {
                    setQuery(false);
                    setWorking(true);
                    setFinished(false);
                    obj.writeData(command);
                    Calendar cal = Calendar.getInstance();
                    long time = cal.getTimeInMillis() / 1000;
                    while ((cal.getTimeInMillis() / 1000) - time > retryTime || !isWorking());
                    if (!isWorking()) {
                        System.out.println("Did not receive an answer from the arduino, trying again.");
                        obj.writeData(command);
                    }
                    time = cal.getTimeInMillis() / 1000;
                    while ((cal.getTimeInMillis() / 1000) - time > retryTime || !isWorking());
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
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, e);
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
        Calendar cal = Calendar.getInstance();
        long time = cal.getTimeInMillis() / 1000;
        while (!finished.get()) {
            if (cal.getTimeInMillis() / 1000 - time >= timeOut) {
                return null;
            }
        }
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
    public synchronized void setCommand(String aCommand) {
        Calendar cal = Calendar.getInstance();
        long time = cal.getTimeInMillis() / 1000;
        while (query.get() || working.get()) {
            if (cal.getTimeInMillis() / 1000 - time >= timeOut) {
                return;
            }
        }
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

    public synchronized void setPort(String port) {
        this.port = port;
        problem = true;
    }

    public void restart(SerialClass obj) throws InterruptedException {
        //Doesn't work on windows 7. Works on windows 8.1
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if (currPortId.getName().equals(port)) {
                if (problem) {
                    //Test
                    //obj.close();
                    obj.initialize(this, ac, currPortId, rCmds);
                    ArdConnector.sleep(2000);
                    problem = false;
                }

                break;
            } else {
                obj.close();
                problem = true;
            }

        }
    }

    public void simpleInit(SerialClass obj) throws InterruptedException {
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if (currPortId.getName().equals(port)) {
                            //Test
                //obj.close();
                obj.initialize(this, ac, currPortId, rCmds);
                ArdConnector.sleep(2000);
                problem = false;
            }

        }
    }

}
