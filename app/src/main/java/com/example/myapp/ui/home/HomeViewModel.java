package com.example.myapp.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;
import com.example.myapp.data.dao.RestaurantDao;
import com.example.myapp.ui.DBRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final DBRepository repo;
    private final LiveData<List<Restaurant>> restaurants;
    private final LiveData<List<MenuItem>> menuItems;

    public HomeViewModel(@NonNull Application app) {
        super(app);
        repo = new DBRepository(app);
        restaurants = repo.getAllRestaurants();
        menuItems = repo.getAllMenus();
    }

    public LiveData<List<Restaurant>> getRestaurants() { return restaurants; }

    public LiveData<List<MenuItem>> getMenuItems() { return menuItems; }

    /*
    private void loadPlacesData() {
        List<Place> dummyList = new ArrayList<>();
        dummyList.add(new Place("식당1", "주소1", "카테고리1", 0));
        dummyList.add(new Place("식당2", "주소2", "카테고리2", 1));
        dummyList.add(new Place("식당3", "주소3", "카테고리3", 2));
        dummyList.add(new Place("식당1", "주소1", "카테고리1", 3));
        dummyList.add(new Place("식당2", "주소2", "카테고리2", 4));
        dummyList.add(new Place("식당3", "주소3", "카테고리3", 5));
        dummyList.add(new Place("식당1", "주소1", "카테고리1", 6));
        dummyList.add(new Place("식당2", "주소2", "카테고리2", 1));
        dummyList.add(new Place("식당3", "주소3", "카테고리3", 2));
        dummyList.add(new Place("식당1", "주소1", "카테고리1", 3));
        dummyList.add(new Place("식당2", "주소2", "카테고리2", 4));
        dummyList.add(new Place("식당3", "주소3", "카테고리3", 5));
        placesLiveData.setValue(dummyList);

    }
    */

}