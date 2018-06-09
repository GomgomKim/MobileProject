package org.androidtown.sharepic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import org.androidtown.sharepic.BTPhotoTransfer.SelectBT2;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void connectBT(View view) {
//        Intent intent = new Intent(MainActivity.this, SelectBT.class);
        Intent intent = new Intent(MainActivity.this, SelectBT2.class);
        startActivity(intent);
    }
}
