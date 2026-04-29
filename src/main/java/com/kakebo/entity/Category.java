package com.kakebo.entity;

public enum Category {
    SURVIVAL("Survival - Food, transport, essentials"),
    ENTERTAINMENT("Entertainment and treats"),
    CULTURE("Culture and education"),
    EXTRAS("Extras and unexpected expenses");

    private final String description;

    Category(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
