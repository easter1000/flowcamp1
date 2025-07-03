package com.example.myapp.ui.gallery;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;

import java.util.List;

public class GalleryFragment extends Fragment {
    private GalleryViewModel viewModel;
    private RecyclerView recyclerView;
    private GalleryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        Spinner spinnerFilter = view.findViewById(R.id.spinner_filter);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.gallery_filter_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerFilter.setAdapter(spinnerAdapter);

        ImageButton btnAdd = view.findViewById(R.id.btn_add);
        ImageButton btnFilter = view.findViewById(R.id.btn_filter);
        ImageButton btnSort = view.findViewById(R.id.btn_sort);
        ImageButton btnToggle = view.findViewById(R.id.btn_toggle_view);

        btnAdd.setOnClickListener(v -> {

        });
        btnFilter.setOnClickListener(v -> {

        });
        btnSort.setOnClickListener(v -> {

        });
        btnToggle.setOnClickListener(v -> {

        });

        recyclerView = view.findViewById(R.id.recycler_gallery);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        adapter = new GalleryAdapter();
        recyclerView.setAdapter(adapter);

        viewModel.getImageUris().observe(getViewLifecycleOwner(), uris -> {
            adapter.submitList(uris);
        });

        viewModel.loadImages();
    }


    private static class GalleryAdapter
            extends RecyclerView.Adapter<GalleryAdapter.ImageViewHolder> {

        private final List<Uri> items = new java.util.ArrayList<>();

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType
        ) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_gallery_image, parent, false);
            return new ImageViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(
                @NonNull ImageViewHolder holder, int position
        ) {
            Uri uri = items.get(position);
            if (uri != null) {
                holder.imageView.setImageURI(uri);
            } else {
                holder.imageView.setImageDrawable(null);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        void submitList(List<Uri> list) {
            items.clear();
            items.addAll(list);
            notifyDataSetChanged();
        }

        static class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ImageViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.img_photo);
            }
        }
    }
}
