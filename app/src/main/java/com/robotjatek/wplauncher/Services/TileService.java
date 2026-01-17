package com.robotjatek.wplauncher.Services;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.InternalApps.Clock.ClockTileContent;
import com.robotjatek.wplauncher.InternalApps.Settings.OnChangeListener;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.StaticTileContent;
import com.robotjatek.wplauncher.TileGrid.Tile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;


public class TileService implements OnChangeListener<AccentColor> {

    private static final String PREF_NAME = "WPLAUNCHER";
    private static final String TILES = "TILES";
    private static final List<Size<Integer>> tileSizes = List.of(
            new Size<>(4, 2),
            new Size<>(2, 2),
            new Size<>(1, 1));
    private final List<ITileListChangedListener> _subscribers = new ArrayList<>();
    private final Queue<Runnable> _tileCommands = new ConcurrentLinkedQueue<>();
    private final List<Tile> _tiles = new ArrayList<>();
    private final Context _context;
    private final InternalAppsService _internalAppsService;
    private final SettingsService _settingsService;

    public TileService(Context context, InternalAppsService internalAppsService, SettingsService settingsService) {
        _context = context;
        _internalAppsService = internalAppsService;
        _settingsService = settingsService;
        _settingsService.subscribe(this);
        _tiles.addAll(loadPersistedTiles());
    }

    /**
     * Pins the application on the TileGrid
     * Execution is delayed until calling {@link #executeCommands()}
     * Calls to this method can be off main-thread
     * To avoid crashes this method puts its calls into a queue.
     * Execution of these commands must be fired on the main thread using {@link #executeCommands()}
     * @param app The app to pin on the TileScreen
     */
    public void queuePinTile(App app) {
        var lowestPoint = _tiles.stream().mapToInt(t -> t.getPosition().y() + t.getSize().height()).max().orElse(0);
        _tileCommands.add(() -> {
            var tile = createTile(app.name(), new Position<>(0, lowestPoint), new Size<>(2, 2), app);
            _tiles.add(tile);
            persistTiles();
        });
    }

    /**
     * Removes and disposes tile.
     * Execution is delayed until calling {@link #executeCommands()}.
     * Calls to this method can be off main-thread.
     * To avoid crashes this method puts its calls into a queue.
     * Execution of these commands must be fired on the main thread using {@link #executeCommands()}
     *
     * NOTE: this has to be put in the command queue because it disposes the unpinned tile
     * @param packageName The name of the package the tile corresponds to
     */
    public void queueUnpinTile(String packageName) {
        var tile = _tiles.stream()
                .filter(t -> t.getPackageName().equals(packageName))
                .findFirst();
        tile.ifPresent(t -> _tileCommands.add(() -> {
            _tiles.remove(t);
            t.dispose();
            notifySubscribers();
            compactGrid();
            persistTiles();
        }));
    }

    public void resizeTile(Tile tile) {
        // 4x2 => 2x2 => 1x1 => 4x2
        var sizeIndex = tileSizes.indexOf(new Size<>(tile.getSize().width(), tile.getSize().height()));
        var nextIndex = (sizeIndex + 1) % tileSizes.size();
        var nextSize = tileSizes.get(nextIndex);
        tile.setSize(nextSize);
        persistTiles();
    }

    public List<Tile> getTiles() {
        return _tiles;
    }

    public List<Tile> loadPersistedTiles() {

        var tiles = new ArrayList<Tile>();
        try {
            var prefs = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            var tilesJson = prefs.getString(TILES, null);
            if (tilesJson == null) {
                return List.of();
            }

            var jsonArray = new JSONArray(tilesJson);
            for (var i = 0; i < jsonArray.length(); i++) {
                var obj = jsonArray.getJSONObject(i);
                var x = obj.getInt("x");
                var y = obj.getInt("y");
                var colSpan = obj.getInt("colSpan");
                var rowSpan = obj.getInt("rowSpan");
                var title = obj.getString("title");
                App app;
                if (obj.has("packageName")) {
                    var packageName = obj.getString("packageName");
                    if (packageName.startsWith("launcher:")) {
                        app = _internalAppsService.getInternalApp(packageName);
                    } else {
                        var intent = _context.getPackageManager().getLaunchIntentForPackage(packageName);
                        if (intent == null) { // the package was uninstalled
                            Log.w("loadPersistedTiles", "Could not find package: " + packageName);
                            continue;
                        }
                        var icon = _context.getPackageManager().getActivityIcon(intent);
                        app = new App(title, packageName, icon, () -> _context.startActivity(intent));
                    }
                    tiles.add(createTile(title, new Position<>(x, y), new Size<>(colSpan, rowSpan), app));
                }
            }

            return tiles;
        } catch (JSONException | PackageManager.NameNotFoundException e) {
            Log.e("loadPersistedTiles", Objects.requireNonNull(e.getMessage()));
            return List.of();
        }
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

    private Tile createTile(String title, Position<Integer> position, Size<Integer> size, App app) {
        if (app.packageName().equalsIgnoreCase("launcher:clock")) {
            return new Tile(position,
                    size,
                    title,
                    app,
                    _settingsService.getAccentColor().color(),
                    new ClockTileContent(_context));
        }

        return new Tile(position,
                size,
                title,
                app,
                _settingsService.getAccentColor().color(),
                new StaticTileContent());
    }

    public void persistTiles() {
        try {
            var tileArray = new JSONArray();
            for (var tile : _tiles) {
                var tileJson = new JSONObject();
                tileJson.put("x", tile.getPosition().x());
                tileJson.put("y", tile.getPosition().y());
                tileJson.put("colSpan", tile.getSize().width());
                tileJson.put("rowSpan", tile.getSize().height());
                tileJson.put("title", tile.title);
                tileJson.put("packageName", tile.getPackageName());
                tileArray.put(tileJson);
            }

            var prefs = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString(TILES, tileArray.toString())
                    .apply();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Push down a given group of tiles with an offset.
     * The move will be relative to the tiles original position.
     * @param tiles The group of tiles to move together
     * @param offset The offset of the move
     */
    public void pushDownTiles(List<Tile> tiles, int offset) {
        for (var tile: tiles) {
            var oldPos = tile.getPosition();
            tile.setPosition(new Position<>(oldPos.x(), oldPos.y() + offset));
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
                    .filter(t -> t.getPosition().y() > currentRow)
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
        persistTiles();
        notifySubscribers();
    }

    private int calculateGroupLowestPoint(List<Tile> group) {
        return group.stream().mapToInt(t -> t.getPosition().y() + t.getSize().height()).max().orElse(0);
    }

    private boolean isRowEmpty(int row) {
        return _tiles.stream().noneMatch(t -> row >= t.getPosition().y() && row < t.getPosition().y() + t.getSize().height());
    }

    private int getTopOfTheGroup(List<Tile> group) {
        return group.stream().mapToInt(t -> t.getPosition().y()).min().orElse(0);
    }

    public void dispose() {
        _tiles.forEach(Tile::dispose);
        _tiles.clear();
    }

    @Override
    public void changed(AccentColor changed) {
        _tiles.forEach(t -> t.setBgColor(changed.color()));
    }
}
