package com.flo.miroir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class LocalMusicActivity extends Activity{

	private final String TAG = this.getClass().getName();
	
	private ListView mListView = null;
	private View mPlaybakControlView = null;
	private Button mPlayButton;
	private Button mStopButton;
	private Button mPreviousButton;
	private Button mNextButton;
	private ContentRowAdapter mAudioAdapter;
	
	private Cursor mCursorAudioStore;
	private ArrayList<ContentDetails> mAudioDetailsList = null;
	
	private int mCurrentPosInList = -1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_local_video);
        
        mListView = (ListView)findViewById(R.id.list_view);
        mPlaybakControlView = (View) findViewById(R.id.playback_controls_view);
        mPlayButton = (Button) findViewById(R.id.btn_play_video);
        mStopButton = (Button) findViewById(R.id.btn_stop_video);
        mPreviousButton = (Button) findViewById(R.id.btn_previous_video);
        mNextButton = (Button) findViewById(R.id.btn_next_video);
        
        mAudioDetailsList = new ArrayList<ContentDetails>();

        initUI();

        readPhoneMediaDataAsync();
    } 
	
	 @Override
	    protected void onResume() {
	        // Be sure to call the super class.
	        super.onResume();
	        RemoteDisplayManager.getInstance().displayAudioPresentation(this);
	    }
		
	    @Override
	    protected void onPause() {
	    	super.onPause();
	    	RemoteDisplayManager.getInstance().hideAudioPlayerPresentation();
	    }
	    
	    @Override
	    protected void onDestroy() {
	    	Log.e(TAG, "onDestroy");
	    	super.onDestroy();
	    }
	    
	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        getMenuInflater().inflate(R.menu.main, menu);
	        
	        /*MenuItem mediaRouteMenuItem = menu.findItem(R.id.menu_media_route);
	        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)mediaRouteMenuItem.getActionProvider();
	        mediaRouteActionProvider.setRouteTypes(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
	        */
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
	    	
	    	mPlayButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					RemoteDisplayManager.getInstance().pauseAudioPlayer();
				}
			});
	    	
	    	mStopButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					RemoteDisplayManager.getInstance().stopAudioPlayer();
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
			});
	    	
	    	mPlaybakControlView.setVisibility(View.GONE);

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
	    
	    private void updateAudioPrez(int position){
	    	Log.d(TAG, "updateVideoPrez: " + position);

			if(position >= 0 && position < mAudioDetailsList.size()){
				ContentDetails item = mAudioDetailsList.get(position);
				mCurrentPosInList = position;
				RemoteDisplayManager.getInstance().updateAudioPlayerPresentation(item);
				
				//add playback controls to UI
				mPlaybakControlView.setVisibility(View.VISIBLE);
			}
	    }
	    
		String[] mAudioMediaColumns = {
	    		MediaStore.Audio.Media._ID,
	    		MediaStore.Audio.Media.DATA,
	    		MediaStore.Audio.Media.TITLE,
	    		MediaStore.Audio.Media.MIME_TYPE,
	    		MediaStore.Audio.Media.SIZE,
	    		MediaStore.Audio.Media.DURATION,
	    		MediaStore.Audio.Media.ARTIST
	    		};
		
		String[] mAudioAlbumColumns = {
	    		MediaStore.Audio.Albums._ID,
	    		MediaStore.Audio.Albums.ALBUM_ART,
	    		MediaStore.Audio.Albums.ARTIST,
	    		MediaStore.Audio.Albums.ALBUM
	    		};
		
		private ContentDetails collectLocalAudioAlbumsInfo() {
			Cursor cursorAlbums = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, mAudioAlbumColumns, null, null, null);
			
			if (cursorAlbums.moveToFirst()) {
        		do {
			
					ContentDetails info = new ContentDetails("");
		    		
		    		info.setTitle(cursorAlbums.getString(cursorAlbums.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)));
					Log.v("collectLocalAudioAlbumsInfo", "Title " + info.getTitle());
						
					
					String aaUri = cursorAlbums.getString(cursorAlbums.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
					
					if(aaUri != null){
						Log.v("collectLocalAudioAlbumsInfo", "aaUri " + aaUri);
						
						Uri artworkUri = Uri.parse(aaUri);
						
						Bitmap bitmap;
						try {
							bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), artworkUri);
							if(bitmap != null){
								Log.v("collectLocalAudioAlbumsInfo", "bitmap set ");
		                    	info.setThumbnail(bitmap);
		                    }
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
					}
					}
					info.setArtist(cursorAlbums.getString(cursorAlbums.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)));
		            Log.v("collectLocalAudioAlbumsInfo", "artist " + info.getArtist());
					
        		}while (cursorAlbums.moveToNext());
			}
			
			return null;
		}
		
		private ContentDetails collectLocalAudioMediaInfo() {
			ContentDetails info = new ContentDetails(mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
    		
    		info.setTitle(mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
			Log.v(TAG, "Title " + info.getTitle());
				
			info.setMimeType(mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)));
			Log.v(TAG, "Mime " + info.getMimeType());
			
			info.setSize(mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));
			Log.v(TAG, "size " + info.getSize());

			info.setDurationMediaStore(mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
            Log.v(TAG, "duration " + info.getDurationMediaStore());
            
			info.setArtist(mCursorAudioStore.getString(mCursorAudioStore.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
            Log.v(TAG, "artist " + info.getArtist());
			
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
	        	
	        	collectLocalAudioAlbumsInfo();
	        	
	        	mCursorAudioStore = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mAudioMediaColumns, null, null, null);
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
}
