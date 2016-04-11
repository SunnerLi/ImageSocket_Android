package com.example.sunner.sendimg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sunner.imagesocket.Socket.ImageSocket;

import java.io.IOException;

public class UDPSend extends AppCompatActivity {
    Button button;
    String oppositeHost = "192.168.0.100";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udpsend);
    }

    @Override
    protected void onResume() {
        super.onResume();
        button = (Button)findViewById(R.id.btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        super.run();

                        ImageSocket imageSocket = new ImageSocket(oppositeHost, 12345);
                        try {
                            imageSocket.setProtocol(ImageSocket.UDP)                                // Must set protocol first!!!

                                    .getSocket(true)                                                // Set If need to check the port is occupied

                                    .setOppoPort(12345);                                            // Set the destination PC port number

                            imageSocket.send(getImage());                                           // The input is bitmap only

                            imageSocket.close();                                                    // Close the socket at final
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
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
}
