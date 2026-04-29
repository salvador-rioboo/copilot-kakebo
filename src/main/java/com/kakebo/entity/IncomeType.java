package com.kakebo.entity;

public enum IncomeType {
    PRINCIPAL("Main income - salary"),
    EXTRA("Extra income - freelance, gifts, refunds");

    private final String description;

    IncomeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
