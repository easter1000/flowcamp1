package com.example.myapp.ui.gallery;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;

import java.util.List;

public class GalleryViewModel extends AndroidViewModel {

    private final GalleryRepository repo;
    private final LiveData<List<MenuItem>> menuItems;
    private final LiveData<List<Restaurant>> restaurants;

    public GalleryViewModel(@NonNull Application app) {
        super(app);
        repo = new GalleryRepository(app);
        menuItems = repo.getAllMenus();
        restaurants = repo.getAllRestaurants();
    }

    public LiveData<List<MenuItem>> getMenuItems()   { return menuItems; }
    public LiveData<List<Restaurant>> getRestaurants() { return restaurants; }

    public void addMenu(MenuItem item) { repo.insertMenu(item); }

    public void addRestaurantWithFirstMenu(Restaurant r, MenuItem first) {
        repo.insertRestaurant(r, id -> {
            first.restaurantId = id;
            addMenu(first);
        });
    }

    public void loadImages() {}
}
