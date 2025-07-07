package com.example.myapp.ui.gallery;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.myapp.R;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;
import com.example.myapp.ui.DBRepository;

import java.util.List;
import java.util.stream.Collectors;

public class AddMenuDialogFragment extends DialogFragment {

    // --- Arguments Keys ---
    private static final String ARG_MENU_ID = "menu_id";
    private static final String ARG_IMAGE_URI = "image_uri";

    // --- Callbacks ---
    public interface OnMenuCreatedListener { void onCreated(MenuItem item, Restaurant restaurant); }
    public interface OnMenuUpdatedListener { void onUpdated(android.content.Context context); }

    // --- Member Variables ---
    private OnMenuCreatedListener createListener;
    private OnMenuUpdatedListener updateListener;
    private DBRepository repo;
    private List<Restaurant> allRestaurantsList;

    // UI Components
    private EditText menuNameEt, reviewEt, priceEt;
    private AutoCompleteTextView restaurantEt;
    private RatingBar ratingBar;
    private ImageView imageView;
    private AlertDialog dialog;

    // Data
    private long menuId = -1;
    private Uri imageUri;
    private MenuItem currentMenu;

    public static AddMenuDialogFragment newInstanceForCreate(Uri img, OnMenuCreatedListener l) {
        AddMenuDialogFragment f = new AddMenuDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE_URI, img);
        f.setArguments(args);
        f.createListener = l;
        return f;
    }

    public static AddMenuDialogFragment newInstanceForEdit(long menuId, OnMenuUpdatedListener l) {
        AddMenuDialogFragment f = new AddMenuDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_MENU_ID, menuId);
        f.setArguments(args);
        f.updateListener = l;
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            menuId = getArguments().getLong(ARG_MENU_ID, -1);
            imageUri = getArguments().getParcelable(ARG_IMAGE_URI);
        }
        repo = new DBRepository(requireContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_menu, null);
        initializeViews(v);

        setupRestaurantAutoComplete(restaurantEt, repo);

        if (menuId != -1) {
            loadMenuDataForEdit();
        } else {
            Glide.with(this).load(imageUri).centerCrop().into(imageView);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(v)
                .setPositiveButton("저장", null)
                .setNegativeButton("취소", null);

        builder.setTitle(menuId != -1 ? "메뉴 정보 수정" : "메뉴 정보 입력");

        dialog = builder.create();
        dialog.setOnShowListener(this::onDialogShow);

        return dialog;
    }

    private void initializeViews(View v) {
        imageView = v.findViewById(R.id.dialog_image);
        menuNameEt = v.findViewById(R.id.et_menu_name);
        restaurantEt = v.findViewById(R.id.et_restaurant);
        reviewEt = v.findViewById(R.id.review);
        priceEt = v.findViewById(R.id.price);
        ratingBar = v.findViewById(R.id.rating_bar);
    }

    private void onDialogShow(DialogInterface dialogInterface) {
        Button saveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

        TextWatcher textWatcher = new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int st, int bf, int cnt) { saveBtn.setEnabled(checkFields()); }
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
        };

        menuNameEt.addTextChangedListener(textWatcher);
        restaurantEt.addTextChangedListener(textWatcher);
        priceEt.addTextChangedListener(textWatcher);

        saveBtn.setEnabled(menuId != -1 || checkFields());

        saveBtn.setOnClickListener(v -> saveMenu());
    }

    private void loadMenuDataForEdit() {
        repo.getMenuById(menuId).observe(this, menu -> {
            if (menu == null) {
                Toast.makeText(getContext(), "메뉴 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }
            currentMenu = menu;
            imageUri = menu.imageUri;

            Glide.with(this).load(menu.imageUri).centerCrop().into(imageView);
            menuNameEt.setText(menu.menuName);
            reviewEt.setText(menu.review);
            priceEt.setText(String.valueOf(menu.price));
            ratingBar.setRating(menu.rating);

            menuNameEt.setEnabled(false);
            restaurantEt.setEnabled(false);

            repo.getRestaurantById(menu.restaurantId).observe(this, restaurant -> {
                if (restaurant != null) {
                    restaurantEt.setText(restaurant.name);
                    restaurantEt.clearFocus();
                }
            });
        });
    }

    private void saveMenu() {
        if (!checkFields()) return;

        if (menuId != -1) {
            updateExistingMenu();
            return;
        }

        String restName = restaurantEt.getText().toString().trim();
        repo.restaurantExists(restName, exists -> {
            if (exists) {
                Restaurant existingRestaurant = findExistingRestaurant(restName);
                if (existingRestaurant != null) {
                    completeMenuCreation(existingRestaurant);
                } else {
                    Log.e("AddMenu", "Error: " + restName);
                    Toast.makeText(getContext(), "오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } else {
                new AlertDialog.Builder(requireContext())
                        .setMessage("“" + restName + "”은(는) 등록되지 않은 가게입니다.\n새로 생성하시겠습니까?")
                        .setPositiveButton("예", (d, w) -> {
                            AddRestaurantDialogFragment newRestaurantDialog =
                                    AddRestaurantDialogFragment.newInstance(restName);
                            newRestaurantDialog.show(getParentFragmentManager(), "AddNewRestaurantDialog");
                        })
                        .setNegativeButton("아니오", null)
                        .show();
            }
        });
    }

    private void updateExistingMenu() {
        if (currentMenu == null) return;

        currentMenu.menuName = menuNameEt.getText().toString().trim();
        currentMenu.review = reviewEt.getText().toString().trim();
        currentMenu.price = Integer.parseInt(priceEt.getText().toString());
        currentMenu.rating = ratingBar.getRating();

        repo.updateMenu(currentMenu);
        if (updateListener != null) {
            updateListener.onUpdated(requireContext());
        }
        dismiss();
    }


    private void completeMenuCreation(Restaurant restaurant) {
        MenuItem m = new MenuItem();
        m.menuName = menuNameEt.getText().toString().trim();
        m.imageUri = imageUri;
        m.rating = ratingBar.getRating();
        m.review = reviewEt.getText().toString();
        m.price = Integer.parseInt(priceEt.getText().toString());
        m.restaurantId = restaurant.id;

        if (createListener != null) {
            createListener.onCreated(m, restaurant);
        }
        dismiss();
    }

    private void setupRestaurantAutoComplete(AutoCompleteTextView autoCompleteTextView, DBRepository repo) {
        repo.getAllRestaurants().observe(this, restaurants -> {
            if (restaurants != null) {
                this.allRestaurantsList = restaurants;
                List<String> restaurantNames = restaurants.stream()
                        .map(r -> r.name)
                        .collect(Collectors.toList());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        restaurantNames
                );
                autoCompleteTextView.setAdapter(adapter);
                autoCompleteTextView.setThreshold(1);
            }
        });
    }

    private boolean checkFields() {
        boolean isMenuEmpty = menuNameEt.getText().toString().trim().isEmpty();
        boolean isRestaurantEmpty = restaurantEt.getText().toString().trim().isEmpty();
        boolean isPriceEmpty = priceEt.getText().toString().trim().isEmpty();
        return !isMenuEmpty && !isRestaurantEmpty && !isPriceEmpty;
    }

    private Restaurant findExistingRestaurant(String name) {
        if (allRestaurantsList != null) {
            for (Restaurant r : allRestaurantsList) {
                if (r.name.equalsIgnoreCase(name.trim())) {
                    return r;
                }
            }
        }
        return null;
    }
}