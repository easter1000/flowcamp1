package com.example.myapp.ui.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.Place.Type;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnPoiClickListener {

    private FragmentMapBinding binding;
    private MapViewModel mapViewModel;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private CardView cardPlaceInfo;
    private TextView restaurantName;
    private TextView restaurantType;
    private TextView restaurantRating;
    private TextView restaurantLocation;
    private TextView distanceTextView;
    private TextView ratingAddressSeparatorTextView;
    private TextView showMenuboardTextView;
    HorizontalScrollView horizontalScrollViewImages;
    LinearLayout expandableContainer;
    LinearLayout linearLayoutImages;
    Button delBtn, editBtn;
    private Marker selectedMarker;
    private SelectedRestaurantViewModel selectedVM;
    private final Map<Long, Marker> markerMap = new HashMap<>();
    private Marker tempMarker;
    private PlacesClient placesClient;
    private Geocoder geocoder;
    private CardView cardNewPlaceInfo;
    private Place temporarySelectedPlace;
    private TextView tvNewPlaceName, tvNewPlaceAddress, tvNewPlaceCategory;

    private static final float DEFAULT_HUE = 40;
    private boolean isGenericLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        selectedVM = new ViewModelProvider(requireActivity()).get(SelectedRestaurantViewModel.class);

        if (getContext() != null) {
            placesClient = Places.createClient(getContext());
            geocoder = new Geocoder(getContext(), Locale.KOREAN);
        }

        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getParentFragmentManager().setFragmentResultListener("restaurantAddedRequest", this, (requestKey, bundle) -> {
            clearTemporarySelection();

            long newRestaurantId = bundle.getLong("newRestaurantId", -1);
            if (newRestaurantId != -1) {
                mapViewModel.getRestaurantById(newRestaurantId).observe(getViewLifecycleOwner(), newRestaurant -> {
                    if (newRestaurant != null) {
                        selectedVM.select(newRestaurant);
                    }
                });
            }
        });

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
            spinner.setDropDownVerticalOffset(spinner.getHeight());
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
            distanceTextView = cardPlaceInfo.findViewById(R.id.distanceTextView);
            ratingAddressSeparatorTextView = cardPlaceInfo.findViewById(R.id.textViewRatingAddressSeparator);
            showMenuboardTextView = cardPlaceInfo.findViewById(R.id.textViewMenuboard);
            ImageView mapBtn = cardPlaceInfo.findViewById(R.id.map_btn);
            if (mapBtn != null) mapBtn.setVisibility(View.GONE);
            cardPlaceInfo.setOnClickListener(v -> {});
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            loadCurrentLocation();
        }

        selectedVM.getSelected().observe(getViewLifecycleOwner(), restaurant -> {
            if (restaurant != null) focusOnRestaurant(restaurant);
        });

        initNewPlaceCard(view);
    }

    private void initNewPlaceCard(View view) {
        cardNewPlaceInfo = view.findViewById(R.id.card_new_place_info);
        tvNewPlaceName = view.findViewById(R.id.tv_new_place_name);
        tvNewPlaceAddress = view.findViewById(R.id.tv_new_place_address);
        tvNewPlaceCategory = view.findViewById(R.id.tv_new_place_category);
        Button btnRegisterPlace = view.findViewById(R.id.btn_register_place);
        ImageButton btnCloseNewCard = view.findViewById(R.id.btn_close_new_card);

        btnCloseNewCard.setOnClickListener(v -> clearTemporarySelection());

        btnRegisterPlace.setOnClickListener(v -> {
            if (temporarySelectedPlace != null && temporarySelectedPlace.getLatLng() != null) {
                AddRestaurantDialogFragment dialog = AddRestaurantDialogFragment.newInstanceForCreateFromPlace(
                        temporarySelectedPlace.getName(),
                        temporarySelectedPlace.getAddress(),
                        temporarySelectedPlace.getLatLng().latitude,
                        temporarySelectedPlace.getLatLng().longitude,
                        isGenericLocation
                );
                dialog.show(getParentFragmentManager(), "AddRestaurantFromMap");
            }
        });
    }

    private void loadCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    currentLocation = location;
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMarkerClickListener(this::onMarkerClick);
        this.googleMap.setOnMapClickListener(this::onMapClick);
        this.googleMap.setOnPoiClickListener(this::onPoiClick);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
            this.googleMap.setMyLocationEnabled(true);
            this.googleMap.setOnMyLocationButtonClickListener(this::onMyLocationButtonClick);
        }
        observeViewModel();
    }

    @Override
    public void onPoiClick(@NonNull PointOfInterest poi) {
        clearAllSelections();
        fetchPlaceDetails(poi.placeId);
    }

    private void onMapClick(LatLng latLng) {
        clearAllSelections();
        if (cardPlaceInfo.getVisibility() == View.VISIBLE) {
            return;
        }
        reverseGeocode(latLng);
    }

    private void fetchPlaceDetails(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            isGenericLocation = false;
            showNewPlaceInfoCard(place);
        }).addOnFailureListener(exception -> {
            Toast.makeText(getContext(), "장소 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            Log.e("MapFragment", "Place not found.", exception);
        });
    }

    private void reverseGeocode(LatLng latLng) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                String featureName = address.getFeatureName();
                String placeName;
                if (featureName != null && !featureName.equals(addressLine) && !Character.isDigit(featureName.charAt(0))) {
                    placeName = featureName;
                    isGenericLocation = false;
                } else {
                    placeName = "선택된 위치";
                    isGenericLocation = true;
                }

                Place place = Place.builder()
                        .setName(placeName)
                        .setAddress(addressLine)
                        .setLatLng(latLng)
                        .build();
                showNewPlaceInfoCard(place);
            } else {
                Toast.makeText(getContext(), "해당 위치의 주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("MapFragment", "Geocoder service not available", e);
            Toast.makeText(getContext(), "주소 변환 서비스를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
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
        markerMap.clear();

        if (restaurants == null || restaurants.isEmpty()) {
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Restaurant selectedRestaurant = selectedVM.getSelected().getValue();
        Long currentSelectedId = selectedRestaurant != null ? selectedRestaurant.id : null;

        for (Restaurant restaurant : restaurants) {
            if (restaurant.latitude == 0 || restaurant.longitude == 0) continue;

            LatLng pos = new LatLng(restaurant.latitude, restaurant.longitude);
            builder.include(pos);

            boolean isSelected = currentSelectedId != null && restaurant.id == currentSelectedId;
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            isSelected ? BitmapDescriptorFactory.HUE_RED
                                    : DEFAULT_HUE))
            );
            if (marker != null) {
                marker.setTag(restaurant.id);
                markerMap.put(restaurant.id, marker);
                if (isSelected) {
                    selectedMarker = marker;
                }
            }
        }

        if (selectedRestaurant == null) {
            if (getView() != null) {
                getView().post(() -> {
                    try {
                        if (restaurants.size() == 1) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(restaurants.get(0).latitude, restaurants.get(0).longitude), 15));
                        } else if (!restaurants.isEmpty()) {
                            int padding = 300;
                            LatLngBounds bounds = builder.build();
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                        }
                    } catch (IllegalStateException e) {
                        Log.e("MapFragment", "LatLngBounds or view not ready for camera update.", e);
                    }
                });
            }
        }
    }

    private void focusOnRestaurant(@NonNull Restaurant restaurant) {
        if (googleMap == null) return;
        clearTemporarySelection();

        if (selectedMarker != null) {
            selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(DEFAULT_HUE));
        }

        LatLng pos = new LatLng(restaurant.latitude, restaurant.longitude);

        selectedMarker = findMarkerByRestaurantId(restaurant.id);
        if (selectedMarker != null) {
            selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
        mapViewModel.getRestaurantWithMenus(restaurant.id)
                .observe(getViewLifecycleOwner(), this::showPlaceInfoCard);
    }

    private Marker findMarkerByRestaurantId(long id) {
        return markerMap.get(id);
    }
    private boolean onMarkerClick(@NonNull Marker marker) {
        Object tag = marker.getTag();

        if (tag instanceof Long) {
            clearTemporarySelection();
            long restaurantId = (Long) tag;

            mapViewModel.getRestaurantWithMenus(restaurantId)
                    .observe(getViewLifecycleOwner(), r -> {
                        if (r == null) return;

                        showPlaceInfoCard(r);
                        selectedVM.select(r.restaurant);

                        if (selectedMarker != null) {
                            selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(DEFAULT_HUE));
                        }
                        selectedMarker = marker;
                        selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    });
            return true;
        }
        return true;
    }

    private void showNewPlaceInfoCard(Place place) {
        temporarySelectedPlace = place;
        if (place.getLatLng() == null) return;

        if (tempMarker == null) {
            tempMarker = googleMap.addMarker(new MarkerOptions()
                    .position(place.getLatLng())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        } else {
            tempMarker.setPosition(place.getLatLng());
        }
        tempMarker.setTitle(place.getName());
        if (!tempMarker.isVisible()) tempMarker.setVisible(true);

        tvNewPlaceName.setText(place.getName());
        tvNewPlaceAddress.setText(place.getAddress());

        String category = getCategoryString(place.getTypes());
        if (category.isEmpty()) {
            tvNewPlaceCategory.setVisibility(View.GONE);
        } else {
            tvNewPlaceCategory.setText(category);
            tvNewPlaceCategory.setVisibility(View.VISIBLE);
        }

        cardNewPlaceInfo.setVisibility(View.VISIBLE);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 17));
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

        if (types.contains(Type.FOOD) || types.contains(Type.MEAL_TAKEAWAY) || types.contains(Type.MEAL_DELIVERY)) return "음식점";

        return "";
    }

    private void clearTemporarySelection() {
        if (tempMarker != null) {
            tempMarker.remove();
            tempMarker = null;
        }
        if (cardNewPlaceInfo != null) {
            cardNewPlaceInfo.setVisibility(View.GONE);
        }
        temporarySelectedPlace = null;
    }

    private void clearAllSelections() {
        if (cardPlaceInfo.getVisibility() == View.VISIBLE) {
            cardPlaceInfo.setVisibility(View.GONE);
        }
        if (selectedMarker != null) {
            selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(DEFAULT_HUE));
        }
        if (selectedVM.getSelected().getValue() != null) {
            selectedVM.select(null);
        }
        selectedMarker = null;

        clearTemporarySelection();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onMyLocationButtonClick() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
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
            restaurantRating.setText(String.format(Locale.ROOT, "⭐ %.1f", averageRating));
        } else {
            restaurantRating.setText("⭐ N/A");
        }

        if (currentLocation != null) {
            ratingAddressSeparatorTextView.setVisibility(View.VISIBLE);
            distanceTextView.setVisibility(View.VISIBLE);
            double distanceInMeters = distance(restaurant);
            String distanceText;
            if (distanceInMeters < 1000) {
                distanceText = String.format(Locale.getDefault(), "%.0fm", distanceInMeters);
            } else {
                double distanceInKm = distanceInMeters / 1000.0;
                distanceText = String.format(Locale.getDefault(), "%.1fkm", distanceInKm);
            }
            distanceTextView.setText(distanceText);
        } else {
            ratingAddressSeparatorTextView.setVisibility(View.GONE);
            distanceTextView.setVisibility(View.GONE);
        }

        if(restaurant.menuBoardUri != null) {
            showMenuboardTextView.setVisibility(View.VISIBLE);
            showMenuboardTextView.setPaintFlags(showMenuboardTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            showMenuboardTextView.setOnClickListener(v -> {
                showMenuDialog(requireContext(), restaurant);
            });
        } else {
            showMenuboardTextView.setVisibility(View.GONE);
        }

        restaurantName.setText(restaurant.name);
        restaurantType.setText(Converters.fromCuisine(restaurant.cuisineType));
        restaurantLocation.setText(restaurant.location);
        expandableContainer.setVisibility(View.VISIBLE);
        populateImages(menus, linearLayoutImages, requireContext());
        delBtn.setOnClickListener(v -> onDeleteClick(restaurant));
        editBtn.setOnClickListener(v -> onEditClick(restaurant));
        cardPlaceInfo.setVisibility(View.VISIBLE);
    }

    private void showMenuDialog(Context context, Restaurant r) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.item_menuboard, null);
        ImageView menuboard = dialogView.findViewById(R.id.imageMenuboard);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageButton closeButton = dialogView.findViewById(R.id.buttonClose);
        TextView dialogTitle = dialogView.findViewById(R.id.textViewDialogTitle);

        dialogTitle.setText("메뉴판");

        closeButton.setOnClickListener(v -> dialog.dismiss());

        Glide.with(context)
                .load(r.menuBoardUri)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_dashboard_black_24dp)
                .into(menuboard);

        dialog.show();
    }

    private double distance(Restaurant r) {
        if (currentLocation == null) return 0;
        Location restaurantLocation = new Location("restaurant");
        restaurantLocation.setLatitude(r.latitude);
        restaurantLocation.setLongitude(r.longitude);

        return currentLocation.distanceTo(restaurantLocation);
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
