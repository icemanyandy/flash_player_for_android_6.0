package com.serenegiant.online;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.aspsine.multithreaddownload.DownloadConfiguration;
import com.aspsine.multithreaddownload.DownloadManager;
import com.aspsine.multithreaddownload.DownloadService;
import com.aspsine.multithreaddownload.RequestDownloadInfo;
import com.serenegiant.audiovideosample.LivePhotosActivity;
import com.serenegiant.audiovideosample.R;
import com.serenegiant.glutils.PhotoHelpTools;

import java.util.List;

public class OnlineLivePhotosActivity extends Activity {
    GridView mGridView;
    OnlineLivePhotoAdapter mLivePhotoAdapter;
    SelectPicPopupWindow dddd = null;
    String currentPath;
    ParseOnlineString mParseOnlineTool;
    AutoLineLayout mAutoLineLayout;
    private DownloadReceiver mReceiver;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_livephoto_mainlayout);
        initDownloader();
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

   /*      for (AppInfo info : mAppInfos) {
            DownloadInfo downloadInfo = DownloadManager.getInstance().getDownloadInfo(info.getUrl());
            if (downloadInfo != null) {
                info.setProgress(downloadInfo.getProgress());
                info.setDownloadPerSize(Utils.getDownloadPerSize(downloadInfo.getFinished(), downloadInfo.getLength()));
                info.setStatus(AppInfo.STATUS_PAUSED);
            }
        }
        */

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, final int position, long id) {
                final ImageView imgView = (ImageView) view.findViewById(R.id.itemImage);
                SelectPicPopupWindow menuWindow = new SelectPicPopupWindow(OnlineLivePhotosActivity.this,new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        dddd.dismiss();
                        OnlineLivePhotoItem livePhotoItem = mLivePhotoAdapter.getItem(position);
                        if (v.getId() == R.id.btn_download) {
                            if(imgView.getDrawable() != null && imgView.getDrawable() instanceof BitmapDrawable) {
                                PhotoHelpTools.saveBitmpFile((Bitmap)((BitmapDrawable) imgView.getDrawable()).getBitmap(),livePhotoItem.title+".jpg");
                            }else {
                                Toast.makeText(OnlineLivePhotosActivity.this,"图片还未下载显示呢，请稍等。",Toast.LENGTH_SHORT).show();
                                return;
                            }
                             RequestDownloadInfo requestDownloadInfo = new RequestDownloadInfo(livePhotoItem.title,livePhotoItem.videoUrl);
                            DownloadService.intentDownload(OnlineLivePhotosActivity.this, livePhotoItem.title, requestDownloadInfo);

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

    private void register() {
        mReceiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_BROAD_CAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }

    private void unRegister() {
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        register();
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegister();
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


    class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null || !action.equals(DownloadService.ACTION_DOWNLOAD_BROAD_CAST)) {
                return;
            }
            final RequestDownloadInfo tmpInfo = (RequestDownloadInfo) intent.getSerializableExtra(DownloadService.EXTRA_APP_INFO);
            final RequestDownloadInfo appInfo = tmpInfo;
            final int status = tmpInfo.getStatus();
            Log.e("yangdi","DownloadReceiver status "+status);
            if(mLivePhotoAdapter != null) {
                List<OnlineLivePhotoItem> itemList = mLivePhotoAdapter.getItemList();
                if(itemList != null) {
                    for (OnlineLivePhotoItem item : itemList) {
                        if(appInfo.getUrl().equals(item.videoUrl)){
                            item.download_state = status;
                            if(RequestDownloadInfo.STATUS_DOWNLOADING == status)
                                item.download_progress = tmpInfo.getProgress();
                            else if(RequestDownloadInfo.STATUS_COMPLETE == status)
                                item.download_progress = 100;
                            mLivePhotoAdapter.notifyDataSetInvalidated();
                            break;
                        }
                    }
                }
            }

            switch (status) {
                case RequestDownloadInfo.STATUS_CONNECTING:
                    appInfo.setStatus(RequestDownloadInfo.STATUS_CONNECTING);

                    break;

                case RequestDownloadInfo.STATUS_DOWNLOADING:
                    appInfo.setStatus(RequestDownloadInfo.STATUS_DOWNLOADING);
                    appInfo.setProgress(tmpInfo.getProgress());
                    appInfo.setDownloadPerSize(tmpInfo.getDownloadPerSize());

                    break;
                case RequestDownloadInfo.STATUS_COMPLETE:
                    appInfo.setStatus(RequestDownloadInfo.STATUS_COMPLETE);
                    appInfo.setProgress(tmpInfo.getProgress());
                    appInfo.setDownloadPerSize(tmpInfo.getDownloadPerSize());
                    Toast.makeText(OnlineLivePhotosActivity.this,appInfo.getName() +" 下载完成。",Toast.LENGTH_SHORT).show();

                    break;

                case RequestDownloadInfo.STATUS_PAUSED:
                    appInfo.setStatus(RequestDownloadInfo.STATUS_PAUSED);
                    break;
                case RequestDownloadInfo.STATUS_NOT_DOWNLOAD:
                    appInfo.setStatus(RequestDownloadInfo.STATUS_NOT_DOWNLOAD);
                    appInfo.setProgress(tmpInfo.getProgress());
                    appInfo.setDownloadPerSize(tmpInfo.getDownloadPerSize());
                    break;
                case RequestDownloadInfo.STATUS_DOWNLOAD_ERROR:
                    appInfo.setStatus(RequestDownloadInfo.STATUS_DOWNLOAD_ERROR);
                    appInfo.setDownloadPerSize("");
                    Toast.makeText(OnlineLivePhotosActivity.this,appInfo.getName() +" 下载错误。",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
