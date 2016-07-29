package com.sunner.imagesocket.Packet;

import android.util.Base64;

import com.sunner.imagesocket.Log.ImageSocketLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * Created by sunner on 2016/7/28.
 */
public class BTPacket {
    int payloadMaxLength = 60000;
    String TAG = "藍牙封包";

    // Set the max length of payload
    public BTPacket setMaxLengthPayload(int length) {
        payloadMaxLength = length;
        return this;
    }

    // 編碼(default)
    public byte[] encode(String payload, int imageIndex) {
        // 第 1 個參數為image之index
        // 第 3 個參數表示是否為結尾   ( 0 為結尾 )
        return encode(payload, imageIndex, (payload.length() >= payloadMaxLength ? 1 : 0));
    }

    // 編碼實作
    public byte[] encode(String payload, int imageIndex, int marker) {
        int header = 0;
        header = (imageIndex & 0xFF) << 1;
        header = header | marker;
        return Base64Encode(header, payload);
    }

    // 解碼實作
    public void decode(byte[] packet) {
        int header = Base64Decode(packet);
        int marker = header | 0x01;
        int imageIndex = header >> 1;
    }

    /*----------------------------------------------------------------------------------------------
     *                                    Array Transform
     *----------------------------------------------------------------------------------------------*/
    public byte[] int2ByteArr(int _int) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        ImageSocketLog.i(TAG, "length: " + intBuffer.capacity());
        intBuffer.put(_int);
        return byteBuffer.array();
    }

    public int byteArr2Int(byte[] bytes) {
        IntBuffer intBuf = ByteBuffer.wrap(bytes)
                .order(ByteOrder.BIG_ENDIAN)
                .asIntBuffer();
        int[] array = new int[intBuf.remaining()];
        intBuf.get(array);
        return array[0];
    }

    /*----------------------------------------------------------------------------------------------
     *                                    Base64 En/Decode
     *----------------------------------------------------------------------------------------------*/

    public byte[] Base64Encode(int header, String payload) {
        byte[] headerBytes = int2ByteArr(header);
        String headerString = new String(Base64.encode(headerBytes, Base64.DEFAULT));

        ImageSocketLog.v(TAG, "Encode header string長度：" + headerString.length());
        ImageSocketLog.v(TAG, "Encode payload string長度：" + payload.length());


        return (headerString + payload).getBytes();
    }

    public int Base64Decode(byte[] packet) {
        String wholeString = new String(packet);
        String headerString = wholeString.substring(0, 65);
        String payload = wholeString.substring(65);

        int header = byteArr2Int(Base64.decode(headerString.getBytes(), Base64.DEFAULT));
        ImageSocketLog.v(TAG, "Decode header string長度：" + Base64.decode(headerString.getBytes(), Base64.DEFAULT).length);
        return header;
    }
}
