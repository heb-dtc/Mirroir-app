package com.flo.miroir;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Utils {
	//public static String LocalRouteName = "Phone";
	
	public static String imagePresentationName = "ImagePresentation";
	public static String videoPresentationName = "VideoPresentation";
	public static String musicPresentationName = "MusicPresentation";
	public static String standbyPresentationName = "StandByPresentation";
	
	public static Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {
		   
		Bitmap bm = null;
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
	       
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	       
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		bm = BitmapFactory.decodeFile(path, options); 
	       
		return bm;   
	}
		  
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
	   	int inSampleSize = 1;
		   
	   	if (height > reqHeight || width > reqWidth) {
	   		if (width > height) {
	   			inSampleSize = Math.round((float)height / (float)reqHeight);    
	   		} else {
	   			inSampleSize = Math.round((float)width / (float)reqWidth);    
	   		}   
	   }
		   
	   return inSampleSize;    
	}
}
