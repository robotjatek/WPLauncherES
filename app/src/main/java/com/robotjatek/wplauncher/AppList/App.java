package com.robotjatek.wplauncher.AppList;

import android.graphics.drawable.Drawable;

public record App(String name, String packageName, Drawable icon, Runnable action, boolean isSystemApp) {} // TODO: do i really need to keep a reference to the icon here?
