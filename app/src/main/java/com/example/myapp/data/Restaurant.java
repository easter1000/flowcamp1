package com.example.myapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

import android.net.Uri;

@Entity(tableName = "restaurants")
public class Restaurant {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name="name")
    public String name;

    @ColumnInfo(name="menu_board_uri")
    public Uri menuBoardUri;

    @ColumnInfo(name="location")
    public String location;

    @ColumnInfo(name="cuisine_type")
    public CuisineType cuisineType;

    @ColumnInfo(name="latitude")
    public double latitude;

    @ColumnInfo(name="longitude")
    public double longitude;
}
