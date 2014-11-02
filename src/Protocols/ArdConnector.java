/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import gnu.io.CommPortIdentifier;
import java.io.OutputStream;
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

    ACQueue ac;
    public String port = "COM3";
    private String inputLine = null;
    private String command = "no_command!";
    private final AtomicBoolean query = new AtomicBoolean(false);
    private final AtomicBoolean quit = new AtomicBoolean(false);
    private final AtomicBoolean working = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private boolean problem=true;
    /**
     * Sets the port that the software will communicate with.
     *
     * @param port The port name as a string. eg. "COM3" (which is the default
     * on my pc)
     */
    public ArdConnector(String port) {
        this.port = port;
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
            ac = new ACQueue();

            while (!quit.get()) {
                CommPortIdentifier portId = null;
                Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
                while (portEnum.hasMoreElements()) {
                    CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                    if (currPortId.getName().equals(port)) {
                        if(problem){                            
                            obj.initialize(this, ac, currPortId);
                            this.sleep(2000);
                        }
                        problem=false;
                        break;
                    }else{
                        problem=true;
                    }

                }
                if (query.get()&&!problem) {
                    changePhase(false);
                    setWorking(true);
                    setFinished(false);
                    obj.writeData(command);
                    Calendar cal = Calendar.getInstance();
                    long time = cal.getTimeInMillis() / 1000;
                    while ((cal.getTimeInMillis() / 1000) - time > 3 || !getWorking());
                    if (getWorking()) {
                        System.out.println("Did not receive an answer from the arduino, trying again.");
                        obj.writeData(command);
                    }
                    time = cal.getTimeInMillis() / 1000;
                    while ((cal.getTimeInMillis() / 1000) - time > 3 || !getWorking());
                    if (getWorking()) {
                        System.out.println("Did not receive an answer from the arduino, stopping.");
                        setInputLine("no_answer!");
                        setWorking(false);
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
        quit.compareAndSet(false, true);
    }

    /**
     * Changes the query phase after a command has been sent to the arduino. A
     * false value means that there is no command waiting to be sent.
     *
     * @param newValue The new value for the phase.
     */
    public void changePhase(Boolean newValue) {
        query.compareAndSet(!newValue, newValue);
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
        working.compareAndSet(!newValue, newValue);
    }

    /**
     * Gets the state of the arduino, which is split into two. True: Processing
     * a command. False: Not processing a command.
     *
     * @return A boolean indicating the state.
     */
    public Boolean getWorking() {
        return working.get();
    }

    /**
     * Returns the answer from the arduino.
     *
     * @return A string with the answer.
     */
    public String getInputLine() {
        String reply = inputLine;
        inputLine = null;
        return reply;
    }

    /**
     * Sets the answer, used by the SerialClass to edit the answer when received
     * from the arduino.
     *
     * @param aInputLine The answer received from the arduino.
     */
    public void setInputLine(String aInputLine) {
        inputLine = aInputLine;
    }

    /**
     * Sets a new command to be processed and sets the query phase to true if it
     * is false, indicating that there is a command waiting.
     *
     * @param aCommand The command to be processed by the arduino.
     */
    public void setCommand(String aCommand) {
        command = aCommand;
        query.compareAndSet(false, true);
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
        command = "no_command!";
    }

    /**
     * Returns a boolean showing if a task has finished.
     *
     * @return
     */
    public AtomicBoolean getFinished() {
        return finished;
    }

    /**
     * Sets the boolean that shows if a task is finished.
     *
     * @param aFinished True: finished False: not finished.
     */
    public void setFinished(Boolean aFinished) {
        if (!finished.get() == false && !aFinished == false) {
            finished.compareAndSet(!aFinished, aFinished);
        }
    }

    public ACQueue getArduinoQueue() {
        return ac;
    }

}
