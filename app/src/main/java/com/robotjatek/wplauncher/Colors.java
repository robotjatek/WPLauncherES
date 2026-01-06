package com.robotjatek.wplauncher;

import android.graphics.Color;

import com.robotjatek.wplauncher.Services.AccentColor;

import java.util.List;

public class Colors {
    public static final int WHITE = 0xffffffff;
    public static final int BLACK = 0xff000000;
    public static final int TRANSPARENT = 0;
    public static final int LIGHT_GRAY = 0xffbbbbbb;
    public static final int CONTEXT_MENU_GRAY = 0xff222222;
    public static final int MIDNIGHT_BLUE = Color.argb(255, 26, 26, 46);
    public static final int TILE_AMBER = Color.argb(255, 240, 163, 10);
    public static final int TILE_BROWN = Color.argb(255, 130, 90, 44);
    public static final int TILE_COBALT = Color.argb(255, 0, 80, 239);
    public static final int TILE_CRIMSON = Color.argb(255, 162, 0, 37);
    public static final int TILE_CYAN = Color.argb(255, 27, 161, 226);
    public static final int TILE_EMERALD = Color.argb(255, 0, 138, 0);
    public static final int TILE_GREEN = Color.argb(255, 96, 169, 23);
    public static final int TILE_INDIGO = Color.argb(255, 106, 0, 255);
    public static final int TILE_LIME = Color.argb(255, 164, 196, 0);
    public static final int TILE_MAGENTA = Color.argb(255, 216, 0, 115);
    public static final int TILE_MAUVE = Color.argb(255, 118, 96, 138);
    public static final int TILE_OLIVE = Color.argb(255, 109, 135, 100);
    public static final int TILE_ORANGE = Color.argb(255, 250, 104, 0);
    public static final int TILE_PINK = Color.argb(255, 244, 114, 208);
    public static final int TILE_RED = Color.argb(255, 229, 20, 0);
    public static final int TILE_STEEL = Color.argb(255, 100, 118, 135);
    public static final int TILE_TAUPE = Color.argb(255, 135, 121, 78);
    public static final int TILE_TEAL = Color.argb(255, 0, 117, 169);
    public static final int TILE_VIOLET = Color.argb(255, 170, 0, 255);
    public static final int TILE_YELLOW = Color.argb(255, 227, 200, 0);

    public static final AccentColor ACCENT_MIDNIGHT = new AccentColor("midnight blue", Colors.MIDNIGHT_BLUE);
    public static final AccentColor ACCENT_AMBER = new AccentColor("amber", Colors.TILE_AMBER);
    public static final AccentColor ACCENT_BROWN = new AccentColor("brown", Colors.TILE_BROWN);
    public static final AccentColor ACCENT_COBALT = new AccentColor("cobalt", Colors.TILE_COBALT);
    public static final AccentColor ACCENT_CRIMSON = new AccentColor("crimson", Colors.TILE_CRIMSON);
    public static final AccentColor ACCENT_CYAN = new AccentColor("cyan", Colors.TILE_CYAN);
    public static final AccentColor ACCENT_EMERALD = new AccentColor("emerald", Colors.TILE_EMERALD);
    public static final AccentColor ACCENT_GREEN = new AccentColor("green", Colors.TILE_GREEN);
    public static final AccentColor ACCENT_INDIGO = new AccentColor("indigo", Colors.TILE_INDIGO);
    public static final AccentColor ACCENT_LIME = new AccentColor("lime", Colors.TILE_LIME);
    public static final AccentColor ACCENT_MAGENTA = new AccentColor("magenta", Colors.TILE_MAGENTA);
    public static final AccentColor ACCENT_MAUVE = new AccentColor("mauve", Colors.TILE_MAUVE);
    public static final AccentColor ACCENT_OLIVE = new AccentColor("olive", Colors.TILE_OLIVE);
    public static final AccentColor ACCENT_ORANGE = new AccentColor("orange", Colors.TILE_ORANGE);
    public static final AccentColor ACCENT_PINK = new AccentColor("pink", Colors.TILE_PINK);
    public static final AccentColor ACCENT_RED = new AccentColor("red", Colors.TILE_RED);
    public static final AccentColor ACCENT_STEEL = new AccentColor("steel", Colors.TILE_STEEL);
    public static final AccentColor ACCENT_TAUPE = new AccentColor("taupe", Colors.TILE_TAUPE);
    public static final AccentColor ACCENT_TEAL = new AccentColor("teal", Colors.TILE_TEAL);
    public static final AccentColor ACCENT_VIOLET = new AccentColor("violet", Colors.TILE_VIOLET);
    public static final AccentColor ACCENT_YELLOW = new AccentColor("yellow", Colors.TILE_YELLOW);

    public static final List<AccentColor> ACCENT_COLORS = List.of(
            ACCENT_MIDNIGHT,
            ACCENT_AMBER,
            ACCENT_BROWN,
            ACCENT_COBALT,
            ACCENT_CRIMSON,
            ACCENT_CYAN,
            ACCENT_EMERALD,
            ACCENT_GREEN,
            ACCENT_INDIGO,
            ACCENT_LIME,
            ACCENT_MAGENTA,
            ACCENT_MAUVE,
            ACCENT_OLIVE,
            ACCENT_ORANGE,
            ACCENT_PINK,
            ACCENT_RED,
            ACCENT_STEEL,
            ACCENT_TAUPE,
            ACCENT_TEAL,
            ACCENT_VIOLET,
            ACCENT_YELLOW);

}
