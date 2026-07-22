package com.robotjatek.wplauncher.InternalApps.Photos;

import com.robotjatek.wplauncher.Components.Icon.Icon;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.MediaService;
import com.robotjatek.wplauncher.TileGrid.ITileContent;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.Tile;

public class PhotosTileContent implements ITileContent {
    private Tile _tile;
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Icon _picture = new Icon(null, new Size<>(-1, -1)); // TODO: create a custom Image component
    private boolean _dirty = true;
    private boolean _disposed = false;
    private float _totalTime = (float) (Math.random() * 10000);
    private float _baseOffsetX = 0f;
    private float _baseOffsetY = 0f;
    private final MediaService _mediaService;

    public PhotosTileContent(MediaService mediaService) {
        _mediaService = mediaService;
        _layout.addChild(_picture, Position.ZERO);
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer, Position<Float> position, Size<Integer> size) {
        _totalTime += delta;
        if (_dirty) {
            // TODO: BUG: tiles getting dirty flag set on scroll stop (forceredraw <- tile.Setscale <- TileGrid.cancelSelection!!! (valószínűleg a tilegrid.IdleState.enter a baj)
            _layout.setBgColor(_tile.bgColor);

            var uris = _mediaService.loadLatestPhotoUris();
            if (!uris.isEmpty()) {
                // TODO: handle more than one image
                // TODO: periodically change the drawn photo
                var targetW = Math.max(1, size.width() * 2);
                var targetH = Math.max(1, size.height() * 2);
                
                var bitmap = _mediaService.loadThumbnail(uris.get(5), targetW, targetH);
                if (bitmap != null && size.width() > 0 && size.height() > 0) {
                    // little zoomed in, centered
                    var scale = Math.max((float)size.width() / bitmap.getWidth(), (float)size.height() / bitmap.getHeight()) * 1.3f;
                    var w = bitmap.getWidth() * scale;
                    var h = bitmap.getHeight() * scale;
                    _baseOffsetX = (size.width() - w) / 2f;
                    _baseOffsetY = (size.height() - h) / 2f;
                    _picture.setSize(new Size<>((int)w, (int)h));
                    _picture.setBitmap(bitmap);
                }
            }

            // TODO: start async load of thumbnails when _dirty = true
            // TODO: free the old resources when the loading is done
            // TODO: discard old resources on resize and load new ones


            // TODO: small tile should just show the application icon as StaticTileContent would do
            // TODO: downscale images to a reasonable resolution
            // TODO: downscaled size should be around the tile resolution
            // TODO: load and downscale images asynchronously
            // TODO: show normal app icon while the images are loading/decoding
            // TODO: cycle images
            // TODO: random zoom and UV for the loaded images => slowly move the images around
            _dirty = false;
        }

        // Apply float effect
        var xAmplitude = _baseOffsetX * 0.9f;
        var yAmplitude = _baseOffsetY * 0.9f;
        var driftX = (float) Math.sin(_totalTime / 4500f) * xAmplitude; // TODO: make float effect slower on the medium sized tile
        var driftY = (float) Math.cos(_totalTime / 4700f) * yAmplitude;
        _layout.setChildPosition(_picture, new Position<>(_baseOffsetX + driftX, _baseOffsetY + driftY));

        _layout.draw(delta, projMatrix, viewMatrix, renderer, position, size);

        // TODO: show app name like on other tiles
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
            _layout.dispose();
            _disposed = true;
        }
    }
}
