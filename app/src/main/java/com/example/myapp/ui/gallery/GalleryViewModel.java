package com.example.myapp.ui.gallery;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class GalleryViewModel extends ViewModel {
    private final MutableLiveData<List<Uri>> imageUris = new MutableLiveData<>();

    public GalleryViewModel() {
        imageUris.setValue(new ArrayList<>());
    }

    public LiveData<List<Uri>> getImageUris() {
        return imageUris;
    }

    public void loadImages() {
        // imageUris.setValue(loadedUriList);
    }

    public void addImage(Uri uri) {
        List<Uri> current = imageUris.getValue();
        if (current == null) {
            current = new ArrayList<>();
        }
        current.add(uri);
        imageUris.setValue(current);
    }
}
