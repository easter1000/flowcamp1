package com.example.myapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.myapp.data.CuisineType;
import com.example.myapp.data.MenuItem;

import java.util.List;

@Dao
public interface MenuItemDao {

    @Insert long insert(MenuItem item);

    @Update void update(MenuItem item);

    @Delete void delete(MenuItem item);

    @Query("SELECT * FROM menu_items ORDER BY id DESC")
    LiveData<List<MenuItem>> getAll();

    @Query("SELECT * FROM restaurants NATURAL INNER JOIN menu_items WHERE cuisine_type = :type ORDER BY id DESC")
    LiveData<List<MenuItem>> getByType(CuisineType type);

    @Query("SELECT * FROM menu_items WHERE id = :id")
    LiveData<MenuItem> getById(long id);
}
