/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

/**
 * The serial class establishes a connection with the arduino and adds a
 * listener to the serial port. It is responsible for all the commands sent and
 * received.
 *
 * @author Sozos
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerialClass implements SerialPortEventListener {

    Boolean autocheck = false;
    public SerialPort serialPort;
    public BufferedReader input;
    public OutputStream output;
    ArdConnector ard;
    ACQueue ac;
    String[] parts;
    String[] parts2;
    /**
     * Milliseconds to block while waiting for port open
     */
    public final int TIME_OUT = 2000;
    /**
     * Default bits per second for COM port.
     */
    public final int DATA_RATE = 38400;

    public void initialize(ArdConnector ard, ACQueue ac, CommPortIdentifier portId) {
        this.ard = ard;
        this.ac = ac;
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
        } catch (PortInUseException | UnsupportedCommOperationException | IOException | TooManyListenersException e) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, e);
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

                    System.out.println("Received: " + inputz);
                    parts = inputz.split("_");
                    parts2 = ard.getCommand().split("_");
                    if ("autochkstart!".equals(inputz)) {
                        autocheck = true;
                    } else if ("eol!".equals(inputz)) {
                        autocheck = false;
                    } else if ((parts[0].equals(parts2[0]) || parts[0].equals("error")) && !autocheck) {
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
                Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, e);
            }
        }

    }

    public synchronized void writeData(String data) {
        System.out.println("Sent: " + data);
        try {
            output.write(data.getBytes());
            //output.flush();
        } catch (Exception e) {
            System.out.println("Could not write to port.");
        }
    }
}
