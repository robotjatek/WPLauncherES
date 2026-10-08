package com.robotjatek.wplauncher.Theme;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;

public class LightTheme implements ITheme {

    // TODO: text size DP aware
    private final CursorStyle _cursor = new CursorStyle(Colors.BLACK);
    private final LabelStyle _text = new LabelStyle(64, Typeface.NORMAL, Colors.DARK_GRAY, Colors.TRANSPARENT);
    private final LabelStyle _title = new LabelStyle(64, Typeface.NORMAL, Colors.BLACK, Colors.TRANSPARENT);
    private final LabelStyle _subtitle = new LabelStyle(160, Typeface.NORMAL, Colors.BLACK, Colors.TRANSPARENT);
    private final LabelStyle _caption = new LabelStyle(48, Typeface.NORMAL, Colors.DARK_GRAY, Colors.TRANSPARENT);
    private final LabelStyle _listItem = new LabelStyle(60, Typeface.NORMAL, Colors.DARK_GRAY, Colors.TRANSPARENT);
    private final LabelStyle _pageLink = new LabelStyle(96, Typeface.NORMAL, Colors.BLACK, Colors.TRANSPARENT);
    private final TextBlockStyle _textblock = new TextBlockStyle(48, Typeface.NORMAL, Colors.DARK_GRAY, Colors.TRANSPARENT);
    private final ButtonStyle _button = new ButtonStyle(48, Typeface.BOLD, Colors.BLACK, Colors.WHITE, Colors.BLACK);
    private final InputBoxStyle _inputBox = new InputBoxStyle(48, Typeface.BOLD, Colors.BLACK, Colors.WHITE, Colors.BLACK, Colors.DARK_GRAY, _cursor);
    private final LayoutStyle _layout = new LayoutStyle(Colors.WHITE);
    private final DropdownStyle _dropdown = new DropdownStyle(Colors.WHITE, Colors.BLACK, 48, Typeface.BOLD, Colors.BLACK);
    private final CheckboxStyle _checkbox = new CheckboxStyle(Colors.WHITE, Colors.BLACK, 48, Typeface.NORMAL, Colors.BLACK);
    private final AdornerStyle _adorner = new AdornerStyle(Colors.BLACK);
    private final ButtonStyle _modalButton = new ButtonStyle(48, Typeface.BOLD, Colors.BLACK, Colors.WHITE, Colors.BLACK);
    private final LabelStyle _modalTitle = new LabelStyle(72, Typeface.NORMAL, Colors.BLACK, Colors.TRANSPARENT);
    private final TextBlockStyle _modalDescription = new TextBlockStyle(48, Typeface.NORMAL, Colors.DARK_GRAY, Colors.TRANSPARENT);
    private final LayoutStyle _modalBackground = new LayoutStyle(Colors.WHITE);
    private final ModalStyle _modal = new ModalStyle(_modalBackground, label(TextRole.MODAL_TITLE), _modalDescription, _modalButton);
    private final ContextMenuStyle _contextMenu = new ContextMenuStyle(Colors.BLACK, 48, Typeface.BOLD, Colors.WHITE, Colors.LIGHT_GRAY);

    @Override
    public String name() {
        return "light";
    }

    @Override
    public int getBgColor() {
        return Colors.WHITE;
    }

    @Override
    public boolean isLight() {
        return true;
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
            case MODAL_TITLE -> _modalTitle;
        };
    }

    @Override
    public TextBlockStyle textBlock() {
        return _textblock;
    }

    @Override
    public ButtonStyle button() {
        return _button;
    }

    @Override
    public InputBoxStyle inputBox() {
        return _inputBox;
    }

    @Override
    public LayoutStyle layout() {
        return _layout;
    }

    @Override
    public DropdownStyle dropdown() {
        return _dropdown;
    }

    @Override
    public CheckboxStyle checkbox() {
        return _checkbox;
    }

    @Override
    public AdornerStyle adorner() {
        return _adorner;
    }

    @Override
    public ModalStyle modal() {
        return _modal;
    }

    @Override
    public ContextMenuStyle contextMenu() {
        return _contextMenu;
    }
}
