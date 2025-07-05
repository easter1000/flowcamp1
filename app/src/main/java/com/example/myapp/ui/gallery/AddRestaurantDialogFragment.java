package com.example.myapp.ui.gallery;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.myapp.R;
import com.example.myapp.data.CuisineType;
import com.example.myapp.data.Restaurant;
import com.example.myapp.ui.DBRepository;

public class AddRestaurantDialogFragment extends DialogFragment {

    private static final String ARG_RESTAURANT_NAME = "restaurantName";
    private EditText arName;
    private EditText arLocation;

    public static AddRestaurantDialogFragment newInstance(String restaurantName) {
        AddRestaurantDialogFragment fragment = new AddRestaurantDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RESTAURANT_NAME, restaurantName);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_restaurant, null);

        arName = view.findViewById(R.id.ar_restaurant);
        arLocation = view.findViewById(R.id.ar_location);
        Spinner spCuisineType = view.findViewById(R.id.spinner_cuisine_type);

        String restaurantName = getArguments().getString(ARG_RESTAURANT_NAME);
        arName.setText(restaurantName);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                CuisineType.getDisplayNames(false)
        );
        spCuisineType.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("메뉴 정보 입력")
                .setView(view)
                .setPositiveButton("저장", null)
                .setNegativeButton("취소", null)
                .create();

        dialog.setOnShowListener(dlg -> {
            Button saveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

            TextWatcher textWatcher = new TextWatcher() {
                @Override public void onTextChanged(CharSequence s, int st, int bf, int cnt) { saveBtn.setEnabled(checkFields(arName, arLocation)); }
                @Override public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
                @Override public void afterTextChanged(Editable s) {}
            };

            arName.addTextChangedListener(textWatcher);
            arLocation.addTextChangedListener(textWatcher);
            saveBtn.setEnabled(checkFields(arName, arLocation));

            saveBtn.setOnClickListener(v1 -> {
                String restName = arName.getText().toString().trim();
                String locName = arLocation.getText().toString().trim();

                if (!checkFields(arName, arLocation)) {
                    Toast.makeText(getContext(), "오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                int selectedPosition = spCuisineType.getSelectedItemPosition();
                CuisineType selectedType = CuisineType.values()[selectedPosition + 1];

                Restaurant newRestaurant = new Restaurant();
                newRestaurant.name = restName;
                newRestaurant.location = locName;
                newRestaurant.cuisineType = selectedType;

                DBRepository repository = new DBRepository(requireContext());
                repository.insertRestaurant(newRestaurant, id -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (id > -1) {
                                Toast.makeText(getContext(), "“" + restName + "”가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                                dismiss();
                            } else {
                                Toast.makeText(getContext(), "오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });
                    }
                });
            });
        });

        return dialog;
    }

    private boolean checkFields(EditText name, EditText loc) {
        boolean isNameEmpty = name.getText().toString().trim().isEmpty();
        boolean isLocationEmpty = loc.getText().toString().trim().isEmpty();
        return !isNameEmpty && !isLocationEmpty;
    }
}