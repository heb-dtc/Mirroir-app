package com.flo.miroir;

import android.app.Presentation;
import android.content.Context;
import android.view.Display;

public class RemotePresentation extends Presentation{

	private String mRemotePresentationName;
	private IPrezCallbacks mListener;
	
	public RemotePresentation(Context outerContext, Display display) {
		super(outerContext, display);
	}

	public void setName(String name){
		mRemotePresentationName = name;
	}
	
	public String getName(){
		return mRemotePresentationName;
	}
	
	public void setListener(IPrezCallbacks cb){
		mListener = cb;
	}
	
	protected IPrezCallbacks getListener(){
		return mListener;
	}
}
