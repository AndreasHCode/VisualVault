package com.heimann.andreas.visualvault;

/**
 * Created by Andreas on 07.04.2017.
 */

public class Message {

    private String title;
    private int id;

    public Message(String title, int id) {
        this.title = title;
        this.id = id;
    }

    public Message(String title) {
        this.title = title;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
