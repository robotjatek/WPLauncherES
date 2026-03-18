package com.robotjatek.wplauncher.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.robotjatek.wplauncher.AppList.App;

import java.util.ArrayList;
import java.util.List;

public class AppChangeReceiver extends BroadcastReceiver {

    private final List<IAppChangeListener> _listeners = new ArrayList<>();

    public interface IAppChangeListener {
        default void onAppRemove(String packageName) {}
        default void onAppInstall(App app) {}
        void onAppReplace(App app);
    }

    public void subscribe(IAppChangeListener listener) {
        _listeners.add(listener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        var action = intent.getAction();
        var packageName = intent.getData() != null ? intent.getData().getSchemeSpecificPart() : null;

        if (packageName == null || action == null) {
            return;
        }

        switch (action) {
            case Intent.ACTION_PACKAGE_ADDED -> {
                var replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                if (replacing) {
                    try {
                        var app = loadAppByPackageName(context, packageName);
                        _listeners.forEach(l -> l.onAppReplace(app));
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e("AppChangeReceiver", "Installed app not found: " + packageName);
                    }

                } else {
                    try {
                        var app = loadAppByPackageName(context, packageName);
                        _listeners.forEach(l -> l.onAppInstall(app));
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e("AppChangeReceiver", "Installed app not found: " + packageName);
                    }
                }

            }
            case Intent.ACTION_PACKAGE_REMOVED -> {
                var replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                if (!replacing) {
                    _listeners.forEach(l -> l.onAppRemove(packageName));
                }
            }
            case Intent.ACTION_PACKAGE_REPLACED -> {
                try {
                    var app = loadAppByPackageName(context, packageName);
                    _listeners.forEach(l -> l.onAppReplace(app));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("AppChangeReceiver", "Installed app not found: " + packageName);
                }
            }
        }
    }

    private App loadAppByPackageName(Context context, String packageName) throws PackageManager.NameNotFoundException {
        var pm = context.getPackageManager();
        var info = pm.getApplicationInfo(packageName, 0);
        var name = pm.getApplicationLabel(info).toString();
        var icon = pm.getApplicationIcon(info);
        var launchIntent = pm.getLaunchIntentForPackage(packageName);
        return new App(name, packageName, icon, () -> context.startActivity(launchIntent));
    }
}
