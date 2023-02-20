package com.example.mybooks.model;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Club implements Serializable {
    /**
     * 모임 데이터를 서버에서 받아오는 모델
     */
    @SerializedName("name") private String name;
    @SerializedName("introduction") private String introduction;
    @SerializedName("imageUrl") private String imageUrl;
    @SerializedName("bookTitle") private String bookTitle;
    @SerializedName("turnout") private int turnout;
    @SerializedName("fixed_num") private int fixed_num;
    @SerializedName("ages") private String ages;
    @SerializedName("theme") private String theme;
    @SerializedName("start_date") private Long start_date;
    @SerializedName("finish_date") private Long finish_date;
    @SerializedName("id") private String id;
    @SerializedName("master_img") private String master_img;
    @SerializedName("master_name") private String master_name;


    @SerializedName("response") public boolean response;
    @SerializedName("message") public String message;


    public String getMaster_img() {
        return master_img;
    }

    public void setMaster_img(String master_img) {
        this.master_img = master_img;
    }

    public String getMaster_name() {
        return master_name;
    }

    public void setMaster_name(String master_name) {
        this.master_name = master_name;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }

    private String[] themeList = new String[] {"IT/컴퓨터", "경영/경제", "교육/공부", "기획/마케팅", "요리/여행", "종료/봉사", "자연/환경", "정치/사회", "문화/예술", "인문/과학", "자기계발/취미", "고전문학", "주식/투자", "시/소설"};

    public String[] getThemeList() {
        return themeList;
    }

    public void setThemeList(String[] themeList) {
        this.themeList = themeList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getTurnout() {
        return turnout;
    }

    public void setTurnout(int turnout) {
        this.turnout = turnout;
    }

    public int getFixed_num() {
        return fixed_num;
    }

    public void setFixed_num(int fixed_num) {
        this.fixed_num = fixed_num;
    }

    public String getAges() {
        return ages;
    }

    public void setAges(String ages) {
        this.ages = ages;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Long getStart_date() {
        return start_date;
    }

    public void setStart_date(Long start_date) {
        this.start_date = start_date;
    }

    public Long getFinish_date() {
        return finish_date;
    }

    public void setFinish_date(Long finish_date) {
        this.finish_date = finish_date;
    }

    @Override
    public String toString() {
        return "Club{" +
                "\nid='" + id + '\'' +
                "\nname='" + name + '\'' +
                "\nbookTitle='" + bookTitle + '\'' +
                "\n, introduction='" + introduction + '\'' +
                "\n, imageUrl='" + imageUrl + '\'' +
                "\n, turnout=" + turnout +
                "\n, fixed_num=" + fixed_num +
                "\n, ages='" + ages + '\'' +
                "\n, theme='" + theme + '\'' +
                "\n, start_date='" + start_date + '\'' +
                "\n, finish_date='" + finish_date + '\'' +
                "\n, master_img='" + master_img + '\'' +
                "\n, master_name='" + master_name + '\'' +
                '}';
    }
}
