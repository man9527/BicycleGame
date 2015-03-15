package com.jawi.usb;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by wchu on 3/15/2015.
 */
public class CommPortSender {
    static OutputStream out;

    public static void setWriterStream(OutputStream out) {
        CommPortSender.out = out;
    }

    public static void send(byte[] bytes) {
        try {
            System.out.println("SENDING: " + new String(bytes, 0, bytes.length));

            // sending through serial port is simply writing into OutputStream
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
