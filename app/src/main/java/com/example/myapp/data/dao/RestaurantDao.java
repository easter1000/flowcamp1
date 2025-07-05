package com.example.myapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Relation;
import androidx.room.Transaction;

import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;

import java.util.List;

@Dao
public interface RestaurantDao {
    class RestaurantWithMenus {
        @Embedded
        public Restaurant restaurant;

        @Relation(
                parentColumn = "id",
                entityColumn = "restaurant_id"
        )
        public List<MenuItem> menus;
    }

    @Insert
    long insert(Restaurant restaurant);

    @Query("SELECT * FROM restaurants ORDER BY name")
    LiveData<List<Restaurant>> getAll();

    @Transaction
    @Query("SELECT * FROM restaurants WHERE id = :restaurantId")
    LiveData<RestaurantWithMenus> getRestaurantWithMenus(long restaurantId);

    @Transaction
    @Query("SELECT * FROM restaurants NATURAL INNER JOIN menu_items ORDER BY name")
    LiveData<List<RestaurantWithMenus>> getAllRestaurantsWithMenus();

    @Query("SELECT * FROM restaurants WHERE id = :id")
    LiveData<Restaurant> getById(long id);

    @Query("SELECT COUNT(*) FROM restaurants WHERE name = :name")
    Boolean existByName(String name);

}
