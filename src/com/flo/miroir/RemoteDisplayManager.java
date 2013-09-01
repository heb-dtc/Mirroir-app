package com.flo.miroir;

import java.util.ArrayList;

import android.app.Presentation;
import android.content.Context;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Display;

public class RemoteDisplayManager {

	private final String TAG = this.getClass().getName();
	
	private static RemoteDisplayManager mInstance;
	
	private Context mContext;
	private MediaRouter mMediaRouter;
	private RemotePresentation mCurenntPresentation;
	
	private ArrayList<IRemoteDisplayCallbacks> mSubscriberList;
	
	public RemoteDisplayManager(){
		mSubscriberList = new ArrayList<IRemoteDisplayCallbacks>();
	}
	
	public void initializeRemoteDisplayManager(Context c){
		mMediaRouter = (MediaRouter) c.getSystemService(Context.MEDIA_ROUTER_SERVICE);
		mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
	}
	
	public static RemoteDisplayManager getInstance(){
		if(mInstance == null){
			mInstance = new RemoteDisplayManager();
		}
		return mInstance;
	}
	
	public void subscribeToCallabcks(IRemoteDisplayCallbacks listener){
		mSubscriberList.add(listener);
	}
	
	public void unsubscribeToCallbacks(IRemoteDisplayCallbacks listener){
		mSubscriberList.remove(listener);
	}
	
	public boolean displayStandByPresentation(Context c){
		boolean success = false;
		
		if(mMediaRouter != null){
			// Get the current route and its presentation display.
	        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
	        
	        if (route != null) {
	        	Display presentationDisplay = route.getPresentationDisplay();
	        	if (presentationDisplay != null) {
	        		StandByPrez prez = new StandByPrez(c, presentationDisplay);
	        		
	        		if(mCurenntPresentation != null){
	        			mCurenntPresentation.cancel();
	        		}
	        		mCurenntPresentation = prez;
	        		mCurenntPresentation.show();
	        		mContext = c;
	        		success = true;
	        	}
	        }
		}
		
		return success;
	}
	
	public void hideStandByPresentation(){
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.standbyPresentationName)){
				mCurenntPresentation.cancel();
			}
		}
	}
	
	public boolean displayAudioPresentation(Context c){
		boolean success = false;
		
		if(mMediaRouter != null){
			// Get the current route and its presentation display.
	        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
	        
	        if (route != null) {
	        	Display presentationDisplay = route.getPresentationDisplay();
	        	if (presentationDisplay != null) {
	        		AudioPlayerPrez prez = new AudioPlayerPrez(c, presentationDisplay);
	        		
	        		if(mCurenntPresentation != null){
	        			mCurenntPresentation.cancel();
	        		}
	        		mCurenntPresentation = prez;
	        		mCurenntPresentation.show();
	        		mContext = c;
	        		success = true;
	        	}
	        }
		}
		
		return success;
	}
	
	public boolean displayImagePresentation(Context c){
		boolean success = false;
		
		if(mMediaRouter != null){
			// Get the current route and its presentation display.
	        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
	        
	        if (route != null) {
	        	Display presentationDisplay = route.getPresentationDisplay();
	        	if (presentationDisplay != null) {
	        		ImagePlayerPrez prez = new ImagePlayerPrez(c, presentationDisplay);
	        		
	        		if(mCurenntPresentation != null){
	        			mCurenntPresentation.cancel();
	        		}
	        		mCurenntPresentation = prez;
	        		mCurenntPresentation.show();
	        		mContext = c;
	        		success = true;
	        	}
	        }
		}
		
		return success;
	}
	
	public void hideImagePresentation(){
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.imagePresentationName)){
				mCurenntPresentation.cancel();
			}
		}
	}

	public boolean displayVideoPresentation(Context c){
		boolean success = false;
		
		if(mMediaRouter != null){
			// Get the current route and its presentation display.
	        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
	        
	        if (route != null) {
	        	Display presentationDisplay = route.getPresentationDisplay();
	        	if (presentationDisplay != null) {
	        		VideoPlayerPrez prez = new VideoPlayerPrez(c, presentationDisplay);
	        		
	        		if(mCurenntPresentation != null){
	        			mCurenntPresentation.cancel();
	        		}
	        		mCurenntPresentation = prez;
	        		mCurenntPresentation.show();
	        		mContext = c;
	        		success = true;
	        	}
	        }
		}
		
		return success;
	}
	
	public void hideVideoPlayerPresentation(){
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.videoPresentationName)){
				mCurenntPresentation.cancel();
			}
		}
	}
	
	public void shutDown(){
		if(mMediaRouter != null){
			mMediaRouter.removeCallback(mMediaRouterCallback);
		}
	}
	
	public RouteInfo[] getAllRoutes(){
		if(mMediaRouter != null){
			int nrRoutes = mMediaRouter.getRouteCount();
	        RouteInfo[] routes = new RouteInfo[nrRoutes];
	    	
	        for(int i=0 ; i < nrRoutes ; i++){
	    		routes[i] = mMediaRouter.getRouteAt(i);
	    	}
	        
	        return routes;
        }
		else{
			return null;
		}
	}
	
	public void selectRoute(RouteInfo info){
		mMediaRouter.selectRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, info);
	}
	
	public void updateImagePlayerPresentation(String imagePath){
		Log.d(TAG, "updateImagePlayerPresentation");
		
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.imagePresentationName)){
				ImagePlayerPrez prez = (ImagePlayerPrez)mCurenntPresentation;
				prez.setImage(imagePath);
			}
		}
	}
	
	public void updateVideoPlayerPresentation(ContentDetails item){
		Log.d(TAG, "updateImagePlayerPresentation");
		
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.videoPresentationName)){
				VideoPlayerPrez prez = (VideoPlayerPrez)mCurenntPresentation;
				prez.startVideoPlayer(item);
			}
		}
	}
	
	public void updateAudioPlayerPresentation(ContentDetails item){
		Log.d(TAG, "updateImagePlayerPresentation");
		
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.musicPresentationName)){
				AudioPlayerPrez prez = (AudioPlayerPrez)mCurenntPresentation;
				prez.startAudioPlayer();
			}
		}
	}
	
	public void pauseVideoPlayer(){
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.videoPresentationName)){
				VideoPlayerPrez prez = (VideoPlayerPrez)mCurenntPresentation;
				prez.pauseVideoPlayer();
			}
		}
	}
	
	public void stopVideoPlayer(){
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.videoPresentationName)){
				VideoPlayerPrez prez = (VideoPlayerPrez)mCurenntPresentation;
				prez.stopVideoPlayer();
			}
		}
	}
	
	/**
	 * CALLBACKS
	 */
	
    private final MediaRouter.SimpleCallback mMediaRouterCallback =
	    new MediaRouter.SimpleCallback() {
	        @Override
	        public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
	            Log.d(TAG, "onRouteSelected: type=" + type + ", info=" + info);
	            
	            for(IRemoteDisplayCallbacks cb : mSubscriberList){
	            	cb.onRouteSelected(router, type, info);
	            }
	        }
	
	        @Override
	        public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
	            Log.d(TAG, "onRouteUnselected: type=" + type + ", info=" + info);
	            
	            for(IRemoteDisplayCallbacks cb : mSubscriberList){
	            	cb.onRouteUnselected(router, type, info);
	            }
	        }
	
	        @Override
	        public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
	            Log.d(TAG, "onRoutePresentationDisplayChanged: info=" + info);
	            
	            for(IRemoteDisplayCallbacks cb : mSubscriberList){
	            	cb.onRoutePresentationDisplayChanged(router, info);
	            }
	        }
    	};
}
