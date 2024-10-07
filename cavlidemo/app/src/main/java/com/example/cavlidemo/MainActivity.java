package com.example.cavlidemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import vendor.cavli.hardware.uart.CavliUart;
import vendor.cavli.hardware.uart.ICavliUartCallback;
import vendor.cavli.hardware.uart.V1_0.IUartCallback;
import vendor.cavli.hardware.uart.V1_0.UartBaudRate;
import vendor.cavli.hardware.uart.V1_0.UartConfig;
import vendor.cavli.hardware.uart.V1_0.UartHardwareFlowControl;
import vendor.cavli.hardware.uart.V1_0.UartParity;

public class MainActivity extends AppCompatActivity {
    CavliUart uart;
    String str = "";
    private final BlockingQueue<ArrayList<Byte>> packageQueue = new LinkedBlockingQueue<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.textView);

        uart = new CavliUart(Looper.getMainLooper(), new ICavliUartCallback() {
            @Override
            public void onDataReceived(ArrayList<Byte> arrayList) throws RemoteException {
                try {
                    packageQueue.put(arrayList);
                } catch (Exception e) {
                    Log.i("cavlidemo", "Push blockingQueue + " + e.getMessage() );
                }
            }
        });

        UartConfig config = new UartConfig();
        uart.open_port("/dev/ttyHS0");
        config.baudRate = UartBaudRate.BAUD_921600;
        config.parity = UartParity.NONE;
        config.hardwareFlowControl = UartHardwareFlowControl.NONE;
        Log.i("cavlidemo", "run configure");
        uart.configure(config);

        Button btn = findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";
                ArrayList<Byte> send_pkg = new ArrayList<>();
                input.chars().forEach(c -> send_pkg.add((byte) c));
                uart.transmit(send_pkg);
                try {
                    ArrayList<Byte> recv_pkg = packageQueue.poll( 1, TimeUnit.SECONDS);
                    if (recv_pkg.equals(send_pkg)) {
                        tv.setText("PASS");
                    } else {
                        tv.setText("FAILED");
                    }
                } catch (Exception e) {
                    Log.i("cavlidemo", "Poll blockingQueue + " + e.getMessage() );
                }
            }
        });

    }
}