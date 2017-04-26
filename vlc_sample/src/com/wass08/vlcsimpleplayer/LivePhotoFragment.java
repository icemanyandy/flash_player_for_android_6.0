package com.wass08.vlcsimpleplayer;

import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import net.qiujuer.genius.blur.StackBlur;

import org.videolan.vlc.VlcVideoView;
import org.videolan.vlc.listener.MediaListenerEvent;

import java.lang.reflect.Field;

/**
 * Created by yangdi1 on 2017/1/22.
 */
public class LivePhotoFragment extends Fragment implements View.OnClickListener {
    private static final int SCALE_FACTOR = 4;

    private String urlToStream = "default_video";
    private String urlImage = "default_picture";
    private VlcVideoView vlcVideoView;
    private float rate = 1.0f;
    private Bitmap mBitmap;
    private Bitmap mCompressBitmap;
    ImageView mImageBlur;
    ImageView mImageOrg;
    ValueAnimator animator;
    private boolean isBlured = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.activity_fullscreen_vlc_player, container, false);

        vlcVideoView = (VlcVideoView) rootView.findViewById(R.id.vlc_surface);
        vlcVideoView.setLoop(false);
        mImageBlur = (ImageView) rootView.findViewById(R.id.img_blur);
        mImageOrg = (ImageView) rootView.findViewById(R.id.img_org);
        vlcVideoView.setMediaListenerEvent(new MediaListenerEvent() {

            @Override
            public void eventBuffing(float buffing, boolean show) {

            }

            @Override
            public void eventPlayInit(boolean openClose) {

            }

            @Override
            public void eventStop(boolean isPlayError) {
                initA(true);
                keepScreenOn(false);
            }

            @Override
            public void eventError(int error, boolean show) {

            }

            @Override
            public void eventPlay(boolean isPlaying) {
                keepScreenOn(false);
            }
        });
        initBlur();

        //mImageBlur.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onDestroy() {
        vlcVideoView.onStop();
        vlcVideoView.removeCallbacks(pauseRunnable);
        vlcVideoView.removeCallbacks(startRunnable);
        vlcVideoView = null;
        if (mCompressBitmap != null && !mCompressBitmap.isRecycled()) {
            mCompressBitmap.recycle();
            mCompressBitmap = null;
        }
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }

        super.onDestroy();
    }

    public void setPV(String photo, String video) {
        urlToStream = video;
        urlImage = photo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Bundle b = getActivity().getIntent().getExtras();
            urlToStream = b.getString("url", urlToStream);
            urlImage = b.getString("img", urlImage);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initA(final boolean showcover) {
        initA(showcover, false);
    }

    private void initA(final boolean showcover, final boolean alwaysBlur) {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        float oB = mImageOrg.getAlpha(), oE;
        float bB = mImageBlur.getAlpha(), bE;
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
                if (alwaysBlur) {
                    if (showcover) {
                        mImageOrg.setAlpha(1 - frame);
                        mImageBlur.setAlpha(frame);
                        mImageBlur.setPivotX(mImageBlur.getWidth() / 2);
                        mImageBlur.setPivotY(mImageBlur.getHeight() / 2);
                        mImageBlur.setScaleX((1.25f - frame / 8f));
                        mImageBlur.setScaleY((1.25f - frame / 8f));
                    } else {
                        mImageOrg.setAlpha(0f);
                        mImageBlur.setAlpha(1f - frame);

                        mImageBlur.setPivotX(mImageBlur.getWidth() / 2);
                        mImageBlur.setPivotY(mImageBlur.getHeight() / 2);
                        mImageBlur.setScaleX((1f + frame / 8f));
                        mImageBlur.setScaleY((1f + frame / 8f));
                    }
                } else {
                    float temp = Math.abs(2 * (1 - frame));

                    if (showcover) {
                        mImageOrg.setAlpha(frame);
                        mImageBlur.setAlpha(temp > 1.0f ? 1.f : temp);
                        mImageBlur.setPivotX(mImageBlur.getWidth() / 2);
                        mImageBlur.setPivotY(mImageBlur.getHeight() / 2);
                        mImageBlur.setScaleX((1.25f - frame / 8f));
                        mImageBlur.setScaleY((1.25f - frame / 8f));
                    } else {
                        mImageOrg.setAlpha(1 - frame);
                        mImageBlur.setAlpha(temp > 1.0f ? 1.f : temp);

                        mImageBlur.setPivotX(mImageBlur.getWidth() / 2);
                        mImageBlur.setPivotY(mImageBlur.getHeight() / 2);
                        mImageBlur.setScaleX((1f + frame / 8f));
                        mImageBlur.setScaleY((1f + frame / 8f));
                    }
                }
            }
        });
        animator.start();
    }

    private void applyBlur(final boolean toPlay, final boolean always, final boolean inner) {
        // Run Thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String time = "" + blur(1, toPlay, always, inner);
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
        if (TextUtils.isEmpty(urlImage) || urlImage.equals("default_picture")) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_picture, getBitmapOptions(getActivity()));
        } else {
            mBitmap = BitmapFactory.decodeFile(urlImage, getBitmapOptions(getActivity()));
        }
        if(TextUtils.isEmpty(urlToStream) || urlToStream.equals("default_video")){
            String printTxtPath = getActivity().getApplicationContext().getFilesDir().getParent()+"/lib/libdefault_livephoto.so";
            urlToStream = printTxtPath;//"file:///android_asset/default_livephoto.mov";
        }
        mImageOrg.setImageBitmap(mBitmap);
        // 压缩并保存位图
        Matrix matrix = new Matrix();
        matrix.postScale(1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR);
        // 新的压缩位图
        mCompressBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);

        mImageOrg.setAlpha(1.0f);
        mImageBlur.setAlpha(0f);
        applyBlur(false, false, true);
    }

    private long blur(int type, final boolean toPlay, final boolean always) {
        return blur(type, toPlay, always, false);
    }

    private long blur(int type, final boolean toPlay, final boolean always, final boolean inner) {
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
        isBlured = true;
        final Bitmap overlayT = overlay;

            mImageBlur.post(new Runnable() {
                @Override
                public void run() {
                    mImageBlur.setImageBitmap(overlayT);
                    if (!inner) {
                        if (toPlay) {
                            initA(false, always);
                            vlcVideoView.startPlay(urlToStream);
                        } else {
                            initA(true, always);
                        }
                    }
                }
            });

        return System.currentTimeMillis() - startMs;
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

    public void onClick(View v) {
        if (vlcVideoView.isPlaying()) {
            vlcVideoView.pause();
            initA(true);
        } else {
            if (isBlured) {
                initA(false);
                vlcVideoView.startPlay(urlToStream);
            } else {
                applyBlur(true, false, false);
            }
        }
    }

    PauseRunnable pauseRunnable = new PauseRunnable();

    public class PauseRunnable implements Runnable {
        public boolean always = false;

        @Override
        public void run() {
            if (getActivity() != null && !getActivity().isFinishing()) {
                if (vlcVideoView.isPlaying()) {
                    vlcVideoView.pause();
                }
                if (mImageBlur.getAlpha() >= 0.95f && always) {
                    return;
                }
                if(vlcVideoView.isPlaying() || always){
                    initA(true, always);
                }
            }
        }
    }

    ;

    public class StartRunnable implements Runnable {
        public boolean always = false;

        @Override
        public void run() {

            if (getActivity() != null && !getActivity().isFinishing()) {
                if (isBlured) {
                    initA(false, always);
                    vlcVideoView.startPlay(urlToStream);
                } else {
                    applyBlur(true, always, false);
                }
            }
        }
    }

    ;

    StartRunnable startRunnable = new StartRunnable();

    public void setPlay(boolean play, boolean always) {
        //Log.e("yangdi","setPlay +play "+play +" always "+always,new Throwable());
        Log.e("yangdi", "setPlay +play " + play + " always " + always);

        if (play) {
            vlcVideoView.removeCallbacks(startRunnable);
            vlcVideoView.removeCallbacks(pauseRunnable);
            startRunnable.always = always;
            vlcVideoView.postDelayed(startRunnable, 200);
        } else {
            vlcVideoView.removeCallbacks(startRunnable);
            vlcVideoView.removeCallbacks(pauseRunnable);
            pauseRunnable.always = always;
            vlcVideoView.postDelayed(pauseRunnable, 500);
        }
    }

    public void justBlur(boolean blur) {
        Log.e("yangdi", "  justBlur " + blur);
        if (blur) {
            Log.e("yangdi", "  justBlur mImageBlur " + mImageBlur.getAlpha());
            if (mImageBlur.getAlpha() >= 0.95f) {
                mImageOrg.setAlpha(0.f);
                mImageBlur.setAlpha(1.0f);
                return;
            }
            mImageBlur.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //show blur
                    applyBlur(false, true, false);
                }
            }, 300);
        } else {
            Log.e("yangdi", "  mImageOrg " + mImageOrg.getAlpha());

            if (mImageBlur.getAlpha() <= 0.5f || mImageOrg.getAlpha() >= 0.95f) {
                mImageBlur.setAlpha(0.0f);
                mImageOrg.setAlpha(1.f);
                return;
            }

            initA(true, false);
        }
    }

    public boolean isPlaying() {
        return vlcVideoView.isPlaying();
    }

    public Bitmap getBackGroundBitmap() {
        return mBitmap;
    }


    public void keepScreenOn(boolean on){
        //vlcVideoView.setKeepScreenOn(on);
    }
}
