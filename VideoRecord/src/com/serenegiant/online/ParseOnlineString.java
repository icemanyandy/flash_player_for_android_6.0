package com.serenegiant.online;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yangdi1 on 2017/3/5.
 */
public class ParseOnlineString {
     List<OnlineLivePhotoItem> mItemList = new ArrayList<OnlineLivePhotoItem>() ;
    String lables ;
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
                    OnlineLivePhotoItem itemOne = new OnlineLivePhotoItem(split);
                    lables+="|"+split[1];
                    mItemList.add(itemOne);
                }else {
                    continue;
                }
            }
            Collections.sort(mItemList);
        }catch (Exception e){
            e.printStackTrace();
        }
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
}
