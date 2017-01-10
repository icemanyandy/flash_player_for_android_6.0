package com.wass08.vlcsimpleplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import java.lang.ref.WeakReference;

public class FullscreenVlcPlayer extends Activity implements SurfaceHolder.Callback, IVideoPlayer {

    private String              urlToStream;

    private SurfaceView         mSurface;
    private SurfaceHolder       holder;
    // media player
    public LibVLC              libvlc;
    private int                 mVideoWidth;
    private int                 mVideoHeight;
    private final static int    VideoSizeChanged = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        urlToStream = b.getString("url", null);

       setContentView(R.layout.activity_fullscreen_vlc_player);
       SbVideoView sbview =  (SbVideoView)findViewById(R.id.vlc_surface);
       //  mSurface = (SurfaceView) findViewById(R.id.vlc_surface);
       // playMovie();
       sbview.playMovie(urlToStream);

    }





    private void setupControls() {
        mSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSurface.setVisibility(View.VISIBLE);
                rate += rateFrame;
                if(rate>=5f)
                	rate = 0f;
                Toast.makeText(FullscreenVlcPlayer.this, "rate +"+rate , Toast.LENGTH_SHORT).show();
                libvlc.setRate(rate);
            }
        });
    }

    float rate = 1.0f;
    float rateFrame = 0.3f;
    public void playMovie() {
        if (libvlc != null && libvlc.isPlaying())
            return ;
        holder = mSurface.getHolder();
        holder.addCallback(this);
        createPlayer(urlToStream);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(libvlc != null)
        libvlc.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int format,
                               int width, int height) {
        if (libvlc != null)
            libvlc.attachSurface(surfaceholder.getSurface(), this);
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
    	  if(libvlc != null)
    		  libvlc.detachSurface();
    }

    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        // get screen size
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        if (holder != null)
            holder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }

    @Override
    public void setSurfaceSize(int width, int height, int visible_width,
                               int visible_height, int sar_num, int sar_den) {
        Message msg = Message.obtain(mHandler, VideoSizeChanged, width, height);
        msg.sendToTarget();
    }

    private void createPlayer(String media) {
        releasePlayer();
        setupControls();
        try {
            libvlc = LibVLC.getInstance();
            libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_FULL);
            libvlc.eventVideoPlayerActivityCreated(true);
            libvlc.setSubtitlesEncoding("");
            libvlc.setAout(LibVLC.AOUT_OPENSLES);
            libvlc.setTimeStretching(true);
            libvlc.setChroma("RV32");
            libvlc.setVerboseMode(true);
            LibVLC.restart(this);
            EventHandler.getInstance().addHandler(mHandler);
            holder.setFormat(PixelFormat.RGBX_8888);
            holder.setKeepScreenOn(true);
            MediaList list = libvlc.getMediaList();
            list.clear();
            list.add(new Media(libvlc, LibVLC.PathToURI(media)), false);
            libvlc.playIndex(0);
        } catch (Exception e) {
            Toast.makeText(this, "Could not create Vlc Player", Toast.LENGTH_LONG).show();
        }
    }

    private void releasePlayer() {

        EventHandler.getInstance().removeHandler(mHandler);
        if (libvlc == null)
            return;
        libvlc.eventVideoPlayerActivityCreated(false);

        //libvlc.detachSurface();
        holder = null;
        libvlc.closeAout();

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private Handler mHandler = new MyHandler(this);
    private static class MyHandler extends Handler {
        private WeakReference<FullscreenVlcPlayer> mOwner;

        public MyHandler(FullscreenVlcPlayer owner) {
            mOwner = new WeakReference<FullscreenVlcPlayer>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            FullscreenVlcPlayer player = mOwner.get();
            if (msg.what == VideoSizeChanged) {
                player.setSize(msg.arg1, msg.arg2);
                return;
            }
            Bundle b = msg.getData();
            Integer dd = b.getInt("event");
            switch (b.getInt("event")) {
                case EventHandler.MediaPlayerEndReached:
                    player.libvlc.stop();
                    player.libvlc.setTime(0);
                    player.libvlc.play();
                     break;
                case EventHandler.MediaPlayerPlaying:
                case EventHandler.MediaPlayerPaused:
                    break;
                case EventHandler.MediaPlayerStopped:
                default:
                    break;
            }
        }
    }

}
