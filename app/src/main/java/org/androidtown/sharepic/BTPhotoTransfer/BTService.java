package org.androidtown.sharepic.BTPhotoTransfer;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.androidtown.sharepic.MyApplication;
import org.androidtown.sharepic.btxfr.ClientThread;
import org.androidtown.sharepic.btxfr.ProgressData;
import org.androidtown.sharepic.btxfr.ServerThread;

import java.util.Set;

public class BTService extends Service {
    private static String TAG = "BTPHOTO/BTService";
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
    protected static BluetoothDevice device;
    // 외부로 데이터를 전달하려면 바인더 사용

    // Binder 객체는 IBinder 인터페이스 상속구현 객체입니다
    //public class Binder extends Object implements IBinder

    //기연추가
   // protected static SelectBT2.DeviceData deviceData; //액티비티에서 이용할 서비스
    //서비스에서 다루는 디바이스 데이터

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication myapp = (MyApplication)getApplication();
        if(!myapp.isOn()) {
            adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                if (adapter.isEnabled()) {
                    pairedDevices = adapter.getBondedDevices();
                } else {
                    Log.e(TAG, "Bluetooth is not enabled");
                    disableType = false;
                }
            } else {
                Log.e(TAG, "Bluetooth is not supported on this device");
                disableType = true;
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        adapter = null;
        super.onDestroy();
    }

}