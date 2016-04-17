package com.sunner.imagesocket.Socket;

import android.graphics.Bitmap;

import com.sunner.imagesocket.Log.Log;
import com.sunner.imagesocket.RTP.RTPPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Semaphore;

/**
 * Created by sunner on 2016/4/8.
 */
class ImageSocket_TCP extends ImgSocket {
    Socket socket = null;
    SocketAddress socketAddress;
    OutputStream outputStream = null;
    Semaphore semaphore = new Semaphore(1);
    long time;

    public ImageSocket_TCP(String host, int port) throws IOException {
        localPort = port;
        oppoHost = host;
        while (!portsAvaliable(localPort)) {
            Log.v(TAG, "本地連接阜" + localPort + "被佔用，自動產生新連接阜號碼");
            determineNewPort();
        }
        Log.v(TAG, "本地連接阜號碼為" + localPort);
        socket = new Socket(host, port);
        socketAddress = socket.getRemoteSocketAddress();
    }

    // The Image socket can set the time to keep connecting if it fail at first
    public ImageSocket_TCP keepConnect(int timeRepeatConnect) throws IOException {
        for (int i = 0; i < timeRepeatConnect; i++) {
            Log.v(TAG, "第" + i + "次嘗試連線");
            if (!socket.isConnected()) {
                connect();
            } else
                break;
        }
        if (!socket.isConnected())
            Log.e(TAG, "Keep connecting fail, please check if the opposite is ready.");
        else
            Log.v(TAG, "Connect Success!");

        return null;
    }

    // The Inputstream cannot send the image directly, skip implementation
    public ImageSocket_TCP getInputStream() throws IOException {
        outputStream = socket.getOutputStream();
        return this;
    }

    // Close the image socket
    public void close() throws IOException {
        if (socket != null)
            socket.close();
    }

    // Connect to the server
    public void connect() {
        if (!socket.isConnected()) {
            try {
                socket.connect(socketAddress);
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

    public ImageSocket_TCP send(Bitmap bitmap) throws IOException, InterruptedException {
        if (outputStream == null)
            Log.e(TAG, "Haven't get input stream yet.");
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

                // Encode the RTP
                RTPPacket rtpPacket = new RTPPacket().setMaxLengthPayload(imageLength);
                byte[] _package = rtpPacket.encode(smallString, imageIndex++);

                // Send the pachage
                outputStream.write(_package);
                //Thread.sleep(10);
            } while (bitmapString.length() > 0);
            Thread.sleep(100);
            RTPPacket rtpPacket = new RTPPacket();
            byte[] end = rtpPacket.encode("", 0);
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
