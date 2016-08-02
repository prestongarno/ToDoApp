package com.example.prest.simpletodo;

import java.util.Date;

/**
 * Created by prest on 8/1/2016.
 */
public class note {

    private String title, description;
    private Date dateCreated;

    public note(String title) {
        this.title = title;
        this.dateCreated = new Date();
        this.description = "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
