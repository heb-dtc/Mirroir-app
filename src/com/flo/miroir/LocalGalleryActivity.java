package com.flo.miroir;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
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
	
	private GridView mImgGrid;
	private ImageAdapter mImgAdpt;
	
	private Cursor mCursor;
	private int columnIndex; //idx of the ID column

	private LoadImgAsyncTask mLoadImgtask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_local_gallery);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle("Images Gallery");

		mImgGrid = (GridView)findViewById(R.id.gridview);
		
		mImgAdpt = new ImageAdapter(this);
		mImgGrid.setAdapter(mImgAdpt);
		mImgGrid.setOnItemClickListener(mImageItemClickListener);
		
		mLoadImgtask = new LoadImgAsyncTask();
		mLoadImgtask.execute();
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        RemoteDisplayManager.INSTANCE.displayImagePresentation(this);
    }
	
    @Override
    protected void onPause() {
    	super.onPause();
    	RemoteDisplayManager.INSTANCE.hideImagePresentation();
    }
    
	protected void onDestroy() {  
        super.onDestroy();  
        final GridView grid = mImgGrid;  
        final int count = grid.getChildCount();  
        ImageView v = null;  
        
        //cancel the potential task
        mLoadImgtask.cancel(true);
        
        for (int i = 0; i < count; i++) {  
            v = (ImageView) grid.getChildAt(i);  
            ((BitmapDrawable) v.getDrawable()).setCallback(null);  
        }
    }  
	
	OnItemClickListener mImageItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
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
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
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
	    	
	    	imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);  
	    	imageView.setLayoutParams(new GridView.LayoutParams(240, 240));
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
	        String[] projection = {
	        		MediaStore.Images.Media._ID,
	        		MediaStore.Images.Media.DATA};
	        
			//Do the query
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
			
			String imgURI;
			
	        for (int i = 0; i < mCursor.getCount(); i++) { 
	        	//check and cleanup
	        	if(isCancelled()){
	        		mCursor.close();
	        		return null;
	        	}
	        	
	        	mCursor.moveToPosition(i);  
	        	
	            imageID = mCursor.getInt(columnIndex);  
	            imgURI = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
	            		
	            try {  
	            	//bitmap = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), imageID, MediaStore.Images.Thumbnails.MINI_KIND, null);
	                /*if (bitmap != null) {  
	                    newBitmap = Bitmap.createScaledBitmap(bitmap, 240, 240, true);

	                    bitmap.recycle();  
	                    if (newBitmap != null) {  
	                        publishProgress(new LoadedImage(newBitmap));  
	                    }  
	                }  */
	            	
	            	if (imgURI != null) {
	            		newBitmap = Utils.decodeSampledBitmapFromUri(imgURI, 240, 240);
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
		
		@Override
		protected void onCancelled(Object result) {
			setProgressBarIndeterminateVisibility(false);
		}
	}
	
	private void updatePrezImg(String imagePath) {
		RemoteDisplayManager.INSTANCE.updateImagePlayerPresentation(imagePath);
	}
}
