package com.example.myapp.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapp.model.Place;
import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    //private final MutableLiveData<String> mText;
    private final MutableLiveData<List<Place>> placesLiveData = new MutableLiveData<>();

    public HomeViewModel() {
        //mText = new MutableLiveData<>();
        //mText.setValue("This is home fragment");
        loadPlacesData();
    }

    public LiveData<List<Place>> getPlaces() {
        return placesLiveData;
    }

    private void loadPlacesData() {
        List<Place> dummyList = new ArrayList<>();
        dummyList.add(new Place("식당1", "주소1", "카테고리1"));
        dummyList.add(new Place("식당2", "주소2", "카테고리2"));
        dummyList.add(new Place("식당3", "주소3", "카테고리3"));
        placesLiveData.setValue(dummyList);

    }

    /*public LiveData<String> getText() {
        return mText;
    }*/
}