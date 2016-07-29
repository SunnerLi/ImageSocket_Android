package com.sunner.imagesocket.Socket;


import android.graphics.Bitmap;

import com.sunner.imagesocket.Log.ImageSocketLog;
import com.sunner.imagesocket.Packet.RTPPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;

/**
 * <p>
 *  <font color=red>
 *      This class is the detail implementation of udp image socket<br>
 *      The usual developer don't need to use this class object to send the image directly.<br>
 *      -->> Please use "ImageSocket" class to do your operation<br>
 *  </font>
 * <p>
 */
class ImageSocket_UDP extends ImgSocket {
    DatagramSocket datagramSocket[] = new DatagramSocket[5];
    private InetAddress inetAddress;
    Semaphore semaphore = new Semaphore(1);
    long time;


    public ImageSocket_UDP(String host, int port) {
        // remember to add check if the port number is valid
        localPort = port;
        oppoHost = host;
    }

    public ImageSocket_UDP getSocketWithoutCheck() throws UnknownHostException, SocketException {
        for (int i = 0; i < 5; i++) {
            datagramSocket[i] = new DatagramSocket(localPort + i);
            datagramSocket[i].setReuseAddress(true);
            inetAddress = InetAddress.getByName(oppoHost);
            if (!datagramSocket[i].isClosed())
                ImageSocketLog.v(TAG, "socket [" + localPort + i + "] 開啟中");
            else
                ImageSocketLog.v(TAG, "socket [" + localPort + i + "] 已關閉");
        }
        return this;
    }

    public ImageSocket_UDP getSocketWithCheck() throws UnknownHostException, SocketException {
        while (!portsAvaliable(localPort)) {
            ImageSocketLog.v(TAG, "本地連接阜" + localPort + "被佔用，自動產生新連接阜號碼");
            determineNewPort();
        }
        ImageSocketLog.v(TAG, "本地連接阜號碼為" + localPort);
        return getSocketWithoutCheck();
    }

    // Set the opposite port number
    public ImageSocket_UDP setOppoPort(int port) {
        if (port < 10000 || port > 60000)
            ImageSocketLog.e(TAG, "Invalid opposite port number");
        else
            oppoPort = port;
        return this;
    }


    // Send the image
    public ImageSocket_UDP send(Bitmap bitmap) throws IOException, InterruptedException {
        int imageIndex = 0;
        semaphore.acquire();
        time = System.currentTimeMillis();
        String bitmapString = bitMap2String(bitmap);
        String smallString = "";

        try {
            do {
                // Get the piece payload of image first and remove it
                if (bitmapString.length() > imageLength) {
                    smallString = bitmapString.substring(0, imageLength);
                    bitmapString = bitmapString.substring(imageLength, bitmapString.length());
                } else {
                    smallString = bitmapString;
                    bitmapString = "";
                }

                // Encode the RTP
                RTPPacket rtpPacket = new RTPPacket().setMaxLengthPayload(imageLength);
                byte[] _package = rtpPacket.encode(smallString, imageIndex++);

                // Send the pachage
                DatagramPacket packet = new DatagramPacket(_package, _package.length, inetAddress, oppoPort);
                ImageSocketLog.v(TAG, "封包長度: " + packet.getLength());
                datagramSocket[imageIndex % 5].send(packet);
            } while (bitmapString.length() > 0);
        } finally {
            close();
            time = System.currentTimeMillis() - time;
            semaphore.release();
        }
        return this;
    }

    // Set the max length of payload (Default is 60000)
    public ImageSocket_UDP setImageLength(int length) {
        super.setImageLengthOri(length);
        return this;
    }

    // Close socket
    public void close() {
        for (int i = 0; i < 5; i++) {
            if (datagramSocket[i] != null)
                datagramSocket[i].close();
        }
    }

    // Get the time of sending image
    public long getSendTime(){
        return time;
    }
}
