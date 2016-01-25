package com.inductivebiblestudyapp.ui.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.ui.adapters.ImageFetchRecyclerAdapter.ViewHolder;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

/**
 * A wrapper for {@link ImageFetchRecyclerAdapter}.
 * 
 * @author Jason Jenkins
 * @version 0.2.0-20150713
 */
public class ImageFetchGridAdapter extends ArrayAdapter<ImageItem> {

	private final ImageFetchRecyclerAdapter mAdapter;
	
	/**
	 * @param activity
	 * @param size The maxmium number of items to display. If 0, it is ignored.
	 */
	public ImageFetchGridAdapter(Activity activity, ImageFetchRecyclerAdapter adapter) {
		super(activity, 0);
		this.mAdapter = adapter;
		this.mAdapter.setOnNotifyDataSetChangedListener(
				new ImageFetchRecyclerAdapter.OnNotifyDataSetChangedListener() {			
			@Override
			public void onNotifyDataSetChanged() {
				notifyDataSetChanged();
			}
		});
	}

	
	/** @return The listener to avoid stuttering when loading the actual images. */
	public PauseOnScrollListener getScrollListener() {
		//pause the loader on both scroll and fling.
		return mAdapter.getScrollListener();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public int getCount() {
		return mAdapter.getItemCount();
	}

	@Override
	public ImageItem getItem(int pos) {
		return mAdapter.getItem(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = mAdapter.createViewHolder(parent, 0);
			convertView = holder.rootView;
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		mAdapter.bindViewHolder(holder, position);
		
		return holder.rootView;
	}

}
