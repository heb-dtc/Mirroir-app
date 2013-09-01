package com.flo.miroir;

import java.util.ArrayList;

import android.app.Activity;
import android.app.MediaRouteActionProvider;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaRouter;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class LocalVideoActivity extends Activity{

	private final String TAG = this.getClass().getName();
	
	private MediaRouter mMediaRouter;
	private VideoPlayerPrez mVideoPrez;
	
	private ListView mListView = null;
	private View mPlaybakControlView = null;
	private Button mPlayButton;
	private Button mStopButton;
	private Button mPreviousButton;
	private Button mNextButton;
	private ContentRowAdapter mVideoAdapter;
	
	private Cursor mCursorVideoStore;
	private ArrayList<ContentDetails> mVideoDetailsList = null;
	
	private int mCurrentPosInList = -1;
	private boolean mShowingPrez = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_local_video);
        
        mMediaRouter = (MediaRouter)getSystemService(Context.MEDIA_ROUTER_SERVICE);
        
        mListView = (ListView)findViewById(R.id.list_view);
        mPlaybakControlView = (View) findViewById(R.id.playback_controls_view);
        mPlayButton = (Button) findViewById(R.id.btn_play_video);
        mStopButton = (Button) findViewById(R.id.btn_stop_video);
        mPreviousButton = (Button) findViewById(R.id.btn_previous_video);
        mNextButton = (Button) findViewById(R.id.btn_next_video);
        
        mVideoDetailsList = new ArrayList<ContentDetails>();

        initUI();

        readPhoneMediaDataAsync();
    } 
        
    @Override
    protected void onResume() {
        // Be sure to call the super class.
        super.onResume();

        // Listen for changes to media routes.
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
    }
	
    @Override
    protected void onPause() {
    	super.onPause();
    	mMediaRouter.removeCallback(mMediaRouterCallback);
    }
    
    @Override
    protected void onDestroy() {
    	Log.e(TAG, "onDestroy");
    	super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.menu_media_route);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)mediaRouteMenuItem.getActionProvider();
        mediaRouteActionProvider.setRouteTypes(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        
		return true;
    }
    
    private OnItemClickListener mListItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			updateVideoPrez(position);
		}
    };
    
    /**
     * 
     * Create and set the adapter for the ListView.
     * 
     */
    private void initUI(){
    	mListView.setOnItemClickListener(mListItemClickListener);
    	
    	mPlayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mVideoPrez != null){
					mVideoPrez.pauseVideoPlayer();
				}
			}
		});
    	
    	mStopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mVideoPrez.stopVideoPlayer();
			}
		});
    	
    	mPreviousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int pos = mCurrentPosInList-1;
				if(pos >=0 && pos < mVideoDetailsList.size()){
					updateVideoPrez(pos);
				}
			}
		});
    	
    	mNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int pos = mCurrentPosInList+1;
				if(pos >=0 && pos < mVideoDetailsList.size()){
					updateVideoPrez(pos);
				}
			}
		});
    	
    	mPlaybakControlView.setVisibility(View.GONE);

    	mVideoAdapter = new ContentRowAdapter(getApplicationContext());
    	mListView.setAdapter(mVideoAdapter);
    }
    
    /**
     * Add item(s) to the list view adapter
     * 
     * @param item
     */
    private void addVideoToList(ContentDetails...item){
    	for (ContentDetails video : item) {  
    		mVideoAdapter.addVideo(video);  
    		mVideoAdapter.notifyDataSetChanged();  
    	}
    }
    
    /**
     * Call this to kick off the ReadDataTask.
     * 
     */
    private void readPhoneMediaDataAsync(){
		ReadDataTask readDataTask = new ReadDataTask();
		readDataTask.execute();
    }
	
    private void updateVideoPrez(int position){
    	Log.d(TAG, "updateVideoPrez: " + position);
    	
		if(mVideoPrez != null){
			if(position >= 0 && position < mVideoDetailsList.size()){
				ContentDetails item = mVideoDetailsList.get(position);
				mCurrentPosInList = position;
				mVideoPrez.startVideoPlayer(item);
				
				//add playback controls to UI
				mPlaybakControlView.setVisibility(View.VISIBLE);
			}
		}
    }
    
	String[] mVideoMediaColumns = {
    		MediaStore.Video.Media._ID,
    		MediaStore.Video.Media.DATA,
    		MediaStore.Video.Media.TITLE,
    		MediaStore.Video.Media.MIME_TYPE,
    		MediaStore.Video.Media.SIZE,
    		MediaStore.Video.Media.DURATION,
    		MediaStore.Video.Media.RESOLUTION 
    		};
    
	private ContentDetails collectLocalVideoMediaInfo() {
		ContentDetails info = new ContentDetails(mCursorVideoStore.getString(mCursorVideoStore.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
		
		info.setTitle(mCursorVideoStore.getString(mCursorVideoStore.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)));
		Log.v(TAG, "Title " + info.getTitle());
			
		info.setMimeType(mCursorVideoStore.getString(mCursorVideoStore.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)));
		Log.v(TAG, "Mime " + info.getMimeType());
		
		info.setSize(mCursorVideoStore.getString(mCursorVideoStore.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));
		Log.v(TAG, "size " + info.getSize());
		
		info.setDurationMediaStore(mCursorVideoStore.getString(mCursorVideoStore.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
	    Log.v(TAG, "duration " + info.getDurationMediaStore());
	        			
	    info.setResolution(mCursorVideoStore.getString(mCursorVideoStore.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION)));
	    Log.v(TAG, "resolution " + info.getResolution());
	    
	    //other way to get the thumbnail...
	    //Bitmap thumb = MediaStore.Video.Thumbnails.getThumbnail( getContentResolver(), id, MediaStore.Video.Thumbnails.MINI_KIND, null); 
		
		Log.v(TAG, "collectLocalVideoMediaInfo " + info.getFilePath());
		
		return info;
	}
	
	private void extractThumbnail(ContentDetails info, long frameAt){
    	Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		
		try {
			retriever.setDataSource(info.getFilePath());
			
			if(frameAt != 0){
				bitmap = retriever.getFrameAtTime(frameAt);
			}
			else{
				bitmap = retriever.getFrameAtTime();
			}
			
			if(bitmap != null){
				info.setThumbnail(ThumbnailUtils.extractThumbnail(bitmap, 128, 100, ThumbnailUtils.OPTIONS_RECYCLE_INPUT));
			}
			
		}
		catch (IllegalArgumentException ex) {
        } 
		catch (RuntimeException ex) {
        } 
		finally {
           try	{
                retriever.release();
           } 
           catch (RuntimeException ex) {
           }
		}    	
    }
	
	private class ReadDataTask extends AsyncTask<Void, ContentDetails, Boolean>{
    	
    	public ReadDataTask() {
        }
        
        @Override
        protected void onPreExecute() {
        	super.onPreExecute();
        }
		
        @Override
		protected Boolean doInBackground(Void... params) {
        	setProgressBarIndeterminateVisibility(true);   
        	
        	mCursorVideoStore = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mVideoMediaColumns, null, null, null);
        	if (mCursorVideoStore.moveToFirst()) {
        		do {
        			//get local media info
                    ContentDetails item = collectLocalVideoMediaInfo();
                    
                    Long duration =  Long.valueOf(item.getDurationMediaStore());
    				duration++;
    				Long framePosition = duration*200;
    				extractThumbnail(item, framePosition);
                    
    				//save it
                    mVideoDetailsList.add(item);
                    //publish --> update UI
                    publishProgress(item);
        		}while (mCursorVideoStore.moveToNext());
        	}
			return true;
		}
        
        @Override
        protected void onProgressUpdate(ContentDetails... item) {
        	addVideoToList(item);
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
			setProgressBarIndeterminateVisibility(false);   
        }
    }
	
	private class ContentRowAdapter extends BaseAdapter{
    	private Context context;
    	private ArrayList<ContentDetails> items = new ArrayList<ContentDetails>();
    	
    	private LayoutInflater inflater;
    	
    	public ContentRowAdapter(Context appContext) {
    		context = appContext;
    		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	}
    	
    	public void addVideo(ContentDetails item){
    		items.add(item);
    	}
    	
		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int pos) {
			return items.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			final View contentRow;
			
			if (convertView == null){
				contentRow = inflater.inflate(R.layout.video_list_row, null);
			}
			else{
				contentRow = convertView;
			}
			
			ImageView videoThumbView = (ImageView) contentRow.findViewById(R.id.video_thumbnail);
			
			ContentDetails item = items.get(pos);

			if (item.getThumbnail() != null){
				videoThumbView.setImageBitmap(item.getThumbnail()); 
			}
			else{
				videoThumbView.setImageResource(R.drawable.default_video_background);
			}
				
			TextView title = (TextView) contentRow.findViewById(R.id.content_title);
			title.setText(item.getTitle());
			
			TextView type = (TextView) contentRow.findViewById(R.id.content_type);
			type.setText(item.getMimeType());
			
			TextView durationMediaStore = (TextView) contentRow.findViewById(R.id.content_duration_media_store);
			durationMediaStore.setText(item.getDurationMediaStore());
			
			return contentRow;
		}
    }
	
	/**
	 * 
	 * MEDIA ROUTER RELATED METHODS
	 * 
	 */
	
	private void startVideoPlayerPresentation(){
		Log.d(TAG, "startVideoPlayerPresentation");
		
		// Get the current route and its presentation display.
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
  
        if (route != null) {
        	Display presentationDisplay = route.getPresentationDisplay();
        	if (presentationDisplay != null) {
        		//create the prez
        		mVideoPrez = new VideoPlayerPrez(this, presentationDisplay);
        		
        		//show it
        		mVideoPrez.show();
        	}
        }
	}
	
	private final MediaRouter.SimpleCallback mMediaRouterCallback =
		    new MediaRouter.SimpleCallback() {
		        @Override
		        public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
		            Log.e(TAG, "onRouteSelected: type=" + type + ", info=" + info);
		            if(info.getPlaybackType() == MediaRouter.RouteInfo.PLAYBACK_TYPE_LOCAL){
		            }
		            else{
		            }
		        }
		
		        @Override
		        public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
		            Log.e(TAG, "onRouteUnselected: type=" + type + ", info=" + info);
		            if(info.getPlaybackType() == MediaRouter.RouteInfo.PLAYBACK_TYPE_LOCAL){
		            }
		            else{
		            }
		        }
		
		        @Override
		        public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
		            Log.e(TAG, "onRoutePresentationDisplayChanged: info=" + info);
		            if(info.getPlaybackType() == MediaRouter.RouteInfo.PLAYBACK_TYPE_LOCAL){
		            }
		            else{
		            	//if remote and if the prez is not shown 
		            	//it means the prez should be loaded
		            	if(!mShowingPrez){
		            		startVideoPlayerPresentation();
		            		mShowingPrez = true;
		            	}
		            	else{
		            		mShowingPrez = false;
		            		
		            		//update UI
		            		mPlaybakControlView.setVisibility(View.GONE);
		            	}
		            }
		        }
	};
    
}
