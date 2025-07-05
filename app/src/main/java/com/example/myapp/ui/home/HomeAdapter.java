package com.example.myapp.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapp.R;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;
import com.example.myapp.data.db.Converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.RestaurantViewHolder> {

    private final Context context;
    private List<Restaurant> restaurants;
    private static List<MenuItem> menuItems;
    private int currentlyOpened = RecyclerView.NO_POSITION;

    public HomeAdapter(Context context) {
        this.context = context;
        this.restaurants = new ArrayList<>();
    }

    public void setRestaurants(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
        notifyDataSetChanged();
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        HomeAdapter.menuItems = menuItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurants.get(position);
        holder.bind(restaurant, context, this, position);
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

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRestaurantName = itemView.findViewById(R.id.textViewPlaceName);
            textViewRestaurantType = itemView.findViewById(R.id.textViewPlaceCategory);
            textViewRestaurantRating = itemView.findViewById(R.id.textViewAverageRating);
            textViewRestaurantLocation = itemView.findViewById(R.id.textViewPlaceAddress);
            horizontalScrollViewImages = itemView.findViewById(R.id.horizontalScrollViewImages);
            linearLayoutImages = itemView.findViewById(R.id.linearLayoutImages);
        }

        public void bind(final Restaurant restaurant, final Context context, final HomeAdapter adapter, final int position) {

            List<MenuItem> restaurantMenus = menuItems.stream()
                    .filter(m -> m.restaurantId == restaurant.id)
                    .collect(Collectors.toList());

            float ratingSum = 0;
            int ratingCount = restaurantMenus.size();
            for (MenuItem m : restaurantMenus) {
                ratingSum += m.rating;
            }
            float averageRating = ratingSum / ratingCount;

            textViewRestaurantName.setText(restaurant.name);
            textViewRestaurantType.setText(Converters.fromCuisine(restaurant.cuisineType));
            textViewRestaurantRating.setText(String.format(Locale.ROOT, "⭐ %.1f", averageRating));
            textViewRestaurantLocation.setText(restaurant.location);

            final boolean isOpened = position == adapter.currentlyOpened;
            horizontalScrollViewImages.setVisibility(isOpened ? View.VISIBLE : View.GONE);

            if (isOpened) {
                populateImages(restaurantMenus, linearLayoutImages, context);
            } else {
                linearLayoutImages.removeAllViews();
            }

            // --- 아이템 클릭 리스너 설정 ---
            itemView.setOnClickListener(v -> {
                /*
                // 현재 아이템의 확장/축소 상태 토글
                boolean isVisible = horizontalScrollViewImages.getVisibility() == View.VISIBLE;
                horizontalScrollViewImages.setVisibility(isVisible ? View.GONE : View.VISIBLE);

                if (!isVisible) { // 펼쳐질 때 이미지 동적 추가
                    populateImages(place, linearLayoutImages, context);
                } else { // 숨겨질 때 이미지 제거 (선택적, 메모리 관리)
                    linearLayoutImages.removeAllViews();
                }
                */
                int previouslyOpened = adapter.currentlyOpened;

                if (isOpened) { // 이미 열려 있는 아이템을 클릭한 경우
                    adapter.currentlyOpened = RecyclerView.NO_POSITION;
                } else { // 새로운 아이템을 클릭하거나, 닫힌 아이템을 클릭한 경우
                    adapter.currentlyOpened = position;
                }

                if (adapter.currentlyOpened != previouslyOpened) {
                    adapter.notifyItemChanged(previouslyOpened); // 이전에 열려 있던 아이템 닫기
                }
                if (adapter.currentlyOpened != RecyclerView.NO_POSITION &&
                        adapter.currentlyOpened != previouslyOpened) { // 다른 아이템을 클릭한 경우
                    adapter.notifyItemChanged(adapter.currentlyOpened); // 클릭한 아이템 열기
                } else if (adapter.currentlyOpened == RecyclerView.NO_POSITION &&
                        previouslyOpened == position) { // 열려 있는 아이템을 클릭한 경우
                    adapter.notifyItemChanged(position);
                }

            });
            /*
            // 초기 상태에서는 이미지가 보이지 않도록 항상 설정 (클릭 시 로드)
            horizontalScrollViewImages.setVisibility(View.GONE);
            linearLayoutImages.removeAllViews();
            */
        }

        private void populateImages(List<MenuItem> restaurantMenus, LinearLayout imageContainer, Context context) {
            imageContainer.removeAllViews(); // 기존 이미지 제거

            for (int i = 0; i < restaurantMenus.size(); i++) {
                MenuItem menuItem = restaurantMenus.get(i);

                FrameLayout frameLayout = new FrameLayout(context);
                LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                        300, // 너비
                        300  // 높이
                );
                containerParams.setMargins(0, 0, 15, 0); // 오른쪽 마진
                frameLayout.setLayoutParams(containerParams);

                ImageView imageView = new ImageView(context);
                FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
                imageView.setLayoutParams(imageParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                /*
                imageView.setImageResource(R.drawable.ic_dashboard_black_24dp); // 임시 플레이스홀더
                frameLayout.addView(imageView);
                 */

                Glide.with(context)
                        .load(menuItem.imageUri)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_dashboard_black_24dp)
                        .into(imageView);
                frameLayout.addView(imageView);

                TextView indexTextView = new TextView(context);
                FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                textParams.setMargins(16, 16, 0, 0);
                indexTextView.setPadding(6, 6, 6, 6);

                indexTextView.setLayoutParams(textParams);
                indexTextView.setText(menuItem.menuName);
                indexTextView.setTextSize(12);
                indexTextView.setTextColor(Color.WHITE);
                indexTextView.setBackgroundColor(Color.argb(128, 0, 0, 0));

                frameLayout.addView(indexTextView);

                imageContainer.addView(frameLayout);
            }
        }
    }

}
