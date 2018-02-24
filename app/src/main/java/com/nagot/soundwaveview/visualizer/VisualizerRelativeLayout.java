package com.nagot.soundwaveview.visualizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.nagot.soundwaveview.visualizer.renderer.Renderer;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Nagot on 24/02/2018.
 */

public class VisualizerRelativeLayout extends RelativeLayout {
    private static final String TAG = "VisualizerView";

    private byte[] mBytes;
    private byte[] mFFTBytes;
    private Rect mRect = new Rect();
    private Visualizer mVisualizer;

    private Set<Renderer> mRendererSet;

    private Paint mFlashPaint = new Paint();
    private Paint mFadePaint = new Paint();

    public VisualizerRelativeLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs);
        init();
    }

    public VisualizerRelativeLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public VisualizerRelativeLayout(Context context)
    {
        this(context, null, 0);
    }

    private void init() {
        mBytes = null;
        mFFTBytes = null;

        mFlashPaint.setColor(Color.argb(122, 255, 255, 255));
        mFadePaint.setColor(Color.argb(230, 255, 255, 255)); // Adjust alpha to change how quickly the image fades
        mFadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        mRendererSet = new HashSet<>();
    }

    /**
     * Links the visualizer to a player
     * @param player - MediaPlayer instance to link to
     */
    public void link(MediaPlayer player)
    {
        if(player == null)
        {
            throw new NullPointerException("Cannot link to null MediaPlayer");
        }

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(player.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        // Pass through Visualizer data to VisualizerView
        Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener()
        {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                              int samplingRate)
            {
                updateVisualizer(bytes);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                                         int samplingRate)
            {
                updateVisualizerFFT(bytes);
            }
        };

        mVisualizer.setDataCaptureListener(captureListener,
                Visualizer.getMaxCaptureRate() / 2, true, true);

        // Enabled Visualizer and disable when we're done with the stream
        mVisualizer.setEnabled(true);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer)
            {
                //mVisualizer.setEnabled(false);
                Log.d("complete", "completed");
                flash();
            }
        });
    }

    public void addRenderer(Renderer renderer)
    {
        if(renderer != null)
        {
            mRendererSet.add(renderer);
        }
    }

    public void clearRenderers()
    {
        mRendererSet.clear();
    }

    /**
     * Call to release the resources used by VisualizerView. Like with the
     * MediaPlayer it is good practice to call this method
     */
    public void release()
    {
        mVisualizer.release();
    }

    /**
     * Pass data to the visualizer. Typically this will be obtained from the
     * Android Visualizer.OnDataCaptureListener call back. See
     * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
     * @param bytes
     */
    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }

    /**
     * Pass FFT data to the visualizer. Typically this will be obtained from the
     * Android Visualizer.OnDataCaptureListener call back. See
     * {@link Visualizer.OnDataCaptureListener#onFftDataCapture }
     * @param bytes
     */
    public void updateVisualizerFFT(byte[] bytes) {
        mFFTBytes = bytes;
        invalidate();
    }

    boolean mFlash = false;

    /**
     * Call this to make the visualizer flash. Useful for flashing at the start
     * of a song/loop etc...
     */
    public void flash() {
        mFlash = true;
        invalidate();
    }

    Bitmap mCanvasBitmap;
    Canvas mCanvas;


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Create canvas once we're ready to draw
        mRect.set(0, 0, getWidth(), getHeight());

        if(mCanvasBitmap == null)
        {
            mCanvasBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        }
        if(mCanvas == null)
        {
            mCanvas = new Canvas(mCanvasBitmap);
        }

        if (mBytes != null) {
            // Render all audio renderers
            AudioData audioData = new AudioData(mBytes);
            for(Renderer r : mRendererSet)
            {
                r.render(mCanvas, audioData, mRect);
            }
        }

        if (mFFTBytes != null) {
            // Render all FFT renderers
            FFTData fftData = new FFTData(mFFTBytes);
            for(Renderer r : mRendererSet)
            {
                r.render(mCanvas, fftData, mRect);
            }
        }

        // Fade out old contents
        mCanvas.drawPaint(mFadePaint);
        //mCanvas.drawColor(Color.WHITE);

        if(mFlash)
        {
            mFlash = false;
            mCanvas.drawPaint(mFlashPaint);
        }

        canvas.drawBitmap(mCanvasBitmap, new Matrix(), null);
    }
}
