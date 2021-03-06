package com.jawi.usb;

/**
 * Created by wchu on 3/15/2015.
 */
import java.io.IOException;
import java.io.InputStream;

public class CommPortReceiver extends Thread {

    InputStream in;
    Protocol protocol;

    public CommPortReceiver(InputStream in, Protocol protocol) {
        this.in = in;
        this.protocol = protocol;
    }

    public void run() {
        try {
            int b;
            while(true) {

                // if stream is not bound in.read() method returns -1
                while((b = in.read()) != -1) {
                    protocol.onReceive((byte) b);
                }
                protocol.onStreamClosed();

                // wait 10ms when stream is broken and check again
                sleep(10);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}