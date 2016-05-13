package meng.FileManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import android.media.MediaPlayer.OnPreparedListener;
/**
 * Created by mengyouxu on 4/22/15.
 */
public class VideoPlayer extends Activity {

    private static String TAG = "VideoPlayer";
    String file_path = null;
    FileDescriptor fd = null;
    
    private SurfaceView videoView;
    private SurfaceHolder holder;

    private Button btnLeftTop;
    private Button btnMiddle720p;
    private Button btnFullScreen;
    private LinearLayout.LayoutParams params_r;

    private Uri videoUri;
    private MediaPlayer mediaPlayer;

    private MediaController mediaController;
    protected SurfaceHolder surfaceHolder;
    private Rect surface_Rect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.videoplayer);

        new SimpleDateFormat("HH:mm:ss");

        Executors.newScheduledThreadPool(2);
        new Handler();

        videoView = (SurfaceView) this.findViewById(R.id.surfaceView);
        videoView.getHolder().addCallback(surfaceHolderCallback);


        Intent intent_1 = this.getIntent();
        Bundle bundle_1 = intent_1.getExtras();

        file_path = bundle_1.getString("file_path");
        Log.i(TAG,"setDataSource: " + file_path);



        //params_r.width = 600;
        //params_r.height = 400;
        //params_r.leftMargin = 212;
        //params_r.topMargin = 184;
        //params_r.leftMargin = 0;
        //videoView.setLayoutParams(params_r);

        btnLeftTop = (Button)this.findViewById(R.id.btn1);
        btnMiddle720p = (Button)this.findViewById(R.id.btn2);
        btnFullScreen = (Button)this.findViewById(R.id.btn3);

        btnLeftTop.setOnClickListener(buttonListener);
        btnMiddle720p.setOnClickListener(buttonListener);
        btnFullScreen.setOnClickListener(buttonListener);

        holder = videoView.getHolder();
        //videoView.setMediaController(controller);
        mediaController = new MediaController(this);

        setVideoURI(Uri.parse(file_path));
    }

    private View.OnClickListener buttonListener =  new View.OnClickListener(){
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch(v.getId()){
                case R.id.btn1:
                    Log.i("VideoPlayer", "R.id.btn1");
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                    }
                    //videoView.layout(0,0,400,300);
                    //holder.setFixedSize(400,300);
                    break;
                case R.id.btn2:
                    Log.i("VideoPlayer","R.id.btn2");
                    if (mediaPlayer != null) {
                        mediaPlayer.pause();
                    }
                    //holder.setFixedSize(600,400);
                    break;
                case R.id.btn3:
                    Log.i("VideoPlayer","R.id.btn3");
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        //mediaPlayer.reset();
                    }
                    //holder.setFixedSize(800,600);
                    break;
                default:
                    break;
            }

        }
    };

    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            Log.i("TAG","surfaceChanged");
            Log.i("TAG","w = " + w + " h = " + h);

            surface_Rect = holder.getSurfaceFrame();
            Log.i(TAG,"surface_Rect.bottom = " + surface_Rect.bottom);
            Log.i(TAG,"surface_Rect.left = " + surface_Rect.left);
            Log.i(TAG,"surface_Rect.right = " + surface_Rect.right);
            Log.i(TAG,"surface_Rect.top = " + surface_Rect.top);


            //setLayoutParams(params_l);
            //surfaceHolder.setFixedSize(480,320);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            surfaceHolder = holder;
            Log.d(TAG,"surfaceCreated");
            if (mediaPlayer != null) {
                //surfaceHolder.setFixedSize(480,320);

                mediaPlayer.setDisplay(surfaceHolder);
                resume();
            } else {
                openVideo();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            surfaceHolder = null;
            if (mediaController != null) {
                mediaController.hide();
            }
            Log.i(TAG,"surfaceDestroyed");
            release();
        }
    };

    private void release() {
        Log.d(TAG,"release");
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            fd = null;
        }
    }

    public void resume() {
        if (surfaceHolder == null) {
            return;
        }
        if (mediaPlayer != null) {
            return;
        }
        openVideo();
    }

    public void setVideoURI(Uri uri) {
        this.videoUri = uri;
        openVideo();
    }

    private void openVideo() {
        this.mediaPlayer = new MediaPlayer();
        try {
            this.mediaPlayer.setDataSource(this.getBaseContext(),videoUri);
            //fd= (new FileInputStream(videoUri.getPath()).getFD());
            //this.mediaPlayer.setDataSource(fd);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            throw new RuntimeException(e);
        }
        mediaController.setMediaPlayer(new MediaController.MediaPlayerControl() {

            @Override
            public void start() {
                Log.i(TAG, "MediaControl.start");
                mediaPlayer.start();
            }

            @Override
            public void pause() {
                Log.i(TAG,"MediaControl.pause" );
                mediaPlayer.pause();
            }

            @Override
            public int getDuration() {
                int  duration = mediaPlayer.getDuration();
                Log.i(TAG,"getDuration : " + duration);
                return duration;
            }

            @Override
            public int getCurrentPosition() {
                Log.i(TAG,"MediaControl.getCurrentPosition" );

                return mediaPlayer.getCurrentPosition();
            }

            @Override
            public void seekTo(int i) {
                Log.i(TAG,"MediaControl.getCurrentPosition : " + i );
                mediaPlayer.seekTo(i);
            }

            @Override
            public boolean isPlaying() {
                Log.i(TAG,"MediaControl.isPlaying" );
                return mediaPlayer.isPlaying();
            }

            @Override
            public int getBufferPercentage() {
                Log.i(TAG,"MediaControl.getBufferPercentage" );
                return 0;
            }

            @Override
            public boolean canPause() {
                Log.i(TAG,"MediaControl.canPause" );
                return true;
            }

            @Override
            public boolean canSeekBackward() {
                Log.i(TAG,"MediaControl.canSeekBackward" );
                return true;
            }

            @Override
            public boolean canSeekForward() {
                Log.i(TAG,"MediaControl.canSeekForward" );
                return true;
            }

            @Override
            public int getAudioSessionId() {
                Log.i(TAG,"MediaControl.getAudioSessionId" );
                return 0;
            }
        });

        this.mediaPlayer.prepareAsync();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i(TAG, "onPrepared");
                //mp.start();
                mediaController.setAnchorView(videoView);
                mediaController.show();
                videoView.setOnTouchListener(new View.OnTouchListener(){

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        mediaController.show();
                        return false;
                    }
                });
            }
        });
        this.mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                Log.i(TAG, "onSeekComplete");
            }
        });
        this.mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.i(TAG, "onError what: " + what + ", extra : " + extra);
                return true;
            }
        });

        this.mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Log.i(TAG, "onInfo what: " + what + ", extra : " + extra);
                return true;
            }
        });
    }
}
