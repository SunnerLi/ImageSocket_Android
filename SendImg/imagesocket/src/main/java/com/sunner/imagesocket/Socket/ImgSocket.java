package com.sunner.imagesocket.Socket;

import android.graphics.Bitmap;
import android.util.Base64;

import com.sunner.imagesocket.Log.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

/**
 * Created by sunner on 2016/4/9.
 */
public class ImgSocket {
    protected String TAG = "資訊";                                                                  // Log tag

    protected int localPort = -1;
    protected String oppoHost = "";
    protected int oppoPort = -1;

    protected static int imageLength = 1380;                                                       // the max length of payload


    // 檢查後5個阜號碼是否閒置
    protected boolean portsAvaliable(int port) {
        for (int i = port; i < port + 5; i++) {
            if (!portAvaliable(i))
                return false;
        }
        return true;
    }

    // 檢查特定1個阜號碼是否閒置
    protected static boolean portAvaliable(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    // 產生新的阜號碼並連接模式代碼
    protected void determineNewPort() {
        // 休息一會兒讓系統釋放阜
        int port = -1;
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        do {
            port = (int) (Math.random() * 10000 + 40000);
        } while (!portsAvaliable(port));
        localPort = port;
    }

    // Bitmap change to String
    protected String bitMap2String(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        Log.v("資訊", "bitmap[0]:" + (bitmap.getPixel(0, 0)));
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    // Set the max length of payload (Default is 60000)
    public void setImageLengthOri(int length) {
        if (length > 60001 || length < 10)                                                            // length should in range 10-60000
            Log.e(TAG, "Invalid image length");
        else
            imageLength = length;
    }
}
