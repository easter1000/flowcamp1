package com.example.myapp.ui.map;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.myapp.data.CuisineType;
import com.example.myapp.data.Restaurant;
import com.example.myapp.data.dao.RestaurantDao;
import com.example.myapp.ui.DBRepository;

import java.util.List;

public class MapViewModel extends AndroidViewModel {

    private final DBRepository repo;
    private final LiveData<List<Restaurant>> restaurants;
    private final MutableLiveData<CuisineType> filterType = new MutableLiveData<>();

    public MapViewModel(@NonNull Application app) {
        super(app);
        repo = new DBRepository(app);
        restaurants = repo.getAllRestaurants();
    }

    public LiveData<List<Restaurant>> getRestaurants() { return restaurants; }

    public LiveData<RestaurantDao.RestaurantWithMenus> getRestaurantWithMenus(long restaurantId) {
        return repo.getRestaurantWithMenus(restaurantId);
    }

    public void setFilter(CuisineType type) {
        filterType.setValue(type);
    }

    public void deleteRestaurant(Restaurant restaurant) {
        repo.deleteRestaurant(restaurant);
    }
}