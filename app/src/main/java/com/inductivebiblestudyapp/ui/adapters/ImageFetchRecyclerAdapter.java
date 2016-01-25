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
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.AppCache.OnCacheUpdateListener;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.data.model.ImageListResponse;
import com.inductivebiblestudyapp.data.service.ImageService;
import com.inductivebiblestudyapp.util.BitmapRecycler;
import com.inductivebiblestudyapp.util.RecyclableImageViewAware;
import com.inductivebiblestudyapp.util.Utility;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

/**
 * Fetches the image item list from the server. After, it loads image items as necessary.
 * Use {@link #getScrollListener()} to avoid stuttering when scrolling & loading images.
 * 
 * Call #clear() when finished to recycle remaining bitmaps & clear adapter. 
 * 
 * @author Jason Jenkins
 * @version 0.12.0-20150831
 */
public class ImageFetchRecyclerAdapter extends Adapter<ImageFetchRecyclerAdapter.ViewHolder> 
	implements OnCacheUpdateListener<ImageListResponse> {
	
	final static private String LOGTAG = ImageFetchRecyclerAdapter.class
			.getSimpleName();
	
	/** Whether or not to include unused items for {@link #SORT_RECENT_ASC}.  */ 
	private static final boolean SORT_RECENT_UNUSED = true;

	public static final int SORT_NAME_ASC = 0;
	public static final int SORT_RECENT_ASC = 1;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The maxmimum size for this adapter to display. If 0, ignored. */
	private final int mDisplayLimit;
	
	private final ImageLoader mImageLoader;
	
	private final Activity mActivity;
	
	private final boolean mUseLibrary; 
		
	/* Remember: double references do not equal double memory or objects. */
	/** The original items. */
	private final List<ImageItem> mImageItems = new ArrayList<ImageItem>();
	
	/** The filtered name items. */
	private final List<ImageItem> mFilteredItemsByName = new ArrayList<ImageItem>();
	/** The filtered items limited by recent. */
	private final List<ImageItem> mFilteredItemsByRecent = new ArrayList<ImageItem>();
	
	private final BitmapRecycler mBitmapRecycler = new BitmapRecycler();
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End finals
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private boolean isLoading = true;
	private boolean noMatchingResults = false;
	private boolean isEmpty = false;
	private boolean cannotConnect = false;
	
	/** Default visibility for efficiency. For internal use only. */
	/*default*/ OnImageItemClickListener mOnItemClickListener = null;
	
	/** Can be <code>null</code>. */
	private OnAdapterStateListener mOnAdapterStateListener = null;
	/** Can be <code>null</code>. */
	private OnNotifyDataSetChangedListener mOnNotifyDataSetChangedListener = null;
	
	private int mSortMode = SORT_NAME_ASC;
	
	/** The current filter string. Set in {@link #filter(String)} */
	private String mFilterString = "";
	
	/**
	 * @param activity
	 * @param size The maxmium number of items to display. If 0, it is ignored.
	 * @param library <code>true</code> if to use library fetch, <code>false</code>
	 * to use user fetch (not yet implemented)
	 */
	public ImageFetchRecyclerAdapter(Activity activity, int size, boolean library) {
		this.mDisplayLimit = size;		
		this.mImageLoader = ImageLoader.getInstance();		
		this.mActivity = activity;
		
		this.mUseLibrary = library;
		
		final String accessToken = new CurrentUser(mActivity).getIBSAccessToken();
		fetchImageItems(accessToken);
	}
	
	/** Same as calling #ImageFetchGridAdapter(Activity, int, boolean) with false */
	public ImageFetchRecyclerAdapter(Activity activity, int size) {
		this(activity, size, false);
	}
	
	
	/** Same as calling #ImageFetchGridAdapter(Activity, int, boolean) with 0 and false */
	public ImageFetchRecyclerAdapter(Activity activity) {
		this(activity, 0, false);
	}
	
	/** Same as calling #ImageFetchGridAdapter(Activity, int) with 0. */
	public ImageFetchRecyclerAdapter(Activity activity, boolean library) {
		this(activity, 0, library);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// public methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Remove all elements from the list & recycles all bitmaps that have been displayed.
	 */
	public void clear() {
		mBitmapRecycler.recycleAll();
		mImageItems.clear();
		mFilteredItemsByName.clear();
		mFilteredItemsByRecent.clear();
		notifyDataSetChangedWrapper();
	}
	
	/** Sets the filter and causes the adapter to update results based on naming & sorting. 
	 * @param The string to filter on, case insensitive. */
	public void filter(String filterString) {
		if (filterString == null || mFilterString.equals(filterString.trim().toLowerCase(Locale.US))) {
			return; //do nothing
		}
		mFilterString = filterString.trim().toLowerCase(Locale.US);
		
		mFilteredItemsByName.clear();		
		mFilteredItemsByRecent.clear();
		
		for (ImageItem item : mImageItems) {			
			addImageItemToFilter(item);
		}		
		Collections.sort(mFilteredItemsByRecent, mSortByRecentComparator);
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
	
	/** Invalidates the cache and rorces the adapter to reload the image list from the server. */
	public void forceReload() {
		final String accessToken = new CurrentUser(mActivity).getIBSAccessToken();

		final boolean removed = AppCache.removeImageListUpdateListener(this);		
		AppCache.setImageListResponse(null); //prevent loops
		if (removed) {
			AppCache.addImageListUpdateListener(this);
		}
		fetchImageItems(accessToken);
	}

	/** @return The listener to avoid stuttering when loading the actual images. */
	public PauseOnScrollListener getScrollListener() {
		//pause the loader on both scroll and fling.
		return new PauseOnScrollListener(mImageLoader, true, true);
	}
	
	
	/** @return Returns the image item at the given position. */
	public ImageItem getItem(int pos) {
		if (isLoading || noMatchingResults || isEmpty || cannotConnect) {
			return null;
		}
		
		if (pos > mDisplayLimit && mDisplayLimit > 0) {
			pos = mDisplayLimit;
		}

		return getList().get(pos);
	}
	
	/** @param listener The image item click listener. */
	public void setOnImageItemClickListener(OnImageItemClickListener listener) {
		this.mOnItemClickListener = listener;
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
	//// Override methods
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
	public void onBindViewHolder(ViewHolder holder, int position) {
		if (isLoading) {
			setHolderMessage(holder, R.string.ibs_text_loading);
			return;
		} else if (cannotConnect) {
			setHolderMessage(holder, R.string.ibs_error_cannotConnect);
			return;
		} else if (isEmpty) {
			int stringRes = 0; 
			if (mUseLibrary) {
				stringRes = R.string.ibs_text_noImages_library;
			} else if (SORT_RECENT_ASC == mSortMode) {
				stringRes = R.string.ibs_text_noImages_user_recent;
			} else {
				stringRes = R.string.ibs_text_noImages_user;
			}
			setHolderMessage(holder, stringRes);
			return;
		} else if (noMatchingResults) {
			setHolderMessage(holder, R.string.ibs_text_noImageResults);
			return;
		} else {
			holder.frame.setVisibility(View.VISIBLE);
			
			holder.rootView.getLayoutParams().height = holder.defaultRootHeight;
			holder.rootView.getLayoutParams().width = holder.defaultRootWidth;
			holder.descrip.getLayoutParams().width = holder.defaultDescripWidth;
		}
		 
		//mBitmapRecycler.recycle(holder.img); //this may be premature
		
		final ImageItem item = getItem(position);
		
		holder.descrip.setText(getItem(position).getName());
		holder.img.setContentDescription(item.getName());
		
		ImageAware imageAware = new RecyclableImageViewAware(holder.img, false, mBitmapRecycler);
		
		mImageLoader.displayImage(item.getUrl(), imageAware, createImageLoadingListener(holder));
		
		holder.rootView.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				if (mOnItemClickListener != null) {
					mOnItemClickListener.onImageItemClick(item);
				}					
			}
		});
	}

	
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	    View v = LayoutInflater.from(parent.getContext())
	    			.inflate(R.layout.grid_item_image_fetch, parent, false);
	    return new ViewHolder(v);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Hides the image view and sets the text for display */
	private void setHolderMessage(ViewHolder holder, int stringId) {
		holder.frame.setVisibility(View.GONE);
		holder.descrip.setText(stringId);
		
		holder.rootView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
		holder.rootView.getLayoutParams().width =
		holder.descrip.getLayoutParams().width = LayoutParams.MATCH_PARENT;
	}

	
	/** Used to call {@link #notifyDataSetChanged()} & update listener. */
	private void notifyDataSetChangedWrapper(){
		if (getSize() == 0 && !isLoading) {
			isEmpty = mImageItems.isEmpty(); //only if the actual items are empty, not the filter
			noMatchingResults = !mFilterString.isEmpty(); //only if we are filtering
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
	
	/** Creates a new image loading listener for the given holder. */
	private ImageLoadingListener createImageLoadingListener(final ViewHolder holder) {
		return new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				holder.progressBar.setVisibility(View.VISIBLE);
				holder.img.setVisibility(View.GONE);
			}
			
			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {}
			
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				Utility.switchFadeProgressViews(holder.progressBar, holder.img, false);
			}
			
			@Override
			public void onLoadingCancelled(String imageUri, View view) {}
		};
	}
	
	/** Fetches the images from cache or, if not found, from the server 
	 * and stores them in the cache
	 * @param accessToken
	 */
	private void fetchImageItems(final String accessToken) {
		ImageListResponse response = 
				mUseLibrary ? AppCache.getLibraryImageListResponse() : AppCache.getImageListResponse();
		if (response != null) {
			processImageResponse(response);
			return;
		}
		
		ImageService service = RestClient.getInstance().getImageService();
		if (mUseLibrary) {
			service.listLibrary(accessToken, mImageCallback);
		} else {
			service.list(accessToken, mImageCallback);
		}
	}
	
	/** Set #isLoading to <code>false</code>.
	 * Then steps through counting & sets images before finally sorting them */
	private void processImageResponse(ImageListResponse response) {
		ImageItem[] items = response.getImages();
		isLoading = false;
		
		mImageItems.clear();
		mFilteredItemsByName.clear();		
		mFilteredItemsByRecent.clear();
		//as we just replaced all images, the previous images are not used
		mBitmapRecycler.recycleAll(); 
		
		for (ImageItem item : items) {
			mImageItems.add(item);			
			addImageItemToFilter(item);
		}
		
		Collections.sort(mFilteredItemsByRecent, mSortByRecentComparator);	

		notifyDataSetChangedWrapper();
	}

	/** Tests the given image item & {@link #mFilterString} to see if the item should be 
	 * added to the filter lists.
	 * @param item
	 */
	private void addImageItemToFilter(ImageItem item) {
		final boolean matchesFilter = mFilterString.isEmpty() || 
				item.getName().toLowerCase(Locale.US).startsWith(mFilterString);
		if (matchesFilter) {
			mFilteredItemsByName.add(item);
			
			if (item.getLastUsedTime() > 0 || SORT_RECENT_UNUSED) { 
				//only if a date is set				
				mFilteredItemsByRecent.add(item); 
			}
		}
	}

	/** Returns the appropriate size, and sets isEmpty*/
	private int getSize() {
		return getList().size();
	}
	
	/** Returns list based on sort mode. */
	private List<ImageItem> getList() {
		/*
		 * We use two lists so that we can exclude unused items from the recent list. 
		 */
		if (SORT_NAME_ASC == mSortMode) {
			return mFilteredItemsByName;
		} 
		return mFilteredItemsByRecent;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The sorter that sorts the content according to the current mode. */
	private Comparator<ImageItem> mSortByRecentComparator = new Comparator<ImageItem>() {

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
		//compare(StudyNoteItem lhs, StudyNoteItem rhs)
		public int compare(ImageItem leftHS, ImageItem rightHS) {
			/* Remember: recent ASCENDING is the same as time DESCENDING.
			 * So flip results: 
			 * 	> 0 if leftHandSide is less than rightHandSide
			 *  < 0 if leftHandSide is greater than rightHandSide. 
			 */
			if (leftHS.getLastUsedTime() < rightHS.getLastUsedTime()) {
				return 1;
			} else if (leftHS.getLastUsedTime() > rightHS.getLastUsedTime()) {
				return -1;
			}
			//if not sorted, fall through to name sort
			return leftHS.getName().compareToIgnoreCase(rightHS.getName());
		}		
	};
	
	private Callback<ImageListResponse> mImageCallback = new Callback<ImageListResponse>() {

		@Override
		public void success(ImageListResponse response, Response arg1) {
			Log.d(ImageFetchGridAdapter.class.getSimpleName(), "Response: " + response);
			if (response != null) {
				if (mUseLibrary) {
					AppCache.setLibraryImageListResponse(response);
				} else {
					AppCache.setImageListResponse(response); //faster fetches
				}
				
				processImageResponse(response);
				
			} else {
				cannotConnect = true;
				isLoading = false;
				notifyDataSetChangedWrapper();
			}
		}
		
		@Override
		public void failure(RetrofitError arg0) {
			cannotConnect = true;
			isLoading = false;
			notifyDataSetChangedWrapper();		
		}
	};
	
	@Override
	public void onCacheUpdate(String key, ImageListResponse value) {
		if (value == null && !mUseLibrary) {
			forceReload();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes & interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/** @version 0.2.0-20150720 */
	public static class ViewHolder extends RecyclerView.ViewHolder {
		final ProgressBar progressBar;
		final ImageView img;
		final TextView descrip;
		
		final View frame;
		
		final View rootView;
		

		public final int defaultDescripWidth;

		public final int defaultRootWidth;
		public final int defaultRootHeight;
		
		public ViewHolder(View rootView) {
			super(rootView);
			this.rootView = rootView;
			
			this.img = (ImageView) rootView.findViewById(R.id.grid_item_img);
			this.descrip = (TextView) rootView.findViewById(R.id.grid_item_img_title);
			this.progressBar = (ProgressBar) rootView.findViewById(R.id.grid_item_progress);
			
			this.frame = rootView.findViewById(R.id.grid_item_frame_container);
			
			this.defaultRootWidth = rootView.getLayoutParams().width;
			this.defaultRootHeight = rootView.getLayoutParams().height;
			this.defaultDescripWidth = descrip.getLayoutParams().width;
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
	
	/** @version 0.1.0-20150713 */
	public static interface OnImageItemClickListener {
		public void onImageItemClick(ImageItem item);
	}
	
	/** @version 0.2.0-20150713 */
	protected static interface OnNotifyDataSetChangedListener {
		public void onNotifyDataSetChanged();
	}


}
