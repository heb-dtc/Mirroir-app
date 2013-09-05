package com.flo.miroir;

import java.util.ArrayList;

import android.content.Context;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Display;

public enum RemoteDisplayManager {
	INSTANCE;

	private final String TAG = this.getClass().getName();
	
	//private Context mContext;
	private MediaRouter mMediaRouter;
	private RemotePresentation mCurenntPresentation;
	private RouteInfo mCurrentRouteSelected;
	private RouteInfo mLocalRoute;
	
	private ArrayList<IRemoteDisplayCallbacks> mSubscriberList;
	
	 /*RemoteDisplayManager(){
		mSubscriberList = new ArrayList<IRemoteDisplayCallbacks>();
	}*/
	
	public void initializeRemoteDisplayManager(Context c){
		mSubscriberList = new ArrayList<IRemoteDisplayCallbacks>();
		
		//get Media Router hooks
		mMediaRouter = (MediaRouter) c.getSystemService(Context.MEDIA_ROUTER_SERVICE);
		mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
		
		//some stuff to init
		findLocalRoute();
		mCurrentRouteSelected = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
	}
	
	/*public static RemoteDisplayManager getInstance(){
		if(mInstance == null){
			mInstance = new RemoteDisplayManager();
		}
		return mInstance;
	}*/
	
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
	        		//mContext = c;
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
	        		//mContext = c;
	        		success = true;
	        	}
	        }
		}
		
		return success;
	}
	
	public void hideAudioPlayerPresentation(){
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.musicPresentationName)){
				mCurenntPresentation.cancel();
			}
		}
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
	        		//mContext = c;
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
	        		//mContext = c;
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
			if(mLocalRoute != mCurrentRouteSelected)
				releaseConnection();
			
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
	
	public boolean isConnectedToRemoteDisplay(){
		return (mCurrentRouteSelected.getPlaybackType() == MediaRouter.RouteInfo.PLAYBACK_TYPE_REMOTE) ? true : false;
	}
	
	public boolean isCurrentRoute(RouteInfo info){
		if(mCurrentRouteSelected != null && mMediaRouter != null){
			if(info == mCurrentRouteSelected)
				return true;
		}
		
		return false;
	}
	
	public boolean isLocalRoute(RouteInfo info){
		if(mLocalRoute != null && mMediaRouter != null){
			if(info == mLocalRoute)
				return true;
		}
		
		return false;
	}
	
	public void findLocalRoute(){
		Log.d(TAG, "");
		int nrRoutes = mMediaRouter.getRouteCount();
    	
        for(int i=0 ; i < nrRoutes ; i++){
        	//assuming there is only one...
        	//true for Nexus, TBT on more devices... 
    		if(mMediaRouter.getRouteAt(i).getPlaybackType() == MediaRouter.RouteInfo.PLAYBACK_TYPE_LOCAL){
    			mLocalRoute = mMediaRouter.getRouteAt(i);
    			Log.d(TAG, "");
    		}
    	}
	}
	
	public String getLocalRouteName(){
		if(mLocalRoute != null){
			return mLocalRoute.getName().toString();
		}
		
		return null;
	}
	
	public boolean selectRoute(RouteInfo info){
		boolean success = false;
		if(mMediaRouter != null){
			mMediaRouter.selectRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, info);
			mCurrentRouteSelected = info;
			success = true;
		}
		
		return success;
	}
	
	public boolean releaseConnection(){
		boolean success = false;
		if(mLocalRoute != null && mMediaRouter != null){
			mMediaRouter.selectRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mLocalRoute);
			success = true;
		}
		
		return success;
	}
	
	/**
	 * 
	 * PRESENTATION CURRENT CONTENT UPDATE HOOKS
	 * 
	 * @param imagePath
	 */
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
		Log.d(TAG, "updateVideoPlayerPresentation");
		
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.videoPresentationName)){
				VideoPlayerPrez prez = (VideoPlayerPrez)mCurenntPresentation;
				prez.startVideoPlayer(item);
			}
		}
	}
	
	public void updateAudioPlayerPresentation(ContentDetails item){
		Log.d(TAG, "updateAudioPlayerPresentation");
		
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.musicPresentationName)){
				AudioPlayerPrez prez = (AudioPlayerPrez)mCurenntPresentation;
				prez.startAudioPlayer(item);
			}
		}
	}
	
	/**
	 * 
	 * MUSIC PLAYER PRESENTATION CONTROLS HOOKS
	 * 
	 */
	
	public void resumeAudioPlayer(){
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.musicPresentationName)){
				AudioPlayerPrez prez = (AudioPlayerPrez)mCurenntPresentation;
				prez.resumeAudioPlayer();
			}
		}
	}
	
	public void pauseAudioPlayer(){
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.musicPresentationName)){
				AudioPlayerPrez prez = (AudioPlayerPrez)mCurenntPresentation;
				prez.resumeAudioPlayer();
			}
		}
	}
	
	public void stopAudioPlayer(){
		if(mCurenntPresentation != null){
			if(mCurenntPresentation.getName().equals(Utils.musicPresentationName)){
				AudioPlayerPrez prez = (AudioPlayerPrez)mCurenntPresentation;
				prez.stopAudioPlayer();
			}
		}
	}
	
	/**
	 * 
	 * VIDEO PLAYER PRESENTATION CONTROLS HOOKS
	 * 
	 */
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
