package com.example.myapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.ColumnInfo;

import static androidx.room.ForeignKey.CASCADE;

import android.net.Uri;

@Entity(
        tableName = "menu_items",
        foreignKeys = @ForeignKey(
                entity = Restaurant.class,
                parentColumns = "id",
                childColumns = "restaurant_id",
                onDelete = CASCADE
        ),
        indices = @Index("restaurant_id")
)
public class MenuItem {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name="menu_name")
    public String menuName;

    @ColumnInfo(name="image_uri")
    public Uri imageUri;

    @ColumnInfo(name="rating")
    public float rating;

    @ColumnInfo(name="review")
    public String review;

    @ColumnInfo(name="restaurant_id")
    public long restaurantId;
}
