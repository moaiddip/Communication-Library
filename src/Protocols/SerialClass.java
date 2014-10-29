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
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.Enumeration;

public class SerialClass implements SerialPortEventListener {
    Boolean autocheck = false;
    public SerialPort serialPort;
    public static BufferedReader input;
    public static OutputStream output;
    /**
     * Milliseconds to block while waiting for port open
     */
    public static final int TIME_OUT = 2000;
    /**
     * Default bits per second for COM port.
     */
    public static final int DATA_RATE = 115200;

    public void initialize() {
        String port = ArdConnector.port;
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if (currPortId.getName().equals(port)) {
                portId = currPortId;
                break;
            }

        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

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
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                ArdQueue ard =new ArdQueue();
                String inputz;
                while ((inputz = input.readLine()) != null) {
                    
                    System.out.println("Received: " + inputz);

                    String[] parts = inputz.split("_");
                    String[] parts2 = ArdConnector.getCommand().split("_");
                    System.out.println(ArdConnector.getInputLine());
                    if ("autochkstart!".equals(inputz)){
                        autocheck=true;
                    }
                    else if ("eol!".equals(inputz)){
                        autocheck=false;
                    }
                    else if ((parts[0].equals(parts2[0])|| parts[0].equals("error")) && !autocheck) {
                        ArdConnector.setInputLine(inputz);
                        ArdConnector.setCommandDefault();
                        ArdConnector.setWorking(false);
                    }else if(autocheck){
                        ard.putMsg(inputz);
                    }
                    else{
                        ard.putMsg(inputz);
                    }
                }
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }

    }

    public static synchronized void writeData(String data) {
        System.out.println("Sent: " + data);
        try {
            output.write(data.getBytes());
            //output.flush();
        } catch (Exception e) {
            System.out.println("could not write to port");
        }
    }
}