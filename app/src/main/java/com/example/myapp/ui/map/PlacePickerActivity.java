package com.example.myapp.ui.map;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.example.myapp.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.api.model.Place.Type;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PlacePickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Place selectedPlace;
    private Marker currentMarker;
    private Button btnSelectLocation;
    private static final LatLng DEFAULT_LOCATION = new LatLng(37.5665, 126.9780); // 서울 시청

    private FusedLocationProviderClient fusedLocationClient;
    private AutocompleteSupportFragment autocompleteFragment;
    private PlacesClient placesClient;

    private CardView cardPlaceInfo;
    private TextView tvPlaceName;
    private TextView tvPlaceAddress;
    private TextView tvPlaceCategory;
    private ImageButton btnCloseCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placepicker);

        placesClient = Places.createClient(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnSelectLocation = findViewById(R.id.btn_select_location);
        btnSelectLocation.setEnabled(false);

        initInfoCardView();

        setupAutocompleteFragment();
        setupSelectButton();
    }

    private void initInfoCardView() {
        cardPlaceInfo = findViewById(R.id.card_place_info);
        tvPlaceName = findViewById(R.id.tv_place_name);
        tvPlaceAddress = findViewById(R.id.tv_place_address);
        tvPlaceCategory = findViewById(R.id.tv_place_category);
        btnCloseCard = findViewById(R.id.btn_close_card);

        btnCloseCard.setOnClickListener(v -> hidePlaceInfoCard());
    }

    private void setupAutocompleteFragment() {
        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteFragment == null) return;

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));
        autocompleteFragment.setCountry("KR");
        autocompleteFragment.setHint("가게 이름 검색");

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                hidePlaceInfoCard();
                selectedPlace = place;
                updateMapWithPlace(place);
                btnSelectLocation.setEnabled(true);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e("MapPlacePicker", "An error occurred during place selection: " + status);
                Toast.makeText(PlacePickerActivity.this, "장소를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSelectButton() {
        btnSelectLocation.setOnClickListener(v -> {
            if (selectedPlace != null) {
                Intent resultIntent = new Intent();
                boolean isFromMapClick = selectedPlace.getId() == null;

                resultIntent.putExtra("place_name", selectedPlace.getName());
                resultIntent.putExtra("place_address", selectedPlace.getAddress());
                if (selectedPlace.getLatLng() != null) {
                    resultIntent.putExtra("place_lat", selectedPlace.getLatLng().latitude);
                    resultIntent.putExtra("place_lng", selectedPlace.getLatLng().longitude);
                }
                resultIntent.putExtra("is_from_map_click", isFromMapClick);

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        updateLocationUI();
        getDeviceLocation();

        mMap.setOnMapClickListener(this::handleMapClick);

        mMap.setOnPoiClickListener(poi -> {
            if (poi.placeId != null) {
                fetchPlaceFromId(poi.placeId);
            }
        });
    }

    private void fetchPlaceFromId(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.TYPES);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    selectedPlace = place;
                    updateMapWithPlace(place);
                    autocompleteFragment.setText(place.getName());
                    btnSelectLocation.setEnabled(true);
                    showPlaceInfoCard(place);
                })
                .addOnFailureListener(e -> {
                    Log.e("MapPlacePicker", "Place not found.", e);
                    Toast.makeText(this, "가게 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showPlaceInfoCard(Place place) {
        tvPlaceName.setText(place.getName());
        tvPlaceAddress.setText(place.getAddress());
        cardPlaceInfo.setVisibility(View.VISIBLE);
        String category = getCategoryString(place.getTypes());
        if (category.isEmpty()) {
            tvPlaceCategory.setVisibility(View.GONE);
        } else {
            tvPlaceCategory.setText(category);
            tvPlaceCategory.setVisibility(View.VISIBLE);
        }
    }

    private void hidePlaceInfoCard() {
        if (cardPlaceInfo.getVisibility() == View.VISIBLE) {
            cardPlaceInfo.setVisibility(View.GONE);
        }
    }

    private String getCategoryString(List<Type> types) {
        if (types == null || types.isEmpty()) {
            return "";
        }

        if (types.contains(Type.CAFE)) return "카페";
        if (types.contains(Type.BAKERY)) return "베이커리";
        if (types.contains(Type.BAR)) return "주점/바";
        if (types.contains(Type.RESTAURANT)) return "음식점";
        if (types.contains(Type.STORE)) return "상점";

        return "";
    }

    private void getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (mMap != null) {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    } else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 12));
                        Toast.makeText(this, "현재 위치를 찾을 수 없습니다. 기본 위치로 시작합니다.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) return;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void handleMapClick(LatLng latLng) {
        boolean isCardVisible = cardPlaceInfo.getVisibility() == View.VISIBLE;

        hidePlaceInfoCard();

        if (isCardVisible) {
            return;
        }
        Geocoder geocoder = new Geocoder(this, Locale.KOREAN);
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                String featureName = address.getFeatureName();
                String placeName = (featureName != null && !featureName.equals(addressLine) && !Character.isDigit(featureName.charAt(0))) ? featureName : addressLine;

                selectedPlace = Place.builder()
                        .setName(placeName)
                        .setAddress(addressLine)
                        .setLatLng(latLng)
                        .build();

                updateMapWithPlace(selectedPlace);
                autocompleteFragment.setText(placeName);
                btnSelectLocation.setEnabled(true);

            } else {
                Toast.makeText(this, "해당 위치의 주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("MapPlacePicker", "Geocoder service not available", e);
            Toast.makeText(this, "주소 변환 서비스를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMapWithPlace(Place place) {
        if (mMap == null || place.getLatLng() == null) return;

        if (currentMarker != null) {
            currentMarker.remove();
        }

        LatLng placeLatLng = place.getLatLng();
        String title = place.getName() != null ? place.getName() : "선택된 위치";
        currentMarker = mMap.addMarker(new MarkerOptions().position(placeLatLng).title(title));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, mMap.getCameraPosition().zoom > 15 ? mMap.getCameraPosition().zoom : 16));
    }
}