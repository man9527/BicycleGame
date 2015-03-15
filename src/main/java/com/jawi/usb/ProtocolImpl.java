package com.jawi.usb;

/**
 * Created by wchu on 3/15/2015.
 */
public class ProtocolImpl implements Protocol {

    byte[] buffer = new byte[1024];
    int tail = 0;

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
            String message = getMessage(buffer, tail);
            //System.out.println("RECEIVED MESSAGE: " + message);

            // this logic should be placed in some kind of
            // message interpreter class not here
            if ("HELO".equals(message)) {
                CommPortSender.send(getMessage("OK"));
            } else if ("OK".equals(message)) {
                CommPortSender.send(getMessage("OK ACK"));
            }
            tail = 0;
        }
    }

    // helper methods
    public byte[] getMessage(String message) {
        return (message+"\n").getBytes();
    }

    public String getMessage(byte[] buffer, int len) {
        System.out.println(buffer[0]);
        return new String(buffer, 0, tail);
    }
}
