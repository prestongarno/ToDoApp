package com.example.prest.simpletodo;

/**
 * Created by Preston Garno on 9/5/2016.
 */
public enum LOGIN_ACTIVITY_MODE {
    SIGN_IN_MODE("SIGN_IN_MODE"),
    SIGN_UP_MODE("SIGN_UP_MODE");

    private String value;

    private LOGIN_ACTIVITY_MODE(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
