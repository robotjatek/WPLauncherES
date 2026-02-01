package com.robotjatek.wplauncher.TileGrid;

import android.app.Notification;
import android.graphics.Typeface;
import android.opengl.Matrix;
import android.service.notification.StatusBarNotification;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.FlexLayout.FlexLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.HorizontalAlign;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.INotificationChangedListener;
import com.robotjatek.wplauncher.Services.NotificationListener;
import com.robotjatek.wplauncher.TileUtil;
import com.robotjatek.wplauncher.VerticalAlign;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

// TODO: use layouts and components instead of hardcoded TileUtil textures
public class StaticTileContent implements ITileContent, INotificationChangedListener {
    private static final int ICON_SIZE_PX = 512;
    private final float[] _modelMatrix = new float[16];
    private int _textureId = -1;
    private int _iconTextureId = -1;
    private int _notificationCountTextureId = -1;
    private boolean _dirty = true;
    private final String _packageName;
    private final Deque<StatusBarNotification> _notifications = new ConcurrentLinkedDeque<>();

    private final FlexLayout _rootLayout;
    private final FlexLayout _titleLayout;
    private final Label _titleLabel;

    // TODO: first impl: bg is still a texture and draw the layout on top of it
    // TODO: bgColor for layout
    public StaticTileContent(App app) {
        _packageName = app.packageName();
        NotificationListener.subscribe(this);
        _rootLayout = new FlexLayout(
                new FlexLayout.JustifyContent(FlexLayout.JustifyContentEnum.START, true),
                FlexLayout.AlignItems.START,
                FlexLayout.Direction.COLUMN);

        _titleLayout = new FlexLayout(
                new FlexLayout.JustifyContent(FlexLayout.JustifyContentEnum.END, false),
                FlexLayout.AlignItems.START,
                FlexLayout.Direction.COLUMN);
         _titleLabel = new Label("", 48, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
        _titleLayout.addChild(_titleLabel);
       // _titleLayout.addChild(new Label("Másik", 48, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT));

        //_rootLayout.addChild(_titleLayout); // TODO: make it happen
        // rootLayout.addChild(iconLayout);
        // TODO: when calling draw on a root layout call draw for all child layouts
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer, Tile tile, Position<Float> position, Size<Float> size) {
        if (_dirty) {
            // TODO: move this to a command buffer and run before rendering a frame
            TileUtil.deleteTexture(_textureId);
            TileUtil.deleteTexture(_iconTextureId);
            _textureId = TileUtil.createTextTexture("", size.width().intValue(), size.height().intValue(),
                    48, Typeface.BOLD, Colors.WHITE, tile.bgColor, HorizontalAlign.LEFT, VerticalAlign.BOTTOM);
            _iconTextureId = BitmapUtil.createTextureFromDrawable(tile.getApp().icon(), ICON_SIZE_PX, ICON_SIZE_PX);

            TileUtil.deleteTexture(_notificationCountTextureId); // Don't show the title when the when the tile is small
            _notificationCountTextureId = TileUtil.createTextTexture(_notifications.size() + "", ICON_SIZE_PX, ICON_SIZE_PX,
                    300, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT, HorizontalAlign.CENTER, VerticalAlign.CENTER);

            _titleLabel.setText(tile.getSize().equals(Tile.SMALL) ? "" : tile.title);
            _titleLayout.onResize(size.width().intValue(), size.height().intValue()); // TODO: ez elég hacky itt
            _dirty = false;
        }

        // TODO: stretch behavior for size
//        var iconLayout = new FlexLayout(
//                new FlexLayout.JustifyContent(FlexLayout.JustifyContentEnum.CENTER, false),
//                FlexLayout.AlignItems.CENTER,
//                FlexLayout.Direction.COLUMN);
        // TODO: add icon


        drawBackground(projMatrix, viewMatrix, renderer, position, size, _textureId);
        _titleLayout.draw(delta, projMatrix, viewMatrix, renderer, position);
     //   drawIcon(projMatrix, viewMatrix, renderer, width, height, x, y);
        if (!_notifications.isEmpty()) {
        //    drawNotificationCount(projMatrix, viewMatrix, renderer, width, height, x, y, tile);
        }
    }

    private void drawIcon(float[] projMatrix, float[] viewMatrix, QuadRenderer renderer, float width, float height, float correctedX, float correctedY) {
        // Center icon, keep aspect ratio on wide tiles
        var iconSize = Math.min(width, height) / 2;
        var iconX = correctedX + (width - iconSize) / 2;
        var iconY = correctedY + (height - iconSize) / 2;
        // Move icon slightly to left when there are notifications
        if (!_notifications.isEmpty()) {
            iconX -= width / 4;
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, iconX, iconY, 0f);
        Matrix.scaleM(_modelMatrix, 0, iconSize, iconSize, 1);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        renderer.draw(projMatrix, _modelMatrix, _iconTextureId);
    }

    private void drawBackground(float[] projMatrix, float[] viewMatrix, QuadRenderer renderer, Position<Float> position, Size<Float> size, int texId) {
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, position.x(), position.y(), 0f);
        Matrix.scaleM(_modelMatrix, 0, size.width(), size.height(), 1);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        renderer.draw(projMatrix, _modelMatrix, texId);
    }

    private void drawNotificationCount(float[] projMatrix, float[] viewMatrix, QuadRenderer renderer, float width, float height, float correctedX, float correctedY, Tile tile) {
        var size = Math.min(width, height) / 2; // keep aspect ratio on wide tile
        var offset = tile.getSize().equals(Tile.WIDE) ? 0 : width / 10;
        var x = correctedX + (width - size) / 2 + offset;
        var y = correctedY + (height - size) / 2 + height / 7;

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0f);
        Matrix.scaleM(_modelMatrix, 0, size, size, 1);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        renderer.draw(projMatrix, _modelMatrix, _notificationCountTextureId);
    }

    public void dispose() {
        TileUtil.deleteTexture(_textureId);
        TileUtil.deleteTexture(_iconTextureId);
        TileUtil.deleteTexture(_notificationCountTextureId);
        _textureId = -1;
        _iconTextureId = -1;
        _notificationCountTextureId = -1;

        _titleLayout.dispose();
    }

    @Override
    public void forceRedraw() {
        _dirty = true;
    }

    @Override
    public void onChange() {
        // TODO: this now runs on every notification for ALL listeners
        var notifications = NotificationListener.getInstance().getNotifications(_packageName);
        _notifications.clear();
        for (var n : notifications) {
            var flags = n.getNotification().flags;
            var isGroup = (flags & Notification.FLAG_GROUP_SUMMARY) != 0;
            if (!isGroup) {
                _notifications.add(n);
            }

//            var title = n.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE);
//            var text = n.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT);
//            var messages = n.getNotification().extras.getCharSequence(Notification.EXTRA_MESSAGES);
//            var summary = n.getNotification().extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT);
        }
        _dirty = true;
    }
}
