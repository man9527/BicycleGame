package com.jawi.usb;

import gnu.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by man9527 on 2015/2/20.
 */
public class UsbProxy implements Protocol {

    private static UsbProxy instance = new UsbProxy();
    private List<UsbListener> listeners = new ArrayList<>();

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Random random = new Random();
    private volatile boolean running = true;

    private byte[] buffer = new byte[1024];
    private int tail = 0;


    public static UsbProxy get() {return instance;}

    public void connect() throws NoSuchPortException, UnsupportedCommOperationException, PortInUseException, IOException {
        if (System.getProperty("simulate")!=null && System.getProperty("simulate").equals("true")) {
            startGetValue();
        } else {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(System.getProperty("COM_PORT"));

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
    }

    public void startGetValue() {
        running = true;
        executorService.submit(() -> {
            while(running) {
                byte[] buffer = new byte[13];
                buffer[0]=48;
                buffer[1]= (byte) (random.nextInt(2)+1);
                buffer[2]= (byte) (random.nextInt(9));
                buffer[3]= (byte) (random.nextInt(9));

                buffer[5]= (byte) (random.nextInt(2)+2);
                buffer[6]= (byte) (random.nextInt(9));
                buffer[7]= (byte) (random.nextInt(9));

                buffer[9]= 0;
                buffer[10]= 0;
                buffer[11]= (byte) (random.nextInt(9));
                buffer[12]= (byte) (random.nextInt(9));

                int rpm = NumberParser.parseRpm(buffer);
                float highTemperature = NumberParser.parseHighTemperature(buffer);
                float lowTemperature = NumberParser.parseLowTemperature(buffer);

                listeners.forEach((e)->{
                    e.setRpm(rpm);
                    e.setHighTemperature(highTemperature);
                    e.setLowTemperature(lowTemperature);
                    e.setEnvTemperature((highTemperature+lowTemperature)/2F);
                });

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stopGetValue() {
        running = false;
    }

    public void addListener(UsbListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onReceive(byte b) {
        // simple protocol: each message ends with new line
        if (b=='\n') {
            onMessage();
        } else {
            buffer[tail] = b;
            tail++;
        }
    }

    public void onStreamClosed() {
        onMessage();
    }

    /*
     * When message is recognized onMessage is invoked
     */
    private void onMessage() {
        if (tail!=0) {
            // constructing message
            getMessage(buffer, tail);
            tail = 0;
        }
    }

    public String getMessage(byte[] buffer, int len) {
        if (buffer.length>0 && buffer[0]==48) {
            int rpm = NumberParser.parseRpm(buffer);
            float highTemperature = NumberParser.parseHighTemperature(buffer);
            float lowTemperature = NumberParser.parseLowTemperature(buffer);

            listeners.forEach((e)->{
                e.setRpm(rpm);
                e.setHighTemperature(highTemperature);
                e.setLowTemperature(lowTemperature);
                e.setEnvTemperature((highTemperature+lowTemperature)/2F);
            });

        }
        return "";
    }
}
