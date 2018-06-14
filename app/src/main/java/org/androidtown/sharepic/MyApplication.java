package org.androidtown.sharepic;

import android.app.Application;

public class MyApplication extends Application {
    private boolean isOn; //자동전송
    private String album_title;

    public String getAlbum_title() {
        return album_title;
    }

    public void setAlbum_title(String album_title) {
        this.album_title = album_title;
    }


    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

}
