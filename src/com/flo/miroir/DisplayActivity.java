package com.flo.miroir;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DisplayActivity extends Activity {
	
	private final String TAG = this.getClass().getName();

	private DisplayListAdapter mDisplayListAdapter;
	private ListView mListView;
    
    private final IRemoteDisplayCallbacks mMediaRouterCallbacks = 
    		new IRemoteDisplayCallbacks(){

				@Override
				public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
				}

				@Override
				public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {	
				}

				@Override
				public void onRoutePresentationDisplayChanged(MediaRouter router, RouteInfo info) {
					if(!info.getName().equals(RemoteDisplayManager.INSTANCE.getLocalRouteName())){
						setProgressBarIndeterminateVisibility(false);
						showStandByPrez();
						mDisplayListAdapter.updateContents();
    	            }
				}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_display_list);
        
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

        mDisplayListAdapter = new DisplayListAdapter(this);
        mListView = (ListView)findViewById(R.id.display_list_view);
        mListView.setAdapter(mDisplayListAdapter);
        
        mListView.setOnItemClickListener(displayListListener);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mDisplayListAdapter.updateContents();
        RemoteDisplayManager.INSTANCE.subscribeToCallabcks(mMediaRouterCallbacks);
        
        RemoteDisplayManager.INSTANCE.displayStandByPresentation(this);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	RemoteDisplayManager.INSTANCE.unsubscribeToCallbacks(mMediaRouterCallbacks);
    	RemoteDisplayManager.INSTANCE.hideStandByPresentation();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.display, menu);
        
		return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	        case R.id.action_refresh:
	        	mDisplayListAdapter.updateContents();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
    	}
    }
    
    private OnItemClickListener displayListListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			RouteInfo info = mDisplayListAdapter.getItem(position);
			if(!RemoteDisplayManager.INSTANCE.isCurrentRoute(info)){
				makeConnection(info);
			}
		}
	};
    
	private void makeConnection(RouteInfo info){
		setProgressBarIndeterminateVisibility(true);   
		RemoteDisplayManager.INSTANCE.selectRoute(info);
	}
	
	private void showStandByPrez(){
		Log.d(TAG, "showStandByPrez");
		RemoteDisplayManager.INSTANCE.displayStandByPresentation(this);
	}
    
    private final class DisplayListAdapter extends ArrayAdapter<RouteInfo> {
        final Context mContext;
        
        public DisplayListAdapter(Context context) {
            super(context, R.layout.row_display_item_list);
            mContext = context;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	final View v;
            if (convertView == null) {
                v = ((Activity) mContext).getLayoutInflater().inflate(R.layout.row_display_item_list, null);
            } else {
                v = convertView;
            }

            final RouteInfo info = getItem(position);
            
            //NAME
            TextView tv = (TextView)v.findViewById(R.id.display_title);
            tv.setText(info.getName());
            
            if(RemoteDisplayManager.INSTANCE.isLocalRoute(info))
            	tv.setTextColor(Color.parseColor("#cc0066"));
            
            if(RemoteDisplayManager.INSTANCE.isCurrentRoute(info))
            	tv.setTextColor(Color.parseColor("#4f9b7a"));

            //CAPABILITIES
            tv = (TextView)v.findViewById(R.id.display_capability);
            tv.setText(info.getStatus());
            
            ImageView icon = (ImageView) findViewById(R.id.display_icon);
            
            if(info.getIconDrawable() != null){
            	icon.setImageDrawable(info.getIconDrawable());
            }
            
            if(!info.isEnabled())
            	v.setClickable(false);
            
            return v;
        }
        
        public void updateContents() {
        	Log.d(TAG, "update content");
            clear();
            addAll(RemoteDisplayManager.INSTANCE.getAllRoutes());
        }
    }
}
