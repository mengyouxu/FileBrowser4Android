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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    private static String TAG = "videoplayer";
    String file_path = null;

    private SurfaceView videoView;
    private SurfaceHolder holder;
    private ImageView imageView;
    private TextView timeTextView;
    private TextView titleTextView;
    private TextView clockTextView;
    private Button btnLeftTop;
    private Button btnMiddle720p;
    private Button btnFullScreen;
    private ScheduledExecutorService scheduledExecutorService;
    private Handler handler;
    private SimpleDateFormat dateFormat;
    private RelativeLayout.LayoutParams params_r;


    private boolean pause;
    private boolean seekBackward;
    private boolean seekForward;
    private Uri videoUri;
    private MediaPlayer mediaPlayer;
    private Context context;
    private MediaPlayer.OnPreparedListener onPreparedListener;
    private MediaController mediaController;
    protected SurfaceHolder surfaceHolder;
    private Rect surface_Rect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.videoplayer);

        this.dateFormat = new SimpleDateFormat("HH:mm:ss");

        this.scheduledExecutorService = Executors.newScheduledThreadPool(2);
        this.handler = new Handler();

        videoView = (SurfaceView) this.findViewById(R.id.surfaceView);

        videoView.getHolder().addCallback(surfaceHolderCallback);
        //this.imageView = (ImageView) this.findViewById(R.id.image);
        //this.imageView.setImageResource(R.drawable.ad);

        //this.timeTextView = (TextView) this.findViewById(R.id.timeText);
        //this.titleTextView = (TextView) this.findViewById(R.id.videoTitle);
        //this.clockTextView = (TextView) this.findViewById(R.id.clockText);

        Intent intent_1 = this.getIntent();
        Bundle bundle_1 = intent_1.getExtras();

        file_path = bundle_1.getString("file_path");

        params_r = (RelativeLayout.LayoutParams)videoView.getLayoutParams();

        params_r.width = 600;
        params_r.height = 400;
        params_r.leftMargin = 212;
        params_r.topMargin = 184;
        //params_r.leftMargin = 0;
        videoView.setLayoutParams(params_r);

        btnLeftTop = (Button)this.findViewById(R.id.btnLeftTop);
        btnMiddle720p = (Button)this.findViewById(R.id.btnMiddle720p);
        btnFullScreen = (Button)this.findViewById(R.id.btnFullScreen);

        btnLeftTop.setOnClickListener(buttonListener);
        btnMiddle720p.setOnClickListener(buttonListener);
        btnFullScreen.setOnClickListener(buttonListener);

        MediaController controller = new MediaController(this);
        holder = videoView.getHolder();
        //videoView.setMediaController(controller);

        //holder.setSizeFromLayout();
        holder.setFixedSize(480,320);

        //LayoutParams params = new LinearLayout.LayoutParams(480, 320);
        //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.videoView.getLayoutParams();
        //params.width = 1280;
        //params.height = 720;
        //this.getWindowManager().getDefaultDisplay().getWidth();
        //this.videoView.setLayoutParams(params);

        setVideoURI(Uri.parse(file_path));
    }

    private View.OnClickListener buttonListener =  new View.OnClickListener(){
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch(v.getId()){
                case R.id.btnLeftTop:
                    Log.i("VideoPlayer", "R.id.btnLeftTop");
                    videoView.layout(0,0,400,300);

                    params_r.width = 400;
                    params_r.height = 300;
                    params_r.leftMargin = 0;
                    params_r.topMargin = 100;
                    videoView.setLayoutParams(params_r);

                    //holder.setFixedSize(400,300);
                    break;
                case R.id.btnMiddle720p:
                    Log.i("VideoPlayer","R.id.btnMiddle720p");
                    params_r.width = 600;
                    params_r.height = 400;
                    params_r.leftMargin = 212;
                    params_r.topMargin = 184;
                    videoView.setLayoutParams(params_r);
                    //holder.setFixedSize(600,400);
                    break;
                case R.id.btnFullScreen:
                    Log.i("VideoPlayer","R.id.btnFullScreen");
                    params_r.width = 800;
                    params_r.height = 600;
                    params_r.leftMargin = 112;
                    params_r.topMargin = 84;
                    videoView.setLayoutParams(params_r);
                    holder.setFixedSize(800,600);
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
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            throw new RuntimeException(e);
        }
        this.mediaPlayer.prepareAsync();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

}
