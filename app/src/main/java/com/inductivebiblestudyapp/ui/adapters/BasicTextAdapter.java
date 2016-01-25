package com.inductivebiblestudyapp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;

public class BasicTextAdapter extends ArrayAdapter<String> {

	private final String[] data;
	
	private LayoutInflater inflater = null;
	private final int SIZE;
	private final int layoutId;
	
	
	public BasicTextAdapter(Context context, String[] data, int size) {
		super(context, R.layout.list_item_text);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutId = R.layout.list_item_text;
		this.data = data;
		this.SIZE = size;			
	}
	
	public BasicTextAdapter(Context context, int resource, String[] data) {
		super(context, resource);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutId = resource;
		this.data = data;
		this.SIZE = data.length;
	}
	
	@Override
	public int getCount() {
		return SIZE;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Holder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(layoutId, parent, false);
			holder = new Holder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		holder.text.setText(data[position % data.length]);
		
		return convertView;
	}
	
	public static class Holder {
		public final TextView text;
		public Holder(View rootView) {
			this.text = (TextView) rootView.findViewById(R.id.grid_item_text);
		}
	}
	

}
