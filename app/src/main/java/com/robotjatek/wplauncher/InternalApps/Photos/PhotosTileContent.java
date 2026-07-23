package com.robotjatek.wplauncher.InternalApps.Photos;

import android.graphics.Bitmap;
import android.graphics.Typeface;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Icon.Icon;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.MediaService;
import com.robotjatek.wplauncher.TileGrid.ITileContent;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhotosTileContent implements ITileContent {
    private Tile _tile;
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Icon _picture = new Icon(null, new Size<>(-1, -1));
    private boolean _dirty = true;
    private boolean _disposed = false;
    private float _totalTime = (float) (Math.random() * 10000);
    private float _baseOffsetX = 0f;
    private float _baseOffsetY = 0f;
    private final MediaService _mediaService;
    private final ExecutorService _exec = Executors.newSingleThreadExecutor();
    private final List<Bitmap> _bgPictures = new ArrayList<>();
    private final List<Bitmap> _pictures = new ArrayList<>();
    private final AtomicBoolean _loading = new AtomicBoolean(false);
    private final AtomicBoolean _picturesReady = new AtomicBoolean(false);
    private float _pictureTime = 0f;
    private final Random _rand = new Random();
    private int _currentPicId = -1;
    private Size<Integer> _prevSize = new Size<>(-1, -1);
    private final Label _titleLabel;
    private final Icon _icon;

    public PhotosTileContent(MediaService mediaService, App app) {
        _mediaService = mediaService;
        _titleLabel = new Label(app.name(), 48, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
        _icon = new Icon(app.icon(), new Size<>(256, 256));
        _exec.execute(this::loadPicturesInBackground);
    }

    private void loadPicturesInBackground() {
        _loading.set(true);
        var ids = _mediaService.loadLatestPhotoUris();
        for (var id : ids) {
            var pic = _mediaService.loadThumbnail(id, 512, 512);
            if (pic != null) {
                _bgPictures.add(pic);
            }
        }
        _loading.set(false);
    }

    private void selectARandomPicture(Size<Integer> size) {
        if (_picturesReady.get() && !_pictures.isEmpty()) {
            _currentPicId = _rand.nextInt(_pictures.size());
            var bitmap = _pictures.get(_currentPicId);
            resizeCurrentPicture(bitmap, size);
            _picture.setBitmap(bitmap);
        }
    }

    private void resizeCurrentPicture(Bitmap bitmap, Size<Integer> size) {
        var scale = Math.max((float)size.width() / bitmap.getWidth(), (float)size.height() / bitmap.getHeight()) * 1.2f;
        var w = bitmap.getWidth() * scale;
        var h = bitmap.getHeight() * scale;
        _baseOffsetX = (size.width() - w) / 2f;
        _baseOffsetY = (size.height() - h) / 2f;
        _picture.setSize(new Size<>((int)w, (int)h));
        _picture.setBitmap(bitmap);
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer, Position<Float> position, Size<Integer> size) {
        _totalTime += delta;
        _pictureTime -= delta;
        if (!_loading.get()) {
            if (!_bgPictures.isEmpty()) {
                _pictures.addAll(_bgPictures);
                _bgPictures.clear();
                _picturesReady.set(true);
            }
        }

        if (_pictureTime < 0 && _picturesReady.get()) {
            selectARandomPicture(size);
            _pictureTime = 10000;
        }

        if (_dirty) {
            _layout.setBgColor(_tile.bgColor);
            if (!_prevSize.equals(size) && _currentPicId != -1) {
                resizeCurrentPicture(_pictures.get(_currentPicId), size);
                _prevSize = size;
            }

            _layout.removeChild(_picture);
            _layout.removeChild(_icon);
            _layout.removeChild(_titleLabel);

            // Add the background layer (Photo or Icon)
            if (_tile.getSize().equals(Tile.SMALL)) {
                var iconSize = size.width() / 2;
                _icon.setSize(new Size<>(iconSize, iconSize));
                var iconX = (size.width() - iconSize) / 2f;
                var iconY = (size.height() - iconSize) / 2f;
                _layout.addChild(_icon, new Position<>(iconX, iconY));
            } else {
                _layout.addChild(_picture, new Position<>(_baseOffsetX, _baseOffsetY));
            }

            // Always add the Title Label LAST so it's on top
            var titleText = _tile.getSize().equals(Tile.SMALL) ? "" : _tile.getApp().name();
            if (!titleText.equals(_titleLabel.getText())) {
                _titleLabel.setText(titleText);
            }
            var padding = size.height() * 0.035f;
            _titleLabel.setMaxWidth(size.width() - padding);
            var labelHeight = _titleLabel.measure().height();
            _layout.addChild(_titleLabel, new Position<>(padding, size.height() - labelHeight - padding / 2));

            // TODO: listen for new pictures and add them to the list
            // TODO: downscaled size should be around the tile resolution
            // TODO: smooth picture change
            _dirty = false;
        }

        // Apply float effect for the photo if it's visible
        if (!_tile.getSize().equals(Tile.SMALL)) {
            var xAmplitude = _baseOffsetX * 0.9f;
            var yAmplitude = _baseOffsetY * 0.9f;
            var driftX = (float) Math.sin(_totalTime / 4500f) * xAmplitude;
            var driftY = (float) Math.cos(_totalTime / 4700f) * yAmplitude;
            _layout.setChildPosition(_picture, new Position<>(_baseOffsetX + driftX, _baseOffsetY + driftY));
        }

        _layout.draw(delta, projMatrix, viewMatrix, renderer, position, size);
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
    public void setParent(Tile parent) {
        _tile = parent;
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _disposed = true;
            _exec.shutdownNow();
            _layout.dispose();
            _titleLabel.dispose();
            _icon.dispose();
            for (var bmp : _pictures) {
                bmp.recycle();
            }
            _pictures.clear();
            for (var bmp : _bgPictures) {
                bmp.recycle();
            }
            _bgPictures.clear();
        }
    }
}
