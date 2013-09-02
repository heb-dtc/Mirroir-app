package com.flo.miroir;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;

public class ImagePlayerPrez extends RemotePresentation {
	
	private final String TAG = this.getClass().getName();

	private ImageView mImgView;
	private Display mDisplay;
	private DisplayMetrics mDispMetrics = new DisplayMetrics();
	
	
	public ImagePlayerPrez(Context outerContext, Display display) {
		super(outerContext, display);
		mDisplay = display;
		mDisplay.getMetrics(mDispMetrics);
		
		setName(Utils.imagePresentationName);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "Create image prez");
		
		super.onCreate(savedInstanceState);

        setContentView(R.layout.prez_image_player);
        mImgView = (ImageView)findViewById(R.id.image_view);
	}
	
	@Override
	public void onDisplayRemoved() {
		super.onDisplayRemoved();
		Log.e(TAG, "onDisplayRemoved");
		
		//mandatory clean up before exiting
		//...
	}
	
	@Override
	public void onDisplayChanged() {
		super.onDisplayChanged();
		Log.e(TAG, "onDisplayRemoved");
	}
	
	/*
	 * 
	 * OUTER CONTROL INTERFACE
	 * 
	 */
	
	public void setImage(final String imgPath){
		Log.v(TAG, "setNewImage");
		
		FileInputStream is = null;  
        BufferedInputStream bis = null;  
        try {  
            is = new FileInputStream(new File(imgPath));  
            bis = new BufferedInputStream(is);  
            Bitmap bitmap = BitmapFactory.decodeStream(bis);  
            //Bitmap scaledBmp = Bitmap.createScaledBitmap(bitmap, mDispMetrics.widthPixels, mDispMetrics.heightPixels, true);  
            //bitmap.recycle();
            //if(scaledBmp != null){
            //	mImgView.setImageBitmap(scaledBmp);
            //}
            mImgView.setImageBitmap(bitmap);
        }   
        catch (Exception e) {  
            //Try to recover  
        }
        finally {  
            try {  
                if (bis != null) {  
                    bis.close();  
                }  
                if (is != null) {  
                    is.close();  
                }  
            } catch (Exception e) {  
            }  
        }
	}
}
