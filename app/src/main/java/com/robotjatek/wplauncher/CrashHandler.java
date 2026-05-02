package com.robotjatek.wplauncher;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final Context _context;
    private final Thread.UncaughtExceptionHandler _handler;

    public CrashHandler(Context context) {
        _context = context;
        _handler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        try {
            var timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
            var file = new File(_context.getFilesDir(), "crash_" + timestamp.format(new Date()) + ".txt");
            var writer = new FileWriter(file, true);
            e.printStackTrace(new PrintWriter(writer));
            writer.close();
            // TODO: signal new crash
        } catch (IOException ex) {
            // Ignore
        }
        _handler.uncaughtException(t, e);
    }
}
