package com.example.myapp.ui.notifications;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;
import com.example.myapp.ui.DBRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NotificationsViewModel extends AndroidViewModel {

    private final DBRepository repository;
    private final LiveData<List<Restaurant>> allRestaurants;
    private final LiveData<List<MenuItem>> allMenus;

    private final MediatorLiveData<Map<Restaurant, List<MenuItem>>> combinedData = new MediatorLiveData<>();

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        repository = new DBRepository(application);
        allRestaurants = repository.getAllRestaurants();
        allMenus = repository.getAllMenus();

        combinedData.addSource(allRestaurants, restaurants -> combineLiveData());
        combinedData.addSource(allMenus, menuItems -> combineLiveData());
    }

    private void combineLiveData() {
        List<Restaurant> restaurants = allRestaurants.getValue();
        List<MenuItem> menus = allMenus.getValue();

        if (restaurants == null || menus == null) {
            return;
        }

        Map<Restaurant, List<MenuItem>> resultMap = new LinkedHashMap<>();
        for (Restaurant restaurant : restaurants) {
            resultMap.put(restaurant, new ArrayList<>());
        }

        for (MenuItem menu : menus) {
            for (Restaurant restaurant : restaurants) {
                if (menu.restaurantId == restaurant.id) {
                    List<MenuItem> menuListOfRestaurant = resultMap.get(restaurant);
                    if (menuListOfRestaurant != null) {
                        menuListOfRestaurant.add(menu);
                    }
                    break;
                }
            }
        }
        combinedData.setValue(resultMap);
    }

    public LiveData<Map<Restaurant, List<MenuItem>>> getCombinedData() {
        return combinedData;
    }
}