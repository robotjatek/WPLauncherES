package com.robotjatek.wplauncher.Theme;

import android.graphics.Color;

public interface ITheme {

    record LabelStyle(int textSize, int typeface, int textColor, int bgColor) {
        public LabelStyle withTextColor(int color) {
            return new LabelStyle(textSize, typeface, color, bgColor);
        }

        public LabelStyle withTypeFace(int typeface) {
            return new LabelStyle(textSize, typeface, textColor, bgColor);
        }

        public LabelStyle withTextSize(int textSize) {
            return new LabelStyle(textSize, typeface, textColor, bgColor);
        }
    }

    record TextBlockStyle(int textSize, int typeface, int textColor, int bgColor) {}

    record ButtonStyle(int textSize, int typeface, int textColor, int bgColor, int borderColor) {}

    record InputBoxStyle(int textSize, int typeface, int textColor, int bgColor, int borderColor, int placeholderColor, CursorStyle cursor) {}

    record LayoutStyle(int bgColor)  {}

    record DropdownStyle(int bgColor, int borderColor, int textSize, int typeface, int textColor) {}

    record CheckboxStyle(int bgColor, int borderColor, int textSize, int typeface, int textColor) {}

    record AdornerStyle(int tint) {}

    record ModalStyle(LayoutStyle bgColor, LabelStyle title, TextBlockStyle messageStyle, ButtonStyle buttonStyle) {}

    record CursorStyle(int color) {}

    record ContextMenuStyle(int bgColor, int textSize, int typeface, int textColor, int disabledTextColor) {}

    enum TextRole {
        TITLE,
        SUBTITLE,
        TEXT,
        CAPTION,
        LIST_ITEM,
        PAGE_LINK,
        MODAL_TITLE
    }

    String name();

    int getBgColor();

    boolean isLight();

    LabelStyle label(TextRole role);

    TextBlockStyle textBlock();

    ButtonStyle button();

    LayoutStyle layout();

    DropdownStyle dropdown();

    CheckboxStyle checkbox();

    InputBoxStyle inputBox();

    AdornerStyle adorner();

    ModalStyle modal();

    ContextMenuStyle contextMenu();

    // TODO: separate listview and listpage styles?
}
