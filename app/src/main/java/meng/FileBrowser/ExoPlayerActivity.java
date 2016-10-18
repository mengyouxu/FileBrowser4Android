package meng.FileBrowser;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer.extractor.ts.TsExtractor;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.upstream.TransferListener;
import com.google.android.exoplayer.upstream.UdpDataSource;
import com.google.android.exoplayer.util.Util;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by mengyouxu on 2016/5/12.
 */
public class ExoPlayerActivity extends Activity implements ExoPlayer.Listener, MediaCodecAudioTrackRenderer.EventListener,
        TransferListener {
    private String TAG = "ExoPlayerActivity";
    String file_path = null;
    Button btn1;
    Button btn2;
    Button btn3;

    private ExoPlayer mPlayer;
    private String userAgent;
    private Handler mainHandler;
    private ExtractorSampleSource mSampleSource;
    private DataSource mDataSource;
    private DefaultBandwidthMeter bandwidthMeter;
    private MediaCodecAudioTrackRenderer mAudioRenderer;
    private MediaCodecVideoTrackRenderer mVideoRenderer;
    private Allocator allocator;
    private Uri uri;
    private Extractor mp3Extractor;
    private Extractor mTSExtractor;
    private SurfaceView mSurfaceView;
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.videoplayer);
        btn1 = (Button) this.findViewById(R.id.btn1);
        btn2 = (Button) this.findViewById(R.id.btn2);
        btn3 = (Button) this.findViewById(R.id.btn3);

        btn1.setOnClickListener(buttonListener);
        btn2.setOnClickListener(buttonListener);
        btn3.setOnClickListener(buttonListener);

        Intent intent_1 = this.getIntent();
        String action = intent_1.getAction();
        if (intent_1.ACTION_VIEW.equals(action)) {
            Log.i(TAG, "get action");
            uri = (Uri) intent_1.getData();
        } else {
            Bundle bundle_1 = intent_1.getExtras();
            file_path = bundle_1.getString("file_path");
            uri = Uri.parse(file_path);
        }
        Log.i(TAG, "file name : " + uri.toString() + ", host: " + uri.getHost() + ", port: " + uri.getPort());
        Log.i(TAG, "uri scheme : " + uri.getScheme());

        mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);

        mPlayer = ExoPlayer.Factory.newInstance(2, 1000, 5000);
        mPlayer.addListener(this);
        allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        mainHandler = new Handler();
        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        Log.i(TAG, "UserAgent : " + userAgent);
        mp3Extractor = new Mp3Extractor();
        mTSExtractor = new TsExtractor();

        bandwidthMeter = new DefaultBandwidthMeter(mainHandler, null);

        if(uri.getScheme() == null){
            mDataSource = new DefaultUriDataSource(this, bandwidthMeter, userAgent);
            Log.i(TAG, "Use DefaultUriDataSource (uri scheme == null)");
        } else if(uri.getScheme().toString().compareTo("udp") == 0){
            //  UdpDataSource(TransferListener listener, int maxPacketSize, int socketTimeoutMillis)
            //mDataSource = new UdpDataSource(this, 2000, 3000);
            mDataSource = new UdpDataSource(this, 2000, 8000);
            Log.i(TAG, "Use UdpDataSource");
        } else if(uri.getScheme().toString().compareTo("udp2") == 0){
            //  UdpDataSource(TransferListener listener, int maxPacketSize, int socketTimeoutMillis)
            //mDataSource = new UdpDataSource(this, 2000, 3000);
            mDataSource = new UdpDataSource2(this, 2000, 8000);
            Log.i(TAG, "Use UdpDataSource2");
        } else if(uri.getScheme().toString().compareTo("udp3") == 0){
            //  UdpDataSource(TransferListener listener, int maxPacketSize, int socketTimeoutMillis)
            //mDataSource = new UdpDataSource(this, 2000, 3000);
            mDataSource = new UdpDataSource3(this, 2000, 8000);
            Log.i(TAG, "Use UdpDataSource3");
        }else {
            mDataSource = new DefaultUriDataSource(this, bandwidthMeter, userAgent);
            Log.i(TAG, "Use DefaultUriDataSource");
        }
        mSampleSource = new ExtractorSampleSource(uri, mDataSource, allocator,
                BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE, mp3Extractor, mTSExtractor);

        mAudioRenderer = new MediaCodecAudioTrackRenderer(mSampleSource,
                MediaCodecSelector.DEFAULT, null, true);
        mVideoRenderer = new MediaCodecVideoTrackRenderer(this, mSampleSource,
                MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);

        mPlayer.sendMessage(mVideoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, mSurfaceView.getHolder().getSurface());
        mPlayer.prepare(mAudioRenderer,mVideoRenderer);
        mPlayer.setPlayWhenReady(true);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.btn1:
                    Log.i(TAG, "R.id.btn1");
                    mPlayer.setPlayWhenReady(true);
                    break;
                case R.id.btn2:
                    Log.i(TAG, "R.id.btn2");

                    break;
                case R.id.btn3:
                    Log.i(TAG, "R.id.btn3");

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
        // interface @ MediaCodecAudioTrackRenderer.EventListener
        Log.i(TAG, "onAudioTrackInitializationError");
    }

    @Override
    public void onAudioTrackWriteError(AudioTrack.WriteException e) {
        //  interface @ MediaCodecAudioTrackRenderer.EventListener
        Log.i(TAG, "onAudioTrackWriteError");
    }

    @Override
    public void onAudioTrackUnderrun(int i, long l, long l1) {
        //  interface @ MediaCodecAudioTrackRenderer.EventListener
        Log.i(TAG, "onAudioTrackUnderrun");
    }

    @Override
    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
        //  interface @ MediaCodecAudioTrackRenderer.EventListener
        Log.i(TAG, "onDecoderInitializationError");
    }

    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {
        //  interface @ MediaCodecAudioTrackRenderer.EventListener
        Log.i(TAG, "onCryptoError");
    }

    @Override
    public void onDecoderInitialized(String s, long l, long l1) {
        //  interface @ MediaCodecAudioTrackRenderer.EventListener
        Log.i(TAG, "onDecoderInitialized");
    }

    @Override
    public void onPlayerStateChanged(boolean b, int i) {
        //  interface @ ExoPlayer.Listener
        Log.i(TAG, "onPlayerStateChanged");
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        //  interface @ ExoPlayer.Listener
        Log.i(TAG, "onPlayWhenReadyCommitted");
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        //  interface @ ExoPlayer.Listener
        Log.i(TAG, "onPlayerError : " + e.getMessage().toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // The activity is about to become visible
        Log.i(TAG, "onStart");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ExoPlayer Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://meng.FileBrowser/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // The activity has become visible(it is now resumed)
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Another activity is tacking focus (this activity is about to be "paused")
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ExoPlayer Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://meng.FileBrowser/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        // The activity is no longer visible (it is now "stopped")
        mPlayer.stop();
        Log.i(TAG, "onStop stop mPlayer");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    @Override
    public void onTransferStart() {
        //  interface @ TransferListener
        Log.i(TAG, "onTransferStart");
    }

    @Override
    public void onBytesTransferred(int i) {
        //  interface @ TransferListener
        //Log.i(TAG, "onBytesTransferred : " + i + " bytes");
    }

    @Override
    public void onTransferEnd() {
        //  interface @ TransferListener
        Log.i(TAG, "onTransferEnd");
    }
}
