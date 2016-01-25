package com.inductivebiblestudyapp.data.loaders;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.data.service.MarkingService;

/**
 * Loader to update markings api using {@link AsyncTaskLoader} and APPLY markings. 
 * 
 * @author Jason Jenkins
 * @version 0.1.0-20150721
 */
public class MarkingApplyAsyncLoader extends AbstractUpdateAsyncLoader2<MarkingService> {
	final static private String CLASS_NAME = MarkingApplyAsyncLoader.class
			.getSimpleName();
	
	
	public static final String TYPE_LETTERING = CLASS_NAME + ".TYPE_LETTERING";
	public static final String TYPE_IMAGE = CLASS_NAME + ".TYPE_IMAGE";
	
	/** Bundle key: String. The Type to use; either {@link #TYPE_LETTERING} or {@link #TYPE_IMAGE} */
	public static final String KEY_TYPE = CLASS_NAME + ".KEY_TYPE";
	/** Bundle key: String. The id to create with. */
	public static final String KEY_TYPE_ID = CLASS_NAME + ".KEY_TYPE_ID";

	private final boolean mIsLetteringMode;
	private final String mId;
	
	private final String mChapterId;
	private final int mSelectedStart;
	private final int mSelectedEnd;
	private final String mVerseRange;
		
	public MarkingApplyAsyncLoader(Context context, Bundle args, String chapterId, 
			int startIndex, int endIndex, String verseRange) {
		super(context, RestClient.getInstance().getMarkingService());
		if ( !(	args.containsKey(KEY_TYPE) && args.containsKey(KEY_TYPE_ID)) ) {
			throw new IllegalArgumentException("Bundle is missing one or more minimum requirments: " + args);
		}
		
		this.mIsLetteringMode = args.getString(KEY_TYPE).equals(TYPE_LETTERING);
		this.mId = args.getString(KEY_TYPE_ID);
		
		this.mChapterId = chapterId;
		this.mSelectedStart = startIndex;
		this.mSelectedEnd = endIndex;
		this.mVerseRange = verseRange;
	}
	
	

	@Override
	protected UpdateResult fetchResult() {
		UpdateResult result = null;

		if (mIsLetteringMode) {
			result = mService.createLetteringMarking(
					mAccessToken, 
					mId, 
					mSelectedStart, 
					mSelectedEnd, 
					mChapterId,
					mVerseRange);
		} else {
			result = mService.createImageMarking(
					mAccessToken, 
					mId, 
					mSelectedStart, 
					mSelectedEnd, 
					mChapterId,
					mVerseRange);
		}		
		
		return result;
	}	
}
