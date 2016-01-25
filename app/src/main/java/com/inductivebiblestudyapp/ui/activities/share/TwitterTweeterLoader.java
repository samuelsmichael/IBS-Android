package com.inductivebiblestudyapp.ui.activities.share;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

/**
 * Loader to share via twitter using {@link AsyncTaskLoader}. 
 * @author Jason Jenkins
 * @version 0.1.0-20150805
 */
class TwitterTweeterLoader extends AsyncTaskLoader<Boolean> {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = TwitterTweeterLoader.class
			.getSimpleName();
	
	protected static int RETRY_TIME = 300; //milliseconds
	
	protected static int RETRY_MAX = 2; // .6 seconds
	
	private final Twitter mTwitter;
	private final StatusUpdate mShareStatus;
	
	public TwitterTweeterLoader(Context context, Twitter twitter, StatusUpdate status) {
		super(context);
		mTwitter = twitter;	
		mShareStatus = status;
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		forceLoad(); //required for bug
	}
	
	@Override
	public Boolean loadInBackground() {
		return updateStatus(0);
	}
	
	private Boolean updateStatus(int attemptCount) {
		try {
			attemptCount++;
			mTwitter.updateStatus(mShareStatus);
			return true;
		} catch (TwitterException e) {
			Log.w(CLASS_NAME, "Share failed", e);
		} catch (IllegalStateException e) {
			Log.w(CLASS_NAME, "Share failed (bad state)", e);
		}
		return false;
	}
}
