package com.example.colorpicker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.example.colorpicker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
  private ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
    setContentView(binding.getRoot());

    binding.cpPicker.setOnColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
      @Override
      public void onColorSelected(final int color) {
        Toast.makeText(MainActivity.this, "got color: " + color, Toast.LENGTH_SHORT).show();
        binding.ivPicker.setBackgroundColor(color);

        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            binding.ivPicker.setBackgroundColor(color);
            binding.cpPicker.setColor(color);
          }
        }, 500);
      }
    });
  }
}
