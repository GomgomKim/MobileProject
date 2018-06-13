package org.androidtown.sharepic;

import android.net.Uri;

public class Picture {
    private Uri _uri;
    private String _path;

    public Picture() {
    }

    public Picture(Uri uri, String path) {
        this._uri =uri;
        this._path = path;
    }

    //uri
    public Uri get_Uri() {
        return _uri;
    }

    public void set_Uri(Uri _uri) {
        this._uri = _uri;
    }

    //path
    public String get_Path() {
        return _path;
    }

    public void set_Path(String _path) {
        this._path = _path;
    }
}