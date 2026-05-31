package com.robotjatek.wplauncher.Services;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
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
                        var apps = loadAppsByPackageName(context, packageName);
                        apps.forEach(app -> _listeners.forEach(l -> l.onAppReplace(app)));
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e("AppChangeReceiver", "Installed app not found: " + packageName);
                    }
                } else {
                    try {
                        var apps = loadAppsByPackageName(context, packageName);
                        apps.forEach(app -> _listeners.forEach(l -> l.onAppInstall(app)));
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
                    var apps = loadAppsByPackageName(context, packageName);
                    apps.forEach(app -> _listeners.forEach(l -> l.onAppReplace(app)));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("AppChangeReceiver", "Installed app not found: " + packageName);
                }
            }
        }
    }

    private List<App> loadAppsByPackageName(Context context, String packageName) throws PackageManager.NameNotFoundException {
        var pm = context.getPackageManager();
        var intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);
        var activities = pm.queryIntentActivities(intent, 0);

        return activities.stream().map(resolveInfo -> {
           var label = resolveInfo.loadLabel(pm).toString();
           var icon = resolveInfo.loadIcon(pm);
           var isSystemApp = (resolveInfo.activityInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
           var className = resolveInfo.activityInfo.name;

           var launchIntent = new Intent(Intent.ACTION_MAIN);
           launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
           launchIntent.setComponent(new ComponentName(packageName, className));

           return new App(label, packageName, className, icon, () -> context.startActivity(launchIntent), isSystemApp);
        }).toList();
    }
}
