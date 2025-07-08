package com.example.myapp.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapp.R;
import com.example.myapp.data.Restaurant;
import com.example.myapp.databinding.FragmentMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class MapFragment extends Fragment  implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private MapViewModel mapViewModel;
    private GoogleMap googleMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //observeViewModel();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // OnMapReadyCallback을 현재 클래스로 설정
        }

    }

    // Get a handle to the GoogleMap object and display marker.
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMarkerClickListener(this::onMarkerClick);
        observeViewModel();
    }

    public void observeViewModel() {
        mapViewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            if (this.googleMap != null && restaurants != null) {
                displayMarkers(restaurants);
            }
        });
    }

    public void displayMarkers(List<Restaurant> restaurants) {
        this.googleMap.clear();

        if (restaurants.isEmpty()) {
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant restaurant = restaurants.get(i);
            LatLng latLng = new LatLng(1 + i, 1 + i);
            builder.include(latLng);
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(1 + i, 1 + i))
                    .icon(BitmapDescriptorFactory.defaultMarker(40)));
            if (marker != null) {
                marker.setTag(restaurant.id);
            }
        }

        LatLngBounds bounds = builder.build();
        int padding = 100; // 지도의 가장자리로부터의 여백 (픽셀 단위)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }


    public boolean onMarkerClick(@NonNull Marker marker) {
        Object tag = marker.getTag();

        if (tag instanceof Long) {
            long restaurantId = (Long) tag;
            MapBottomSheet.newInstance(restaurantId)
                    .show(getChildFragmentManager(), "detail");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
