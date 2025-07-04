package com.example.myapp.ui.gallery;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.myapp.R;
import com.example.myapp.data.CuisineType;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;

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

        return new AlertDialog.Builder(requireContext())
                .setTitle("메뉴 정보 입력")
                .setView(v)
                .setPositiveButton("저장", (d, w) -> {
                    MenuItem m = new MenuItem();
                    m.menuName = menu.getText().toString();
                    m.imageUri = imageUri;
                    m.rating = rating.getRating();
                    m.review = review.getText().toString();

                    String restName = restaurant.getText().toString().trim();
                    if (restName.isEmpty()) {
                        listener.onCreated(m, null);
                    } else {
                        Restaurant r = new Restaurant();
                        r.name = restName;
                        r.location = "";
                        r.cuisineType = CuisineType.OTHER;
                        listener.onCreated(m, r);
                    }
                })
                .setNegativeButton("취소", (d,w)->{})
                .create();
    }
}
