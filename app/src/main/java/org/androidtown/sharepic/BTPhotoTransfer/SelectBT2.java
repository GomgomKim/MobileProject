package org.androidtown.sharepic.BTPhotoTransfer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.androidtown.sharepic.MainActivity;
import org.androidtown.sharepic.R;
import org.androidtown.sharepic.btxfr.ClientThread;
import org.androidtown.sharepic.btxfr.MessageType;
import org.androidtown.sharepic.btxfr.ProgressData;
import org.androidtown.sharepic.btxfr.ServerThread;
import org.androidtown.sharepic.Picture;

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
    public static final int BT_DISABLE = 0;
//    private final String sendStringPath = Environment.getExternalStorageDirectory()+"/nerang";

    Button clientButton;

    //추가부분
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ContentResolver resolver;
    ContentObserver observer;
    SharedPreferences preferences;
    ArrayList<String> filePaths;
    ArrayList<Uri> uris;
    static final String RES = android.Manifest.permission.READ_EXTERNAL_STORAGE;
    static final String WES = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
    File file;
    //ArrayList<DeviceData> deviceDataList;

    //기연추가
    DeviceData deviceData;
    BTService.DeviceData bt_devicedata;
    ArrayList<DeviceData> deviceDataList;
    ArrayList<String> spinList;

    // DB용
    String pathNow;
    EditText dbtestEdit;
    GridLayout dbtestGrid;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectbt2);

        //DB테스트
        dbtestEdit = findViewById(R.id.query);
        dbtestGrid = findViewById(R.id.queryGrid);

        BTService.pairedDevices = null;

        clientButton = (Button) findViewById(R.id.clientButton);

        dbHandler = new MyDBHandler(this, null, null, 1); //전송할파일uri 저장하기 위한 db handler입니다.

        BTService.clientHandler = new Handler() {

            @Override
            public void handleMessage(Message message) { //보내는 쪽
                switch (message.what) {
                    case MessageType.READY_FOR_DATA: {
                        try {
                            //전처리 & 전송


                            /*String sendStringPath =  Environment.getExternalStorageDirectory().toString();
                            fileName = "btimage.jpg"; //임의 지정
                            File file = new File(sendStringPath,fileName);*/

                            ByteArrayOutputStream compressedImageStream = new ByteArrayOutputStream();
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 2;
                            Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                            image.compress(Bitmap.CompressFormat.JPEG, BTService.IMAGE_QUALITY, compressedImageStream);
                            byte[] compressedImage = compressedImageStream.toByteArray();
                            Log.v(TAG, "Compressed image size: " + compressedImage.length);

                            ImageView imageView = (ImageView) findViewById(R.id.imageView);
                            imageView.setImageBitmap(image);

                            // Invoke client thread to send
                            Message imageMessage = new Message();
                            imageMessage.obj = compressedImage;
                            BTService.clientThread.incomingHandler.sendMessage(imageMessage);

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


        BTService.serverHandler = new Handler() {
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
                        saveBitmaptoJpeg(image, "nirang", BTService.IMAGE_FILE_NAME + dateString);//파일명예시: nr20180606043124.jpg



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
                        BTService.progressData = (ProgressData) message.obj;
                        double pctRemaining = 100 - (((double) BTService.progressData.remainingSize / BTService.progressData.totalSize) * 100);
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

        init2();


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
                if (BTService.serverThread == null) {
                    Log.v(TAG, "Starting server thread.  Able to accept photos.");
                    BTService.serverThread = new ServerThread(BTService.adapter, BTService.serverHandler);
                    BTService.serverThread.start();
                }
                deviceDataList = new ArrayList<DeviceData>();
                spinList = new ArrayList<>();
                for (BluetoothDevice device : BTService.pairedDevices) {
                    deviceDataList.add(new DeviceData(device.getName(), device.getAddress()));
                    spinList.add(device.getName()); //기연추가 스핀 목록위해 String 어레이리스트 생성
                }

                ArrayAdapter<String> deviceArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinList);
                deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                deviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);
                deviceSpinner.setAdapter(deviceArrayAdapter);

                //기연추가 스피너 선택시 들어가도록
                deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        bt_devicedata = new BTService.DeviceData(deviceDataList.get(position).getSpinnerText(), deviceDataList.get(position).getValue());
                        /*Log.i("datas ", bt_devicedata.getSpinnerText()+ " " + bt_devicedata.getValue());
                        Log.i("풀네임 : ", deviceSpinner.getSelectedItem().toString());*/
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                clientButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deviceData = (DeviceData) deviceSpinner.getSelectedItem();
                        for (BluetoothDevice device : BTService.adapter.getBondedDevices()) {
                            if (device.getAddress().contains(deviceData.getValue())) {
                                Log.v(TAG, "Starting client thread");
                                if (BTService.clientThread != null) {
                                    BTService.clientThread.cancel();
                                }
                                BTService.clientThread = new ClientThread(device, BTService.clientHandler);
                                BTService.clientThread.start();
                            }
                        }
                    }
                });
            }
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

    public void saveBitmaptoJpeg(Bitmap bitmap, String folder, String name){ //bitmap객체를 jpg파일로 변환해 저장.

        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard

        String foler_name = "/"+folder+"/";
        String file_name = name+".jpg"; //=fname
        String string_path = ex_storage+foler_name;

        File file_path;

        try{
            file_path = new File(string_path); // file
            if(!file_path.isDirectory()) {
                file_path.mkdirs();
            }

            //기연 수정. file 정보 수정
            File nirang_file = new File(file_path, file_name);
            FileOutputStream out = new FileOutputStream(nirang_file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); //미디어 스캐닝 (갤러리에 앨범 띄워주기 위해)
            intent.setData(Uri.fromFile(nirang_file));
            sendBroadcast(intent);
            out.flush();
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


        if (requestCode == BTService.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) { //연결에 성공하면 pairing한다
                pairing();
            }else{
                startActivityForResult(new Intent(this, MainActivity.class),BT_DISABLE);
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

        public String getSpinnerText() {
            return spinnerText;
        }

        String spinnerText;
        String value;
    }


    //코드합치기
    // 사진찍고, 내랑폴더 저장

    void moveFile(){

        //내랑 폴더 생성
//                String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ "/Camera/nerang";
        String root = Environment.getExternalStorageDirectory().toString()+ "/nerang"; //영민 변경

        File myDir = new File(root);
        if(!myDir.exists()){
            myDir.mkdirs();
        }

        String filePath;
        Bitmap picture_bitmap;
        Uri uri;
        for(int i=0;i<filePaths.size();i++){
            filePath = filePaths.get(i);
            uri = uris.get(i);

            //사진 복사본 파일 저장
            String image_name = String.valueOf(System.currentTimeMillis());
            String fname = "Image-" + image_name + ".jpg"; //이미지 이름
            file = new File(myDir, fname);
            fileName = fname;
            System.out.println(file.getAbsolutePath()); //로그캣 확인
            // if (file.exists()) file.delete();
            Log.i("LOAD", root + fname); //복사본 저장 확인
            try { // 앨범에 이미지 복사본 저장
                FileOutputStream out = new FileOutputStream(file);
                picture_bitmap = BitmapFactory.decodeFile(filePath);
                picture_bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out); //영민 jpg로 변경
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


            //자동전송 기연추가
            for (BluetoothDevice device : BTService.adapter.getBondedDevices()) {
                if (device.getAddress().contains(bt_devicedata.getValue())) { //서비스에서 받아오도록했음
                    Log.v(TAG, "Starting client thread");
                    if (BTService.clientThread != null) {
                        BTService.clientThread.cancel();
                    }
                    BTService.clientThread = new ClientThread(device, BTService.clientHandler);
                    BTService.clientThread.start();
                }
            }

            //앨범 삭제
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);
            Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Images.Media.DATA + " = ?";
            String[] selectionArgs = {filePath}; // 실제 파일의 경로
            resolver.delete(images, selection, selectionArgs);


            //원본 파일 삭제
            File file_delete = new File(uri.getPath());
            if(file_delete.exists()){
                file_delete.delete();
            }
        }
    }
    public void init2() {
        resolver = getContentResolver();
        observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                Log.i("DB변경", "DB가 변경됨을 감지했습니다.");
                getAddedFile();
                if(filePaths.size() > 0) {
                    moveFile();
                }
            }
        };

        resolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                observer);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("lastDatabaseUpdateTime", (new Date()).getTime()); //현재 시간 찍는다.
        editor.commit();
    }


    void getAddedFile(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long lastDatabaseUpdateTime = preferences.getLong("lastDatabaseUpdateTime", 0); //전에 추가됐던 시간 가져옴
        long newDatabaseUpdateTime = (new Date()).getTime(); //이미지 추가된 시간 저장

        String[] projection = { MediaStore.Images.Media.DATA };

        String where = MediaStore.MediaColumns.DATE_ADDED + ">" + (lastDatabaseUpdateTime/1000); //조건 : 전에 추가됐던 시간 이후에 추가 된 사진

        Cursor imageCursor = MediaStore.Images.Media.query(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where, null, null);


        filePaths = new ArrayList<>();
        uris = new ArrayList<>(imageCursor.getCount());

        int dataColumnIndex = imageCursor.getColumnIndex(projection[0]);

        if (imageCursor == null) {
            // Error 발생
            // 적절하게 handling 해주세요
        } else {
            int count = imageCursor.getCount();
            if (count > 0 && imageCursor.moveToFirst()) {
                do {
                    String filePath = imageCursor.getString(dataColumnIndex);
                    if (!filePath.contains("nerang")) {
                        filePaths.add(filePath);
                        Uri imageUri = Uri.parse(filePath);
                        uris.add(imageUri);

                        // DB에 추가
                        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
                        Picture picture = new Picture(imageUri, pathNow);
                        dbHandler.addPicture(picture);
                    }
                } while(imageCursor.moveToNext());
            }
        }

        imageCursor.close();

//If no exceptions, then save the new timestamp
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("lastDatabaseUpdateTime", newDatabaseUpdateTime); //새로 찍는다.
        editor.commit();

    }


    // 폴더 지정
    public void selectFolderBTN(View view) {
        AlertDialog.Builder ad = new AlertDialog.Builder(SelectBT2.this);

        ad.setTitle("앨범 선택");       // 제목 설정
        ad.setMessage("저장할 폴더 이름을 적어주세요");   // 내용 설정

        final EditText et = new EditText(SelectBT2.this);
        ad.setView(et);

        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pathNow = et.getText().toString();
                dialog.dismiss();
            }
        });
        ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    // db테스트용 코드
    public void queryExec(View view) {
        String sql = dbtestEdit.getText().toString();
        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
        Cursor cursor = dbHandler.selectQuery(sql);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            int column = cursor.getColumnCount();
            int row = cursor.getCount();
            dbtestGrid.removeAllViewsInLayout(); // layout안에 있는 모든 뷰를 없앤당
            dbtestGrid.setColumnCount(column);
            dbtestGrid.setRowCount(row + 1);
            dbtestGrid.setUseDefaultMargins(true);

            for (int i = 0; i < column; i++) {
                View view1 = getLayoutInflater().inflate(R.layout.row, null);
                TextView item = view1.findViewById(R.id.item);
                item.setText(cursor.getColumnName(i));
                item.setBackgroundColor(Color.LTGRAY);
                dbtestGrid.addView(view1);
            }
            while (!cursor.isAfterLast()) {
                for (int i = 0; i < column; i++) {
                    View view1 = getLayoutInflater().inflate(R.layout.row, null);
                    TextView item = (TextView) view1.findViewById(R.id.item);
                    item.setText(cursor.getString(i));
                    dbtestGrid.addView(view1);
                }
                cursor.moveToNext();
            }
        }
    }

    public void deletePicture(View view) {
        // DB를 전부 삭제
        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
        dbHandler.deleteAll();
//        if (result) {
//            dbtestEdit.setText("");
//            Toast.makeText(this, "Record Deleted", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "No Match Found", Toast.LENGTH_SHORT).show();
//        }
    }
}