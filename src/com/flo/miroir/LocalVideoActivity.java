package com.flo.miroir;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class LocalVideoActivity extends Activity implements IPrezCallbacks, SeekBar.OnSeekBarChangeListener {

	private final String TAG = this.getClass().getName();
	
	private ListView mListView = null;
	private ContentRowAdapter mVideoAdapter;
	private View mPlaybakControlView = null;
	private View mPlaybakControlViewBottom = null;
	
	//PCV controls
	private ImageView mPcvVideoThumbnailView;
	private ImageButton mPcvPlayButton;
	private TextView mPcvVideoName;
	private SeekBar mPcvProgressBar;
	/*private Button mStopButton;
	private Button mPreviousButton;
	private Button mNextButton;*/
	
	private Cursor mCursorVideoStore;
	private ArrayList<ContentDetails> mVideoDetailsList = null;
	
	private ReadDataTask mReadDataTask;
	
	private int mCurrentPosInList = -1;
	private boolean mIsPaused = false;
	private boolean mUpdateSeekBar = true;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_local_video);
        
        ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle("Video Gallery");

        mListView = (ListView)findViewById(R.id.list_view);
        mPlaybakControlView = (View) findViewById(R.id.playback_controls_view);
        mPlaybakControlViewBottom = (View) findViewById(R.id.pcv_bottom_view);
        
        mPcvPlayButton = (ImageButton) findViewById(R.id.pcv_btn_play_video);
        /*mStopButton = (Button) findViewById(R.id.btn_stop_video);
        mPreviousButton = (Button) findViewById(R.id.btn_previous_video);
        mNextButton = (Button) findViewById(R.id.btn_next_video);*/
        mPcvVideoThumbnailView = (ImageView) findViewById(R.id.pcv_video_tn);
        mPcvProgressBar = (SeekBar) findViewById(R.id.pcv_seek_bar);
        mPcvVideoName = (TextView) findViewById(R.id.pcv_video_name);
        
        mVideoDetailsList = new ArrayList<ContentDetails>();

        initUI();

        readPhoneMediaDataAsync();
    } 
        
    @Override
    protected void onResume() {
        // Be sure to call the super class.
        super.onResume();
        RemoteDisplayManager.INSTANCE.displayVideoPresentation(this);
        RemoteDisplayManager.INSTANCE.registerVideoPlayerCallback(this);
    }
	
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	RemoteDisplayManager.INSTANCE.stopVideoPlayer();
    	RemoteDisplayManager.INSTANCE.hideVideoPlayerPresentation();
    }
    
    @Override
    protected void onDestroy() {
    	Log.e(TAG, "onDestroy");
    	super.onDestroy();
    	
    	//clean
    	if(mReadDataTask != null){
    		mReadDataTask.cancel(true);
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        
		return true;
    }
    
    private OnItemClickListener mListItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
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
    	
    	mPcvProgressBar.setOnSeekBarChangeListener(this);
    	
    	mPcvPlayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mIsPaused){
					mPcvPlayButton.setImageDrawable(getResources().getDrawable( R.drawable.pause_light));
					RemoteDisplayManager.INSTANCE.resumeVideoPlayer();
					mIsPaused = false;
				}
				else{
					mPcvPlayButton.setImageDrawable(getResources().getDrawable( R.drawable.play_light));
					RemoteDisplayManager.INSTANCE.pauseVideoPlayer();
					mIsPaused = true;
				}
				
			}
		});
    	
    	/*mStopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RemoteDisplayManager.INSTANCE.stopVideoPlayer();
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
		});*/
    	
    	mPlaybakControlView.setVisibility(View.GONE);
    	mPlaybakControlViewBottom.setVisibility(View.GONE);

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
		mReadDataTask = new ReadDataTask();
		mReadDataTask.execute();
    }
    
    private void playNext(){
    	int pos = mCurrentPosInList+1;
		if(pos >=0 && pos < mVideoDetailsList.size()){
			updateVideoPrez(pos);
		}
    }
	
    private void updateVideoPrez(int position){
    	Log.d(TAG, "updateVideoPrez: " + position);

		if(position >= 0 && position < mVideoDetailsList.size()){
			ContentDetails item = mVideoDetailsList.get(position);
			mCurrentPosInList = position;
			RemoteDisplayManager.INSTANCE.updateVideoPlayerPresentation(item);
				
			//add playback controls to UI
			mPlaybakControlView.setVisibility(View.VISIBLE);
			mPlaybakControlViewBottom.setVisibility(View.VISIBLE);
			
			//update TN
			if(item.getThumbnail() != null)
				mPcvVideoThumbnailView.setImageBitmap(item.getThumbnail());
			//update video name
			mPcvVideoName.setText(item.getTitle());
			
			mPcvPlayButton.setImageDrawable(getResources().getDrawable( R.drawable.pause_light));
			mIsPaused = false;
			
			//init progress bar
			mPcvProgressBar.setProgress(0);
			mPcvProgressBar.setMax(100);
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
		
		info.setDuration(mCursorVideoStore.getString(mCursorVideoStore.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
	    Log.v(TAG, "duration " + info.getDuration());
	        			
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
        	
        	boolean keepGoing = true;
        	
        	mCursorVideoStore = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mVideoMediaColumns, null, null, null);
        	if (mCursorVideoStore.moveToFirst()) {
        		do {
        			if(isCancelled())
        				keepGoing = false;
        			
        			//get local media info
                    ContentDetails item = collectLocalVideoMediaInfo();
                    
                    Long duration =  Long.valueOf(item.getDuration());
    				duration++;
    				Long framePosition = duration*200;
    				extractThumbnail(item, framePosition);
                    
    				//save it
                    mVideoDetailsList.add(item);
                    //publish --> update UI
                    publishProgress(item);
        		}while (mCursorVideoStore.moveToNext() && keepGoing);
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
        
        @Override
        protected void onCancelled() {
        	setProgressBarIndeterminateVisibility(false);
        	super.onCancelled();
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
				contentRow = inflater.inflate(R.layout.row_video_item_list, null);
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
			durationMediaStore.setText(item.getDuration());
			
			return contentRow;
		}
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		//stop updating the progress bar since the user is now moving it
		mUpdateSeekBar = false;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		RemoteDisplayManager.INSTANCE.seekToVideoPlayer(seekBar.getProgress());
		//keep updating the seek bar
		mUpdateSeekBar = true;
	}

	@Override
	public void onProgressChanged(int value) {
		Log.e(TAG, "onProgressChanged: " + value);
		
		if(mPcvProgressBar.getProgress() != value && mUpdateSeekBar){
			mPcvProgressBar.setProgress(value);
		}
	}

	@Override
	public void onPlaybackCompleted() {
		Log.e(TAG, "onPlaybackCompleted");
		//set the correct UI
		mPcvPlayButton.setImageDrawable(getResources().getDrawable( R.drawable.play_light));
		mIsPaused = true;
		
		playNext();
	}
}
