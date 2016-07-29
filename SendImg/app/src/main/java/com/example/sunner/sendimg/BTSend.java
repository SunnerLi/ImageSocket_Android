package com.example.sunner.sendimg;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sunner.imagesocket.Socket.ImageSocket;

import java.io.IOException;

public class BTSend extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    Button button;
    String oppositeHost = "48:51:B7:D6:62:2A";                                                      // ThinkPad
    int REMIND_OPEN_BLUETOOTH = 1;                                                                  // used in handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btsend);
    }

    @Override
    protected void onResume() {
        super.onResume();
        button = (Button)findViewById(R.id.btn);
        button.setTransformationMethod(null);                                                       // Cancel the whole capital
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (bluetoothAdapter.isEnabled()) {

                            try {
                                ImageSocket imageSocket = new ImageSocket(bluetoothAdapter, oppositeHost);
                                imageSocket.connect();
                                imageSocket.send(getImage());
                                Log.v("SendImg", "傳輸時間(ms):" + imageSocket.getSendTime());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }else{
                            // Send to handler to raise a toast
                            Message message = new Message();
                            message.what = REMIND_OPEN_BLUETOOTH;
                            remindOpen.sendMessage(message);
                        }

                    }
                }.start();
            }
        });
    }

    // Get image from drawable folder
    public Bitmap getImage() {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dog1, opts);
        return bitmap;
    }

    // Remind the user to open the bluetooth
    Handler remindOpen = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == REMIND_OPEN_BLUETOOTH){
                Toast.makeText(BTSend.this, "Please open the bluetooth first !",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
}
