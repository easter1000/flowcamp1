package com.example.myapp.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;

import java.util.List;
import java.util.Locale;

public class DBViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_RESTAURANT = 0;
    private static final int TYPE_MENU = 1;

    private final List<Object> items;

    public DBViewAdapter(List<Object> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Restaurant) {
            return TYPE_RESTAURANT;
        } else {
            return TYPE_MENU;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_RESTAURANT) {
            View view = inflater.inflate(R.layout.item_db_restaurant, parent, false);
            return new RestaurantViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_db_menu, parent, false);
            return new MenuViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_RESTAURANT) {
            ((RestaurantViewHolder) holder).bind((Restaurant) items.get(position));
        } else {
            ((MenuViewHolder) holder).bind((MenuItem) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView details;

        RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_restaurant_name);
            details = itemView.findViewById(R.id.tv_restaurant_details);
        }

        void bind(Restaurant restaurant) {
            name.setText("R: " + restaurant.name);
            String detailText = String.format(Locale.getDefault(),
                    "id: %d, location: %s, type: %s",
                    restaurant.id, restaurant.location, restaurant.cuisineType.toString());
            details.setText(detailText);
        }
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView details;

        MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_menu_name);
            details = itemView.findViewById(R.id.tv_menu_details);
        }

        void bind(MenuItem menuItem) {
            name.setText("M: " + menuItem.menuName);
            String detailText = String.format(Locale.getDefault(),
                    "ID: %d, rID: %d, price: %d, rating: %.1f, review: %s",
                    menuItem.id, menuItem.restaurantId, menuItem.price, menuItem.rating, menuItem.review);
            details.setText(detailText);
        }
    }
}