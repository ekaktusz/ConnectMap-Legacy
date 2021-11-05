package com.example.connectmap.database;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Post {
    private String postId;
    private String text;
    private double longitude;
    private double latitude;
    private int score;
    private String imageUrl;
    private Object date;
    @Exclude private boolean visible = true;

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Object getDate() {
        return date;
    }

    public void setDate(Object date) {
        this.date = date;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    Post() {}

    public Post(String postId, String text, String imageUrl, double longitude, double latitude, Object date) {
        this.postId = postId;
        this.text = text;
        this.longitude = longitude;
        this.latitude = latitude;
        this.score = 0;
        this.imageUrl = imageUrl;
        this.date = date;
    }

    @Exclude
    public long getDateLong(){
        return (long) date;
    }

    @Exclude
    public String getDateString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(new Date(getDateLong()));
        return dateString;
    }

}
