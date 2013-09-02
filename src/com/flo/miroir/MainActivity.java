package com.flo.miroir;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private final String TAG = this.getClass().getName();

	private Button mStartRadioPrezBtn;
	private Button mStartLocalGallleryBtn;
	private Button mStartLocalVideoBtn;
	private Button mStartConfigureBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		RemoteDisplayManager.INSTANCE.initializeRemoteDisplayManager(this);

		mStartRadioPrezBtn = (Button)findViewById(R.id.start_radio_button);
		mStartLocalGallleryBtn = (Button)findViewById(R.id.start_local_gallery_button);
		mStartLocalVideoBtn = (Button)findViewById(R.id.start_local_video_button);
		mStartConfigureBtn = (Button)findViewById(R.id.start_configure_button);
		
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
		
		mStartConfigureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDisplayListView();
			}
		});
	}

    @Override
    protected void onResume() {
        super.onResume();
        
        if(RemoteDisplayManager.INSTANCE.isConnectedToRemoteDisplay()){
        	RemoteDisplayManager.INSTANCE.displayStandByPresentation(this);
        	
        	//Enable UI
        	mStartRadioPrezBtn.setEnabled(true);
    		mStartLocalGallleryBtn.setEnabled(true);
    		mStartLocalVideoBtn.setEnabled(true);
        }
        else{
        	//Disable UI
        	mStartRadioPrezBtn.setEnabled(false);
    		mStartLocalGallleryBtn.setEnabled(false);
    		mStartLocalVideoBtn.setEnabled(false);
        }
        	
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	RemoteDisplayManager.INSTANCE.hideStandByPresentation();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	RemoteDisplayManager.INSTANCE.shutDown();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
        /*MenuItem mediaRouteMenuItem = menu.findItem(R.id.menu_media_route);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)mediaRouteMenuItem.getActionProvider();
        mediaRouteActionProvider.setRouteTypes(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        //mediaRouteActionProvider.setRouteTypes(MediaRouter.ROUTE_TYPE_LIVE_AUDIO);*/
        
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
	
	private void startLocalGallery(){
		Intent localGalleryIntent = new Intent(this, LocalGalleryActivity.class);
		startActivity(localGalleryIntent);
	}
}
