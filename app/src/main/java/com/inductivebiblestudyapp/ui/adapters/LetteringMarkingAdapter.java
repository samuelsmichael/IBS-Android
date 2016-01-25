package com.inductivebiblestudyapp.ui.adapters;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
/**
 * Adapter for a horizontal recycler view.
 * @author Jason Jenkins
 * @version 0.1.0-20150616
 */
public class LetteringMarkingAdapter extends Adapter<LetteringMarkingAdapter.ViewHolder> {

	
	private final List<SpannableString> mDataset;
	
	public LetteringMarkingAdapter(List<SpannableString> markings) {
		mDataset = markings;
	}
	  
	public void add(int position, SpannableString item) {
		mDataset.add(position, item);
	    notifyItemInserted(position);
	}
	  
	
	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		final SpannableString marking = mDataset.get(position);
		holder.tvContent.setText(marking);
		
		
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	    View v = LayoutInflater.from(parent.getContext())
	    			.inflate(R.layout.grid_item_lettering, parent, false);
	    
	    return new ViewHolder(v);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		public final TextView tvContent;
		public ViewHolder(View v) {
			super(v);
			tvContent = (TextView) v.findViewById(R.id.grid_item_text);
		}
	}

}
