package com.jawi.usb;

import javax.usb.*;
import java.util.List;

/**
 * Created by man9527 on 2015/3/8.
 */
public class UsbHighLevel {

    /** The vendor ID of the missile launcher. */
    private static final short VENDOR_ID = 0x067b;

    /** The product ID of the missile launcher. */
    private static final short PRODUCT_ID = 0x2303;


    public static void main(String[] args) throws UsbException {
        UsbDevice device = findMissileLauncher(
                UsbHostManager.getUsbServices().getRootUsbHub());
        if (device == null)
        {
            System.err.println("Missile launcher not found.");
            System.exit(1);
            return;
        }

        // Claim the interface
        UsbConfiguration configuration = device.getUsbConfiguration((byte) 1);
        UsbInterface iface = configuration.getUsbInterface((byte) 0);
        iface.claim(new UsbInterfacePolicy()
        {
            @Override
            public boolean forceClaim(UsbInterface usbInterface)
            {
                return true;
            }
        });

        try
        {
            UsbEndpoint endpoint = iface.getUsbEndpoint((byte) 0x81);
            UsbPipe pipe = endpoint.getUsbPipe();
            pipe.open();
            try
            {
                byte[] data = new byte[8];
                int received = pipe.syncSubmit(data);
                System.out.println(received + " bytes received");
            }
            finally
            {
                pipe.close();
            }
        }
        finally
        {
            iface.release();
        }

    }

    public static UsbDevice findMissileLauncher(UsbHub hub)
    {
        UsbDevice launcher = null;

        for (UsbDevice device: (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            if (device.isUsbHub())
            {
                launcher = findMissileLauncher((UsbHub) device);
                if (launcher != null) return launcher;
            }
            else
            {
                UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
                if (desc.idVendor() == VENDOR_ID &&
                        desc.idProduct() == PRODUCT_ID) {
                    System.out.println("DEVICE FOUND");
                    return device;
                }
            }
        }
        return null;
    }
}
