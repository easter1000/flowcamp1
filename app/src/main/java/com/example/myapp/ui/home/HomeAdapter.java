package com.example.myapp.ui.home;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapp.R;
import com.example.myapp.animations.AnimationUtils;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;
import com.example.myapp.data.SortOrder;
import com.example.myapp.data.db.Converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.RestaurantViewHolder> {

    private final Context context;
    private List<Restaurant> restaurants;
    private List<MenuItem> menuItems;
    private final Set<Long> openedItems = new HashSet<>();
    private SortOrder sortOrder = SortOrder.DATE_DESC;
    private Location currentLocation;

    public HomeAdapter(Context context) {
        this.context = context;
        this.restaurants = new ArrayList<>();
        setHasStableIds(true);
    }

    public void setCurrentLocation(@Nullable Location loc) {
        this.currentLocation = loc;
        notifyDataSetChanged();          // 거리 갱신
    }

    @Override
    public long getItemId(int position) {
        return restaurants.get(position).id;
    }

    public void setRestaurants(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
        Set<Long> existingIds = restaurants.stream().map(r -> r.id).collect(Collectors.toSet());
        openedItems.retainAll(existingIds);
        sort(sortOrder);
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
        sort(sortOrder);
    }

    public void sort(SortOrder order) {
        this.sortOrder = order;
        if (restaurants == null) return;

        Comparator<Restaurant> comp;
        switch (order) {
            case NAME_ASC: comp = Comparator.comparing(r -> r.name.toLowerCase()); break;
            case NAME_DESC: comp = Comparator.comparing((Restaurant r) -> r.name.toLowerCase()).reversed(); break;
            case DISTANCE_ASC: comp = Comparator.comparingDouble(this::distance); break;
            case RATING_ASC: comp = Comparator.comparingDouble(this::avgRating); break;
            case RATING_DESC: comp = Comparator.comparingDouble(this::avgRating).reversed(); break;
            case DATE_ASC: comp = Comparator.comparingLong(r -> r.id); break;
            case DATE_DESC: default: comp = Comparator.comparingLong((Restaurant r) -> r.id).reversed();
        }
        Collections.sort(restaurants, comp);
        notifyDataSetChanged();
    }

    private double distance(Restaurant r) {
        // TODO: home 주소가 너무 긴것도!
        if (currentLocation == null) return 0;
        float[] results = new float[1];
        Location.distanceBetween(currentLocation.getLatitude(),  currentLocation.getLongitude(), r.latitude, r.longitude, results);
        return results[0];
    }

    private double avgRating(Restaurant r) {
        if (menuItems == null) return 0;
        double sum = 0; int cnt = 0;
        for (MenuItem m : menuItems) if (m.restaurantId == r.id) { sum += m.rating; cnt++; }
        return cnt == 0 ? 0 : sum / cnt;
    }

    public interface OnImageClickListener {
        void onImageClick(long menuId);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Restaurant restaurant);
    }

    public interface OnEditClickListener {
        void onEditClick(Restaurant restaurant);
    }

    private OnImageClickListener imageClickListener;
    private OnDeleteClickListener deleteClickListener;
    private OnEditClickListener editClickListener;

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.imageClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurants.get(position);
        holder.bind(restaurant, context, this, menuItems);
    }

    @Override
    public int getItemCount() {
        return restaurants == null ? 0 : restaurants.size();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRestaurantName;
        TextView textViewRestaurantType;
        TextView textViewRestaurantRating;
        TextView textViewRestaurantLocation;
        HorizontalScrollView horizontalScrollViewImages;
        LinearLayout linearLayoutImages;
        Button delBtn, editBtn;
        LinearLayout buttonContainer, expandableContainer;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRestaurantName = itemView.findViewById(R.id.textViewPlaceName);
            textViewRestaurantType = itemView.findViewById(R.id.textViewPlaceCategory);
            textViewRestaurantRating = itemView.findViewById(R.id.textViewAverageRating);
            textViewRestaurantLocation = itemView.findViewById(R.id.textViewPlaceAddress);
            horizontalScrollViewImages = itemView.findViewById(R.id.horizontalScrollViewImages);
            linearLayoutImages = itemView.findViewById(R.id.linearLayoutImages);
            expandableContainer = itemView.findViewById(R.id.expandableContainer);
            delBtn = itemView.findViewById(R.id.btnDel);
            editBtn = itemView.findViewById(R.id.btnEdit);
            buttonContainer = itemView.findViewById(R.id.buttonContainer);
        }

        public void bind(final Restaurant restaurant, final Context context, final HomeAdapter adapter, final List<MenuItem> menuItems) {
            List<MenuItem> restaurantMenus;
            if (menuItems != null) {
                restaurantMenus = menuItems.stream()
                        .filter(m -> m.restaurantId == restaurant.id)
                        .collect(Collectors.toList());
            } else {
                restaurantMenus = new ArrayList<>();
            }

            float ratingSum = 0;
            int ratingCount = restaurantMenus.size();
            for (MenuItem m : restaurantMenus) {
                ratingSum += m.rating;
            }
            float averageRating;
            if (ratingCount > 0) {
                averageRating = ratingSum / ratingCount;
                textViewRestaurantRating.setText(String.format(Locale.ROOT, "⭐ %.1f", averageRating));
            } else {
                textViewRestaurantRating.setText("⭐ N/A");
            }

            textViewRestaurantName.setText(restaurant.name);
            textViewRestaurantType.setText(Converters.fromCuisine(restaurant.cuisineType));
            textViewRestaurantLocation.setText(restaurant.location);

            final int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            final boolean isOpened = adapter.openedItems.contains(restaurant.id);

            if (isOpened) {
                expandableContainer.setVisibility(View.VISIBLE);
                populateImages(restaurantMenus, linearLayoutImages, context, adapter);
            } else {
                expandableContainer.setVisibility(View.GONE);
                linearLayoutImages.removeAllViews();
            }

            itemView.setOnClickListener(v -> {
                if (getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;

                if (adapter.openedItems.contains(restaurant.id)) {
                    AnimationUtils.collapse(expandableContainer);
                    adapter.openedItems.remove(restaurant.id);
                } else {
                    populateImages(restaurantMenus, linearLayoutImages, context, adapter);
                    AnimationUtils.expand(expandableContainer);
                    adapter.openedItems.add(restaurant.id);
                }
            });

            delBtn.setOnClickListener(v -> {adapter.deleteClickListener.onDeleteClick(restaurant);});
            editBtn.setOnClickListener(v -> {adapter.editClickListener.onEditClick(restaurant);});
        }

        private void populateImages(List<MenuItem> restaurantMenus, LinearLayout imageContainer, Context context, HomeAdapter adapter) {
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

                itemView.setOnClickListener(v -> {
                    if (adapter.imageClickListener != null) {
                        adapter.imageClickListener.onImageClick(menuItem.id);
                    }
                });

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
}