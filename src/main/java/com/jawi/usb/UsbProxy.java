package com.jawi.usb;

import org.usb4java.DeviceHandle;

import javax.usb.*;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import javax.usb.util.DefaultUsbIrp;
import javax.usb.util.UsbUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by man9527 on 2015/2/20.
 */
public class UsbProxy {
    /** The vendor ID of the missile launcher. */
    private static final short VENDOR_ID = 0x10c4;

    /** The product ID of the missile launcher. */
    private static final short PRODUCT_ID = (short)0xea60;

    private static UsbProxy instance = new UsbProxy();
    private List<UsbListener> listeners = new ArrayList<>();

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    Random random = new Random();

    private volatile boolean running = true;

    private UsbProxy() {
//        System.out.println(VENDOR_ID + "," + PRODUCT_ID);
//        try {
//            UsbDevice device = findUsbDevice(UsbHostManager.getUsbServices().getRootUsbHub());
//
//            UsbConfiguration configuration = device.getUsbConfiguration((byte) 1);
//            UsbInterface iface = configuration.getUsbInterface((byte) 0);
//            iface.claim(usbInterface -> true);
//
//            byte[] bytes1 = HexDump.hexStringToByteArray("10");
//            byte[] bytes2 = HexDump.hexStringToByteArray("31");
//            byte[] bytes3 = HexDump.hexStringToByteArray("72");
//
//            UsbEndpoint endpoint = iface.getUsbEndpoint((byte)0x81);
//
//            UsbPipe pipe = endpoint.getUsbPipe();
//            pipe.open();
//
//            pipe.addUsbPipeListener(new UsbPipeListener()
//            {
//                @Override
//                public void errorEventOccurred(UsbPipeErrorEvent event)
//                {
//                    UsbException error = event.getUsbException();
//
//                }
//
//                @Override
//                public void dataEventOccurred(UsbPipeDataEvent event)
//                {
//                    byte[] data = event.getData();
//                    System.out.println(new String(data));
//                }
//            });
//
//            byte[] buffer = new byte[UsbUtil.unsignedInt(pipe.getUsbEndpoint().getUsbEndpointDescriptor().wMaxPacketSize())];
//
//            pipe.asyncSubmit(buffer);
//
//        } catch (UsbException e) {
//            e.printStackTrace();
//        }
    }

    public static void sendMessage(UsbDevice device, byte[] message)
            throws UsbException
    {
        UsbControlIrp irp = device.createUsbControlIrp(
                (byte) (UsbConst.REQUESTTYPE_TYPE_CLASS |
                        UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE), (byte) 0x01,
                (short) 2, (short) 1);
        irp.setData(message);
        device.syncSubmit(irp);
    }

    public static UsbProxy get() {return instance;}

    public void startGetValue() {
        running = true;
        executorService.submit(() -> {
            while(running) {
                int rpm = random.nextInt(50);
                int highTemperature = random.nextInt(30);
                int lowTemperature = random.nextInt(20);

                listeners.forEach((e)->{
                    e.setRpm(rpm);
                    e.setHighTemperature(highTemperature);
                    e.setLowTemperature(lowTemperature);
                });

                try {
                    Thread.sleep(100);
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

    private UsbDevice findUsbDevice(UsbHub hub)
    {
        UsbDevice launcher = null;

        for (UsbDevice device: (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            if (device.isUsbHub())
            {
                launcher = findUsbDevice((UsbHub) device);
                if (launcher != null) return launcher;
            }
            else
            {
                UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
                if (desc.idVendor() == VENDOR_ID &&
                        desc.idProduct() == PRODUCT_ID) return device;
            }
        }

        return null;
    }
}
