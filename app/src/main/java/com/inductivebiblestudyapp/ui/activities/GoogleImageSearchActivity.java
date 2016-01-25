package com.inductivebiblestudyapp.ui.activities;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.googleimages.GoogleImageSearchLoader;
import com.inductivebiblestudyapp.data.model.googleimages.GoogleImageResponse;
import com.inductivebiblestudyapp.data.model.googleimages.GoogleImageResponse.GoogleImageResult;
import com.inductivebiblestudyapp.data.model.googleimages.GoogleImageResponse.Page;
import com.inductivebiblestudyapp.ui.activities.GoogleImageSearchActivity.GoogleImageFetchGridAdapter.OnEvent;
import com.inductivebiblestudyapp.ui.adapters.ImageFetchGridAdapter;
import com.inductivebiblestudyapp.util.BitmapRecycler;
import com.inductivebiblestudyapp.util.RecyclableImageViewAware;
import com.inductivebiblestudyapp.util.Utility;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

/**
 * An activity to search Google images and return a selected image.
 * Returns a {@link GoogleImageResult} via {@link Activity#RESULT_OK} on click.
 *
 * @author Jason Jenkins
 * @version 0.4.2-20150831
 */
public class GoogleImageSearchActivity extends FragmentActivity implements OnItemClickListener {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = GoogleImageSearchActivity.class
			.getSimpleName();
	
	/** Extra. String: Used to reference search term. Required. */
	public static final String EXTRA_SEARCH_TERM = CLASS_NAME + ".EXTRA_SEARCH_TERM";
	
	/** Extra. Parcelable/{@link GoogleImageResult}: The result selected by the user.. */
	public static final String EXTRA_IMAGE_RESULT = CLASS_NAME + ".EXTRA_IMAGE_RESULT";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private GoogleImageFetchGridAdapter mAdapter = null;
	
	private View mProgressBar = null;
	private View mErrorMessage = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_image_search);
		
		String searchTerm = getIntent().getStringExtra(EXTRA_SEARCH_TERM);
		if (searchTerm == null) {
			throw new IllegalArgumentException("Must supply a search term.");
		}
		
		mProgressBar = findViewById(R.id.ibs_googleimagesearch_progress);
		mErrorMessage = findViewById(R.id.ibs_googleimagesearch_text_cannotConnect);
		
		((TextView) findViewById(R.id.ibs_googleimagesearch_search_topic))
			.setText(getString(R.string.ibs_label_googleImageSearch_resultsFor, searchTerm));
		
		mAdapter = new GoogleImageFetchGridAdapter(this, searchTerm, mEventListener);
		GridView results = (GridView) findViewById(R.id.ibs_googleimagesearch_searchResults_gridview);
		results.setAdapter(mAdapter);
		results.setOnScrollListener(mAdapter);
		results.setOnItemClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAdapter.clear();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Fetches the image item list from the Google Image service. After, it loads image items as necessary.
	 * Set this adapter as the {@link OnScrollListener} to ensure smooth loading and scrolling.
	 * Call {@link #clear()} at onDestroy() to clear bitmaps.
	 * @author Jason Jenkins
	 * @version 0.3.3-20150831
	 */
	public static class GoogleImageFetchGridAdapter extends ArrayAdapter<GoogleImageResult> 
		implements OnScrollListener, LoaderCallbacks<GoogleImageResponse> {
		/** Max limit for loading pages. This is to keep the requests on the custom search API low
		 * and the fact the basic ajax api has a max of 8. */
		private static final int LOADING_PAGE_LIMIT = 8;
		
		/** minimum amount items to have below current scroll position before trying to load more. */
		private static final int LOADING_THRESHOLD = 6;
		
		/** The starting number for the loaders + start are the id's to use. */
		private static final int LOADER_ID_STUB = 0x200;
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// End constants
		////////////////////////////////////////////////////////////////////////////////////////////////

		private LayoutInflater inflater = null;
			
		private final ImageLoader mImageLoader;
		
		private final PauseOnScrollListener mPauseOnScrollListener;
		
		private final String mSearchTerm;
		
		private final ArrayList<GoogleImageResult> mItems = new ArrayList<GoogleImageResult>();
		
		private final OnEvent mListener;
		
		private final LoaderManager mLoaderManager;
		
		private final BitmapRecycler mBitmapRecycler = new BitmapRecycler();
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// End final members
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		private GoogleImageResponse mCurrentSearchResponse = null;
				
		/** Whether the method {@link #performImageSearch(int)} is running or not. */
		private boolean mIsSearching = false;
		/** Whether no more pages can be loaded. */
		private boolean mLoadingComplete = false; 
		
		/** The current start. */
		private int mStart = 0;
		
		/**
		 * @param activity
		 * @param size The maximum number of items to display. If 0, it is ignored.
		 */
		public GoogleImageFetchGridAdapter(FragmentActivity activity, String searchTerm, OnEvent listener) {
			super(activity, 0);
			inflater = (LayoutInflater) activity.getLayoutInflater();
			mImageLoader = ImageLoader.getInstance();
			mLoaderManager = activity.getSupportLoaderManager();
			
			mSearchTerm = searchTerm;
			
			mListener = listener;
			
			mPauseOnScrollListener = new PauseOnScrollListener(mImageLoader, true, true);
					
			performImageSearch(0);
		}
		/**
		 * Remove all elements from the list & recycles all bitmaps that have been displayed.
		 */
		@Override
		public void clear() {
			super.clear();
			mBitmapRecycler.recycleAll();
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Override methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public GoogleImageResult getItem(int pos) {
			return mItems.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.grid_item_google_image, parent, false);
				holder = new Holder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			
			final GoogleImageResult image = getItem(position);
			
			//mBitmapRecycler.recycle(holder.img); //this may be premature
			holder.img.setContentDescription(image.getTitleNoFormatting());
			
			ImageAware imageAware = new RecyclableImageViewAware(holder.img, false, mBitmapRecycler);
			
			mImageLoader.displayImage(image.getThumbnailUrl(), imageAware, createImageLoadingListener(holder));
			
			if (position >= getCount() - LOADING_THRESHOLD && !mLoadingComplete) {
				//if we can see everything and can load more. load more.
				loadNextPageOfResults();
			}
			
			return convertView;
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Helper methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		/** Creates a new image loading listener for the given holder. */
		private ImageLoadingListener createImageLoadingListener(final Holder holder) {
			return new ImageLoadingListener() {
				
				boolean firstLoad = false;
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					firstLoad = true;
					holder.progressBar.setVisibility(View.VISIBLE);
					holder.img.setVisibility(View.GONE);
				}
				
				@Override
				public void onLoadingFailed(String imageUri, View view,
						FailReason failReason) {
					mListener.onError();
				}
				
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					if (firstLoad) {
						Utility.switchFadeProgressViews(holder.progressBar, holder.img, false);
					} else {
						holder.img.setVisibility(View.VISIBLE);
					}
				}
				
				@Override
				public void onLoadingCancelled(String imageUri, View view) {}
			};
		}
		
		/** Fetches the images from cache or, if not found, from google search 
		 * and stores paths in the cache
		 */
		private void performImageSearch(int start) {
			mIsSearching = true;
			mStart = start;
			final String cacheKey = mSearchTerm + start;
			
			
			mCurrentSearchResponse = AppCache.getGoogleImageSearchResponse(cacheKey);
			if (mCurrentSearchResponse != null) {
				mItems.addAll(Arrays.asList(mCurrentSearchResponse.getResults()));
				notifyDataSetChanged();
				mIsSearching = false;
				return;
			}			
			
			mLoaderManager.initLoader(LOADER_ID_STUB + start, null, this);
		
		}
		

		/** Loads the next page of results. */
		/*default*/ void loadNextPageOfResults() {
			if (mCurrentSearchResponse == null || mIsSearching) {
				return;
			}
			Page nextPage = mCurrentSearchResponse.getNextPage();
			
			//remember: the label is 1-indexed (starting at 1)
			if (nextPage != null && nextPage.getLabelAsInt() + 1 <= LOADING_PAGE_LIMIT) {
				int start = nextPage.getStartAsInt(); //get next start
				performImageSearch(start);
				mLoadingComplete = false;
			} else {
				mLoadingComplete = true;
			}
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Listeners
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			mPauseOnScrollListener.onScrollStateChanged(view, scrollState);
		}


		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			mPauseOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			
			if (mLoadingComplete) {
				return; //we can do no more if the loading is complete
			}
			
	        //if not searching & beyond the threshold, load more.
			if (!mIsSearching && (totalItemCount - visibleItemCount) <= (firstVisibleItem + LOADING_THRESHOLD)) {
				loadNextPageOfResults();
			}
		}

		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Loader callbacks
		////////////////////////////////////////////////////////////////////////////////////////////////

		@Override
		public Loader<GoogleImageResponse> onCreateLoader(int arg0, Bundle arg1) {
			mListener.onLoadingStart();
			return new GoogleImageSearchLoader(getContext(), mSearchTerm, mStart);
		}


		@Override
		public void onLoadFinished(Loader<GoogleImageResponse> loader,
				GoogleImageResponse response) {
			
			final int id = loader.getId();
			mLoaderManager.destroyLoader(id); //makes it easier to track
			
			
			Log.d(ImageFetchGridAdapter.class.getSimpleName(), "Response: " + response);
			if (response != null) {

				final String cacheKey = mSearchTerm + response.getStart();
				AppCache.addGoogleImageSearchResult(cacheKey, response);
				
				mCurrentSearchResponse = response;
				mItems.addAll(Arrays.asList(mCurrentSearchResponse.getResults()));	
				
				notifyDataSetChanged();
				mIsSearching = false;
				mListener.onLoadingComplete();
			} else {
				mListener.onError();
			}
		}


		@Override
		public void onLoaderReset(Loader<GoogleImageResponse> arg0) {}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Internal classes
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		private static class Holder {
			final ProgressBar progressBar;
			final ImageView img;
			public Holder(View rootView) {
				this.img = (ImageView) rootView.findViewById(R.id.grid_item_google_img);
				this.progressBar = (ProgressBar) rootView.findViewById(R.id.grid_item_google_progress);
			}
		}
		
		/** @version 0.1.0-20150714 */
		public static interface OnEvent {
			public void onError();
			public void onLoadingStart();
			public void onLoadingComplete();
		}

	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private GoogleImageFetchGridAdapter.OnEvent mEventListener = new OnEvent() {
		
		@Override
		public void onLoadingStart() {
			mProgressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		public void onLoadingComplete() {
			mProgressBar.setVisibility(View.GONE);
			mErrorMessage.setVisibility(View.GONE);
		}
		
		@Override
		public void onError() {
			mProgressBar.setVisibility(View.GONE);
			mErrorMessage.setVisibility(View.VISIBLE);
		}
	};
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		GoogleImageResult result = mAdapter.getItem(position);
		Intent data = new Intent();
		data.putExtra(EXTRA_IMAGE_RESULT, result);
		setResult(RESULT_OK, data);
		finish();
	}

	
}
