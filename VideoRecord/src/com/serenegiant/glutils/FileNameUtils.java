package com.serenegiant.glutils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangdi1 on 2017/3/26.
 */
public class FileNameUtils {
    static File mRootFile;
    //只支持jpg图片
    public static String getVideoPath(String imagePath) {
        String defText = imagePath;
        String defaultName = defText.replace(".jpg", ".mp4");
        File defFile = new File(defaultName);
        if (defFile.exists()) {
            return defFile.getPath();
        }
        List<String> supportVideo = new ArrayList<>();
        supportVideo.add(".mov");
        supportVideo.add(".mkv");
        supportVideo.add(".wmv");
        supportVideo.add(".avi");
        supportVideo.add(".mpg");
        supportVideo.add(".mpeg");
        supportVideo.add(".dat");
        supportVideo.add(".3gp");
        for (String sv : supportVideo) {
            defFile = new File(defText.replace(".jpg", sv));
            if (defFile.exists()) {
                return defFile.getPath();
            }
        }
        return null;
    }

    //通过图片名 找本地视频名
    public static String  getVideoPathByName(String livePhotoName){
        if(mRootFile == null) {
            mRootFile = PhotoHelpTools.getLivePhotoDirPath();
        }
        File imgFile = new File(mRootFile,livePhotoName+".jpg");
        if(imgFile.exists()){
            return getVideoPath(imgFile.getPath());
        }else
            return null;
    }

    //通过图片名 找本地图片
    public static String  getImagePathByName(String livePhotoName){
        if(mRootFile == null) {
            mRootFile = PhotoHelpTools.getLivePhotoDirPath();
        }
        File imgFile = new File(mRootFile,livePhotoName+".jpg");
        return imgFile.getPath();
    }
 }
