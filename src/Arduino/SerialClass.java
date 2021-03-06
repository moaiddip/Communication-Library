/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Arduino;

/**
 * The serial class establishes a connection with the arduino and adds a
 * listener to the serial port. It is responsible for all the commands sent and
 * received.
 *
 * @author Sozos
 */
import Server.ConnectionHandler;
import Queue.SimpleQueue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerialClass implements SerialPortEventListener {

    private Boolean autocheck = false;
    public SerialPort serialPort;
    public BufferedReader input;
    public OutputStream output;
    private ArduinoHandler ard;
    private SimpleQueue ac;
    private String[] parts;
    private String[] parts2;
    private String divider; 
    private String errCmd;
    private String autoCheck;
    private String endAutoCheck;
    /**
     * Milliseconds to block while waiting for port open
     */
    public final int TIME_OUT = 2000;
    /**
     * Default bits per second for COM port.
     */
    public final int DATA_RATE = 38400;
    /**
     * Initializes connection to an arduino
     * @param ard The ArduinoHandler this class is created in
     * @param ac The SimpleQueue created in the ArduinoHandler
     * @param portId The port
     * @param rCmds The restricted commands
     */
    public void initialize(ArduinoHandler ard, SimpleQueue ac, CommPortIdentifier portId, String[] rCmds) {
        this.ard = ard;
        this.ac = ac;
        divider = rCmds[0];
        errCmd = rCmds[1];
        autoCheck = rCmds[2];
        endAutoCheck = rCmds[3];
        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            /*serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN
             | SerialPort.FLOWCONTROL_RTSCTS_OUT);
             */
            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();
            char ch = 1;
            output.write(ch);

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            System.out.println("Done initializing connection to the Arduino at port: " + portId.getName());
        } catch (Exception e) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    /**
     * Handles the information received from the arduino and decides whether
     * they need to go to a queue or set as a specific reply to a command.
     *
     * @param oEvent Arduino Event
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                //ACQueue ard =new ACQueue();
                String inputz;
                while ((inputz = input.readLine()) != null) {
                    if (inputz==null || inputz.equals("")){
                        break;
                    }
                    parts = inputz.split(divider);
                    parts2 = ard.getCommand().split(divider);
                    if (autoCheck.equals(inputz)) {
                        autocheck = true;
                    } else if (endAutoCheck.equals(inputz)) {
                        autocheck = false;
                    } else if ((parts[0].equals(parts2[0]) || parts[0].equals(errCmd)) && !autocheck) {
                        ard.setReply(inputz);
                        ard.setCommandDefault();
                        ard.setWorking(false);
                        ard.setFinished(true);
                    } else if (autocheck) {
                        ac.putCmd(inputz);
                    } else {
                        ac.putCmd(inputz);
                    }
                }
            } catch (Exception e) {
                
            }
        }

    }

    public synchronized void writeData(String data) {
        try {
            output.write(data.getBytes());
            output.flush();
        } catch (Exception e) {
            System.out.println("Could not write to port.");
        }
    }
}
