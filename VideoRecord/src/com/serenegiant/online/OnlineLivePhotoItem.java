package com.serenegiant.online;

import android.text.TextUtils;

/**
 * Created by yangdi1 on 2017/3/5.
 */
public  class OnlineLivePhotoItem implements Comparable {
    public String title;//标题
    public String lables;//标签
    public String sortId;//排序 越小越在前
    public String payMoney;//收费
    public String picUrl;//图片
    public String videoUrl;//下载地址
    public String tips;//说明

    public String headURL = "";
    //内部更新
    public int download_progress = 0;
    public int download_state = -1;
    public boolean payed = false; //该视频是否已经付费
    public OnlineLivePhotoItem(String sp[],String theadURL){
        if(sp == null||sp.length<5)
            return;
        lables = sp[1];
        title = sp[0];
        sortId =  sp[2];
        payMoney = sp[3];
        picUrl = sp[4];
        videoUrl = sp[5];
        headURL = theadURL;
        if(!TextUtils.isEmpty(theadURL)) {
            picUrl = picUrl.startsWith("http") ? picUrl : headURL + picUrl;
            videoUrl = videoUrl.startsWith("http") ? videoUrl : headURL + videoUrl;
        }
    }

    public Float getFloatMoney(){
        Float defaut = new Float(0.0f);
        try{
           return Float.valueOf(payMoney);
        }catch (Exception e){

        }
        return defaut;
    }

    @Override
    public int compareTo(Object another) {
        OnlineLivePhotoItem other = (OnlineLivePhotoItem) another;
        if(TextUtils.isEmpty(other.sortId)){
            return -1;
        }
        if(TextUtils.isEmpty(sortId)){
            return -1;
        }
        return sortId.compareTo(other.sortId);
    }
}
