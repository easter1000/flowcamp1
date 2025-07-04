package com.example.myapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapp.databinding.FragmentHomeBinding;
import com.example.myapp.model.Place;
import com.example.myapp.model.PlaceAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PlaceAdapter placeAdapter;
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
        placeAdapter = new PlaceAdapter(new ArrayList<>());
        binding.recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewPlaces.setAdapter(placeAdapter);
    }

    private void observeViewModel() {
        // HomeViewModel에서 장소 목록 LiveData를 관찰
        homeViewModel.getPlaces().observe(getViewLifecycleOwner(), new Observer<List<Place>>() {
            @Override
            public void onChanged(List<Place> places) {
                // LiveData가 변경될 때마다 어댑터의 데이터 업데이트
                if (placeAdapter != null && places != null ) {
                    placeAdapter.updateData(places);
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