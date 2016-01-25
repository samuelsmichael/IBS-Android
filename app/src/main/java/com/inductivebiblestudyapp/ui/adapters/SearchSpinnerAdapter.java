package com.inductivebiblestudyapp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.inductivebiblestudyapp.data.model.bible.IBibleSearchItem;

/**
 * The spinner adapter used to display search items used for narrowing the search.
 * 
 * @author Jason Jenkins
 * @version 0.1.1-20150808
 *
 */
public class SearchSpinnerAdapter extends ArrayAdapter<IBibleSearchItem> {	
	
	
	private final LayoutInflater mInflater;
	 /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private final int mLayoutResource;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter in a drop down widget.
     */
    private final int mDropDownResource;
    
    /** Whether or not to show an "All" first item. */
    private final boolean mIsFirstItemAll;
    private final String mAllString;
    
    private DisplayNameCallback mCallback = null;

	/**
	 * Constructor. Note that it requires all text views to have the id <code>android.R.id.text1</code>
	 * @param context The current context.
     * @param layoutResource The resource ID for a layout file containing a layout to use when
     *                 instantiating views.
	 */
	public SearchSpinnerAdapter(Context context, int layoutResource) {
		super(context, layoutResource);
		
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayoutResource = mDropDownResource = layoutResource;
		mAllString = "";
		mIsFirstItemAll = false;
	}
	
	/**
	 * Constructor. Note that it requires all text views to have the id <code>android.R.id.text1</code>
	 * @param context The current context.
     * @param layoutResource The resource ID for a layout file containing a layout to use when
     *                 instantiating views.
     * @param allResourceId The string resource used to define "all", note this will
     * cause {@link #getItem(int)} to return <code>null</code> on 0.
	 */
	public SearchSpinnerAdapter(Context context, int layoutResource,
			int dropdownResource, int allResourceId) {
		super(context, layoutResource);
		
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayoutResource = mDropDownResource = layoutResource;
		mAllString = context.getString(allResourceId);
		mIsFirstItemAll = true;
	}
	
	/** Set the object item to set the display name on. */
	public void setDisplayNameCallback(DisplayNameCallback mCallback) {
		this.mCallback = mCallback;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start overrides
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public int getCount() {
		final int count = super.getCount(); 
		return count + (mIsFirstItemAll && count > 0 ? 1 : 0);
	}
	
	@Override
	public long getItemId(int position) {
		if (mIsFirstItemAll) {
			position++; //adjust offset
		}
		return position;
	}
	
	@Override
	public int getPosition(IBibleSearchItem item) {
		return super.getPosition(item) + (mIsFirstItemAll ? 1 : 0);
	}
	
	/**
	 * Note: This may return <code>null</code> if the first item is set to "all"
	 * <br/>
	 * {@inheritDoc}
	 */
	@Override
	public IBibleSearchItem getItem(int position) {
		if (mIsFirstItemAll) {
			position--; //adjust for offset
		}
		if (position < 0) {
			return null;
		}
		return super.getItem(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mLayoutResource);
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mDropDownResource);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Creates the view from the given resource. */
	private View createViewFromResource(int position, View convertView, ViewGroup parent,
            int resource) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
        	convertView = mInflater.inflate(resource, parent, false);
        	viewHolder = new ViewHolder(convertView);
        } else {
        	viewHolder = (ViewHolder) convertView.getTag();
        }

        IBibleSearchItem item = getItem(position);
        String text = "";
        if (item != null) {
        	text = mCallback != null ? mCallback.getDisplayString(item) : item.getSearchResultName();
        } else {
        	text = mAllString;
        }
        
        viewHolder.text.setText(text);

        return convertView;
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Classes & interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static interface DisplayNameCallback {
		public String getDisplayString(IBibleSearchItem item);
	}
	
	private static class ViewHolder {
		final TextView text;
		public ViewHolder(View root) {
			text = (TextView) root.findViewById(android.R.id.text1);
			if (text == null) {
				throw new IllegalStateException("No text id found (must contain android.R.id.text1)");
			}
			root.setTag(this);
		}
	}

}
