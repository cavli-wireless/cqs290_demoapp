package com.example.cavlidemo;

import android.os.Looper;
import java.util.Random;
import android.os.RemoteException;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import vendor.cavli.hardware.uart.CavliUart;
import vendor.cavli.hardware.uart.ICavliUartCallback;
import vendor.cavli.hardware.uart.V1_0.UartBaudRate;
import vendor.cavli.hardware.uart.V1_0.UartConfig;
import vendor.cavli.hardware.uart.V1_0.UartHardwareFlowControl;
import vendor.cavli.hardware.uart.V1_0.UartParity;

@RunWith(AndroidJUnit4.class)
public class CavliUartTest {
    private static final String TAG = "CavliUartTest";

    private CavliUart uart;
    private final BlockingQueue<ArrayList<Byte>> packageQueue = new LinkedBlockingQueue<>();

    public void openPort() {
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
        uart.open_port("/dev/ttyHS1");
        config.baudRate = UartBaudRate.BAUD_921600;
        config.parity = UartParity.NONE;
        config.hardwareFlowControl = UartHardwareFlowControl.NONE;
        uart.configure(config);
    }

    public void closePort() {
        uart.close_port();
    }

    @Test
    public void A_001_01() throws InterruptedException, RemoteException {
        ArrayList<Byte> send_data = new ArrayList<>();
        send_data.add((byte) 1);
        send_data.add((byte) 2);
        send_data.add((byte) 3);
        openPort();
        int result = uart.transmit(send_data);
        assertEquals("Transmit failed", 0, result);

        ArrayList<Byte> recv_pkg = new ArrayList<Byte>();
        while(true) {
            ArrayList<Byte> tmp_recv_pkg = packageQueue.poll( 2, TimeUnit.SECONDS);
            assertNotNull("recv_pkg null" , tmp_recv_pkg);
            recv_pkg.addAll(tmp_recv_pkg);
            if (recv_pkg.size() == send_data.size()) {
                break;
            }
            assertTrue("What!!!", recv_pkg.size() > send_data.size());
        }
        ArrayListComparator.ComparisonResult comparisonResult = ArrayListComparator.compareByteLists(send_data,recv_pkg);
        assertTrue("Received second data does not match sent data send_data.size=" + send_data.size() + " receivedData.size=" + recv_pkg.size() + " differenceCount="
                        + comparisonResult.differenceCount + " firstDifferenceIndex=" + comparisonResult.firstDifferenceIndex  ,
                comparisonResult.differenceCount == 0);
        closePort();
    }

    @Test
    public void A_001_02() throws InterruptedException, RemoteException {
        ArrayList<Byte> send_data = new ArrayList<>();

        openPort();
        int result = uart.transmit(send_data);
        assertEquals("Transmit failed", 0, result);

        ArrayList<Byte> recv_pkg = packageQueue.poll( 2, TimeUnit.SECONDS);
        assertNull("recv_pkg null" , recv_pkg);

        closePort();
    }

    @Test
    public void A_001_03() throws InterruptedException, RemoteException {
        ArrayList<Byte> send_data = new ArrayList<>();
        for (int i = 0; i < 1024*2; i++) {
            send_data.add((byte)i);
        }

        openPort();
        int result = uart.transmit(send_data);
        assertEquals("Transmit failed", 0, result);

        ArrayList<Byte> recv_pkg = new ArrayList<Byte>();
        while(true) {
            ArrayList<Byte> tmp_recv_pkg = packageQueue.poll( 2, TimeUnit.SECONDS);
            assertNotNull("recv_pkg null" , tmp_recv_pkg);
            recv_pkg.addAll(tmp_recv_pkg);
            if (recv_pkg.size() == send_data.size()) {
                break;
            }
            assertTrue("What!!!", recv_pkg.size() > send_data.size());
        }
        ArrayListComparator.ComparisonResult comparisonResult = ArrayListComparator.compareByteLists(send_data,recv_pkg);
        assertTrue("Received second data does not match sent data send_data.size=" + send_data.size() + " receivedData.size=" + recv_pkg.size() + " differenceCount="
                        + comparisonResult.differenceCount + " firstDifferenceIndex=" + comparisonResult.firstDifferenceIndex  ,
                comparisonResult.differenceCount == 0);
        closePort();
    }

    @Test
    public void A_001_04() throws InterruptedException, RemoteException {
        openPort();
        try {
            int result = uart.transmit(null);
            fail("Expected an exception when transmitting null data");
        } catch (NullPointerException e) {
            // Expected exception
        }
        closePort();
    }

    @Test
    public void A_001_05() throws InterruptedException, RemoteException {
        ArrayList<Byte> firstData = new ArrayList<>();
        firstData.add((byte) 1);
        firstData.add((byte) 2);

        ArrayList<Byte> secondData = new ArrayList<>();
        secondData.add((byte) 3);
        secondData.add((byte) 4);

        openPort();
        int result1 = uart.transmit(firstData);
        assertEquals("First transmit failed", 0, result1);

        ArrayList<Byte> recv_pkg = new ArrayList<Byte>();
        while(true) {
            ArrayList<Byte> tmp_recv_pkg = packageQueue.poll( 2, TimeUnit.SECONDS);
            assertNotNull("recv_pkg null" , tmp_recv_pkg);
            recv_pkg.addAll(tmp_recv_pkg);
            if (recv_pkg.size() == firstData.size()) {
                break;
            }
            assertTrue("What!!!", recv_pkg.size() > firstData.size());
        }
        ArrayListComparator.ComparisonResult comparisonResult = ArrayListComparator.compareByteLists(firstData,recv_pkg);
        assertTrue("Received second data does not match sent data send_data.size=" + firstData.size() + " receivedData.size=" + recv_pkg.size() + " differenceCount="
                        + comparisonResult.differenceCount + " firstDifferenceIndex=" + comparisonResult.firstDifferenceIndex  ,
                comparisonResult.differenceCount == 0);

        int result2 = uart.transmit(secondData);
        assertEquals("Second transmit failed", 0, result2);

        recv_pkg = new ArrayList<Byte>();
        while(true) {
            ArrayList<Byte> tmp_recv_pkg = packageQueue.poll( 2, TimeUnit.SECONDS);
            assertNotNull("recv_pkg null" , tmp_recv_pkg);
            recv_pkg.addAll(tmp_recv_pkg);
            if (recv_pkg.size() == secondData.size()) {
                break;
            }
            assertTrue("What!!!", recv_pkg.size() > secondData.size());
        }
        comparisonResult = ArrayListComparator.compareByteLists(secondData,recv_pkg);
        assertTrue("Received second data does not match sent data send_data.size=" + secondData.size() + " receivedData.size=" + recv_pkg.size() + " differenceCount="
                        + comparisonResult.differenceCount + " firstDifferenceIndex=" + comparisonResult.firstDifferenceIndex  ,
                comparisonResult.differenceCount == 0);
        closePort();
    }

    private void heavyTest(int time, int max_length) throws InterruptedException, RemoteException {
        Random random = new Random();
        for(int j = 0; j < time ; j++ ) {
            int length = random.nextInt(max_length);
            if (length==0) {
                length=max_length;
            }
            ArrayList<Byte> send_data = new ArrayList<>();

            for (int i = 0; i < length; i++) {
                send_data.add((byte)random.nextInt(256));
            }

            int result = uart.transmit(send_data);
            assertEquals("Transmit failed", 0, result);

            ArrayList<Byte> recv_pkg = new ArrayList<Byte>();
            while(true) {
                ArrayList<Byte> tmp_recv_pkg = packageQueue.poll( 2, TimeUnit.SECONDS);
                assertNotNull("recv_pkg null" , tmp_recv_pkg);
                recv_pkg.addAll(tmp_recv_pkg);
                if (recv_pkg.size() == send_data.size()) {
                    break;
                }
                assertFalse("What!!!" + recv_pkg.size() + " " + send_data.size(), recv_pkg.size() > send_data.size());
            }

            ArrayListComparator.ComparisonResult comparisonResult = ArrayListComparator.compareByteLists(send_data,recv_pkg);
            assertEquals("Received second data does not match sent data index=" + j + " send_data.size=" + send_data.size() + " receivedData.size=" + recv_pkg.size() + " differenceCount="
                            + comparisonResult.differenceCount + " firstDifferenceIndex=" + comparisonResult.firstDifferenceIndex  ,
                    0, comparisonResult.differenceCount);

            // Log progress every 100 iterations
            if (j % 100 == 0) {
                Log.i(TAG, "Progress: " + j + " / " + time);
                // Also log differences if any
                if (comparisonResult.differenceCount > 0) {
                    Log.w(TAG, "Difference found at iteration " + j + ": " +
                            "differenceCount=" + comparisonResult.differenceCount +
                            ", firstDifferenceIndex=" + comparisonResult.firstDifferenceIndex);
                }
            }
        }
    }

    @Test
    public void A_001_06() throws InterruptedException, RemoteException {
        openPort();
        openPort();
        heavyTest(2048, 2048);
        closePort();
    }
    @Test
    public void A_001_07() throws InterruptedException, RemoteException {
        openPort();
        heavyTest(2048, 2048);
        closePort();
        openPort();
        heavyTest(2048, 2048);
        closePort();
    }
    @Test
    public void A_001_08() throws InterruptedException, RemoteException {
        openPort();
        heavyTest(64*1024, 2048);
        closePort();
    }
}
