package com.github.guilhe.sample;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import com.github.guilhe.android.rangeseekbar.R;
import com.github.guilhe.android.rangeseekbar.databinding.ActivityMainBinding;
import com.github.guilhe.views.SeekBarRangedView;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Random random = new Random();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.activityMainTextView.setText(String.format(Locale.getDefault(), "min: %2.0f max: %2.0f", binding.activityMainDRangeSeekBarView.getMinValue(), binding.activityMainDRangeSeekBarView.getMaxValue()));

        binding.activityMainARangeSeekBarView.setBackgroundColor(Color.LTGRAY);
        binding.activityMainARangeSeekBarView.setProgressColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));

        binding.activityMainStepRangeSeekBarView.setBackgroundColor(Color.LTGRAY);
        binding.activityMainStepRangeSeekBarView.setProgressColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        binding.activityMainStepRangeSeekBarView.enableProgressBySteps(true);
        binding.activityMainStepRangeSeekBarView.setProgressSteps(25, 50, 75);

        binding.activityMainCRangeSeekBarView.setRounded(true);
        binding.activityMainCRangeSeekBarView.setBackgroundHeight(50);
        binding.activityMainCRangeSeekBarView.setProgressHeight(15);
        binding.activityMainCRangeSeekBarView.setActionCallback(new SeekBarRangedView.SeekBarRangedChangeCallback() {
            @Override
            public void onChanged(float minValue, float maxValue) {
                updateLayout(minValue, maxValue);
            }

            @Override
            public void onChanging(float minValue, float maxValue) {
                updateLayout(minValue, maxValue);
            }

            private void updateLayout(float minValue, float maxValue) {
                binding.activitySeekbarCCurrentTextView.setText(String.format(Locale.getDefault(), "min: %2.0f max: %2.0f", minValue, maxValue));
            }
        });

        binding.activityUpdateButton.setOnClickListener(v -> {
            binding.activityMainCRangeSeekBarView.setMinValue(Float.parseFloat(binding.activityMainMinEditText.getText().toString()));
            binding.activityMainCRangeSeekBarView.setMaxValue(Float.parseFloat(binding.activityMainMaxEditText.getText().toString()));
            binding.activitySeekbarCMinTextView.setText(String.valueOf(binding.activityMainCRangeSeekBarView.getMinValue()));
            binding.activitySeekbarCMaxTextView.setText(String.valueOf(binding.activityMainCRangeSeekBarView.getMaxValue()));
        });
        String min = String.valueOf(binding.activityMainCRangeSeekBarView.getMinValue());
        String max = String.valueOf(binding.activityMainCRangeSeekBarView.getMaxValue());
        binding.activitySeekbarCCurrentTextView.setText(String.format(Locale.getDefault(), "min: %s max: %s", min, max));
        binding.activitySeekbarCMinTextView.setText(min);
        binding.activitySeekbarCMaxTextView.setText(max);


        binding.activityMainDRangeSeekBarView.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar_line));
        binding.activityMainDRangeSeekBarView.setActionCallback(new SeekBarRangedView.SeekBarRangedChangeCallback() {
            @Override
            public void onChanged(float minValue, float maxValue) {
                updateLayout(minValue, maxValue);
            }

            @Override
            public void onChanging(float minValue, float maxValue) {
                updateLayout(minValue, maxValue);
            }

            private void updateLayout(float minValue, float maxValue) {
                binding.activityMainTextView.setText(String.format(Locale.getDefault(), "min: %2.0f max: %2.0f", minValue, maxValue));
            }
        });
        binding.activityMainDRangeSeekBarView.setSelectedMinValue(25, true);
        binding.activityMainDRangeSeekBarView.setSelectedMaxValue(86, true, 2000);

        binding.activityAnimateButton.setOnClickListener(view -> {
            float min1 = random.nextInt((int) (binding.activityMainDRangeSeekBarView.getSelectedMaxValue() - binding.activityMainDRangeSeekBarView.getMinValue() + 1)) + binding.activityMainDRangeSeekBarView.getMinValue();
            float max1 = random.nextInt((int) (binding.activityMainDRangeSeekBarView.getMaxValue() - binding.activityMainDRangeSeekBarView.getSelectedMinValue() + 1)) + binding.activityMainDRangeSeekBarView.getSelectedMinValue();
            binding.activityMainDRangeSeekBarView.setSelectedMinValue(min1, true);
            binding.activityMainDRangeSeekBarView.setSelectedMaxValue(max1, true, 2000);
        });
    }
}