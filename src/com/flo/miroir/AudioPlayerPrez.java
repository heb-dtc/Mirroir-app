package com.flo.miroir;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;

public class AudioPlayerPrez extends RemotePresentation implements OnCompletionListener {
	
	private final String TAG = this.getClass().getName();

	private Context mCtx;
	private Handler mHandler;
	private TextView mSongNameView;
	
	private MediaPlayer mPlayer;
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
		
		mHandler = new Handler();
		
        setContentView(R.layout.prez_audio_player);
        mSongNameView = (TextView) findViewById(R.id.song_title_view);
        mSongNameView.setText("blablablablalba");  
        
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
    }
	
	@Override
	public void onDisplayRemoved() {
		super.onDisplayRemoved();
		Log.e(TAG, "onDisplayRemoved");
		
		//mandatory clean up before exiting
		stopPlayer();
		mPlayer.release();
	}
	
	@Override
	public void onDisplayChanged() {
		super.onDisplayChanged();
		Log.e(TAG, "onDisplayRemoved");
	}
		
	private void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
	
	private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mPlayer.getDuration();
            long currentDuration = mPlayer.getCurrentPosition();

            // Updating progress bar
            int progress = (int)(Utils.getProgressPercentage(currentDuration, totalDuration));
            getListener().onProgressChanged(progress);
            
            if(mPlayer.isPlaying()){
	            // Running this thread after 100 milliseconds
	            mHandler.postDelayed(this, 100);
            }
        }
     };
	
	private void startRadio(){
    	if(mPlayer != null && mCurrentContent != null){
			try {
				mPlayer.reset();
				mPlayer.setDataSource(mCtx, Uri.parse(mCurrentContent.getFilePath()));
		    	
		    	mPlayer.prepare();
		    	mPlayer.start();
		    	
		    	updateProgressBar();
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
	
	private void resumePlayer(){
    	if(mPlayer != null && !mPlayer.isPlaying()){
    			mPlayer.start();
    			//restart update thread
    			updateProgressBar();
    	}
    }
	
	private void pausePlayer(){
    	if(mPlayer != null){
    		if(mPlayer.isPlaying()){
    			mPlayer.pause();
    		}
    	}
    }
	
	private void stopPlayer(){
    	if(mPlayer != null){
    		if(mPlayer.isPlaying()){
    			mHandler.removeCallbacks(mUpdateTimeTask);
    			mPlayer.stop();
    		}
    	}
    }
	
	private void seekToPlayer(int pos){
    	if(mPlayer != null){
    		if(mPlayer.isPlaying()){
    			int totalDuration = mPlayer.getDuration();
    	        int currentPosition = Utils.progressToTimer(pos, totalDuration);
    			mPlayer.seekTo(currentPosition);
    		}
    	}
    }

	/*
	 * 
	 * OUTER CONTROL INTERFACE
	 * 
	 */
	
	public void startAudioPlayer(ContentDetails item){
		mCurrentContent = item;
		startRadio();
	}
	
	public void pauseAudioPlayer(){
		pausePlayer();
	}
	
	public void resumeAudioPlayer(){
		resumePlayer();
	}
	
	public void stopAudioPlayer(){
		stopPlayer();
	}
	
	public void seekTo(int position){
		seekToPlayer(position);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG, "onCompletion");
		getListener().onPlaybackCompleted();
	}
}
