package com.inductivebiblestudyapp.ui.dialogs;

import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.util.Utility;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;

/**
 * Dependent on the library project: https://github.com/LarsWerkman/HoloColorPicker, this 
 * dialog allows the user to pick a colour and its "brightness".
 * 
 * @author Jason Jenkins 
 * @version 0.1.0-20150616
 * 
 */
public class ColorPickerDialog extends DialogFragment implements OnClickListener {
	final static private String CLASS_NAME = ColorPickerDialog.class
			.getSimpleName();
	
	private static final String ARG_TITLE = CLASS_NAME + ".ARG_TITLE";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static ColorPickerDialog newInstance(String title) {
		ColorPickerDialog dialog = new ColorPickerDialog();
		Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		dialog.setArguments(args);
		return dialog;
	}
	
	private OnColorPickedListener mOnColorPickedListener = null;
	
	private ColorPicker mPicker = null;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SimpleDarkDialog);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//inflate from activity to ensure the dialog is styled correctly.
		View rootView = View.inflate(getActivity(), R.layout.dialog_color_picker, null); 
		
		Bundle args = getArguments();
		
		if (args != null) {
			((TextView) rootView.findViewById(R.id.dialog_colorPicker_text_title))
				.setText(args.getString(ARG_TITLE));
		}
		
		mPicker = (ColorPicker) rootView.findViewById(R.id.dialog_colorPicker_picker);
		//Saturation & Values i.e. brightness
		SVBar svBar = (SVBar) rootView.findViewById(R.id.dialog_colorPicker_svbar);

		
		mPicker.addSVBar(svBar);

		

		//To set the old selected color u can do it like this
		mPicker.setOldCenterColor(mPicker.getColor());
		// adds listener to the colorpicker which is implemented
		//in the activity
		//picker.setOnColorChangedListener(this); 

		//to turn on/off showing the old color
		mPicker.setShowOldCenterColor(false);

		//adding onChangeListeners to bars
		//valuebar.setOnValueChangeListener(new OnValueChangeListener �)
		//saturationBar.setOnSaturationChangeListener(new OnSaturationChangeListener �)
		
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_dialog_button_positive, this);
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_dialog_button_negative, this);
		
		
		return rootView;
	}	
	

	public void setOnColorPickedListener(OnColorPickedListener listener) {
		this.mOnColorPickedListener = listener;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.ibs_dialog_button_positive:
			if (mOnColorPickedListener != null) {
				int argbColor = mPicker.getColor();
				mOnColorPickedListener.onColorPicked(argbColor);
			}
			dismiss();
			break;		
		case R.id.ibs_dialog_button_negative:
			dismiss();
			onCancel(getDialog());
			break;
			
		}
	}

	
	
	@Override
	public void onCancel(DialogInterface dialog) {	
		super.onCancel(dialog);
		if (mOnColorPickedListener != null) {
			mOnColorPickedListener.onCancel();
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static interface OnColorPickedListener {
		/**
		 * @param color The int hex color code (where 0xffffff == 255)
		 */
		public void onColorPicked(int color);
		public void onCancel();
	}
}
