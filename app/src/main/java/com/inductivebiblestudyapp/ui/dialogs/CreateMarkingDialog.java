package com.inductivebiblestudyapp.ui.dialogs;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.ui.actionwrappers.EditLetteringsActionWrapper;
import com.inductivebiblestudyapp.ui.actionwrappers.EditLetteringsActionWrapper.OnActionListener;
import com.inductivebiblestudyapp.ui.style.TextStyle;
import com.inductivebiblestudyapp.ui.widget.ExpandableHeightGridView;
import com.inductivebiblestudyapp.util.Utility;

/**
 * The create & save dialog for custom lettering markings.
 * @author Jason Jenkins
 * @version 0.8.1-20150723
 */
public class CreateMarkingDialog extends SimpleSaveDialog implements OnActionListener {
	
	final static private String CLASS_NAME = CreateMarkingDialog.class
			.getSimpleName();
	
	
	/** Bundle key: Parcelable. */
	private static final String KEY_STYLE_BUILDER_PRODUCT = CLASS_NAME + ".KEY_STYLE_BUILDER";
	
	private static final boolean DEBUG = false;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static CreateMarkingDialog newInstance() {
		return new CreateMarkingDialog();
	}

	//Default visibility for performance
	final TextStyle.Builder mStyleBuilder = new TextStyle.Builder();
		
	
	/** The listener for the dialog. */
	protected OnLetteringCreateListener mDialogListener = null;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_STYLE_BUILDER_PRODUCT, mStyleBuilder.build());
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mStyleBuilder.set((TextStyle) savedInstanceState.getParcelable(KEY_STYLE_BUILDER_PRODUCT));
		}
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//inflate from activity to ensure the dialog is styled correctly.
		View rootView = View.inflate(getActivity(), R.layout.dialog_marking_save, null); 
				
		
		RecyclerView libraryView = 
				(RecyclerView) rootView.findViewById(R.id.dialog_createMarking_recyclerView);
		
		ExpandableHeightGridView markingsGridView = 
				(ExpandableHeightGridView) rootView.findViewById(R.id.ibs_dialog_grid_createMarking);
		markingsGridView.setExpanded(true);
		
		EditText inputText = (EditText) rootView.findViewById(R.id.ibs_dialog_input);        	
				
		View saveButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_dialog_button_positive, this);
		View cancelButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_dialog_button_negative, this);
		

		//performs actions on behalf of dialog	
		new EditLetteringsActionWrapper(getActivity(), libraryView, markingsGridView, inputText, saveButton, cancelButton, this);		
		
		
		if (DEBUG) {
			testStyleOnTitle(rootView);
		}
				
		return rootView;
	}
	
	/**
	 * WARNING! You will need to re-attach an manage this on rotations.
	 * @param listener
	 */
	public void setOnLetteringCreateListener(OnLetteringCreateListener listener) {
		this.mDialogListener = listener;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** Applies the current style to the title text.
	 * Useful for debugging. */
	private void testStyleOnTitle(View rootView) {		
		if (rootView != null) {
			Log.d(CLASS_NAME, "testStyleOnTitle");
			
			TextView title = (TextView) rootView.findViewById(R.id.ibs_dialog_text_message);
			String text = title.getText().toString();
			
			TextStyle style = mStyleBuilder.build();
			title.setText(style.apply(text, 0, text.length()));
			title.invalidate();
		}
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onSave(LetteringItem item) {
		mDialogListener.onLetterngItemCreate(item);
	}

	@Override
	public void onCancel() {
		mDialogListener.onCancel();
		dismiss();
	}

	@Override
	public void enableViews(boolean enabled) {
		//nothing to do here?
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.1.0-20150720 */
	public static interface OnLetteringCreateListener {
		
		public void onLetterngItemCreate(LetteringItem item);
		
		public void onCancel();
	}

}
