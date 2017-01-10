package com.wass08.vlcsimpleplayer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import java.lang.ref.WeakReference;

/**
 * Created by yangdi1 on 2017/1/10.
 */
public class SbVideoView extends SurfaceView implements SurfaceHolder.Callback, IVideoPlayer {
    private LibVLC libvlc;
    private SurfaceHolder holder;
    private Handler mHandler;

    public SbVideoView(Context context) {
        super(context);
        initVideoLib();
    }

    public SbVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoLib();
    }

    public SbVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoLib();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    float rate = 1.0f;
    float rateFrame = 0.3f;

    private void setupControls() {
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rate += rateFrame;
                if (rate >= 5f)
                    rate = 0f;
                Toast.makeText(getContext(), "rate +" + rate, Toast.LENGTH_SHORT).show();
                if(libvlc != null) {
                    libvlc.setRate(rate);
                }
            }
        });
    }

    public void initVideoLib() {
        try {
            setupControls();
            mHandler = new MyHandler(this);
            libvlc = LibVLC.getInstance();
            libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_FULL);
            libvlc.eventVideoPlayerActivityCreated(true);
            libvlc.setSubtitlesEncoding("");
            libvlc.setTimeStretching(true);
            libvlc.setChroma("RV32");
            libvlc.setVerboseMode(true);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Could not init Vlc Player", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (libvlc != null) {
            libvlc.attachSurface(holder.getSurface(), this);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (libvlc != null) {
            libvlc.stop();
            libvlc.detachSurface();
        }
    }

    @Override
    public void setSurfaceSize(final int width, final int height, int visible_width, int visible_height, int sar_num, int sar_den) {
        post(new Runnable() {
            @Override
            public void run() {
                if (holder != null) {
                    holder.setFixedSize(width, height);
                }
            }
        });
    }

    public void playMovie(String path) {
        if (libvlc != null && libvlc.isPlaying())
            return;
        holder = getHolder();
        holder.addCallback(this);
        createPlayer(path);
    }

    public void start(){
        if (libvlc != null && !libvlc.isPlaying())
            return;
        libvlc.play();
    }

    public void pause(){
        if (libvlc != null && libvlc.isPlaying())
            return;
        libvlc.pause();
    }

    private void createPlayer(String media) {
        //
        try {
            holder = getHolder();
            holder.addCallback(this);
            libvlc.setAout(LibVLC.AOUT_OPENSLES);
            LibVLC.restart(getContext());
            EventHandler.getInstance().addHandler(mHandler);
            holder.setFormat(PixelFormat.RGBA_8888);
            MediaList list = libvlc.getMediaList();
            list.clear();
            list.add(new Media(libvlc, LibVLC.PathToURI(media)), false);
            libvlc.playIndex(0);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Could not create Vlc Player", Toast.LENGTH_LONG).show();
        }
    }

    public void releasePlayer() {
        EventHandler.getInstance().removeHandler(mHandler);
        if (libvlc == null)
            return;
        libvlc.eventVideoPlayerActivityCreated(false);
        libvlc.stop();
        libvlc.destroy();
        libvlc = null;
        holder = null;
    }

    private static class MyHandler extends Handler {
        private WeakReference<SbVideoView> mOwner;

        public MyHandler(SbVideoView owner) {
            mOwner = new WeakReference<SbVideoView>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            SbVideoView owner = mOwner.get();

            Bundle b = msg.getData();
            Integer dd = b.getInt("event");
            switch (b.getInt("event")) {
                case EventHandler.MediaPlayerEndReached:
                    if (owner.libvlc != null) {
                        owner.libvlc.stop();
                        owner.libvlc.setTime(0);
                        owner.libvlc.play();
                    }
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
