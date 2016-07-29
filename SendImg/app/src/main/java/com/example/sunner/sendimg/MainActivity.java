package com.example.sunner.sendimg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button tcpBtn, udpBtn, btBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get the view of button object
        tcpBtn = (Button)findViewById(R.id.tcp_btn);
        udpBtn = (Button)findViewById(R.id.udp_btn);
        btBtn = (Button)findViewById(R.id.bt_btn);

        // Cancel the whole capital
        tcpBtn.setTransformationMethod(null);
        udpBtn.setTransformationMethod(null);
        btBtn.setTransformationMethod(null);

        tcpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, TCPSend.class);
                startActivity(intent);
            }
        });
        udpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, UDPSend.class);
                startActivity(intent);
            }
        });
        btBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, BTSend.class);
                startActivity(intent);
            }
        });
    }
}
