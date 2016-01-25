package com.inductivebiblestudyapp.ui.adapters;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.bible.BibleSearchResponse;
import com.inductivebiblestudyapp.data.model.bible.IBibleSearchItem;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse.Book;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse.Verse;
/**
 * Adapter for bible search results. Will show "Loading..." until either 
 * {@link #setError(String)} or {@link #setResults(BibleSearchResponse)} is called. 
 * This can be restored via {@link #clear()}.
 * @author Jason Jenkins
 * @version 0.2.3-20150824
 */
public class BibleSearchResultsRecyclerAdapter extends Adapter<BibleSearchResultsRecyclerAdapter.ViewHolder> {
	
	private final List<IBibleSearchItem> mResults = new ArrayList<IBibleSearchItem>();
	private final OnBibleSearchItemClickListener mOnSearchItemClickListener;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End finals
	////////////////////////////////////////////////////////////////////////////////////////////////
		
	private boolean mIsLoading = true;
	private boolean mIsEmpty = true;
	
	private String mErrorMessage = "";
	
	/**
	 * @param listener
	 */
	public BibleSearchResultsRecyclerAdapter(OnBibleSearchItemClickListener listener) {
		mOnSearchItemClickListener = listener;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End intialization
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** Clears all results & errors and resets the view to both "loading" and "empty" */
	public void clear() {
		mResults.clear();
		mIsLoading = true;
		mIsEmpty = mResults.isEmpty();
		mErrorMessage = "";
		notifyDataSetChanged();
	}
	
	/** Sets the results for the adapter. 
	 * @param response The response to get results from. Pass <code>null</code>
	 * to set results as empty. */
	public void setResults(BibleSearchResponse response) {
		mResults.clear();
		
		if (response != null) { 
			mResults.addAll(response.getBooks());
			mResults.addAll(response.getChapters());
			mResults.addAll(response.getVerses());
		}
		
		mIsLoading = false;
		mIsEmpty = mResults.isEmpty();
		mErrorMessage = "";
		notifyDataSetChanged();
	}
	
	/** Sets the error state for the adapter. */
	public void setError(String error) {
		this.mErrorMessage = error;
		mIsLoading = false;
		notifyDataSetChanged();
	}
	
	public boolean isEmpty() {
		return mIsEmpty;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public int getItemCount() {
		if (mIsLoading || mIsEmpty || hasError()) {
			return 1; //the message item
		}
		
		//display all
		return mResults.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		if (mIsLoading) {
			holder.text.setText(R.string.ibs_text_loading);
			holder.rootView.setOnClickListener(null);
			return;
		} else if (hasError()) {
			holder.text.setText(mErrorMessage);
			holder.rootView.setOnClickListener(null);
			return;
		} else if (mIsEmpty) {
			holder.text.setText(R.string.ibs_text_noBibleResults);
			holder.rootView.setOnClickListener(null);
			return;
		}
		
		final IBibleSearchItem item = mResults.get(position);
		
		holder.text.setText(item.getSearchResultName());
		
		holder.rootView.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				if (mOnSearchItemClickListener != null) {
					mOnSearchItemClickListener.onBibleSearchItemClick(item);
				}					
			}
		});
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	    View v = LayoutInflater.from(parent.getContext())
	    			.inflate(R.layout.grid_item_bible_search_text, parent, false);
	    
	    return new ViewHolder(v);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private boolean hasError() {
		return !TextUtils.isEmpty(mErrorMessage);
	}
		
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.1.0-20150722 */
	public static class ViewHolder extends RecyclerView.ViewHolder {
		public final TextView text;
		public final View rootView;
		
		public ViewHolder(View rootView) {
			super(rootView);
			this.rootView = rootView;
			this.text = (TextView) rootView;
		}
	}
	
	/** @version 0.1.0-20150807 */
	public static interface OnBibleSearchItemClickListener {
		/**
		 * @param item Expected types are {@link Book}, {@link Chapter}, {@link Verse}.
		 */
		public void onBibleSearchItemClick(IBibleSearchItem item);
	}
	

}
