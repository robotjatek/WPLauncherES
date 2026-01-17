package com.robotjatek.wplauncher.InternalApps.Clock;

import android.content.Context;
import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.HorizontalAlign;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.Services.LocationService;
import com.robotjatek.wplauncher.TileGrid.ITileContent;
import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileUtil;
import com.robotjatek.wplauncher.VerticalAlign;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockTileContent implements ITileContent {
    private final float[] _modelMatrix = new float[16];
    private int _clockTexture;
    private int _locationTexture;
    private int _bgTexture;
    private long _lastUpdate = System.currentTimeMillis();
    private int _lastHour = -1;
    private int _lastMinute = -1;
    private String _location = "";
    private boolean _dirty = true;
    private final Context _context;

    public ClockTileContent(Context context) {
        _context = context;
    }

    @Override
    public void draw(float[] projMatrix, float[] viewMatrix, IDrawContext<Tile> drawContext,
                     Tile tile, float x, float y, float width, float height) {
        if (_dirty) {
            redraw(tile, width, height);
        }

        drawTexture(projMatrix, viewMatrix, drawContext, x, y, width, height, _bgTexture);
        updateContent();

        if (_clockTexture > 0) {
            drawTexture(projMatrix, viewMatrix, drawContext, x, y + 30, width, height, _clockTexture);
        }

        if (_locationTexture > 0 && isLocationEnabled()) {
            drawTexture(projMatrix, viewMatrix, drawContext, x, y, width, height, _locationTexture);
        }
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
                var currentLocation = LocationService.get().getCity();
                if (!_location.equals(currentLocation)) {
                    setLocation(currentLocation);
                }
            } else {
                setLocation("");
            }

            _lastUpdate = System.currentTimeMillis();
        }
    }

    private void redraw(Tile tile, float w, float h) {
        // TODO: 1x1 different layout and text size
        TileUtil.deleteTexture(_bgTexture);
        _bgTexture = BitmapUtil.createTextureFromBitmap(
                BitmapUtil.createRect(1, 1, 0, tile.bgColor));

        var time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH mm"));
        TileUtil.deleteTexture(_clockTexture);
        _clockTexture = TileUtil.createTextTexture(time,
                (int) w,
                (int) h,
                160, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT, HorizontalAlign.LEFT, VerticalAlign.CENTER);

        TileUtil.deleteTexture(_locationTexture);
        _locationTexture = TileUtil.createTextTexture(_location,
                (int) w,
                (int) h,
                72, Typeface.NORMAL,
                Colors.WHITE, Colors.TRANSPARENT, HorizontalAlign.RIGHT, VerticalAlign.TOP);
        _dirty = false;
    }

    private void setLocation(String location) {
        _location = location;
        _dirty = true;
    }

    private void drawTexture(float[] projMatrix, float[] viewMatrix, IDrawContext<Tile> drawContext,
                             float correctedX, float correctedY, float width, float height, int textureId) {
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, correctedX, correctedY, 0f);
        Matrix.scaleM(_modelMatrix, 0, width, height, 1);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        drawContext.getRenderer().draw(projMatrix, _modelMatrix, textureId);
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
        TileUtil.deleteTexture(_bgTexture);
        TileUtil.deleteTexture(_clockTexture);
        TileUtil.deleteTexture(_locationTexture);
    }

    @Override
    public void forceRedraw() {
        _dirty = true;
    }
}
