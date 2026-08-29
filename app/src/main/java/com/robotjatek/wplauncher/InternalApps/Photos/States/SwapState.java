package com.robotjatek.wplauncher.InternalApps.Photos.States;

import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.InternalApps.Photos.PhotosTileContent;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.Random;

public class SwapState extends BasePhotosState {

    private final PhotosTileContent _context;
    private final Random _rand = new Random();
    private int _nextPictureId = -1;
    private Size<Integer> _prevSize = new Size<>(-1, -1);
    private float _nextX, _nextY, _startX, _startY, _tileWidth;
    private float _oldDriftX, _oldDriftY, _targetDriftX, _targetDriftY;
    private float _elapsed = 0f;
    private static final float DURATION = 850f;

    public SwapState(PhotosTileContent context) {
        _context = context;
    }

    @Override
    public void enter() {
        var pictures = _context.getPictures();
        if (pictures.size() < 2) {
            _context.changeState(_context.IDLE_STATE());
            return;
        }

        var currentId = _context.getCurrentPicId();
        var currentBitmap = currentId != -1 ? pictures.get(currentId) : null;
        int attempts = 0;
        do {
            _nextPictureId = _rand.nextInt(pictures.size());
            attempts++;
        } while ((_nextPictureId == currentId || (currentBitmap != null && pictures.get(_nextPictureId).sameAs(currentBitmap))) && attempts < 10);

        recomputeLayout(_context.getLastSize());

        var totalTime = _context.getTotalTime();
        var oldDrift = calculateDrift(totalTime, _startX, _startY);
        _oldDriftX = oldDrift.x();
        _oldDriftY = oldDrift.y();

        var endTime = totalTime + DURATION;
        var targetDrift = calculateDrift(endTime, _nextX, _nextY);
        _targetDriftX = targetDrift.x();
        _targetDriftY = targetDrift.y();

        var next = _context.getNextPicture();
        next.setBitmap(pictures.get(_nextPictureId));
        _context.getLayout().addChild(next, new Position<>(_nextX + _tileWidth, _nextY));

        // Always add the Title Label LAST so it's on top
        updateTitleLabel(_context);
    }

    @Override
    public void update(float delta) {
        if (_nextPictureId  == -1) {
            return;
        }

        var size = _context.getLastSize();
        if (!size.equals(_prevSize)) {
            recomputeLayout(size);
        }

        _elapsed += delta;
        var t = Math.min(_elapsed / DURATION, 1);
        var eased = easeOut(t);
        var dx = eased * _tileWidth;

        _context.getLayout().setChildPosition(_context.getPicture(), new Position<>(_startX + _oldDriftX - dx, _startY + _oldDriftY));
        _context.getLayout().setChildPosition(_context.getNextPicture(), new Position<>(_nextX + _targetDriftX + _tileWidth - dx, _nextY + _targetDriftY));

        if (t >= 1f) {
            finishSwap();
        }
    }

    @Override
    public void exit() {
        _context.getLayout().removeChild(_context.getNextPicture());
    }

    private void recomputeLayout(Size<Integer> size) {
        _tileWidth = size.width();

        var current = _context.getPictures().get(_context.getCurrentPicId());
        var currentScale = getScale(current, size);
        var currentSize = getPhotoSize(current, currentScale);
        var currentOffset = getPhotoOffset(currentSize, size);
        _startX = currentOffset.x();
        _startY = currentOffset.y();
        _context.getPicture().setSize(currentSize);

        var next = _context.getPictures().get(_nextPictureId);
        var nextScale = getScale(next, size);
        var nextSize = getPhotoSize(next, nextScale);
        var nextOffset = getPhotoOffset(nextSize, size);
        _nextX = nextOffset.x();
        _nextY = nextOffset.y();
        _context.getNextPicture().setSize(nextSize);

        _prevSize = size;
    }

    private void finishSwap() {
        _context.setCurrentPicId(_nextPictureId);
        _context.setBaseOffsetX(_nextX);
        _context.setBaseOffsetY(_nextY);
        _context.getPicture().setSize(_context.getNextPicture().measure());
        _context.getPicture().setBitmap(_context.getPictures().get(_nextPictureId));

        _context.getLayout().setChildPosition(_context.getPicture(), new Position<>(_nextX + _targetDriftX, _nextY + _targetDriftY));
        _context.getLayout().removeChild(_context.getNextPicture());
        _context.changeState(_context.IDLE_STATE());
    }

    private float easeOut(float t) {
        return 1 - (1 - t) * (1 - t) * (1 - t) * (1 - t);
    }
}
