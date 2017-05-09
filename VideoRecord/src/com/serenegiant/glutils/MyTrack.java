package com.serenegiant.glutils;

import android.content.Context;

import com.baidu.mobstat.StatService;

/**
 * Created by yangdi1 on 2017/5/4.
 */
public class MyTrack {

    public static void StartPage(Context context, String page){
        StatService.onPageStart(context, page);
    }

    public static void EndPage(Context context, String page){
        StatService.onPageEnd(context, page);
    }

    public static void TrackPage(Context context, String page) {
        StatService.onEvent(context, "PAGE_NO", page, 1);
    }

    public static void TrackCLICK(Context context, String button_name) {
        StatService.onEvent(context, "CLICK_NO", button_name, 1);
    }

    public static void TrackPayVodClick(Context context, String vodname) {
        StatService.onEvent(context, "CLICK_VOD", vodname, 1);
    }

    public static void TrackDownloadVodClick(Context context, String vodname) {
        StatService.onEvent(context, "CLICK_DOWNLOAD", vodname, 1);
    }

    public static void TrackPrewVodClick(Context context, String vodname) {
        StatService.onEvent(context, "CLICK_PREVIEW", vodname, 1);
    }

    public static void TrackDownDONEVodClick(Context context, String vodname) {
        StatService.onEvent(context, "CLICK_DOWNDONE", vodname, 1);
    }

    public static void TrackPAY(Context context, String goods) {
        StatService.onEvent(context, "PAY_ME", goods, 1);
    }

    public static void TrackPAYCLICK(Context context, String name) {
        StatService.onEvent(context, "PAY_CLICK", name, 1);
    }

    public static void TrackSetVodCLICK(Context context, String name) {
        StatService.onEvent(context, "SET_LIVEPHOTO", name, 1);
    }

    public static void TrackSetIMAGECLICK(Context context, String name) {
        StatService.onEvent(context, "SET_STATICPHOTO", name, 1);
    }

    public static void TrackTAKELIVECLICK(Context context, String name) {
        StatService.onEvent(context, "TAKE_LIVEPHOTO", name, 1);
    }

    public static void TrackCONTENT(Context context, String name) {
        StatService.onEvent(context, "USER_CONTENT", ""+name, 1);
    }
}
