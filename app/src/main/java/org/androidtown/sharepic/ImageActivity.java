package org.androidtown.sharepic;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

// 앨범 내 사진 목록
public class ImageActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ImageAdapter adapter;

    String album;   // 현재 앨범 이름
    List<Photo> albumPic;   // 앨범 내 사진 목록

    private ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        contentResolver = this.getContentResolver();

        albumPic = new ArrayList<>();

        adapter = new ImageAdapter(this, albumPic);

        layoutManager = new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        album = intent.getStringExtra("album_name");
        System.out.println(album);

        update();
        queryExec();

    }

    public void queryExec() {
        recyclerView.removeAllViewsInLayout();

        // button 눌렀을 때 입력한 질의문을 가져오는 작업 수행
        String sql = "select * from albums where title = \'" + album + "\'";

        AlbumDBHandler dbHandler = new AlbumDBHandler(this, null, null, 1);   // dbHandler 객체 생성
        Cursor cursor = dbHandler.selectQuery(sql); // selectQuery 를 던진다 -> 질의에 대한 결과를 받았다
        cursor.moveToFirst();   // cursor 객체의 처음으로
        if(!cursor.isAfterLast()){  // 다음이 last 가 아니면
            while(!cursor.isAfterLast()){   // 다음이 last 가 아닐 동안
                String string = cursor.getString(1);
                    Photo photo = new Photo(Uri.parse(cursor.getString(2)), Uri.parse(cursor.getString(3)));
                    albumPic.add(photo);
                    adapter.notifyDataSetChanged();
                cursor.moveToNext();    // cursor 를 다음으로 넘겨준다
            }
        } // last 면 data(질의문에 대한 답)가 없다

    }

    public void update() {
//        List<Uri> uris = loadInBackground("nerang");
//        List<Uri> uris1 = loadInBackground("nirang");
        List<Uri> uris = loadInBackground2();

        String sql = "select id, imageUri from albums";

        AlbumDBHandler dbHandler = new AlbumDBHandler(this, null, null, 1);   // dbHandler 객체 생성
        Cursor cursor = dbHandler.selectQuery(sql); // selectQuery 를 던진다 -> 질의에 대한 결과를 받았다
        cursor.moveToFirst();   // cursor 객체의 처음으로
        if (!cursor.isAfterLast()) {  // 다음이 last 가 아니면
            while (!cursor.isAfterLast()) {   // 다음이 last 가 아닐 동안
                if(!uris.contains(Uri.parse(cursor.getString(1)))){
                    deleteProduct(Integer.parseInt(cursor.getString(0)));

//                    if(!uris1.contains(Uri.parse(cursor.getString(1)))) {
//                        deleteProduct(Integer.parseInt(cursor.getString(0)));
//                    }
                }
                cursor.moveToNext();
            }
        }
    }

    public void deleteProduct(int id) {  // delete
        AlbumDBHandler dbHandler = new AlbumDBHandler(this, null, null, 1);
        boolean result = dbHandler.deleteProduct(id);
        if(result){
            Toast.makeText(this, "Record Deleted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "No Match Found", Toast.LENGTH_SHORT).show();
        }
    }

    public List<Uri> loadInBackground(String bucket) {
        // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME  + " == " + "\""+bucket+"\"";
        // TODO: Order to time
        Cursor imageCursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,   // content://로 시작하는 content table uri
                projection, // Which columns to return
                selection,   // Return all rows
                null,
                null);  // 어떻게 정렬할 것인지

        ArrayList<Uri> result = new ArrayList<>(imageCursor.getCount());
        int dataColumnIndex = imageCursor.getColumnIndex(projection[0]);

        if (imageCursor.moveToFirst()) {
            do {
                String filePath = imageCursor.getString(dataColumnIndex);

                Uri fullImageUri = Uri.parse(filePath);
                result.add(fullImageUri);
            } while(imageCursor.moveToNext());
        }
        imageCursor.close();

        return result;
    }

    public List<Uri> loadInBackground2() {
        // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
//        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME  + " == " + "\""+bucket+"\"";
        // TODO: Order to time
        Cursor imageCursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,   // content://로 시작하는 content table uri
                projection, // Which columns to return
                null,   // Return all rows
                null,
                null);  // 어떻게 정렬할 것인지

        ArrayList<Uri> result = new ArrayList<>(imageCursor.getCount());
        int dataColumnIndex = imageCursor.getColumnIndex(projection[0]);

        if (imageCursor.moveToFirst()) {
            do {
                String filePath = imageCursor.getString(dataColumnIndex);

                Uri fullImageUri = Uri.parse(filePath);
                result.add(fullImageUri);
            } while(imageCursor.moveToNext());
        }
        imageCursor.close();

        return result;
    }
}
