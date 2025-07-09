package com.example.myapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.libraries.places.api.Places;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {

    private static final long MIN_SPLASH_TIME = 2500;
    private boolean isMinTimeElapsed = false;
    private boolean isInitializationComplete = false;
    private ProgressBar progressBar;
    private ImageView splashLogo;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                finishInitialization();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBar);
        splashLogo = findViewById(R.id.splash_logo);

        splashScreen.setKeepOnScreenCondition(()->{return false;});
        startLogoAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isMinTimeElapsed = true;
            proceedToMain();
        }, MIN_SPLASH_TIME);

        startLoadingTasks();
    }

    private void startLoadingTasks() {
        checkAndRequestLocationPermission();
    }

    private void startLogoAnimation() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        splashLogo.startAnimation(fadeIn);
    }

    private void finishInitialization() {
        Log.d("SplashActivity", "Initialization tasks finished.");
        initializePlacesAPI();
        isInitializationComplete = true;
        proceedToMain();
    }

    private synchronized void proceedToMain() {
        if (isMinTimeElapsed && isInitializationComplete) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        } else {
            Log.d("SplashActivity", "Conditions not met yet. MinTime: " + isMinTimeElapsed + ", InitComplete: " + isInitializationComplete);
        }
    }

    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("SplashActivity", "Permission already granted.");
            finishInitialization();
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
                    finishInitialization();
                })
                .setCancelable(false)
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
                    finishInitialization();
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    Toast.makeText(this, "권한이 없어 지도 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    finishInitialization();
                })
                .setCancelable(false)
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
            Log.e("SplashActivity", "Manifest에서 API 키를 읽는 중 오류 발생", e);
        }

        if (Objects.equals(apiKey, "-1") || apiKey == null) {
            Log.e("SplashActivity", "API 키가 설정되지 않았습니다.");
            return;
        }

        if (!Places.isInitialized()) {
            Places.initialize(this.getApplicationContext(), apiKey);
        }
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, null);
        Places.createClient(this);
        Log.d("SplashActivity", "Places API and Maps Initializer initialized.");
    }
}