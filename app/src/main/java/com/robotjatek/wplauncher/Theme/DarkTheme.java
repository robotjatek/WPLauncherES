package com.robotjatek.wplauncher.Theme;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;

public class DarkTheme implements ITheme {

    @Override
    public String name() {
        return "dark";
    }

    @Override
    public int getBgColor() {
        return Colors.BLACK;
    }

    @Override
    public LabelStyle label(TextRole role) {
        return switch (role) {
            case TEXT -> new LabelStyle(64, Typeface.NORMAL, Colors.LIGHT_GRAY, Colors.TRANSPARENT);
            case TITLE -> new LabelStyle(48, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT);
            case SUBTITLE -> new LabelStyle(160, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT);
            case DROPDOWN_LABEL -> new LabelStyle(48, Typeface.NORMAL, Colors.LIGHT_GRAY, Colors.TRANSPARENT);
            case LIST_ITEM -> new LabelStyle(60, Typeface.NORMAL, Colors.LIGHT_GRAY, Colors.TRANSPARENT);
        };
    }

    @Override
    public TextBlockStyle textBlock() {
        return new TextBlockStyle(48, Typeface.NORMAL, Colors.LIGHT_GRAY, Colors.TRANSPARENT);
    }

    @Override
    public ButtonStyle button() {
        return new ButtonStyle(48, Typeface.BOLD, Colors.WHITE, Colors.BLACK, Colors.WHITE);
    }

    @Override
    public LayoutStyle layout() {
        return new LayoutStyle(Colors.BLACK);
    }

    @Override
    public DropdownStyle dropdown() {
        return new DropdownStyle(Colors.BLACK, Colors.WHITE, 48, Typeface.BOLD, Colors.WHITE);
    }
}
