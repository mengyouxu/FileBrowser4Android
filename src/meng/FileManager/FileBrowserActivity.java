package meng.FileManager;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
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

	private String TAG = "Filebrowser";
	private ListView fileListView = null;
	private TextView currentDirTextView = null;
	private String currentDir = null;
	private String[] fileList = null;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                    showMusicPlayer(filePath);
    			}else if(filePath.endsWith(".ts")){
    				//ComponentName componentName = new ComponentName(
    				//		"meng.FileManager",
    				//		"meng.FileManager.VideoPlayer");
    				Intent intent = new Intent();

                    intent.setClass(FileBrowserActivity.this,VideoPlayerActivity.class);

    				 Bundle bundle = new Bundle();
    			     bundle.putString("file_path", filePath);
    			     intent.putExtras(bundle);
    			     //intent.setComponent(componentName);
    			     startActivity(intent);  
    			}
    			Log.i(TAG,"filePath = "+ filePath);
    			break;
    		}
    	}
    };
    
    private void showMusicPlayer(String path){

    	Intent intent = new Intent(FileBrowserActivity.this,MusicPlayerActivity.class);
        Bundle bundle_1 = new Bundle();
        bundle_1.putString("file_path",path);

    	intent.putExtras(bundle_1);

    	startActivity(intent);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        File tempFile = null;
        Log.i(TAG,currentDir);
        if(currentDir.equals("/")){
            return super.onKeyDown(keyCode, event);
        }else if(keyCode == KeyEvent.KEYCODE_BACK){

        }
        return true;
    }

}
