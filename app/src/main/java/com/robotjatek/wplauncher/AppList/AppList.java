package com.robotjatek.wplauncher.AppList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.AppList.States.IdleState;
import com.robotjatek.wplauncher.AppList.States.ContextMenuState;
import com.robotjatek.wplauncher.AppList.States.ScrollState;
import com.robotjatek.wplauncher.AppList.States.TappedState;
import com.robotjatek.wplauncher.AppList.States.TouchingState;
import com.robotjatek.wplauncher.ContextMenu.ContextMenu;
import com.robotjatek.wplauncher.ContextMenu.ContextMenuDrawContext;
import com.robotjatek.wplauncher.ContextMenu.MenuOption;
import com.robotjatek.wplauncher.Page;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.ScrollController;
import com.robotjatek.wplauncher.Shader;
import com.robotjatek.wplauncher.StartPage.IPageNavigator;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

// TODO: a scrollingot kiszervezni egy külön (base?)osztályba -- manual scroll.onTouch* calls are error prone
// TODO: meg a view alapú render logicot is...
public class AppList implements Page, IItemListContainer<App> {

    public IState IDLE_STATE() {
        return new IdleState(this);
    }

    public IState TOUCHING_STATE(float x, float y) {
        return new TouchingState(this, x, y);
    }

    public IState TAPPED_STATE(float y) {
        return new TappedState(this, y);
    }

    public IState CONTEXT_MENU_STATE(float x, float y) {
        return new ContextMenuState(this, x, y);
    }

    public IState SCROLL_STATE(float y) {
        return new ScrollState(this, y);
    }

    private IState _state = IDLE_STATE();

    private final Queue<Runnable> _commandQueue = new ConcurrentLinkedQueue<>();
    private final float[] scrollMatrix = new float[16]; // scroll position transformation
    private final Shader _shader = new Shader("", "");
    private final QuadRenderer _renderer = new QuadRenderer(_shader);
    private final ScrollController _scroll = new ScrollController();
    private List<ListItem<App>> _items = new ArrayList<>();
    public static final int TOP_MARGIN_PX = 152;
    public static final int ITEM_HEIGHT_PX = 128;
    public static final int ITEM_GAP_PX = 5;
    private static final int PAGE_PADDING_PX = 24;
    private int _listWidth;
    private int _viewPortHeight;
    private ContextMenu _contextMenu;
    private final List<App> _apps;
    private final Context _context;
    private final TileService _tileService;
    private final ListItemDrawContext<App, AppList> _listItemDrawContext;
    private final ContextMenuDrawContext _contextMenuDrawContext;
    private final IPageNavigator _navigator;

    public AppList(Context context, IPageNavigator navigator, TileService tileService) {
        // TODO: reload results after app install/uninstall
        _context = context;
        _navigator = navigator;
        _tileService = tileService;
        _listItemDrawContext = new ListItemDrawContext<>(PAGE_PADDING_PX, ITEM_HEIGHT_PX, ITEM_GAP_PX, this, _renderer);
        _contextMenuDrawContext = new ContextMenuDrawContext(_listWidth, _viewPortHeight, _renderer);
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
        }).sorted(Comparator.comparing(App::name, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix) {
        _state.update(delta);
        executeCommands();

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
        _state.handleMove(x, y);
    }

    @Override
    public void touchStart(float x, float y) {
        _state.handleTouchStart(x, y);
    }

    @Override
    public void touchEnd(float x, float y) {
        _state.handleTouchEnd(x, y);
    }

    @Override
    public void handleLongPress(float x, float y) {
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    public ScrollController getScroll() {
        return this._scroll;
    }

    public ContextMenu openContextMenu(float x, float y, App app) {
            _contextMenu = createAppListContextMenu(
            new Position(x, y),
            () -> pinApp(app),
            () ->  {
                uninstallApp(app.packageName());
                _tileService.unpinTile(app.packageName());
            });

            return _contextMenu;
    }

    public void closeContextMenu() {
        if (_contextMenu != null) {
            final var oldMenu = _contextMenu;
            _commandQueue.add(oldMenu::dispose);
            _contextMenu = null;
        }
    }

    @Override
    public void onSizeChanged(int width, int height) {
        _viewPortHeight = height;
        _listWidth = width - 2 * PAGE_PADDING_PX;
        _listItemDrawContext.onResize(_listWidth);
        _contextMenuDrawContext.onResize(_listWidth, height);

        _items.forEach(ListItem::dispose);
        _items = createItems(_apps);

        var contentHeight = _items.size() * (ITEM_HEIGHT_PX + ITEM_GAP_PX);

        if (contentHeight <= height) {
            // content fits on screen, don't allow scrolling
            _scroll.setBounds(0, 0);
        } else {
            var minScroll = -(contentHeight - height) - TOP_MARGIN_PX;
            _scroll.setBounds(minScroll, 0);
        }
    }

    private List<ListItem<App>> createItems(List<App> apps) {
        return apps.stream().map(a -> new ListItem<>(a.name(), a.icon(), _listItemDrawContext, a.action(), a))
                .collect(Collectors.toList());
    }

    public List<ListItem<App>> getItems() {
        return _items;
    }

    /**
     * Creates a new context menu instance for the applist. It position is always absolute screen position
     * @param position Absolute screen position
     * @param pin The action to run when pinning an application
     * @param uninstall The action to run when tapping uninstall
     * @return The context menu on applist with Pin and uninstall options
     */
    private ContextMenu createAppListContextMenu(Position position, Runnable pin, Runnable uninstall) {
        var menu = new ContextMenu(position, _contextMenuDrawContext);
        var options = List.of(
                new MenuOption("Pin", pin, menu),
                new MenuOption("Uninstall", uninstall, menu));
        menu.addOptions(options);
        return menu;
    }

    private void uninstallApp(String packageName) {
        var intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        _context.startActivity(intent);
        _tileService.unpinTile(packageName);
    }

    private void pinApp(App app) {
        _tileService.pinTile(app);
        _navigator.previousPage();
    }

    @Override
    public boolean isCatchingGestures() {
        return _contextMenu != null;
    }

    private void executeCommands() {
        Runnable command;
        while ((command = _commandQueue.poll()) != null) {
            command.run();
        }
    }

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
