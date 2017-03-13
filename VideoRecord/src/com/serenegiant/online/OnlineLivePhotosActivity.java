package com.serenegiant.online;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.aspsine.multithreaddownload.DownloadConfiguration;
import com.aspsine.multithreaddownload.DownloadManager;
import com.serenegiant.audiovideosample.LivePhotosActivity;
import com.serenegiant.audiovideosample.R;

public class OnlineLivePhotosActivity extends Activity {
    GridView mGridView;
    OnlineLivePhotoAdapter mLivePhotoAdapter;
    SelectPicPopupWindow dddd = null;
    String currentPath;
    ParseOnlineString mParseOnlineTool;
    AutoLineLayout mAutoLineLayout;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_livephoto_mainlayout);
        String testString = ParseOnlineString.getTestAssertFile(this);
        mParseOnlineTool = new ParseOnlineString(testString);

        //SettingTool.init(this);
        mGridView = (GridView) this.findViewById(R.id.gridview_photos);
        mLivePhotoAdapter = new OnlineLivePhotoAdapter(this,mParseOnlineTool.getItemList());
        mGridView.setAdapter(mLivePhotoAdapter);
        View onlineview  = this.findViewById(R.id.title_online);
        onlineview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(OnlineLivePhotosActivity.this,LivePhotosActivity.class);
                startActivity(intent);
            }
        });

        //init lables
        mAutoLineLayout = (AutoLineLayout) this.findViewById(R.id.toplables);
        String[] lables = mParseOnlineTool.getLables();
        String lableTemp = "";
        if(lables != null) {
            mAutoLineLayout.removeAllViews();
            int count = 0;
            for (String ls :lables) {
                if(lableTemp.contains(ls)){
                    continue;
                }
                count++;
                lableTemp+=ls;
                ViewGroup viewGroup  = (ViewGroup) getLayoutInflater().inflate(R.layout.online_itemlabel,null);
                TextView tview = (TextView) viewGroup.findViewById(R.id.tag_text);
                tview.setText(ls);
                if(count == 1){
                    tview.setEnabled(true);
                }
                tview.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String label = ((TextView) v).getText().toString();
                        selectLable(mAutoLineLayout,label);
                        if (label.equals("全部")) {
                            label = null;
                        }
                        mLivePhotoAdapter.setList(mParseOnlineTool.getItemList(label));
                        mLivePhotoAdapter.notifyDataSetChanged();
                    }
                });
                mAutoLineLayout.addView(viewGroup);
            }
        }
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, final int position, long id) {
                SelectPicPopupWindow menuWindow = new SelectPicPopupWindow(OnlineLivePhotosActivity.this,new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        dddd.dismiss();
                        if (v.getId() == R.id.btn_download) {

                        }else if(v.getId() == R.id.btn_preview){

                        }
                    }
                });
                dddd = menuWindow;
                AlphaAnimation backAlpha = new AlphaAnimation(1.f, 0.4f);
                backAlpha.setDuration(300);
                backAlpha.setFillAfter(true);
                OnlineLivePhotosActivity.this.findViewById(R.id.container).startAnimation(backAlpha);

                menuWindow.showAtLocation(OnlineLivePhotosActivity.this.findViewById(R.id.container),
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            }
        });
    }

    public void selectLable(AutoLineLayout autoLayout, String text) {
        int childcount = autoLayout.getChildCount();
        for (int i = 0; i < childcount; i++) {
            LinearLayout childview = (LinearLayout) autoLayout.getChildAt(i);
            TextView lableText = (TextView) childview.getChildAt(0);

            if (lableText.getText().toString().equals(text)) {
                lableText.setSelected(true);
            } else {
                lableText.setSelected(false);
            }
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.backimage) {
            this.finish();
        }
    }

    public class SelectPicPopupWindow extends PopupWindow {

        private Button btn_download, btn_preview, btn_cancel   ;
        private View mMenuView;

        public SelectPicPopupWindow(Activity context, OnClickListener itemsOnClick) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mMenuView = inflater.inflate(R.layout.online_livephoto_pop_menu, null);
            btn_download = (Button) mMenuView.findViewById(R.id.btn_download);
            btn_preview = (Button) mMenuView.findViewById(R.id.btn_preview);
            btn_cancel = (Button) mMenuView.findViewById(R.id.btn_cancel);
            btn_cancel.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    dismiss();
                }
            });
            TextView tipsv = (TextView) mMenuView.findViewById(R.id.textView_tips);
            if (!TextUtils.isEmpty(currentPath)) {
                tipsv.setText(currentPath);
                this.setHeight(dip2px(context, 200));
            } else {
                this.setHeight(dip2px(context, 180));
            }
            // 设置按钮监听
            btn_download.setOnClickListener(itemsOnClick);
            btn_preview.setOnClickListener(itemsOnClick);
             // 设置SelectPicPopupWindow的View
            this.setContentView(mMenuView);
            this.setBackgroundDrawable(null);
            // 设置SelectPicPopupWindow弹出窗体的宽
            this.setWidth(LayoutParams.FILL_PARENT);
            // 设置SelectPicPopupWindow弹出窗体的高
            // 设置SelectPicPopupWindow弹出窗体可点击
            this.setFocusable(true);
            // 设置SelectPicPopupWindow弹出窗体动画效果
            this.setOutsideTouchable(false);
            this.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss() {
                    AlphaAnimation backAlpha = new AlphaAnimation(0.4f, 1f);
                    backAlpha.setDuration(300);
                    backAlpha.setFillAfter(true);
                    OnlineLivePhotosActivity.this.findViewById(R.id.container).startAnimation(backAlpha);
                }
            });
        }

    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void initDownloader() {
        DownloadConfiguration configuration = new DownloadConfiguration();
        configuration.setMaxThreadNum(6);
        configuration.setThreadNum(3);
        DownloadManager.getInstance().init(getApplicationContext(), configuration);
    }
}
