package com.robotjatek.wplauncher.Services.WeatherService;

import android.util.Log;

import androidx.core.text.util.LocalePreferences;

import com.robotjatek.wplauncher.Services.LocationService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherService {
    private final LocationService _locationService;
    private final List<IWeatherListener> _listeners = new ArrayList<>();
    private boolean _started = false;
    private final ScheduledExecutorService _executorService = Executors.newSingleThreadScheduledExecutor();
    private static final String URL_TEMPLATE = "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=is_day,temperature_2m,weather_code&temperature_unit=%s";
    public WeatherService(LocationService locationService) {
        _locationService = locationService;
    }

    public void start() {
        _executorService.scheduleWithFixedDelay(this::queryTemperature, 1, 10, TimeUnit.MINUTES);
    }

    public void stop() {
        _executorService.shutdownNow();
    }

    public void subscribe(IWeatherListener listener) {
        _listeners.add(listener);
        if (!_started) {
            start();
            _started = true;
        }
    }

    public void unsubscribe(IWeatherListener listener) {
        _listeners.remove(listener);
        if (!_started && _listeners.isEmpty()) {
            stop();
            _started = false;
        }
    }

    private void queryTemperature() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            var location = _locationService.getLocation();
            if (location == null) {
                return;
            }

            final var url = new URL(String.format(Locale.US, URL_TEMPLATE, location.getLatitude(), location.getLongitude(), getTemperatureUnit()));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            var responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                var resultJson = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    resultJson.append(line);
                }

                var jsonObject = new JSONObject(resultJson.toString());
                var current = jsonObject.getJSONObject("current");
                var isDay = current.getInt("is_day") == 1;
                var temperature = (int) current.getDouble("temperature_2m");
                var weatherCode = current.getInt("weather_code");
                var unit = LocalePreferences.getTemperatureUnit().equals(LocalePreferences.TemperatureUnit.CELSIUS) ? TemperatureUnit.CELSIUS : TemperatureUnit.FAHRENHEIT;
                _listeners.forEach(_listener -> _listener.onWeatherUpdate(new WeatherData(temperature, weatherCode, unit, isDay)));
            }
        } catch (Exception e) {
            Log.e(WeatherService.class.getName(), "Failed to query weather", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {}
            }
        }
    }

    private String getTemperatureUnit() {
        return LocalePreferences.getTemperatureUnit().equals(LocalePreferences.TemperatureUnit.CELSIUS) ? "celsius" : "fahrenheit";
    }
}
