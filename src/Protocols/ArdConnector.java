/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Sozos
 */
public class ArdConnector extends Thread {

    public static OutputStream output;
    public static String port = "COM3";
    private static String inputLine = null;
    private static String command;
    private final static AtomicBoolean query = new AtomicBoolean(false);

    private final AtomicBoolean quit = new AtomicBoolean(false);
    private final static AtomicBoolean working = new AtomicBoolean(false);

    public ArdConnector(String port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            SerialClass obj = new SerialClass();
            obj.initialize();
            this.sleep(2000);
            output = SerialClass.output;

            while (!quit.get()) {
                if (query.get()) {
                    changePhase(false);
                    setWorking(true);
                    obj.writeData(command);
                    //this.sleep(2500);
                }
            }
            obj.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void quitCommunication() {
        quit.compareAndSet(false, true);
    }

    public void changePhase(Boolean newValue) {
        query.compareAndSet(!newValue, newValue);
    }

    public static void setWorking(Boolean newValue) {
        working.compareAndSet(!newValue, newValue);
    }
    public Boolean getWorking(){
        return working.get();
    }

    public String getInputLine() {
        return inputLine;
    }

    public static void setInputLine(String aInputLine) {
        inputLine = aInputLine;
    }
    public void setCommand(String aCommand) {
        command = aCommand;
        query.compareAndSet(false, true);
    }

}
