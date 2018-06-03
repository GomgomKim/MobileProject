package org.androidtown.sharepic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PictureActivity extends AppCompatActivity {
    final int SELECT_IMAGE = 100;
    TextView tv;

    //이미지 Uri를 저장하는 리스트
    List<Uri> selectPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        selectPic = new ArrayList<>();
    }

    public void btnClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.camera:
                accessCamera();
                break;

            case R.id.gallery:
                accessGallery();
                break;
        }
    }

    // 사진 찍기 코드
    public void accessCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivity(intent);
    }

    // 사진 가져오기 코드
    public void accessGallery() {
        if (checkAppPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE})) {
            //퍼미션 동의했을 때
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
            intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SELECT_IMAGE);
        } else {
            //퍼미션 동의하지 않았을 때
            askPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SELECT_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                selectPic.add(data.getData());
            }
        }
    }


    // 권한을 체크하는 함수. 필요한지는 모르겠음
    boolean checkAppPermission(String[] requestPermission) {
        boolean[] requestResult = new boolean[requestPermission.length];
        for (int i = 0; i < requestResult.length; i++) {
            requestResult[i] = (ContextCompat.checkSelfPermission(this,
                    requestPermission[i]) == PackageManager.PERMISSION_GRANTED);
            if (!requestResult[i]) {
                return false;
            }
        }
        return true;
    }

    void askPermission(String[] requestPermission, int REQ_PERMISSION) {
        ActivityCompat.requestPermissions(this,
                requestPermission,
                REQ_PERMISSION);
    }
}