package com.serenegiant.online;

import android.content.Context;
import android.text.TextUtils;

import com.aspsine.multithreaddownload.Constants;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yangdi1 on 2017/3/5.
 */
public class ParseOnlineString {
    List<OnlineLivePhotoItem> mItemList = new ArrayList<OnlineLivePhotoItem>() ;
    String lables ;
    String headURL = null;
    public String dataStr;


    public ParseOnlineString(String data){
        dataStr = data;
        lables = "全部";
        mItemList.clear();
        try {
        BufferedReader br = new BufferedReader(new InputStreamReader
                (new ByteArrayInputStream(dataStr.getBytes())));
            String line ;
            int count = 0;
            while ((line = br.readLine()) != null) {
                String split[] = line.split(",");
                count++;
                if(split != null && count != 1) {
                    OnlineLivePhotoItem itemOne = new OnlineLivePhotoItem(split,headURL);
                    lables+="|"+split[1];
                    mItemList.add(itemOne);
                }else {
                    headURL = split[5];
                    continue;
                }
            }
            Collections.sort(mItemList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getHeadURL(){
        return headURL;
    }

    public String[] getLables(){
         return lables.split("\\|");
    }

    public static String getTestAssertFile(Context context)
    {
        String Result="";
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open("online_data.csv") );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            while((line = bufReader.readLine()) != null) {
                Result += line+"\r";
            }
         } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return Result;
    }

    public List getItemList(){
        return mItemList;
    }
    public List getItemList(String label){
        if(TextUtils.isEmpty(label)){
            return mItemList;
        }
        List<OnlineLivePhotoItem> tempList = new ArrayList<>();
        for(OnlineLivePhotoItem item : mItemList){
            if(item.lables != null && item.lables.contains(label)){
                tempList.add(item);
            }
        }
        return tempList;
    }

    public String loadConfig(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(Constants.HTTP.CONNECT_TIME_OUT);
            conn.setReadTimeout(Constants.HTTP.READ_TIME_OUT);
            conn.setRequestMethod(Constants.HTTP.GET);
            InputStream input = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = in.readLine()) != null) {
                sb.append(line + "\r");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    Thread mThread;
    int count = 0;
    final String configURL1 = "http://git.oschina.net/softboys/database/raw/master/livephoto/online_data.csv";
    final String configURL2 = "http://git.oschina.net/softboys/database/raw/master/livephoto/online_data.csv";
    String loadURL = configURL1;
     public void startLoadConfig(final CallBack cb) {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        count = 0;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (count > 6)
                    return;
                else if (count > 3) {
                    loadURL = configURL2;
                }
                try {
                    String out = loadConfig(loadURL);
                    if(cb != null){
                        cb.onLoadConfig(out);
                    }
                    count++;
                } catch (Exception e) {
                    this.run();
                }
            }
        });
        mThread.start();
     }

    public interface CallBack {
        public void onLoadConfig(String data);
    }
}
