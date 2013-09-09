package com.flo.miroir;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoPlayerPrez extends RemotePresentation implements OnCompletionListener, OnPreparedListener {

	private final String TAG = this.getClass().getName();
	
	private Handler mHandler;
	
	//UI component
	private TextView mVideoNameView;
	private VideoView mVideoView;
	private ImageView mImageView;
	
	//private PlayerAsyncTask mPlayerAsyncTask;
	
	private ContentDetails mCurrentContent;
	
	public VideoPlayerPrez(Context outerContext, Display display) {
		super(outerContext, display);
		setName(Utils.videoPresentationName);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "Create Video prez");
		super.onCreate(savedInstanceState);
		
		mHandler = new Handler();
		
		setContentView(R.layout.prez_video_player);
		
		mVideoView = (VideoView) findViewById(R.id.videoView);
		mVideoNameView = (TextView) findViewById(R.id.video_name_view);
		mImageView = (ImageView) findViewById(R.id.image_view);
		
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnPreparedListener(this);
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

			mImageView.setVisibility(View.GONE);
			mVideoView.setVisibility(View.VISIBLE);
			mVideoNameView.setVisibility(View.VISIBLE);
			
			mVideoView.start();
		}
	}

	private void stopPlayer(){
		if(mVideoView != null && mVideoView.isPlaying()){
			mVideoView.stopPlayback();
			
			mImageView.setVisibility(View.VISIBLE);
			mVideoView.setVisibility(View.GONE);
			mVideoNameView.setVisibility(View.GONE);
			
		}
	}
	
	private void pausePlayer(){
		if(mVideoView != null && mVideoView.isPlaying()){
			Log.d(TAG, "pausePlayer");
			mVideoView.pause();
		}
	}
	
	private void resumePlayer(){
		if(mVideoView != null && !mVideoView.isPlaying()){
			Log.d(TAG, "resumePlayer");
			mVideoView.start();
			updateProgressBar();
		}
	}
	
	private void seekToPlayer(int pos){
    	if(mVideoView != null){
    		if(mVideoView.isPlaying()){
    			int totalDuration = mVideoView.getDuration();
    	        int currentPosition = Utils.progressToTimer(pos, totalDuration);
    	        mVideoView.seekTo(currentPosition);
    		}
    	}
    }
	
	private void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
	
	private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mVideoView.getDuration();
            long currentDuration = mVideoView.getCurrentPosition();

            // Updating progress bar
            int progress = (int)(Utils.getProgressPercentage(currentDuration, totalDuration));
            getListener().onProgressChanged(progress);
            
            if(mVideoView.isPlaying()){
	            // Running this thread after 100 milliseconds
	            mHandler.postDelayed(this, 100);
            }
        }
     };
	
	/*
	 * 
	 * OUTER CONTROL INTERFACE
	 * 
	 */
	
	public void startVideoPlayer(ContentDetails item){
		mCurrentContent = item;
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
	
	public void seekTo(int position){
		seekToPlayer(position);
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		Log.d(TAG, "onCompletion");
		getListener().onPlaybackCompleted();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(TAG, "onPrepared");
		updateProgressBar();
	}
}
