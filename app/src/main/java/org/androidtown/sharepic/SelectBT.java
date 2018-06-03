package org.androidtown.sharepic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SelectBT extends AppCompatActivity {

    static final int REQUEST_ENABLE_BT = 10;
    int data = 0;
    BluetoothAdapter mBluetoothAdapter;
    char mCharDelimiter = '\n';
    Set<BluetoothDevice> mDevices;
    InputStream mInputStream = null;
    OutputStream mOutputStream = null;
    int mPairedDeviceCount = 0;
    BluetoothDevice mRemoteDevice;
    //  private SensorManager mSensorManager;
    BluetoothSocket mSocket = null;
    String mStrDelimiter = "\n";
    Thread mWorkerThread = null;
    byte[] readBuffer;
    int readBufferPosition;
    String[] temp;

    void checkBluetooth()
    {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (this.mBluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "기기가 블루투스를 지원하지 않습니다.",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!this.mBluetoothAdapter.isEnabled())
        {
            Toast.makeText(getApplicationContext(), "현재 블루투스가 비활성화 상태입니다.",Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 10);
            return;
        }
        selectDevice();
    }



    void connectToSelectedDevice(String paramString)
    {
        this.mRemoteDevice = getDeviceFromBondedList(paramString);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try
        {
            this.mSocket = this.mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
            this.mSocket.connect();
            this.mOutputStream = this.mSocket.getOutputStream();
            this.mInputStream = this.mSocket.getInputStream();
            sendData("t");
            return;
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"블루투스 연결 중 오류 발생",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    BluetoothDevice getDeviceFromBondedList(String paramString)
    {
        Iterator localIterator = this.mDevices.iterator();
        BluetoothDevice localBluetoothDevice;
        do
        {
            if (!localIterator.hasNext()) {
                return null;
            }
            localBluetoothDevice = (BluetoothDevice)localIterator.next();
        } while (!paramString.equals(localBluetoothDevice.getName()));
        return localBluetoothDevice;
    }

    protected void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
    {
        switch (paramInt1)
        {
        }
        for (;;)
        {
            super.onActivityResult(paramInt1, paramInt2, paramIntent);
            // return;
            if (paramInt2 == -1)
            {
                selectDevice();
            }
            else if (paramInt2 == 0)
            {
                Toast.makeText(getApplicationContext(), "블루투스를 사용할 수 없어 종료합니다.",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_bt);
    }

    public void connectBT1(View view) {
        checkBluetooth();
    }

    public boolean onCreateOptionsMenu(Menu paramMenu)
    {
        //  getMenuInflater().inflate(2131492864, paramMenu);
        return true;
    }

    protected void onDestroy()
    {
        try
        {
            // this.mWorkerThread.interrupt();
            this.mInputStream.close();
            this.mOutputStream.close();
            this.mSocket.close();
            super.onDestroy();
            return;
        }
        catch (Exception localException)
        {
            for (;;) {}
        }
    }

    void selectDevice()
    {
        this.mDevices = this.mBluetoothAdapter.getBondedDevices();
        this.mPairedDeviceCount = this.mDevices.size();
        if (this.mPairedDeviceCount == 0)
        {
            Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setTitle("블루투스 장치 선택");
        List<String> listItems = new ArrayList<String>();
        for(BluetoothDevice device : mDevices){
            listItems.add(device.getName());
        }
        listItems.add("취소");

        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        localBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which== SelectBT.this.mPairedDeviceCount){
                    Toast.makeText(SelectBT.this.getApplicationContext(), "연결할 장치를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
                    SelectBT.this.finish();
                    return;
                }
                else{
                    connectToSelectedDevice(items[which].toString());
                }
            }
        });

        localBuilder.setCancelable(false);
        AlertDialog alert = localBuilder.create();
        alert.show();

    }

    void sendData(String paramString)
    {
        paramString = paramString + this.mStrDelimiter;
        try
        {
            this.mOutputStream.write(paramString.getBytes());
            return;
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "데이터 전송 중 오류 발생.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // 카메라 activity로
    public void toCamera(View view) {
        Intent intent = new Intent(this, PictureActivity.class);
        startActivity(intent);
    }
}

