package com.robotjatek.wplauncher.ContextMenu;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileUtil;

import java.util.ArrayList;
import java.util.List;

public class ContextMenu implements IMenuItemDrawContext {

    private final IContextMenuDrawContext _context;
    private final List<MenuOption> _options = new ArrayList<>();
    public final Position position;
    private final float[] _modelMatrix = new float[16];
    private final int _bgId;

    public ContextMenu(Position position, com.robotjatek.wplauncher.ContextMenu.IContextMenuDrawContext context) {
        _context = context;
        this.position = position;
        Matrix.setIdentityM(_modelMatrix, 0);
        _bgId = TileUtil.createTextTexture("", 1, 1, 0, 0xff000000); // TODO: no text, bg only version
    }

    private static final float ITEM_HEIGHT_PX = 100;

    public void draw(float delta, float[] proj, float[] view) {
        var menuHeight = calculateHeight();
        // draw bg
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, _context.x(this), _context.y(this), 0); // TODO: x,y should consider the w and h of the menu so it resides in the confines of the parent
        Matrix.scaleM(_modelMatrix, 0, _context.width(this), menuHeight, 0); // Menu height is based on the number of items
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        _context.getRenderer().draw(proj, _modelMatrix, _bgId);

        // Draw options on top of the background
        Matrix.translateM(_modelMatrix, 0, _context.x(this), _context.y(this), 0);
        Matrix.scaleM(_modelMatrix, 0, _context.width(this), menuHeight, 0);
        for (var i = 0; i < _options.size(); i++) {
            // draw each option
            _options.get(i).draw(delta, proj, view);
        }
    }

    public static ContextMenu CreateAppListContextMenu(Position position, com.robotjatek.wplauncher.ContextMenu.IContextMenuDrawContext context) {
        var menu = new ContextMenu(position, context);
        var options = List.of(
                new MenuOption("Pin", () -> {
                }, menu),
                new MenuOption("Uninstall", () -> {
                }, menu));
        menu.addOptions(options);
        return menu;
    }

    public void addOptions(List<MenuOption> options) {
        _options.addAll(options);
    }

    public void dispose() {
    }

    @Override
    public QuadRenderer getRenderer() {
        return _context.getRenderer();
    }

    @Override
    public float xOf(MenuOption item) {
        return _context.x(this);
    }

    @Override
    public float yOf(MenuOption item) {
        // Y is based on the index of the option
        var itemId = _options.indexOf(item);
        return _context.y(this) + itemId * ITEM_HEIGHT_PX;
    }

    @Override
    public float widthOf(MenuOption item) {
        return _context.width(this);
    }

    @Override
    public float heightOf(MenuOption item) {
        // fixed height
        return ITEM_HEIGHT_PX;
    }

    public float calculateHeight() {
        return _options.size() * ITEM_HEIGHT_PX;
    }
}
