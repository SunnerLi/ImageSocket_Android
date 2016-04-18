package com.sunner.imagesocket.Socket;

import android.graphics.Bitmap;

import com.sunner.imagesocket.Log.ImageSocketLog;
import com.sunner.imagesocket.RTP.RTPPacket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Semaphore;

/**
 * <p/>
 * <font color=red>
 * This class is the detail implementation of tcp image socket<br/>
 * The usual developer don't need to use this class object to send the image directly.<br/>
 * -->> Please use "ImageSocket" class to do your operation<br/>
 * </font>
 * <p/>
 */
class ImageSocket_TCP extends ImgSocket {
    Socket socket = null;
    SocketAddress socketAddress;
    OutputStream outputStream = null;
    Semaphore semaphore = new Semaphore(1);
    long time;

    /**
     * Constructor
     * @param host: The
     * @param port
     * @throws IOException
     */
    public ImageSocket_TCP(String host, int port) throws IOException {
        localPort = port;
        oppoHost = host;
        while (!portsAvaliable(localPort)) {
            ImageSocketLog.v(TAG, "本地連接阜" + localPort + "被佔用，自動產生新連接阜號碼");
            determineNewPort();
        }
        ImageSocketLog.v(TAG, "本地連接阜號碼為" + localPort);
        socket = new Socket(host, port);
        socketAddress = socket.getRemoteSocketAddress();
    }

    // The Image socket can set the time to keep connecting if it fail at first

    /**
     * @param timeRepeatConnect
     * @return
     * @throws IOException
     */
    public ImageSocket_TCP keepConnect(int timeRepeatConnect) throws IOException {
        for (int i = 0; i < timeRepeatConnect; i++) {
            ImageSocketLog.v(TAG, "第" + i + "次嘗試連線");
            if (!socket.isConnected()) {
                connect();
            } else
                break;
        }
        if (!socket.isConnected())
            ImageSocketLog.e(TAG, "Keep connecting fail, please check if the opposite is ready.");
        else
            ImageSocketLog.v(TAG, "Connect Success!");

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
            ImageSocketLog.e(TAG, "Haven't get input stream yet.");
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
