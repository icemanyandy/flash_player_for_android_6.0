package com.serenegiant.audiovideosample;

import android.app.Activity;
import android.os.Bundle;

import com.serenegiant.glutils.CustomProgressDialog;

/**
 * Created by yangdi1 on 2017/3/16.
 */
public class BaseActivity extends Activity {
    CustomProgressDialog mLoadingDailog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String loadingStr = getResources().getString(R.string.activity_loading);
        mLoadingDailog = new CustomProgressDialog(this, loadingStr,R.anim.frame);
     }

    public void showDailog(){
        if(!mLoadingDailog.isShowing()){
            mLoadingDailog.show();
        }
    }

    public void hideDailog(){
        if(mLoadingDailog.isShowing()){
            mLoadingDailog.dismiss();
        }
    }
}
