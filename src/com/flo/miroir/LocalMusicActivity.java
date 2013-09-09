package com.flo.miroir;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class LocalMusicActivity extends Activity implements IPrezCallbacks, SeekBar.OnSeekBarChangeListener {

	private final String TAG = this.getClass().getName();
	
	private ListView mListView = null;
	private View mPlaybakControlView = null;
	private View mPlaybakControlViewBottom = null;
	
	//PCV controls
	private ImageView mPcvAlbumThumbnailView;
	private ImageButton mPcvPlayButton;
	private TextView mPcvSongName;
	private TextView mPcvArtistName;
	private SeekBar mPcvProgressBar;
	
	/*private Button mStopButton;
	private Button mPreviousButton;
	private Button mNextButton;*/
	private ContentRowAdapter mAudioAdapter;
	
	private Cursor mCursorAudioStore;
	private ArrayList<ContentDetails> mAudioDetailsList = null;
	
	private int mCurrentPosInList = -1;
	private boolean mIsPaused = false;
	private boolean mUpdateSeekBar = true;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_local_music);
        
        ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle("Music Gallery");
        
        mListView = (ListView)findViewById(R.id.list_view);
        mPlaybakControlView = (View) findViewById(R.id.playback_controls_view);
        mPlaybakControlViewBottom = (View) findViewById(R.id.pcv_bottom_view);
        
        mPcvPlayButton = (ImageButton) findViewById(R.id.pcv_btn_play);
        mPcvSongName = (TextView) findViewById(R.id.pcv_song_name);
        mPcvArtistName = (TextView) findViewById(R.id.pcv_artist_name);
        mPcvProgressBar = (SeekBar) findViewById(R.id.pcv_seek_bar);
        mPcvAlbumThumbnailView = (ImageView)findViewById(R.id.pcv_album_tn);
        
        /*mStopButton = (Button) findViewById(R.id.btn_stop_video);
        mPreviousButton = (Button) findViewById(R.id.btn_previous_video);
        mNextButton = (Button) findViewById(R.id.btn_next_video);*/
        
        mAudioDetailsList = new ArrayList<ContentDetails>();

        initUI();

        readPhoneMediaDataAsync();
    } 
	
	 @Override
	 protected void onResume() {
		 // Be sure to call the super class.
		 super.onResume();
		 RemoteDisplayManager.INSTANCE.displayAudioPresentation(this);
		 RemoteDisplayManager.INSTANCE.registerAudioPlayerCallback(this);
	 }
		
	 @Override
	 protected void onPause() {
		 super.onPause();
		 RemoteDisplayManager.INSTANCE.stopAudioPlayer();
		 RemoteDisplayManager.INSTANCE.hideAudioPlayerPresentation();
	 }
	    
	 @Override
	 protected void onDestroy() {
		 Log.e(TAG, "onDestroy");
		 super.onDestroy();
	 }
	    
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {
		 getMenuInflater().inflate(R.menu.main, menu);

		 return true;
	 }
	    
	 private OnItemClickListener mListItemClickListener = new OnItemClickListener(){
		 @Override
		 public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			 updateAudioPrez(position);
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
						RemoteDisplayManager.INSTANCE.resumeAudioPlayer();
						mIsPaused = false;
					}
					else{
						mPcvPlayButton.setImageDrawable(getResources().getDrawable( R.drawable.play_light));
						RemoteDisplayManager.INSTANCE.pauseAudioPlayer();
						mIsPaused = true;
					}
				}
			});
	    	
	    	/*mStopButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					RemoteDisplayManager.INSTANCE.stopAudioPlayer();
				}
			});
	    	
	    	mPreviousButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = mCurrentPosInList-1;
					if(pos >=0 && pos < mAudioDetailsList.size()){
						updateAudioPrez(pos);
					}
				}
			});
	    	
	    	mNextButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = mCurrentPosInList+1;
					if(pos >=0 && pos < mAudioDetailsList.size()){
						updateAudioPrez(pos);
					}
				}
			});*/
	    	
	    	mPlaybakControlView.setVisibility(View.GONE);
	    	mPlaybakControlViewBottom.setVisibility(View.GONE);

	    	mAudioAdapter = new ContentRowAdapter(getApplicationContext());
	    	mListView.setAdapter(mAudioAdapter);
	    }
	    
	    /**
	     * Add item(s) to the list view adapter
	     * 
	     * @param item
	     */
	    private void addAudioItemToList(ContentDetails...item){
	    	for (ContentDetails song : item) {  
	    		mAudioAdapter.addAudioItem(song);  
	    		mAudioAdapter.notifyDataSetChanged();  
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
	    
	    private void playNext(){
	    	int pos = mCurrentPosInList+1;
			if(pos >=0 && pos < mAudioDetailsList.size()){
				updateAudioPrez(pos);
			}
	    }
	    
	    private void updateAudioPrez(int position){
	    	Log.d(TAG, "updateAudioPrez: " + position);

			if(position >= 0 && position < mAudioDetailsList.size()){
				ContentDetails item = mAudioDetailsList.get(position);
				mCurrentPosInList = position;
				RemoteDisplayManager.INSTANCE.updateAudioPlayerPresentation(item);
				
				//add playback controls to UI
				mPlaybakControlView.setVisibility(View.VISIBLE);
				mPlaybakControlViewBottom.setVisibility(View.VISIBLE);
				
				//update song and artist names
				mPcvSongName.setText(item.getTitle());
				mPcvArtistName.setText(item.getArtist());
				
				//update AA
				if(item.getThumbnail() != null)
					mPcvAlbumThumbnailView.setImageBitmap(item.getThumbnail());
				
				mPcvPlayButton.setImageDrawable(getResources().getDrawable( R.drawable.pause_light));
				mIsPaused = false;
				
				//init progress bar
				mPcvProgressBar.setProgress(0);
				mPcvProgressBar.setMax(100);
			}
	    }
	    
		String[] mAudioMediaColumns = {
	    		MediaStore.Audio.Media._ID,
	    		MediaStore.Audio.Media.DATA,
	    		MediaStore.Audio.Media.TITLE,
	    		MediaStore.Audio.Media.MIME_TYPE,
	    		MediaStore.Audio.Media.SIZE,
	    		MediaStore.Audio.Media.DURATION,
	    		MediaStore.Audio.Media.ARTIST,
	    		MediaStore.Audio.Media.ALBUM_ID
	    		};
		
		String[] mAudioAlbumColumns = {
	    		MediaStore.Audio.Albums._ID,
	    		MediaStore.Audio.Albums.ALBUM_ART
	    		};
		
		private ContentDetails collectLocalAudioMediaInfo() {
			ContentDetails info = new ContentDetails(mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
    		
    		info.setTitle(mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
			Log.v(TAG, "Title " + info.getTitle());
				
			info.setMimeType(mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)));
			Log.v(TAG, "Mime " + info.getMimeType());
			
			info.setSize(mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));
			Log.v(TAG, "size " + info.getSize());

			info.setDuration(mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
            Log.v(TAG, "duration " + info.getDuration());
            
			info.setArtist(mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
            Log.v(TAG, "artist " + info.getArtist());
            
            //try to get potential AA
            String albId = mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            Log.v(TAG, "Album ID: " + albId);
            
            Cursor cursorAlbum = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, mAudioAlbumColumns,
            		MediaStore.Audio.Albums._ID+ "=" + albId, null, null);
            
            if(cursorAlbum != null  && cursorAlbum.moveToFirst()){

              String uri = cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
              cursorAlbum.close();
              
              if(uri != null ){    
            	  //info.setThumbnail(BitmapFactory.decodeFile(uri));
            	  info.setThumbnail(Utils.decodeSampledBitmapFromUri(uri, 80, 80));
            	  Log.v(TAG, "AA: " + uri);
              }
            } 
            
			Log.v(TAG, "collectLocalAudioMediaInfo " + info.getFilePath());
			
			return info;
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
	        	
	        	mCursorAudioStore = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mAudioMediaColumns, 
	        			MediaStore.Audio.Media.IS_MUSIC + "=1", null, null);
	        	if (mCursorAudioStore.moveToFirst()) {
	        		do {
	        			//get local media info
	                    ContentDetails item = collectLocalAudioMediaInfo();
	                    
	    				//save it
	                    mAudioDetailsList.add(item);
	                    //publish --> update UI
	                    publishProgress(item);
	        		}while (mCursorAudioStore.moveToNext());
	        	}
				return true;
			}
	        
	        @Override
	        protected void onProgressUpdate(ContentDetails... item) {
	        	addAudioItemToList(item);
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
	    	
	    	public void addAudioItem(ContentDetails item){
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
					contentRow = inflater.inflate(R.layout.row_audio_item_list, null);
				}
				else{
					contentRow = convertView;
				}
				
				ImageView audioAlbumImgView = (ImageView) contentRow.findViewById(R.id.audio_album_img);
				
				ContentDetails item = items.get(pos);

				if (item.getThumbnail() != null){
					audioAlbumImgView.setImageBitmap(item.getThumbnail()); 
				}
				else{
					audioAlbumImgView.setImageResource(R.drawable.default_video_background);
				}
					
				TextView title = (TextView) contentRow.findViewById(R.id.song_title);
				title.setText(item.getTitle());
				
				TextView artist = (TextView) contentRow.findViewById(R.id.song_artist);
				artist.setText(item.getArtist());
				
				return contentRow;
			}
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
		//set UI in correct state
		mPcvPlayButton.setImageDrawable(getResources().getDrawable( R.drawable.play_light));
		mIsPaused = true;
		
		playNext();
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
		RemoteDisplayManager.INSTANCE.seekToAudioPlayer(seekBar.getProgress());
		//keep updating the seek bar
		mUpdateSeekBar = true;
	}
}
