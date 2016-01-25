package com.inductivebiblestudyapp.data.loaders;

import retrofit.RetrofitError;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

/**
 * Loader to fetch/parse contact api using {@link AsyncTaskLoader}. 
 * @author Jason Jenkins
 * @version 0.2.0-20150618
 */
public abstract class AbstractFetchAsyncLoader<Result> extends AsyncTaskLoader<Result> {
	/** Class name for debugging purposes. */
	final static private String LOGTAG = AbstractFetchAsyncLoader.class
			.getSimpleName();
	
	private static final boolean DEBUG = false; 
	
	protected static int RETRY_TIME = 400; //milliseconds
	
	protected static int RETRY_MAX = 5; //5 * 400 = 2 seconds
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	public AbstractFetchAsyncLoader(Context context) {
		super(context);
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		forceLoad(); //required for bug
	}
	
	
	@Override
	public Result loadInBackground() {
		if (DEBUG) {
			Log.d(LOGTAG, "loadInBackground");
			try { Thread.sleep(5000);
			} catch (InterruptedException e1) {}
		}
		return attemptFetch(0);		
	}
	
	/** Performs the fetch, safely within a try block. */
	abstract protected Result fetchResult();

	protected Result attemptFetch(int attemptCount) {
		try {
			attemptCount++;
			return fetchResult();
			
		} catch (RetrofitError e) {
			if (attemptCount < RETRY_MAX) {
				try { Thread.sleep(RETRY_TIME);} 
				catch (InterruptedException e1) {}
				return attemptFetch(attemptCount);
			} else {
				Log.e(LOGTAG, "Cannot retrieve contents: " + e);
				return null;
			}
		}
	}
	

}
