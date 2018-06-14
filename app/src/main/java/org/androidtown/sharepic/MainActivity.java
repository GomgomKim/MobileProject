package org.androidtown.sharepic;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.androidtown.sharepic.BTPhotoTransfer.BTService;
import org.androidtown.sharepic.BTPhotoTransfer.SelectBT2;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    static final String RES = android.Manifest.permission.READ_EXTERNAL_STORAGE;
    static final String WES = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermission(RES)) {
                requestPermission(RES);
            }
            if (!checkPermission(WES)) {
                requestPermission(WES);
            }
        }

        MyApplication myapp = (MyApplication)getApplication();
        if(!myapp.isOn()){
            stopService(new Intent(this, BTService.class));
        }
    }

    public void connectBT(View view) {
//        Intent intent = new Intent(MainActivity.this, SelectBT.class);
        //BTService를 시작합니다. //background에서 실행 //블루투스 초기화함
        Intent Service = new Intent(this, BTService.class);
        startService(Service);
        Intent intent = new Intent(MainActivity.this, SelectBT2.class);
        startActivity(intent);
    }

    ///////////// 앨범 추가 기능 /////////////
    public void goAlbum(View view) {
        Intent intent = new Intent(this, AlbumActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SelectBT2.BT_DISABLE) {
            Toast.makeText(this,"You should turn on bluetooth",Toast.LENGTH_SHORT).show();
        }
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