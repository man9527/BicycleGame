package com.jawi.usb;

/**
 * Created by wchu on 3/15/2015.
 */
public interface Protocol {

    // protocol manager handles each received byte
    void onReceive(byte b);

    // protocol manager handles broken stream
    void onStreamClosed();
}
