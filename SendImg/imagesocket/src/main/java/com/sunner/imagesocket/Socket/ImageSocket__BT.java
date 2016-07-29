package com.sunner.imagesocket.Socket;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.util.Log;

import com.sunner.imagesocket.Log.ImageSocketLog;
import com.sunner.imagesocket.RTP.BTPacket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * <p/>
 * <font color=red>
 * This class is the detail implementation of bluetooth image socket<br/>
 * The usual developer don't need to use this class object to send the image directly.<br/>
 * -->> Please use "ImageSocket" class to do your operation<br/>
 * </font>
 * <p/>
 */
class ImageSocket__BT extends ImgSocket {
    private BluetoothSocket bluetoothSocket;                                                        // Socket object
    public int timeStapm = 0;
    public int imageLength = 60000;                                                                 // The length of data each time to pass
    OutputStream outputStream;                                                                      // output stream object
    Semaphore semaphore = new Semaphore(1);                                                         // concurrent control
    long time;                                                                                      // Measure sending time

    /**
     * Constructor
     * @param bluetoothAdapter: The bluetooth adapter object
     * @param address: the opposite bluetooth address
     * @param uuid: the standard uuid
     */
    public ImageSocket__BT(BluetoothAdapter bluetoothAdapter, String address, UUID uuid) {
        // 嘗試建立連線
        try {
            bluetoothSocket = bluetoothAdapter.getRemoteDevice(address)
                    .createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // The Image socket can set the time to keep connecting if it fail at first

    /**
     * @param timeRepeatConnect:
     * @return
     * @throws IOException
     */
    public ImageSocket__BT keepConnect(int timeRepeatConnect) throws IOException {
        for (int i = 0; i < timeRepeatConnect; i++) {
            ImageSocketLog.v(TAG, "第" + i + "次嘗試連線");
            if (!bluetoothSocket.isConnected()) {
                connect();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else
                break;
        }
        if (!bluetoothSocket.isConnected())
            ImageSocketLog.e(TAG, "Keep connecting fail, please check if the opposite is ready.");
        else
            ImageSocketLog.v(TAG, "Connect Success!");

        return null;
    }

    // connect to the PC
    public void connect() throws IOException, NullPointerException {
        if (bluetoothSocket == null)
            ImageSocketLog.i(TAG, "socket為空");
        else {
            ImageSocketLog.v(TAG, "是否連線：" + bluetoothSocket.isConnected());
            if (!bluetoothSocket.isConnected()) {
                try {
                    bluetoothSocket.connect();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // The Inputstream cannot send the image directly, skip implementation
    public ImageSocket__BT getOutputStream() throws IOException {
        outputStream = bluetoothSocket.getOutputStream();
        return this;
    }

    // Close the image socket
    public void close() throws IOException {
        if (bluetoothSocket != null)
            bluetoothSocket.close();
    }

    // 傳送
    public ImageSocket__BT send(Bitmap bitmap) throws IOException, InterruptedException {
        timeStapm = 0;

        // get the output stream first
        getOutputStream();
        if (outputStream == null)
            ImageSocketLog.e(TAG, "Haven't get output stream yet.");
        else {
            int imageIndex = 0;
            semaphore.acquire();
            time = System.currentTimeMillis();
            String bitmapString = bitMap2String(bitmap);
            String smallString = "";
            do {
                // Get the piece payload of image first and remove it
                if (bitmapString.length() > imageLength) {
                    smallString = bitmapString.substring(0, imageLength);
                    bitmapString = bitmapString.substring(imageLength, bitmapString.length());
                } else {
                    smallString = bitmapString;
                    bitmapString = "";
                }

                // Encode the bluetooth packet
                BTPacket btPacket = new BTPacket().setMaxLengthPayload(imageLength);
                byte[] _package = btPacket.encode(smallString, imageIndex++);

                // Send the pachage
                outputStream.write(_package);
            } while (bitmapString.length() > 0);
            Thread.sleep(100);
            BTPacket btPacket = new BTPacket();
            byte[] end = btPacket.encode("", 0);
            outputStream.write(end);
            time = System.currentTimeMillis() - time;
            semaphore.release();
        }
        return this;
    }

    // Get the time of sending image
    public long getSendTime() {
        return time;
    }
}
