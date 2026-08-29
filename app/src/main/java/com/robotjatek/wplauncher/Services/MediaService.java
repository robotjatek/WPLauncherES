package com.robotjatek.wplauncher.Services;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Size;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper service to load bitmap images from the device's photo gallery.
 */
public class MediaService {
    private static final int LAST_PHOTOS_COUNT = 20;
    private boolean _hasPermission = false;
    private final Context _context;

    public MediaService(Context context) {
        _context = context;
    }

    public boolean hasPermission() {
        return _hasPermission;
    }

    public void setHasPermission(boolean value) {
        _hasPermission = value;
    }

    /**
     * Loads the last 20 photos from the device's photo gallery.
     * @return A list of uris of the last 20 photos
     */
    public List<Uri> loadLatestPhotoUris() {
        if (!hasPermission()) {
            return Collections.emptyList();
        }

        String[] projection = {MediaStore.Images.Media._ID};
        var sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";
        var selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = { Environment.DIRECTORY_DCIM + "/Camera/%" };
        var uris = new ArrayList<Uri>();

        try (var cursor = _context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null) {
                var idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                while (cursor.moveToNext() && uris.size() < LAST_PHOTOS_COUNT) {
                    var id = cursor.getLong(idColumn);
                    var uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    uris.add(uri);
                }
            }
        }

        return uris;
    }

    /**
     * Loads a thumbnail from the device's photo gallery.
     * @param uri the uri of the image
     * @param width Target width
     * @param height Target height
     * @return The thumbnail in the target size
     */
    public Bitmap loadThumbnail(Uri uri, int width, int height) {
        try {
            return _context.getContentResolver().loadThumbnail(uri, new Size(width, height), null);
            // This could result in a way higher image quality at the cost of performance and memory
            // ImageDecoder.Source source = ImageDecoder.createSource(_context.getContentResolver(), uri);
            //        return ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
            //            // This decodes the image directly to your target size
            //            // providing much better quality than loadThumbnail
            //            decoder.setTargetSize(width, height);
        } catch (IOException e) {
            return null;
        }
    }
}
