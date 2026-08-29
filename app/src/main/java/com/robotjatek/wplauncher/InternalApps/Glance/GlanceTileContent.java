package com.robotjatek.wplauncher.InternalApps.Glance;

import android.content.Context;
import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.LocationService;
import com.robotjatek.wplauncher.Services.WeatherService.IWeatherListener;
import com.robotjatek.wplauncher.Services.WeatherService.TemperatureUnit;
import com.robotjatek.wplauncher.Services.WeatherService.WeatherData;
import com.robotjatek.wplauncher.Services.WeatherService.WeatherService;
import com.robotjatek.wplauncher.TileGrid.ITileContent;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.Tile;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class GlanceTileContent implements ITileContent, IWeatherListener {
    private boolean _disposed = false;
    private long _lastUpdate = System.currentTimeMillis();
    private int _lastHour = -1;
    private int _lastMinute = -1;
    private WeatherData _weatherData = new WeatherData(-1, -1, TemperatureUnit.CELSIUS, false);
    private String _location = "";
    private boolean _dirty = true;
    private final Context _context;
    private final LocationService _locationService;
    private final WeatherService _weatherService;
    private boolean _subscribedToWeatherService = false;
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Label _clockLabel = new Label("", 160, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT);
    private final Label _locationLabel = new Label("", 72, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT);
    private final Label _temperatureLabel = new Label("", 60, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT);
    private final Label _weatherCodeLabel = new Label("", 60, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT);
    private boolean _locationLabelsPresent = false;
    private Tile _tile;

    public GlanceTileContent(Context context, LocationService locationService, WeatherService weatherService) {
        _context = context;
        _locationService = locationService;
        _weatherService = weatherService;
        _layout.addChild(_clockLabel, Position.ZERO);
    }

    @Override
    public void setParent(Tile parent) {
        _tile = parent;
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer, Position<Float> position, Size<Integer> size) {
        updateContent();
        if (_dirty) {
            _layout.setBgColor(_tile.bgColor);

            var padding = size.height() * 0.035f;
            var time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH mm"));
            var fontSize = _tile.getSize().equals(Tile.SMALL) ? 80 : 160;
            _clockLabel.setText(time);
            _clockLabel.setTextSize(fontSize);
            _clockLabel.setMaxWidth(size.width() - padding * 2);
            var clockPosition = new Position<>(position.x() + padding, position.y() + size.height() / 3);
            _layout.setChildPosition(_clockLabel, clockPosition);

            if (isLocationEnabled() &&
                    !_tile.getSize().equals(Tile.SMALL)) {
                // Location
                _locationLabel.setText(_location);
                _locationLabel.setMaxWidth(size.width() - padding * 2);
                var locationSize = _locationLabel.measure();
                var locX = size.width() - locationSize.width() - padding; // Right aligned
                var locationPosition = new Position<>(locX, position.y() + padding);
                _layout.setChildPosition(_locationLabel, locationPosition);

                // Temperature
                _temperatureLabel.setMaxWidth(size.width() - padding * 2);
                var temperatureSize = _temperatureLabel.measure();
                var temperatureX = size.width() - temperatureSize.width() - padding; // Right aligned
                var temperatureY  = locationPosition.y() + locationSize.height();
                _layout.setChildPosition(_temperatureLabel, new Position<>(temperatureX, temperatureY));

                // TODO: show weather icon on large tile

                // Weather code
                _weatherCodeLabel.setMaxWidth(size.width() - padding * 2);
                var codeSize = _weatherCodeLabel.measure();
                var codeX = (float)size.width() - codeSize.width() - padding;
                var codeY = size.height() - codeSize.height() - padding;
                _layout.setChildPosition(_weatherCodeLabel, new Position<>(codeX, codeY));
            }

            _dirty = false;
        }

        _layout.draw(delta, projMatrix, viewMatrix, renderer, position, size);
    }

    private void updateContent() {
        var elapsedTime = System.currentTimeMillis() - _lastUpdate;
        if (elapsedTime > 1000 || _dirty) {
            var currentTime = LocalTime.now();
            var h = currentTime.getHour();
            var m = currentTime.getMinute();

            if (_lastHour != h || _lastMinute != m || _dirty) {
                _lastHour = h;
                _lastMinute = m;
                _dirty = true;
            }

            if (isLocationEnabled()) {
                var currentLocation = _locationService.getCity();
                if (!_location.equals(currentLocation)) {
                    setLocation(currentLocation);
                }
                if (!_subscribedToWeatherService) {
                    _weatherService.subscribe(this);
                    _subscribedToWeatherService = true;
                }
                if (!_locationLabelsPresent) {
                    _layout.addChild(_locationLabel, Position.ZERO);
                    _layout.addChild(_temperatureLabel, Position.ZERO);
                    _layout.addChild(_weatherCodeLabel, Position.ZERO);
                    _locationLabelsPresent = true;
                }
            } else {
                setLocation("");
                _layout.removeChild(_locationLabel);
                _layout.removeChild(_temperatureLabel);
                _layout.removeChild(_weatherCodeLabel);
                _locationLabelsPresent = false;
                if (_subscribedToWeatherService) {
                    _weatherService.unsubscribe(this);
                    _subscribedToWeatherService = false;
                }
            }

            _lastUpdate = System.currentTimeMillis();
        }
    }

    private void setLocation(String location) {
        _location = location;
        _dirty = true;
    }

    private boolean isLocationEnabled() {
        var prefs = _context.getSharedPreferences(Glance.PREF_NAME, Context.MODE_PRIVATE);
        var settingsJson = prefs.getString(Glance.SETTINGS, null);
        if (settingsJson == null) {
            return false;
        }
        try {
            var settings = new JSONObject(settingsJson);
            return settings.getBoolean("locationEnabled") && !_tile.getSize().equals(Tile.SMALL);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            // Make sure that the labels are disposed. The layout only disposes children that are on it in the time of the disposal
            _clockLabel.dispose();
            _locationLabel.dispose();
            _temperatureLabel.dispose();
            _weatherCodeLabel.dispose();
            _weatherService.unsubscribe(this);
            _disposed = true;
        }
    }

    @Override
    public void forceRedraw() {
        _dirty = true;
    }

    @Override
    public boolean hasContent() {
        return true;
    }

    @Override
    public void onWeatherUpdate(WeatherData data) {
        if (data.temperature() == null) {
            _temperatureLabel.setText("");
            _weatherCodeLabel.setText("");
            _dirty = true;
            return;
        }

        if (!_weatherData.equals(data)) {
            _weatherData = data;
            _temperatureLabel.setText(getTemperatureString(data));
            _weatherCodeLabel.setText(data.getWeatherDescription());
            _dirty = true;
        }
    }

    private String getTemperatureString(WeatherData data) {
        if (data.temperature() == null) {
            return "";
        }

        if (data.unit() == TemperatureUnit.CELSIUS) {
            return String.format(Locale.US, "%d°C", data.temperature());
        } else {
            return String.format(Locale.US, "%d°F", data.temperature());
        }
    }
}
