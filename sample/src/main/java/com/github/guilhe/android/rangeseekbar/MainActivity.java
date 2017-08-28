package com.github.guilhe.android.rangeseekbar;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.github.guilhe.android.rangeseekbar.databinding.ActivityMainBinding;
import com.github.guilhe.rangeseekbar.SeekBarRangedView;

import java.util.Random;

/**
 * Created by gdelgado on 24/08/2017.
 */

public class MainActivity extends AppCompatActivity {

    private Random mRandom = new Random();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.activityMainTextView.setText("min: " + (int) binding.activityMainDRangeSeekBarView.getMinValue() + " max: " + (int) binding.activityMainDRangeSeekBarView.getMaxValue());

        binding.activityMainARangeSeekBarView.setBackgroundColor(Color.LTGRAY);
        binding.activityMainARangeSeekBarView.setProgressColorResource(android.R.color.holo_green_dark);

        binding.activityMainCRangeSeekBarView.setRounded(true);
        binding.activityMainCRangeSeekBarView.setBackgroundHeight(50);
        binding.activityMainCRangeSeekBarView.setProgressHeight(15);
        binding.activityMainCRangeSeekBarView.setSelectedMinValue(10);
        binding.activityMainCRangeSeekBarView.setSelectedMaxValue(55);

        binding.activityMainDRangeSeekBarView.setProgressColorResource(R.color.progress_bar_line);
        binding.activityMainDRangeSeekBarView.setOnSeekBarRangedChangeListener(new SeekBarRangedView.OnSeekBarRangedChangeListener() {
            @Override
            public void onChanged(SeekBarRangedView view, double minValue, double maxValue) {
                updateLayout(minValue, maxValue);
            }

            @Override
            public void onChanging(SeekBarRangedView view, double minValue, double maxValue) {
                updateLayout(minValue, maxValue);
            }

            private void updateLayout(double minValue, double maxValue) {
                binding.activityMainTextView.setText("min: " + (int) minValue + " max: " + (int) maxValue);
            }
        });
        binding.activityMainDRangeSeekBarView.setSelectedMinValue(25, true);
        binding.activityMainDRangeSeekBarView.setSelectedMaxValue(86, true, 2000);

        binding.activityMainAppCompatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float min = mRandom.nextInt((int) (binding.activityMainDRangeSeekBarView.getSelectedMaxValue() - binding.activityMainDRangeSeekBarView.getMinValue() + 1)) + binding.activityMainDRangeSeekBarView.getMinValue();
                float max = (float) (mRandom.nextInt((int) (binding.activityMainDRangeSeekBarView.getMaxValue() - binding.activityMainDRangeSeekBarView.getSelectedMinValue() + 1)) + binding.activityMainDRangeSeekBarView.getSelectedMinValue());
                binding.activityMainDRangeSeekBarView.setSelectedMinValue(min, true);
                binding.activityMainDRangeSeekBarView.setSelectedMaxValue(max, true, 2000);
            }
        });
    }
}