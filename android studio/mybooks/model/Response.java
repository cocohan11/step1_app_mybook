package com.example.mybooks.model;

import com.google.gson.annotations.SerializedName;

public class Response {
    /**
     * http response
     */
    @SerializedName("response") public boolean response;
    @SerializedName("message") public String message;
    @SerializedName("userName") public String userName;
    @SerializedName("userImg") public String userImg;
    @SerializedName("countMyBook") public String countMyBook;
//    @SerializedName("isbn") public String isbn;


    public String getCountMyBook() {
        return countMyBook;
    }

    public void setCountMyBook(String countMyBook) {
        this.countMyBook = countMyBook;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public String getMessage() { return message; }

    public boolean isResponse() {
        return response;
    }

    public void setMessage(String message) { this.message = message; }

    public void setResponse(boolean response) {
        this.response = response;
    }


}
