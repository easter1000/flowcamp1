package com.example.myapp.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapp.data.Restaurant;

public class SelectedRestaurantViewModel extends ViewModel {
    private final MutableLiveData<Restaurant> selected = new MutableLiveData<>();

    public void select(Restaurant restaurant) { selected.setValue(restaurant); }

    public LiveData<Restaurant> getSelected() { return selected; }
}