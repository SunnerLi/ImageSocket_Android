package com.sunner.imagesocket.Socket;

import android.graphics.Bitmap;

import com.sunner.imagesocket.Log.Log;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.crypto.spec.DESedeKeySpec;

/**
 * Created by sunner on 2016/4/8.
 */
public class ImageSocket {
    String TAG = "資訊";

    // The Transfer Protocol
    public final static int DEF = -1;
    public final static int TCP = 0;
    public final static int UDP = 1;
    public int mode = DEF;

    // Log
    public static boolean enableLog = true;


    public ImageSocket_TCP socket_tcp = null;
    public ImageSocket_UDP socket_udp = null;
    public String host = null;
    public int port = -1;

    public ImageSocket(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ImageSocket setProtocol(int mode) throws IOException {
        if (this.mode != DEF) {
            if (this.mode == UDP && mode == UDP) {
                socket_udp = new ImageSocket_UDP(host, port);
                return this;
            } else if (this.mode == TCP && mode == TCP) {
                socket_tcp = new ImageSocket_TCP(host, port);
                return this;
            } else
                Log.e(TAG, "Mode cannot change unless create a new one");
            return this;
        } else {
            this.mode = mode;
            switch (mode) {
                case TCP:
                    try {
                        socket_tcp = new ImageSocket_TCP(host, port);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return this;
                case UDP:
                    socket_udp = new ImageSocket_UDP(host, port);
                    return this;
                default:
                    Log.e(TAG, "Wrong Mode Number");
                    return null;
            }
        }
    }

    public ImageSocket showProtocol() {
        if (mode == TCP)
            Log.v(TAG, "protocol為：TCP");
        if (mode == UDP)
            Log.v(TAG, "protocol為：UDP");
        if (mode == DEF)
            Log.v(TAG, "protocol為：None");

        return this;
    }

    // UDP mode: true to check the socket is open
    public ImageSocket getSocket(boolean have_to_check_if_port_is_availiable) throws IOException {
        if (mode == TCP)
            Log.e(TAG, "TCP mode cannot use this function");
        else if (mode == DEF)
            PROTO_ERROR("getSocket(boolean)");
        else {
            if (have_to_check_if_port_is_availiable)
                socket_udp.getSocketWithCheck();
            else
                socket_udp.getSocketWithoutCheck();
        }
        return this;
    }

    // TCP mode: the number to assign how many time to check
    public ImageSocket getSocket(int times_to_reconnect_if_connect_fail) throws IOException {
        if (mode == UDP)
            Log.e(TAG, "UDP mode cannot use this function");
        else if (mode == DEF)
            PROTO_ERROR("getSocket(int)");
        else {
            keepConnect(times_to_reconnect_if_connect_fail);
        }
        return this;
    }

    // The Image socket can set the time to keep connecting if it fail at first
    protected ImageSocket keepConnect(int timeRepeatConnect) throws IOException {
        if (socket_tcp != null)
            socket_tcp.keepConnect(timeRepeatConnect);
        else if (socket_udp != null)
            Log.e(TAG, "UDP mode cannot use this function");
        else
            PROTO_ERROR("keepConnect(int)");
        return this;
    }

    // Set the Opposite port number (UDP)
    public ImageSocket setOppoPort(int port) {
        if (mode == TCP)
            Log.e(TAG, "TCP mode cannot use this function");
        else if (mode == DEF)
            PROTO_ERROR("setOppoPort(int)");
        else {
            socket_udp.setOppoPort(port);
        }
        return this;
    }

    // Inherit the usage of the socket(didn't return the real inputStream)
    public ImageSocket getInputStream() throws IOException {
        if (socket_tcp != null)
            socket_tcp.getInputStream();
        else if (socket_udp != null)
            Log.e(TAG, "UDP mode cannot use this function");
        else
            PROTO_ERROR("getInputStream()");
        return null;
    }

    // Close the image socket
    public void close() throws IOException {
        if (socket_tcp != null)
            socket_tcp.close();
        else if (socket_udp != null)
            socket_udp.close();
    }

    // Connect to the server
    public ImageSocket connect() throws IOException {
        if (socket_tcp != null)
            socket_tcp.connect();
        else if (socket_udp != null)
            Log.e(TAG, "UDP mode cannot use this function");
        return this;
    }

    // Send the Image
    public ImageSocket send(Bitmap bitmap) throws IOException, InterruptedException {
        if (socket_tcp != null)
            socket_tcp.send(bitmap);
        else if (socket_udp != null)
            socket_udp.send(bitmap);
        return this;
    }

    // Get the time of sending image
    public long getSendTime() {
        if (socket_tcp != null)
            return socket_tcp.getSendTime();
        else if (socket_udp != null)
            return socket_udp.getSendTime();
        else {
            PROTO_ERROR("getSendTime()");
            return -1;
        }
    }

    private void PROTO_ERROR(String name){
        Log.e(TAG, "ImageSocket Error: fail to do the process " + name);
        Log.e(TAG, "\tmight didn't connect to the PC under tcp mode");
        Log.e(TAG, "\tmight forget to set the protocol");
        Log.e(TAG, "\tPlease check the order of the function call");
    }

}

