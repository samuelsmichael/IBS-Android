package com.inductivebiblestudyapp.ui.adapters;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse.Book;

/**
 * Manages the bible book list.
 * @author Jason Jenkins
 * @version 0.4.0-20150831
 */
public class BibleBookListAdapter extends ArrayAdapter<Book> {

	private Book[] mBooks = new Book[0];
	private int SIZE = 0;
	
	private final LayoutInflater mInflater;
	private final int mLayoutId;
	
	private String mDisplayMessage = "";
	
	public BibleBookListAdapter(Context context) {
		super(context, R.layout.grid_item_bible_text);
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mLayoutId =  R.layout.grid_item_bible_text;			
	}
	
	public void setMessage(String message) {
		this.mDisplayMessage = message;
		this.SIZE = 1;
		notifyDataSetChanged();
	}
	
	public void updateBookList(List<Book> books) {
		this.mDisplayMessage = "";
		this.SIZE = books.size();
		this.mBooks = new Book[SIZE];
		books.toArray(this.mBooks);
		notifyDataSetChanged();
	}
	
	@Override
	public Book getItem(int position) {	
		if (!TextUtils.isEmpty(mDisplayMessage)) {
			return null;
		}
		return mBooks[position];
	}
	

	
	@Override
	public int getCount() {
		return SIZE;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Holder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(mLayoutId, parent, false);
			holder = new Holder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		if (!TextUtils.isEmpty(mDisplayMessage)) {
			holder.text.setText(mDisplayMessage);
			return convertView;
		}
		
		holder.text.setText(mBooks[position].getName());
		
		return convertView;
	}
	
	public static class Holder {
		public final TextView text;
		public Holder(View rootView) {
			this.text = (TextView) rootView.findViewById(R.id.grid_item_text);
		}
	}	

}
