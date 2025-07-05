package com.example.myapp.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.myapp.R;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;
import com.example.myapp.ui.DBRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MenuDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_MENU_ID = "menu_id";
    private DBRepository repo;

    private ImageView mdImg;
    private TextView mdName;
    private RatingBar mdRating;
    private TextView mdReview;
    private TextView mdRestName;
    private TextView mdLocation;

    public static MenuDetailBottomSheet newInstance(long menuId) {
        MenuDetailBottomSheet f = new MenuDetailBottomSheet();
        Bundle b = new Bundle();
        b.putLong(ARG_MENU_ID, menuId);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.menu_detail, container, false);

        mdImg = v.findViewById(R.id.md_img);
        mdName = v.findViewById(R.id.md_name);
        mdRating = v.findViewById(R.id.md_rating);
        mdReview = v.findViewById(R.id.md_review);
        mdRestName = v.findViewById(R.id.md_res);
        mdLocation = v.findViewById(R.id.md_location);

        repo = new DBRepository(requireContext());
        long menuId = (getArguments() != null) ? getArguments().getLong(ARG_MENU_ID, -1) : -1;

        if (menuId == -1) {
            dismiss();
            return v;
        }

        repo.getMenuById(menuId).observe(
                getViewLifecycleOwner(), menu -> {
                    if (menu == null) return;
                    bindMenu(menu);

                    repo.getRestaurantById(menu.restaurantId)
                            .observe(getViewLifecycleOwner(), this::bindRestaurant);
                });

        return v;
    }

    private void bindMenu(@NonNull MenuItem m) {
        Glide.with(this)
                .load(m.imageUri)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(mdImg);
        mdName.setText(m.menuName);
        mdRating.setRating(m.rating);
        mdReview.setText(m.review);
    }

    private void bindRestaurant(@Nullable Restaurant r) {
        if (r == null) return;
        mdRestName.setText(r.name);
        mdLocation.setText(r.location);
    }
}
