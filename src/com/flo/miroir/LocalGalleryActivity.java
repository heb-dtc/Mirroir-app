package com.flo.miroir;

import java.util.ArrayList;

import android.app.Activity;
import android.app.MediaRouteActionProvider;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class LocalGalleryActivity extends Activity {
	
	private final String TAG = this.getClass().getName();
	
	private MediaRouter mMediaRouter;
	private ImagePlayerPrez mImgPrez;
	
	private GridView mImgGrid;
	private ImageAdapter mImgAdpt;
	
	private Cursor mCursor;
	private int columnIndex; //idx of the ID column
	//private Uri mUri = android.provider.MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
	
	private boolean mShowingPrez = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_local_gallery);
		
		//get the Media Router Service
		mMediaRouter = (MediaRouter)getSystemService(Context.MEDIA_ROUTER_SERVICE);

		
		mImgGrid = (GridView)findViewById(R.id.gridview);
		
		mImgAdpt = new ImageAdapter(this);
		mImgGrid.setAdapter(mImgAdpt);
		
		mImgGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView parent, View v, int position, long id) { 
	            String[] projection = {MediaStore.Images.Media.DATA};  
	            
	            Cursor cursor = getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  
	                    projection, 
	                    null,  
	                    null,  
	                    null);  
	            
	            if(cursor != null){
		            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  
		            cursor.moveToPosition(position);  
		            
		            // Get image filename  
		            String imagePath = cursor.getString(idx);
		            updatePrezImg(imagePath);
		            
		            //clean up
	                cursor.close();  
	                projection = null;  
	            }
			}
		});
		
		LoadImgAsyncTask task = new LoadImgAsyncTask();
		task.execute();
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
    
	protected void onDestroy() {  
        super.onDestroy();  
        final GridView grid = mImgGrid;  
        final int count = grid.getChildCount();  
        ImageView v = null;  
        
        for (int i = 0; i < count; i++) {  
            v = (ImageView) grid.getChildAt(i);  
            ((BitmapDrawable) v.getDrawable()).setCallback(null);  
        }  
    }  
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.menu_media_route);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)mediaRouteMenuItem.getActionProvider();
        mediaRouteActionProvider.setRouteTypes(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);

		return true;
	}

	private void test(){
		Bitmap[] thumbnails;
		String[] arrPath;
		
		final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media._ID;
        Cursor imagecursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy);
        
        int image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
        
        Log.d(TAG, "There are " + imagecursor.getCount() + " items");
        
        int count = imagecursor.getCount();
        thumbnails = new Bitmap[count];
        arrPath = new String[count];
        
        for (int i = 0; i < count; i++) {
            imagecursor.moveToPosition(i);
            int id = imagecursor.getInt(image_column_index);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(
                    getApplicationContext().getContentResolver(), id,
                    MediaStore.Images.Thumbnails.MICRO_KIND, null);
            arrPath[i]= imagecursor.getString(dataColumnIndex);
        }
        
        imagecursor.close();
	}

	/**
	 * 
	 * LOCAL IMG RELATED METHODS
	 * 
	 */

    private void addImage(LoadedImage... value) {  
        for (LoadedImage image : value) {  
        	mImgAdpt.addPhoto(image);  
        	mImgAdpt.notifyDataSetChanged();  
        }  
    }  
	
	private class ImageAdapter extends BaseAdapter {
	     
		private Context mContext;
		private ArrayList<LoadedImage> mImgList = new ArrayList<LoadedImage>();  
		
	    public ImageAdapter(Context c) {
	    	mContext = c; 
	    }
	    
	    public void addPhoto(LoadedImage img) {   
	    	mImgList.add(img);   
        }  
	    
	    @Override
	    public int getCount() {
	    	return mImgList.size();  
	    }

	    @Override
	    public Object getItem(int position) {
	    	return mImgList.get(position); 
	    }

	    @Override
	    public long getItemId(int position) {
	    	return position;
	    }
	  
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	final ImageView imageView;
	    	if (convertView == null) {  // if it's not recycled, initialize some attributes
	    		imageView = new ImageView(mContext);
	    	} else {
	    		imageView = (ImageView) convertView;
	    	}
	    	
	    	imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);  
            imageView.setPadding(8, 8, 8, 8);  
            imageView.setImageBitmap(mImgList.get(position).getBitmap());

	    	return imageView;
	    }
	}
	
	private static class LoadedImage {  
        Bitmap mBitmap;  
  
        LoadedImage(Bitmap bitmap) {  
            mBitmap = bitmap;  
        }  
  
        public Bitmap getBitmap() {  
            return mBitmap;  
        }  
    }  
	
	/**
	 * 
	 * ASYNC TASK FOR LOADING IMG
	 * 
	 */
	private class LoadImgAsyncTask extends AsyncTask<Object, LoadedImage, Object> {	
		@Override
		public void onPreExecute(){
	 	}
		
		@Override
		protected Void doInBackground(Object...params) {
			setProgressBarIndeterminateVisibility(true);   
			
			Bitmap bitmap = null;  
	        Bitmap newBitmap = null;  
	        //Uri uri = null;        
			
			//String[] projection = {MediaStore.Images.Thumbnails._ID};
	        String[] projection = {MediaStore.Images.Media._ID};
			//Do the query
			//mCursor = getContentResolver().query( mUri,
	        mCursor = getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			                projection,
			                null,
			                null,
			                null);
			
			//security checks, exit if needed
			if (mCursor == null) {
	            // Query failed...
	            Log.e(TAG, "Failed to retrieve image: cursor is null");
	            return null;
	        }
	        if (mCursor.getCount() == 0) {
	            Log.e(TAG, "Failed to move cursor to first row (no query results).");
	            return null;
	        }
			Log.d(TAG, "There are " + mCursor.getCount() + " items");
			
			columnIndex = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
			
			//make the bitmap one by one and update UI accordingly
			int imageID = 0;  
	        for (int i = 0; i < mCursor.getCount(); i++) {  
	        	mCursor.moveToPosition(i);  
	            imageID = mCursor.getInt(columnIndex);  

	            //uri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, "" + imageID);  
	            try {  
	                //bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));  
	                bitmap = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), imageID, MediaStore.Images.Thumbnails.MINI_KIND, null);
	                if (bitmap != null) {  
	                    newBitmap = Bitmap.createScaledBitmap(bitmap, 96, 96, true);  
	                    bitmap.recycle();  
	                    if (newBitmap != null) {  
	                        publishProgress(new LoadedImage(newBitmap));  
	                    }  
	                }  
	            } catch (Exception e) {  
	                //Error fetching image, try to recover  
	            }  
	        }  
	        
	        mCursor.close();  
			return null;
		}
		
		@Override  
        public void onProgressUpdate(LoadedImage... value) {  
            addImage(value);  
        }  
		
		@Override
		public void onPostExecute(Object res) {
			setProgressBarIndeterminateVisibility(false);  
		}
	}
	
	/**
	 * 
	 * MEDIA ROUTER RELATED METHODS
	 * 
	 */
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
		            
		            if(info.getPlaybackType() == MediaRouter.RouteInfo.PLAYBACK_TYPE_LOCAL){
		            }
		            else{
		            	//if remote and if the prez is not shown 
		            	//it means the prez should be loaded
		            	if(!mShowingPrez){
		            		startImagePlayerPresentation(); 
		            		mShowingPrez = true;
		            	}
		            	else{
		            		mShowingPrez = false;
		            	}
		            }
		        }
	};
	
	private void startImagePlayerPresentation(){
		Log.d(TAG, "startImagePlayerPresentation");
		
		// Get the current route and its presentation display.
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
  
        if (route != null) {
        	Display presentationDisplay = route.getPresentationDisplay();
        	if (presentationDisplay != null) {
        		//create the prez
        		mImgPrez = new ImagePlayerPrez(this, presentationDisplay);
        		
        		//show it
        		mImgPrez.show();
        	}
        }
	}
	
	private void updatePrezImg(String imagePath) {
		Log.d(TAG, "updatePrezImg");
		if(mImgPrez != null){
			mImgPrez.setImage(imagePath);
		}
	}
}
