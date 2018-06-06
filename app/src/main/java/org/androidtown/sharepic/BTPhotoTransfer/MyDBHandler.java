package org.androidtown.sharepic.BTPhotoTransfer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHandler extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "nirangnerang.db";
    public static final String DATABASE_TABLE = "uriList";

    public static final String COLUMN_URI = "uri";

    Context context;
    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "create table if not exists " + DATABASE_TABLE + "(" + COLUMN_URI + " String primary key)";
        db.execSQL(CREATE_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + DATABASE_TABLE);
        onCreate(db);
    }


    public void addUri(String uri) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_URI, uri);

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(DATABASE_TABLE, null, values);
        db.close();
    }

    public Cursor selectQuery(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery(query, null);
    }

    public String findUri(String uri) {
        String query = "select * from " + DATABASE_TABLE + " where " +
                COLUMN_URI + "= \'" + uri + "\'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        String uriF = cursor.getString(0);
        db.close();
        return uriF;
    }


    public boolean deleteProduct(String uri){
        boolean result = false;
        String query="select * from "+DATABASE_TABLE +
                " where "+COLUMN_URI+"= \'"+uri+"\'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        String uriF;
        if(cursor.moveToFirst()){
            db.delete(DATABASE_TABLE, COLUMN_URI + "=" + uri, null);
            cursor.close();
            db.close();return true;
        }
        db.close();
        return result;
    }

}

