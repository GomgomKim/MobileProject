package org.androidtown.sharepic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class PhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        Intent intent = getIntent();
        Uri uri = Uri.parse(intent.getStringExtra("Uri"));
        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageURI(uri);
    }
}
