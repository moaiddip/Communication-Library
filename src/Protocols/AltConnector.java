/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AltConnector {

    SerialPort serialPort;
    String answer = "empty";
    static BufferedReader input;
    static OutputStream output;
    /**
     * Milliseconds to block while waiting for port open
     */
    final int TIME_OUT = 2000;
    /**
     * Default bits per second for COM port.
     */
    final int DATA_RATE = 9600;

    public String ardSend(String msg, String port) {
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
            return "nosuchport";
        }
        try {
            CommPort commPort = portId.open(this.getClass().getName(), 6000);
            SerialPort serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_ODD);
            InputStream in = serialPort.getInputStream();
            OutputStream out = serialPort.getOutputStream();
            (new Thread(new SerialReader(in))).start();
            (new Thread(new SerialWriter(out, msg))).start();
            synchronized(answer){
                while("empty".equals(answer)){
                    answer.wait();
                }
            }
        } catch (PortInUseException | UnsupportedCommOperationException | IOException | InterruptedException ex) {
            Logger.getLogger(AltConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return answer;
    }

    public class SerialReader implements Runnable {

        InputStream in;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void run() {
            byte[] buffer = new byte[200];
            int len = -1;
            try {
                while ((len = this.in.read(buffer)) > -1) {
                    if (Arrays.toString(buffer).contains("!")) {
                        synchronized (answer) {
                            answer = Arrays.toString(buffer);
                            answer.notify();
                        }
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class SerialWriter implements Runnable {

        OutputStream out;
        String message;

        public SerialWriter(OutputStream out, String message) {
            this.out = out;
            this.message = message;
        }

        public void run() {
            try {

                
                while (true) {
                    this.out.write(message.getBytes());
                    this.out.flush();
                    Thread.sleep(1500);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
