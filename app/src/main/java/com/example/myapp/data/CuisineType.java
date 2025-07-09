package com.example.myapp.data;

import java.util.ArrayList;
import java.util.List;

public enum CuisineType {
    ALL("전체"),
    KOREAN("한식"),
    WESTERN("양식"),
    CHINESE("중식"),
    JAPANESE("일식"),
    OTHER("기타"),
    UNKNOWN("오류");

    private final String displayName;
    CuisineType(String name) { this.displayName = name; }
    @Override public String toString() { return displayName; }
    public static String[] getDisplayNames(boolean includeALL) {
        List<String> namesList = new ArrayList<>();
        for (CuisineType type : values()) {
            if (type == UNKNOWN || !includeALL && type == ALL) { continue; }
            namesList.add(type.toString());
        }
        return namesList.toArray(new String[0]);
    }
}
