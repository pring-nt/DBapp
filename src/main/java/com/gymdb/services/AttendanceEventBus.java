package com.gymdb.services;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;

public final class AttendanceEventBus {
    private static final SimpleIntegerProperty counter = new SimpleIntegerProperty(0);
    private AttendanceEventBus(){}

    public static void increment() {
        Platform.runLater(() -> counter.set(counter.get() + 1));
    }

    public static void addListener(ChangeListener<? super Number> listener) {
        counter.addListener(listener);
    }
}
