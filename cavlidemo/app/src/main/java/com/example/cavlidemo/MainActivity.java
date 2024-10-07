package com.example.cavlidemo;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private CavliUart uart;
    private BlockingQueue<ArrayList<Byte>> packageQueue ;
    private Spinner flowControlSpinner;
    private Spinner baudSpinner;
    private final int SIZE = 2048;
    private final String dev_name = "/dev/ttyHS1";
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.getMainLooper());
    @Override
    protected  void onDestroy() {
        super.onDestroy();
        Log.i("cavlidemo", "onDestroy");
        uart.close_port();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.textView);
        TextView tv_recv = findViewById(R.id.textView4);
        packageQueue = new LinkedBlockingQueue<>();

        HandlerThread handlerThread = new HandlerThread("CavliUartThread");
        handlerThread.start();
        uart = new CavliUart(handlerThread.getLooper(), new ICavliUartCallback() {
            @Override
            public void onDataReceived(ArrayList<Byte> arrayList) throws RemoteException {
                try {
                    tv_recv.setText(arrayList.toString());
                    packageQueue.put(arrayList);
                } catch (Exception e) {
                    Log.i("cavlidemo", "Push blockingQueue + " + e.getMessage() );
                }
            }
        });

        uart.open_port(dev_name);
        UartConfig config = new UartConfig();
        config.baudRate = UartBaudRate.BAUD_921600;
        config.parity = UartParity.NONE;
        config.hardwareFlowControl = UartHardwareFlowControl.NONE;
        uart.configure(config);
        Button btn = findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                packageQueue.clear();
                tv_recv.setText("");
                tv.setText("");

                ArrayList<Byte> send_data = new ArrayList<>();
                for(int i=0;i<SIZE;i++)
                {
                    send_data.add( (byte)(i) );
                }
                long startTime = System.nanoTime();

                uart.transmit(send_data);
                try {
                    ArrayList<Byte> recv_pkg, total_pkg = new ArrayList<Byte>();
                    boolean done=false;
                    int cnt=0;
                    while (!done) {
                        recv_pkg = packageQueue.poll( 4000, MILLISECONDS);
                        if ( null != recv_pkg ){
                            cnt+=recv_pkg.size();
                            total_pkg.addAll(recv_pkg);
                            if ( cnt == send_data.size() ){
                                done=true;
                            }
                        } else {
                            done=true;
                        }
                    }
                    long endTime = System.nanoTime();
                    long elapsedTime = (endTime - startTime) / 1_000_000;
                    if (null != total_pkg &&  total_pkg.equals(send_data)) {
                        tv.setText("PASS time=" + elapsedTime + "ms");
                    } else {
                        tv.setText("FAILED time=" + elapsedTime + "ms");
                    }
                } catch (Exception e) {
                    tv.setText("FAILED " + e.getMessage());
                    Log.i("cavlidemo", "Poll blockingQueue + " + e.getMessage() );
                }
            }
        });

        flowControlSpinner = findViewById(R.id.spinner2);

        // Set up Spinner selection handling
        flowControlSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = (String) parent.getItemAtPosition(position);

                switch (selectedOption) {
                    case "NONE":
                        Log.i("cavlidemo", "UartHardwareFlowControl.NONE");
                        config.hardwareFlowControl = UartHardwareFlowControl.NONE;
                        break;
                    case "CTS/RTS":
                        Log.i("cavlidemo", "UartHardwareFlowControl.RTS_CTS");
                        config.hardwareFlowControl = UartHardwareFlowControl.RTS_CTS;
                        break;
                    case "XONXOFF":
                        Log.i("cavlidemo", "UartHardwareFlowControl.XON_XOFF");
                        config.hardwareFlowControl = UartHardwareFlowControl.XON_XOFF;
                        break;
                }
                uart.configure(config);
                // Now you can use `config` with the appropriate flow control
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle case where no selection is made
            }
        });
        baudSpinner = findViewById(R.id.spinner3);

        // Set up Spinner selection handling
        baudSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = (String) parent.getItemAtPosition(position);

                switch (selectedOption) {
                    case "9600":
                        config.baudRate = UartBaudRate.BAUD_9600;
                        break;
                    case "19200":
                        config.baudRate = UartBaudRate.BAUD_19200;
                        break;
                    case "38400":
                        config.baudRate = UartBaudRate.BAUD_38400;
                        break;
                    case "57600":
                        config.baudRate = UartBaudRate.BAUD_57600;
                        break;
                    case "115200":
                        config.baudRate = UartBaudRate.BAUD_115200;
                        break;
                    case "230400":
                        config.baudRate = UartBaudRate.BAUD_230400;
                        break;
                    case "460800":
                        config.baudRate = UartBaudRate.BAUD_460800;
                        break;
                    case "921600":
                        config.baudRate = UartBaudRate.BAUD_921600;
                        break;
                    case "1000000":
                        config.baudRate = UartBaudRate.BAUD_1000000;
                        break;
                    case "1152000":
                        config.baudRate = UartBaudRate.BAUD_1152000;
                        break;
                    case "1500000":
                        config.baudRate = UartBaudRate.BAUD_2000000;
                        break;
                    case "2000000":
                        config.baudRate = UartBaudRate.BAUD_2000000;
                        break;
                    case "2500000":
                        config.baudRate = UartBaudRate.BAUD_2000000;
                        break;
                    case "3000000":
                        config.baudRate = UartBaudRate.BAUD_2000000;
                        break;
                    case "3500000":
                        config.baudRate = UartBaudRate.BAUD_2000000;
                        break;
                    case "4000000":
                        config.baudRate = UartBaudRate.BAUD_4000000;
                        break;

                }
                uart.configure(config);
                // Now you can use `config` with the appropriate flow control
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle case where no selection is made
            }
        });
    }
}