package com.flo.miroir;

import android.app.Presentation;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoPlayerPrez extends Presentation {

	private final String TAG = this.getClass().getName();
	
	//UI component
	private Context mCtx;
	private TextView mVideoNameView;
	private VideoView mVideoView;
	
	private PlayerAsyncTask mPlayerAsyncTask;
	
	private boolean mIsPaused = false;
	private ContentDetails mCurrentContent;
	
	public VideoPlayerPrez(Context outerContext, Display display) {
		super(outerContext, display);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "Create Video prez");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.video_player_prez);
		
		mVideoView = (VideoView) findViewById(R.id.videoView);
		mVideoNameView = (TextView) findViewById(R.id.video_name_view);
	}
	
	@Override
	public void onDisplayRemoved() {
		super.onDisplayRemoved();
		Log.e(TAG, "onDisplayRemoved");
		
		//mandatory clean up before exiting
		stopPlayer();
	}
	
	@Override
	public void onDisplayChanged() {
		super.onDisplayChanged();
		Log.e(TAG, "onDisplayRemoved");
	}
	
	private void startPlayer(){
		if(mVideoView != null && mCurrentContent != null){
			Log.e(TAG, "Start video playback");
			mVideoView.setVideoPath(mCurrentContent.getFilePath());
			mVideoNameView.setText(mCurrentContent.getTitle());
			mVideoView.start();
		}
	}

	private void stopPlayer(){
		if(mVideoView != null && mVideoView.isPlaying()){
			mVideoView.stopPlayback();
		}
	}
	
	private void pausePlayer(){
		if(mVideoView != null && mVideoView.isPlaying()){
			mVideoView.pause();
			mIsPaused = true;
		}
	}
	
	private void resumePlayer(){
		if(mVideoView != null && mIsPaused){
			mVideoView.resume();
			mIsPaused = false;
		}
	}
	
	/*
	 * 
	 * ASYNC. TASK FOR MEDIA PLAYER
	 * 
	 */
	private class PlayerAsyncTask extends AsyncTask<Void, Void, Void> {	
		@Override
		public void onPreExecute(){
	 	}
		
		@Override
		protected Void doInBackground(Void...params) {
			startPlayer();
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
	
	public void startVideoPlayer(ContentDetails item){
		mCurrentContent = item;
		
		//mPlayerAsyncTask = new PlayerAsyncTask();
		//mPlayerAsyncTask.execute();
		startPlayer();
	}
	
	public void stopVideoPlayer(){
		stopPlayer();
	}
	
	public void pauseVideoPlayer(){
		pausePlayer();
	}
	
	public void resumeVideoPlayer(){
		resumePlayer();
	}
}
