package com.robotjatek.wplauncher;

import android.opengl.Matrix;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

record Position(float x, float y) {}

class DragInfo {
    public float startX = 0f;
    public float startY = 0f;
    public float totalX = 0f;
    public float totalY = 0f;

    public void start(float startX, float startY) {
        this.startX = startX;
        this.startY = startY;
        this.totalX = 0;
        this.totalY = 0;
    }

    public void update(float x, float y) {
        this.totalX = x - startX;
        this.totalY = y - startY;
    }
}

// TODO: resize tile
public class TileGrid implements Page {

    private final ScrollController _scroll = new ScrollController();

    private final float[] scrollMatrix = new float[16]; // Stores the state of the scroll position transformation
    private final float[] modelMatrix = new float[16]; // Reused model matrix for the individual tiles
    private boolean _isTouching = false;
    private long _touchStart = 0;
    private float _touchStartX = 0;
    private float _touchStartY = 0;

    private Tile _selectedTile;
    private final DragInfo _dragInfo = new DragInfo(); // Reused drag information to mitigate GC pressure
    private boolean _isDragging = false;

    private final Tile tile1 = new Tile(0, 0, 2, 2, "Első"); // 2x2 tile
    private final Tile tile2 = new Tile(0, 2, 1, 1, "Második"); // 1x1 tile
    private final Tile tile3 = new Tile(0, 4, 4, 2, "Wide tile"); // Wide tile
    private final Tile tile4 = new Tile(0, 8, 4, 2, ""); // Wide tile
    private final Tile tile5 = new Tile(0, 20, 4, 4, "Úristen, very big"); // 4x4 large tile far down
    private final Tile tile6 = new Tile(2, 0, 2, 2, ""); // 2x2 tile
    private final Tile tile7 = new Tile(2, 2, 2, 2, ""); // 2x2 tile
    private final List<Tile> _tiles = new ArrayList<>(List.of(tile1, tile2, tile3, tile4, tile5, tile6, tile7));

    private static final int COLUMNS = 4;
    private static final float TOP_MARGIN_PX = 128;
    private static final float PAGE_PADDING_PX = 24;
    private static final float TILE_GAP_PX = 32;

    private final QuadRenderer renderer = new QuadRenderer();

    private float tileSizePx;
    private float _pageHeight;

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix) {
        _scroll.update(delta);

        if(_isDragging) {
            var screenPosY = tileY(_selectedTile) + _dragInfo.totalY + _scroll.getScrollOffset();
            var scrollSpeed = 2 * delta;
            if (screenPosY + tileHeight(_selectedTile) > _pageHeight - 200) { // reached bottom while dragging
                _scroll.adjustOffset(-scrollSpeed);
            } else if (screenPosY < 200) { // reached top while dragging
                _scroll.adjustOffset(scrollSpeed);
            }
        }

       if (_isTouching) {
            var deltaTouchTime = System.currentTimeMillis() - _touchStart;
            if (deltaTouchTime > ViewConfiguration.getLongPressTimeout()) {
                _touchStart = 0;
                _isTouching = false;
            }
       }

        Matrix.setIdentityM(scrollMatrix, 0);
        Matrix.translateM(scrollMatrix, 0, 0, _scroll.getScrollOffset() + TOP_MARGIN_PX, 0);

        for (var t : _tiles) {
            // Do not render the selected tile here
            if (t == _selectedTile) {
                continue;
            }

            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, tileX(t), tileY(t), 0);
            Matrix.scaleM(modelMatrix, 0, tileWidth(t), tileHeight(t), 1);

            Matrix.multiplyMM(modelMatrix, 0, scrollMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(modelMatrix, 0, viewMatrix, 0, modelMatrix, 0);

            renderer.draw(projMatrix, modelMatrix, t.textureId);
        }

        // render the selected tile
        if (_selectedTile != null) {
            var width = tileWidth(_selectedTile) * 1.05f;
            var height = tileHeight(_selectedTile) * 1.05f;
            var xDiff = (width - tileWidth(_selectedTile)) / 2;
            var yDiff = (height - tileHeight(_selectedTile)) / 2;

            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0,
                    tileX(_selectedTile) + _dragInfo.totalX - xDiff,
                    tileY(_selectedTile) + _dragInfo.totalY - yDiff, 0f);
            Matrix.scaleM(modelMatrix, 0, width, height, 1);
            Matrix.multiplyMM(modelMatrix, 0, scrollMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(modelMatrix, 0, viewMatrix, 0, modelMatrix, 0);
            renderer.draw(projMatrix, modelMatrix, _selectedTile.textureId);
        }

    }

    public void onSizeChanged(int width, int height) {
        var usableWidth = width - 2 * PAGE_PADDING_PX - (COLUMNS - 1) * TILE_GAP_PX;
        tileSizePx = usableWidth / COLUMNS;
        _pageHeight = height;
        setScrollBounds();
    }

    @Override
    public void touchStart(float x, float y) {
        _touchStartX = x;
        _touchStartY = y;
        _touchStart = System.currentTimeMillis();
        _isTouching = true;
        _scroll.onTouchStart(y);
    }

    @Override
    public void touchMove(float x, float y) {
        _isTouching = false; // TODO: maybe fine-tune this to ignore random noises
        _touchStart = 0;
        if (_selectedTile == null) {
            _scroll.onTouchMove(y);
        } else {
            // Update pan info of the selected tile
            _isDragging = true;
            _dragInfo.update(x, y);
        }
    }

    @Override
    public void touchEnd(float x, float y) {
        if (_isTouching) {
            handleTap(x, y);
            _isTouching = false;
            _touchStart = 0;
        }

        if (_isDragging) {
            // drop tile to its new location, recalculate new tile positions
            var newPosition = calculateNewPosition(_dragInfo);
            if (isInbounds(newPosition)) {
                var collidingTiles = getCollidingTiles(newPosition); // TODO: ha csak 1 tilelal ütközik akkor lehet hogy jobb lenne ha helyet cserélnének
                var lowestPoint = calculateGroupLowestPoint(collidingTiles);
                var nonCollidingBelow = getTilesBelowGroup(collidingTiles, lowestPoint);
                var offset = calculateReflowOffset(collidingTiles, newPosition);

                pushDownTiles(collidingTiles, offset);
                pushDownTiles(nonCollidingBelow, offset);

                _selectedTile.x = (int) newPosition.x();
                _selectedTile.y = (int) newPosition.y();

                compactGrid();
            }

            _isDragging = false;
            _selectedTile = null;
            setScrollBounds();
        }

        _scroll.onTouchEnd();
    }

    /**
     * Calculate the new tile position after a drag
     * @param args Drag information
     * @return The calculated position of the tile
     */
    private Position calculateNewPosition(DragInfo args)
    {
        var calculatedTranslationX = args.totalX / (tileSizePx + TILE_GAP_PX);
        var calculatedTranslationY = args.totalY / (tileSizePx + TILE_GAP_PX);

        var calculatedColumn = Math.round(_selectedTile.x + calculatedTranslationX);
        var calculatedRow = Math.round(_selectedTile.y + calculatedTranslationY);

        return new Position(calculatedColumn, calculatedRow);
    }

    private boolean isInbounds(Position position) {
        return position.x() >= 0 && position.x() + _selectedTile.colSpan <= COLUMNS
                && position.y() >= 0;
    }

    /**
     * Calculates the collisions of the selected tile with other tiles on its final calculated position
     * It does not take the visual position during drag into consideration.
     * Used for reflow calculations.
     * <p>
     *     Note: a 1 by 1 tile can only overlap with at most 1 tile as its position is snapped its final position not its visual pos during dragging
     * </p>
     * @param newPosition The supposed new position of the selected tile
     * @return The list of the colliding tiles on its new position
     */
    private List<Tile> getCollidingTiles(Position newPosition) {
        var colliding = new ArrayList<Tile>();

        for (var tile : _tiles)
        {
            if (tile == _selectedTile) {
                continue;
            }
            var collisionX = tile.x < newPosition.x() + _selectedTile.colSpan &&
                    tile.x + tile.colSpan > newPosition.x();

            var collisionY = tile.y < newPosition.y() + _selectedTile.rowSpan &&
                    tile.y + tile.rowSpan > newPosition.y();

            if (collisionX && collisionY) {
                colliding.add(tile);
            }
        }

        return colliding;
    }

    private int calculateGroupLowestPoint(List<Tile> group) {
        return group.stream().mapToInt(t -> t.y + t.rowSpan).max().orElse(0);
    }

    /**
     * Calculates the offset for the new tile position after a move
     * @param group The group of tiles which should be moved
     * @param newPosition The new position of the colliding tile
     * @return The new offset for the tile group
     */
    private int calculateReflowOffset(List<Tile> group, Position newPosition) {
        if (group.isEmpty()) {
            return 0;
        }
        var bottom = newPosition.y() + _selectedTile.rowSpan;
        var minY = group.stream().mapToInt(t -> t.y).min().orElse(0);
        return (int)bottom - minY;
    }

    private List<Tile> getTilesBelowGroup(List<Tile> collidedGroup, int groupHeight) {
        // filter out the collided and the selected tiles
        var nonCollided = _tiles.stream().filter(t -> !collidedGroup.contains(t) && t != _selectedTile);
        var below = nonCollided.filter(t -> t.y >= groupHeight);
        return below.collect(Collectors.toList());
    }

    /**
     * Push down a given group of tiles with an offset.
     * The move will be relative to the tiles original position.
     * @param tiles The group of tiles to move together
     * @param offset The offset of the move
     */
    private void pushDownTiles(List<Tile> tiles, int offset) {
        for (var tile: tiles) {
            tile.y += offset;
        }
    }

    private void compactGrid() {
        var maxRow = calculateGroupLowestPoint(_tiles);
        for (var i = 0; i < maxRow; i++) {
            if (!isRowEmpty(i)) {
                continue;
            }

            final int currentRow = i;
            var group = _tiles.stream()
                    .filter(t -> t.y > currentRow)
                    .collect(Collectors.toList());

            if (group.isEmpty()) {
                continue;
            }

            var top = getTopOfTheGroup(group);
            var offset = currentRow - top;

            if (offset < 0) {
                pushDownTiles(group, offset);
            }
        }
    }

    private boolean isRowEmpty(int row) {
        return _tiles.stream().noneMatch(t -> row >= t.y && row < t.y + t.rowSpan);
    }

    private int getTopOfTheGroup(List<Tile> group) {
        return group.stream().mapToInt(t -> t.y).min().orElse(0);
    }

    /**
     * Calculates screen space X position of a tile
     * @param t The tile to measure
     * @return Screen space Y positon
     */
    private float tileX(Tile t) {
        return PAGE_PADDING_PX + t.x * (tileSizePx + TILE_GAP_PX);
    }

    /**
     * Calculates screen space Y position of a tile
     * @param t The tile to measure
     * @return Screen space Y positon
     */
    private float tileY(Tile t) {
        return PAGE_PADDING_PX + t.y * (tileSizePx + TILE_GAP_PX);
    }

    private float tileWidth(Tile t) {
        return t.colSpan * tileSizePx + (t.colSpan - 1) * TILE_GAP_PX;
    }

    private float tileHeight(Tile t) {
        return t.rowSpan * tileSizePx + (t.rowSpan - 1) * TILE_GAP_PX;
    }

    private float getContentHeight() {
        var max = 0f;
        for (var t : _tiles) {
            var bottom = tileY(t) + tileHeight(t) + PAGE_PADDING_PX;
            if (bottom > max) max = bottom;
        }
        return max;
    }

    private void handleTap(float x, float y) {
        var tappedTile = getTileAt(x, y);
        if (_selectedTile != null) {
            // Tapped same tile or empty space, deselect
            if (tappedTile.isEmpty() || tappedTile.get() == _selectedTile) {
                cancelSelection();
            } else {
                // Tap different tile
                selectTile(tappedTile.get());
            }
            _dragInfo.start(x, y);
            return;
        }

        tappedTile.ifPresent(t -> {}); // TODO: handle tap: start application
    }

    public void handleLongPress(float x, float y) {
        var tile = getTileAt(x, y);
        tile.ifPresent(this::selectTile);
        _dragInfo.start(x, y);
    }

    private Optional<Tile> getTileAt(float x, float y) {
        return _tiles.stream().filter(t -> {
            var scrollPosition = _scroll.getScrollOffset();
            var left = tileX(t);
            var top = tileY(t) + scrollPosition + TOP_MARGIN_PX;
            var right = left + tileWidth(t);
            var bottom = top + tileHeight(t);

            return x >= left && x <= right && y >= top && y <= bottom;
        }).findFirst();
    }

    private void selectTile(Tile tile) {
        _selectedTile = tile;
    }

    private void cancelSelection() {
        if (_selectedTile != null) {
            _selectedTile = null;
        }
    }

    @Override
    public boolean isCatchingGestures() {
        return _selectedTile != null;
    }

    private void setScrollBounds() {
        var contentHeight = getContentHeight();
        var min = Math.min(0, _pageHeight - contentHeight - TOP_MARGIN_PX);
        _scroll.setBounds(min, 0);
    }

    public void dispose() {
        _tiles.forEach(Tile::dispose);
        renderer.dispose();
    }
}
