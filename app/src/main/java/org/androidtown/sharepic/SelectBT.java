package org.androidtown.sharepic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_bt);
        //checkBluetooth();
    }

    void checkBluetooth()
    {
        //장치가 블루투스 지원하지 않는 경우
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (this.mBluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "기기가 블루투스를 지원하지 않습니다.",Toast.LENGTH_SHORT).show();
            finish();
        } else{ //장치가 블루투스 지원하는 경우
            if (!this.mBluetoothAdapter.isEnabled()) //지원하지만 비활성화 상태
            {
                Toast.makeText(getApplicationContext(), "현재 블루투스가 비활성화 상태입니다.",Toast.LENGTH_SHORT).show();
                //활성화 상태 변환 위해 퍼미션 요청
                startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), REQUEST_ENABLE_BT);
            }
            else{ //블투 지원하며 활성 상태이면 기기선택
                selectDevice();
            }
        }


    }


//장치 연결 (최종)
    void connectToSelectedDevice(String paramString)
    {
        mRemoteDevice = getDeviceFromBondedList(paramString);
        //범용 고유 번호

        try
        {
            //소켓 생성
            mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            //소켓 연결
            mSocket.connect();
            //데이터 송수신 위해 스트림개설 -> onDestroy에서 닫음
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();
        }
        catch (Exception e) //연결도중 오류나면
        {
            Toast.makeText(getApplicationContext(),"블루투스 연결 중 오류 발생",Toast.LENGTH_SHORT).show();
            finish(); //종료
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

    //블투 미연결시 퍼미션 제공 후 사용자가 아니요/ 예 선택후 코드
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK){ //사용자가 예 눌렀을때 블투 활성 상태로 변경
                    selectDevice();
                }
                else if(resultCode == RESULT_CANCELED){ //아니요 눌러서 블투 비활성
                    Toast.makeText(getApplicationContext(), "블루투스를 사용할 수 없어 종료합니다.",Toast.LENGTH_SHORT).show();
                    finish(); // 종료
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    //앱 종료될 때 입출력 스트림 소켓 닫음
    protected void onDestroy()
    {
        try
        {
            // this.mWorkerThread.interrupt();
            mInputStream.close();
            mOutputStream.close();
            mSocket.close();
            super.onDestroy();
            return;
        }
        catch (Exception localException)
        {
            for (;;) {}
        }
    }

    //페어링된 장치 선택
    void selectDevice()
    {
        this.mDevices = this.mBluetoothAdapter.getBondedDevices();
        this.mPairedDeviceCount = this.mDevices.size();
        if (this.mPairedDeviceCount == 0) //페어링된 장치가 없는경우
        {
            Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 종료
        } else{ // 있는경우
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
            localBuilder.setTitle("블루투스 장치 선택");

            //페어링 된 블투 장치 이름 목록 표시
            List<String> listItems = new ArrayList<String>();
            for(BluetoothDevice device : mDevices){
                listItems.add(device.getName());
            }
            listItems.add("취소"); //취소항목 추가

            final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
            localBuilder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which== mPairedDeviceCount){ //취소 누른 경우
                        Toast.makeText(SelectBT.this.getApplicationContext(), "연결할 장치를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SelectBT.this, MainActivity.class));
                    }
                    else{ //장치 선택한 경우
                        connectToSelectedDevice(items[which].toString());
                    }
                }
            });

            localBuilder.setCancelable(false); //뒤로가기 버튼 사용 금지
            AlertDialog alert = localBuilder.create();
            alert.show();
        }


    }


    // 카메라 activity로
    public void toCamera(View view) {
        Intent intent = new Intent(this, PictureActivity.class);
        startActivity(intent);
    }

    public void takePic(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.takePicFrame, new TakePhotoFragment())
                .commit();
    }
}

