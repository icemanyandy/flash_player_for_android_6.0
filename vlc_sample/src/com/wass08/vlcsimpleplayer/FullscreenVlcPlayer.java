package com.wass08.vlcsimpleplayer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import net.qiujuer.genius.blur.StackBlur;

import org.videolan.vlc.VlcVideoView;
import org.videolan.vlc.listener.MediaListenerEvent;

import java.lang.reflect.Field;


public class FullscreenVlcPlayer extends Activity implements View.OnClickListener {
    private static final int SCALE_FACTOR = 4;

    private String urlToStream;
    private String urlImage;
    private VlcVideoView vlcVideoView;
    private float rate = 1.0f;
    private Bitmap mBitmap;
    private Bitmap mCompressBitmap;
    ImageView mImageBlur;
    ImageView mImageOrg;
    ValueAnimator animator;
    private boolean isBlured = false;

    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        urlToStream = b.getString("url", null);
        urlImage = b.getString("img", null);
        setContentView(R.layout.activity_fullscreen_vlc_player);


        if (savedInstanceState == null && false) {//debug LivePhotoFragment
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new LivePhotoFragment()).commit();
            return;
        }


        vlcVideoView = (VlcVideoView) findViewById(R.id.vlc_surface);
        vlcVideoView.setLoop(false);

        mImageBlur = (ImageView) this.findViewById(R.id.img_blur);
        mImageOrg = (ImageView) this.findViewById(R.id.img_org);
        mProgressBar = (ProgressBar) this.findViewById(R.id.loadingprogress);
        //vlcVideoView.setOnClickListener(this);
        //mImageBlur.setOnClickListener(this);
        mImageOrg.setOnClickListener(this);
        initBlur();
        vlcVideoView.setMediaListenerEvent(new MediaListenerEvent() {

            @Override
            public void eventBuffing(float buffing, boolean show) {
                if (!show) {
                    mProgressBar.setVisibility(View.GONE);
                } else if (urlToStream.startsWith("http")) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void eventPlayInit(boolean openClose) {

            }

            @Override
            public void eventStop(boolean isPlayError) {
                initA(true);
            }

            @Override
            public void eventError(int error, boolean show) {

            }

            @Override
            public void eventPlay(boolean isPlaying) {

            }
        });
    }


    private void initA(final boolean showcover) {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        if (showcover) {
            animator = ValueAnimator.ofFloat(0, 1f);
        } else {
            animator = ValueAnimator.ofFloat(0, 1f);//隐藏
        }
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(800);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float frame = (Float) animation.getAnimatedValue();
                if (showcover) {
                    mImageOrg.setAlpha(frame);
                    mImageBlur.setAlpha(Math.abs(2 * (1 - frame)));
                    mImageBlur.setPivotX(mImageBlur.getWidth() / 2);
                    mImageBlur.setPivotY(mImageBlur.getHeight() / 2);
                    mImageBlur.setScaleX((1.25f - frame / 8f));
                    mImageBlur.setScaleY((1.25f - frame / 8f));
                } else {
                    mImageOrg.setAlpha(1 - frame);
                    mImageBlur.setAlpha(Math.abs(frame <= 0.5f ? 2 * frame : 2 - 2 * frame));

                    mImageBlur.setAlpha(Math.abs(2 * (1 - frame)));
                    mImageBlur.setPivotX(mImageBlur.getWidth() / 2);
                    mImageBlur.setPivotY(mImageBlur.getHeight() / 2);
                    mImageBlur.setScaleX((1f + frame / 8f));
                    mImageBlur.setScaleY((1f + frame / 8f));
                }
            }
        });
        animator.start();
    }

    private void applyBlur() {
        // Run Thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String time = "" + blur(1);
                    Log.e("yangdi", "消费时间 " + time);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void initBlur() {
        if (TextUtils.isEmpty(urlImage)) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_picture, getBitmapOptions(this));
        } else {
            mBitmap = BitmapFactory.decodeFile(urlImage, getBitmapOptions(this));
        }
        mImageOrg.setImageBitmap(mBitmap);
        // 压缩并保存位图
        Matrix matrix = new Matrix();
        matrix.postScale(1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR);
        // 新的压缩位图
        mCompressBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);

    }

    private long blur(int type) {
        long startMs = System.currentTimeMillis();

        // Is Compress
        float radius = 20;
        Bitmap overlay = mCompressBitmap;
        if (type == 1)
            overlay = StackBlur.blur(overlay, (int) radius, false);
            // Bitmap JNI Native
        else if (type == 2)
            overlay = StackBlur.blurNatively(overlay, (int) radius, false);
            // Pixels JNI Native
        else if (type == 3)
            overlay = StackBlur.blurNativelyPixels(overlay, (int) radius, false);

        final Bitmap overlayT = overlay;
        mImageBlur.post(new Runnable() {
            @Override
            public void run() {
                isBlured = true;
                mImageBlur.setImageBitmap(overlayT);
                initA(false);
                vlcVideoView.startPlay(urlToStream);
            }
        });
        return System.currentTimeMillis() - startMs;
    }

    @Override
    public void onBackPressed() {
        vlcVideoView.onStop();
        vlcVideoView = null;
        if (mCompressBitmap != null && !mCompressBitmap.isRecycled()) {
            mCompressBitmap.recycle();
            mCompressBitmap = null;
        }
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
        super.onBackPressed();
    }

    private static BitmapFactory.Options getBitmapOptions(Context context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inJustDecodeBounds = false;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            Field field = null;
            try {
                field = BitmapFactory.Options.class.getDeclaredField("inNativeAlloc");
                field.setAccessible(true);
                field.setBoolean(options, true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        int displayDensityDpi = context.getResources().getDisplayMetrics().densityDpi;
        float displayDensity = context.getResources().getDisplayMetrics().density;
//        if (displayDensityDpi > DEFAULT_DENSITY && displayDensity > 1.5f) {
//            int density = (int) (displayDensityDpi * SCALE_FACTOR);
//            options.inDensity = density;
//            options.inTargetDensity = density;
//        }
        return options;
    }

    @Override
    public void onClick(View v) {
        if (vlcVideoView.isPlaying()) {
            vlcVideoView.pause();
            initA(true);
        } else {
            if (isBlured) {
                initA(false);
                vlcVideoView.startPlay(urlToStream);
            } else {
                applyBlur();
            }
        }
    }
}
