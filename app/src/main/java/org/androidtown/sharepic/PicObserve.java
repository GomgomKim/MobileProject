package org.androidtown.sharepic;

import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


import org.androidtown.sharepic.btxfr.ClientThread;
import org.androidtown.sharepic.btxfr.ServerThread;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

public class PicObserve extends AppCompatActivity {

    static final int PERMISSION_REQUEST_CODE = 10;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ContentResolver resolver;
    ContentObserver observer;
    SharedPreferences preferences;
    ArrayList<String> filePaths;
    ArrayList<Uri> uris;
    static final String RES = android.Manifest.permission.READ_EXTERNAL_STORAGE;
    static final String WES = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private Spinner deviceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_observe);
        init();
    }

    public void init() {
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

    void moveFile(){

        //picture_bitmap = getBitmapFromUri(uri);

                /*String current_url = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + File.separator + "nerang" + File.separator;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File file = new File(current_url);
                    if(!file.isDirectory()){
                        file.mkdirs();
                    }
                }



                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA,
                        current_url + uri);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                resolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);



                String fileName = String.valueOf(System.currentTimeMillis())+".jpg";
                File saveFile = new File(fileName);
                OutputStream out = null;
                try {
                    saveFile.createNewFile();
                    out = new FileOutputStream(saveFile);

                    picture_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }*/


        //내랑 폴더 생성
//                String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ "/Camera/nerang";
        String root = Environment.getExternalStorageDirectory().toString()+ "/nerang"; //영민 변경

        File myDir = new File(root);
        if(!myDir.exists()){
            myDir.mkdirs();
        }

        //권한
        if (Build.VERSION.SDK_INT >= 23) { //todo:권한 앱 처음 실행시킬 때로 옮겨야 됨
            if (!checkPermission(WES)) {
                requestPermission(WES);
            }
        }

        String filePath;
        Bitmap picture_bitmap;
        Uri uri;
        for(int i=0;i<filePaths.size();i++){
            filePath = filePaths.get(i);
            uri = uris.get(i);

            //사진 복사본 파일 저장
            String image_name = String.valueOf(System.currentTimeMillis());
            String fname = "Image-" + image_name + ".jpg";
            File file = new File(myDir, fname);
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


    void getAddedFile(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long lastDatabaseUpdateTime = preferences.getLong("lastDatabaseUpdateTime", 0); //전에 추가됐던 시간 가져옴
        long newDatabaseUpdateTime = (new Date()).getTime(); //이미지 추가된 시간 저장

        String[] projection = { MediaStore.Images.Media.DATA };

        String where = MediaStore.MediaColumns.DATE_ADDED + ">" + (lastDatabaseUpdateTime/1000); //조건 : 전에 추가됐던 시간 이후에 추가 된 사진


        //권한
        if (Build.VERSION.SDK_INT >= 23) { //todo:권한 앱 처음 실행시킬 때로 옮겨야 됨
            if (!checkPermission(RES)) {
                requestPermission(RES);
            }
        }

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



    private boolean checkPermission(String type) {
        int result = ContextCompat.checkSelfPermission(this, type);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(String type) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,type)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{type}, PERMISSION_REQUEST_CODE);
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


}
