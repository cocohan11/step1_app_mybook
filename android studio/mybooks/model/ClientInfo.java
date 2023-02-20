package com.example.mybooks.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.net.Socket;

public class ClientInfo {
    /**
     * 클라이언트 1명에 대한 정보
     * */
    public Socket socket; // 안드1개와 서버와 연결되는 소켓
    public String clubNum;
    public String purpose;
    public String email;
    public String name; // 닉네임
    public String img;
    public String chat;
    public int viewType;

    public ClientInfo() {
    }


    @JsonCreator
    public ClientInfo(
            @JsonProperty("socket") Socket socket
            , @JsonProperty("clubNum")String clubNum
            , @JsonProperty("purpose") String purpose
            , @JsonProperty("email") String email
            , @JsonProperty("name") String name
            , @JsonProperty("img") String img
            , @JsonProperty("chat") String chat
            , @JsonProperty("viewType") int viewType)
    {
        this.socket = socket;
        this.clubNum = clubNum;
        this.purpose = purpose;
        this.email = email;
        this.name = name;
        this.img = img;
        this.chat = chat;
        this.viewType = viewType;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getClubNum() {
        return clubNum;
    }

    public void setClubNum(String clubNum) {
        this.clubNum = clubNum;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        img = img;
    }

    @Override
    public String toString() {
        return "ClientInfo{" +
                "\n     clubNum='" + clubNum + '\'' +
                "\n     , socket=" + socket +
                "\n     , purpose='" + purpose + '\'' +
                "\n     , email='" + email + '\'' +
                "\n     , name='" + name + '\'' +
                "\n     , img='" + img + '\'' +
                "\n     , chat='" + chat + '\'' +
                "\n     , viewType='" + viewType + '\'' +
                '}';
    }
}