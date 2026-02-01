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
import com.robotjatek.wplauncher.Services.InternalAppsService;
import com.robotjatek.wplauncher.Page;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.SettingsService;
import com.robotjatek.wplauncher.Shader;
import com.robotjatek.wplauncher.StartPage.IPageNavigator;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.Services.TileService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppList implements Page, OnChangeListener<AccentColor> {

    private static final int PAGE_PADDING_PX = 60;
    private int _listWidth;
    private int _viewPortHeight;
    private final Context _context;
    private final TileService _tileService;
    private final ContextMenuDrawContext<App> _contextMenuDrawContext;
    private final IPageNavigator _navigator;
    private final InternalAppsService _internalAppsService;
    private final SettingsService _settingsService;
    private final ListView<App> _list;

    public AppList(Context context, IPageNavigator navigator, TileService tileService,
                   InternalAppsService internalAppsService, SettingsService settingsService) {
        // TODO: reload results after app install/uninstall
        _context = context;
        _navigator = navigator;
        _tileService = tileService;
        _settingsService = settingsService;
        _settingsService.subscribe(this);
        _contextMenuDrawContext = new ContextMenuDrawContext<>(_listWidth, _viewPortHeight);
        _internalAppsService = internalAppsService;
        _list = new ListView<>();
        var apps = loadAppList();
        var newItems = createItems(apps);
        _list.addItems(newItems);
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

    private List<App> loadAppList() {
        var internalApps = _internalAppsService.getInternalApps().stream();
        var pm = _context.getPackageManager();
        var intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        var activities = pm.queryIntentActivities(intent, 0);
        var installedApps = activities.stream().map(resolveInfo -> {
            var label = resolveInfo.loadLabel(pm).toString();
            var packageName = resolveInfo.activityInfo.packageName;
            var icon = resolveInfo.loadIcon(pm);
            var launchIntent = pm.getLaunchIntentForPackage(packageName);
            return new App(label, packageName, icon, () -> _context.startActivity(launchIntent));
        }).sorted(Comparator.comparing(App::name, String.CASE_INSENSITIVE_ORDER));

        return Stream.concat(internalApps, installedApps).toList();
    }

    private List<ListItem<App>> createItems(List<App> apps) {
        var bgColor = _settingsService.getAccentColor().color();
        return apps.stream().map(a -> new ListItem<>(a.name(), a.icon(), a.action(), a, bgColor))
                .collect(Collectors.toList());
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
        _list.dispose();
    }

    @Override
    public void changed(AccentColor changed) {
        _list.getItems().forEach(i -> i.setBgColor(changed.color()));
    }
}
