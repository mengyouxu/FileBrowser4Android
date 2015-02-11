package meng.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FileBrowserActivity extends Activity {
	
	private enum playStatus{
		statusStopped,
		statusPlaying,
		statusPaused
	}
	private String TAG = "Filebrowser";
	private Button buttonPlay = null; 
	private Button buttonStop = null;
	private Button buttonPrev = null;
	private Button buttonNext = null;
	private ListView fileListView = null;
	private TextView currentDirTextView = null;
	private String currentDir = null;
	private String[] fileList = null;
	
	private MediaPlayer streamPlayer = null;
	private  playStatus PlayStatus =playStatus.statusStopped;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        streamPlayer = new MediaPlayer();
        initFileList();
        updateFileList("/");
    }
    
    private void initFileList(){
        setContentView(R.layout.main);
        currentDirTextView = (TextView)findViewById(R.id.currentDir);
        fileListView = (ListView)findViewById(R.id.fileListView);
        fileListView.setOnItemClickListener(fileListListener);
    }
    
    private void showFilelist(){
    	setContentView(R.layout.main);
    	currentDirTextView = (TextView)findViewById(R.id.currentDir);
    	fileListView = (ListView)findViewById(R.id.fileListView);
    	fileListView.setOnItemClickListener(fileListListener);
    	updateFileList(currentDir);
    }
    
    private void updateFileList(String path){
    	currentDirTextView.setText(path);
    	setFileListView(getFileList(path));
    }  
    
    private void setFileListView(String[] fileList){
    	Log.i(TAG,"File list length = " + fileList.length);
    	for(String fileName:fileList){
    		Log.i(TAG,fileName);
    	}
    	fileListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,fileList));
    }
    
    private String[] getFileList(String path){
    	String[] fileNameList =  null;
    	File dir = null;
    	//File[] fileList = null;
    	
    	dir =  new File(path);
    	//fileList =  dir.listFiles();
    	
    	fileNameList = dir.list();
    	/*
    	for(String fileName:fileNameList){
    		Log.i("",fileName);
    	}
    	*/
    	return fileNameList;
    }
    
    private OnItemClickListener fileListListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
    		ArrayAdapter<?> tempAdapter = null;
    		String filePath = (String) currentDirTextView.getText();
    		File fileOnClick = null;
    		if(!filePath.endsWith("/")){
    			filePath += "/";
    		}
    		
    		Log.i(TAG,"onItemClick!\n");
    		Log.i(TAG,"position = " + position + " / " + "id = " + id);
    		switch(position){
    		default:
    			tempAdapter = (ArrayAdapter<?>) parent.getAdapter();
    			filePath += (String) tempAdapter.getItem(position);
    			fileOnClick =  new File(filePath);
    			if(fileOnClick.isDirectory()){
    				currentDir = filePath;
    				updateFileList(filePath);
    			}else if(filePath.endsWith(".mp3")){
    				showPlayer(filePath);
    				/*
    				Intent intent = new Intent(Intent.ACTION_MAIN);
    				intent.addCategory(Intent.CATEGORY_LAUNCHER);
    			
    				intent.putExtra("filename", filePath);
    				ComponentName cn =  new ComponentName("meng.mediaplayer.StreamPlayer","meng.mediaplayer.StreamPlayer.StreamPlayerActivity");
    				intent.setComponent(cn);
    				startActivity(intent);
    				*/
    			}else if(filePath.endsWith(".ts")){
    				ComponentName componentName = new ComponentName(  
    						"meng.FileManager",  
    						"meng.FileManager.VideoPlayer"); 
    				 Intent intent = new Intent();  
    				 
    				 //Bundle bundle = new Bundle();  
    			     //bundle.putString("filename", filePath);   
    			     intent.putExtra("filename",filePath);  
    			     intent.setComponent(componentName);  
    			     startActivity(intent);  
    			}
    			Log.i(TAG,"filePath = "+ filePath);
    			break;
    		}
    	}
    };
    
    private void showPlayer(String path){
    	setContentView(R.layout.streamplayer);
    	
    	buttonPlay = (Button)findViewById(R.id.buttonPlay);
    	buttonStop = (Button)findViewById(R.id.buttonStop);
    	buttonPrev = (Button)findViewById(R.id.buttonPrev);
    	buttonNext = (Button)findViewById(R.id.buttonNext);
    	
    	buttonPlay.setOnClickListener(buttonListener);
    	buttonStop.setOnClickListener(buttonListener);
    	buttonPrev.setOnClickListener(buttonListener);
    	buttonNext.setOnClickListener(buttonListener);
    	
    	buttonPlay.setText(R.string.Pause);
    	play(path);
    }
    
    private OnClickListener buttonListener =  new OnClickListener(){
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
					//play("/sdcard/medias/ogg/mmw-deadzy.ogg");
					//play(Uri.parse("rtsp://192.168.56.1:8554/Welcome_To_Hearbreak.mp3").toString());
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
				//initFileList();
				showFilelist();
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
    	Log.i(TAG,"play --> " + path);
    	if(streamPlayer.isPlaying()){
    		//return 0;
    	}
    	streamPlayer.reset();
    	try {
			streamPlayer.setDataSource(path);
			streamPlayer.prepare();
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
    	streamPlayer.start();

    	PlayStatus = playStatus.statusPlaying;
    	return 0;
    }
    
    private int resume(){
    	Log.i(TAG,"resume!");
    	streamPlayer.start();
    	PlayStatus = playStatus.statusPlaying;
    	return 0;
    }
    
    private int pause(){
    	Log.i(TAG,"pause!");
    	if(!streamPlayer.isPlaying()){
    		return 0;
    	}
    	streamPlayer.pause();
    	PlayStatus = playStatus.statusPaused;
    	return 0;
    }
    
    private int stop(){
    	Log.i(TAG,"stop!");
    	streamPlayer.stop();
    	//streamPlayer.release();
    	streamPlayer.reset();
    	PlayStatus = playStatus.statusStopped;
    	return 0;
    }
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// ����Ƿ��ؼ�,ֱ�ӷ��ص�����
		File tempFile = null;
		Log.i(TAG,currentDir);
		if(currentDir.equals("/")){
			return super.onKeyDown(keyCode, event);
		}else if(keyCode == KeyEvent.KEYCODE_BACK){
			tempFile = new File(currentDir);
			currentDir = tempFile.getParent();
			updateFileList(currentDir);
		}
		return true;
	}
}
