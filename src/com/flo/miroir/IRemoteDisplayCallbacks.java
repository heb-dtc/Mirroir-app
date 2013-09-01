package com.flo.miroir;

import android.media.MediaRouter;

public interface IRemoteDisplayCallbacks {
	void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info);
	void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info);
	void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info);
}
