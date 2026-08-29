package com.robotjatek.wplauncher.InternalApps.Photos;

import android.graphics.Bitmap;
import android.graphics.Typeface;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Icon.Icon;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.InternalApps.Photos.States.IdleState;
import com.robotjatek.wplauncher.InternalApps.Photos.States.SwapState;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.MediaService;
import com.robotjatek.wplauncher.TileGrid.ITileContent;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhotosTileContent implements ITileContent {
    private Tile _tile;
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    // These icons are always instantiated, only the underlying bitmap is changed. This is to avoid creating new Icon objects every time a picture is swapped in, which would be inefficient.
    private final Icon _picture = new Icon(null, new Size<>(-1, -1));
    private final Icon _nextPicture = new Icon(null, new Size<>(-1, -1));
    private boolean _dirty = true;
    private boolean _disposed = false;
    private float _totalTime = (float) (Math.random() * 10000);
    private float _baseOffsetX = 0f;
    private float _baseOffsetY = 0f;
    private final MediaService _mediaService;
    private final ExecutorService _exec = Executors.newSingleThreadExecutor();
    private final ConcurrentLinkedQueue<Bitmap> _bgPictures = new ConcurrentLinkedQueue<>();
    private final List<Bitmap> _pictures = new ArrayList<>();
    private final AtomicBoolean _loading = new AtomicBoolean(false);
    private final AtomicBoolean _picturesReady = new AtomicBoolean(false);
    private int _currentPicId = -1;
    private Size<Integer> _lastSize = new Size<>(0, 0);
    private final Label _titleLabel;
    private final Icon _icon;
    private final IState _idleState = new IdleState(this);

    public IState IDLE_STATE() {
        return _idleState;
    }
    public IState SWAP_STATE() {
        return new SwapState(this);
    }

    private IState _state = IDLE_STATE();

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

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
                _bgPictures.offer(pic);
            }
        }
        _loading.set(false);
    }

    public Tile getTile() { return _tile; }
    public AbsoluteLayout getLayout() { return _layout; }
    public Icon getPicture() { return _picture; }
    public Icon getNextPicture() { return _nextPicture; }
    public Label getTitleLabel() { return _titleLabel; }
    public Icon getIcon() { return _icon; }
    public boolean isDirty() { return _dirty; }
    public void setDirty(boolean dirty) { _dirty = dirty; }
    public float getTotalTime() { return _totalTime; }
    public float getBaseOffsetX() { return _baseOffsetX; }
    public void setBaseOffsetX(float baseOffsetX) { _baseOffsetX = baseOffsetX; }
    public float getBaseOffsetY() { return _baseOffsetY; }
    public void setBaseOffsetY(float baseOffsetY) { _baseOffsetY = baseOffsetY; }
    public ConcurrentLinkedQueue<Bitmap> getBgPictures() { return _bgPictures; }
    public List<Bitmap> getPictures() { return _pictures; }
    public AtomicBoolean getLoading() { return _loading; }
    public AtomicBoolean getPicturesReady() { return _picturesReady; }
    public int getCurrentPicId() { return _currentPicId; }
    public void setCurrentPicId(int currentPicId) { _currentPicId = currentPicId; }
    public Size<Integer> getLastSize() { return _lastSize; }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer, Position<Float> position, Size<Integer> size) {
        _totalTime += delta;
        _lastSize = size;
        _state.update(delta);
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
            _picture.dispose();
            _nextPicture.dispose();
            for (var bmp : _pictures) {
                bmp.recycle();
            }
            _pictures.clear();
            Bitmap bgBmp;
            while ((bgBmp = _bgPictures.poll()) != null) {
                bgBmp.recycle();
            }
        }
    }
}
