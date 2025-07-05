package com.example.myapp.ui.gallery;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

    public interface OnMenuCreated { void onCreated(MenuItem item, Restaurant restaurantOrNull); }

    private Uri imageUri;
    private OnMenuCreated listener;

    private EditText menu, review, price;
    private AutoCompleteTextView restaurant;
    private RatingBar rating;
    private AlertDialog dialog;
    private List<Restaurant> allRestaurantsList;

    public static AddMenuDialogFragment newInstance(Uri img, OnMenuCreated l) {
        AddMenuDialogFragment f = new AddMenuDialogFragment();
        f.imageUri = img;
        f.listener = l;
        return f;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        var v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_menu, null);
        ImageView img = v.findViewById(R.id.dialog_image);
        menu = v.findViewById(R.id.et_menu_name);
        restaurant = v.findViewById(R.id.et_restaurant);
        review = v.findViewById(R.id.review);
        price = v.findViewById(R.id.price);
        rating = v.findViewById(R.id.rating_bar);

        DBRepository repo = new DBRepository(requireContext());
        setupRestaurantAutoComplete(restaurant, repo);

        Glide.with(requireContext())
                .load(imageUri)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(img);

        dialog = new AlertDialog.Builder(requireContext())
                .setTitle("메뉴 정보 입력")
                .setView(v)
                .setPositiveButton("저장", null)
                .setNegativeButton("취소", null)
                .create();
        dialog.setOnShowListener(dlg -> {
            Button saveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

            TextWatcher textWatcher = new TextWatcher() {
                @Override public void onTextChanged(CharSequence s, int st, int bf, int cnt) { saveBtn.setEnabled(checkFields(menu, restaurant, price)); }
                @Override public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
                @Override public void afterTextChanged(Editable s) {}
            };

            menu.addTextChangedListener(textWatcher);
            restaurant.addTextChangedListener(textWatcher);
            price.addTextChangedListener(textWatcher);
            saveBtn.setEnabled(checkFields(menu, restaurant, price));

            saveBtn.setOnClickListener(v1 -> {
                String restName = restaurant.getText().toString().trim();

                if (!checkFields(menu, restaurant, price)) {
                    Toast.makeText(getContext(), "오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                new DBRepository(requireContext())
                        .restaurantExists(restName, exists -> {
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
                                        .setPositiveButton("예", (dialog, which) -> {
                                            AddRestaurantDialogFragment newRestaurantDialog =
                                                    AddRestaurantDialogFragment.newInstance(restName);
                                            newRestaurantDialog.show(getParentFragmentManager(), "AddNewRestaurantDialog");
                                        })
                                        .setNegativeButton("아니오", null)
                                        .show();
                            }
                });
            });
        });

        return dialog;
    }

    private void completeMenuCreation(Restaurant restaurant) {
        String menuName = menu.getText().toString().trim();
        String r = review.getText().toString();
        int p = Integer.parseInt(price.getText().toString());

        MenuItem m = new MenuItem();
        m.menuName = menuName;
        m.imageUri = imageUri;
        m.rating = rating.getRating();
        m.review = r;
        m.price = p;
        m.restaurantId = restaurant.id;

        listener.onCreated(m, restaurant);
        dialog.dismiss();
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

    private boolean checkFields(EditText menu, EditText restaurant, EditText price) {
        boolean isMenuEmpty = menu.getText().toString().trim().isEmpty();
        boolean isRestaurantEmpty = restaurant.getText().toString().trim().isEmpty();
        boolean isPriceEmpty = price.getText().toString().trim().isEmpty();
        return !isMenuEmpty && !isRestaurantEmpty && !isPriceEmpty;
    }

    private Restaurant findExistingRestaurant(String name) {
        if (allRestaurantsList != null) {
            for (Restaurant r : allRestaurantsList) {
                if (r.name.equals(name)) {
                    return r;
                }
            }
        }
        return null;
    }
}
