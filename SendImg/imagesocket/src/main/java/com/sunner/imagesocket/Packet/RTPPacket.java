package com.sunner.imagesocket.Packet;

import android.util.Base64;

import com.sunner.imagesocket.Log.ImageSocketLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Calendar;

/**
 * <p>
 *  <font color=red>
 *      This class is used to make the RTP package, including the parameter of RTP header<br>
 *      If you are not familiar with the RTP protocol, suggest don't modify this class.<br>
 *  </font>
 * <p>
 */
public class RTPPacket {
    String TAG = "RTP封包";

    /*----------------------------------------------------------------------------------------------
     *                                      Header Variable
     *----------------------------------------------------------------------------------------------*/
    //  version 為版號，目前RTP版號為2
    //  1011 0011 = 1 + 2 + 16 + 32 + 128 = 179
    private static int version = 2;

    //  padding 為加密演算法，目前不使用
    //  1為使用，0為不使用
    private static int padding = 1;

    // extension 為頭部延長(預設不延長)
    // 1為延長，0為不延長
    private static int extension = 1;

    //  cc 為csrc個數，這裡只有手機(1)
    private static int cc = 3;

    //  pt 為type，jpeg設定為1(?)
    private static int pt = 1;

    //  ssrc為ss個數，本插件目前僅支援單播
    private static int ssrc = 1;

    /*----------------------------------------------------------------------------------------------
     *                                      Rest Variable
     *----------------------------------------------------------------------------------------------*/
    int minute = -1, second = -1, millisecond = -1;
    int payloadMaxLength = 60000;


    // 建構式
    public RTPPacket() {

    }

    // Get the current time
    public void getCurrentTime() {
        // 獲取時間資訊
        Calendar calendar = Calendar.getInstance();
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
        millisecond = calendar.get(Calendar.MILLISECOND);
    }

    // Set the max length of payload
    public RTPPacket setMaxLengthPayload(int length) {
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
        int[] header = new int[12];

        header[0] = (header[0] | version << 6);
        header[0] = (header[0] | padding << 5);
        header[0] = (header[0] | extension << 4);                                                   // 是否頭部延長
        header[0] = (header[0] | (cc & 0x0F));                                                      // cc用4個bit
        header[1] = (header[1] | marker << 7);                                                      // 第2個byte的第1個bit
        header[1] = (header[1] | (pt & 0x7F));                                                      // 第2個byte的第2-8個bit
        header[2] = (imageIndex & 0xFF00) >> 8;                                                     // 第3個byte
        header[3] = (imageIndex & 0xFF);                                                            // 第4個
        getCurrentTime();
        header[4] = minute & 0xFF;                                                                  // 32 bit timestamp
        header[5] = second & 0xFF;
        header[6] = (millisecond >> 3) & 0xFF;
        header[7] = (millisecond & 0x07) << 5;
        header[7] = header[7] | (ssrc & 0x0F);                                                      // ssrc用4個bit

        return Base64Encode(header, payload);
    }

    // 解碼實作
    public void decode(byte[] packet) {
        int header[] = Base64Decode(packet);

        int _version = (header[0] >> 6);
        int _padding = (header[0] & 0x20) >> 5;
        int _extension = (header[0] & 0x10) >> 4;
        int _cc = (header[0] & 0x0F);
        int _marker = (header[1] >> 7);
        int _pt = (header[1] | (pt & 0x7F));        // ?????????????????
        int _imageIndex = (header[2] << 8) + header[3];
        int _minute = header[4];
        int _second = header[5];
        int _millisecond = (header[6] << 3) + ((header[7] & 0xE0) >> 5);
        int _ssrc = header[7] & 0x0F;

        /*
        Log.v(TAG, "版本號:" + _version);
        Log.v(TAG, "padding:" + _padding);
        Log.v(TAG, "頭部延長:" + _extension);
        Log.v(TAG, "cc個數:" + _cc);
        Log.v(TAG, "是否為結尾:" + _marker);
        Log.v(TAG, "影像類型:" + _pt);
        Log.v(TAG, "第幾楨:" + _imageIndex);
        Log.v(TAG, "分:" + _minute);
        Log.v(TAG, "秒:" + _second);
        Log.v(TAG, "毫秒:" + _millisecond);
        Log.v(TAG, "ssrc:" + _ssrc);
        */

    }

    /*----------------------------------------------------------------------------------------------
     *                                    Array Transform
     *----------------------------------------------------------------------------------------------*/
    public byte[] intArr2ByteArr(int[] ints) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(ints.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(ints);
        return byteBuffer.array();
    }

    public int[] byteArr2IntArr(byte[] bytes) {
        IntBuffer intBuf = ByteBuffer.wrap(bytes)
                .order(ByteOrder.BIG_ENDIAN)
                .asIntBuffer();
        int[] array = new int[intBuf.remaining()];
        intBuf.get(array);
        return array;
    }

    /*----------------------------------------------------------------------------------------------
     *                                    Base64 En/Decode
     *----------------------------------------------------------------------------------------------*/

    public byte[] Base64Encode(int[] header, String payload) {
        byte[] headerBytes = intArr2ByteArr(header);
        String headerString = new String(Base64.encode(headerBytes, Base64.DEFAULT));

        ImageSocketLog.v(TAG, "Encode header int長度：" + header.length);
        ImageSocketLog.v(TAG, "Encode header string長度：" + headerString.length());
        ImageSocketLog.v(TAG, "Encode payload string長度：" + payload.length());


        return (headerString + payload).getBytes();
    }

    public int[] Base64Decode(byte[] packet) {
        String wholeString = new String(packet);
        String headerString = wholeString.substring(0, 65);
        String payload = wholeString.substring(65);


        int[] header = byteArr2IntArr(Base64.decode(headerString.getBytes(), Base64.DEFAULT));
        ImageSocketLog.v(TAG, "Decode header string長度：" + Base64.decode(headerString.getBytes(), Base64.DEFAULT).length);
        return header;
    }


}
