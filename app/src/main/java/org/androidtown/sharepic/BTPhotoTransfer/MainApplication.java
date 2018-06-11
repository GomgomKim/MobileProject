package org.androidtown.sharepic.BTPhotoTransfer;

import android.app.Application;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.androidtown.sharepic.btxfr.ClientThread;
import org.androidtown.sharepic.btxfr.ProgressData;
import org.androidtown.sharepic.btxfr.ServerThread;

import java.util.Set;

public class MainApplication extends Service {
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

    //기연 추가
    IBinder mBinder = new MyBinder();
    //SelectBT2.DeviceData deviceData;
    //protected static DeviceData deviceData;


    static class DeviceData {
        public DeviceData(String spinnerText, String value) {
            this.spinnerText = spinnerText;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String toString() {
            return spinnerText;
        }

        String spinnerText;
        String value;
    }

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

    //기연추가
    class MyBinder extends Binder {
        MainApplication getService(){ //서비스 객체 리턴
            return MainApplication.this;
        }
    }

    //기연추가
    @Nullable
    @Override
    public IBinder onBind(Intent intent) { //액티비티에서 bindService()로 실행된것
        return mBinder; //서비스 객체 리턴
    }

    /*SelectBT2.DeviceData getDeviceData(){
        return deviceData;
    }*/

    //기연추가
    //전달완료
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
