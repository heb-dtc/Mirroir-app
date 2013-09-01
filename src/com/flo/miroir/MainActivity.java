package com.flo.miroir;

import android.app.Activity;
import android.app.MediaRouteActionProvider;
import android.app.Presentation;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private final String TAG = this.getClass().getName();
	
	private MediaRouter mMediaRouter;
	private AudioPlayerPrez mAudioPrez;
	
	private Button mStartRadioPrezBtn;
	private Button mStartLocalGallleryBtn;
	private Button mStartLocalVideoBtn;
	
	//private final SparseArray<Presentation> mActivePresentations = new SparseArray<Presentation>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//get the Media Router Service
		mMediaRouter = (MediaRouter)getSystemService(Context.MEDIA_ROUTER_SERVICE);
		
		mStartRadioPrezBtn = (Button)findViewById(R.id.start_radio_button);
		mStartLocalGallleryBtn = (Button)findViewById(R.id.start_local_gallery_button);
		mStartLocalVideoBtn = (Button)findViewById(R.id.start_local_video_button);
		
		mStartLocalGallleryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startLocalGallery();
			}
		});
		
		mStartRadioPrezBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startLocalMusicView();
			}
		});
		
		mStartLocalVideoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startLocalVideoView();
			}
		});
		
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}

    @Override
    protected void onResume() {
        // Be sure to call the super class.
        super.onResume();

        // Listen for changes to media routes.
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
        //mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_AUDIO, mMediaRouterCallback);
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	mMediaRouter.removeCallback(mMediaRouterCallback);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.menu_media_route);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)mediaRouteMenuItem.getActionProvider();
        mediaRouteActionProvider.setRouteTypes(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        //mediaRouteActionProvider.setRouteTypes(MediaRouter.ROUTE_TYPE_LIVE_AUDIO);
        
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    	case R.id.action_settings:
	    		startDisplayListView();
	    		break;
	    	default: break;		
	    }
		
		return true;
	}
	
	private void startAudioPlayerPresentation(){
		Log.d(TAG, "startAudioPlayerPresentation");
		
		// Get the current route and its presentation display.
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
  
        if (route != null) {
        	Display presentationDisplay = route.getPresentationDisplay();
        	if (presentationDisplay != null) {
        		//create the prez
        		mAudioPrez = new AudioPlayerPrez(this, presentationDisplay);
        		
        		//show it
        		mAudioPrez.show();
        	}
        }
	}
	
	private void startDisplayListView(){
		Log.d(TAG, "startDisplayListView");
		
		Intent displayListIntent = new Intent(this, DisplayActivity.class);
		startActivity(displayListIntent);
	}
	
	private void startLocalMusicView(){
		Log.d(TAG, "startLocalMusicView");
		
		Intent localMusicIntent = new Intent(this, LocalMusicActivity.class);
		startActivity(localMusicIntent);
	}
	
	private void startLocalVideoView(){
		Log.d(TAG, "startLocalVideoView");
		
		Intent localVideoIntent = new Intent(this, LocalVideoActivity.class);
		startActivity(localVideoIntent);
	}
	
	private void startRadioPlayback(){
		if(mAudioPrez != null){
			mAudioPrez.startAudioPlayer();
		}
	}
	
	private void startLocalGallery(){
		Intent localGalleryIntent = new Intent(this, LocalGalleryActivity.class);
		startActivity(localGalleryIntent);
	}
	
	/*
	 * 
	 * LISTENER
	 * 
	 */

	
	/*
	 * 
	 * CALLBACKS
	 * 
	 */
	
    private final MediaRouter.SimpleCallback mMediaRouterCallback =
	    new MediaRouter.SimpleCallback() {
	        @Override
	        public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
	            Log.d(TAG, "onRouteSelected: type=" + type + ", info=" + info);
	            //startAudioPlayerPresentation();
	        }
	
	        @Override
	        public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
	            Log.d(TAG, "onRouteUnselected: type=" + type + ", info=" + info);
	        }
	
	        @Override
	        public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
	            Log.d(TAG, "onRoutePresentationDisplayChanged: info=" + info);
	            //startAudioPlayerPresentation();
	        }
    	};
}
