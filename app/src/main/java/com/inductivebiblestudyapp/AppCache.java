package com.inductivebiblestudyapp;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.model.ImageListResponse;
import com.inductivebiblestudyapp.data.model.LetteringListResponse;
import com.inductivebiblestudyapp.data.model.StudyNotesResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleSearchResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseDetailsResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.CrossReferenceResponse;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.StrongsResponse;
import com.inductivebiblestudyapp.data.model.googleimages.GoogleImageResponse;
import com.inductivebiblestudyapp.util.DialogStateHolder;
import com.inductivebiblestudyapp.util.PreferenceUtil;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

/**
 * Simple caching system for the platform. All values can be <code>null</code>.
 * @author Jason Jenkins
 * @version 0.17.0-20150921
 */
public class AppCache extends Application {
	
	private static boolean REMOTE_DEBUGGING = DebugConstants.REMOTE_ERROR_REPORTING;
	/** Includes user specific, sensitive data. Always set false for production. */
	private static boolean REMOTE_VERBOSE_DEBUGGING = DebugConstants.REMOTE_PDETAIL_REPORTING; 
	
	private static final int INTERVAL_GOOGLE_TRIM = 3600000; //1 hr 
	private static final int INTERVAL_VALID_USER_CONTENT = 30000; //30 seconds
	private static final int INTERVAL_GOOGLE_SEARCH_RESULTS = 21600000; //6 hrs
	
	private static final int MIN_CACHE_COUNT = 2;
	private static final int SMALL_CACHE_COUNT = 8;
	private static final int CACHE_COUNT = 15;

	
	private static final Object sLock = new Object();	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start constant content
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static BibleResponse sBibleResponse = null;
	
	private static final FrequentlyUsedList<Chapter> sChapterDataList = 
			new FrequentlyUsedList<Chapter>(CACHE_COUNT);
	
	private static final FrequentlyUsedList<BibleSearchResponse> sBibleSearchResponseList = 
			new FrequentlyUsedList<BibleSearchResponse>(CACHE_COUNT);
	
	private static final FrequentlyUsedList<BibleVerseDetailsResponse> sBibleVerseDetailsResponseList = 
			new FrequentlyUsedList<BibleVerseDetailsResponse>(SMALL_CACHE_COUNT);
	
	private static final FrequentlyUsedList<StrongsResponse> sStrongsResponseList = 
			new FrequentlyUsedList<StrongsResponse>(SMALL_CACHE_COUNT);
	
	private static final FrequentlyUsedList<CrossReferenceResponse> sCrossReferenceResponseList = 
			new FrequentlyUsedList<CrossReferenceResponse>(SMALL_CACHE_COUNT);
	
	private static final DialogStateHolder sUpgradeDialogState = new DialogStateHolder();
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start mixed content
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static final FrequentlyUsedList<TimedWrapper<BibleChapterResponse>> sBibleChapterResponseList = 
			new FrequentlyUsedList<TimedWrapper<BibleChapterResponse>>(SMALL_CACHE_COUNT);
	
	private static final FrequentlyUsedList<TimedWrapper<BibleVerseResponse>> sBibleVerseResponseList = 
			new FrequentlyUsedList<TimedWrapper<BibleVerseResponse>>(CACHE_COUNT);
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start user content
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static TimedWrapper<StudyNotesResponse> sStudyNotesReponse = null; 
	
	private static final FrequentlyUsedList<TimedWrapper<StudyNotesResponse>> sStudyNoteSearchResponseList = 
			new FrequentlyUsedList<TimedWrapper<StudyNotesResponse>>(SMALL_CACHE_COUNT);
	
	private static TimedWrapper<ImageListResponse> sImageListResponse = null;
	private static TimedWrapper<LetteringListResponse> sLetteringListResponse = null;
	
	private static TimedWrapper<ImageListResponse> sLibraryImageListResponse = null;
	private static TimedWrapper<LetteringListResponse> sLibraryLetteringListResponse = null;
	
	private static final FrequentlyUsedList<TimedWrapper<GoogleImageResponse>> sGoogleImageResponse = 
			new FrequentlyUsedList<TimedWrapper<GoogleImageResponse>>(SMALL_CACHE_COUNT);

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End cache values
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static final List<OnCacheUpdateListener<ImageListResponse>> sImageListListeners 
		= Collections.synchronizedList(new ArrayList<OnCacheUpdateListener<ImageListResponse>>());
	
	private static final List<OnCacheUpdateListener<LetteringListResponse>> sLetteringListListeners 
		= Collections.synchronizedList(new ArrayList<OnCacheUpdateListener<LetteringListResponse>>());
	
	private static final List<OnCacheUpdateListener<BibleVerseResponse>> sBibleVerseListeners 
		= Collections.synchronizedList(new ArrayList<OnCacheUpdateListener<BibleVerseResponse>>());
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End static members
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	@Override
	public void onCreate() {
		super.onCreate();
		
		if (REMOTE_DEBUGGING) {
			 
			String pendingError = clearPendingErrorTrace();
			if (pendingError != null) {
				getDefaultTracker().send(
						new HitBuilders	.ExceptionBuilder()
										.setDescription(pendingError)
										.setFatal(true).build());
			}
			
			// Set global uncaught exception tracking:
			// http://stackoverflow.com/questions/25351648/how-to-get-google-analytics-v4-to-report-uncaught-exceptions-crashes-for-my-ap
			// https://developers.google.com/analytics/devguides/collection/android/v4/exceptions

			final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
			UncaughtExceptionHandler myHandler = new ExceptionReporter(
					getDefaultTracker(),                              // Currently used Tracker.
					new Thread.UncaughtExceptionHandler() { 		// Current default uncaught exception handler.
					    @Override
					    public void uncaughtException(Thread thread, Throwable throwable) {		
					    	setPendingErrorTrace(thread, throwable);
					        //Log.wtf("AppCache", err);
					        defaultUncaughtExceptionHandler.uncaughtException(thread, throwable);
					    }
					},      
				    this); 						// Context of the application.
		    Thread.setDefaultUncaughtExceptionHandler(myHandler);
		}
		
		//consider: http://stackoverflow.com/questions/17211154/universal-image-loader-uil-nostra-out-of-memory-error 
		
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
        	//we do not cache in memory to avoid Out of Memory exceptions
			.cacheInMemory(false)	
        	.cacheOnDisk(true)        	
        	.bitmapConfig(Bitmap.Config.RGB_565)
        	.imageScaleType(ImageScaleType.EXACTLY)
        	.build();
		
		final int MEM_SIZE = 1024 * 1024;
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
        	.defaultDisplayImageOptions(defaultOptions)
        	.memoryCache(new WeakMemoryCache())
        	.memoryCacheSize(MEM_SIZE)
        	.threadPoolSize(2)
        	.diskCacheExtraOptions(600, 600, null)
            .build();
        
        ImageLoader.getInstance().init(config);
        
        //clean up every start
        ImageLoader.getInstance().clearDiskCache();
        ImageLoader.getInstance().clearMemoryCache();
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		//clear if low on memory
        ImageLoader.getInstance().clearDiskCache();        

        resizeList(sChapterDataList);
        resizeList(sBibleSearchResponseList);
        resizeList(sBibleVerseDetailsResponseList);
        resizeList(sStrongsResponseList);
        resizeList(sCrossReferenceResponseList);
        
        resizeList(sBibleChapterResponseList);
        resizeList(sBibleVerseResponseList);
        resizeList(sGoogleImageResponse);
        
        cleanList(sBibleChapterResponseList, INTERVAL_VALID_USER_CONTENT);
		cleanList(sBibleVerseResponseList, INTERVAL_VALID_USER_CONTENT);
		cleanList(sStudyNoteSearchResponseList, INTERVAL_VALID_USER_CONTENT);
		cleanList(sGoogleImageResponse, INTERVAL_GOOGLE_TRIM);
	}

	
	
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		if (level >= TRIM_MEMORY_MODERATE) {
			cleanList(sBibleChapterResponseList, INTERVAL_VALID_USER_CONTENT);
			cleanList(sBibleVerseResponseList, INTERVAL_VALID_USER_CONTENT);
			cleanList(sStudyNoteSearchResponseList, INTERVAL_VALID_USER_CONTENT);
			cleanList(sGoogleImageResponse, INTERVAL_GOOGLE_TRIM);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Error handling
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static final String PENDING_STACK_TRACE = AppCache.class.getSimpleName() + ".PENDING_STACK_TRACE";
	
	/** Clears and returns the pending stack trace. 
	 * @return <code>null</code> if unset. */
	protected String clearPendingErrorTrace() {
		String result = PreferenceUtil.getPreferences(this).getString(PENDING_STACK_TRACE, null);
		PreferenceUtil.getPreferences(this).edit().putString(PENDING_STACK_TRACE, null).commit();
		return result;
	}
	/** Creates and saves the pending stack-trace. */
	protected String setPendingErrorTrace(Thread thread, Throwable throwable) {
		StackTraceElement[] stack = throwable.getStackTrace();
		
		StringBuilder err = new StringBuilder();
		err.append(	"Caused by: " + 
						new StandardExceptionParser(getApplicationContext(), null)
								.getDescription(thread.getName(), throwable));
		
		if (REMOTE_VERBOSE_DEBUGGING) {  
			err.append("\n(Message): " + throwable.getMessage());
			CurrentUser user =  new CurrentUser(this);
			String userId = "";
			if (user != null) {
				userId = user.getMemberId();
				err.append("\n(User '" + userId+"' )");
			}
		} else {
			CurrentUser user =  new CurrentUser(this);
			String msg = "\n(User '";
			if (user != null && !TextUtils.isEmpty(user.getMemberId())) {
				msg += "signed in')";
			} else {
				msg += "not signed in')";
			}
			err.append("\n(User '" + msg+"' )");
		}
		err.append("\n(Partial-stack):\n");
		if (stack != null) {
            for (int i = 0; i < stack.length; i++) {
            	err.append("\tat ");
            	err.append(stack[i].toString());
            	err.append("\n");
            }
        }
		PreferenceUtil.getPreferences(this).edit()
			.putString(PENDING_STACK_TRACE, err.toString()).commit();
		return err.toString();
	}
	
	
	private Tracker mTracker;

	  /**
	   * Gets the default {@link Tracker} for this {@link Application}.
	   * @return tracker
	   */
	synchronized public Tracker getDefaultTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.analytics_debug_tracking_config);
			mTracker.enableExceptionReporting(true);
			mTracker.enableAutoActivityTracking(true);
		}
		return mTracker;
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Event Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Remember to call {@link #removeBibleVerseListener(OnCacheUpdateListener)} 
	 * when destroying object to avoid memory leaks. Notifies whenever an item is <b>added</b>.
	 * If <code>value</code> returned is <code>null</code>, the contents have been reset. */
	public static void addBibleVerseUpdateListener(OnCacheUpdateListener<BibleVerseResponse> listener) {
		synchronized (sBibleVerseListeners) {
			if (!sBibleVerseListeners.contains(listener)) { //only add what is not already there.
				sBibleVerseListeners.add(listener);
			}
		}
	}
	
	/** Removed the event listener. */
	public static boolean removeBibleVerseUpdateListener(OnCacheUpdateListener<BibleVerseResponse> listener) {
		synchronized (sBibleVerseListeners) {
			return sBibleVerseListeners.remove(listener);
		}
	}
	
	/** Remember to call {@link #removeLetteringListListener(OnCacheUpdateListener)} 
	 * when destroying object to avoid memory leaks. Notifies whenever an item is <b>added</b>.
	 * If <code>value</code> returned is <code>null</code>, the contents have been reset. */
	public static void addLetteringListUpdateListener(OnCacheUpdateListener<LetteringListResponse> listener) {
		synchronized (sLetteringListListeners) {
			if (!sLetteringListListeners.contains(listener)) { //only add what is not already there.
				sLetteringListListeners.add(listener);
			}
		}
	}
	
	/** Removed the event listener. */
	public static boolean removeLetteringListUpdateListener(OnCacheUpdateListener<LetteringListResponse> listener) {
		synchronized (sLetteringListListeners) {
			return sLetteringListListeners.remove(listener);
		}
	}
	
	/** Remember to call {@link #removeImageListListener(OnCacheUpdateListener)} 
	 * when destroying object to avoid memory leaks. Notifies whenever an item is <b>added</b>.
	 * If <code>value</code> returned is <code>null</code>, the contents have been reset. */
	public static void addImageListUpdateListener(OnCacheUpdateListener<ImageListResponse> listener) {
		synchronized (sImageListListeners) {
			if (!sImageListListeners.contains(listener)) { //only add what is not already there.
				sImageListListeners.add(listener);
			}
		}
	}
	
	/** Removed the event listener. */
	public static boolean removeImageListUpdateListener(OnCacheUpdateListener<ImageListResponse> listener) {
		synchronized (sImageListListeners) {
			return sImageListListeners.remove(listener);
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Cache clearing
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Removes the provided uri image from {@link ImageLoader} */
	public static void clearImageFromCache(String imageUri) {
		if (imageUri == null) {
			return; //safety
		}
		final ImageLoader imageLoader =  ImageLoader.getInstance();
		MemoryCacheUtils.removeFromCache(imageUri, imageLoader.getMemoryCache());
		DiskCacheUtils.removeFromCache(imageUri, imageLoader.getDiskCache());
	}

	/** Clears all the bible responses for current version */
	public static void clearBibleResponses() {
		synchronized (sLock) {
			sBibleResponse = null;
			sBibleSearchResponseList.clear();	
			sBibleVerseDetailsResponseList.clear();

			sChapterDataList.clear();		
			
			sBibleChapterResponseList.clear();
			sBibleVerseResponseList.clear();
			
			sStrongsResponseList.clear();
			sCrossReferenceResponseList.clear();			
		}		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Mutators
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void setUpgradeDialogContent(boolean fetched, String title, String message) {
		sUpgradeDialogState.fetched = fetched;
		sUpgradeDialogState.title = title;
		sUpgradeDialogState.message = message;
	}
	
	public static void setsStudyNotesReponse(StudyNotesResponse response) {
		synchronized (sLock) {
			AppCache.sStudyNotesReponse =  new TimedWrapper<StudyNotesResponse>(response);
		}
	}
	
	public static void setLetteringListResponse(LetteringListResponse response) {
		synchronized (sLock) {
			AppCache.sLetteringListResponse = new TimedWrapper<LetteringListResponse>(response);
			if (response == null && AppCache.sLetteringListResponse != null) {
				AppCache.sLetteringListResponse = null;
				notifyCacheListeners(sLetteringListListeners, null, response);
			}
		}
	}
	
	public static void setImageListResponse(ImageListResponse response) {
		synchronized (sLock) {
			AppCache.sImageListResponse = new TimedWrapper<ImageListResponse>(response);
			if (response == null && AppCache.sImageListResponse != null) {
				AppCache.sImageListResponse = null;
				notifyCacheListeners(sImageListListeners, null, response);
			}
		}
	}
	
	public static void setLibraryLetteringListResponse(LetteringListResponse response) {
		synchronized (sLock) {
			AppCache.sLibraryLetteringListResponse = new TimedWrapper<LetteringListResponse>(response);
			if (response == null) {
				AppCache.sLibraryLetteringListResponse = null;
			}
		}
	}
	
	public static void setLibraryImageListResponse(ImageListResponse response) {
		synchronized (sLock) {
			AppCache.sLibraryImageListResponse = new TimedWrapper<ImageListResponse>(response);
			if (response == null) {
				AppCache.sLibraryImageListResponse = null;
			}
		}
	}
	
		
	public static void addBibleChapterResponse(String key, BibleChapterResponse response) {
		synchronized (AppCache.sBibleChapterResponseList) {
			AppCache.sBibleChapterResponseList.put(key, new TimedWrapper<BibleChapterResponse>(response));
		}
	}

	
	/** Adds verse response, if <code>null</code> clears the response and the lettering + image list responses. */
	public static void addBibleVerseResponse(String key, BibleVerseResponse response) {
		synchronized (AppCache.sBibleVerseResponseList) {
			if (!sBibleVerseResponseList.containsKey(key) && response == null) {
				return; //if never existed, do not update (prevent unnecessary calls)
			} else if (response == null) {
				AppCache.setsStudyNotesReponse(null); //clear notes response
			}
			AppCache.sBibleVerseResponseList.put(key, new TimedWrapper<BibleVerseResponse>(response));
			notifyCacheListeners(sBibleVerseListeners, key, response);
		}
	}
	

	
	public static void addStudyNotesSearchResponse(String key, StudyNotesResponse response) {
		synchronized (AppCache.sStudyNoteSearchResponseList) {
			AppCache.sStudyNoteSearchResponseList.put(key, new TimedWrapper<StudyNotesResponse>(response));
		}
	}
	
	public static void addGoogleImageSearchResult(String key, GoogleImageResponse response) {
		synchronized (AppCache.sGoogleImageResponse) {
			AppCache.sGoogleImageResponse.put(key, new TimedWrapper<GoogleImageResponse>(response));
		}
	}
			
	public static void addBibleVerseDetailsResponse(String key, BibleVerseDetailsResponse response) {
		synchronized (AppCache.sBibleVerseDetailsResponseList) {
			AppCache.sBibleVerseDetailsResponseList.put(key, response);
		}
	}	
	
	public static void addCrossReferenceResponse(String key, CrossReferenceResponse response) {
		synchronized (AppCache.sCrossReferenceResponseList) {
			AppCache.sCrossReferenceResponseList.put(key, response);
		}
	}
	
	public static void addStrongsResponse(String key, StrongsResponse response) {
		synchronized (AppCache.sStrongsResponseList) {
			AppCache.sStrongsResponseList.put(key, response);
		}
	}

	
	
	public static void addChapterData(String key, Chapter chapter) {
		synchronized (AppCache.sChapterDataList) {
			AppCache.sChapterDataList.put(key, chapter);
		}
	}
	
	public static void addBibleSearchResponse(String key, BibleSearchResponse response) {
		synchronized (AppCache.sBibleSearchResponseList) {
			AppCache.sBibleSearchResponseList.put(key, response);
		}
	}
	
	public static void setBibleResponse(BibleResponse response) {
		synchronized (sLock) {
			AppCache.sBibleResponse = response;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Accessors
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static DialogStateHolder getUpgradeDialogState() {
		return sUpgradeDialogState;
	}
	
	/** Determines if a given image is in the {@link ImageLoader} cache. */
	public static boolean isImageInLoaderCache(String imageUri) {
		if (imageUri == null) {
			return false; //safety
		}
		final ImageLoader imageLoader =  ImageLoader.getInstance();
		File file = DiskCacheUtils.findInCache(imageUri, imageLoader.getDiskCache());
		return 	!MemoryCacheUtils.findCachedBitmapsForImageUri(imageUri, imageLoader.getMemoryCache()).isEmpty() || 
				file.exists();
	}
	
	/** @return The study notes response or <code>null</code> if invalid. */
	public static StudyNotesResponse getStudyNotesResponse() {
		synchronized (sLock) {
			return checkCacheTimeAndReturn(sStudyNotesReponse, INTERVAL_VALID_USER_CONTENT);
		}
	}
	
	/** @return The lettering list response or <code>null</code> if invalid. */
	public static LetteringListResponse getLetteringListResponse() {
		synchronized (sLock) {
			return checkCacheTimeAndReturn(sLetteringListResponse, INTERVAL_VALID_USER_CONTENT);
		}
	}
	
	/** @return The image list response or <code>null</code> if invalid. */
	public static ImageListResponse getImageListResponse() {
		synchronized (sLock) {
			return checkCacheTimeAndReturn(sImageListResponse, INTERVAL_VALID_USER_CONTENT);
		}
	}
	
	/** @return The lettering list response or <code>null</code> if invalid. */
	public static LetteringListResponse getLibraryLetteringListResponse() {
		synchronized (sLock) {
			return checkCacheTimeAndReturn(sLibraryLetteringListResponse, INTERVAL_VALID_USER_CONTENT);
		}
	}
	
	/** @return The image list response or <code>null</code> if invalid. */
	public static ImageListResponse getLibraryImageListResponse() {
		synchronized (sLock) {
			//Thought: consider changing to 6 hours
			return checkCacheTimeAndReturn(sLibraryImageListResponse, INTERVAL_VALID_USER_CONTENT); 
		}
	}
		

	
	public static BibleChapterResponse getBibleChapterResponse(String key) {
		synchronized (sBibleChapterResponseList) {
			return cleanAndReturnValue(sBibleChapterResponseList, key, INTERVAL_VALID_USER_CONTENT);
		}
	}
	
	public static BibleVerseResponse getBibleVerseResponse(String key) {
		synchronized (sBibleVerseResponseList) {
			return cleanAndReturnValue(sBibleVerseResponseList, key, INTERVAL_VALID_USER_CONTENT);
		}
	}
	
	
	public static GoogleImageResponse getGoogleImageSearchResponse(String key) {
		synchronized (sGoogleImageResponse) {
			return cleanAndReturnValue(sGoogleImageResponse, key, INTERVAL_GOOGLE_SEARCH_RESULTS);
		}
	}
	

	
	public static StudyNotesResponse getStudyNotesSearchResponse(String key) {
		synchronized (sStudyNoteSearchResponseList) {
			return cleanAndReturnValue(sStudyNoteSearchResponseList, key, INTERVAL_VALID_USER_CONTENT);
		}
	}
	
	
	public static BibleVerseDetailsResponse getBibleVerseDetailsResponse(String key) {
		synchronized (sBibleVerseDetailsResponseList) {
			return sBibleVerseDetailsResponseList.get(key);
		}
	}
	
	public static CrossReferenceResponse getCrossReferenceResponse(String key) {
		synchronized (sCrossReferenceResponseList) {
			return sCrossReferenceResponseList.get(key);
		}
	}
	
	public static StrongsResponse getStrongsResponse(String key) {
		synchronized (sStrongsResponseList) {
			return sStrongsResponseList.get(key);
		}
	}
	
	public static Chapter getChapterData(String key) {
		synchronized (sChapterDataList) {
			return sChapterDataList.get(key);
		}
	}
	
	public static BibleSearchResponse getBibleSearchResponse(String key) {
		synchronized (sBibleSearchResponseList) {
			return sBibleSearchResponseList.get(key);
		}
	}
	
	public static BibleResponse getBibleResponse() {
		synchronized (sLock) {
			return sBibleResponse;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Notifies the listeners of the given list. 
	 * @param listenerList
	 * @param key
	 * @param value
	 */
	private static <E> void notifyCacheListeners(final List<OnCacheUpdateListener<E>> listenerList, String key, E value) {
		synchronized (listenerList) {			
			final int SIZE =  listenerList.size();
			for(int index = 0; index < SIZE; index++) {
				OnCacheUpdateListener<E> listener = listenerList.get(index);
				if (listener != null) {
					listener.onCacheUpdate(key, value);
				}
			}
		}
	}

	/** Checks to see if the given item has expired, if not returns value, if so sets to null
	 * returns null. */
	private static <E> E checkCacheTimeAndReturn(TimedWrapper<E> wrapper, int interval) {
		if (wrapper != null) {
			if (new Date().getTime() - wrapper.cacheTime > interval) {
				wrapper = null;
				return null;
			}
			return wrapper.object;
		}
		//response is null, return null
		return null;
	}
	
	/** Cleans the given {@link FrequentlyUsedList} with {@link TimedWrapper} element, and returns the requested
	 * element with the given key
	 * @param list
	 * @param key
	 * @param interval
	 * @return
	 */
	private static <E> E cleanAndReturnValue(FrequentlyUsedList<TimedWrapper<E>> list, String key, int interval) {
		cleanList(list, interval);
		
		TimedWrapper<E> wrapper = list.get(key);
		if (wrapper == null) {
			return null;
		}
		return wrapper.object;
	}
	
	
	/** Simply cleans the given {@link FrequentlyUsedList} with {@link TimedWrapper} element
	 * @param list
	 * @param interval
	 */
	private static <E> void cleanList(FrequentlyUsedList<TimedWrapper<E>> list, int interval) {
		boolean hasExpired = false;
		do { //clean cache before fetching.
			String leastRecentKey = list.getLeastRecentKey(); //oldest key
			if (leastRecentKey == null) {
				break;
			}
			TimedWrapper<E> wrapper = list.get(leastRecentKey);
			if (wrapper == null) {
				break;
			}
			hasExpired = (new Date().getTime() - wrapper.cacheTime) //check if oldest key has expired
					> interval; 
					
			if (hasExpired) { //remove if so
				list.put(leastRecentKey, null);
			}			
		} while (hasExpired); 
	}
	
	/** Attempts to resize the list by half, if half is less than min size then sets to min size. */
	private static <E> void resizeList(FrequentlyUsedList<E> list) {
		final int CACHE_SIZE = list.size();
		if (CACHE_SIZE == MIN_CACHE_COUNT) {
			return; //nothing to be done.
		}
        int newCacheSize = CACHE_SIZE/2;
        if (newCacheSize < MIN_CACHE_COUNT) {
        	newCacheSize = MIN_CACHE_COUNT;
        } 
        list.resize(newCacheSize);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Provides a means to date items before inserting into a list. 
	 * @version 0.1.0-20150714 */
	private static class TimedWrapper<E> {
		public final E object;
		public final long cacheTime;
		public TimedWrapper(E object) {
			this.object = object;
			this.cacheTime = new Date().getTime();
		}
	}
	
	/** @version 0.2.1-20150804 */
	private static class FrequentlyUsedList<E> extends Hashtable<String,E> {
		private static final long serialVersionUID = -9147816780983547487L;
		
		private final ArrayDeque<String> leastFrequentlyUsed = new ArrayDeque<String>();
		private int limit;
		
		public FrequentlyUsedList(int size) {
			super(size);
			this.limit = size;
		}
	
		@Override
		public synchronized E put(String key, E value) {
			if (value == null) {
				super.remove(key);
				leastFrequentlyUsed.remove(key);
				return null;
			}
			if (super.size() + 1 > limit) {
				String leastUsed = leastFrequentlyUsed.pop();
				super.remove(leastUsed);
			}
			leastFrequentlyUsed.addLast(key);
			return super.put(key, value);
		}
		
		/** Do not use. 
		 * Use {@link #put(String, Object)} with <code>null</code> instead. */
		@Override
		public synchronized E remove(Object key) {
			return null;
		}
		
		@Override
		public synchronized E get(Object key) {
			if (key == null) {
				return null; //null key null results
			}
			if (super.containsKey(key)) { //if found
				leastFrequentlyUsed.remove(key);
				leastFrequentlyUsed.addLast((String)key); //update position
			}
			return super.get(key);
		}
		
		@Override
		public synchronized int size() {
			return super.size() > limit ? super.size() : limit;
		}
		
		/** @return Returns the least recently used key or <code>null</code>, if empty. */ 
		public synchronized String getLeastRecentKey() {
			return leastFrequentlyUsed.peek();
		}
		
		/** Sets the size and removes any items larger than the container.  
		 * @param size Sets a new size to limit the container. */
		public synchronized void resize(int size) {
			this.limit = size;
			while(leastFrequentlyUsed.size() > size) {
				String key = leastFrequentlyUsed.remove();
				remove(key); //remove all old items
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Notifies listeners when a cache item is changed in any way.
	 * @version 1.0.0-20150728
	 *
	 */
	public static interface OnCacheUpdateListener <E> {
		/**
		 * Sent when an element is updated
		 * @param key The key updated, <code>null</code> if not present.
		 * @param value The value given, can be <code>null</code>
		 */
		public void onCacheUpdate(@Nullable String key, @Nullable E value);
	}
}
