package com.example.web;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TaskDetails implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public String toString() {
        return "TaskDetails{" + "id=" + id + ", name=" + name + ", description=" + description + ", createdDate=" + createdDate + ", lastModifiedDate=" + lastModifiedDate + '}';
    }

}
