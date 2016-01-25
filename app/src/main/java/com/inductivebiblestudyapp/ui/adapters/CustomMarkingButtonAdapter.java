package com.inductivebiblestudyapp.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.inductivebiblestudyapp.R;

/**
 *  Contains all formatting.
 * @author Jason Jenkins
 * @version 0.3.0-201506714
 */
public class CustomMarkingButtonAdapter extends BaseAdapter {

	
	//MUST be sequential
	public static final int ID_HIGHLIGHT = 0;
	public static final int ID_UNDERLINE = 1;
	public static final int ID_UNDERLINE_DOUBLE = 2;
	public static final int ID_STRIKE = 3;	
	public static final int ID_BOX = 4;
	public static final int ID_BOX_DOUBLE = 5;
	public static final int ID_COLOR = 6;
	public static final int ID_BOLD = 7;
	public static final int ID_ITALICS = 8;
	
	/** Parallel to content descriptions and used as id's */
	private static final int[] BUTTON_IDS = new int[]{
		ID_HIGHLIGHT,
		ID_UNDERLINE,
		ID_UNDERLINE_DOUBLE,
		ID_STRIKE,
		ID_BOX,
		ID_BOX_DOUBLE,
		ID_COLOR,
		ID_BOLD,
		ID_ITALICS
	};
	
	static {
		final int SIZE = BUTTON_IDS.length;
		for (int index = 0; index < SIZE; index++) {
			if (index != BUTTON_IDS[index]) {
				//This is necessary for its contract & the position being used as id in app.
				throw new IllegalStateException("The id's are incorrectly configured, starting at index:" + index);
			}
		}
	}
	
	/** Parallel to content descriptions and used as id's */
	private static final int[] BUTTON_IMAGES = new int[]{
		//R.drawable.ic_format_note,
		R.drawable.ic_format_highlight,
		//R.drawable.ic_format_img,
		R.drawable.ic_format_underline,
		R.drawable.ic_format_double_underline,
		R.drawable.ic_format_strike,
		//R.drawable.ic_format_outline,
		//R.drawable.ic_format_tag,
		R.drawable.ic_format_box,
		R.drawable.ic_format_double_box,
		R.drawable.ic_format_color,
		R.drawable.ic_format_bold,
		R.drawable.ic_format_italics
	};
	/** Used for accessibility purposes. */
	private static final int[] BUTTON_DESCRIPTIONS = new int[]{
		//R.string.ibs_contentDescription_format_note,
		R.string.ibs_contentDescription_format_highlight,
		//R.string.ibs_contentDescription_format_image,
		R.string.ibs_contentDescription_format_underline_single,
		R.string.ibs_contentDescription_format_underline_double,
		R.string.ibs_contentDescription_format_strikethrough,
		//R.string.ibs_contentDescription_format_outline,
		//R.string.ibs_contentDescription_format_tag,
		R.string.ibs_contentDescription_format_box_single,
		R.string.ibs_contentDescription_format_box_double,
		R.string.ibs_contentDescription_format_color,
		R.string.ibs_contentDescription_format_bold,
		R.string.ibs_contentDescription_format_italics
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// end constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected final LayoutInflater inflater;	
	protected final Resources res;
	
	public CustomMarkingButtonAdapter(Context context) {
		if (BUTTON_IMAGES.length !=  BUTTON_DESCRIPTIONS.length || 
			BUTTON_IMAGES.length != BUTTON_IDS.length ) {
			throw new IllegalStateException("Cannot have mismatching parallel arrays");
		}
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.res = context.getResources();
	}
	
	@Override
	public int getCount() {
		return BUTTON_IMAGES.length;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return BUTTON_IDS[position];
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.grid_item_marking, parent, false);
			holder = new Holder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		holder.button.setContentDescription(res.getString(BUTTON_DESCRIPTIONS[position]));
		holder.button.setImageDrawable(res.getDrawable(BUTTON_IMAGES[position]));
		
		return convertView;
	}
	
	public static class Holder {
		public final ImageView button;
		
		public Holder(View rootView) {
			button = (ImageView) rootView.findViewById(R.id.grid_item_imagebuttom);
		}
	}

}
