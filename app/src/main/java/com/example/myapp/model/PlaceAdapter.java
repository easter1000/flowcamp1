package com.example.myapp.model;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapp.R;
import java.util.List;
import java.util.ArrayList;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Place> places;
    private final Context context;
    private int currentlyExpandedPlace = RecyclerView.NO_POSITION;

    public PlaceAdapter(Context context, List<Place> places) {
        this.context = context;
        this.places = (places == null) ? new ArrayList<>() : places;
    }

    // ViewHolder: 각 아이템 뷰의 구성 요소를 보관
    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPlaceName;
        TextView textViewPlaceCategory;
        TextView textViewPlaceAddress;
        HorizontalScrollView horizontalScrollViewImages;
        LinearLayout linearLayoutImages;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPlaceName = itemView.findViewById(R.id.textViewPlaceName);
            textViewPlaceCategory = itemView.findViewById(R.id.textViewPlaceCategory);
            textViewPlaceAddress = itemView.findViewById(R.id.textViewPlaceAddress);
            horizontalScrollViewImages = itemView.findViewById(R.id.horizontalScrollViewImages);
            linearLayoutImages = itemView.findViewById(R.id.linearLayoutImages);
        }

        public void bind(final Place place, final Context context, final PlaceAdapter adapter, final int position) {
            textViewPlaceName.setText(place.getName());
            textViewPlaceCategory.setText(place.getCategory());
            textViewPlaceAddress.setText(place.getAddress());

            final boolean isExpanded = position == adapter.currentlyExpandedPlace;
            horizontalScrollViewImages.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            if (isExpanded) {
                populateImages(place, linearLayoutImages, context);
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
                int previouslyExpandedPlace = adapter.currentlyExpandedPlace;

                if (isExpanded) { // 이미 열려 있는 아이템을 클릭한 경우
                    adapter.currentlyExpandedPlace = RecyclerView.NO_POSITION;
                } else { // 새로운 아이템을 클릭하거나, 닫힌 아이템을 클릭한 경우
                    adapter.currentlyExpandedPlace = position;
                }

                if (adapter.currentlyExpandedPlace != previouslyExpandedPlace) {
                    adapter.notifyItemChanged(previouslyExpandedPlace); // 이전에 열려 있던 아이템 닫기
                }
                if (adapter.currentlyExpandedPlace != RecyclerView.NO_POSITION &&
                    adapter.currentlyExpandedPlace != previouslyExpandedPlace) { // 다른 아이템을 클릭한 경우
                    adapter.notifyItemChanged(adapter.currentlyExpandedPlace); // 클릭한 아이템 열기
                } else if (adapter.currentlyExpandedPlace == RecyclerView.NO_POSITION &&
                    previouslyExpandedPlace == position) { // 열려 있는 아이템을 클릭한 경우
                    adapter.notifyItemChanged(position);
                }

            });
            /*
            // 초기 상태에서는 이미지가 보이지 않도록 항상 설정 (클릭 시 로드)
            horizontalScrollViewImages.setVisibility(View.GONE);
            linearLayoutImages.removeAllViews();
            */
        }

        private void populateImages(Place place, LinearLayout imageContainer, Context context) {
            imageContainer.removeAllViews(); // 기존 이미지 제거

            for (int i = 0; i < place.getReviews(); i++) {
                // 임시로 플레이스홀더 이미지를 사용
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
                imageView.setImageResource(R.drawable.ic_dashboard_black_24dp); // 임시 플레이스홀더
                frameLayout.addView(imageView);

                TextView indexTextView = new TextView(context);
                FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                textParams.setMargins(16, 16, 0, 0);
                indexTextView.setPadding(6, 6, 6, 6);

                indexTextView.setLayoutParams(textParams);
                indexTextView.setText(String.valueOf(i + 1));
                indexTextView.setTextSize(12);
                indexTextView.setTextColor(Color.WHITE);
                indexTextView.setBackgroundColor(Color.argb(128, 0, 0, 0)); // 반투명 검은색 배경

                frameLayout.addView(indexTextView);

                imageContainer.addView(frameLayout);
            }
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
        //holder.bind(places.get(position));
        holder.bind(places.get(position), context, this, holder.getAbsoluteAdapterPosition());
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
