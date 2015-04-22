package meng.FileManager;
//package com.mengyouxu.mediacodectest;

import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MediaCodecTest extends Activity implements SurfaceHolder.Callback {
	private Intent mIntent = null;
	private RenderThread threadRender = null;
	private FeederThread threadFeeder = null;
	private AssetManager assetManager = null;
	private int frame_num = 0;
	private String file_path = null;
	private Handler timerHandler = null;
	private final String TAG = "MediaCodecTest";
	
	private TextView textViewPlayStatus = null;
	private TextView textViewMediaInfo = null;
	private Button btnNeedRender = null;
	private boolean bNeedRender = true;
	
	private MediaExtractor extractor;
	private MediaCodec decoder;
	private AssetFileDescriptor fileDescriptor;
	private ByteBuffer[] inputBuffers = null;
	private ByteBuffer[] outputBuffers = null;
	
	private Runnable runnable = new Runnable() {  
	    public void run() {  
	    	Log.e(TAG,"frame rate = " + frame_num);
	    	textViewPlayStatus.setText("Frame rate: " + frame_num);
	    	frame_num = 0;
	    	timerHandler.postDelayed(this, 1000);  
	    }  
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mediacodec_test);
		//SurfaceView sv = new SurfaceView(this);
		SurfaceView sv = (SurfaceView)findViewById(R.id.surfaceView);
		sv.getHolder().addCallback(this);
		
		textViewPlayStatus = (TextView)findViewById(R.id.playerStatus);
		textViewPlayStatus.setTextSize(50);
		
		textViewMediaInfo = (TextView)findViewById(R.id.mediaInfo);
		textViewMediaInfo.setTextSize(20);
		
		
		btnNeedRender = (Button)findViewById(R.id.buttonNeedRender);
		btnNeedRender.setOnClickListener(buttonListener);
		btnNeedRender.setText("Click me to stop rendering video");
		bNeedRender = true;
		
		assetManager = getAssets();
		
		timerHandler = new Handler();
		mIntent = this.getIntent();
		Bundle bundle=mIntent.getExtras(); // 也可以用bundle
		file_path=bundle.getString("file_path");
		//file_path = mIntent.getStringExtra("file_path");
		
		textViewMediaInfo.setText("Filename: " + file_path);
		
		Log.i(TAG,"filename: " + file_path);
		timerHandler.postDelayed(runnable, 500);  
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG,"surfaceCreated");
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (threadFeeder == null) {
			
			threadFeeder = new FeederThread(holder.getSurface());
			threadFeeder.start();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		if (threadFeeder != null) {
			threadFeeder.interrupt();
		}
		if (threadRender != null) {
			threadRender.interrupt();
		}
	}
	
	/*
	 * FeederThread 从Extractor 拿数据 并decoder.queueInputBuffer
	 * 
	 */
	private class FeederThread extends Thread{
		private Surface surface;
		
		public FeederThread(Surface surface) {
			this.surface = surface;
			/*
			try {
				fileDescriptor = assetManager.openFd("0068.ts");
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			*/
		}
		@Override
		public void run() {	
			boolean isEOS = false;
			Log.i(TAG,"FeederThread.run()");
			extractor = new MediaExtractor();
			try {
				//extractor.setDataSource(fileDescriptor.getFileDescriptor(),	fileDescriptor.getStartOffset(),fileDescriptor.getLength());
				extractor.setDataSource(file_path);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//http://developer.android.com/reference/android/media/MediaCodec.html
			for (int i = 0; i < extractor.getTrackCount(); i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				
				if (mime.startsWith("video/")) {
					extractor.selectTrack(i);
					decoder = MediaCodec.createDecoderByType(mime);
					decoder.configure(format, surface, null, 0);
					Log.i(TAG,"Create decoder ok");
					break;
				}
			}

			if (decoder != null) {
				decoder.start();
				inputBuffers = decoder.getInputBuffers();
				outputBuffers = decoder.getOutputBuffers();

				if (threadRender == null) {
					threadRender = new RenderThread(surface);
					threadRender.start();
				}	
			}else{
				Log.e(TAG,"decoder == null");
			}
			while (!Thread.interrupted()) {
				if (isEOS == false) {
					int inIndex = decoder.dequeueInputBuffer(1000000); //timeout 1s
					Log.i(TAG,"dequeueInputBuffer inIndex: " + inIndex);
					if (inIndex >= 0) {
						ByteBuffer buffer = inputBuffers[inIndex];
						int sampleSize = extractor.readSampleData(buffer, 0);
						if (sampleSize < 0) {
							Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
							decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
							isEOS = true;
						} else {
							Log.i(TAG,"extractor.getSampleTime() -> " +extractor.getSampleTime());
							//decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
							Log.i(TAG,"queueInputBuffer inIndex: " + inIndex);
							decoder.queueInputBuffer(inIndex, 0, sampleSize, 0, 0);//PTS 设置成0，以最快速度解码
							//Log.i(TAG,"befor extractor.advance ");
							extractor.advance();
							//Log.i(TAG,"after extractor.advance ");
						}
					}
				}
			}
				
		}

	};
	private class RenderThread extends Thread {
		private Surface surface;
		
		public RenderThread(Surface surface) {
			this.surface = surface;
		}

		@Override
		public void run() {
			boolean isEOS = false;
			BufferInfo info = new BufferInfo();
			Log.i(TAG,"RenderThread.run()");
			while(!Thread.interrupted()){
				if(isEOS == false){
					int outIndex = decoder.dequeueOutputBuffer(info, 1000000);
					Log.i(TAG,"dequeueOutputBuffer outIndex: " + outIndex);
					//Log.i(TAG,"framerate");
					switch (outIndex) {
					case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
						Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
						outputBuffers = decoder.getOutputBuffers();
						break;
					case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
						Log.d(TAG, "New format " + decoder.getOutputFormat());
						break;
					case MediaCodec.INFO_TRY_AGAIN_LATER:
						Log.d(TAG, "dequeueOutputBuffer timed out! TRY AGAIN");
						outIndex = decoder.dequeueOutputBuffer(info, 1000000);
						if(outIndex == MediaCodec.INFO_TRY_AGAIN_LATER){
							Log.d(TAG, "dequeueOutputBuffer timed out again");
							break;
						}
					default:
						ByteBuffer buffer = outputBuffers[outIndex];
	
						frame_num++;
						//Log.i(TAG,"render : " + outIndex);
						decoder.releaseOutputBuffer(outIndex, bNeedRender);
						Log.i(TAG,"releaseOutputBuffer outIndex: " + outIndex);
						break;
					}
				}
				if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					Log.d(TAG, "MediaCodec.BUFFER_FLAG_END_OF_STREAM");
					isEOS = true;
					break;
				}
			}
			timerHandler.removeCallbacks(runnable);
			decoder.stop();
			decoder.release();
			extractor.release();
			decoder = null;
			extractor = null;
			
		}
	}
	
    private OnClickListener buttonListener =  new OnClickListener(){
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.buttonNeedRender:
					if(bNeedRender == true){
						bNeedRender = false;
						btnNeedRender.setText("Click me to start rendering video(Just decoding now)");
					}else{
						bNeedRender = true;
						btnNeedRender.setText("Click me to stop rendering video(Decoding & Rendering now)");
					}
					
					break;
			default:
					break;
			}
			
		}
    };
}