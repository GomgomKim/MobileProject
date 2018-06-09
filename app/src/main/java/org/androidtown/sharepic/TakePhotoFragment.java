package org.androidtown.sharepic;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TakePhotoFragment extends Fragment {

    Button takePicBtn;
    static final int REQUEST_IMAGE_CAPTURE = 1; //사진찍기 위한 시리얼넘버
    ImageView showImg;
    ContentResolver resolver;
    ContentObserver observer;
    Uri uri;
    Intent takePictureIntent;
    String pic_data;

    //프래그먼트 기본 생성자
    public TakePhotoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_take_photo, container, false);
    }

    //액티비티 다 띄워 졌을 때
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        takePicBtn.setOnClickListener(new View.OnClickListener() { //사진찍기 버튼 누를 때
            @Override
            public void onClick(View v) {
                takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //카메라앱으로 넘어가기
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) { //사진이 찍혔다면
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE); //onActivityResult 함수로 정보보내기
                }
            }
        });


        resolver = getActivity().getContentResolver();

        observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                Log.i("DBChange", "DB가 변경됨을 감지했습니다.");

                String current_url = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + File.separator + "nerang" + File.separator;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File file = new File(current_url);
                    pic_data = file.getAbsolutePath();
                }

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA,
                        current_url + "nerang");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                resolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        };

        resolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                observer);


        //resolver.unregisterContentObserver(observer);
    }


    //찍고와서 받은 정보
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) { //사용자가 카메라 앱에서 예 를 눌렀고, 정보가 왔다면
            Bundle extras = data.getExtras(); //데이터 가져오기 위한 자료구조
            Bitmap imageBitmap = (Bitmap) extras.get("data"); //받아온 정보를 비트맵에 저장
            showImg.setImageBitmap(imageBitmap); //이미지뷰에 띄워줌
        }

    }

    public void init(){
        takePicBtn = (Button)getActivity().findViewById(R.id.photobtn);
        showImg = (ImageView)getActivity().findViewById(R.id.photo);
    }



}