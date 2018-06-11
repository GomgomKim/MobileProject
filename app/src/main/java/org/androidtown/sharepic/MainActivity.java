package org.androidtown.sharepic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.androidtown.sharepic.BTPhotoTransfer.BTService;
import org.androidtown.sharepic.BTPhotoTransfer.SelectBT2;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void connectBT(View view) {
//        Intent intent = new Intent(MainActivity.this, SelectBT.class);
        //BTService를 시작합니다. //background에서 실행 //블루투스 초기화함
        Intent Service = new Intent(this, BTService.class);
        startService(Service);
        Intent intent = new Intent(MainActivity.this, SelectBT2.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SelectBT2.BT_DISABLE) {
            Toast.makeText(this,"You should turn on bluetooth",Toast.LENGTH_SHORT).show();
        }
    }
}