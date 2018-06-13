package org.androidtown.sharepic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class AlbumDBHandler extends SQLiteOpenHelper { // database 의 생성, 열기, upgrade 를 담당하는 SQLiteOpenHelper class 상속받아 재정의
    // 자주 사용하는 값 상수로 선언
    public static final String DATABASE_NAME = "albumDB.db";
    public static final String DATABASE_TABLE = "albums";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_thumbnailUri = "thumbnailUri";
    public static final String COLUMN_imageUri = "imageUri";

    Context context;

    public AlbumDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {   // 생성자: DB 생성
        super(context, DATABASE_NAME, factory, version);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {   // table 생성
        // DB 가 처음 만들어질 때 호출
        // table 생성 및 초기 record 삽입: create table

        // SQL 구문
        String CREATE_TABLE = "create table if not exists " + DATABASE_TABLE + " (" +
                COLUMN_ID + " integer primary key autoincrement, " +
                COLUMN_TITLE + " text, " +
                COLUMN_thumbnailUri + " text, " +
                COLUMN_imageUri + " text" + ")";

        db.execSQL(CREATE_TABLE);   // SQL 질의문 수행
//
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  // table 삭제 후 다시 생성: version 이 바뀌었을 때 자동으로 호출
        // DB 를 upgrade 할 때 호출
        // 기존 table 을 삭제하고 새로 만들거나 alter table 로 schema 수정

        db.execSQL("drop table if exists " + DATABASE_NAME);
        onCreate(db);

    }

    public void addProduct(Album product){    // 들어온 data 들을 insert 하는 기능을 담당
        ContentValues values = new ContentValues(); // 속성, value 쌍 mapping
        values.put(COLUMN_TITLE, product.getTitle());
        values.put(COLUMN_thumbnailUri, product.getData().getThumbnailUri().toString());
        values.put(COLUMN_imageUri, product.getData().getImageUri().toString());

        SQLiteDatabase db = this.getWritableDatabase(); // 일고 쓰기 위한 db 를 연다
        db.insert(DATABASE_TABLE, null, values);    // data 의 삽입 - 빈 값이 있으면 null 로 채운다
        db.close(); // db 를 닫는다
    }

    public boolean deleteProduct(int id){   // data 의 삭제
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE, COLUMN_ID + "=?", new String[] {String.valueOf(id)});    // ?: 인자로 값을 줘야 한다
        return true;
    }

    public Album findProduct(int id){ // 검색
        String query = "select * from " + DATABASE_TABLE + " where " + COLUMN_ID + " = \'" + id + "\'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);   // select 를 실행 후 cursor 반환
        Album product = new Album();
        if(cursor.moveToFirst()) {   // cursor 의 제일 첫 행으로 이동
            product.setId(Integer.parseInt(cursor.getString(0)));   // 첫 번째 나온 객체에 해당하는 정보 한 번만 가져온다
            product.setTitle(cursor.getString(1));
            product.setData(new Photo(Uri.parse(cursor.getString(2)), Uri.parse(cursor.getString(3))));
            cursor.close();
        }else{
            product = null;
        }
        db.close();
        return product;
    }

    public Cursor selectQuery(String query) {
        SQLiteDatabase db = this.getWritableDatabase(); // database 객체
        return db.rawQuery(query, null);    // cursor 객체 반환
    }
}
