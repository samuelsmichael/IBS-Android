package com.inductivebiblestudyapp.ui.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.ui.adapters.LetteringFetchRecyclerAdapter.ViewHolder;

/**
 * A wrapper for {@link LetteringFetchRecyclerAdapter}.
 * 
 * @author Jason Jenkins
 * @version 0.1.0-20150715
 */
public class LetteringFetchGridAdapter extends ArrayAdapter<LetteringItem> {

	private final LetteringFetchRecyclerAdapter mAdapter;
	
	/**
	 * @param activity
	 * @param size The maxmium number of items to display. If 0, it is ignored.
	 */
	public LetteringFetchGridAdapter(Activity activity, LetteringFetchRecyclerAdapter adapter) {
		super(activity, 0);
		this.mAdapter = adapter;
		this.mAdapter.setOnNotifyDataSetChangedListener(
				new LetteringFetchRecyclerAdapter.OnNotifyDataSetChangedListener() {			
			@Override
			public void onNotifyDataSetChanged() {
				notifyDataSetChanged();
			}
		});
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public int getCount() {
		return mAdapter.getItemCount();
	}

	@Override
	public LetteringItem getItem(int pos) {
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
