package com.example.lightningpanelapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String RPI_IP = "http://192.168.1.100:5000"; //  IP Raspberry Pi

    // текущий цвет кисти (RGB)
    private int currentRed = 255;
    private int currentGreen = 0;
    private int currentBlue = 0;

    // Массив для хранения цвета каждого пикселя [y][x]
    private int[][] pixelColors = new int[16][16];

    {
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                pixelColors[y][x] = 0xFF000000; // Чёрный (ARGB)
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Разрешаем сеть в главном потоке (ТОЛЬКО для теста!)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setupGrid();
    }

    private void setupGrid() {
        GridLayout grid = findViewById(R.id.gridLayout);
        int margin = 1; // Отступ между ячейками

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                View cell = new View(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.columnSpec = GridLayout.spec(y, 1, 1.0f); // Важно: вес 1.0f
                params.rowSpec = GridLayout.spec(x, 1, 1.0f);
                params.setMargins(margin, margin, margin, margin);
                cell.setLayoutParams(params);
                cell.setBackgroundColor(pixelColors[y][x]);

                final int finalX = x;
                final int finalY = y;
                cell.setOnClickListener(v -> onPixelClick(finalX, finalY));

                grid.addView(cell);
            }
        }
    }

    private void onPixelClick(int x, int y) {
        int color = 0xFF000000 | (currentRed << 16) | (currentGreen << 8) | currentBlue;

        pixelColors[y][x] = color;

        View cell = getCellAt(x, y);
        if (cell != null) {
            cell.setBackgroundColor(color);
        }

        // Отправляем на Raspberry Pi
        sendToRaspberryPi(x, y, currentRed, currentGreen, currentBlue);
    }

    private View getCellAt(int x, int y) {
        GridLayout grid = findViewById(R.id.gridLayout);
        int index = y * 16 + x;
        if (index < grid.getChildCount()) {
            return grid.getChildAt(index);
        }
        return null;
    }

    private void sendToRaspberryPi(int x, int y, int r, int g, int b) {
        try {
            // Формируем URL: http://192.168.1.100:5000/set/x/y/r/g/b
            String urlString = RPI_IP + "/set/" + x + "/" + y + "/" + r + "/" + g + "/" + b;
            java.net.URL url = new java.net.URL(urlString);

            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                runOnUiThread(() -> Toast.makeText(this, "✓ (" + x + "," + y + ")", Toast.LENGTH_SHORT).show());
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Ошибка: " + responseCode, Toast.LENGTH_SHORT).show());
            }
            conn.disconnect();
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Нет связи с RPi", Toast.LENGTH_LONG).show());
            e.printStackTrace();
        }
    }

    public void selectRed(View view) {
        currentRed = 255;
        currentGreen = 0;
        currentBlue = 0;
        Toast.makeText(this, "Цвет:  Красный", Toast.LENGTH_SHORT).show();
    }

    public void selectBlue(View view) {
        currentRed = 0;
        currentGreen = 0;
        currentBlue = 255;
        Toast.makeText(this, "Цвет:  Синий", Toast.LENGTH_SHORT).show();
    }

    public void selectWhite(View view) {
        currentRed = 255;
        currentGreen = 255;
        currentBlue = 255;
        Toast.makeText(this, "Цвет:  Белый", Toast.LENGTH_SHORT).show();
    }
}