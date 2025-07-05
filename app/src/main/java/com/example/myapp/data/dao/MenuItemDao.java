package com.example.myapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.myapp.data.MenuItem;

import java.util.List;

@Dao
public interface MenuItemDao {

    @Insert long insert(MenuItem item);

    @Update void update(MenuItem item);

    @Delete void delete(MenuItem item);

    @Query("SELECT * FROM menu_items ORDER BY id DESC")
    LiveData<List<MenuItem>> getAll();

    @Query("SELECT * FROM menu_items JOIN restaurants ON restaurants.id WHERE cuisine_type = :type")
    LiveData<List<MenuItem>> getByType(String type);

    @Query("SELECT * FROM menu_items WHERE id = :id")
    LiveData<MenuItem> getById(long id);
}
