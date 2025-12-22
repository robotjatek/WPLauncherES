package com.robotjatek.wplauncher.AppList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.ContextMenu.ContextMenu;
import com.robotjatek.wplauncher.ContextMenu.IContextMenuDrawContext;
import com.robotjatek.wplauncher.ContextMenu.MenuOption;
import com.robotjatek.wplauncher.Page;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.ScrollController;
import com.robotjatek.wplauncher.Shader;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO: a scrollingot kiszervezni egy külön (base?)osztályba -- manual scroll.onTouch* calls are error prone
// TODO: meg a view alapú render logicot is...
public class AppList implements Page, IListItemDrawContext<App>, IContextMenuDrawContext {

    private final float[] scrollMatrix = new float[16]; // scroll position transformation

    private final Shader _shader = new Shader("", "");
    private final QuadRenderer _renderer = new QuadRenderer(_shader);
    private final ScrollController _scroll = new ScrollController();

    private List<ListItem<App>> _items = new ArrayList<>();

    private static final int TOP_MARGIN_PX = 152;
    private static final int ITEM_HEIGHT_PX = 128;
    private static final int ITEM_GAP_PX = 5;
    private static final int PAGE_PADDING_PX = 24;
    private int _listWidth;
    private int _viewPortHeight;
    private boolean _isTouching = false;

    private ContextMenu _contextMenu;
    private final List<App> _apps;
    private final Context _context;
    private final TileService _tileService;

    public AppList(Context context, TileService tileService) {
        // TODO: extract method
        // TODO: cache results
        // TODO: reload results after app install/uninstall
        _context = context;
        _tileService = tileService;
        var pm = _context.getPackageManager();
        var intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        var activities = pm.queryIntentActivities(intent, 0);
        _apps = activities.stream().map(resolveInfo -> {
            var label = resolveInfo.loadLabel(pm).toString();
            var packageName = resolveInfo.activityInfo.packageName;
            var icon = resolveInfo.loadIcon(pm);
            var launchIntent = pm.getLaunchIntentForPackage(packageName);
            return new App(label, packageName, icon, () -> context.startActivity(launchIntent)); // TODO: internal apps
        }).sorted(Comparator.comparing(App::name)).toList();
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix) {
        _scroll.update(delta);

        Matrix.setIdentityM(scrollMatrix, 0);
        Matrix.translateM(scrollMatrix, 0, 0, _scroll.getScrollOffset() + TOP_MARGIN_PX, 0);
        Matrix.multiplyMM(scrollMatrix, 0, scrollMatrix, 0, viewMatrix, 0);

        for (var i = 0; i < _items.size(); i++) {
            var item = _items.get(i);
            item.update();
            item.draw(projMatrix, scrollMatrix);
        }

        // Draw the context menu last so it shows up above everything else
        if (_contextMenu != null) {
            _contextMenu.draw(projMatrix, viewMatrix);
        }
    }

    @Override
    public void touchMove(float x, float y) {
        _scroll.onTouchMove(y);
    }

    @Override
    public void touchStart(float x, float y) {
        _scroll.onTouchStart(y);
        _isTouching = true;
    }

    @Override
    public void touchEnd(float x, float y) {
        _scroll.onTouchEnd();

        if (_isTouching) {
            handleTap(x, y);
            _isTouching = false;
        }
    }

    @Override
    public void handleLongPress(float x, float y) {
        var tappedItem = getItemAt(y);
        tappedItem.ifPresent(i -> {
            if (_contextMenu != null) {
                _contextMenu.dispose();
                _contextMenu = null;
            }
            _contextMenu = createAppListContextMenu(
                    new Position(x, y),
                    () -> pinApp(i.getPayload()),
                    () ->  {
                        uninstallApp(i.getPayload().packageName());
                        _tileService.unpinTile(i.getPayload());
                    });
        });
    }

    @Override
    public void onSizeChanged(int width, int height) {
        _viewPortHeight = height;
        _listWidth = width - 2 * PAGE_PADDING_PX;
        _items.forEach(ListItem::dispose);
        _items = createItems(_apps, _listWidth);

        var contentHeight = _items.size() * (ITEM_HEIGHT_PX + ITEM_GAP_PX);

        if (contentHeight <= height) {
            // content fits on screen, don't allow scrolling
            _scroll.setBounds(0, 0);
        } else {
            var minScroll = -(contentHeight - height) - TOP_MARGIN_PX;
            _scroll.setBounds(minScroll, 0);
        }
    }

    private List<ListItem<App>> createItems(List<App> apps, int width) {
        return apps.stream().map(a -> new ListItem<>(a.name(), width, ITEM_HEIGHT_PX, this, a.action(), a))
                .collect(Collectors.toList());
    }

    private void handleTap(float x, float y) {
        if (_contextMenu != null) {
            if (_contextMenu.isTappedOn(x, y)) {
                _contextMenu.onTap(x, y);
            }

            _contextMenu.dispose();
            _contextMenu = null; // TODO: áttérni state machinere, mert a childcontrol state a parentben áthív ide touchendkor
            return;
        }

        var tappedItem = getItemAt(y);
        tappedItem.ifPresent(ListItem::onTap);
    }

    private Optional<ListItem<App>> getItemAt(float y) {
        var adjustedY = y - (_scroll.getScrollOffset() + TOP_MARGIN_PX);
        var index = (int)(adjustedY / (ITEM_HEIGHT_PX + ITEM_GAP_PX));
        if (index >= 0 && index < _items.size()) {
            return Optional.of(_items.get(index));
        }

        return Optional.empty();
    }

    @Override
    public QuadRenderer getRenderer() {
        return _renderer;
    }

    // TODO: composition over inheritance?
    @Override
    public float xOf(ContextMenu menu) {
        // confine to screen
        return Math.clamp(menu.position.x(), 0, _listWidth - this.widthOf(menu));
    }

    @Override
    public float yOf(ContextMenu menu) {
        // confine to screen
        return Math.clamp(menu.position.y(), 0, _viewPortHeight - this.heightOf(menu));
    }

    @Override
    public float widthOf(ContextMenu menu) {
        return 400;
    }

    @Override
    public float heightOf(ContextMenu menu) {
        return menu.calculateHeight();
    }

    /**
     * Creates a new context menu instance for the applist. It position is always absolute screen position
     * @param position Absolute screen position
     * @param pin The action to run when pinning an application
     * @param uninstall The action to run when tapping uninstall
     * @return The context menu on applist with Pin and uninstall options
     */
    private ContextMenu createAppListContextMenu(Position position, Runnable pin, Runnable uninstall) {
        var menu = new ContextMenu(position, this);
        var options = List.of(
                new MenuOption("Pin", pin, menu),
                new MenuOption("Uninstall", uninstall, menu));
        menu.addOptions(options);
        return menu;
    }

    @Override
    public float xOf(ListItem<App> item) {
        return PAGE_PADDING_PX;
    }

    @Override
    public float yOf(ListItem<App> item) {
        var index = _items.indexOf(item);
        if (index == -1) {
            throw new RuntimeException("List item not found");
        }
        return index * (ITEM_HEIGHT_PX + ITEM_GAP_PX);
    }

    @Override
    public float widthOf(ListItem<App> item) {
        return _listWidth - PAGE_PADDING_PX;
    }

    @Override
    public float heightOf(ListItem<App> item) {
        return ITEM_HEIGHT_PX;
    }

    private void uninstallApp(String packageName) {
        var intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        _context.startActivity(intent);
        // TODO: remove from tilelist if pinned
    }

    private void pinApp(App app) {
        _tileService.pinTile(app);
    }

    // TODO: uncomment after state machine implementation
//    @Override
//    public boolean isCatchingGestures() {
//        return _contextMenu != null;
//    }

    @Override
    public void dispose() {
        _items.forEach(ListItem::dispose);
        _items.clear();
        _shader.delete();
        _renderer.dispose();
        if (_contextMenu != null) {
            _contextMenu.dispose();
        }
    }
}
