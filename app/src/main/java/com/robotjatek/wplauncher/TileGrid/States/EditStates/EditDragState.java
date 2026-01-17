package com.robotjatek.wplauncher.TileGrid.States.EditStates;

import com.robotjatek.wplauncher.TileGrid.DragInfo;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class EditDragState extends EditBaseState {
    private final float _x;
    private final float _y;


    public EditDragState(EditState context, TileGrid tilegrid, float x, float y) {
        super(context, tilegrid);
        _x = x;
        _y = y;
    }

    @Override
    public void enter() {
        super.enter();
        _tilegrid.getSelectedTile().getDragInfo().start(_x, _y);
    }

    @Override
    public void exit() {
        super.exit();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        var selectedTile = _tilegrid.getSelectedTile();
        var drawContext = _tilegrid.getDrawContext();
        var screenPosY = drawContext.yOf(selectedTile)
                + selectedTile.getDragInfo().totalY
                + _tilegrid.getScroll().getScrollOffset();
        var scrollSpeed = 2 * delta;
        if (screenPosY + drawContext.heightOf(selectedTile) > _tilegrid.getPageHeight() - 200) { // reached bottom while dragging
            _tilegrid.getScroll().adjustOffset(-scrollSpeed);
        } else if (screenPosY < 200) { // reached top while dragging
            _tilegrid.getScroll().adjustOffset(scrollSpeed);
        }
    }

    @Override
    public void handleMove(float x, float y) {
        super.handleMove(x, y);
        _tilegrid.getSelectedTile()
                .getDragInfo().update(x, y);
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        super.handleTouchEnd(x, y);
        var dragInfo = _tilegrid.getSelectedTile().getDragInfo();

        // drop tile to its new location, recalculate new tile positions, remove empty lines from the grid
        var newPosition = calculateNewPosition(dragInfo);
        if (isInbounds(newPosition)) {
            reflowTiles(newPosition);
        }

        _tilegrid.cancelSelection();
        _tilegrid.changeState(_tilegrid.IDLE_STATE());
    }

    /**
     * Calculate the new tile position after a drag
     * @param args Drag information
     * @return The calculated position of the tile
     */
    private Position<Integer> calculateNewPosition(DragInfo args)
    {
        var calculatedTranslationX = args.totalX / (_tilegrid.getTileSizePx() + TileGrid.TILE_GAP_PX);
        var calculatedTranslationY = args.totalY / (_tilegrid.getTileSizePx() + TileGrid.TILE_GAP_PX);

        var selectedTile = _tilegrid.getSelectedTile();
        var calculatedColumn = Math.round(selectedTile.getPosition().x() + calculatedTranslationX);
        var calculatedRow = Math.round(selectedTile.getPosition().y() + calculatedTranslationY);

        return new Position<>(calculatedColumn, calculatedRow);
    }
}
