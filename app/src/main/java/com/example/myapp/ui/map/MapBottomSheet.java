package com.example.myapp.ui.map;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.myapp.R;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;
import com.example.myapp.data.dao.RestaurantDao;
import com.example.myapp.data.db.Converters;
import com.example.myapp.ui.DBRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.Locale;

public class MapBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_RESTAURANT_ID = "restaurant_id";

    private TextView restaurantName;
    private TextView restaurantType;
    private TextView restaurantRating;
    private TextView restaurantLocation;
    HorizontalScrollView horizontalScrollViewImages;
    LinearLayout linearLayoutImages;

    public static MapBottomSheet newInstance(long restaurantId) {
        MapBottomSheet f = new MapBottomSheet();
        Bundle b = new Bundle();
        b.putLong(ARG_RESTAURANT_ID, restaurantId);
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
        View v = inflater.inflate(R.layout.item_place, container, false);

        restaurantName = v.findViewById(R.id.textViewPlaceName);
        restaurantType = v.findViewById(R.id.textViewPlaceCategory);
        restaurantRating = v.findViewById(R.id.textViewAverageRating);
        restaurantLocation = v.findViewById(R.id.textViewPlaceAddress);
        horizontalScrollViewImages = v.findViewById(R.id.horizontalScrollViewImages);
        linearLayoutImages = v.findViewById(R.id.linearLayoutImages);

        DBRepository repo = new DBRepository(requireContext());
        long restaurantId = (getArguments() != null) ? getArguments().getLong(ARG_RESTAURANT_ID, -1) : -1;

        if (restaurantId == -1) {
            Toast.makeText(getContext(), "맛집 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            dismiss();
            return v;
        }

        repo.getRestaurantWithMenus(restaurantId).observe(getViewLifecycleOwner(), restaurantWithMenus -> {
            if (restaurantWithMenus == null) {
                if (isVisible()) {
                    dismiss();
                }
                return;
            }
            bindRestaurantWithMenus(restaurantWithMenus);
        });

        return v;
    }

    private void bindRestaurantWithMenus(@NonNull RestaurantDao.RestaurantWithMenus r) {
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

        restaurantName.setText(restaurant.name);
        restaurantType.setText(Converters.fromCuisine(restaurant.cuisineType));
        restaurantLocation.setText(restaurant.location);
        horizontalScrollViewImages.setVisibility(View.VISIBLE);
        populateImages(menus, linearLayoutImages, requireContext());
    }

    private void populateImages(List<MenuItem> restaurantMenus, LinearLayout imageContainer, Context context) {
        imageContainer.removeAllViews();

        final int IMAGE_SIZE_DP = 120;
        final int MARGIN_RIGHT_DP = 8;

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

}