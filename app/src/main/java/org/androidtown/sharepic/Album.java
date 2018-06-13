package org.androidtown.sharepic;

// 앨범
public class Album {
    private int id;
    private String title;   // 앨범 제목
    private Photo data; // 앨범 내 사진

    public Album(){

    }
    public Album(int id, String title, Photo data){
        this.id = id;
        this.title = title;
        this.data = data;
    }
    public Album(String title, Photo data){
        this.title = title;
        this.data = data;
    }

    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return id;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
    public void setData(Photo data){
        this.data = data;
    }
    public Photo getData(){
        return data;
    }
}
