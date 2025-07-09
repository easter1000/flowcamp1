package com.example.myapp.ui.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.myapp.R;
import com.example.myapp.data.CuisineType;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;
import com.example.myapp.data.dao.RestaurantDao;
import com.example.myapp.data.db.Converters;
import com.example.myapp.databinding.FragmentMapBinding;
import com.example.myapp.ui.gallery.AddRestaurantDialogFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private MapViewModel mapViewModel;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private CardView cardPlaceInfo;
    private TextView restaurantName;
    private TextView restaurantType;
    private TextView restaurantRating;
    private TextView restaurantLocation;
    HorizontalScrollView horizontalScrollViewImages;
    LinearLayout expandableContainer;
    LinearLayout linearLayoutImages;
    Button delBtn, editBtn;

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

        Spinner spinner = view.findViewById(R.id.spinner_filter);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                CuisineType.getDisplayNames(true)
        );
        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                CuisineType selectedType = CuisineType.values()[pos];
                mapViewModel.setFilter(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.post(() -> {
            spinner.setDropDownWidth(spinner.getWidth());
            spinner.setDropDownHorizontalOffset(0);
            spinner.setDropDownVerticalOffset(spinner.getHeight() + 20);
        });

        ImageButton btnAdd = view.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(v -> {
            AddRestaurantDialogFragment dialogFragment = AddRestaurantDialogFragment.newInstanceForCreate("");
            dialogFragment.show(getParentFragmentManager(), "AddRestaurantDialog");
        });

        if (binding != null) {
            cardPlaceInfo = binding.cardPlaceInfoContainer;
            restaurantName = cardPlaceInfo.findViewById(R.id.textViewPlaceName);
            restaurantType = cardPlaceInfo.findViewById(R.id.textViewPlaceCategory);
            restaurantRating = cardPlaceInfo.findViewById(R.id.textViewAverageRating);
            restaurantLocation = cardPlaceInfo.findViewById(R.id.textViewPlaceAddress);
            horizontalScrollViewImages = cardPlaceInfo.findViewById(R.id.horizontalScrollViewImages);
            expandableContainer = cardPlaceInfo.findViewById(R.id.expandableContainer);
            linearLayoutImages = cardPlaceInfo.findViewById(R.id.linearLayoutImages);
            delBtn = cardPlaceInfo.findViewById(R.id.btnDel);
            editBtn = cardPlaceInfo.findViewById(R.id.btnEdit);
        }

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
        this.googleMap.setOnMapClickListener(this::onEmptySpaceClick);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
            this.googleMap.setMyLocationEnabled(true);
            this.googleMap.setOnMyLocationButtonClickListener(this::onMyLocationButtonClick);
        }
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
        cardPlaceInfo.setVisibility(View.GONE);

        if (restaurants.isEmpty()) {
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Restaurant restaurant : restaurants) {
            double lat = restaurant.latitude;
            double lng = restaurant.longitude;
            if (lat != 0 && lng != 0) {
                LatLng latLng = new LatLng(lat, lng);
                builder.include(latLng);
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(40)));
                if (marker != null) {
                    marker.setTag(restaurant.id);
                }
            }
        }

        if (restaurants.size() == 1) {
            double lat = restaurants.get(0).latitude;
            double lng = restaurants.get(0).longitude;
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
        } else {
            LatLngBounds bounds = builder.build();
            int padding = 100; // 지도의 가장자리로부터의 여백 (픽셀 단위)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        }
    }


    public void onEmptySpaceClick(LatLng latLng) {
        cardPlaceInfo.setVisibility(View.GONE);
    }

    public boolean onMarkerClick(@NonNull Marker marker) {
        Object tag = marker.getTag();
        LatLng latLng = marker.getPosition();

        if (tag instanceof Long) {
            long restaurantId = (Long) tag;
            //Log.d("MapFragment", "onMarkerClick: " + restaurantId);
            mapViewModel.getRestaurantWithMenus(restaurantId).observe(getViewLifecycleOwner(), r -> {
                if (r != null) {
                    showPlaceInfoCard(r);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
                else {
                    //Log.d("MapFragment", "onMarkerClick: r is null");
                    cardPlaceInfo.setVisibility(View.GONE);
                }
            });
        } else {
            return false;
        }
        return true;
    }

    public boolean onMyLocationButtonClick() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });
        return true;
    }

    public void showPlaceInfoCard(RestaurantDao.RestaurantWithMenus r) {
        Restaurant restaurant = r.restaurant;
        List<MenuItem> menus = r.menus;

        float ratingSum = 0;
        int ratingCount = menus.size();
        for (MenuItem m : menus) {
            ratingSum += m.rating;
        }
        float averageRating;
        if (ratingCount > 0) {
            averageRating = ratingSum / ratingCount;
            restaurantRating.setText(String.format(Locale.ROOT, "%.1f", averageRating));
        } else {
            restaurantRating.setText("");
        }

        restaurantName.setText(restaurant.name);
        restaurantType.setText(restaurant.cuisineType.toString());
        restaurantLocation.setText(restaurant.location);
        expandableContainer.setVisibility(View.VISIBLE);
        populateImages(menus, linearLayoutImages, requireContext());
        delBtn.setOnClickListener(v -> onDeleteClick(restaurant));
        editBtn.setOnClickListener(v -> onEditClick(restaurant));
        cardPlaceInfo.setVisibility(View.VISIBLE);
    }

    private void populateImages(List<MenuItem> restaurantMenus, LinearLayout imageContainer, Context context) {
        imageContainer.removeAllViews();

        final int IMAGE_SIZE_DP = 110;
        final int MARGIN_RIGHT_DP = 2;

        int imageSizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, IMAGE_SIZE_DP, context.getResources().getDisplayMetrics());
        int marginRightInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, MARGIN_RIGHT_DP, context.getResources().getDisplayMetrics());

        LayoutInflater inflater = LayoutInflater.from(context);

        for (MenuItem menuItem : restaurantMenus) {
            View itemView = inflater.inflate(R.layout.item_gallery_image, imageContainer, false);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageSizeInPx, imageSizeInPx);
            params.rightMargin = marginRightInPx;
            itemView.setLayoutParams(params);

            ImageView imageView = itemView.findViewById(R.id.imageView);
            RatingBar ratingBar = itemView.findViewById(R.id.ratingBar);

            Glide.with(context)
                    .load(menuItem.imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_dashboard_black_24dp)
                    .into(imageView);

            ratingBar.setRating(menuItem.rating);

            imageContainer.addView(itemView);
        }
    }

    private void onDeleteClick(Restaurant restaurant) {
        Log.d("HomeFragment", "onDeleteClick received for: " + restaurant.name);
        new AlertDialog.Builder(requireContext())
                .setTitle("레스토랑 삭제")
                .setMessage("'" + restaurant.name + "'을(를) 정말 삭제하시겠습니까?\n관련된 모든 메뉴 정보도 함께 삭제됩니다.")
                .setPositiveButton("삭제", (dialog, which) -> {
                    mapViewModel.deleteRestaurant(restaurant);
                    Toast.makeText(getContext(), "'" + restaurant.name + "'이(가) 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void onEditClick(Restaurant restaurant) {
        AddRestaurantDialogFragment dialogFragment = AddRestaurantDialogFragment.newInstanceForEdit(restaurant.id);
        dialogFragment.show(getParentFragmentManager(), "EditRestaurantDialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
