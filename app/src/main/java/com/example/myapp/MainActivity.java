package com.example.myapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapp.databinding.ActivityMainBinding;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    showPermissionGuidanceDialog();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_map)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        checkAndRequestLocationPermission();
        initializePlacesAPI();
    }

    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionRationaleDialog();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
                .setTitle("위치 권한 필요")
                .setMessage("지도를 표시하고 현재 위치를 찾으려면 위치 정보 접근 권한이 반드시 필요합니다.")
                .setPositiveButton("권한 허용", (dialog, which) -> {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                })
                .setNegativeButton("거부", (dialog, which) -> {
                    Toast.makeText(this, "권한이 거부되어 지도 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                })
                .create()
                .show();
    }

    private void showPermissionGuidanceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("권한 설정 안내")
                .setMessage("위치 기능 사용을 위해 '앱 설정'에서 위치 권한을 항상 허용해주세요.")
                .setPositiveButton("설정으로 이동", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    Toast.makeText(this, "권한이 없어 지도 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                })
                .create()
                .show();
    }

    private void initializePlacesAPI() {
        String apiKey = "-1";
        try {
            ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(
                    this.getPackageName(),
                    PackageManager.GET_META_DATA
            );
            if (appInfo.metaData != null) {
                apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("MainActivity", "Manifest에서 API 키를 읽는 중 오류 발생", e);
        }

        if (Objects.equals(apiKey, "-1")) {
            Log.e("MainActivity", "API 키가 설정되지 않았습니다.");
            return;
        }

        if (!Places.isInitialized()) {
            assert apiKey != null;
            Places.initialize(this.getApplicationContext(), apiKey);
        }
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, null);
        Places.createClient(this);
    }
}