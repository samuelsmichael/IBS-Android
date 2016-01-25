package com.inductivebiblestudyapp.data.googleimages;

import retrofit.RetrofitError;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.inductivebiblestudyapp.DebugConstants;
import com.inductivebiblestudyapp.data.model.googleimages.GoogleImageResponse;
import com.inductivebiblestudyapp.data.service.googleimages.BasicGoogleImageSearchService;
import com.inductivebiblestudyapp.data.service.googleimages.CustomSearchGoogleImageSearchService;

/**
 * Performs the Google Image search request. Initially attempts using
 * 
 * then falls back to using the {@link GoogleApiRestClient}.
 * @author Jason Jenkins
 * @version 0.2.1-20150910
 */
public class GoogleImageSearchLoader extends AsyncTaskLoader<GoogleImageResponse> {
	final static private String LOGTAG = GoogleImageSearchLoader.class
			.getSimpleName();
	/** Increased delay between request to prevent throttling. */
	private static final int RETRY_CLIPART_DELAY = 300; //ms
	private static final int RETRY_DEFAULT_DELAY = 100;
	private static final int RETRY_MAX = 4;
	private static final int RETRY_CLIPART = 2;

	private final int mStart; 
	private final String mSearchTerm;
	
	private final BasicGoogleImageSearchService mBasicSearchService;
	private final CustomSearchGoogleImageSearchService mCustomSearchService;
	
	private boolean mIsUsingBasicRequest = false;
	
	/**
	 * 
	 * @param context
	 * @param searchTerm
	 * @param start The 0-based index to start at
	 */
	public GoogleImageSearchLoader(Context context, String searchTerm, int start) {
		super(context);
		this.mStart = start;
		this.mSearchTerm = searchTerm;
		
		this.mBasicSearchService = GoogleApiRestClient.getBasicImageApi().getGoogleImageSearchService();
		this.mCustomSearchService = GoogleApiRestClient.getCustomSearchApi().getGoogleImageSearchService();
		
		mIsUsingBasicRequest = mIsUsingBasicRequest || DebugConstants.FORCE_SIMPLE_IMAGE_SEARCH;
	}
	
	/** Same as calling {@link #GoogleImageSearchLoader(Context, String, int)} with 0 */
	public GoogleImageSearchLoader(Context context, String searchTerm) {
		this(context, searchTerm, 0);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End initialization
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		forceLoad(); //required for bug
	}
	
	
	@Override
	public GoogleImageResponse loadInBackground() {
		return attemptFetch(0);		
	}

	protected GoogleImageResponse attemptFetch(int attemptCount) {
		try {
			attemptCount++;
			return fetchResult();
			
		} catch (RetrofitError e) {
			int status = e.getResponse().getStatus();
			if (!mIsUsingBasicRequest && attemptCount >= RETRY_CLIPART) {
				mIsUsingBasicRequest = true;
				Log.w(LOGTAG, "Status "+status+": Cannot fetch clip art - Trying basic search instead");
			}
			return sleepAndRetry(attemptCount, e);
		} 
	}

	private GoogleImageResponse sleepAndRetry(int attemptCount, Exception e) {
		if (attemptCount < RETRY_MAX) {
			try { Thread.sleep(mIsUsingBasicRequest ? RETRY_DEFAULT_DELAY : RETRY_CLIPART_DELAY);} 
			catch (InterruptedException  e1) {}
			return attemptFetch(attemptCount);
		} else {
			Log.e(LOGTAG, "Cannot retrieve contents: " + e);
			return null;
		}
	}

	protected GoogleImageResponse fetchResult() {
		GoogleImageResponse result = null;
		if (mIsUsingBasicRequest) {
			result = mBasicSearchService.search(mSearchTerm, String.valueOf(mStart));
		} else {
			result = mCustomSearchService.searchClipArt(mSearchTerm, String.valueOf(mStart+1));
		}
		return result;
	}


}
