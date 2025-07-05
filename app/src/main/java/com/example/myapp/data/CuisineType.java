package com.example.myapp.data;

public enum CuisineType {
    KOREAN("한식"),
    WESTERN("양식"),
    JAPANESE("일식"),
    CHINESE("중식"),
    OTHER("기타");

    private final String korean;
    CuisineType(String korean) { this.korean = korean; }
    @Override public String toString() { return korean; }
}
