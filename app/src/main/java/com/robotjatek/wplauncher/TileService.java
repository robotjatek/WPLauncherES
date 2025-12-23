package com.robotjatek.wplauncher;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.Tile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class TileService {

    private static final String PREF_NAME = "WPLAUNCHER";
    private static final String TILES = "TILES";
    private final List<ITileListChangedListener> _subscribers = new ArrayList<>();
    private final Queue<Runnable> _tileCommands = new ConcurrentLinkedQueue<>();
    private final List<Tile> _tiles = new ArrayList<>();
    private final Context _context;

    public TileService(Context context) {
        _context = context;
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
    public void pinTile(App app) {
        var lowestPoint = _tiles.stream().mapToInt(t -> t.y + t.rowSpan).max().orElse(0);
        _tileCommands.add(() -> {
            var tile = createTile(app.name(), new Position(0, lowestPoint), app);
            _tiles.add(tile);
            persistTiles();
        });
    }

    public void unpinTile(String packageName) {
        var tile = _tiles.stream()
                .filter(t -> t.getPackageName().equals(packageName))
                .findFirst();
        tile.ifPresent(t -> _tileCommands.add(() -> {
            _tiles.remove(t);
            t.dispose();
            persistTiles();
        }));
    }

    public List<Tile> getTiles() {
        return _tiles;
    }

    public List<Tile> loadPersistedTiles() {

        var tiles = new ArrayList<Tile>();
        try {
            var prefs = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String tilesJson = prefs.getString(TILES, null);
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

                App app = null;
                if (obj.has("packageName")) {
                    var packageName = obj.getString("packageName");
                    var intent = _context.getPackageManager().getLaunchIntentForPackage(packageName);
                    if (intent == null) { // the package was uninstalled
                        Log.w("loadPersistedTiles", "Could not find package: " + packageName);
                        continue;
                    }
                    var icon = _context.getPackageManager().getApplicationIcon(packageName);
                    app = new App(title, packageName, icon, () -> _context.startActivity(intent));
                }

                tiles.add(new Tile(x, y, colSpan, rowSpan, title, app));
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

    private Tile createTile(String title, Position position, App app) {
        return new Tile((int)position.x(), (int)position.y(), 2, 2, title, app);
    }

    public void persistTiles() {
        try {
            var tileArray = new JSONArray();
            for (var tile : _tiles) {
                var tileJson = new JSONObject();
                tileJson.put("x", tile.x);
                tileJson.put("y", tile.y);
                tileJson.put("colSpan", tile.colSpan);
                tileJson.put("rowSpan", tile.rowSpan);
                tileJson.put("title", tile.title);
                if (tile.getPackageName() != null && !tile.getPackageName().isEmpty()) {
                    tileJson.put("packageName", tile.getPackageName());
                }
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

    public void dispose() {
        _tiles.forEach(Tile::dispose);
        _tiles.clear();
    }
}
