package org.androidtown.sharepic.BTPhotoTransfer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.androidtown.sharepic.R;
import org.androidtown.sharepic.btxfr.ClientThread;
import org.androidtown.sharepic.btxfr.MessageType;
import org.androidtown.sharepic.btxfr.ProgressData;
import org.androidtown.sharepic.btxfr.ServerThread;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
참조 : 사진을 전송하는 쪽이 클라이언트이고 받는 쪽이 서버입니다.
따라서 클라이언트handler와 서버handler가 나눠져 있습니다.
*/
public class SelectBT2 extends Activity {
    private static final String TAG = "BTPHOTO/SelectBT2";
    private Spinner deviceSpinner;
    private ProgressDialog progressDialog;
    private String fileName; //찍은 사진파일 이름
    private MyDBHandler dbHandler;
    private static final int PERMISSION_REQUEST_CODE = 123;
//    private final String sendStringPath = Environment.getExternalStorageDirectory()+"/nirangnerangSend";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectbt2);

        dbHandler = new MyDBHandler(this, null, null, 1);

        MainApplication.clientHandler = new Handler() {

            @Override
            public void handleMessage(Message message) { //보내는 쪽
                switch (message.what) {
                    case MessageType.READY_FOR_DATA: {
                        try {
                            //전처리 & 전송

                            //권한
                            if (Build.VERSION.SDK_INT >= 23) {
                                if (!checkPermission()) {
                                    requestPermission();
                                }
                            }

                            String sendStringPath =  Environment.getExternalStorageDirectory().toString();
                            fileName = "btimage.jpg"; //임의 지정
                            File file = new File(sendStringPath,fileName);

                            ByteArrayOutputStream compressedImageStream = new ByteArrayOutputStream();
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 2;
                            Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                            image.compress(Bitmap.CompressFormat.JPEG, MainApplication.IMAGE_QUALITY, compressedImageStream);
                            byte[] compressedImage = compressedImageStream.toByteArray();
                            Log.v(TAG, "Compressed image size: " + compressedImage.length);

                            ImageView imageView = (ImageView) findViewById(R.id.imageView);
                            imageView.setImageBitmap(image);

                            // Invoke client thread to send
                            Message imageMessage = new Message();
                            imageMessage.obj = compressedImage;
                            MainApplication.clientThread.incomingHandler.sendMessage(imageMessage);

                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }

                        break;
                    }

                    case MessageType.COULD_NOT_CONNECT: {//전송 불가능한 상태
                        /*todo : 보내려는 파일 db에 저장*/
                        Toast.makeText(SelectBT2.this, "Could not connect to the paired device", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.SENDING_DATA:{
                        break;
                    }

                    case MessageType.DATA_SENT_OK: {
                        Toast.makeText(SelectBT2.this, "Photo was sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.DIGEST_DID_NOT_MATCH: {
                        Toast.makeText(SelectBT2.this, "Photo was sent, but didn't go through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };


        MainApplication.serverHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MessageType.DATA_RECEIVED: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        Bitmap image = BitmapFactory.decodeByteArray(((byte[]) message.obj), 0, ((byte[]) message.obj).length, options);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                        Date currentTime_1 = new Date();
                        String dateString = formatter.format(currentTime_1);
                        saveBitmaptoJpeg(image, "nirang", MainApplication.IMAGE_FILE_NAME + dateString);//파일명예시: nr20180606043124.jpg

                        ImageView imageView = (ImageView) findViewById(R.id.imageView);
                        imageView.setImageBitmap(image);
                        break;
                    }

                    case MessageType.DIGEST_DID_NOT_MATCH: {
                        Toast.makeText(SelectBT2.this, "Photo was received, but didn't come through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.DATA_PROGRESS_UPDATE: { //todo:없애기(우선확인위해남겨둠)
                        // some kind of update
                        MainApplication.progressData = (ProgressData) message.obj;
                        double pctRemaining = 100 - (((double) MainApplication.progressData.remainingSize / MainApplication.progressData.totalSize) * 100);
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(SelectBT2.this);
                            progressDialog.setMessage("Receiving photo...");
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setProgress(0);
                            progressDialog.setMax(100);
                            progressDialog.show();
                        }
                        progressDialog.setProgress((int) Math.floor(pctRemaining));
                        break;
                    }

                    case MessageType.INVALID_HEADER: {
                        Toast.makeText(SelectBT2.this, "Photo was sent, but the header was formatted incorrectly", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };

        if(MainApplication.adapter == null){
            MainApplication.adapter = BluetoothAdapter.getDefaultAdapter();
            if(MainApplication.adapter == null){
                Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            }else{
                if(!MainApplication.adapter.isEnabled()){
                    Toast.makeText(this, "Bluetooth is not enabled on this device", Toast.LENGTH_LONG).show();
                    startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), MainApplication.REQUEST_ENABLE_BT);
                }
            }
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    public static void saveBitmaptoJpeg(Bitmap bitmap, String folder, String name){ //bitmap객체를 jpg파일로 변환해 저장.

        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard

        String foler_name = "/"+folder+"/";
        String file_name = name+".jpg";
        String string_path = ex_storage+foler_name;

        File file_path;

        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path+file_name);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }

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


        if (requestCode == MainApplication.REQUEST_ENABLE_BT){
            MainApplication.pairedDevices = MainApplication.adapter.getBondedDevices();

            if (MainApplication.pairedDevices != null) {
                if (MainApplication.pairedDevices.size() == 0) {
                    Toast.makeText(this,"There are no paired device.",Toast.LENGTH_SHORT).show();
                } else {
                    if (MainApplication.serverThread == null) {
                        Log.v(TAG, "Starting server thread.  Able to accept photos.");
                        MainApplication.serverThread = new ServerThread(MainApplication.adapter, MainApplication.serverHandler);
                        MainApplication.serverThread.start();
                    }
                    ArrayList<DeviceData> deviceDataList = new ArrayList<DeviceData>();
                    for (BluetoothDevice device : MainApplication.pairedDevices) {
                        deviceDataList.add(new DeviceData(device.getName(), device.getAddress()));
                    }

                    ArrayAdapter<DeviceData> deviceArrayAdapter = new ArrayAdapter<DeviceData>(this, android.R.layout.simple_spinner_item, deviceDataList);
                    deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    deviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);
                    deviceSpinner.setAdapter(deviceArrayAdapter);

                    Button clientButton = (Button) findViewById(R.id.clientButton);
                    clientButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DeviceData deviceData = (DeviceData) deviceSpinner.getSelectedItem();
                            for (BluetoothDevice device : MainApplication.adapter.getBondedDevices()) {
                                if (device.getAddress().contains(deviceData.getValue())) {
                                    Log.v(TAG, "Starting client thread");
                                    if (MainApplication.clientThread != null) {
                                        MainApplication.clientThread.cancel();
                                    }
                                    MainApplication.clientThread = new ClientThread(device, MainApplication.clientHandler);
                                    MainApplication.clientThread.start();
                                }
                            }
                        }
                    });
                }
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
}