/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Protocols;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class ArdConnector {

    SerialPort serialPort;
    String answer = "empty";

    public String ardSend(String msg, String port) {
        serialPort = new SerialPort(port); 
        try {
            serialPort.openPort();//Open port
            serialPort.setParams(9600, 8, 1, 0);//Set params
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
            serialPort.writeBytes(msg.getBytes());
            synchronized(answer){
                while(!answer.contains("!")){
                    answer.wait();
                }
                return answer;
            }
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ArdConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /*
     * In this class must implement the method serialEvent, through it we learn about 
     * events that happened to our port. But we will not report on all events but only 
     * those that we put in the mask. In this case the arrival of the data and change the 
     * status lines CTS and DSR
     */
    class SerialPortReader implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {
            byte message[]= new byte[200];
            int countdown = 0;
            if(event.isRXCHAR()){//If data is available
                while(event.getEventValue() >= 1){//Check bytes count in the input buffer
                    //Read data, if 10 bytes available 
                    try {
                        byte buffer[]=serialPort.readBytes(1);
                        message[countdown]=buffer[0];
                        countdown++;
                        if(Arrays.toString(buffer).contains("!")){
                           synchronized(answer){ 
                            answer=Arrays.toString(message);
                            answer.notify();
                           }
                            break;
                        }
                        
                    }
                    catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
                }
            }
            else if(event.isCTS()){//If CTS line has changed state
                if(event.getEventValue() == 1){//If line is ON
                    System.out.println("CTS - ON");
                }
                else {
                    System.out.println("CTS - OFF");
                }
            }
            else if(event.isDSR()){///If DSR line has changed state
                if(event.getEventValue() == 1){//If line is ON
                    System.out.println("DSR - ON");
                }
                else {
                    System.out.println("DSR - OFF");
                }
            }
        }
    }
}