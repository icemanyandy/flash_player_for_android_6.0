package com.serenegiant.audiovideosample;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.example.MyListActivity;
import com.wass08.vlcsimpleplayer.FullscreenVlcPlayer;

import java.io.File;

public class LivePhotosActivity extends Activity {
    GridView mGridView;
    LivePhotoAdapter mLivePhotoAdapter;
    SelectPicPopupWindow dddd = null;
    String currentPath;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.livephoto_mainlayout);
        SettingTool.init(this);
        mGridView = (GridView) this.findViewById(R.id.gridview_photos);
        mLivePhotoAdapter = new LivePhotoAdapter(this);
        mGridView.setAdapter(mLivePhotoAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, final int position, long id) {
                currentPath = mLivePhotoAdapter.getVideoPath(position);
                SelectPicPopupWindow menuWindow = new SelectPicPopupWindow(LivePhotosActivity.this,
                        new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dddd.dismiss();
                                if (v.getId() == R.id.btn_take_photo) {
                                    Intent i = new Intent();
                                    i.setClass(LivePhotosActivity.this, FullscreenVlcPlayer.class);
                                    //ComponentName com= new ComponentName( "com.wass08.vlcsimpleplayer" , "com.wass08.vlcsimpleplayer.FullscreenVlcPlayer");
                                    i.putExtra("url", mLivePhotoAdapter.getVideoPath(position));
                                    i.putExtra("img", mLivePhotoAdapter.getImagePath(position));
                                    startActivity(i);
                                } else if (v.getId() == R.id.btn_delete) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LivePhotosActivity.this);
                                    builder.setMessage("请再次确认删除操作，删除后不可恢复？");
                                    builder.setTitle("警告");
                                    builder.setPositiveButton("确定删除", new AlertDialog.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();

                                            boolean ret = true;
                                            File file = new File(mLivePhotoAdapter.getVideoPath(position));
                                            if (file.isFile() && file.exists()) {
                                                ret &= file.delete();
                                            }
                                            file = new File(mLivePhotoAdapter.getImagePath(position));
                                            if (file.isFile() && file.exists()) {
                                                ret &= file.delete();
                                            }
                                            mLivePhotoAdapter.loadImageList();
                                            mLivePhotoAdapter.notifyDataSetChanged();
                                            Toast.makeText(LivePhotosActivity.this, "删除" + (ret ? "成功" : "失败"), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                    builder.setNegativeButton("取消", new AlertDialog.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.create().show();

                                } else if (v.getId() == R.id.btn_pick_photo) {
                                    SettingTool.setData("livephoto_path_img", mLivePhotoAdapter.getImagePath(position));
                                    SettingTool.setData("livephoto_path_video", mLivePhotoAdapter.getVideoPath(position));
                                    Toast.makeText(LivePhotosActivity.this, "恭喜您，已经设定成功", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                dddd = menuWindow;
                AlphaAnimation backAlpha = new AlphaAnimation(1.f, 0.4f);
                backAlpha.setDuration(300);
                backAlpha.setFillAfter(true);
                LivePhotosActivity.this.findViewById(R.id.container).startAnimation(backAlpha);

                menuWindow.showAtLocation(LivePhotosActivity.this.findViewById(R.id.container),
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

            }
        });
    }

    public void onClick(View v) {
        if (v.getId() == R.id.backimage) {
            this.finish();
            Intent i = new Intent(this, MyListActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
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
                    LivePhotosActivity.this.findViewById(R.id.container).startAnimation(backAlpha);
                }
            });
        }

    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
