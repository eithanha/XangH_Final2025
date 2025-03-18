package com.example.xangh_final2025.models;

import java.util.Date;

public class Activities {
    private int id;
    private String title;
    private String description;
    private Date date;
    private String status;
    private int categoryId;

    
    public Activities() {
    }

    
    public Activities(int id, String title, String description, Date date, String status, int categoryId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.status = status;
        this.categoryId = categoryId;
    }

    
    public Activities(String title, String description, Date date, String status, int categoryId) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.status = status;
        this.categoryId = categoryId;
    }

    
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String toString() {
        return "Activities{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", status='" + status + '\'' +
                ", categoryId=" + categoryId +
                '}';
    }

}
