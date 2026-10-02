package com.robotjatek.wplauncher.Theme;

public interface ITheme {

    record LabelStyle(int textSize, int typeface, int textColor, int bgColor) {
        public LabelStyle withTextColor(int color) {
            return new LabelStyle(textSize, typeface, color, bgColor);
        }
    }

    record TextBlockStyle(int textSize, int typeface, int textColor, int bgColor) {
        public TextBlockStyle withTextColor(int color) {
            return new TextBlockStyle(textSize, typeface, color, bgColor);
        }
    }

    enum TextRole {
        TITLE,
        SUBTITLE,
        TEXT,
        DROPDOWN_LABEL
    }

    String name();
    int getBgColor();

    LabelStyle label(TextRole role);

    TextBlockStyle textBlock();
}
