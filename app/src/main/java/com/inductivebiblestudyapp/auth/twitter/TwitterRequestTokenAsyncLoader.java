package com.inductivebiblestudyapp.auth.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.inductivebiblestudyapp.R;

/**
 * Loader to fetch/parse contact api using {@link AsyncTaskLoader}. 
 * @author Jason Jenkins
 * @version 0.2.0-20150615
 */
public class TwitterRequestTokenAsyncLoader extends AsyncTaskLoader<RequestToken> {

	protected static int RETRY_TIME = 400; //milliseconds
	
	protected static int RETRY_MAX = 5; //5 * 400 = 2 seconds
	
	private final Twitter mTwitter;
	private final String mCallback;
	public TwitterRequestTokenAsyncLoader(Context context, Twitter twitter) {
		super(context);
		mTwitter = twitter;	
		mCallback = context.getString(R.string.twitter_callback);
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		forceLoad(); //required for bug
	}
	
	@Override
	public RequestToken loadInBackground() {
		return requestToken(0);
	}
	
	private RequestToken requestToken(int attemptCount) {
		try {
			attemptCount++;
			return mTwitter.getOAuthRequestToken(mCallback);
			
		} catch (TwitterException e) {
			if (attemptCount < RETRY_MAX) {
				try { Thread.sleep(RETRY_TIME);} 
				catch (InterruptedException e1) {}
				
				return requestToken(attemptCount);
			} else {
				Log.e(TwitterRequestTokenAsyncLoader.class.getSimpleName(), "Cannot fetch token:" + e);
				return null;
			}
		}
	}
}
