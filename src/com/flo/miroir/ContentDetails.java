package com.flo.miroir;

import android.graphics.Bitmap;

public class ContentDetails {
	
	private String mFilePath = null;
	private String mMimeType = null;
	private String mTitle = null;
	private String mArtist = null;
	private String mDurationMediaStore = null;
	private String mResolution = null;
	private String mSize = null;
	private Bitmap mThumbnail = null;
	
	public ContentDetails(String filePath){
		mFilePath = filePath;
		
		mMimeType = "type/unknown";
		mTitle = "Unknown";
		mArtist = "Unknown";
		mDurationMediaStore = "0";
		mResolution = "0";
		mSize = "0";
	}
	
	public String getFilePath(){
		return mFilePath;
	}
	
	public String getMimeType(){
		return mMimeType;
	}
	
	public String getTitle(){
		return mTitle;
	}
	
	public String getArtist(){
		return mArtist;
	}
	
	public String getDurationMediaStore(){
		return mDurationMediaStore;
	}
	
	public String getResolution(){
		return mResolution;
	}
	
	public String getSize(){
		return mSize;
	}
	
	public Bitmap getThumbnail(){
		return mThumbnail;
	}
	
	public void setMimeType(String value){
		mMimeType = value;
	}
	
	public void setTitle(String value){
		mTitle = value;
	}
	
	public void setArtist(String value){
		mArtist = value;
	}
	
	public void setDurationMediaStore(String value){
		mDurationMediaStore = value;
	}
	
	public void setResolution(String value){
		mResolution = value;
	}
	
	public void setSize(String value){
		mSize = value;
	}
	
	public void setThumbnail(Bitmap img){
		mThumbnail = img;
	}
}
