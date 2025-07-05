package com.example.myapp.ui.gallery;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.myapp.R;
import com.example.myapp.data.CuisineType;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;

import java.util.List;

public class AddMenuDialogFragment extends DialogFragment {

    public interface OnMenuCreated { void onCreated(MenuItem item, Restaurant restaurantOrNull); }

    private Uri imageUri;
    private OnMenuCreated listener;

    public static AddMenuDialogFragment newInstance(Uri img, OnMenuCreated l) {
        AddMenuDialogFragment f = new AddMenuDialogFragment();
        f.imageUri = img;
        f.listener = l;
        return f;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        var v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_menu, null);
        EditText menu = v.findViewById(R.id.et_menu_name);
        EditText restaurant = v.findViewById(R.id.et_restaurant);
        EditText review = v.findViewById(R.id.review);
        RatingBar rating = v.findViewById(R.id.rating_bar);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("메뉴 정보 입력")
                .setView(v)
                .setPositiveButton("저장", null)
                .setNegativeButton("취소", (d,w)->{})
                .create();
        dialog.setOnShowListener(dlg -> {
            Button saveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

            saveBtn.setEnabled(!menu.getText().toString().trim().isEmpty() && !restaurant.getText().toString().trim().isEmpty());

            menu.addTextChangedListener(new TextWatcher() {
                @Override public void onTextChanged(CharSequence s, int st, int bf, int cnt) {
                    saveBtn.setEnabled(!s.toString().trim().isEmpty() && !restaurant.getText().toString().trim().isEmpty());
                }
                @Override public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
                @Override public void afterTextChanged(Editable s) {}
            });

            restaurant.addTextChangedListener(new TextWatcher() {
                @Override public void onTextChanged(CharSequence s, int st, int bf, int cnt) {
                    saveBtn.setEnabled(!s.toString().trim().isEmpty() && !menu.getText().toString().trim().isEmpty());
                }
                @Override public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
                @Override public void afterTextChanged(Editable s) {}
            });

            saveBtn.setOnClickListener(v1 -> {

                String menuName = menu.getText().toString().trim();
                String restName = restaurant.getText().toString().trim();

                if (menuName.isEmpty() || restName.isEmpty()) return;

                Runnable createAndReturn = () -> {
                    MenuItem m = new MenuItem();
                    m.menuName = menuName;
                    m.imageUri = imageUri;
                    m.rating = rating.getRating();
                    m.review = review.getText().toString();

                    Restaurant r = findExistingRestaurant(restName);
                    if (r == null) {
                        r = new Restaurant();
                        r.name = restName;
                        r.location = "";
                        r.cuisineType = CuisineType.OTHER;
                    }
                    listener.onCreated(m, r);

                    dialog.dismiss();
                };

                new GalleryRepository(requireContext())
                        .restaurantExists(restName, exists -> {
                            if (exists) {
                                createAndReturn.run();
                            } else {
                                new AlertDialog.Builder(requireContext())
                                        .setMessage("“" + restName + "”은(는) 등록되지 않은 가게입니다.\n새로 생성하시겠습니까?")
                                        .setPositiveButton("예", (d2, w2) -> createAndReturn.run())
                                        .setNegativeButton("아니오", null)
                                        .show();
                            }
                        });
            });
        });

        return dialog;
    }

    private Restaurant findExistingRestaurant(String name) {
        GalleryRepository repo = new GalleryRepository(requireContext());
        List<Restaurant> restaurants = repo.getAllRestaurants().getValue();
        if (restaurants != null) {
            for (Restaurant r : restaurants) {
                Log.d("AddMenuDialogFragment", "Existing restaurant: " + r.name);
                if (r.name.equals(name)) return r;
            }
        }
        return null;
    }
}
