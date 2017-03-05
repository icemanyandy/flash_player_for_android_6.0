package com.serenegiant.online;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;

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
            for (String ls :lables) {
                if(lableTemp.contains(ls)){
                    continue;
                }
                lableTemp+=ls;
                TextView tview = new TextView(this);
                tview.setText(ls);
                tview.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String label = ((TextView) v).getText().toString();
                        if (label.equals("全部")) {
                            label = null;
                        }
                        mLivePhotoAdapter = new OnlineLivePhotoAdapter(OnlineLivePhotosActivity.this,
                                mParseOnlineTool.getItemList(label));
                        mGridView.setAdapter(mLivePhotoAdapter);
                    }
                });
                mAutoLineLayout.addView(tview);
            }
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.backimage) {
            this.finish();
        }
    }

    public class SelectPicPopupWindow extends PopupWindow {

        private Button btn_take_photo, btn_pick_photo, btn_cancel, btn_delete;
        private View mMenuView;

        public SelectPicPopupWindow(Activity context, OnClickListener itemsOnClick) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mMenuView = inflater.inflate(R.layout.livephoto_pop_menu, null);
            btn_take_photo = (Button) mMenuView.findViewById(R.id.btn_take_photo);
            btn_pick_photo = (Button) mMenuView.findViewById(R.id.btn_pick_photo);
            btn_delete = (Button) mMenuView.findViewById(R.id.btn_delete);
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
            btn_pick_photo.setOnClickListener(itemsOnClick);
            btn_take_photo.setOnClickListener(itemsOnClick);
            btn_delete.setOnClickListener(itemsOnClick);
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
}
