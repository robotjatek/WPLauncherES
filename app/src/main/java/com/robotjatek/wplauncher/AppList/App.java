package com.robotjatek.wplauncher.AppList;

import android.graphics.drawable.Drawable;

public record App(String name, String packageName, Drawable icon, Runnable action) {}
