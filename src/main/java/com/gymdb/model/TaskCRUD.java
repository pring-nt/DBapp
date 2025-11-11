package com.gymdb.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TaskCRUD {

    // Static in-memory list of tasks
    private static final ObservableList<Task> tasks = FXCollections.observableArrayList();

    // Return the static list (persistent in memory)
    public static ObservableList<Task> getTasks() {
        return tasks;
    }

    // Add a task
    public void addTask(Task task) {
        tasks.add(task);
    }

    // Remove a task
    public void removeTask(Task task) {
        tasks.remove(task);
    }
}
