package com.example.myapp.ui.gallery;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.myapp.data.CuisineType;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;
import com.example.myapp.ui.DBRepository;

import java.util.List;

public class GalleryViewModel extends AndroidViewModel {

    private final DBRepository repo;
    private final LiveData<List<MenuItem>> menuItems;
    private final LiveData<List<Restaurant>> restaurants;
    private final MutableLiveData<CuisineType> filterType = new MutableLiveData<>();

    public GalleryViewModel(@NonNull Application app) {
        super(app);
        repo = new DBRepository(app);
        menuItems = Transformations.switchMap(filterType, repo::getMenuByType);
        restaurants = repo.getAllRestaurants();
    }

    public LiveData<MenuItem> getMenuItemById(long id) { return repo.getMenuById(id); }
    public LiveData<List<MenuItem>> getMenuItems()   { return menuItems; }
    public LiveData<List<Restaurant>> getRestaurants() { return restaurants; }
    public LiveData<Restaurant> getRestaurantById(long id) { return repo.getRestaurantById(id); }

    public void addMenu(MenuItem item) { repo.insertMenu(item); }

    public void setFilter(CuisineType type) {
        filterType.setValue(type);
    }
}
