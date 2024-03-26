package com.ecom.fyp2023.AppManagers;

import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.Analysis.CompletedProjectAnalysisBarChart;
import com.ecom.fyp2023.Analysis.CompletedProjectsAnalysis;
import com.ecom.fyp2023.Analysis.ProjectProgressAnalysis;

public class SwipeGestureListenerProjectAnalysis extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_THRESHOLD = 80;
    private static final int SWIPE_VELOCITY_THRESHOLD = 80;

    private final Context context;

    public SwipeGestureListenerProjectAnalysis(Context context) {
        this.context = context;
    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        float diffY = e2.getY() - e1.getY();
        float diffX = e2.getX() - e1.getX();

        if (Math.abs(diffX) > Math.abs(diffY)
                && Math.abs(diffX) > SWIPE_THRESHOLD
                && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            // Swipe detected, switch activities
            if (diffX > 0) {
                // Swipe right
                if (context instanceof ProjectProgressAnalysis) {
                    // In Activity A, go to Activity B
                    startActivityB();
                } else if (context instanceof CompletedProjectsAnalysis) {
                    // In Activity B, go to Activity C
                    startActivityC();
                } else if (context instanceof CompletedProjectAnalysisBarChart) {
                    // In Activity C, go to Activity A
                    startActivityA();
                }
            } else {
                // Swipe left
                if (context instanceof ProjectProgressAnalysis) {
                    // In Activity A, go to Activity C
                    startActivityC();
                } else if (context instanceof CompletedProjectsAnalysis) {
                    // In Activity B, go to Activity A
                    startActivityA();
                } else if (context instanceof CompletedProjectAnalysisBarChart) {
                    // In Activity C, go to Activity B
                    startActivityB();
                }
            }
            return true;
        }

        return false;
    }

    private void startActivityA() {
        Intent intent = new Intent(context, ProjectProgressAnalysis.class);
        context.startActivity(intent);
        ((AppCompatActivity) context).finish();
    }

    private void startActivityB() {
        Intent intent = new Intent(context, CompletedProjectsAnalysis.class);
        context.startActivity(intent);
        ((AppCompatActivity) context).finish();
    }

    private void startActivityC() {
        Intent intent = new Intent(context, CompletedProjectAnalysisBarChart.class);
        context.startActivity(intent);
        ((AppCompatActivity) context).finish();
    }
}
