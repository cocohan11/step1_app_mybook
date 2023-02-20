package com.example.mybooks.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AladinBook implements Serializable {
    /**
     * 알라딘 책 검색 api로 받아오는 데이터 항목
     */
    private String link;
    private String pubDate;
    private String description;
    private String publisher;


    @SerializedName("isbn") private String isbn;
    @SerializedName("title") private String title;
    @SerializedName("author") private String author;
    @SerializedName("bookState") private String bookState;
    @SerializedName("cover") private String cover;
    @SerializedName("startDate") private Long startDate;
    @SerializedName("finishDate") private Long finishDate;
    @SerializedName("rating") private float rating;
    @SerializedName("countNote") private int countNote; // 추가
    @SerializedName("date") private String date; // 추가


    public AladinBook(String isbn, String title, String link, String author, String pubDate, String description, String cover, String publisher) {
        this.isbn = isbn;
        this.title = title;
        this.link = link;
        this.author = author;
        this.pubDate = pubDate;
        this.description = description;
        this.cover = cover;
        this.publisher = publisher;
    }


    public AladinBook(String cover, String title, String author, String description) {
        this.cover = cover;
        this.title = title;
        this.author = author;
        this.description = description;
    }

    public int getCountNote() {
        return countNote;
    }

    public void setCountNote(int countNote) {
        this.countNote = countNote;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBookState() {
        return bookState;
    }

    public void setBookState(String bookState) {
        this.bookState = bookState;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Long finishDate) {
        this.finishDate = finishDate;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    @Override
    public String toString() {
        return "\nAladinBook{" +
                "\nlink='" + link + '\'' +
                "\n, pubDate='" + pubDate + '\'' +
                "\n, description='" + description + '\'' +
                "\n, publisher='" + publisher + '\'' +
                "\n, isbn='" + isbn + '\'' +
                "\n, title='" + title + '\'' +
                "\n, author='" + author + '\'' +
                "\n, bookState='" + bookState + '\'' +
                "\n, cover='" + cover + '\'' +
                "\n, startDate=" + startDate +
                "\n, finishDate=" + finishDate +
                "\n, countNote=" + countNote +
                "\n, date=" + date +
                "\n, rating=" + rating +
                "\n}";
    }
}
