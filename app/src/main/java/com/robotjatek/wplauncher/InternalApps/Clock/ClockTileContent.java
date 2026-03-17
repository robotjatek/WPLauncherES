package com.robotjatek.wplauncher.InternalApps.Clock;

import android.content.Context;
import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.LocationService;
import com.robotjatek.wplauncher.TileGrid.ITileContent;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.Tile;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockTileContent implements ITileContent {
    private boolean _disposed = false;
    private long _lastUpdate = System.currentTimeMillis();
    private int _lastHour = -1;
    private int _lastMinute = -1;
    private String _location = "";
    private boolean _dirty = true;
    private final Context _context;
    private final LocationService _locationService;
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Label _clockLabel;
    private final Label _locationLabel;

    public ClockTileContent(Context context, LocationService locationService) {
        _context = context;
        _locationService = locationService;
        _clockLabel = new Label("", 160, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT);
        _locationLabel = new Label("", 72, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT);
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer, Tile tile, Position<Float> position, Size<Integer> size) {
        if (_dirty) {
            _layout.setBgColor(tile.bgColor);

            _layout.removeChild(_clockLabel);
            var padding = size.height() * 0.035f;
            var time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH mm"));
            var fontSize = tile.getSize().equals(Tile.SMALL) ? 80 : 160;
            _clockLabel.setText(time);
            _clockLabel.setTextSize(fontSize);
            _clockLabel.setMaxWidth(size.width() - padding * 2);
            var clockPosition = new Position<>(position.x() + padding, position.y() + size.height() / 3);
            _layout.addChild(_clockLabel, clockPosition);

            _layout.removeChild(_locationLabel);
            if (isLocationEnabled() &&
                    !tile.getSize().equals(Tile.SMALL)) {
                _locationLabel.setText(_location);
                _locationLabel.setMaxWidth(size.width() - padding * 2);
                var locationSize = _locationLabel.measure();
                var x = locationSize.width() + padding < size.width() - padding ?
                        (position.x() + size.width()) - locationSize.width() - padding : padding;
                var locationPosition = new Position<>(x, position.y() + padding); // Right aligned
                _layout.addChild(_locationLabel, locationPosition);
            }

            _dirty = false;
        }

        updateContent();
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
            } else {
                setLocation("");
            }

            _lastUpdate = System.currentTimeMillis();
        }
    }

    private void setLocation(String location) {
        _location = location;
        _dirty = true;
    }

    private boolean isLocationEnabled() {
        var prefs = _context.getSharedPreferences(Clock.PREF_NAME, Context.MODE_PRIVATE);
        var settingsJson = prefs.getString(Clock.SETTINGS, null);
        if (settingsJson == null) {
            return false;
        }
        try {
            var settings = new JSONObject(settingsJson);
            return settings.getBoolean("locationEnabled");
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
}
