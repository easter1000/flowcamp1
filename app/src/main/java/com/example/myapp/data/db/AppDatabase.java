package com.example.myapp.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.myapp.data.dao.MenuItemDao;
import com.example.myapp.data.dao.RestaurantDao;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;

@Database(
        entities = {Restaurant.class, MenuItem.class},
        version = 4,
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract RestaurantDao restaurantDao();
    public abstract MenuItemDao menuItemDao();

    public static AppDatabase getInstance(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(ctx.getApplicationContext(),
                                    AppDatabase.class,
                                    "food_gallery.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
