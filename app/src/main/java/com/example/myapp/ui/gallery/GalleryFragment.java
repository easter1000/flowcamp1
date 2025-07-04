package com.example.myapp.ui.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapp.R;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment implements AddMenuDialogFragment.OnMenuCreated {
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

        Spinner spinner = view.findViewById(R.id.spinner_filter);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.gallery_filter_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinner.setAdapter(spinnerAdapter);

        ImageButton btnAdd = view.findViewById(R.id.btn_add);
        ImageButton btnFilter = view.findViewById(R.id.btn_filter);
        ImageButton btnSort = view.findViewById(R.id.btn_sort);
        ImageButton btnToggle = view.findViewById(R.id.btn_toggle_view);

        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickMediaLauncher.launch(i);
        });
        btnFilter.setOnClickListener(v -> {

        });
        btnSort.setOnClickListener(v -> {

        });
        btnToggle.setOnClickListener(v -> {

        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setDropDownWidth(spinner.getWidth());
                spinner.setDropDownHorizontalOffset(0);
                spinner.setDropDownVerticalOffset(spinner.getHeight()+20);
            }
        });

        recyclerView = view.findViewById(R.id.recycler_gallery);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        adapter = new GalleryAdapter(requireContext(), this::onMenuClicked);
        recyclerView.setAdapter(adapter);

        viewModel.getMenuItems().observe(getViewLifecycleOwner(), adapter::setData);

        viewModel.loadImages();
    }

    public void onMenuClicked(MenuItem item) {
        MenuDetailBottomSheet.newInstance(item.id)
                .show(getChildFragmentManager(), "detail");
    }

    private ActivityResultLauncher<Intent> pickMediaLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    res -> {
                        if (res.getResultCode() == Activity.RESULT_OK && res.getData() != null) {
                            Uri uri = res.getData().getData();
                            if (uri != null) {
                                requireContext().getContentResolver().takePersistableUriPermission(
                                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                AddMenuDialogFragment.newInstance(uri, this)
                                        .show(getChildFragmentManager(), "addMenu");
                            }
                        }
                    });

    @Override
    public void onCreated(MenuItem menu, Restaurant rest) {
        if (rest == null) {
            viewModel.addMenu(menu);
        } else {
            viewModel.addRestaurantWithFirstMenu(rest, menu);
        }
    }

    public interface OnMenuClickListener { void onMenuClicked(MenuItem item); }

    private static class GalleryAdapter
            extends RecyclerView.Adapter<GalleryAdapter.ImageViewHolder> {

        private final Context context;
        private List<MenuItem> data;
        private final OnMenuClickListener listener;

        public GalleryAdapter(Context context, OnMenuClickListener l) {
            this.context = context;
            this.data = new ArrayList<>();
            listener = l;
        }

        public void setData(List<MenuItem> menus) {
            this.data = menus;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_gallery_image,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            Glide.with(context)
                    .load(data.get(position).imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imageView);
            holder.itemView.setOnClickListener(v -> listener.onMenuClicked(data.get(position)));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class ImageViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public ImageViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }
}
