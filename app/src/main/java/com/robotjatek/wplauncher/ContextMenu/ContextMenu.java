package com.robotjatek.wplauncher.ContextMenu;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContextMenu implements IDrawContext<MenuOption> {

    private static final float ITEM_HEIGHT_PX = 150;
    private final IDrawContext<ContextMenu> _context;
    private final List<MenuOption> _options = new ArrayList<>();
    public Position position;
    private final float[] _modelMatrix = new float[16];

    public ContextMenu(Position position, IDrawContext<ContextMenu> context) {
        _context = context;
        this.position = position;
        this.position = new Position(_context.xOf(this), _context.yOf(this)); // recalculate position after confining the menu into the viewport
        Matrix.setIdentityM(_modelMatrix, 0);
    }

    public void draw(float[] proj, float[] view) {
        var menuHeight = calculateHeight();
        // Currently I only draw the child elementss
        Matrix.translateM(_modelMatrix, 0, _context.xOf(this), _context.yOf(this), 0);
        Matrix.scaleM(_modelMatrix, 0, _context.widthOf(this), menuHeight, 0); // Menu height is based on the number of items
        for (var i = 0; i < _options.size(); i++) {
            // draw each option
            _options.get(i).draw(proj, view);
        }
    }


    public void addOptions(List<MenuOption> options) {
        _options.addAll(options);
    }

    public void dispose() {
        _options.forEach(MenuOption::dispose);
        _options.clear();
    }

    @Override
    public QuadRenderer getRenderer() {
        return _context.getRenderer();
    }

    @Override
    public float xOf(MenuOption item) {
        return _context.xOf(this);
    }

    @Override
    public float yOf(MenuOption item) {
        // Y is based on the index of the option
        var itemId = _options.indexOf(item);
        return _context.yOf(this) + itemId * ITEM_HEIGHT_PX;
    }

    @Override
    public float widthOf(MenuOption item) {
        return _context.widthOf(this);
    }

    @Override
    public float heightOf(MenuOption item) {
        // fixed height
        return ITEM_HEIGHT_PX;
    }

    public float calculateHeight() {
        return _options.size() * ITEM_HEIGHT_PX;
    }

    public void onTap(float x, float y) {
        var tappedItem = getOptionAt(x, y);
        tappedItem.ifPresent(MenuOption::onTap);
    }

    public boolean isTappedOn(float x, float y) {
        return x >= _context.xOf(this) && x <= _context.xOf(this) + _context.widthOf(this) &&
                y >= _context.yOf(this) && y <= _context.yOf(this) + _context.heightOf(this);
    }

    private Optional<MenuOption> getOptionAt(float x, float y) {
        return _options.stream().filter(o -> {
            var left = xOf(o);
            var top = yOf(o);
            var right = left + widthOf(o);
            var bottom = top + heightOf(o);

            return x >= left && x <= right && y >= top && y <= bottom;
        }).findFirst();
    }
}
