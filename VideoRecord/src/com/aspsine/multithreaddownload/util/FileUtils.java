package com.aspsine.multithreaddownload.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by aspsine on 15-4-19.
 */
public class FileUtils {
    private static final String DOWNLOAD_DIR = "download";

    public static final File getDownloadDir(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return new File(context.getExternalCacheDir(), DOWNLOAD_DIR);
        }
        return new File(context.getCacheDir(), DOWNLOAD_DIR);
    }

    public static final String getPrefix(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static final String getSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
