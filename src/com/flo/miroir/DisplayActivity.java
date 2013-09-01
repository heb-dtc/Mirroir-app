package com.flo.miroir;

import android.app.Activity;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DisplayActivity extends Activity {
	
	private final String TAG = this.getClass().getName();
	
	//display manager
	private DisplayManager mDisplayManager;
	private DisplayListAdapter mDisplayListAdapter;
	
	//Media Router
	private MediaRouter mMediaRouter;
	private RouteInfo mSelectedRoute;
	
	private ListView mListView;
    //private final SparseArray<RemotePresentation> mActivePresentations = new SparseArray<RemotePresentation>();
    
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {
            mDisplayListAdapter.updateContents();
        }

        @Override
        public void onDisplayChanged(int displayId) {
            mDisplayListAdapter.updateContents();
        }

        @Override
        public void onDisplayRemoved(int displayId) {
            mDisplayListAdapter.updateContents();
        }
    };
    
    private final MediaRouter.SimpleCallback mMediaRouterCallback =
    	    new MediaRouter.SimpleCallback() {
    	        @Override
    	        public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
    	            Log.d(TAG, "onRouteSelected: type=" + type + ", info=" + info);

    	        }
    	
    	        @Override
    	        public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
    	            Log.d(TAG, "onRouteUnselected: type=" + type + ", info=" + info);
    	        }
    	
    	        @Override
    	        public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
    	            Log.d(TAG, "onRoutePresentationDisplayChanged: info=" + info);
    	            
    	            if(info.getName().equals(mSelectedRoute.getName())){
    	            	showStandByPrez();
    	            }
    	        }
        	};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_list);
        
        //get the Media Router Service
        mMediaRouter = (MediaRouter)getSystemService(Context.MEDIA_ROUTER_SERVICE);
        
        //tmp();
        
        mDisplayManager = (DisplayManager)getSystemService(Context.DISPLAY_SERVICE);
        
        mDisplayListAdapter = new DisplayListAdapter(this);
        mListView = (ListView)findViewById(R.id.display_list_view);
        mListView.setAdapter(mDisplayListAdapter);
        
        mListView.setOnItemClickListener(displayListListener);
    }
    
    protected void onResume() {
        super.onResume();
        mDisplayListAdapter.updateContents();
        mDisplayManager.registerDisplayListener(mDisplayListener, null);
        
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	mDisplayManager.unregisterDisplayListener(mDisplayListener);
    	mMediaRouter.removeCallback(mMediaRouterCallback);
    }
    
    private OnItemClickListener displayListListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			RouteInfo info = mDisplayListAdapter.getItem(position);
			makeConnection(info);
		}
	};
    
	private void makeConnection(RouteInfo info){
		mSelectedRoute = info;
		//set up the connection
		mMediaRouter.selectRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, info);
	}
	
	private void showStandByPrez(){
		Log.d(TAG, "showStandByPrez");
        //display stand by prez
        //Display presentationDisplay = mSelectedRoute.getPresentationDisplay();
        
		MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        Display presentationDisplay = route.getPresentationDisplay();
        
    	if (presentationDisplay != null) {
    		Log.d(TAG, "we have a display");
    		//create the prez
    		StandByPrez prex = new StandByPrez(this, presentationDisplay);
    		
    		//show it
    		prex.show();
    	}
	}
	
    private void tmp(){
    	int nrRoutes = mMediaRouter.getRouteCount();
    	
    	for(int i=0 ; i < nrRoutes ; i++){
    		RouteInfo info = mMediaRouter.getRouteAt(i);
    		Log.e(TAG, "Route " + i + ": " + info.getName() + ", enabled: " + info.isEnabled());
    	}
    }
    
    private final class DisplayListAdapter extends ArrayAdapter<RouteInfo> {
        final Context mContext;
        
        public DisplayListAdapter(Context context) {
            super(context, R.layout.display_item_list_row);
            mContext = context;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	final View v;
            if (convertView == null) {
                v = ((Activity) mContext).getLayoutInflater().inflate(R.layout.display_item_list_row, null);
            } else {
                v = convertView;
            }

            final RouteInfo info = getItem(position);
            
            //NAME
            TextView tv = (TextView)v.findViewById(R.id.display_title);
            tv.setText(info.getName());

            //CAPABILITIES
            tv = (TextView)v.findViewById(R.id.display_capability);
            tv.setText(info.getStatus());
            
            ImageView icon = (ImageView) findViewById(R.id.display_icon);
            
            if(info.getIconDrawable() != null){
            	icon.setImageDrawable(info.getIconDrawable());
            }
            
            return v;
        }
        
        public void updateContents() {
            clear();
            
            int nrRoutes = mMediaRouter.getRouteCount();
            RouteInfo[] routes = new RouteInfo[nrRoutes];
        	for(int i=0 ; i < nrRoutes ; i++){
        		routes[i] = mMediaRouter.getRouteAt(i);
        	}

            addAll(routes);
        }
    }
}
