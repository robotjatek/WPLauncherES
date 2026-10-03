package com.robotjatek.wplauncher.Theme;

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

    record TextBlockStyle(int textSize, int typeface, int textColor, int bgColor) {
        public TextBlockStyle withTextColor(int color) {
            return new TextBlockStyle(textSize, typeface, color, bgColor);
        }
    }

    record ButtonStyle(int textSize, int typeface, int textColor, int bgColor, int borderColor) {
    }

    record InputBoxStyle(int textSize, int typeface, int textColor, int bgColor, int borderColor, int placeholderColor) {}

    record LayoutStyle(int bgColor)  {}

    record DropdownStyle(int bgColor, int borderColor, int textSize, int typeface, int textColor) {}

    record CheckboxStyle(int bgColor, int borderColor, int textSize, int typeface, int textColor) {}

    enum TextRole {
        TITLE,
        SUBTITLE,
        TEXT,
        CAPTION,
        LIST_ITEM,
        PAGE_LINK
    }

    String name();
    int getBgColor();

    LabelStyle label(TextRole role);

    TextBlockStyle textBlock();

    ButtonStyle button();

    LayoutStyle layout();

    DropdownStyle dropdown();

    CheckboxStyle checkbox();

    InputBoxStyle inputBox();
    // TODO: separate listview and listpage styles?
}
