package com.example.myapp.model;

public class Place {
    private final String name;
    private final String address;
    private final String category;

    public Place(String name, String address, String category) {
        this.name = name;
        this.address = address;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCategory() {
        return category;
    }
}

