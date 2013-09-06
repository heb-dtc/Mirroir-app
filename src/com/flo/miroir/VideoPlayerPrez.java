package com.flo.miroir;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoPlayerPrez extends RemotePresentation {

	private final String TAG = this.getClass().getName();
	
	//UI component
	private TextView mVideoNameView;
	private VideoView mVideoView;
	private ImageView mImageView;
	
	//private PlayerAsyncTask mPlayerAsyncTask;
	
	private ContentDetails mCurrentContent;
	
	public VideoPlayerPrez(Context outerContext, Display display) {
		super(outerContext, display);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "Create Video prez");
		super.onCreate(savedInstanceState);
		
		setName(Utils.videoPresentationName);
		
		setContentView(R.layout.prez_video_player);
		
		mVideoView = (VideoView) findViewById(R.id.videoView);
		mVideoNameView = (TextView) findViewById(R.id.video_name_view);
		mImageView = (ImageView) findViewById(R.id.image_view);
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
		}
	}
	
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
}
