package com.robotjatek.wplauncher.InternalApps.Photos.States;

import android.graphics.Bitmap;

import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.InternalApps.Photos.PhotosTileContent;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.Tile;

public class IdleState extends BasePhotosState {

    private final PhotosTileContent _context;
    private float _pictureTime = 0f;
    private Size<Integer> _prevSize = new Size<>(-1, -1);

    public IdleState(PhotosTileContent context) {
        _context = context;
    }

    @Override
    public void enter() {
        _pictureTime = 10000f;
    }


    @Override
    public void update(float delta) {
        _pictureTime -= delta;

        Bitmap newPic;
        while ((newPic = _context.getBgPictures().poll()) != null) {
            _context.getPictures().add(newPic);
            _context.getPicturesReady().set(true);
        }

        var size = _context.getLastSize();

        if (_context.getCurrentPicId() == -1 && _context.getPicturesReady().get()) {
            selectInitialPicture(size);
        }

        if (_pictureTime < 0 && _context.getPicturesReady().get()
                && !_context.getTile().getSize().equals(Tile.SMALL)
                && _context.getPictures().size() > 1) {
            _context.changeState(_context.SWAP_STATE());
            return;
        }

        if (_context.isDirty()) {
            _context.getLayout().setBgColor(_context.getTile().bgColor);
            if (!_prevSize.equals(size) && _context.getCurrentPicId() != -1) {
                _context.getLayout().onResize(size.width(), size.height());
                resizePicture(_context.getPictures().get(_context.getCurrentPicId()), size);
                _prevSize = size;
            }

            _context.getLayout().removeChild(_context.getPicture());
            _context.getLayout().removeChild(_context.getIcon());
            _context.getLayout().removeChild(_context.getTitleLabel());

            if (_context.getTile().getSize().equals(Tile.SMALL)) {
                var iconSize = size.width() / 2;
                _context.getIcon().setSize(new Size<>(iconSize, iconSize));
                var iconX = (size.width() - iconSize) / 2f;
                var iconY = (size.height() - iconSize) / 2f;
                _context.getLayout().addChild(_context.getIcon(), new Position<>(iconX, iconY));
            } else {
                _context.getLayout().addChild(_context.getPicture(), new Position<>(_context.getBaseOffsetX(), _context.getBaseOffsetY()));
            }

            // Always add the Title Label LAST so it's on top
            updateTitleLabel(_context);

            _context.setDirty(false);
        }

        // Apply float effect for the photo if it's visible
        if (!_context.getTile().getSize().equals(Tile.SMALL)) {
            var drift = calculateDrift(_context.getTotalTime(), _context.getBaseOffsetX(), _context.getBaseOffsetY());
            _context.getLayout().setChildPosition(_context.getPicture(), new Position<>(_context.getBaseOffsetX() + drift.x(), _context.getBaseOffsetY() + drift.y()));
        }
    }

    private void resizePicture(Bitmap bitmap, Size<Integer> size) {
        var scale = getScale(bitmap, size);
        var photoSize = getPhotoSize(bitmap, scale);
        var offset = getPhotoOffset(photoSize, size);

        _context.setBaseOffsetX(offset.x());
        _context.setBaseOffsetY(offset.y());
        _context.getPicture().setSize(photoSize);
        _context.getPicture().setBitmap(bitmap);
    }

    private void selectInitialPicture(Size<Integer> size) {
        _context.setCurrentPicId(0);
        var bitmap = _context.getPictures().get(0);
        resizePicture(bitmap, size);
    }
}
