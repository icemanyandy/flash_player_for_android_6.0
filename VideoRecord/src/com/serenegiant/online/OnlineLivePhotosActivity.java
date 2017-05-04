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
import com.serenegiant.audiovideosample.BaseActivity;
import com.serenegiant.audiovideosample.LivePhotosActivity;
import com.serenegiant.audiovideosample.R;
import com.serenegiant.audiovideosample.SettingTool;
import com.serenegiant.glutils.FileNameUtils;
import com.serenegiant.glutils.PhotoHelpTools;
import com.wass08.vlcsimpleplayer.FullscreenVlcPlayer;

import java.util.List;

public class OnlineLivePhotosActivity extends BaseActivity {
    GridView mGridView;
    OnlineLivePhotoAdapter mLivePhotoAdapter;
    SelectPicPopupWindow dddd = null;
    String currentPath;
    ParseOnlineString mParseOnlineTool;
    AutoLineLayout mAutoLineLayout;
    private DownloadReceiver mReceiver;

    ViewGroup container;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_livephoto_mainlayout);
        container = (ViewGroup) this.findViewById(R.id.container);
        SettingTool.init(getApplication());
        initDownloader();
        ParseOnlineString.startLoadConfig(onLineCallBack);
        showDailog();
    }

    ParseOnlineString.CallBack onLineCallBack = new ParseOnlineString.CallBack() {
        @Override
        public void onLoadConfig(final String data) {
            Log.e("dige", "onLineCallBack ");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("dige", "onLineCallBack update ui");
                    fillData(data);
                    hideDailog();
                }
            }, 300);
        }
    };

    public void fillData(String data) {
        if (data == null || false) {
            data = ParseOnlineString.getTestAssertFile(this);
            Toast.makeText(this, R.string.video_record_netnotok, Toast.LENGTH_SHORT).show();
        }
        mParseOnlineTool = new ParseOnlineString(data);

        //SettingTool.init(this);
        mGridView = (GridView) this.findViewById(R.id.gridview_photos);
        mLivePhotoAdapter = new OnlineLivePhotoAdapter(this, mParseOnlineTool.getItemList());
        mLivePhotoAdapter.updateFileState();
        mGridView.setAdapter(mLivePhotoAdapter);
        View onlineview = this.findViewById(R.id.title_online);
        onlineview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(OnlineLivePhotosActivity.this, LivePhotosActivity.class);
                startActivity(intent);
            }
        });

        //init lables
        mAutoLineLayout = (AutoLineLayout) this.findViewById(R.id.toplables);
        String[] lables = mParseOnlineTool.getLables();
        String lableTemp = "";
        if (lables != null) {
            mAutoLineLayout.removeAllViews();
            int count = 0;
            for (String ls : lables) {
                if (lableTemp.contains(ls)) {
                    continue;
                }
                count++;
                lableTemp += ls;
                ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.online_itemlabel, null);
                TextView tview = (TextView) viewGroup.findViewById(R.id.tag_text);
                tview.setText(ls);
                if (count == 1) {
                    tview.setSelected(true);
                }
                tview.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String label = ((TextView) v).getText().toString();
                        selectLable(mAutoLineLayout, label);
                        if (label.equals("全部")) {
                            label = null;
                        }
                        mLivePhotoAdapter.setList(mParseOnlineTool.getItemList(label));
                        mLivePhotoAdapter.updateFileState();
                        mLivePhotoAdapter.notifyDataSetChanged();
                    }
                });
                mAutoLineLayout.addView(viewGroup);
            }
        }

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, final int position, long id) {
                final OnlineLivePhotoItem livePhotoItem = mLivePhotoAdapter.getItem(position);
                final String downloadURL = livePhotoItem.videoUrl;
                final String imageURL = livePhotoItem.picUrl;
                boolean donwloaded = livePhotoItem.download_state == RequestDownloadInfo.STATUS_COMPLETE;

                String showtext = getResources().getString(R.string.video_record_download)+" "+livePhotoItem.title;
                if(livePhotoItem.getFloatMoney()>0f && !SettingTool.getInstance().getData(livePhotoItem.title+"vip",false) && !SettingTool.getInstance().getSVIP()){
                    String format = getResources().getString(R.string.video_record_payforname);
                    showtext = String.format(format,livePhotoItem.getFloatMoney(),livePhotoItem.title );
                    //showtext = "支付¥"+livePhotoItem.getFloatMoney()+" 可下载 "+livePhotoItem.title;
                }
                if(donwloaded){

                    String format = getResources().getString(R.string.video_record_settingname);
                    showtext = String.format(format,livePhotoItem.title);
                    //showtext = "设置 "+livePhotoItem.title+" 为动态壁纸";
                }
                final ImageView imgView = (ImageView) view.findViewById(R.id.itemImage);
                SelectPicPopupWindow menuWindow = new SelectPicPopupWindow(OnlineLivePhotosActivity.this,showtext, new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dddd.dismiss();
                        if (v.getId() == R.id.btn_download) {
                            if (livePhotoItem.download_state == RequestDownloadInfo.STATUS_COMPLETE) {
                                Intent inew = new Intent();
                                inew.setClass(OnlineLivePhotosActivity.this, FullscreenVlcPlayer.class);
                                //ComponentName com= new ComponentName( "com.wass08.vlcsimpleplayer" , "com.wass08.vlcsimpleplayer.FullscreenVlcPlayer");
                                String localImage = FileNameUtils.getImagePathByName(livePhotoItem.title);
                                String localVideo = FileNameUtils.getVideoPathByName(livePhotoItem.title);
                                if (TextUtils.isEmpty(localImage) || TextUtils.isEmpty(localVideo)) {

                                    Toast.makeText(OnlineLivePhotosActivity.this,R.string.video_record_settingerrordownload, Toast.LENGTH_SHORT).show();
                                 }else {
                                    SettingTool.setData("livephoto_path_img", localImage);
                                    SettingTool.setData("livephoto_path_video", localVideo);

                                    Toast.makeText(OnlineLivePhotosActivity.this, R.string.video_record_settingok, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }else if(livePhotoItem.getFloatMoney()>0f && !SettingTool.getInstance().getData(livePhotoItem.title+"vip",false) && !SettingTool.getInstance().getSVIP()){//并且未
                                Intent intent = new Intent();
                                intent.setAction("com.softboy.lock.payme");
                                intent.putExtra("vodMode", true);
                                intent.putExtra("vodName",livePhotoItem.title);
                                intent.putExtra("vodMoney",livePhotoItem.getFloatMoney());
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivityForResult(intent, 0x100);
                                return;
                            }

                            RequestDownloadInfo requestVideoDownloadInfo = new RequestDownloadInfo(livePhotoItem.title, livePhotoItem.title + " 视频", downloadURL);
                            DownloadService.intentDownload(OnlineLivePhotosActivity.this, requestVideoDownloadInfo.getShowName(), requestVideoDownloadInfo);

                            RequestDownloadInfo requestPhotoDownloadInfo = new RequestDownloadInfo(livePhotoItem.title, livePhotoItem.title + " 壁纸", imageURL);
                            DownloadService.intentDownload(OnlineLivePhotosActivity.this, requestPhotoDownloadInfo.getShowName(), requestPhotoDownloadInfo);
                        } else if (v.getId() == R.id.btn_preview) {
                            if (livePhotoItem.download_state == RequestDownloadInfo.STATUS_COMPLETE) {//本地预览。
                                Intent inew = new Intent();
                                inew.setClass(OnlineLivePhotosActivity.this, FullscreenVlcPlayer.class);
                                //ComponentName com= new ComponentName( "com.wass08.vlcsimpleplayer" , "com.wass08.vlcsimpleplayer.FullscreenVlcPlayer");
                                String localImage = FileNameUtils.getImagePathByName(livePhotoItem.title);
                                String localVideo = FileNameUtils.getVideoPathByName(livePhotoItem.title);
                                if (TextUtils.isEmpty(localImage) || TextUtils.isEmpty(localVideo)) {

                                    Toast.makeText(OnlineLivePhotosActivity.this, R.string.video_record_previewerror, Toast.LENGTH_SHORT).show();
                                    return;
                                }else {
                                    inew.putExtra("url", localVideo);
                                    inew.putExtra("img", localImage);
                                    startActivity(inew);
                                    return;
                                }
                            }
                            //在线预览
                            Intent i = new Intent();
                            if (imgView.getDrawable() != null && imgView.getDrawable() instanceof BitmapDrawable) {
                                PhotoHelpTools.saveBitmpFile((Bitmap) ((BitmapDrawable) imgView.getDrawable()).getBitmap(), livePhotoItem.title + ".jpg");
                            } else {

                                Toast.makeText(OnlineLivePhotosActivity.this, R.string.video_record_previewnotdownload, Toast.LENGTH_LONG).show();
                                return;
                            }


                            Toast.makeText(OnlineLivePhotosActivity.this,R.string.video_record_previewtips, Toast.LENGTH_LONG).show();
                            i.setClass(OnlineLivePhotosActivity.this, FullscreenVlcPlayer.class);
                            String localImage = FileNameUtils.getImagePathByName(livePhotoItem.title);
                            i.putExtra("img", localImage);
                            i.putExtra("url", downloadURL);
                            startActivity(i);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0x100){
            if(mLivePhotoAdapter != null){
                mLivePhotoAdapter.notifyDataSetInvalidated();
            }
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

        private Button btn_download, btn_preview, btn_cancel;
        private View mMenuView;

        public SelectPicPopupWindow(Activity context,String showText , OnClickListener itemsOnClick) {
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
            if(!TextUtils.isEmpty(showText)){
                btn_download.setText(showText);
            }
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
            Log.e("yangdi", "DownloadReceiver status " + status);
            if (mLivePhotoAdapter != null) {
                List<OnlineLivePhotoItem> itemList = mLivePhotoAdapter.getItemList();
                if (itemList != null) {
                    for (OnlineLivePhotoItem item : itemList) {
                        if (appInfo.getUrl().equals(item.videoUrl)) {
                            item.download_state = status;
                            if (RequestDownloadInfo.STATUS_DOWNLOADING == status)
                                item.download_progress = tmpInfo.getProgress();
                            else if (RequestDownloadInfo.STATUS_COMPLETE == status) {
                                item.download_progress = 100;
                                mLivePhotoAdapter.updateFileState();
                            }
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
                    //if (appInfo.getStatus() != RequestDownloadInfo.STATUS_COMPLETE)
                {

                    Toast.makeText(OnlineLivePhotosActivity.this, appInfo.getShowName() + getResources().getString(R.string.video_record_download_ok), Toast.LENGTH_SHORT).show();
                }
                appInfo.setStatus(RequestDownloadInfo.STATUS_COMPLETE);
                appInfo.setProgress(tmpInfo.getProgress());
                appInfo.setDownloadPerSize(tmpInfo.getDownloadPerSize());
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
                    //if (appInfo.getStatus() != RequestDownloadInfo.STATUS_DOWNLOAD_ERROR)
                {

                    Toast.makeText(OnlineLivePhotosActivity.this, appInfo.getShowName() +getResources().getString(R.string.video_record_download_error), Toast.LENGTH_SHORT).show();
                }
                appInfo.setStatus(RequestDownloadInfo.STATUS_DOWNLOAD_ERROR);
                appInfo.setDownloadPerSize("");
                break;
            }
        }
    }
}
