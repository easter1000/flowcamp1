package com.example.myapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Relation;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.myapp.data.CuisineType;
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

    @Delete
    void delete(Restaurant restaurant);

    @Update
    void update(Restaurant restaurant);

    @Query("SELECT * FROM restaurants ORDER BY name")
    LiveData<List<Restaurant>> getAll();

    @Transaction
    @Query("SELECT * FROM restaurants LEFT OUTER JOIN menu_items ON restaurants.id = menu_items.restaurant_id WHERE restaurants.id = :restaurantId")
    LiveData<RestaurantWithMenus> getRestaurantWithMenus(long restaurantId);

    @Transaction
    @Query("SELECT * FROM restaurants NATURAL INNER JOIN menu_items ORDER BY name")
    LiveData<List<RestaurantWithMenus>> getAllRestaurantsWithMenus();

    @Query("SELECT * FROM restaurants WHERE id = :id")
    LiveData<Restaurant> getById(long id);

    @Query("SELECT * FROM restaurants WHERE cuisine_type = :type ORDER BY id DESC")
    LiveData<List<Restaurant>> getByType(CuisineType type);

    @Query("SELECT COUNT(*) FROM restaurants WHERE name = :name")
    Boolean existByName(String name);

}
