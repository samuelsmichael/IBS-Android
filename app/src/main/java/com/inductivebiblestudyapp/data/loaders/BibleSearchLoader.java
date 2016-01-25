package com.inductivebiblestudyapp.data.loaders;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.bible.BibleSearchResponse;
import com.inductivebiblestudyapp.data.service.IBSSearchService;

/**
 * The loader responsible for bible searches.
 * @author Jason Jenkins
 * @version 0.2.2-20150809
 */
public class BibleSearchLoader extends AbstractFetchAsyncLoader<BibleSearchResponse> {
	final static private String CLASS_NAME = BibleSearchLoader.class
			.getSimpleName();
	
	/** String. Required. */
	public static final String KEY_SEARCH_QUERY = CLASS_NAME + ".KEY_SEARCH_QUERY";
	
	/** String. Optional. */
	public static final String KEY_BOOK_ID = CLASS_NAME + ".KEY_BOOK_ID";
	/** String. Optional. */
	public static final String KEY_CHAPTER_ID = CLASS_NAME + ".KEY_CHAPTER_ID";
	/** String. Optional. Requires {@value #KEY_VERSE_END_ID}.	  */
	public static final String KEY_VERSE_START_ID = CLASS_NAME + ".KEY_VERSE_START_ID";
	/** String. Optional. Requires {@link #KEY_VERSE_START_ID}. */
	public static final String KEY_VERSE_END_ID = CLASS_NAME + ".KEY_VERSE_END_ID";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private final String mQuery;
	private final String mAccessToken;	
	private IBSSearchService mService;
	
	private final Bundle mArgs;
	
	private final OnWebRequestListener mWebListener;
	
	public BibleSearchLoader(Context context, Bundle args, OnWebRequestListener listener) {
		super(context);
		testArgs(args);
		
		mQuery = args.getString(KEY_SEARCH_QUERY);
		mAccessToken = new CurrentUser(getContext()).getIBSAccessToken();
		mService = RestClient.getInstance().getIBSSearchService();
		mArgs = args;
		mWebListener = listener;
	}

	/** Tests if the supplied arguments are valid & fixes any conflicts. */
	private static void testArgs(Bundle args) {
		if (!args.containsKey(KEY_SEARCH_QUERY)) {
			throw new IllegalArgumentException("Must have a query to be a valid search");
		}
		
		final String msgStub = "Search warning: ";
		if (args.containsKey(KEY_VERSE_START_ID) != args.containsKey(KEY_VERSE_END_ID)) {
			Log.w(CLASS_NAME, msgStub + "Verse range is incomplete");
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End initialization
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected BibleSearchResponse fetchResult() {
		/*
		 * Remember: 
		 * - If the verses are specified, there is no need for a chapter_id. And it will fail.
		 * - If the chapters are specified, there is no need for a book_id. And it will fail.
		 * 
		 * Only specify the most precise items, if found. If not found, try the broader terms.
		 */

		//where null is ignored by retrofit
		String bookId = null;
		String chapterId = null;
		String verseStartId = null;
		String verseEndId = null;
		
		//select possible arguments
		if (mArgs.containsKey(KEY_VERSE_START_ID) && mArgs.containsKey(KEY_VERSE_END_ID)) { 
			verseStartId = mArgs.getString(KEY_VERSE_START_ID, null);
			//only end verse if start set
			verseEndId = mArgs.getString(KEY_VERSE_END_ID, null);
			
		} else if (mArgs.containsKey(KEY_CHAPTER_ID)) {
			chapterId = mArgs.getString(KEY_CHAPTER_ID, null);
			
		} else {
			bookId = mArgs.getString(KEY_BOOK_ID, null); 
		}
		
		final String key = mQuery + ";" + bookId + ";" + chapterId + ";" + verseStartId + ";" + verseEndId + ";";
		BibleSearchResponse result = AppCache.getBibleSearchResponse(key);
		if (result == null) {
			mWebListener.onWebRequest();
			result = mService.searchBible(mAccessToken, mQuery, bookId, chapterId, verseStartId, verseEndId);
			AppCache.addBibleSearchResponse(key, result);
		}
		return result;
	}
	
	/** Simple listener whether the loader has to query the web or fetches from cache. */
	public static interface OnWebRequestListener {
		/** Called whenever a webrequest is made, may be made multiple times. */
		public void onWebRequest();
	}

}
