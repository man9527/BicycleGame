package com.jawi.usb;

/**
 * Created by man9527 on 2015/2/20.
 */
public interface UsbListener {
    void setRpm(int rpm);
    void setHighTemperature(float degree);
    void setLowTemperature(float degree);
    void setEnvTemperature(float degree);
}
