package com.example.myapp.ui.home;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeViewModel extends AndroidViewModel {

    private final DBRepository repo;
    private final LiveData<List<Restaurant>> restaurants;
    private final LiveData<List<MenuItem>> menuItems;
    private final MutableLiveData<CuisineType> filterType = new MutableLiveData<>();
    private final Set<Long> openedItems = new HashSet();

    public HomeViewModel(@NonNull Application app) {
        super(app);
        repo = new DBRepository(app);
        restaurants = Transformations.switchMap(filterType, repo::getRestaurantByType);
        menuItems = repo.getAllMenus();
    }

    public LiveData<List<Restaurant>> getRestaurants() { return restaurants; }

    public LiveData<List<MenuItem>> getMenuItems() { return menuItems; }
    public Set<Long> getOpenedItems() { return openedItems; }

    public void setFilter(CuisineType type) {
        filterType.setValue(type);
    }

    public void deleteRestaurant(Restaurant restaurant) {
        repo.deleteRestaurant(restaurant);
    }

    public void updateRestaurant(Restaurant restaurant) {
        repo.updateRestaurant(restaurant);
    }

}