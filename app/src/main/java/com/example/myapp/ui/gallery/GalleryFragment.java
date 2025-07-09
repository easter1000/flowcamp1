package com.example.myapp.ui.gallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapp.R;
import com.example.myapp.data.CuisineType;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;
import com.example.myapp.data.SortOrder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GalleryFragment extends Fragment implements AddMenuDialogFragment.OnMenuCreatedListener {
    private GalleryViewModel viewModel;
    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private CuisineType current = CuisineType.ALL;
    private int selectedSortIndex = 0;
    private View emptyHomeView;
    private Uri photoUri;
    private ActivityResultLauncher<Intent> pickFromGalleryLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerLaunchers();
    }
    private boolean isInfoView = false;

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
        emptyHomeView = view.findViewById(R.id.emptyHomeView);

        Spinner spinner = view.findViewById(R.id.spinner_filter);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                CuisineType.getDisplayNames(true)
        );
        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinner.setAdapter(spinnerAdapter);

        ImageButton btnAdd = view.findViewById(R.id.btn_add);
        ImageButton btnSort = view.findViewById(R.id.btn_sort);
        ImageButton btnToggle = view.findViewById(R.id.btn_toggle_view);

        btnAdd.setOnClickListener(v -> showImageSourceDialog());
        btnSort.setOnClickListener(v -> {
            final String[] items = {"최신순", "오래된순", "이름 A-Z", "이름 Z-A", "별점 높은순", "별점 낮은순", "가격 높은순", "가격 낮은순"};

            final CharSequence[] styledItems = new CharSequence[items.length];
            for (int i = 0; i < items.length; i++) {
                SpannableString s = new SpannableString(items[i]);
                if (i == selectedSortIndex) {
                    s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
                }
                styledItems[i] = s;
            }

            new AlertDialog.Builder(requireContext())
                    .setTitle("정렬 기준")
                    .setItems(styledItems, (dialog, which) -> {
                        selectedSortIndex = which;
                        SortOrder order;
                        switch (which) {
                            case 0: order = SortOrder.DATE_DESC; break;
                            case 1: order = SortOrder.DATE_ASC; break;
                            case 2: order = SortOrder.NAME_ASC; break;
                            case 3: order = SortOrder.NAME_DESC; break;
                            case 4: order = SortOrder.RATING_DESC; break;
                            case 5: order = SortOrder.RATING_ASC; break;
                            case 6: order = SortOrder.PRICE_DESC; break;
                            case 7: default: order = SortOrder.PRICE_ASC;
                        }
                        adapter.sort(order);
                    })
                    .show();
        });
        btnToggle.setOnClickListener(v -> {
            isInfoView = !isInfoView;

            if(isInfoView) {
                recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
            }
            adapter.setInfoViewMode(isInfoView);
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                current = CuisineType.values()[pos];
                viewModel.setFilter(current);
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
        if(isInfoView) {
            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        }
        adapter = new GalleryAdapter(requireContext(), this::onMenuClicked, isInfoView, viewModel, getViewLifecycleOwner());
        recyclerView.setAdapter(adapter);
        adapter.setInfoViewMode(isInfoView);

        viewModel.getMenuItems().observe(getViewLifecycleOwner(), menus -> {
            toggleVisibility();
            adapter.setData(menus);
            adapter.sort(getSortOrderFromIndex(selectedSortIndex));
        });

        viewModel.setFilter(current);

        toggleVisibility();
    }

    private SortOrder getSortOrderFromIndex(int index) {
        switch (index) {
            case 0: return SortOrder.DATE_DESC;
            case 1: return SortOrder.DATE_ASC;
            case 2: return SortOrder.NAME_ASC;
            case 3: return SortOrder.NAME_DESC;
            case 4: return SortOrder.RATING_DESC;
            case 5: return SortOrder.RATING_ASC;
            case 6: return SortOrder.PRICE_DESC;
            case 7: default: return SortOrder.PRICE_ASC;
        }
    }

    private void toggleVisibility() {
        List<MenuItem> list = viewModel.getMenuItems().getValue();
        if (list == null || list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyHomeView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyHomeView.setVisibility(View.GONE);
        }
    }

    public void onMenuClicked(MenuItem item) {
        MenuDetailBottomSheet.newInstance(item.id)
                .show(getChildFragmentManager(), "detail");
    }

    private void registerLaunchers() {
        pickFromGalleryLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        res -> {
                            if (res.getResultCode() == Activity.RESULT_OK && res.getData() != null) {
                                Uri uri = res.getData().getData();
                                if (uri != null) {
                                    requireContext().getContentResolver().takePersistableUriPermission(
                                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    showAddMenuDialog(uri);
                                }
                            }
                        });
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && photoUri != null) {
                        showAddMenuDialog(photoUri);
                    }
                }
        );
    }

    private void showImageSourceDialog() {
        final String[] options = {"카메라로 촬영", "갤러리에서 선택"};
        new AlertDialog.Builder(requireContext())
                .setTitle("사진 추가")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        launchCamera();
                    } else {
                        launchGallery();
                    }
                })
                .show();
    }

    private void launchGallery() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickFromGalleryLauncher.launch(i);
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.myapp.fileprovider",
                    photoFile
            );
            takePictureLauncher.launch(photoUri);
        } catch (IOException ex) {
            Toast.makeText(requireContext(), "이미지 파일을 생성하는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void showAddMenuDialog(Uri uri) {
        AddMenuDialogFragment.newInstanceForCreate(uri, this)
                .show(getChildFragmentManager(), "addMenu");
    }

    @Override
    public void onCreated(MenuItem menu, Restaurant rest) {
        viewModel.addMenu(menu);
    }

    public interface OnMenuClickListener { void onMenuClicked(MenuItem item); }

    private static class GalleryAdapter
            extends RecyclerView.Adapter<GalleryAdapter.ImageViewHolder> {

        private final Context context;
        private List<MenuItem> data;
        private final OnMenuClickListener listener;
        private SortOrder sortOrder = SortOrder.DATE_DESC;
        private boolean isInfoView;
        private final GalleryViewModel viewModel;
        private final LifecycleOwner lifecycleOwner;

        public GalleryAdapter(Context context, OnMenuClickListener l, boolean isInfoView, GalleryViewModel viewModel, LifecycleOwner lifecycleOwner) {
            this.context = context;
            this.data = new ArrayList<>();
            listener = l;
            this.isInfoView = isInfoView;
            this.viewModel = viewModel;
            this.lifecycleOwner = lifecycleOwner;
        }

        public void setData(List<MenuItem> menus) {
            this.data = menus;
        }

        public void sort(SortOrder order) {
            sortOrder = order;
            if (data == null) return;
            Comparator<MenuItem> comp;
            switch (order) {
                case NAME_ASC: comp = Comparator.comparing(m -> m.menuName.toLowerCase()); break;
                case NAME_DESC: comp = Comparator.comparing((MenuItem m) -> m.menuName.toLowerCase()).reversed(); break;
                case RATING_ASC: comp = Comparator.comparingDouble(m -> m.rating); break;
                case RATING_DESC:comp = Comparator.comparingDouble((MenuItem m) -> m.rating).reversed(); break;
                case PRICE_ASC: comp = Comparator.comparingInt(m -> m.price); break;
                case PRICE_DESC: comp = Comparator.comparingInt((MenuItem m) -> m.price).reversed(); break;
                case DATE_ASC: comp = Comparator.comparingLong(m -> m.id); break;
                default: comp = Comparator.comparingLong((MenuItem m) -> m.id).reversed();
            }
            Collections.sort(data, comp);
            notifyDataSetChanged();
        }

        public void setInfoViewMode(boolean isInfoView) {
            this.isInfoView = isInfoView;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_gallery_image,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            MenuItem menu = data.get(position);
            Glide.with(context)
                    .load(data.get(position).imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imageView);
            holder.itemView.setOnClickListener(v -> listener.onMenuClicked(data.get(position)));
            holder.ratingBar.setRating(data.get(position).rating);

            if (isInfoView) {
                holder.menuInfoContainer.setVisibility(View.VISIBLE);
                holder.ratingBar.setVisibility(View.GONE);
                holder.ratingBarLarge.setVisibility(View.VISIBLE);

                viewModel.getRestaurantById(menu.restaurantId).observe(lifecycleOwner, restaurant -> {
                    holder.textViewRestaurantName.setText(restaurant.name);
                });
            } else {
                holder.menuInfoContainer.setVisibility(View.GONE);
                holder.ratingBar.setVisibility(View.VISIBLE);
                holder.ratingBarLarge.setVisibility(View.GONE);
            }
            holder.textViewMenuName.setText(data.get(position).menuName);
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        public static class ImageViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public RatingBar ratingBar;
            public RatingBar ratingBarLarge;
            public LinearLayout menuInfoContainer;
            public TextView textViewMenuName;
            public TextView textViewRestaurantName;
            public TextView menuName;
            public ImageViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                ratingBar = itemView.findViewById(R.id.ratingBar);
                ratingBarLarge = itemView.findViewById(R.id.ratingBarLarge);
                menuInfoContainer = itemView.findViewById(R.id.menuInfoContainer);
                textViewMenuName = itemView.findViewById(R.id.textViewMenuName);
                textViewRestaurantName = itemView.findViewById(R.id.textViewRestaurantName);
                menuName = itemView.findViewById(R.id.menu_name);
            }
        }
    }
}
