package com.jawi.usb;

/**
 * Created by wchu on 3/17/2015.
 */
public class NumberParser {
    public static int parseRpm(byte[] buffer) {
        return buffer[9]*1000 + buffer[10]*100 + buffer[11]*10 + buffer[12];
    }

    public static float parseHighTemperature(byte[] buffer) {
        return buffer[5]*10 + buffer[6] + ((float)buffer[7])/10;
    }

    public static float parseLowTemperature(byte[] buffer) {
        return buffer[1]*10 + buffer[2] + ((float)buffer[3])/10;
    }

}
