package com.robotjatek.wplauncher.Theme;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;

public class LightTheme implements ITheme {

    @Override
    public String name() {
        return "light";
    }

    @Override
    public int getBgColor() {
        return Colors.WHITE;
    }

    @Override
    public LabelStyle label(TextRole role) {
        return switch (role) {
            case TEXT -> new LabelStyle(64, Typeface.NORMAL, Colors.DARK_GRAY, Colors.TRANSPARENT);
            case TITLE -> new LabelStyle(48, Typeface.NORMAL, Colors.BLACK, Colors.TRANSPARENT);
            case SUBTITLE -> new LabelStyle(160, Typeface.NORMAL, Colors.BLACK, Colors.TRANSPARENT);
            case DROPDOWN_LABEL -> new LabelStyle(48, Typeface.NORMAL, Colors.DARK_GRAY, Colors.TRANSPARENT);
        };
    }

    @Override
    public TextBlockStyle textBlock() {
        return new TextBlockStyle(48, Typeface.NORMAL, Colors.DARK_GRAY, Colors.TRANSPARENT);
    }

    @Override
    public ButtonStyle button() {
        return new ButtonStyle(48, Typeface.BOLD, Colors.BLACK, Colors.WHITE, Colors.BLACK);
    }

    @Override
    public LayoutStyle layout() {
        return new LayoutStyle(Colors.WHITE);
    }
}
