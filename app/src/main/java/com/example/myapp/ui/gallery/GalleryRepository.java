package com.example.myapp.ui.gallery;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.myapp.data.dao.MenuItemDao;
import com.example.myapp.data.dao.RestaurantDao;
import com.example.myapp.data.db.AppDatabase;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GalleryRepository {

    private final RestaurantDao restaurantDao;
    private final MenuItemDao menuItemDao;
    private final Executor io = Executors.newSingleThreadExecutor();

    public GalleryRepository(Context ctx) {
        AppDatabase db = AppDatabase.getInstance(ctx);
        restaurantDao = db.restaurantDao();
        menuItemDao = db.menuItemDao();
    }

    public LiveData<List<MenuItem>> getAllMenus() { return menuItemDao.getAll(); }

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


    public interface InsertCallback { void onInserted(long id); }
}
