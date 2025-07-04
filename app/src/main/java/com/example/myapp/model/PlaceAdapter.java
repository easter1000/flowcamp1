package com.example.myapp.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapp.R;
import java.util.List;
import java.util.ArrayList;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Place> places;

    public PlaceAdapter(List<Place> places) {
        this.places = (places == null) ? new ArrayList<>() : places;
    }

    // ViewHolder: 각 아이템 뷰의 구성 요소를 보관
    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPlaceName;
        TextView textViewPlaceCategory;
        TextView textViewPlaceAddress;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPlaceName = itemView.findViewById(R.id.textViewPlaceName);
            textViewPlaceCategory = itemView.findViewById(R.id.textViewPlaceCategory);
            textViewPlaceAddress = itemView.findViewById(R.id.textViewPlaceAddress);
        }

        public void bind(final Place place) {
            textViewPlaceName.setText(place.getName());
            textViewPlaceCategory.setText(place.getCategory());
            textViewPlaceAddress.setText(place.getAddress());
        }
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false); // item_place.xml 인플레이트
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        holder.bind(places.get(position));
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public void updateData(List<Place> newPlaces) {
        this.places = (newPlaces == null) ? new ArrayList<>() : newPlaces;
        notifyDataSetChanged(); // RecyclerView에 데이터 변경 알림 (전체 새로고침)
    }
}
