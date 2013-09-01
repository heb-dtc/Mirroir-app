package com.flo.miroir;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

public class StandByPrez extends RemotePresentation {

	private final String TAG = this.getClass().getName();

	private Context mCtx;
	
	public StandByPrez(Context outerContext, Display display) {
		super(outerContext, display);
		mCtx = outerContext;
		
		setName(Utils.standbyPresentationName);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "Create StandBy prez");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.stand_by_prez);
	}
}
