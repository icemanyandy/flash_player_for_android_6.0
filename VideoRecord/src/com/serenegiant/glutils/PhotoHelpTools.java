package com.serenegiant.glutils;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by yangdi1 on 2017/3/15.
 */
public class PhotoHelpTools {
    private static final String LIVEPHOTO_DIR_NAME = "iLivePhoto";

    public static boolean saveBitmpFile(Bitmap bm, String fileName) {

        final File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), LIVEPHOTO_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir,fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static File getLivePhotoDirPath(){
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), LIVEPHOTO_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return  dir;
    }
}
