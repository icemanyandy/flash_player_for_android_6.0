package com.wass08.vlcsimpleplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.videolan.vlc.VlcVideoView;

public class FullscreenVlcPlayer extends Activity {
    private String urlToStream;
    private VlcVideoView vlcVideoView;
    private float rate = 1.0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        urlToStream = b.getString("url", null);
        setContentView(R.layout.activity_fullscreen_vlc_player);
        vlcVideoView = (VlcVideoView) findViewById(R.id.vlc_surface);
        vlcVideoView.startPlay(urlToStream);
        vlcVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rate += 0.5f;
                if (rate >= 5.f) {
                    rate = 0.1f;
                }
                vlcVideoView.setPlaybackSpeedMedia(rate);
                Toast.makeText(FullscreenVlcPlayer.this, "速度：" + rate, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
