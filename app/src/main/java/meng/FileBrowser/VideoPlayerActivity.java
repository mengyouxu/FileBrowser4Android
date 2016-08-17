package meng.FileBrowser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by mengyouxu on 4/22/15.
 */
public class VideoPlayerActivity extends Activity{
    String TAG="VideoPlayerActivity";
    String file_path = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoplayeractivity);
        Bundle bundle_1 = null;
        Intent intent_1 = this.getIntent();
        String action = intent_1.getAction();
        if(Intent.ACTION_VIEW.equals(action)){
            Log.i(TAG,"get action");
            Uri uri = (Uri)intent_1.getData();
            file_path = uri.getPath();
            Log.i(TAG,"file name : " + file_path);
        }else {
            bundle_1 = intent_1.getExtras();
            file_path = bundle_1.getString("file_path");
        }
        Button button_mediaplayer = (Button) findViewById(R.id.button_use_mediaplayer);
        button_mediaplayer.setOnClickListener(buttonListener);

        Button button_mediacodec = (Button) findViewById(R.id.button_use_mediacode);
        button_mediacodec.setOnClickListener(buttonListener);
    }

    /*
        Howto A: start another activity:
            Method 1:
                Intent intent = new Intent(this,XXX.class);
                startActivity(intent);
            Method 2:
                Intent intent = new Intent();

                startActivity(intent);

        Howto B: send data through Intent:
            Method 1:
                Intent intent = new Intent(this,XXX.class);
                intent.putExtra("name", "xy");
                intent.putExtra("age", 22);
                startActivity(intent);

            Method 2:
                intent.setClass(this,XXX.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", "xy");
                bundle.putInt("age", 22);
                intent.putExtras(bundle);

        Howto C: get data from Intent:
            Method 1:
            Intent intent = this.getIntent();
            String name = intent.getStringExtra("name");
            int age = intent.getIntExtra("age", 22); //default 22

            Method 2:
            Bundle bundle = intent.getExtras();
            String name2 = bundle.getString("name");
            int age2 = bundle.getInt("age", 22);
     */
    private View.OnClickListener buttonListener =  new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Bundle bundle_2 = new Bundle();
            Intent intent = new Intent();
            bundle_2.putString("file_path",file_path);

            switch(v.getId()){
                case R.id.button_use_mediaplayer: {
                    Log.i(TAG, "R.id.button_use_mediaplayer");
                    intent.setClass(VideoPlayerActivity.this,
                            VideoPlayer.class);

                    intent.putExtras(bundle_2);
                    startActivity(intent);
                    break;
                }
                case R.id.button_use_mediacode: {
                    Log.i(TAG, "R.id.button_use_mediacode");
                    intent.setClass(VideoPlayerActivity.this,
                            MediaCodecTest.class);

                    bundle_2.putString("file_path",file_path);
                    intent.putExtras(bundle_2);
                    startActivity(intent);
                    break;
                }
                case R.id.button_use_exoplayer: {
                    Log.i(TAG, "R.id.button_use_exoplayer");
                    intent.setClass(VideoPlayerActivity.this,
                            ExoPlayerActivity.class);

                    bundle_2.putString("file_path",file_path);
                    intent.putExtras(bundle_2);
                    startActivity(intent);
                    break;
                }
            }
        }
    };
}
