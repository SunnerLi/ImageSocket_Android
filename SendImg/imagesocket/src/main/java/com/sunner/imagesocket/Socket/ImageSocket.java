package com.sunner.imagesocket.Socket;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;

import com.sunner.imagesocket.Log.ImageSocketLog;

import java.io.IOException;
import java.util.UUID;

/**
 * <p>
 * <font color=green>
 * This class define the operation of image with imageSocket<br>
 * Please read the wiki in github if you want to know the usage before development.<br>
 * </font>
 * <p>
 */
public class ImageSocket {
    String TAG = "資訊";

    // The Transfer Protocol
    private final static int DEF = -1;
    public final static int TCP = 0;
    public final static int UDP = 1;
    public final static int BT = 2;
    private int mode = DEF;

    // Log
    public final static int forbid_all = 3;
    public final static int forbid_verbose = 4;


    private ImageSocket_TCP socket_tcp = null;
    private ImageSocket_UDP socket_udp = null;
    private ImageSocket__BT socket__bt = null;
    private String host = null;
    private int port = -1;

    // Bluetooth extension
    private BluetoothAdapter btAdapter;
    private UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    /**
     * <p>
     * <font color=green>
     * Constructor
     * </font>
     * <p>
     *
     * @param host: The host address
     * @param port: The host port number
     */
    public ImageSocket(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * <p>
     * <font color=green>
     * Constructor
     * </font>
     * <p>
     *
     * @param bluetoothAdapter: The bluetooth adapter object to get the socket
     * @param host:             The host address
     */
    public ImageSocket(BluetoothAdapter bluetoothAdapter, String host) {
        this.host = host;
        btAdapter = bluetoothAdapter;
        mode = BT;
        socket__bt = new ImageSocket__BT(btAdapter, host, uuid);
    }

    /**
     * <p>
     *  <font color=green>
     *      Set the transferring protocol.
     *  </font>
     *  <font color=red>
     *      This function should call it first, or the backward function would tell error!!!
     *  </font>
     * </p>
     *
     * @param mode: The mode number.<br>
     *              There're two mode constant you can choose:<br>
     *              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 1. TCP<br>
     *              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 2. UDP
     * @return ImageSocket object
     * @throws IOException
     */
    public ImageSocket setProtocol(int mode) throws IOException {
        if (this.mode != DEF) {
            if (this.mode == UDP && mode == UDP) {
                socket_udp = new ImageSocket_UDP(host, port);
                return this;
            } else if (this.mode == TCP && mode == TCP) {
                socket_tcp = new ImageSocket_TCP(host, port);
                return this;
            } else if (this.mode == BT && mode == BT) {
                socket__bt = new ImageSocket__BT(btAdapter, host, uuid);
                return this;
            } else
                ImageSocketLog.e(TAG, "Mode cannot change unless create a new one");
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
                case BT:
                    socket__bt = new ImageSocket__BT(btAdapter, host, uuid);
                    return this;
                default:
                    ImageSocketLog.e(TAG, "Wrong Mode Number");
                    return null;
            }
        }
    }

    /**
     * <p>
     *  <font color=green>
     *      See the protocol after you had setted.
     *  </font>
     * <p>
     *
     * @return ImageSocket object
     */
    public ImageSocket showProtocol() {
        if (mode == TCP)
            ImageSocketLog.v(TAG, "protocol為：TCP");
        if (mode == UDP)
            ImageSocketLog.v(TAG, "protocol為：UDP");
        if (mode == BT)
            ImageSocketLog.v(TAG, "protocol為：BT");
        if (mode == DEF)
            ImageSocketLog.v(TAG, "protocol為：None");

        return this;
    }


    /**
     * <p>
     *  <font color=green>
     *      UDP mode: true to check the socket is open
     *  </font>
     * <p>
     *
     * @param have_to_check_if_port_is_availiable <br>
     *        : If you want to check if the port is availiable
     * @return ImageSocket object
     * @throws IOException
     */
    public ImageSocket getSocket(boolean have_to_check_if_port_is_availiable) throws IOException {
        if (mode == TCP || mode == BT)
            ImageSocketLog.e(TAG, "TCP or BT mode cannot use this function");
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


    /**
     * <p>
     *  <font color=green>
     *      TCP mode: the number to assign how many time to check
     *  </font>
     * <p>
     *
     * @param times_to_reconnect_if_connect_fail <br>
     *        : The number of time you want to re-connect after the socket connect fail
     * @return ImageSocket object
     * @throws IOException
     */
    public ImageSocket getSocket(int times_to_reconnect_if_connect_fail) throws IOException {
        if (mode == UDP || mode == BT)
            ImageSocketLog.e(TAG, "UDP or BT mode cannot use this function");
        else if (mode == DEF)
            PROTO_ERROR("getSocket(int)");
        else {
            keepConnect(times_to_reconnect_if_connect_fail);
        }
        return this;
    }


    /**
     * <p>
     *  <font color=green>
     *      (Protect Function)
     *      The Image socket can set the time to keep connecting if it fail at first
     *  </font>
     * <p>
     *
     * @param timeRepeatConnect <br>
     *        : The number of time you want to re-connect after the socket connect fail
     * @return ImageSocket object
     * @throws IOException
     */
    protected ImageSocket keepConnect(int timeRepeatConnect) throws IOException {
        if (socket_tcp != null)
            socket_tcp.keepConnect(timeRepeatConnect);
        else if (socket_udp != null)
            ImageSocketLog.e(TAG, "UDP mode cannot use this function");
        else if (socket__bt != null)
            socket__bt.keepConnect(timeRepeatConnect);
        else
            PROTO_ERROR("keepConnect(int)");
        return this;
    }


    /**
     * <p>
     *  <font color=green>
     *      Set the Opposite port number (UDP)
     *  </font>
     * <p>
     *
     * @param port <br>
     *        : The destination port number
     * @return ImageSocket object
     */
    public ImageSocket setOppoPort(int port) {
        if (mode == TCP || mode == BT)
            ImageSocketLog.e(TAG, "TCP or BT mode cannot use this function");
        else if (mode == DEF)
            PROTO_ERROR("setOppoPort(int)");
        else {
            socket_udp.setOppoPort(port);
        }
        return this;
    }

    /**
     * <p>
     *  <font color=green>
     *      Inherit the usage of the socket(didn't return the real inputStream)
     *  </font>
     * <p>
     *
     * @return ImageSocket object
     * @throws IOException
     */
    public ImageSocket getOutputStream() throws IOException {
        if (socket_tcp != null)
            socket_tcp.getOutputStream();
        else if (socket__bt != null)
            socket__bt.getOutputStream();
        else if (socket_udp != null)
            ImageSocketLog.e(TAG, "UDP mode cannot use this function");
        else
            PROTO_ERROR("getInputStream()");
        return null;
    }


    /**
     * <p>
     *  <font color=green>
     *      Close the image socket
     *  </font>
     * <p>
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (socket_tcp != null)
            socket_tcp.close();
        else if (socket_udp != null)
            socket_udp.close();
        else if (socket__bt != null)
            socket__bt.close();
    }


    /**
     * <p>
     *  <font color=green>
     *      Connect to the server (TCP or Bluetooth)
     *  </font>
     * <p>
     *
     * @return ImageSocket object
     * @throws IOException
     */
    public ImageSocket connect() throws IOException {
        if (socket_tcp != null)
            socket_tcp.connect();
        else if (socket__bt != null)
            socket__bt.connect();
        else if (socket_udp != null)
            ImageSocketLog.e(TAG, "UDP mode cannot use this function");
        return this;
    }


    /**
     * <p>
     *  <font color=green>
     *      Send the Image
     *  </font>
     * <p>
     *
     * @param bitmap <br>
     *        : The bitmap image you want to send
     * @return ImageSocket object
     * @throws IOException
     * @throws InterruptedException
     */
    public ImageSocket send(Bitmap bitmap) throws IOException, InterruptedException {
        if (socket_tcp != null)
            socket_tcp.send(bitmap);
        else if (socket_udp != null)
            socket_udp.send(bitmap);
        else if (socket__bt != null)
            socket__bt.send(bitmap);
        return this;
    }


    /**
     * <p>
     *  <font color=green>
     *      Get the time of sending image
     *  </font>
     * <p>
     *
     * @return the time to send. (ms)
     */
    public long getSendTime() {
        if (socket_tcp != null)
            return socket_tcp.getSendTime();
        else if (socket_udp != null)
            return socket_udp.getSendTime();
        else if (socket__bt != null)
            return socket__bt.getSendTime();
        else {
            PROTO_ERROR("getSendTime()");
            return -1;
        }
    }

    private void PROTO_ERROR(String name) {
        ImageSocketLog.e(TAG, "ImageSocket Error: fail to do the process " + name);
        ImageSocketLog.e(TAG, "\tmight didn't connect to the PC under tcp mode");
        ImageSocketLog.e(TAG, "\tmight forget to set the protocol");
        ImageSocketLog.e(TAG, "\tPlease check the order of the function call");
    }

    /**
     * <p>
     *  <font color=green>
     *      Disable log with specific command.
     *  </font>
     * <p>
     *
     * @param command <br>
     *                1. forbid_all   : forbid all log, including error<br>
     *                2. forbid_vorbus: forbit only verbose log
     * @return ImageSocket object
     */
    public ImageSocket disableLog(int command) {
        switch (command) {
            case forbid_all:
                ImageSocketLog.shutUp();
                break;
            case forbid_verbose:
                ImageSocketLog.normalDebug();
                break;
            default:
                ImageSocketLog.e(TAG, "Invalid command");
                break;
        }
        return this;
    }
}

