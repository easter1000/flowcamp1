package com.example.myapp.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapp.R;
import com.example.myapp.data.CuisineType;
import com.example.myapp.data.SortOrder;
import com.example.myapp.ui.gallery.AddRestaurantDialogFragment;
import com.example.myapp.ui.gallery.MenuDetailBottomSheet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import com.example.myapp.databinding.FragmentHomeBinding;
import com.example.myapp.data.Restaurant;

public class HomeFragment extends Fragment implements
        HomeAdapter.OnImageClickListener,
        HomeAdapter.OnDeleteClickListener,
        HomeAdapter.OnEditClickListener
{

    private FragmentHomeBinding binding;
    private HomeAdapter homeAdapter;
    private HomeViewModel homeViewModel;
    private int selectedSortIndex = 0;
    private FusedLocationProviderClient fused;
    private ActivityResultLauncher<String> requestPerm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fused = LocationServices.getFusedLocationProviderClient(requireActivity());

        requestPerm = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) fetchLocation();
                });
    }

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
            AddRestaurantDialogFragment dialogFragment = AddRestaurantDialogFragment.newInstanceForCreate("");
            dialogFragment.show(getParentFragmentManager(), "AddRestaurantDialog");
        });
        btnSort.setOnClickListener(v -> {
            final String[] items = {"최신순", "오래된순", "가까운순", "이름 A-Z", "이름 Z-A", "별점 높은순", "별점 낮은순"};

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
                            case 2: order = SortOrder.DISTANCE_ASC; break;
                            case 3: order = SortOrder.NAME_ASC; break;
                            case 4: order = SortOrder.NAME_DESC; break;
                            case 5: order = SortOrder.RATING_DESC; break;
                            case 6: default: order = SortOrder.RATING_ASC;
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

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            requestPerm.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void fetchLocation() {
        fused.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null && homeAdapter != null) homeAdapter.setCurrentLocation(loc);
        });
    }

    private void setupRecyclerView(){
        homeAdapter = new HomeAdapter(requireContext());
        binding.recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewPlaces.setAdapter(homeAdapter);
        homeAdapter.setOnImageClickListener(this);
        homeAdapter.setOnDeleteClickListener(this);
        homeAdapter.setOnEditClickListener(this);

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

    @Override
    public void onEditClick(Restaurant restaurant) {
        AddRestaurantDialogFragment dialogFragment = AddRestaurantDialogFragment.newInstanceForEdit(restaurant.id);
        dialogFragment.show(getParentFragmentManager(), "EditRestaurantDialog");
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