package meng.FileManager;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

public class MusicPlayer extends Activity{

    String TAG = "MusicPlayer";
	private Button buttonPlay = null; 
	private Button buttonStop = null;
	private Button buttonPrev = null;
	private Button buttonNext = null;
    private MediaPlayer musicplayer = null;

    String file_path = null;


    private enum playStatus{
        statusStopped,
        statusPlaying,
        statusPaused
    }
    private  playStatus PlayStatus =playStatus.statusStopped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.musicplayer);

        buttonPlay = (Button)findViewById(R.id.buttonPlay);
        buttonStop = (Button)findViewById(R.id.buttonStop);
        buttonPrev = (Button)findViewById(R.id.buttonPrev);
        buttonNext = (Button)findViewById(R.id.buttonNext);

        buttonPlay.setOnClickListener(buttonListener);
        buttonStop.setOnClickListener(buttonListener);
        buttonPrev.setOnClickListener(buttonListener);
        buttonNext.setOnClickListener(buttonListener);

        Intent intent_1 = this.getIntent();
        Bundle bundle_1 = intent_1.getExtras();
        file_path = bundle_1.getString("file_path");

        musicplayer = new MediaPlayer();

    }


    private View.OnClickListener buttonListener =  new View.OnClickListener(){
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch(v.getId()){
                case R.id.buttonPlay:
                    switch(PlayStatus){
                        case statusPlaying:
                            pause();
                            buttonPlay.setText(R.string.Play);
                            break;
                        case statusStopped:
                            play(file_path);
                            buttonPlay.setText(R.string.Pause);
                            break;
                        case statusPaused:
                            resume();
                            buttonPlay.setText(R.string.Pause);
                            break;
                        default :
                            break;
                    }

                    break;
                case R.id.buttonStop:
                    stop();
                    buttonPlay.setText(R.string.Play);
                    break;
                case R.id.buttonNext:
                    resume();
                default:
                    break;
            }

        }
    };

    private int play(String path){
        Log.i(TAG, "play --> " + path);
        if(musicplayer.isPlaying()){
            //return 0;
        }
        musicplayer.reset();
        try {
            musicplayer.setDataSource(path);
            musicplayer.prepare();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            Log.i("StreamPlayer","IllegalArgumentException!");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            Log.i("StreamPlayer","IllegalStateException!");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.i(TAG,"IOException!");
            e.printStackTrace();
        }
        musicplayer.start();

        PlayStatus = playStatus.statusPlaying;
        return 0;
    }

    private int resume(){
        Log.i(TAG,"resume!");
        musicplayer.start();
        PlayStatus = playStatus.statusPlaying;
        return 0;
    }

    private int pause(){
        Log.i(TAG,"pause!");
        if(!musicplayer.isPlaying()){
            return 0;
        }
        musicplayer.pause();
        PlayStatus = playStatus.statusPaused;
        return 0;
    }

    private int stop(){
        Log.i(TAG,"stop!");
        musicplayer.stop();
        //streamPlayer.release();
        musicplayer.reset();
        PlayStatus = playStatus.statusStopped;
        return 0;
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        stop();
        return super.onKeyDown(keyCode, event);
        //if(keyCode == KeyEvent.KEYCODE_BACK){
        //}
        //return true;
    }
}
