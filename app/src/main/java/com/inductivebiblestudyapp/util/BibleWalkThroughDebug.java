package com.inductivebiblestudyapp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Context;
import android.util.Log;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse.Book;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse.Verse;

/** Debugging class for walking through the entire bible.
 * Useful in debugging and not much else, it is not very safe but that's because we're debugging. 
 * 
 * @author Jason Jenkins 
 * @version 0.1.1-20150818 */
public class BibleWalkThroughDebug {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = BibleWalkThroughDebug.class
			.getSimpleName();
	
	/** Delay between requests. */
	private static final long DELAY = 50; //ms 
	
	private static final boolean SHOW_LOGS = true;
	/** Allows problem chapters, such as those with markings applied, to be skipped. */
	private static final boolean SKIP_PROBLEM_CHAPTERS = true;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private final String mAccessToken;
	
	private boolean mRun = true;
	
	private final OnWalkThroughListener mWalkThroughListener;
	
	private final List<String> mFailedToLoad = new ArrayList<String>();
	
	public BibleWalkThroughDebug(Context context, OnWalkThroughListener listener) {
		CurrentUser user = new CurrentUser(context);
		mAccessToken = user.getIBSAccessToken();
		mWalkThroughListener = listener;
		debug(user.getTranslationName(), "[[DEBUGGING]]");
	}
	
	public void start() {
		mRun = true;
		loadBibleBooks();
		debug("starting", "[[start]]");
	}
	
	public void stop() {
		mRun = false;
		debug("stopping", "[[stop]]");
	}
	
	public void stopAndThrow() {
		stop();
		throw new IllegalStateException("Not a bad state, just killing the app for easier debugging");
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Loading methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** loads the bible books and sets them into the spinner. */
	private void loadBibleBooks() {
		final String TAG = "loadBibleBooks";
		
		
		BibleResponse response = AppCache.getBibleResponse();
		
		if (response == null) {
			RestClient	.getInstance()
						.getBibleFetchService()
						.getBookList(mAccessToken, 
					new Callback<BibleResponse>() {
				
				@Override
				public void success(BibleResponse response, Response arg1) {
					if (!mRun) return;
					debug("Success book!", TAG);

					
					if (response != null) {
						AppCache.setBibleResponse(response);
						loadEachBook(response);							
					} else {
						debug("Book response null", TAG);	
					}
				}				
				
				@Override
				public void failure(RetrofitError arg0) {
					debug("Failed to get books", TAG);
				}
			});
		} else {
			loadEachBook(response);	
		}
	}
	
	
	/** loads the book chapters and sets them into the spinner. 
	 * @param bookId The book id to load, if <code>null</code> simply hides the view. */
	private void loadBibleChapters(final String bookId) {
		final String TAG = "loadBibleChapters";
		
		delay();
		if (!mRun) return;
		BibleChapterResponse response = AppCache.getBibleChapterResponse(bookId);
		
		if (response == null) {
			RestClient	.getInstance()
						.getBibleFetchService()
						.getChapterList(mAccessToken, bookId,
					new Callback<BibleChapterResponse>() {
				
				@Override
				public void success(BibleChapterResponse response, Response arg1) {
					if (!mRun) return;
					debug("Success chapters!", TAG);
					
					if (response != null) {
						AppCache.addBibleChapterResponse(bookId, response);;
						loadEachChapter(response);	
					} else {
						debug("Chapter listing response null", TAG);						
					}
				}				
				
				@Override
				public void failure(RetrofitError arg0) {
					mFailedToLoad.add(bookId);
					debug("Failed to get chapters for " + bookId, TAG);
				}
			});
		} else {
			loadEachChapter(response);
		}
	}
	
	
	/** loads the book chapters and sets them into the spinner.
	 * @param bookId The book id to load, if <code>null</code> simply hides the view.  */
	private void loadBibleVerses(final String chapterId) {
		final String TAG = "loadBibleVerses";
		
		delay();
		if (!mRun) return;
		BibleVerseResponse response = AppCache.getBibleVerseResponse(chapterId);
		
		if (response == null) {
			RestClient	.getInstance()
						.getBibleFetchService()
						.getVerseList(mAccessToken, chapterId,
					new Callback<BibleVerseResponse>() {
				
				@Override
				public void success(BibleVerseResponse response, Response arg1) {
					if (!mRun) return;
					debug("Success chapter contents!", TAG);
					
					if (response != null) {
						AppCache.addBibleVerseResponse(chapterId, response);
						processEachVerse(response);
					} else {
						debug("chapter contents response null", TAG);
					}
				}				
				
				@Override
				public void failure(RetrofitError arg0) {
					debug("Failed to get chapter contents for "  + chapterId, TAG);
					mFailedToLoad.add(chapterId);
					if (SKIP_PROBLEM_CHAPTERS) {
						Log.e(CLASS_NAME, "Skipping chapter: " + chapterId);
						loadNextStep(TAG + "[skip to next]");
					}
				}
			});
		} else {
			processEachVerse(response);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Load Chaining methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	List<Book> mBookList = new ArrayList<BibleResponse.Book>();
	List<Chapter> mChapterList = new ArrayList<BibleChapterResponse.Chapter>();
	
	/*package*/ void loadEachBook(BibleResponse response) {
		final String TAG = "loadEachBook";
		mBookList.clear();
		mBookList.addAll(response.getOldTestament());
		mBookList.addAll(response.getNewTestament());
		
		loadNextStep(TAG);
	}
	
	/*package*/ void loadEachChapter(BibleChapterResponse response) {
		final String TAG = "loadEachChapter";
		mChapterList.clear();
		mChapterList.addAll(Arrays.asList(response.getChapters()));
				
		loadNextStep(TAG);
	}
	
	/*package*/ void loadNextStep(String tag) {
		final String TAG = tag + "->" + "loadNextStep";
		
		if (!mChapterList.isEmpty()) { //load chapters, if any
			Chapter chapter = mChapterList.remove(0);
			debug("(Loading Chapter) " + chapter.getSearchResultName(), TAG);
			loadBibleVerses(chapter.getChapterId());
			
		} else if (!mBookList.isEmpty()) { //next, load books, if any
			
			Book book = mBookList.remove(0); //pop first element to load
			debug("(Loading Book) " + book.getSearchResultName(), TAG);
			loadBibleChapters(book.getBookId()); 
		} else {
			Log.e(CLASS_NAME, "Failed to load the following: " + Arrays.toString(mFailedToLoad.toArray()));
		}
	}
	
	/*package*/ void processEachVerse(BibleVerseResponse response) {
		final String TAG = "processEachVerse";
		
		Verse[] verses = response.getVerses();
		
		for (Verse verse : verses) {
			if (!mRun) return;
			debug("Loaded: " + verse.getSearchResultName(), TAG);
			mWalkThroughListener.onVerse(this, verse);
		}
		loadNextStep(TAG);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static void delay() {
		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {}
	}
	
	private static void debug(String message, String tag) {
		if (SHOW_LOGS) {
			Log.w(CLASS_NAME, tag + " :: " + message);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static interface OnWalkThroughListener {
		public void onVerse(BibleWalkThroughDebug debug, Verse verse);
	}
}
