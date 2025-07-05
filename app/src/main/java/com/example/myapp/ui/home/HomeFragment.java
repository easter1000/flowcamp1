package com.example.myapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import com.example.myapp.databinding.FragmentHomeBinding;
import com.example.myapp.data.Restaurant;
import com.example.myapp.data.MenuItem;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeAdapter homeAdapter;
    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        /*
        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        */
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView(){
        // 어댑터 초기화 (처음에는 빈 리스트 또는 ViewModel의 초기값으로)
        homeAdapter = new HomeAdapter(requireContext());
        binding.recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewPlaces.setAdapter(homeAdapter);

        // 구분선
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(getContext(), MaterialDividerItemDecoration.VERTICAL);
        divider.setDividerInsetStart(32);
        divider.setDividerInsetEnd(32);
        divider.setLastItemDecorated(false);
        binding.recyclerViewPlaces.addItemDecoration(divider);
    }

    private void observeViewModel() {
        // HomeViewModel에서 장소 목록 LiveData를 관찰
        homeViewModel.getRestaurants().observe(getViewLifecycleOwner(), new Observer<List<Restaurant>>() {
            @Override
            public void onChanged(List<Restaurant> restaurants) {
                // LiveData가 변경될 때마다 어댑터의 데이터 업데이트
                if (homeAdapter != null && restaurants != null ) {
                    homeAdapter.setRestaurants(restaurants);
                }
            }
        });

        homeViewModel.getMenuItems().observe(getViewLifecycleOwner(), new Observer<List<MenuItem>>() {
            @Override
            public void onChanged(List<MenuItem> menuItems) {
                // LiveData가 변경될 때마다 어댑터의 데이터 업데이트
                if (homeAdapter != null && menuItems != null ) {
                    homeAdapter.setMenuItems(menuItems);
                }
            }
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}