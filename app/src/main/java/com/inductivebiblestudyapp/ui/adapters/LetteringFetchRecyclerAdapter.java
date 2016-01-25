package com.inductivebiblestudyapp.ui.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.AppCache.OnCacheUpdateListener;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.data.model.LetteringListResponse;
import com.inductivebiblestudyapp.data.service.LetteringService;
import com.inductivebiblestudyapp.util.Utility;
/**
 * Adapter for a horizontal recycler view.
 * @author Jason Jenkins
 * @version 0.10.0-20150831
 */
public class LetteringFetchRecyclerAdapter extends Adapter<LetteringFetchRecyclerAdapter.ViewHolder> 
	implements OnCacheUpdateListener<LetteringListResponse> {
	
	final static private String LOGTAG = LetteringFetchRecyclerAdapter.class
			.getSimpleName();	

	/** Whether or not to include unused items for {@link #SORT_RECENT_ASC}.  */ 
	private static final boolean SORT_RECENT_UNUSED = true;
	
	public static final int SORT_NAME_ASC = 0;
	public static final int SORT_RECENT_ASC = 1;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////

	/* Remember: double references do not equal double memory or objects. */
	/** Set in {@link #responseToMarkingList(LetteringListResponse)} */
	private final List<LetteringSpanHolder> mLetteringSpanList = new ArrayList<LetteringSpanHolder>();
	
	/** Array of indices for {@link #mLetteringSpanList} */
	private final List<LetteringSpanHolder> mFilteredLetteringByName = new ArrayList<LetteringSpanHolder>();
	/** Array of indices for {@link #mLetteringSpanList} */
	private final List<LetteringSpanHolder> mFilteredLetteringByRecent = new ArrayList<LetteringSpanHolder>();
	
	private final Activity mActivity;
	
	private final int mDisplayLimit;
	
	private final boolean mUseLibrary; 
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End finals
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private LetteringListResponse mLetteringListResponse = null;
	
	/** Can be <code>null</code>. */
	private OnLetteringItemClickListener mOnLetteringItemClickListener = null;	
	/** Can be <code>null</code>. */
	private OnAdapterStateListener mOnAdapterStateListener = null;
	/** Can be <code>null</code>. */
	private OnNotifyDataSetChangedListener mOnNotifyDataSetChangedListener = null;
		
	private boolean isLoading = true;
	private boolean noMatchingResults = false;
	private boolean isEmpty = false;
	private boolean cannotConnect = false;
	
	private int mSortMode = SORT_NAME_ASC;
	
	/** The current filter string. Set in {@link #filter(String)} */
	private String mFilterString = "";
	
	/**
	 * @param activity
	 * @param size The maxmium number of items to display. If 0, it is ignored.
	 * @library <code>true</code> to use library fetch, <code>false</code> to use user fetch
	 */
	public LetteringFetchRecyclerAdapter(Activity activity, int size, boolean library) {
		this.mActivity = activity;
		this.mDisplayLimit = size;
		this.mUseLibrary = library;
		
		fetchLetterings();
	}
	
	/** Same as calling #LetteringFetchRecyclerAdapter(Activity, int, boolean)false */
	public LetteringFetchRecyclerAdapter(Activity activity, int size) {
		this(activity, size, false);
	}
	
	/** Same as calling #LetteringFetchRecyclerAdapter(Activity, int, boolean) with 0 */
	public LetteringFetchRecyclerAdapter(Activity activity, boolean library) {
		this(activity, 0, library);
	}
	
	/** Same as calling #LetteringFetchRecyclerAdapter(Activity, int, boolean) with 0 and false */
	public LetteringFetchRecyclerAdapter(Activity activity) {
		this(activity, 0, false);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// public methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Sets the filter and causes the adapter to update results based on naming & sorting. 
	 * @param The string to filter on, case insensitive. */
	public void filter(String filterString) {
		if (filterString == null || mFilterString.equals(filterString.trim().toLowerCase(Locale.US))) {
			return; //do nothing
		}
		mFilterString = filterString.trim().toLowerCase(Locale.US);
		
		mFilteredLetteringByName.clear();		
		mFilteredLetteringByRecent.clear();
		
		for (LetteringSpanHolder holder : mLetteringSpanList) {			
			addSpanItemToFilter(holder);
		}		
		Collections.sort(mFilteredLetteringByRecent, mSortByRecentComparator);
		notifyDataSetChangedWrapper();
	}
	
	/** Sets and applies the sort mode. */
	public void setSortMode(int mode) {
		if (SORT_NAME_ASC != mode && SORT_RECENT_ASC != mode) {
			Log.w(LOGTAG, "Invalid sort mode supplied: " + mode);
			return;
		}
		mSortMode = mode;
		notifyDataSetChangedWrapper();
	}
	  
	/** @return Returns the lettering item at the given position or
	 * <code>null</code> if either empty or cannot connect. */
	public LetteringItem getItem(int pos) {
		if (isLoading || noMatchingResults || isEmpty || cannotConnect) {
			return null;
		}
		
		if (pos > mDisplayLimit && mDisplayLimit > 0) {
			pos = mDisplayLimit;
		}
		final int index = getList().get(pos).indexOfLetteringItem;
		return mLetteringListResponse.getLetterings()[index];
	}
		
	/** @param listener The lettering item click listener. */
	public void setOnLetteringItemClickListener(OnLetteringItemClickListener listener) {
		this.mOnLetteringItemClickListener = listener;
	}
	
	/** @param stateListener The listener to ok, empty, or cannot connect. */
	public void setOnAdapterStateListener(OnAdapterStateListener stateListener) {
		this.mOnAdapterStateListener = stateListener;
		updateStateListener();
	}
	
	/** @param listener The listener for wrappers to know when the data set has been updated. */
	protected void setOnNotifyDataSetChangedListener(OnNotifyDataSetChangedListener listener) {
		this.mOnNotifyDataSetChangedListener = listener;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Override methos
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public int getItemCount() {
		final int SIZE = getSize();
		if (isLoading || noMatchingResults || isEmpty || cannotConnect) {
			return 1; //the message item
		}
		
		if (mDisplayLimit > 0 && mDisplayLimit < SIZE){
			return mDisplayLimit; //at most
		}
		//display all
		return SIZE;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		if (isLoading) {
			setHolderMessage(holder, R.string.ibs_text_loading);
			return;
		} else if (cannotConnect) {
			setHolderMessage(holder, R.string.ibs_error_cannotConnect);
			return;
		} else if (isEmpty) {
			int stringRes = 0; 
			if (mUseLibrary) {
				stringRes = R.string.ibs_text_noLetterings_library;
			} else if (SORT_RECENT_ASC == mSortMode) {
				stringRes = R.string.ibs_text_noLetterings_user_recent;
			} else {
				stringRes = R.string.ibs_text_noLetterings_user;
			}
			setHolderMessage(holder, stringRes);
			return;
		} else if (noMatchingResults) {
			setHolderMessage(holder, R.string.ibs_text_noLetteringResults);
			return;
		} else {
			holder.tvContent.getLayoutParams().width = holder.defaultContentWidth;
		}
		
		final SpannableString marking = getSpanItem(position);
		holder.tvContent.setText(marking);
		
		holder.rootView.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				if (mOnLetteringItemClickListener != null) {
					mOnLetteringItemClickListener.onLetteringItemClick(getItem(position));
				}					
			}
		});
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	    View v = LayoutInflater.from(parent.getContext())
	    			.inflate(R.layout.grid_item_lettering, parent, false);
	    	    
	    return new ViewHolder(v);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Hides the image view and sets the text for display */
	private void setHolderMessage(ViewHolder holder, int stringId) {
		holder.tvContent.setText(stringId);
		holder.tvContent.getLayoutParams().width = LayoutParams.MATCH_PARENT;
	}
	
	/** Used to call {@link #notifyDataSetChanged()} & update listener. */
	private void notifyDataSetChangedWrapper(){
		if (getSize() == 0 && !isLoading) {
			isEmpty = mLetteringSpanList.isEmpty(); //only if the actual items are empty, not the filter
			noMatchingResults = !mFilterString.isEmpty(); //only if a filter is set
		} else {
			isEmpty = false;
			cannotConnect = false;
			noMatchingResults = false;
		}
		
		notifyDataSetChanged();
		if (mOnNotifyDataSetChangedListener != null) {
			mOnNotifyDataSetChangedListener.onNotifyDataSetChanged();
		}
		
		updateStateListener();
	}

	private void updateStateListener() {
		if (mOnAdapterStateListener != null) {
			int state = OnAdapterStateListener.STATE_OK;
			if (isEmpty || noMatchingResults) {
				state = OnAdapterStateListener.STATE_EMPTY;
			} else if (cannotConnect) {
				state = OnAdapterStateListener.STATE_CANNOT_CONNECT;
			}
			mOnAdapterStateListener.onStateUpdate(state);
		}
	}
	
	/** Fetches letterings from the server and caches them. */
	private void fetchLetterings() {
		mLetteringListResponse = 
				mUseLibrary ? AppCache.getLibraryLetteringListResponse() : AppCache.getLetteringListResponse();
		if (mLetteringListResponse != null) {
			responseToMarkingList(mLetteringListResponse);
			isLoading = false;
			return;
		}
		
		final String accessToken = new CurrentUser(mActivity).getIBSAccessToken();
		LetteringService service = RestClient.getInstance().getLetteringService();
		
		if (mUseLibrary) {
			service.listLibrary(accessToken, mLetteringCallback);
		} else {
			service.list(accessToken, mLetteringCallback);
		}
	}
	
	/** Converts a response to a list of spannables to display. */
	private void responseToMarkingList(LetteringListResponse response) {
		LetteringItem[] nameItems = response.getLetterings();

		mLetteringSpanList.clear();
		mFilteredLetteringByName.clear();
		mFilteredLetteringByRecent.clear();
		
		int index = 0;
		for (LetteringItem letteringItem : nameItems) {
			LetteringSpanHolder holder = new LetteringSpanHolder(letteringItem, index++);			
			mLetteringSpanList.add(holder);			
			addSpanItemToFilter(holder);
		}
		Collections.sort(mFilteredLetteringByRecent, mSortByRecentComparator);
		
		notifyDataSetChangedWrapper();
	}
	
	/** Tests the given span holder & {@link #mFilterString} to see if the item should be 
	 * added to the filter lists.
	 * @param spanHolder
	 */
	private void addSpanItemToFilter(LetteringSpanHolder spanHolder) {
		final boolean matchesFilter = mFilterString.isEmpty() || 
				spanHolder.name.toString().toLowerCase(Locale.US).startsWith(mFilterString);
		if (matchesFilter) {
			mFilteredLetteringByName.add(spanHolder);
			
			if (spanHolder.lastUsedTime > 0 || SORT_RECENT_UNUSED) { 
				//only if a date is set				
				mFilteredLetteringByRecent.add(spanHolder); 
			}
		}
	}
	
	/** Returns the correct span size, depending on the sort. */
	private SpannableString getSpanItem(int pos) {
		return getList().get(pos).name;
	}
	
	/** Returns the appropriate size, and sets isEmpty*/
	private int getSize() {
		return getList().size();		
	}
	
	/** Returns list based on sort mode. */
	private List<LetteringSpanHolder> getList() {
		/*
		 * We use two lists so that we can exclude unused items from the recent list. 
		 */
		if (SORT_NAME_ASC == mSortMode) {
			return mFilteredLetteringByName;
		} 
		return mFilteredLetteringByRecent;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The sorter that sorts the content according to the current mode. */
	private Comparator<LetteringSpanHolder> mSortByRecentComparator = new Comparator<LetteringSpanHolder>() {

		/*
		 * Remember: 
		 * 
		 * https://developer.android.com/reference/java/util/Comparator.html#compare%28T,%20T%29
		 * 
		 *  - compare(a,a) returns zero for all a
		 *  - the sign of compare(a,b) must be the opposite of the sign of compare(b,a) for all pairs of (a,b)
		 *  - From compare(a,b) > 0 and compare(b,c) > 0 it must follow compare(a,c) > 0 for all possible combinations of (a,b,c)
		 *  
		 *  
		 *  an integer < 0 if leftHandSide is less than rightHandSide, 
		 *  0 if they are equal, 
		 *  and > 0 if leftHandSide is greater than rightHandSide.
		 *  
		 *  
		 *  (non-Javadoc)
		 */
		@Override
		//compare(LetteringSpanHolder lhs, LetteringSpanHolder rhs)
		public int compare(LetteringSpanHolder leftHS, LetteringSpanHolder rightHS) {
			/* Remember: recent ASCENDING is the same as time DESCENDING.
			 * So flip results: 
			 * 	> 0 if leftHandSide is less than rightHandSide
			 *  < 0 if leftHandSide is greater than rightHandSide. 
			 */
			if (leftHS.lastUsedTime < rightHS.lastUsedTime) {
				return 1;
			} else if (leftHS.lastUsedTime > rightHS.lastUsedTime) {
				return -1;
			}
			//if not sorted, fall through to name sort
			return leftHS.name.toString().compareToIgnoreCase(rightHS.name.toString());
		}		
	};
	
	private Callback<LetteringListResponse> mLetteringCallback = new Callback<LetteringListResponse>() {

		@Override
		public void success(LetteringListResponse response, Response arg1) {
			Log.d(LOGTAG, "Successful lettering");
			isLoading = false;
			if (response != null) {				
				if (mUseLibrary) {
					AppCache.setLibraryLetteringListResponse(response);
				} else {
					AppCache.setLetteringListResponse(response); //faster fetches
				}

				mLetteringListResponse = response;
				responseToMarkingList(response);
				
			} else {
				cannotConnect = true;
				notifyDataSetChangedWrapper();
			}
		}
		
		@Override
		public void failure(RetrofitError arg0) {
			Log.d(LOGTAG, "Failed lettering");
			cannotConnect = true;
			isLoading = false;
			notifyDataSetChangedWrapper();
		}
	};
		
	@Override
	public void onCacheUpdate(String key, LetteringListResponse value) {
		if (value == null && !mUseLibrary) {
			final boolean removed = AppCache.removeLetteringListUpdateListener(this);
			AppCache.setLetteringListResponse(null);
			if (removed) {
				AppCache.addLetteringListUpdateListener(this);
			}
			fetchLetterings();
		}
	} 
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Simple holder to maintain the span state with name & date 
	 * @version 0.1.0-20150811 */
	private static class LetteringSpanHolder {
		/** Date last used (milliseconds since epoch) or 0 if unused. */
		final long lastUsedTime;
		/** Both the name and the spannable string. */
		final SpannableString name;
		/** The index in the original response. */
		final int indexOfLetteringItem; 
		
		public LetteringSpanHolder(LetteringItem lettering, int index) {			
			name = Utility.buildStyleFromLettering(lettering)
					.setCentered(true)
					.apply(lettering.getName(), 0, lettering.getName().length());
			lastUsedTime = lettering.getLastUsedTime();
			indexOfLetteringItem = index;
		}
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		public final TextView tvContent;
		public final View rootView;
		
		public final int defaultContentWidth;
		
		public ViewHolder(View v) {
			super(v);
			rootView = v;
			tvContent = (TextView) v.findViewById(R.id.grid_item_text);
			defaultContentWidth = tvContent.getLayoutParams().width;			
		}
	}
	
	/** @version 0.1.0-20150723 */
	public static interface OnAdapterStateListener {
		public static final int STATE_OK = 0;
		public static final int STATE_EMPTY = 1;
		public static final int STATE_CANNOT_CONNECT = 2;
		
		/** The state of the adapter. */
		public void onStateUpdate(int state);
	}
	
	/** @version 0.1.0-20150714 */
	public static interface OnLetteringItemClickListener {
		public void onLetteringItemClick(LetteringItem item);
	}
	
	/** @version 0.1.0-20150715 */
	protected static interface OnNotifyDataSetChangedListener {
		public void onNotifyDataSetChanged();
	}


}
