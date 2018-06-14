package org.androidtown.sharepic.BTPhotoTransfer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.androidtown.sharepic.MainActivity;
import org.androidtown.sharepic.MyApplication;
import org.androidtown.sharepic.R;

import java.util.ArrayList;

/*
참조 : 사진을 전송하는 쪽이 클라이언트이고 받는 쪽이 서버입니다.
따라서 클라이언트handler와 서버handler가 나눠져 있습니다.
*/
public class SelectBT2 extends Activity {
    private static final String TAG = "BTPHOTO/SelectBT2";
    private Spinner deviceSpinner;
    private ProgressDialog progressDialog;
    private MyDBHandler dbHandler;
    private static final int PERMISSION_REQUEST_CODE = 123;
    public static final int BT_DISABLE = 0;
//    private final String sendStringPath = Environment.getExternalStorageDirectory()+"/nerang";

    public ToggleButton clientButton;
    //추가부분
    static final int REQUEST_IMAGE_CAPTURE = 1;

    //ArrayList<DeviceData> deviceDataList;
    //기연추가
    ArrayList<DeviceData> deviceDataList;
    ArrayList<String> spinList;

//    // DB용
//    public static String pathNow;
//    EditText dbtestEdit;
//    GridLayout dbtestGrid;


    ToggleButton toggleButton;  // on / off

    MyApplication myapp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectbt2);
        myapp = (MyApplication)getApplication();

        // 어플 내 앨범 목록

        final MyApplication myapp = (MyApplication)getApplication();

        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        if(myapp.getAlbum_title()==null){
            toggleButton.setChecked(false);
        }else{
            toggleButton.setChecked(true);
        }
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyApplication myapp = (MyApplication)getApplication();
                if(isChecked) {
                    if(myapp.getAlbum_title()==null){
                        start();
                    }
                }
                else {
                    myapp.setAlbum_title(null);
                }
            }
        });

//        //DB테스트
//        dbtestEdit = findViewById(R.id.query);
//        dbtestGrid = findViewById(R.id.queryGrid);

        BTService.pairedDevices = null;

        clientButton = (ToggleButton) findViewById(R.id.clientButton);
        deviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);


        if(myapp.isOn()){
            deviceSpinner.setEnabled(false);
        }
        clientButton.setChecked(myapp.isOn());

        dbHandler = new MyDBHandler(this, null, null, 1); //전송할파일uri 저장하기 위한 db handler입니다.


        if (BTService.adapter.isEnabled()) {
            pairing();
        } else {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), BTService.REQUEST_ENABLE_BT);
        }
    }

    private void pairing() {
        BTService.pairedDevices = BTService.adapter.getBondedDevices();
        if (BTService.pairedDevices != null) {
            if (BTService.pairedDevices.size() == 0) {
                Toast.makeText(this,"There are no paired device. Pairing please.",Toast.LENGTH_SHORT).show();
            } else {
                deviceDataList = new ArrayList<DeviceData>();
             //   spinList = new ArrayList<>();
                for (BluetoothDevice device : BTService.pairedDevices) {
                    deviceDataList.add(new DeviceData(device.getName(), device.getAddress()));
                  //  spinList.add(device.getName()); //기연추가 스핀 목록위해 String 어레이리스트 생성
                }

                //ArrayAdapter<String> deviceArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinList);
                ArrayAdapter<DeviceData> deviceArrayAdapter = new ArrayAdapter<DeviceData>(this, android.R.layout.simple_spinner_item, deviceDataList);
                deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                deviceSpinner.setAdapter(deviceArrayAdapter);


                clientButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) {
                            if(deviceSpinner.isEnabled()) {
                                DeviceData deviceData = (SelectBT2.DeviceData) deviceSpinner.getSelectedItem();
                                for (BluetoothDevice device : BTService.adapter.getBondedDevices()) {
                                    if (device.getAddress().contains(deviceData.getValue())) {
                                        BTService.device = device;
                                        Intent i = new Intent(getApplicationContext(), TransferService.class);
                                        startService(i);
                                    }
                                }
                            }
                            myapp.setOn(true);
                            deviceSpinner.setEnabled(false);
                        }
                        else {
                            if(!deviceSpinner.isEnabled()) {
                                stopService(new Intent(getApplicationContext(), TransferService.class));
                            }
                            myapp.setOn(false);
                            myapp.setAlbum_title(null);
                            deviceSpinner.setEnabled(true);
                            finish();
                        }
                    }
                });
            }
        }
    }

    class DeviceData {
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
    protected void onStop() {
        super.onStop();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == BTService.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) { //연결에 성공하면 pairing한다
                pairing();
            }else{
                startActivityForResult(new Intent(this, MainActivity.class),BT_DISABLE);
            }
        }

    }

    //코드합치기
    // 사진찍고, 내랑폴더 저장

    public void start() {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);

        ad.setTitle("새로운 앨범");       // 제목 설정
        ad.setMessage("이 앨범의 이름을 입력하십시오.");   // 내용 설정

// EditText 삽입하기
        final EditText et = new EditText(this);
        ad.setView(et);

// 확인 버튼 설정
        ad.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v("album", "Yes Btn Click");

                // Text 값 받아서 로그 남기기
                String value = et.getText().toString();
                Log.v("album", value);
                MyApplication myapp = (MyApplication)getApplication();
                myapp.setAlbum_title(value);

                dialog.dismiss();     //닫기
                // Event
            }
        });

// 중립 버튼 설정
//        ad.setNeutralButton("What?", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Log.v(TAG,"Neutral Btn Click");
//                dialog.dismiss();     //닫기
//                // Event
//            }
//        });

// 취소 버튼 설정
        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v("album","No Btn Click");
                dialog.dismiss();     //닫기
                // Event
            }
        });

// 창 띄우기
        ad.show();
    }




//    // 폴더 지정
//    public void selectFolderBTN(View view) {
//        AlertDialog.Builder ad = new AlertDialog.Builder(SelectBT2.this);
//
//        ad.setTitle("앨범 선택");       // 제목 설정
//        ad.setMessage("저장할 폴더 이름을 적어주세요");   // 내용 설정
//
//        final EditText et = new EditText(SelectBT2.this);
//        ad.setView(et);
//
//        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                pathNow = et.getText().toString();
//                dialog.dismiss();
//            }
//        });
//        ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        ad.show();
//    }
//
//    // db테스트용 코드
//    public void queryExec(View view) {
//        String sql = dbtestEdit.getText().toString();
//        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
//        Cursor cursor = dbHandler.selectQuery(sql);
//        cursor.moveToFirst();
//        if (!cursor.isAfterLast()) {
//            int column = cursor.getColumnCount();
//            int row = cursor.getCount();
//            dbtestGrid.removeAllViewsInLayout(); // layout안에 있는 모든 뷰를 없앤당
//            dbtestGrid.setColumnCount(column);
//            dbtestGrid.setRowCount(row + 1);
//            dbtestGrid.setUseDefaultMargins(true);
//
//            for (int i = 0; i < column; i++) {
//                View view1 = getLayoutInflater().inflate(R.layout.row, null);
//                TextView item = view1.findViewById(R.id.item);
//                item.setText(cursor.getColumnName(i));
//                item.setBackgroundColor(Color.LTGRAY);
//                dbtestGrid.addView(view1);
//            }
//            while (!cursor.isAfterLast()) {
//                for (int i = 0; i < column; i++) {
//                    View view1 = getLayoutInflater().inflate(R.layout.row, null);
//                    TextView item = (TextView) view1.findViewById(R.id.item);
//                    item.setText(cursor.getString(i));
//                    dbtestGrid.addView(view1);
//                }
//                cursor.moveToNext();
//            }
//        }
//    }
//
//    public void deletePicture(View view) {
//        // DB를 전부 삭제
//        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
//        dbHandler.deleteAll();
////        if (result) {
////            dbtestEdit.setText("");
////            Toast.makeText(this, "Record Deleted", Toast.LENGTH_SHORT).show();
////        } else {
////            Toast.makeText(this, "No Match Found", Toast.LENGTH_SHORT).show();
////        }
//    }
}