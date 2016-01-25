package com.inductivebiblestudyapp.data.loaders;


import java.util.List;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter;
import com.inductivebiblestudyapp.data.service.BibleFetchService;

/**
 * Loader to not only fetch/cache data but to then update process the data.
 *  Does NOT give results in {@link LoaderCallbacks#onLoadFinished(android.content.Loader, Object)},
 *  use {@link OnResponseListener} instead.
 * @author Jason Jenkins
 * @version 0.3.1-20150818
 */
public class BibleChapterViewLoader extends AbstractFetchAsyncLoader<List<SpannableString>> {
	/** Class name for debugging purposes. */
	final static private String LOGTAG = BibleChapterViewLoader.class
			.getSimpleName();

	private final String mChapterId;
	
	private final String mAccessToken;
	
	private final BibleFetchService mBibleFetchService;
	
	private final OnResponseListener mListener;
	
	/**
	 * Attempts to fetch the response from cache, if not found fetches it, then threads the 
	 * response via the listener. Does NOT give results in {@link LoaderCallbacks#onLoadFinished(android.content.Loader, Object)}
	 * @param context
	 * @param chapterId
	 * @param listener
	 */
	public BibleChapterViewLoader(Context context, String chapterId, OnResponseListener listener) {
		super(context);
		this.mChapterId = chapterId;
		this.mAccessToken = new CurrentUser(context).getIBSAccessToken();
		
		RestClient restClient = RestClient.getInstance(); 
		this.mBibleFetchService = restClient.getBibleFetchService(); 
		
		this.mListener = listener;
	}

	@Override
	protected List<SpannableString> fetchResult() {
		Chapter chapter = AppCache.getChapterData(mChapterId);
		if (chapter == null) {
			chapter = mBibleFetchService.getChapter(mAccessToken, mChapterId);
			if (chapter != null) {
				Log.d(LOGTAG, "Success fetched chapter data!");
				AppCache.addChapterData(mChapterId, chapter);
			}
		}
		mListener.onChapterData(chapter);
		
		BibleVerseResponse response = AppCache.getBibleVerseResponse(mChapterId);
		if (response == null) {
			response = mBibleFetchService.getVerseList(mAccessToken, mChapterId);
			if (response != null) {
				Log.d(LOGTAG, "Success fetched chapter verses!");
				AppCache.addBibleVerseResponse(mChapterId, response);
			}
		}		
		
		return mListener.onBibleResponse(response);
	}
	
	/** @version 0.2.0-20150722 */
	public static interface OnResponseListener {
		/** Used to process response in another thread. 
		 * Perform ALL UI requests inside a {@link View#post(Runnable)} block.
		 * @param response The response to handle, <code>null</code> if failed.
		 * @return The list of spannable strings to set/append.
		 */
		public List<SpannableString> onBibleResponse(@Nullable BibleVerseResponse response);
		
		/** Used to supply the chapter data such as previous & next ids */
		public void onChapterData(@Nullable Chapter chapter);
	}

}
