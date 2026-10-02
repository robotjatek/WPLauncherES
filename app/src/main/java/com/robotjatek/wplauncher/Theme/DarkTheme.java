package com.robotjatek.wplauncher.Theme;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;

public class DarkTheme implements ITheme {

    private final LabelStyle _text = new LabelStyle(64, Typeface.NORMAL, Colors.LIGHT_GRAY, Colors.TRANSPARENT);
    private final LabelStyle _title = new LabelStyle(64, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT);
    private final LabelStyle _subtitle = new LabelStyle(160, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT);
    private final LabelStyle _caption = new LabelStyle(48, Typeface.NORMAL, Colors.LIGHT_GRAY, Colors.TRANSPARENT);
    private final LabelStyle _listItem = new LabelStyle(60, Typeface.NORMAL, Colors.LIGHT_GRAY, Colors.TRANSPARENT);
    private final LabelStyle _pageLink = new LabelStyle(96, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT);


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
            case TEXT -> _text;
            case TITLE -> _title;
            case SUBTITLE -> _subtitle;
            case CAPTION -> _caption;
            case LIST_ITEM -> _listItem;
            case PAGE_LINK -> _pageLink;
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

    @Override
    public CheckboxStyle checkbox() {
        return new CheckboxStyle(Colors.BLACK, Colors.WHITE, 48, Typeface.NORMAL, Colors.WHITE);
    }
}
