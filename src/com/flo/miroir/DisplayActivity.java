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
					if(!info.getName().equals(Utils.LocalRouteName)){
						showStandByPrez();
    	            }
				}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_list);

        mDisplayListAdapter = new DisplayListAdapter(this);
        mListView = (ListView)findViewById(R.id.display_list_view);
        mListView.setAdapter(mDisplayListAdapter);
        
        mListView.setOnItemClickListener(displayListListener);
    }
    
    protected void onResume() {
        super.onResume();
        mDisplayListAdapter.updateContents();
        RemoteDisplayManager.getInstance().subscribeToCallabcks(mMediaRouterCallbacks);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	RemoteDisplayManager.getInstance().unsubscribeToCallbacks(mMediaRouterCallbacks);
    	RemoteDisplayManager.getInstance().hideStandByPresentation();
    }
    
    private OnItemClickListener displayListListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			RouteInfo info = mDisplayListAdapter.getItem(position);
			makeConnection(info);
		}
	};
    
	private void makeConnection(RouteInfo info){
		RemoteDisplayManager.getInstance().selectRoute(info);
	}
	
	private void showStandByPrez(){
		Log.d(TAG, "showStandByPrez");
		RemoteDisplayManager.getInstance().displayStandByPresentation(this);
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

            addAll(RemoteDisplayManager.getInstance().getAllRoutes());
        }
    }
}
