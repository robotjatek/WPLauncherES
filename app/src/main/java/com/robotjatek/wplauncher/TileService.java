package com.robotjatek.wplauncher;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class TileService {

    private final List<ITileListChangedListener> _subscribers = new ArrayList<>();
    private final Queue<Runnable> _tileCommands = new ConcurrentLinkedQueue<>();
    private final List<Tile> _tiles = new ArrayList<>();

    public TileService() {
        _tiles.addAll(loadPersistedTiles());
    }

    // Calls to this method can be off main-thread! Its content should be put in a command buffer
    public void pinTile(App app) {
        var lowestPoint = _tiles.stream().mapToInt(t -> t.y + t.rowSpan).max().orElse(0);
        _tileCommands.add(() -> {
            var tile = createTile(app.name(), new Position(0, lowestPoint), app);
            _tiles.add(tile);
            // TODO: persist()
        });
    }

    public void unpinTile(App app) {
        var tile = _tiles.stream()
                .filter(t -> t.getPackageName().equals(app.packageName()))
                .findFirst();
        tile.ifPresent(t -> _tileCommands.add(() -> {
            _tiles.remove(t);
            t.dispose();
            // TODO: persist()
        }));
    }

    public List<Tile> getTiles() {
        return _tiles;
    }

    public void persistTiles() {
        // TODO: implement persist
    }

    public List<Tile> loadPersistedTiles() {
        // TODO: load persisted tiles
        final Tile tile1 = new Tile(0, 0, 2, 2, "Első", null); // 2x2 tile
        final Tile tile2 = new Tile(0, 2, 1, 1, "Második", null); // 1x1 tile
        final Tile tile3 = new Tile(0, 4, 4, 2, "Wide tile", null); // Wide tile
        final Tile tile4 = new Tile(0, 8, 4, 2, "", null); // Wide tile
        final Tile tile5 = new Tile(0, 20, 4, 4, "Úristen, very big", null); // 4x4 large tile far down
        final Tile tile6 = new Tile(2, 0, 2, 2, "", null); // 2x2 tile
        final Tile tile7 = new Tile(2, 2, 2, 2, "", null); // 2x2 tile

        return List.of(tile1, tile2, tile3, tile4, tile5, tile6, tile7);
    }

    public void subscribe(ITileListChangedListener l) {
        _subscribers.add(l);
    }

    public void executeCommands() {
        var changed = false;
        Runnable command;
        while ((command = _tileCommands.poll()) != null) {
            command.run();
            changed = true;
        }
        if (changed) {
            notifySubscribers();
        }
    }

    private void notifySubscribers() {
        _subscribers.forEach(ITileListChangedListener::tileListChanged);
    }

    private Tile createTile(String title, Position position, App app) {
        return new Tile((int)position.x(), (int)position.y(), 2, 2, title, app);
    }

    public void dispose() {
        _tiles.forEach(Tile::dispose);
        _tiles.clear();
    }
}
