package com.robotjatek.wplauncher.TileGrid.States.EditStates;

import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.Services.TileService;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class EditBaseState implements IState {

    protected EditState _context;
    protected final TileGrid _tilegrid;
    private final TileService _tileService;

    protected EditBaseState(EditState context, TileGrid tileGrid) {
        _context = context;
        _tilegrid = tileGrid;
        _tileService = _tilegrid.getTileService();
    }

    @Override
    public void enter() {
    }

    @Override
    public void exit() {
    }

    @Override
    public void handleTouchStart(float x, float y) {
    }

    @Override
    public void handleTouchEnd(float x, float y) {
    }

    @Override
    public void handleMove(float x, float y) {
    }

    @Override
    public void update(float delta) {

    }

    protected void reflowTiles(Position<Integer> newPosition) {
        // TODO: could be better if they swapped positions if it collides with 1 tile only
        //  but what to do in other cases: maybe tile occupancy bool map, for every tile top to bottom -> remove -> find the highest available pos where it fits -> place?
        var selectedTile = _tilegrid.getSelectedTile();
        var collidingTiles = getCollidingTiles(newPosition);
        if (collidingTiles.isEmpty()) {
            _tilegrid.getSelectedTile().setPosition(newPosition);
            _tileService.compactGrid();
            return;
        }

        var offset = calculateReflowOffset(collidingTiles, newPosition);
        var minY = collidingTiles.stream().mapToInt(t -> t.getPosition().y()).min().orElse(0);

        var tilesToPush = _tilegrid.getTiles().stream()
                .filter(t -> t != selectedTile)
                .filter(t -> t.getPosition().y() >= minY)
                .collect(Collectors.toList());

        _tileService.pushDownTiles(tilesToPush, offset);
        selectedTile.setPosition(newPosition);
        _tileService.compactGrid();
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
    protected List<Tile> getCollidingTiles(Position<Integer> newPosition) {
        var colliding = new ArrayList<Tile>();
        var selectedTile = _tilegrid.getSelectedTile();
        for (var tile : _tilegrid.getTiles())
        {
            if (tile == selectedTile) {
                continue;
            }
            var collisionX = tile.getPosition().x() < newPosition.x() + selectedTile.getSize().width() &&
                    tile.getPosition().x() + tile.getSize().width() > newPosition.x();

            var collisionY = tile.getPosition().y() < newPosition.y() + selectedTile.getSize().height() &&
                    tile.getPosition().y() + tile.getSize().height() > newPosition.y();

            if (collisionX && collisionY) {
                colliding.add(tile);
            }
        }

        return colliding;
    }

    /**
     * Calculates the offset for the new tile position after a move
     * @param group The group of tiles which should be moved
     * @param newPosition The new position of the colliding tile
     * @return The new offset for the tile group
     */
    protected int calculateReflowOffset(List<Tile> group, Position<Integer> newPosition) {
        if (group.isEmpty()) {
            return 0;
        }
        var selectedTile = _tilegrid.getSelectedTile();
        var bottom = newPosition.y() + selectedTile.getSize().height();
        var minY = group.stream().mapToInt(t -> t.getPosition().y()).min().orElse(0);
        return bottom - minY;
    }

    protected boolean isInbounds(Position<Integer> position) {
        var selectedTile = _tilegrid.getSelectedTile();
        return position.x() >= 0 && position.x() + selectedTile.getSize().width() <= TileGrid.COLUMNS
                && position.y() >= 0;
    }
}
