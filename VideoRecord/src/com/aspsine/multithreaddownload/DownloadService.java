package com.aspsine.multithreaddownload;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.aspsine.multithreaddownload.util.L;
import com.serenegiant.audiovideosample.R;

import java.io.File;

/**
 * Created by aspsine on 15/7/28.
 */
public class DownloadService extends Service {

    private static final String TAG = DownloadService.class.getSimpleName();

    public static final String ACTION_DOWNLOAD_BROAD_CAST = "com.aspsine.multithreaddownload.demo:action_download_broad_cast";

    public static final String ACTION_DOWNLOAD = "com.aspsine.multithreaddownload.demo:action_download";

    public static final String ACTION_PAUSE = "com.aspsine.multithreaddownload.demo:action_pause";

    public static final String ACTION_CANCEL = "com.aspsine.multithreaddownload.demo:action_cancel";

    public static final String ACTION_PAUSE_ALL = "com.aspsine.multithreaddownload.demo:action_pause_all";

    public static final String ACTION_CANCEL_ALL = "com.aspsine.multithreaddownload.demo:action_cancel_all";

    public static final String EXTRA_POSITION = "extra_position";

    public static final String EXTRA_TAG = "extra_tag";

    public static final String EXTRA_APP_INFO = "extra_app_info";

    /**
     * Dir: /Download
     */
    private File mDownloadDir;

    private DownloadManager mDownloadManager;

    private NotificationManagerCompat mNotificationManager;

    public static void intentDownload(Context context, String tag, RequestDownloadInfo info) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_TAG, tag);
        intent.putExtra(EXTRA_APP_INFO, info);
        context.startService(intent);
    }

    public static void intentPause(Context context, String tag) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_PAUSE);
        intent.putExtra(EXTRA_TAG, tag);
        context.startService(intent);
    }

    public static void intentPauseAll(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);
    }

    public static void destory(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        context.stopService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            RequestDownloadInfo RequestDownloadInfo = (RequestDownloadInfo) intent.getSerializableExtra(EXTRA_APP_INFO);
            String tag = intent.getStringExtra(EXTRA_TAG);
            switch (action) {
                case ACTION_DOWNLOAD:
                    download(RequestDownloadInfo, tag);
                    break;
                case ACTION_PAUSE:
                    pause(tag);
                    break;
                case ACTION_CANCEL:
                    cancel(tag);
                    break;
                case ACTION_PAUSE_ALL:
                    pauseAll();
                    break;
                case ACTION_CANCEL_ALL:
                    cancelAll();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void download(final RequestDownloadInfo RequestDownloadInfo, String tag) {
        final DownloadRequest request = new DownloadRequest.Builder()
                .setName(RequestDownloadInfo.getName() + ".apk")
                .setUri(RequestDownloadInfo.getUrl())
                .setFolder(mDownloadDir)
                .build();
        mDownloadManager.download(request, tag, new DownloadCallBack(tag.hashCode(), RequestDownloadInfo, mNotificationManager, getApplicationContext()));
    }

    private void pause(String tag) {
        mDownloadManager.pause(tag);
    }

    private void cancel(String tag) {
        mDownloadManager.cancel(tag);
    }

    private void pauseAll() {
        mDownloadManager.pauseAll();
    }

    private void cancelAll() {
        mDownloadManager.cancelAll();
    }

    public static class DownloadCallBack implements CallBack {

        private int mPosition;

        private RequestDownloadInfo mRequestDownloadInfo;

        private LocalBroadcastManager mLocalBroadcastManager;

        private NotificationCompat.Builder mBuilder;

        private NotificationManagerCompat mNotificationManager;

        private long mLastTime;

        public DownloadCallBack(int position, RequestDownloadInfo RequestDownloadInfo, NotificationManagerCompat notificationManager, Context context) {
            mPosition = position;
            mRequestDownloadInfo = RequestDownloadInfo;
            mNotificationManager = notificationManager;
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
            mBuilder = new NotificationCompat.Builder(context);
        }

        @Override
        public void onStarted() {
            L.i(TAG, "onStart()");
            mBuilder.setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(mRequestDownloadInfo.getName())
                    .setContentText("Init Download")
                    .setProgress(100, 0, true)
                    .setTicker("Start download " + mRequestDownloadInfo.getName());
            updateNotification();
        }

        @Override
        public void onConnecting() {
            L.i(TAG, "onConnecting()");
            mBuilder.setContentText("Connecting")
                    .setProgress(100, 0, true);
            updateNotification();

            mRequestDownloadInfo.setStatus(RequestDownloadInfo.STATUS_CONNECTING);
            sendBroadCast(mRequestDownloadInfo);
        }

        @Override
        public void onConnected(long total, boolean isRangeSupport) {
            L.i(TAG, "onConnected()");
            mBuilder.setContentText("Connected")
                    .setProgress(100, 0, true);
            updateNotification();
        }

        @Override
        public void onProgress(long finished, long total, int progress) {

            if (mLastTime == 0) {
                mLastTime = System.currentTimeMillis();
            }

            mRequestDownloadInfo.setStatus(RequestDownloadInfo.STATUS_DOWNLOADING);
            mRequestDownloadInfo.setProgress(progress);
            mRequestDownloadInfo.setDownloadPerSize(com.aspsine.multithreaddownload.util.Utils.getDownloadPerSize(finished, total));

            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastTime > 500) {
                L.i(TAG, "onProgress()");
                mBuilder.setContentText("Downloading");
                mBuilder.setProgress(100, progress, false);
                updateNotification();

                sendBroadCast(mRequestDownloadInfo);

                mLastTime = currentTime;
            }
        }

        @Override
        public void onCompleted() {
            L.i(TAG, "onCompleted()");
            mBuilder.setContentText("Download Complete");
            mBuilder.setProgress(0, 0, false);
            mBuilder.setTicker(mRequestDownloadInfo.getName() + " download Complete");
            updateNotification();

            mRequestDownloadInfo.setStatus(RequestDownloadInfo.STATUS_COMPLETE);
            mRequestDownloadInfo.setProgress(100);
            sendBroadCast(mRequestDownloadInfo);
        }

        @Override
        public void onDownloadPaused() {
            L.i(TAG, "onDownloadPaused()");
            mBuilder.setContentText("Download Paused");
            mBuilder.setTicker(mRequestDownloadInfo.getName() + " download Paused");
            mBuilder.setProgress(100, mRequestDownloadInfo.getProgress(), false);
            updateNotification();

            mRequestDownloadInfo.setStatus(RequestDownloadInfo.STATUS_PAUSED);
            sendBroadCast(mRequestDownloadInfo);
        }

        @Override
        public void onDownloadCanceled() {
            L.i(TAG, "onDownloadCanceled()");
            mBuilder.setContentText("Download Canceled");
            mBuilder.setTicker(mRequestDownloadInfo.getName() + " download Canceled");
            updateNotification();

            //there is 1000 ms memory leak, shouldn't be a problem
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mNotificationManager.cancel(mPosition + 1000);
                }
            }, 1000);

            mRequestDownloadInfo.setStatus(RequestDownloadInfo.STATUS_NOT_DOWNLOAD);
            mRequestDownloadInfo.setProgress(0);
            mRequestDownloadInfo.setDownloadPerSize("");
            sendBroadCast(mRequestDownloadInfo);
        }

        @Override
        public void onFailed(DownloadException e) {
            L.i(TAG, "onFailed()");
            e.printStackTrace();
            mBuilder.setContentText("Download Failed");
            mBuilder.setTicker(mRequestDownloadInfo.getName() + " download failed");
            mBuilder.setProgress(100, mRequestDownloadInfo.getProgress(), false);
            updateNotification();

            mRequestDownloadInfo.setStatus(RequestDownloadInfo.STATUS_DOWNLOAD_ERROR);
            sendBroadCast(mRequestDownloadInfo);
        }

        private void updateNotification() {
            mNotificationManager.notify(mPosition + 1000, mBuilder.build());
        }

        private void sendBroadCast(RequestDownloadInfo RequestDownloadInfo) {
            Intent intent = new Intent();
            intent.setAction(DownloadService.ACTION_DOWNLOAD_BROAD_CAST);
            intent.putExtra(EXTRA_POSITION, mPosition);
            intent.putExtra(EXTRA_APP_INFO, RequestDownloadInfo);
            mLocalBroadcastManager.sendBroadcast(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDownloadManager = DownloadManager.getInstance();
        mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        mDownloadDir = new File(Environment.getExternalStorageDirectory(), "Download");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDownloadManager.pauseAll();
    }


}
