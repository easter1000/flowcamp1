package com.example.myapp.ui.home;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapp.R;
import com.example.myapp.data.CuisineType;
import com.example.myapp.data.SortOrder;
import com.example.myapp.ui.gallery.AddRestaurantDialogFragment;
import com.example.myapp.ui.gallery.MenuDetailBottomSheet;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import com.example.myapp.databinding.FragmentHomeBinding;
import com.example.myapp.data.Restaurant;

public class HomeFragment extends Fragment implements HomeAdapter.OnImageClickListener, HomeAdapter.OnDeleteClickListener {

    private FragmentHomeBinding binding;
    private HomeAdapter homeAdapter;
    private HomeViewModel homeViewModel;
    private int selectedSortIndex = 7;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        observeViewModel();

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

        btnAdd.setOnClickListener(v -> {
            AddRestaurantDialogFragment dialogFragment = AddRestaurantDialogFragment.newInstance("");

            dialogFragment.show(getParentFragmentManager(), "AddRestaurantDialog");
        });
        btnSort.setOnClickListener(v -> {
            final String[] items = {"이름 A-Z", "이름 Z-A", "평점 높은순", "평점 낮은순", "가격 높은순", "가격 낮은순", "오래된순", "최신순"};

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
                            case 0: order = SortOrder.NAME_ASC; break;
                            case 1: order = SortOrder.NAME_DESC; break;
                            case 2: order = SortOrder.RATING_ASC; break;
                            case 3: order = SortOrder.RATING_DESC; break;
                            case 4: order = SortOrder.PRICE_ASC; break;
                            case 5: order = SortOrder.PRICE_DESC; break;
                            case 6: order = SortOrder.DATE_ASC; break;
                            case 7: default: order = SortOrder.DATE_DESC;
                        }
                        homeAdapter.sort(order);
                    })
                    .show();
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                CuisineType selectedType = CuisineType.values()[pos];
                homeViewModel.setFilter(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.post(() -> {
            spinner.setDropDownWidth(spinner.getWidth());
            spinner.setDropDownHorizontalOffset(0);
            spinner.setDropDownVerticalOffset(spinner.getHeight() + 20);
        });
    }

    private void setupRecyclerView(){
        homeAdapter = new HomeAdapter(requireContext());
        binding.recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewPlaces.setAdapter(homeAdapter);
        homeAdapter.setOnImageClickListener(this);
        homeAdapter.setOnDeleteClickListener(this);

        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(getContext(), MaterialDividerItemDecoration.VERTICAL);
        divider.setDividerInsetStart(32);
        divider.setDividerInsetEnd(32);
        divider.setLastItemDecorated(false);
        binding.recyclerViewPlaces.addItemDecoration(divider);
    }

    @Override
    public void onImageClick(long menuId) {
        MenuDetailBottomSheet bottomSheet = MenuDetailBottomSheet.newInstance(menuId);
        bottomSheet.show(getParentFragmentManager(), "MenuDetailBottomSheetTag");
    }

    @Override
    public void onDeleteClick(Restaurant restaurant) {
        Log.d("HomeFragment", "onDeleteClick received for: " + restaurant.name);
        new AlertDialog.Builder(requireContext())
                .setTitle("레스토랑 삭제")
                .setMessage("'" + restaurant.name + "'을(를) 정말 삭제하시겠습니까?\n관련된 모든 메뉴 정보도 함께 삭제됩니다.")
                .setPositiveButton("삭제", (dialog, which) -> {
                    homeViewModel.deleteRestaurant(restaurant);
                    Toast.makeText(getContext(), "'" + restaurant.name + "'이(가) 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void observeViewModel() {
        homeViewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            if (homeAdapter != null && restaurants != null) {
                homeAdapter.setRestaurants(restaurants);
            }
        });

        homeViewModel.getMenuItems().observe(getViewLifecycleOwner(), menuItems -> {
            if (homeAdapter != null && menuItems != null) {
                homeAdapter.setMenuItems(menuItems);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}