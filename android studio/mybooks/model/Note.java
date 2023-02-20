package com.example.mybooks.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Note implements Serializable {
    /**
     *  내가 작성한 노트
     */
    @SerializedName("id") private int id;
    @SerializedName("page") private int page;
    @SerializedName("open") private boolean open;
    @SerializedName("content") private String content;
    @SerializedName("date") private String date;
    @SerializedName("imgUrl") private String imgUrl;
    @SerializedName("isbn") private String isbn;


    public Note(int page, boolean open, String content, String date, String imgUrl) {
        this.page = page;
        this.open = open;
        this.content = content;
        this.date = date;
        this.imgUrl = imgUrl;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }


    @Override
    public String toString() {
        return "Note{" +
                "\nid=" + id +
                "\n, page=" + page +
                "\n, open=" + open +
                "\n, content='" + content + '\'' +
                "\n, date='" + date + '\'' +
                "\n, imgUrl='" + imgUrl + '\'' +
                "\n, isbn='" + isbn + '\'' +
                '}';
    }
}
