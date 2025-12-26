package com.robotjatek.wplauncher.TileGrid.States.EditStates;

import com.robotjatek.wplauncher.TileGrid.DragInfo;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EditDragState extends EditBaseState {
    private final float _x;
    private final float _y;
    private final Tile _selectedTile;


    public EditDragState(EditState context, TileGrid tilegrid, float x, float y) {
        super(context, tilegrid);
        _x = x;
        _y = y;
        _selectedTile = tilegrid.getSelectedTile();
    }

    @Override
    public void enter() {
        super.enter();
        _selectedTile.getDragInfo().start(_x, _y);
    }

    @Override
    public void exit() {
        super.exit();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        var drawContext = _tilegrid.getDrawContext();
        var screenPosY = drawContext.yOf(_selectedTile)
                + _selectedTile.getDragInfo().totalY
                + _tilegrid.getScroll().getScrollOffset();
        var scrollSpeed = 2 * delta;
        if (screenPosY + drawContext.heightOf(_selectedTile) > _tilegrid.getPageHeight() - 200) { // reached bottom while dragging
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
        var dragInfo = _selectedTile.getDragInfo();

        // drop tile to its new location, recalculate new tile positions
        var newPosition = calculateNewPosition(dragInfo);
        if (isInbounds(newPosition)) {
            var collidingTiles = getCollidingTiles(newPosition); // TODO: ha csak 1 tilelal ütközik akkor lehet hogy jobb lenne ha helyet cserélnének
            var lowestPoint = calculateGroupLowestPoint(collidingTiles);
            var nonCollidingBelow = getTilesBelowGroup(collidingTiles, lowestPoint);
            var offset = calculateReflowOffset(collidingTiles, newPosition);

            _tilegrid.pushDownTiles(collidingTiles, offset);
            _tilegrid.pushDownTiles(nonCollidingBelow, offset);

            _selectedTile.x = (int) newPosition.x();
            _selectedTile.y = (int) newPosition.y();

            _tilegrid.compactGrid();
            _tilegrid.getTileService().persistTiles();
        }

        _tilegrid.cancelSelection();
        _tilegrid.setScrollBounds();

        _tilegrid.changeState(_tilegrid.IDLE_STATE());
    }

    /**
     * Calculate the new tile position after a drag
     * @param args Drag information
     * @return The calculated position of the tile
     */
    private Position calculateNewPosition(DragInfo args)
    {
        var calculatedTranslationX = args.totalX / (_tilegrid.getTileSizePx() + TileGrid.TILE_GAP_PX);
        var calculatedTranslationY = args.totalY / (_tilegrid.getTileSizePx() + TileGrid.TILE_GAP_PX);

        var calculatedColumn = Math.round(_selectedTile.x + calculatedTranslationX);
        var calculatedRow = Math.round(_selectedTile.y + calculatedTranslationY);

        return new Position(calculatedColumn, calculatedRow);
    }

    private boolean isInbounds(Position position) {
        return position.x() >= 0 && position.x() + _selectedTile.colSpan <= TileGrid.COLUMNS
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

        for (var tile : _tilegrid.getTiles())
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

    private List<Tile> getTilesBelowGroup(List<Tile> collidedGroup, int groupHeight) {
        // filter out the collided and the selected tiles
        var nonCollided = _tilegrid.getTiles().stream().filter(t -> !collidedGroup.contains(t) && t != _selectedTile);
        var below = nonCollided.filter(t -> t.y >= groupHeight);
        return below.collect(Collectors.toList());
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
}
