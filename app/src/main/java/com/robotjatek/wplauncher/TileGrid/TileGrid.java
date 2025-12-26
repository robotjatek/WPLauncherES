package com.robotjatek.wplauncher.TileGrid;

import android.content.Context;
import android.opengl.Matrix;

import androidx.core.content.ContextCompat;

import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.ITileListChangedListener;

import com.robotjatek.wplauncher.Page;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.R;
import com.robotjatek.wplauncher.ScrollController;
import com.robotjatek.wplauncher.Shader;
import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.States.IdleState;
import com.robotjatek.wplauncher.TileGrid.States.ScrollState;
import com.robotjatek.wplauncher.TileGrid.States.TappedState;
import com.robotjatek.wplauncher.TileGrid.States.TouchingState;
import com.robotjatek.wplauncher.TileService;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

// TODO: resize tile
public class TileGrid implements Page, IAdornedTileContainer, ITileListChangedListener {

    public IState IDLE_STATE() {
        return new IdleState(this);
    }

    public IState TOUCHING_STATE(float x, float y) {
        return new TouchingState(this, x, y);
    }

    public IState TAPPED_STATE(float x, float y) {
        return new TappedState(this, x, y);
    }

    public IState SCROLL_STATE(float y) {
        return new ScrollState(this, y);
    }

    public IState EDIT_STATE(float x, float y) {
        return new EditState(this, x, y);
    }

    private IState _state = IDLE_STATE();

    private final ScrollController _scroll = new ScrollController();
    private final TileDrawContext _tileDrawContext;
    private final float[] scrollMatrix = new float[16]; // Stores the state of the scroll position transformation
    private Tile _selectedTile;
    private List<Tile> _tiles;
    public static final int COLUMNS = 4;
    public static final float TOP_MARGIN_PX = 128;
    private static final float PAGE_PADDING_PX = 48;
    public static final float TILE_GAP_PX = 32;
    private final Shader _shader = new Shader("", "");
    private final QuadRenderer _renderer = new QuadRenderer(_shader);
    private float tileSizePx;
    private float _pageHeight;
    private final TileService _tileService;
    private final Adorner _unpinButton;
    private final Queue<Runnable> _commands = new ConcurrentLinkedQueue<>();

    public TileGrid(TileService tileService, Context context) {
        _tileService = tileService;
        _tiles = tileService.getTiles();
        _tileService.subscribe(this);
        _tileDrawContext = new TileDrawContext(PAGE_PADDING_PX, TILE_GAP_PX, tileSizePx, _renderer);
        var adornerDrawContext = new AdornerDrawContext<>(_tileDrawContext, _renderer, this);
        var icon = ContextCompat.getDrawable(context, R.drawable.close_circle);
        _unpinButton = new Adorner(() -> _commands.add(() -> {
            if (_selectedTile != null) {
                _tileService.unpinTile(_selectedTile.getPackageName());
                _selectedTile = null;
            }
        }), icon, adornerDrawContext);
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix) {
        _state.update(delta);
        _scroll.update(delta);
        executeCommands();

        Matrix.setIdentityM(scrollMatrix, 0);
        Matrix.translateM(scrollMatrix, 0, 0, _scroll.getScrollOffset() + TOP_MARGIN_PX, 0);
        Matrix.multiplyMM(scrollMatrix, 0, scrollMatrix, 0, viewMatrix, 0);

        for (var tile : _tiles) {
            // Do not render the selected tile here
            if (tile == _selectedTile) {
                continue;
            }
            var scale = _selectedTile == null ? 1.0f : 0.95f;
            tile.drawWithOffsetScaled(projMatrix, scrollMatrix, scale, Position.ZERO, _tileDrawContext);
        }

        // render the selected tile with different scaling, and on its current drag position
        if (_selectedTile != null) {
            _selectedTile.drawWithOffsetScaled(projMatrix, scrollMatrix,
                    1.00f,
                    new Position(_selectedTile.getDragInfo().totalX, _selectedTile.getDragInfo().totalY),
                    _tileDrawContext);
                _unpinButton.draw(projMatrix, scrollMatrix);
        }

    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    public Tile getSelectedTile() {
        return _selectedTile;
    }

    public Adorner getUnpinButton() {
        return _unpinButton;
    }

    public TileService getTileService() {
        return _tileService;
    }

    public float getPageHeight() {
        return _pageHeight;
    }

    public void onSizeChanged(int width, int height) {
        var usableWidth = width - 2 * PAGE_PADDING_PX - (COLUMNS - 1) * TILE_GAP_PX;
        tileSizePx = usableWidth / COLUMNS;
        _pageHeight = height;
        _tileDrawContext.onResize(tileSizePx);
        setScrollBounds();
    }

    @Override
    public void touchStart(float x, float y) {
        _state.handleTouchStart(x, y);
    }

    @Override
    public void touchMove(float x, float y) {
        _state.handleMove(x, y);
    }

    @Override
    public void touchEnd(float x, float y) {
        _state.handleTouchEnd(x, y);
    }

    private int calculateGroupLowestPoint(List<Tile> group) {
        return group.stream().mapToInt(t -> t.y + t.rowSpan).max().orElse(0);
    }

    /**
     * Push down a given group of tiles with an offset.
     * The move will be relative to the tiles original position.
     * @param tiles The group of tiles to move together
     * @param offset The offset of the move
     */
    public void pushDownTiles(List<Tile> tiles, int offset) {
        for (var tile: tiles) {
            tile.y += offset;
        }
    }

    /**
     * Removes empty rows between tiles
     * Note: this works on a row-by-row basis so its not very efficient
     */
    public void compactGrid() {
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

    private float getContentHeight() {
        var max = 0f;
        for (var t : _tiles) {
            var bottom =  _tileDrawContext.yOf(t) + _tileDrawContext.heightOf(t) + PAGE_PADDING_PX;
            if (bottom > max) max = bottom;
        }
        return max;
    }

    public void handleLongPress(float x, float y) {
    }

    public List<Tile> getTiles() {
        return _tiles;
    }

    public ScrollController getScroll() {
        return _scroll;
    }

    public TileDrawContext getDrawContext() {
        return _tileDrawContext;
    }

    public float getTileSizePx() {
        return tileSizePx;
    }

    public void selectTile(Tile tile) {
        _commands.add(() -> {
            _selectedTile = tile;
            _selectedTile.getDragInfo().reset();
        });
    }

    public void cancelSelection() {
        if (_selectedTile != null) {
            _commands.add(() -> _selectedTile = null);
        }
    }

    @Override
    public boolean isCatchingGestures() {
        return _selectedTile != null;
    }

    public void setScrollBounds() {
        var contentHeight = getContentHeight();
        var min = Math.min(0, _pageHeight - contentHeight - TOP_MARGIN_PX);
        _scroll.setBounds(min, 0);
    }

    @Override
    public void tileListChanged() {
        _tiles = _tileService.getTiles();
        compactGrid(); // after uninstall i have to do a reflow, because ta pinned tile could have been uninstalled
        setScrollBounds();
        _tileService.persistTiles();
    }

    private void executeCommands() {
        Runnable command;
        while ((command = _commands.poll()) != null) {
            command.run();
        }
    }

    @Override
    public Tile getAdornedTile() {
        return _selectedTile;
    }

    public void dispose() {
        _renderer.dispose();
        _shader.delete();
        _unpinButton.dispose();
    }
}
