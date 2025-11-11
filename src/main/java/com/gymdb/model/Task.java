package com.gymdb.model;

import javafx.beans.property.SimpleStringProperty;

public class Task {

    private final SimpleStringProperty description;
    private final SimpleStringProperty status;

    public Task(String description, String status) {
        this.description = new SimpleStringProperty(description);
        this.status = new SimpleStringProperty(status);
    }

    public String getDescription() { return description.get(); }
    public void setDescription(String desc) { description.set(desc); }
    public SimpleStringProperty descriptionProperty() { return description; }

    public String getStatus() { return status.get(); }
    public void setStatus(String s) { status.set(s); }
    public SimpleStringProperty statusProperty() { return status; }
}
