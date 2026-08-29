package com.robotjatek.wplauncher.InternalApps.Photos.States;

import android.graphics.Bitmap;

import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.InternalApps.Photos.PhotosTileContent;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.Tile;

public abstract class BasePhotosState implements IState {

    protected Position<Float> calculateDrift(float time, float baseOffsetX, float baseOffsetY) {
        var xAmplitude = baseOffsetX * 0.9f;
        var yAmplitude = baseOffsetY * 0.9f;
        var driftX = (float) Math.sin(time / 4500f) * xAmplitude;
        var driftY = (float) Math.cos(time / 4700f) * yAmplitude;
        return new Position<>(driftX, driftY);
    }

    protected float getScale(Bitmap bitmap, Size<Integer> tileSize) {
        return Math.max((float) tileSize.width() / bitmap.getWidth(), (float) tileSize.height() / bitmap.getHeight()) * 1.2f;
    }

    protected Size<Integer> getPhotoSize(Bitmap bitmap, float scale) {
        var w = (int) (bitmap.getWidth() * scale);
        var h = (int) (bitmap.getHeight() * scale);
        return new Size<>(w, h);
    }

    protected Position<Float> getPhotoOffset(Size<Integer> photoSize, Size<Integer> tileSize) {
        var x = (tileSize.width() - photoSize.width()) / 2f;
        var y = (tileSize.height() - photoSize.height()) / 2f;
        return new Position<>(x, y);
    }

    protected void updateTitleLabel(PhotosTileContent context) {
        var size = context.getLastSize();
        var titleLabel = context.getTitleLabel();
        var layout = context.getLayout();
        var tile = context.getTile();

        layout.removeChild(titleLabel);

        var titleText = tile.getSize().equals(Tile.SMALL) ? "" : tile.getApp().name();
        titleLabel.setText(titleText);

        var padding = size.height() * 0.035f;
        titleLabel.setMaxWidth(size.width() - padding);
        var labelHeight = titleLabel.measure().height();
        layout.addChild(titleLabel, new Position<>(padding, size.height() - labelHeight - padding / 2));
    }

    @Override
    public void enter() {}

    @Override
    public void exit() {}

    @Override
    public abstract void update(float delta);
}
