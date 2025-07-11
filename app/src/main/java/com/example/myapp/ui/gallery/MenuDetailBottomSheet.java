package com.example.myapp.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.myapp.R;
import com.example.myapp.data.MenuItem;
import com.example.myapp.data.Restaurant;
import com.example.myapp.ui.DBRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

public class MenuDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_MENU_ID = "menu_id";
    private DBRepository repo;

    private ImageView mdImg;
    private TextView mdName, mdReview, mdRestName, mdLocation, mdPrice;
    private RatingBar mdRating;

    private long menuId = -1;

    public static MenuDetailBottomSheet newInstance(long menuId) {
        MenuDetailBottomSheet f = new MenuDetailBottomSheet();
        Bundle b = new Bundle();
        b.putLong(ARG_MENU_ID, menuId);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.menu_detail, container, false);

        mdImg = v.findViewById(R.id.md_img);
        mdName = v.findViewById(R.id.md_name);
        mdRating = v.findViewById(R.id.md_rating);
        mdReview = v.findViewById(R.id.md_review);
        mdRestName = v.findViewById(R.id.md_res);
        mdLocation = v.findViewById(R.id.md_location);
        mdPrice = v.findViewById(R.id.md_price);

        Button btnEdit = v.findViewById(R.id.btn_edit);
        Button btnDelete = v.findViewById(R.id.btn_delete);

        repo = new DBRepository(requireContext());
        menuId = (getArguments() != null) ? getArguments().getLong(ARG_MENU_ID, -1) : -1;

        if (menuId == -1) {
            Toast.makeText(getContext(), "메뉴 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            dismiss();
            return v;
        }

        btnEdit.setOnClickListener(v1 -> {
            AddMenuDialogFragment editDialog = AddMenuDialogFragment.newInstanceForEdit(menuId, (context) -> {
                Toast.makeText(context, "메뉴가 수정되었습니다.", Toast.LENGTH_SHORT).show();
            });
            editDialog.show(getParentFragmentManager(), "edit_menu_dialog");
            dismiss();
        });

        btnDelete.setOnClickListener(v2 -> {
            new AlertDialog.Builder(requireContext())
                    .setMessage("삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        repo.deleteMenuById(menuId);
                        Toast.makeText(getContext(), "메뉴가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        repo.getMenuById(menuId).observe(getViewLifecycleOwner(), menu -> {
            if (menu == null) {
                if (isVisible()) {
                    dismiss();
                }
                return;
            }
            bindMenu(menu);

            repo.getRestaurantById(menu.restaurantId)
                    .observe(getViewLifecycleOwner(), this::bindRestaurant);
        });

        return v;
    }

    private void bindMenu(@NonNull MenuItem m) {
        Glide.with(this)
                .load(m.imageUri)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(mdImg);
        mdName.setText(m.menuName);
        mdRating.setRating(m.rating);
        if(Objects.equals(m.review, "")) {
            mdReview.setText("평가가 없습니다.");
        } else {
            mdReview.setText(m.review);
        }
        mdPrice.setText(String.format("%d원", m.price));
    }

    private void bindRestaurant(@Nullable Restaurant r) {
        if (r == null) return;
        mdRestName.setText(r.name);
        mdLocation.setText(r.location);
    }
}