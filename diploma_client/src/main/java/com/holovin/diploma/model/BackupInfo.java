package com.holovin.diploma.model;

public class BackupInfo {
    private String id;
    private String user;
    private String timestamp;
    private String status;

    private String storage;

    // Конструктор, геттери та сеттери

    public BackupInfo() {
        // Конструктор за замовчуванням (може бути порожнім або ініціалізувати деякі значення)
    }

    public BackupInfo(String id, String user, String timestamp, String status, String storage) {
        this.id = id;
        this.user = user;
        this.timestamp = timestamp;
        this.status = status;
        this.storage = storage;
    }

    // Геттери та сеттери

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }
}
