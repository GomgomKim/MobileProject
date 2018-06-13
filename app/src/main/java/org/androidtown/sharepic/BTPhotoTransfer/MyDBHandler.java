package org.androidtown.sharepic.BTPhotoTransfer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.androidtown.sharepic.Picture;

public class MyDBHandler extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "pictureDB.db";
    public static final String DATABASE_TABLE = "pictures";

    public static final String COLUMN_URI = "uri";
    public static final String COLUMN_PATH = "path";

    Context context;

    public MyDBHandler(Context context, String name,
                       SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "create table if not exists " +
                DATABASE_TABLE + "(" +
                COLUMN_URI + " text," +
                COLUMN_PATH + " text" + " )";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + DATABASE_TABLE);
        onCreate(db);
    }

    public void addPicture(Picture pic){
        ContentValues values = new ContentValues();
        values.put(COLUMN_URI, pic.get_Uri().toString()); //uri에서 string으로 형변환
        values.put(COLUMN_PATH, pic.get_Path());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(DATABASE_TABLE, null, values);
        db.close();
    }

    public Cursor selectQuery(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery(query, null);
    }

    public boolean deletePicture(String path) {
        boolean result = false;
        String query = "select * from " + DATABASE_TABLE +
                " where " + COLUMN_PATH + "= \'" + path + "\'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Picture pic = new Picture();
        if (cursor.moveToFirst()) {
            pic.set_Path(cursor.getString(0));
            db.delete(DATABASE_TABLE, COLUMN_PATH + "=?",
                    new String[]{String.valueOf(pic.get_Path())});
            cursor.close();
            db.close();
            return true;
        }
        db.close();
        return result;
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE, null, null);
    }
}