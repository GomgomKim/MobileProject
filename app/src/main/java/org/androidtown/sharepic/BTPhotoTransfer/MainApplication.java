package org.androidtown.sharepic.BTPhotoTransfer;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.util.Log;

import org.androidtown.sharepic.btxfr.ClientThread;
import org.androidtown.sharepic.btxfr.ProgressData;
import org.androidtown.sharepic.btxfr.ServerThread;

import java.util.Set;

public class MainApplication extends Application {
    private static String TAG = "BTPHOTO/MainApplication";
    protected static BluetoothAdapter adapter;
    protected static Set<BluetoothDevice> pairedDevices;
    protected static Handler clientHandler;
    protected static Handler serverHandler;
    protected static ClientThread clientThread;
    protected static ServerThread serverThread;
    protected static ProgressData progressData = new ProgressData();
    protected static final String IMAGE_FILE_NAME = "nr";
    protected static final int PICTURE_RESULT_CODE = 1234;
    protected static final int REQUEST_ENABLE_BT = 10;
    protected static final int IMAGE_QUALITY = 100;
    protected static boolean disableType; // false면 not support, true면 not enable

    @Override
    public void onCreate() {
        super.onCreate();
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (adapter.isEnabled()) {
                pairedDevices = adapter.getBondedDevices();
            } else {
                Log.e(TAG, "Bluetooth is not enabled");
                disableType=false;
            }
        } else {
            Log.e(TAG, "Bluetooth is not supported on this device");
            disableType=true;
        }
    }


}
