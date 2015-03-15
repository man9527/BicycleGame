package com.jawi.usb;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.util.Enumeration;

/**
 * Created by wchu on 3/15/2015.
 */
public class USBTest2 {


    public void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Port in use!");
        } else {
            // points who owns the port and connection timeout
            SerialPort serialPort = (SerialPort) portIdentifier.open("RS232Example", 2000);

            // setup connection parameters
            serialPort.setSerialPortParams(
                    9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            // setup serial port writer
            CommPortSender.setWriterStream(serialPort.getOutputStream());

            // setup serial port reader
            new CommPortReceiver(serialPort.getInputStream()).start();
        }
    }

    public static void main(String[] args) throws Exception {

        // connects to the port which name (e.g. COM1) is in the first argument
        new USBTest2().connect("COM6");

        // send HELO message through serial port using protocol implementation
        CommPortSender.send(new ProtocolImpl().getMessage("HELO"));
    }
}
