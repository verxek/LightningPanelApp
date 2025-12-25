package com.example.lightningpanelapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    private String RPI_IP = "http://unknown:5000"; // будет изменён после поиска

    // Текущий цвет кисти (RGB)
    private int currentRed = 255;
    private int currentGreen = 0;
    private int currentBlue = 0;

    // Массив для хранения цвета каждого пикселя [y][x]
    private int[][] pixelColors = new int[16][16];

    {
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                pixelColors[y][x] = 0x00000000;
            }
        }
    }


    private View colorPreview;
    private SeekBar seekBarRed, seekBarGreen, seekBarBlue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Разрешаем сеть в главном потоке (ТОЛЬКО для теста!)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setupGrid();

        // RGB-пикер
        colorPreview = findViewById(R.id.colorPreview);
        seekBarRed = findViewById(R.id.seekBarRed);
        seekBarGreen = findViewById(R.id.seekBarGreen);
        seekBarBlue = findViewById(R.id.seekBarBlue);

        // Устанавливаем начальные значения ползунков
        seekBarRed.setProgress(currentRed);
        seekBarGreen.setProgress(currentGreen);
        seekBarBlue.setProgress(currentBlue);

        // Обновляем превью
        updateColorPreview();

        // Слушатели для ползунков
        seekBarRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentRed = progress;
                updateColorPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentGreen = progress;
                updateColorPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentBlue = progress;
                updateColorPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupGrid() {
        GridLayout grid = findViewById(R.id.gridLayout);
        int margin = 1;

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                View cell = new View(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.columnSpec = GridLayout.spec(y, 1, 1.0f);
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
            int argbColor = 0xFF000000 | (r << 16) | (g << 8) | b;

            String hexColor = String.format("0x%06X", argbColor & 0xFFFFFF);

            String urlString = RPI_IP + "/set/" + x + "/" + y + "/" + hexColor;
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

    // Обновляет цвет квадрата-превью
    private void updateColorPreview() {
        int color = 0xFF000000 | (currentRed << 16) | (currentGreen << 8) | currentBlue;
        colorPreview.setBackgroundColor(color);
    }

    public void onDiscoverClick(View view) {
        discoverRaspberryPi();
    }
    private AtomicReference<String> discoveredRpiIp = new AtomicReference<>(null);

    private void discoverRaspberryPi() {
        new Thread(() -> {
            String subnet = getLocalSubnet();
            int port = 5000;
            String foundIp = null;

            for (int i = 1; i <= 254; i++) {
                if (foundIp != null) break;

                String ip = subnet + i;
                try {
                    java.net.Socket socket = new java.net.Socket();
                    socket.connect(new java.net.InetSocketAddress(ip, port), 300);
                    socket.getOutputStream().write("PING\n".getBytes());
                    socket.getOutputStream().flush();

                    byte[] buffer = new byte[64];
                    int len = socket.getInputStream().read(buffer);
                    String response = new String(buffer, 0, len).trim();

                    if ("LIGHT_PANEL_OK".equals(response)) {
                        foundIp = ip;
                    }

                    socket.close();
                } catch (Exception e) {
                    // Не отвечает — продолжаем
                }
            }

            discoveredRpiIp.set(foundIp); // Сохраняем результат

            runOnUiThread(() -> {
                String ip = discoveredRpiIp.get();
                if (ip != null) {
                    Toast.makeText(MainActivity.this, "Найдено: " + ip, Toast.LENGTH_LONG).show();
                    RPI_IP = "http://" + ip + ":5000";
                } else {
                    Toast.makeText(MainActivity.this, "Устройство не найдено", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
    // Получаем свою сеть
    private String getLocalSubnet() {
        try {
            java.net.InetAddress local = java.net.InetAddress.getLocalHost();
            String hostAddress = local.getHostAddress();
            // Убираем последнюю часть
            return hostAddress.replaceAll("\\d+$", "");
        } catch (Exception e) {
            return "192.168.1.";
        }
    }
}