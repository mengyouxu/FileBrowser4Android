package meng.FileBrowser;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.Util;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by mengyouxu on 2016/5/12.
 */
public class ExoPlayerActivity extends Activity implements ExoPlayer.Listener, MediaCodecAudioTrackRenderer.EventListener {
    private String TAG = "ExoPlayerActivity";
    String file_path = null;
    Button btn1;
    Button btn2;
    Button btn3;

    private ExoPlayer player;
    private String userAgent;
    private Handler mainHandler;
    private ExtractorSampleSource sampleSource;
    private DataSource dataSource;
    private DefaultBandwidthMeter bandwidthMeter;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private Allocator allocator;
    private Uri uri;
    private Extractor mp3Extractor;

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

        Intent intent_1 = this.getIntent();
        String action = intent_1.getAction();
        if (intent_1.ACTION_VIEW.equals(action)) {
            Log.i(TAG, "get action");
            Uri uri = (Uri) intent_1.getData();
            file_path = uri.getPath();
            Log.i(TAG, "file name : " + file_path);
        } else {
            Bundle bundle_1 = intent_1.getExtras();
            file_path = bundle_1.getString("file_path");
        }

        btn1 = (Button) this.findViewById(R.id.btn1);
        btn2 = (Button) this.findViewById(R.id.btn2);
        btn3 = (Button) this.findViewById(R.id.btn3);

        btn1.setOnClickListener(buttonListener);
        btn2.setOnClickListener(buttonListener);
        btn3.setOnClickListener(buttonListener);

        String sdcardPath;
        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (hasSDCard == true) {
            sdcardPath = Environment.getExternalStorageDirectory().getPath();
            Log.i(TAG, "sdcardPath : " + file_path);
            uri = Uri.parse(file_path);
        } else {
            return;
        }
        player = ExoPlayer.Factory.newInstance(4, 1000, 5000);
        player.addListener(this);
        allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        mainHandler = new Handler();
        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        // Build the audio renderers.
        Log.i(TAG, "play target: " + uri.toString());
        mp3Extractor = new Mp3Extractor();
        bandwidthMeter = new DefaultBandwidthMeter(mainHandler, null);
        dataSource = new DefaultUriDataSource(this, bandwidthMeter, userAgent);
        sampleSource = new ExtractorSampleSource(uri, dataSource, allocator,
                BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE, mp3Extractor);

        audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
                MediaCodecSelector.DEFAULT, null, true);
        player.prepare(audioRenderer);
        player.setPlayWhenReady(true);
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
                    player.setPlayWhenReady(true);
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
        player.stop();
        Log.i(TAG, "onStop stop player");
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
}
