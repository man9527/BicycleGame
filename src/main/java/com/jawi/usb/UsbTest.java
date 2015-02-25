package com.jawi.usb;

import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * Created by man9527 on 2015/2/23.
 */
public class UsbTest {
    /** The vendor ID of the missile launcher. */
    private static final short VENDOR_ID = 0x10c4;
    /** The product ID of the missile launcher. */
    private static final short PRODUCT_ID = (short)0xea60;

    private static final byte OUT_ENDPOINT = (byte)0x01;
    private static final byte IN_ENDPOINT = (byte)0x81;

    private static final long TIMEOUT = 5000;

    public static void main(String[] args) {
        Context context = new Context();
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb.", result);

        int interfaceNumber = 0;
        Device device = findDevice(VENDOR_ID, PRODUCT_ID);
        DeviceHandle handle = new DeviceHandle();
        result = LibUsb.open(device, handle);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to open USB device", result);

        result = LibUsb.claimInterface(handle, interfaceNumber);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to claim interface", result);

        try
        {

            byte[] message = {14};

            byte[] bytes1 = HexDump.hexStringToByteArray("10");
            byte[] bytes2 = HexDump.hexStringToByteArray("31");
            byte[] bytes3 = HexDump.hexStringToByteArray("72");

            write(handle, message);
            write(handle, bytes2);
            write(handle, bytes3);

            final TransferCallback bodyReceived = new TransferCallback()
            {
                @Override
                public void processTransfer(Transfer transfer)
                {
                    System.out.println(transfer.actualLength() + " bytes received");
                    LibUsb.freeTransfer(transfer);
                    System.out.println("Asynchronous communication finished");

                }
            };

            read(handle, 64, bodyReceived);
System.out.println("wait");

            //ByteBuffer header = read(handle, 1);
//            ByteBuffer buffer = ByteBuffer.allocateDirect(1);
//            buffer.put(message);
//            IntBuffer transfered = IntBuffer.allocate(1);
//            result = LibUsb.bulkTransfer(handle, (byte)0x01, buffer, transfered, 10000);
//            System.out.println(transfered.get() + " bytes sent");
//
//
//            buffer = ByteBuffer.allocateDirect(1);
//            buffer.put(HexDump.hexStringToByteArray("14"));
//            transfered = IntBuffer.allocate(1);
//            result = LibUsb.bulkTransfer(handle, (byte)0x01, buffer, transfered, 10000);
//            System.out.println(transfered.get() + " bytes sent");
//
//            buffer = ByteBuffer.allocateDirect(1);
//            buffer.put(HexDump.hexStringToByteArray("14"));
//            transfered = IntBuffer.allocate(1);
//            result = LibUsb.bulkTransfer(handle, (byte)0x01, buffer, transfered, 10000);
//            System.out.println(transfered.get() + " bytes sent");


//            buffer = ByteBuffer.allocateDirect(1);
//            //buffer.put(HexDump.hexStringToByteArray("14"));
//            transfered = IntBuffer.allocate(1);
//            result = LibUsb.bulkTransfer(handle, (byte)0x81, buffer, transfered, 10000);
//            System.out.println(transfered.get() + " bytes sent");

            //if (result != LibUsb.SUCCESS) throw new LibUsbException("Control transfer failed", transfered);

        }
        finally
        {
            result = LibUsb.releaseInterface(handle, interfaceNumber);
            if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to release interface", result);
        }

        LibUsb.exit(context);
    }

    public static void write(DeviceHandle handle, byte[] data)
    {
        ByteBuffer buffer = BufferUtils.allocateByteBuffer(data.length);
        buffer.put(data);
        IntBuffer transferred = BufferUtils.allocateIntBuffer();
        int result = LibUsb.bulkTransfer(handle, OUT_ENDPOINT, buffer,
                transferred, TIMEOUT);
        if (result != LibUsb.SUCCESS)
        {
            throw new LibUsbException("Unable to send data", result);
        }
        System.out.println(transferred.get() + " bytes sent to device");
    }

    public static void read(DeviceHandle handle, int size,
                            TransferCallback callback)
    {
        ByteBuffer buffer = BufferUtils.allocateByteBuffer(size).order(
                ByteOrder.LITTLE_ENDIAN);
        Transfer transfer = LibUsb.allocTransfer();
        LibUsb.fillBulkTransfer(transfer, handle, IN_ENDPOINT, buffer,
                callback, null, TIMEOUT);
        System.out.println("Reading " + size + " bytes from device");
        int result = LibUsb.submitTransfer(transfer);
        if (result != LibUsb.SUCCESS)
        {
            throw new LibUsbException("Unable to submit transfer", result);
        }
    }



    public static Device findDevice(short vendorId, short productId)
    {
        // Read the USB device list
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0) throw new LibUsbException("Unable to get device list", result);

        try
        {
            // Iterate over all devices and scan for the right one
            for (Device device: list)
            {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device descriptor", result);
                if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) return device;
            }
        }
        finally
        {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        // Device not found
        return null;
    }

    static class EventHandlingThread extends Thread
    {
        /** If thread should abort. */
        private volatile boolean abort;

        /**
         * Aborts the event handling thread.
         */
        public void abort()
        {
            this.abort = true;
        }

        @Override
        public void run()
        {
            while (!this.abort)
            {
                // Let libusb handle pending events. This blocks until events
                // have been handled, a hotplug callback has been deregistered
                // or the specified time of 0.5 seconds (Specified in
                // Microseconds) has passed.
                int result = LibUsb.handleEventsTimeout(null, 500000);
                if (result != LibUsb.SUCCESS)
                    throw new LibUsbException("Unable to handle events", result);
            }
        }
    }
}
