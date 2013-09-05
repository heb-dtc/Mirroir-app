package com.flo.miroir;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;

public class AudioPlayerPrez extends RemotePresentation{
	
	private final String TAG = this.getClass().getName();

	private Context mCtx;
	private TextView mSongNameView;
	
	private MediaPlayer mPlayer;
	private PlayerAsyncTask mPlayerAsyncTask;
	
	private boolean mIsPaused = false;
	private ContentDetails mCurrentContent;
	
	public AudioPlayerPrez(Context outerContext, Display display) {
		super(outerContext, display);
		mCtx = outerContext;
		
		setName(Utils.musicPresentationName);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "Create audio prez");
		
		super.onCreate(savedInstanceState);

        setContentView(R.layout.prez_audio_player);
        mSongNameView = (TextView) findViewById(R.id.song_title_view);
        mSongNameView.setText("blablablablalba");  
        
        mPlayer = new MediaPlayer();
    }
	
	@Override
	public void onDisplayRemoved() {
		super.onDisplayRemoved();
		Log.e(TAG, "onDisplayRemoved");
		
		//mandatory clean up before exiting
		stopRadio();
		mPlayer.release();
	}
	
	@Override
	public void onDisplayChanged() {
		super.onDisplayChanged();
		Log.e(TAG, "onDisplayRemoved");
	}
		
	private void startRadio(){
    	if(mPlayer != null && mCurrentContent != null){
			try {
				mPlayer.reset();
				mPlayer.setDataSource(mCtx, Uri.parse(mCurrentContent.getFilePath()));
		    	
		    	mPlayer.prepare();
		    	mPlayer.start();
			}
			catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
	
	private void resumeRadio(){
    	if(mPlayer != null && mIsPaused){
    			mPlayer.start();
    			mIsPaused = false;
    	}
    }
	
	private void pauseRadio(){
    	if(mPlayer != null){
    		if(mPlayer.isPlaying()){
    			mPlayer.pause();
    			mIsPaused = true;
    		}
    	}
    }
	
	private void stopRadio(){
    	if(mPlayer != null){
    		if(mPlayer.isPlaying()){
    			mPlayer.stop();
    		}
    	}
    }
	
	/*
	 * 
	 * ASYNC TASK FOR MEDIA PLAYER
	 * 
	 */
	private class PlayerAsyncTask extends AsyncTask<Void, Void, Void> {	
		@Override
		public void onPreExecute(){
	 	}
		
		@Override
		protected Void doInBackground(Void...params) {
			startRadio();
			return null;
		}
		
		@Override
		public void onPostExecute(Void res) {
		}
	}

	/*
	 * 
	 * OUTER CONTROL INTERFACE
	 * 
	 */
	
	public void startAudioPlayer(ContentDetails item){
		mCurrentContent = item;
		
		mPlayerAsyncTask = new PlayerAsyncTask();
		mPlayerAsyncTask.execute();
	}
	
	public void pauseAudioPlayer(){
		pauseRadio();
	}
	
	public void resumeAudioPlayer(){
		resumeRadio();
	}
	
	public void stopAudioPlayer(){
		stopRadio();
	}
}
