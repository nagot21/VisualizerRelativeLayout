package com.nagot.soundwaveview;

import android.Manifest;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.nagot.soundwaveview.visualizer.renderer.LineRenderer;
import com.nagot.soundwaveview.visualizer.VisualizerRelativeLayout;

public class MainActivity extends AppCompatActivity {
    private Button mButton;
    private MediaPlayer mPlayer;
    private VisualizerRelativeLayout mVisualizerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            init();
        }
    }

    private void init() {
        mPlayer = MediaPlayer.create(this, R.raw.mewtwo);
        mPlayer.setLooping(false);

        mVisualizerView = (VisualizerRelativeLayout) findViewById(R.id.visualizerView);
        mVisualizerView.link(mPlayer);

        mButton = (Button) findViewById(R.id.playButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayer.start();
            }
        });

        addLineRenderer();
    }

    private void addLineRenderer() {
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1f);
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.argb(88, 177, 52, 51));

        Paint lineFlashPaint = new Paint();
        lineFlashPaint.setStrokeWidth(1f);
        lineFlashPaint.setAntiAlias(true);
        lineFlashPaint.setColor(Color.argb(188, 177, 52, 51));
        LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, false);
        mVisualizerView.addRenderer(lineRenderer);
    }

    private void checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO}, 21);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 21: {
                init();
            }
        }
    }
}
