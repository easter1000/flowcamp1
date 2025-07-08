package com.example.myapp.ui.gallery;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.myapp.ui.map.PlacePickerActivity;
import com.example.myapp.R;
import com.example.myapp.data.CuisineType;
import com.example.myapp.data.Restaurant;
import com.example.myapp.ui.DBRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import android.app.Activity;
import android.util.Log;
import androidx.activity.result.contract.ActivityResultContracts;

public class AddRestaurantDialogFragment extends DialogFragment {

    private static final String ARG_RESTAURANT_ID = "restaurant_id";
    private static final int MAX_AR_NAME_LENGTH = 30;
    private static final int MAX_AR_LOCATION_LENGTH = 70;
    private static final String ARG_RESTAURANT_NAME = "restaurantName";
    private TextInputEditText arName, arLocation;
    private EditText dLocation;
    private Spinner spCuisineType;
    private Button btnCancel, btnSelect;
    private ActivityResultLauncher<Intent> startAutocomplete;
    private DBRepository repo;
    private long restaurantId = -1;
    private Restaurant currentRestaurant;
    private AlertDialog dialog;
    private TextInputLayout tilName, tilLocation;
    private ImageView ivMapPreview;
    private FrameLayout mapPreviewContainer;
    private LinearLayout layoutMapPlaceholder;
    private double selectedLat, selectedLng;
    private ImageView ivMenuImage;
    private TextView tvSelectMenuImage;
    private ActivityResultLauncher<String[]> pickImageLauncher;
    private Uri selectedImageUri;

    public static AddRestaurantDialogFragment newInstanceForCreate(String restaurantName) {
        AddRestaurantDialogFragment fragment = new AddRestaurantDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RESTAURANT_NAME, restaurantName);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddRestaurantDialogFragment newInstanceForEdit(long restaurantId) {
        AddRestaurantDialogFragment fragment = new AddRestaurantDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RESTAURANT_ID, restaurantId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            restaurantId = getArguments().getLong(ARG_RESTAURANT_ID, -1);
        }
        repo = new DBRepository(requireContext());
        registerImagePickerLauncher();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_restaurant, null);

        arName = view.findViewById(R.id.ar_restaurant);
        arLocation = view.findViewById(R.id.ar_location);
        spCuisineType = view.findViewById(R.id.spinner_cuisine_type);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSelect = view.findViewById(R.id.btn_select);
        ivMapPreview = view.findViewById(R.id.iv_map_preview);
        mapPreviewContainer = view.findViewById(R.id.map_preview_container);
        layoutMapPlaceholder = view.findViewById(R.id.layout_map_placeholder);
        dLocation = view.findViewById(R.id.ar_dlocation);

        tilName = view.findViewById(R.id.til_restaurant);
        tilLocation = view.findViewById(R.id.til_location);

        ivMenuImage = view.findViewById(R.id.iv_menu_image);
        tvSelectMenuImage = view.findViewById(R.id.tv_select_menu_image);
        tvSelectMenuImage.setPaintFlags(tvSelectMenuImage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        View.OnClickListener imagePickerClickListener = v -> openGallery();
        ivMenuImage.setOnClickListener(imagePickerClickListener);
        tvSelectMenuImage.setOnClickListener(imagePickerClickListener);

        registerAutocompleteLauncher();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                CuisineType.getDisplayNames(false)
        );
        spCuisineType.setAdapter(adapter);

        if (restaurantId != -1) {
            btnSelect.setText("수정");
            loadRestaurantDataForEdit();
        } else {
            btnSelect.setText("추가");
            String restaurantName = getArguments().getString(ARG_RESTAURANT_NAME, "");
            arName.setText(restaurantName);
        }

        mapPreviewContainer.setOnClickListener(v -> startAutocompleteIntent());
        btnCancel.setOnClickListener(v -> dismiss());
        btnSelect.setOnClickListener(v -> saveRestaurant());

        arLocation.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String addressString = arLocation.getText().toString().trim();
                if (!addressString.isEmpty()) {
                    geocodeAddressAndUpdateMap(addressString);
                } else {
                    tilLocation.setError(null);
                }
            }
        });

        btnSelect.setEnabled(checkFields());

        View customTitleView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_custom_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.dialog_title_text);
        ImageView backBtn = customTitleView.findViewById(R.id.dialog_title_back);

        titleTextView.setText(restaurantId != -1 ? "가게 수정" : "가게 추가");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setCustomTitle(customTitleView)
                .setView(view);
        backBtn.setOnClickListener(v -> dismiss());

        dialog = builder.create();
        dialog.setOnShowListener(this::onDialogShow);

        return dialog;
    }

    private void onDialogShow(DialogInterface dialogInterface) {
        addValidationListener(arName, tilName, MAX_AR_NAME_LENGTH, "최대 " + MAX_AR_NAME_LENGTH + "자까지 입력 가능합니다");
        addValidationListener(arLocation, tilLocation, MAX_AR_LOCATION_LENGTH, "최대 " + MAX_AR_LOCATION_LENGTH + "자까지 입력 가능합니다");

        btnSelect.setEnabled(restaurantId != -1 || checkFields());
    }

    private void loadRestaurantDataForEdit() {
        repo.getRestaurantById(restaurantId).observe(this, restaurant -> {
            if (restaurant == null) {
                Toast.makeText(getContext(), "정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }
            currentRestaurant = restaurant;
            arName.setText(restaurant.name);
            arLocation.setText(restaurant.location);
            dLocation.setText(restaurant.detailedLocation);

            if (restaurant.menuBoardUri != null) {
                selectedImageUri = restaurant.menuBoardUri;
                Glide.with(this)
                        .load(selectedImageUri)
                        .into(ivMenuImage);
            }

            int position = restaurant.cuisineType.ordinal() - 1;
            if (position >= 0 && position < spCuisineType.getCount()) {
                spCuisineType.setSelection(position);
            }

            if (restaurant.latitude != 0.0 && restaurant.longitude != 0.0) {
                selectedLat = restaurant.latitude;
                selectedLng = restaurant.longitude;
                updateMapPreview(selectedLat, selectedLng);
            }
        });
    }

    private void saveRestaurant() {
        View currentFocus = requireDialog().getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
        if (!checkFields()) {
            return;
        }

        if (restaurantId != -1) {
            updateExistingRestaurant();
        } else {
            createNewRestaurant();
        }
    }

    private void updateExistingRestaurant() {
        if (currentRestaurant == null) return;

        geocodeAddressAndUpdate(restaurant -> {
            repo.updateRestaurant(restaurant);
            Toast.makeText(getContext(), "“" + restaurant.name + "” 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void createNewRestaurant() {
        geocodeAddressAndUpdate(restaurant -> {
            repo.insertRestaurant(restaurant, id -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (id > -1) {
                            Toast.makeText(getContext(), "“" + restaurant.name + "”이(가) 등록되었습니다.", Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "데이터베이스 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }

    private void geocodeAddressAndUpdate(RestaurantUpdateCallback callback) {
        String restName = arName.getText().toString().trim();
        String locName = arLocation.getText().toString().trim();
        String dlocName = dLocation.getText().toString().trim();

        Geocoder geocoder = new Geocoder(requireContext());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(locName, 1);
        } catch (IOException e) {
            Log.e("AddRestaurantDialog", "주소 변환 오류", e);
            Toast.makeText(getContext(), "주소 변환 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        if (addresses == null || addresses.isEmpty()) {
            Toast.makeText(getContext(), "주소를 찾을 수 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        Address address = addresses.get(0);
        double latitude = address.getLatitude();
        double longitude = address.getLongitude();

        int selectedPosition = spCuisineType.getSelectedItemPosition();
        CuisineType selectedType = CuisineType.values()[selectedPosition + 1];

        Restaurant restaurantToUpdate;
        if (currentRestaurant != null) {
            restaurantToUpdate = currentRestaurant;
        } else {
            restaurantToUpdate = new Restaurant();
        }

        restaurantToUpdate.name = restName;
        restaurantToUpdate.location = locName;
        restaurantToUpdate.detailedLocation = dlocName;
        restaurantToUpdate.cuisineType = selectedType;
        restaurantToUpdate.latitude = latitude;
        restaurantToUpdate.longitude = longitude;

        if (selectedImageUri != null) {
            restaurantToUpdate.menuBoardUri = selectedImageUri;
        }

        callback.onUpdate(restaurantToUpdate);
    }

    interface RestaurantUpdateCallback {
        void onUpdate(Restaurant restaurant);
    }

    private void geocodeAddressAndUpdateMap(String addressString) {
        tilLocation.setError(null);
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.KOREAN);
            try {
                List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
                if (getActivity() == null) return;

                if (addresses != null && !addresses.isEmpty()) {
                    Address location = addresses.get(0);
                    selectedLat = location.getLatitude();
                    selectedLng = location.getLongitude();
                    getActivity().runOnUiThread(() -> {
                        tilLocation.setError(null);
                        updateMapPreview(selectedLat, selectedLng);
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        tilLocation.setError("유효한 주소를 찾을 수 없습니다.");
                    });
                }
            } catch (IOException e) {
                Log.e("AddRestaurantDialog", "수동 주소 지오코딩 실패", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tilLocation.setError("주소 검색 중 오류가 발생했습니다.");
                    });
                }
            }
        }).start();
    }

    private void updateMapPreview(double lat, double lng) {
        if (lat == 0.0 && lng == 0.0 || getContext() == null) return;

        String apiKey = "-1";
        try {
            ApplicationInfo appInfo = requireContext().getPackageManager().getApplicationInfo(
                    requireContext().getPackageName(),
                    PackageManager.GET_META_DATA
            );
            if (appInfo.metaData != null) {
                apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("MainActivity", "Manifest에서 API 키를 읽는 중 오류 발생", e);
        }

        if (Objects.equals(apiKey, "-1")) {
            Log.e("MainActivity", "API 키가 설정되지 않았습니다.");
            return;
        }
        String url = "https://maps.googleapis.com/maps/api/staticmap?" +
                "center=" + lat + "," + lng +
                "&zoom=16" +
                "&size=600x300" +
                "&maptype=roadmap" +
                "&markers=color:red%7C" + lat + "," + lng +
                "&key=" + apiKey;

        Glide.with(getContext())
                .load(url)
                .into(ivMapPreview);

        layoutMapPlaceholder.setVisibility(View.GONE);
    }

    private void registerImagePickerLauncher() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);

                selectedImageUri = uri;

                Glide.with(this)
                        .load(selectedImageUri)
                        .into(ivMenuImage);
            }
        });
    }

    private void openGallery() {
        pickImageLauncher.launch(new String[]{"image/*"});
    }

    private void registerAutocompleteLauncher() {
        startAutocomplete = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String name = data.getStringExtra("place_name");
                        String address = data.getStringExtra("place_address");
                        selectedLat = data.getDoubleExtra("place_lat", 0.0);
                        selectedLng = data.getDoubleExtra("place_lng", 0.0);

                        boolean isFromMapClick = data.getBooleanExtra("is_from_map_click", false);

                        arLocation.setText(address);
                        updateMapPreview(selectedLat, selectedLng);

                        if (isFromMapClick) {
                            arName.requestFocus();

                            arName.postDelayed(() -> {
                                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.showSoftInput(arName, InputMethodManager.SHOW_IMPLICIT);
                                }
                            }, 100);

                        } else {
                            if (name != null && !name.isEmpty()) {
                                arName.setText(name);
                            }
                        }

                    } else {
                        Log.i("AddRestaurantActivity", "장소 선택이 취소되었습니다.");
                    }
                });
    }

    private void startAutocompleteIntent() {
        Intent intent = new Intent(requireContext(), PlacePickerActivity.class);
        if (selectedLat != 0.0 && selectedLng != 0.0) {
            intent.putExtra("init_lat", selectedLat);
            intent.putExtra("init_lng", selectedLng);
        }
        startAutocomplete.launch(intent);
    }

    private boolean checkFields() {
        boolean isNameEmpty = arName.getText().toString().trim().isEmpty();
        boolean isLocationEmpty = arLocation.getText().toString().trim().isEmpty();
        boolean isNameShort = arName.getText().toString().trim().length() < MAX_AR_NAME_LENGTH;
        boolean isLocationShort = arLocation.getText().toString().trim().length() < MAX_AR_LOCATION_LENGTH;
        return !isNameEmpty && !isLocationEmpty && isNameShort && isLocationShort;
    }

    private void addValidationListener(EditText editText, TextInputLayout textInputLayout, int maxLength, String errorMsg) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean selfChange = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (selfChange) return;

                if (s.length() > maxLength) {
                    textInputLayout.setError(errorMsg);
                } else {
                    textInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (selfChange) return;

                if (s.length() > maxLength) {
                    selfChange = true;
                    s.delete(maxLength, s.length());
                    selfChange = false;
                }

                if (btnSelect != null) {
                    btnSelect.setEnabled(checkFields());
                }
            }
        });
    }
}