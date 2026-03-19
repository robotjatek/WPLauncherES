package com.robotjatek.wplauncher.AppList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.robotjatek.wplauncher.Components.ContextMenu.ContextMenu;
import com.robotjatek.wplauncher.Components.ContextMenu.ContextMenuDrawContext;
import com.robotjatek.wplauncher.Components.ContextMenu.MenuOption;
import com.robotjatek.wplauncher.Components.List.ListItem;
import com.robotjatek.wplauncher.Components.List.ListView;
import com.robotjatek.wplauncher.InternalApps.Settings.OnChangeListener;
import com.robotjatek.wplauncher.Services.AccentColor;
import com.robotjatek.wplauncher.Services.AppChangeReceiver;
import com.robotjatek.wplauncher.Services.InternalAppsService;
import com.robotjatek.wplauncher.Page;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.SettingsService;
import com.robotjatek.wplauncher.StartPage.IPageNavigator;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.Services.TileService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppList implements Page, OnChangeListener<AccentColor>, AppChangeReceiver.IAppChangeListener {

    private static final int PAGE_PADDING_PX = 60;
    private boolean _disposed = false;
    private int _listWidth;
    private int _viewPortHeight;
    private final Context _context;
    private final TileService _tileService;
    private final ContextMenuDrawContext<App> _contextMenuDrawContext;
    private final IPageNavigator _navigator;
    private final SettingsService _settingsService;
    private final ListView<App> _list;

    public AppList(Context context, IPageNavigator navigator, TileService tileService,
                   InternalAppsService internalAppsService, SettingsService settingsService,
                   AppChangeReceiver appChangeReceiver) {
        _context = context;
        _navigator = navigator;
        _tileService = tileService;
        _settingsService = settingsService;
        _settingsService.subscribe(this);
        _contextMenuDrawContext = new ContextMenuDrawContext<>(_listWidth, _viewPortHeight);
        _list = new ListView<>();
        var internalApps = internalAppsService.getInternalApps().stream();
        var apps = Stream.concat(loadAppList(), internalApps)
                .sorted(Comparator.comparing(App::name, String.CASE_INSENSITIVE_ORDER)).toList();
        _list.addItems(createItems(apps));
        appChangeReceiver.subscribe(this);
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer) {
        _list.draw(delta, projMatrix, viewMatrix, renderer);
    }

    @Override
    public void touchMove(float x, float y) {
        _list.touchMove(x, y);
    }

    @Override
    public void touchStart(float x, float y) {
        _list.touchStart(x, y);
    }

    @Override
    public void touchEnd(float x, float y) {
        _list.touchEnd(x, y);
    }

    public void resetScroll() {
        _list.resetScroll();
    }

    @Override
    public void onSizeChanged(int width, int height) {
        _viewPortHeight = height;
        _listWidth = width - 2 * PAGE_PADDING_PX;
        _contextMenuDrawContext.onResize(_listWidth, height);
        _list.onSizeChanged(width, height);
        _list.setContextMenu(createContextMenu()); // Context menu must be created when the size information is available
    }

    @Override
    public void resetState() {
        _list.resetState();
    }

    private ContextMenu<App> createContextMenu() {
        var menu = new ContextMenu<>(new Position<>(0f, 0f), _contextMenuDrawContext);
        var options = List.of(
                new MenuOption<>("Pin", this::pinApp, menu),
                new MenuOption<>("Uninstall", (a) -> {
                    uninstallApp(a.packageName());
                    _tileService.queueUnpinTile(a.packageName());
                }, menu));
        menu.addOptions(options);
        return menu;
    }

    private Stream<App> loadAppList() {
        var pm = _context.getPackageManager();
        var intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        var activities = pm.queryIntentActivities(intent, 0);

        return activities.stream().map(resolveInfo -> {
            var label = resolveInfo.loadLabel(pm).toString();
            var packageName = resolveInfo.activityInfo.packageName;
            var icon = resolveInfo.loadIcon(pm);
            var launchIntent = pm.getLaunchIntentForPackage(packageName);
            return new App(label, packageName, icon, () -> _context.startActivity(launchIntent));
        });
    }

    private List<ListItem<App>> createItems(List<App> apps) {
        return apps.stream().map(this::createItem)
                .collect(Collectors.toList());
    }

    private ListItem<App> createItem(App app) {
        var bgColor = _settingsService.getAccentColor().color();
        return new ListItem<>(app.name(), app.icon(), app.action(), app, bgColor);
    }

    private void uninstallApp(String packageName) {
        var intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        _context.startActivity(intent);
        _tileService.queueUnpinTile(packageName);
    }

    private void pinApp(App app) {
        _tileService.queuePinTile(app);
        _list.resetScroll();
        _navigator.previousPage();
    }

    @Override
    public boolean isCatchingGestures() {
        return _list.isCatchingGestures();
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _list.dispose();
            _disposed = true;
        }
    }

    @Override
    public void changed(AccentColor changed) {
        _list.getItems().forEach(i -> i.setBgColor(changed.color()));
    }

    @Override
    public void onAppRemove(String packageName) {
        var item = _list.getItems().stream().filter(i -> i.getPayload().packageName().equals(packageName)).findFirst();
        item.ifPresent(_list::removeItem);
    }

    @Override
    public void onAppInstall(App app) {
        var item = createItem(app);
        var items = _list.getItems();
        var insertIndex = 0;
        for (var i = 0; i < items.size(); i++) {
            if (app.name().compareToIgnoreCase(items.get(i).getPayload().name()) < 0) {
                insertIndex = i;
                break;
            }
            insertIndex = i + 1;
        }

        _list.addItem(insertIndex, item);
    }

    @Override
    public void onAppReplace(App app) {
        var item = _list.getItems().stream().filter(i -> i.getPayload().packageName().equals(app.packageName())).findFirst();
        item.ifPresent(i -> {
            i.setLabel(app.name());
            i.setIcon(app.icon());
            i.setOnTap(app.action());
            i.setPayload(app);
        });
    }
}
