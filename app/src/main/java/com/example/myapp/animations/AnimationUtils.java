package com.example.myapp.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class AnimationUtils {

    public static void expand(final View v) {
        v.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();
        Log.d("AnimationUtils", "expand() called. Target height: " + targetHeight);

        v.getLayoutParams().height = 0;

        ValueAnimator va = ValueAnimator.ofInt(0, targetHeight);
        va.addUpdateListener(animation -> {
            v.getLayoutParams().height = (Integer) animation.getAnimatedValue();
            v.requestLayout();
        });

        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        });

        va.setDuration(300);
        va.start();
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        ValueAnimator va = ValueAnimator.ofInt(initialHeight, 0);
        va.addUpdateListener(animation -> {
            v.getLayoutParams().height = (Integer) animation.getAnimatedValue();
            v.requestLayout();
        });

        va.setDuration(300);
        va.start();
    }
}