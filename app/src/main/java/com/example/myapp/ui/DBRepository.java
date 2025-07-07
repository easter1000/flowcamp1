package com.example.myapp.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.myapp.data.CuisineType;
import com.example.myapp.data.dao.MenuItemDao;
import com.example.myapp.data.dao.RestaurantDao;
import com.example.myapp.data.db.AppDatabase;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class DBRepository {

    private final RestaurantDao restaurantDao;
    private final MenuItemDao menuItemDao;
    private final Executor io = Executors.newSingleThreadExecutor();

    public DBRepository(Context ctx) {
        AppDatabase db = AppDatabase.getInstance(ctx);
        restaurantDao = db.restaurantDao();
        menuItemDao = db.menuItemDao();
    }

    public LiveData<List<MenuItem>> getAllMenus() { return menuItemDao.getAll(); }
    public LiveData<List<MenuItem>> getMenuByType(CuisineType type) {
        if (type == CuisineType.ALL) {
            return menuItemDao.getAll();
        }
        return menuItemDao.getByType(type);
    }
    public LiveData<List<Restaurant>> getRestaurantByType(CuisineType type) {
        if (type == CuisineType.ALL) {
            return restaurantDao.getAll();
        }
        return restaurantDao.getByType(type);
    }

    public void insertMenu(MenuItem item) {
        io.execute(() -> menuItemDao.insert(item));
    }

    public LiveData<List<Restaurant>> getAllRestaurants() { return restaurantDao.getAll(); }

    public void insertRestaurant(Restaurant r, InsertCallback cb) {
        io.execute(() -> {
            long id = restaurantDao.insert(r);
            if (cb != null) cb.onInserted(id);
        });
    }

    public LiveData<MenuItem> getMenuById(long id) { return menuItemDao.getById(id); }
    public LiveData<Restaurant> getRestaurantById(long id) { return restaurantDao.getById(id); }
    public void restaurantExists(String name, Consumer<Boolean> callback) {
        io.execute(() -> {
            boolean exists = restaurantDao.existByName(name);
            new Handler(Looper.getMainLooper()).post(() -> callback.accept(exists));
        });
    }

    public void updateMenu(MenuItem item) {
        io.execute(() -> menuItemDao.update(item));
    }

    public void deleteMenuById(long menuId) {
        io.execute(() -> menuItemDao.deleteById(menuId));
    }

    public void deleteRestaurant(Restaurant restaurant) {
        io.execute(() -> restaurantDao.delete(restaurant));
    }

    public interface InsertCallback { void onInserted(long id); }
}
