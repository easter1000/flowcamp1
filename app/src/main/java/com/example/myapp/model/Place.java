package com.example.myapp.model;

public class Place {
    private final String name;
    private final String address;
    private final String category;
    private final int reviews;

    public Place(String name, String address, String category, int reviews) {
        this.name = name;
        this.address = address;
        this.category = category;
        this.reviews = reviews;
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

    public int getReviews() {
        return reviews;
    }
}

