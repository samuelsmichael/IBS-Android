package com.inductivebiblestudyapp.ui.dialogs;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.util.Utility;

/**
 * Creates a simple dialog with a yes and no button.
 * Note: The following events are available: 
 * <ul>
 * <li>Save == {@link DialogInterface#BUTTON_POSITIVE}</li>
 * <li>No/Cancel/Dismiss == {@link DialogInterface#BUTTON_NEGATIVE}</li>
 * </li>
 * Fragments can either setTarget (and use onActivityResult) or Activities can implement 
 * {@link DialogInterface#OnClickListener}. 
 * Creates a 250dp wide dialog by default.
 * 
 * @author Jason Jenkins
 * @version 0.9.1-20150921
 */
public class SimpleSaveDialog extends DialogFragment implements OnClickListener {
	final static private String CLASS_NAME = SimpleSaveDialog.class
			.getSimpleName();
	
	private static final String ARG_MESSAGE = CLASS_NAME + ".ARG_MESSAGE";
	private static final String ARG_SHARE_AND_DELETE = CLASS_NAME + ".ARG_SHARE_AND_DELETE";
	
	
	/** The action for when the delete button is pressed. */
	public static final int DIALOG_DELETE_ACTION = 0x10;
	/** The action for when the share button is pressed. */
	public static final int DIALOG_SHARE_ACTION = 0x11;
	
	/** Bundle key: String. The value of the input. */
	public static final String KEY_DIALOG_INPUT = CLASS_NAME + ".KEY_DIALOG_INPUT";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @param message The message for the dialog.
	 * @param input The starting content to give the dialog.  
	 * @param extraButtons Whether to show a share & delete buttons or not.
	 */
	public static SimpleSaveDialog newInstance(String message, String input, boolean extraButtons) {
		SimpleSaveDialog simpleDialog = new SimpleSaveDialog(); 
		Bundle args = new Bundle();
		
		args.putString(ARG_MESSAGE, message);
		args.putString(KEY_DIALOG_INPUT, input);
		args.putBoolean(ARG_SHARE_AND_DELETE, extraButtons);
		
		simpleDialog.setArguments(args);
		return simpleDialog;
	}
	
	/** 
	 * Same as calling {@link #newInstance(String, String, boolean)} set <code>true</code>. 
	 */
	public static SimpleSaveDialog newInstance(String message, String input) {
		return newInstance(message, input, true);
	}
	
	/**
	 * Same as calling {@link #newInstance(String, String, boolean)} 
	 *  with an empty input.
	 */
	public static SimpleSaveDialog newInstance(String message, boolean extraButtons) {
		return newInstance(message, "", extraButtons);
	}
	
	/** Same as calling {@link #newInstance(String, String, boolean)}
	 * with an empty input and a boolean set false.
	 * @param message The message for the dialog.
	 */
	public static SimpleSaveDialog newInstance(String message) {
		return newInstance(message, "", false);
	}

	
	
	public SimpleSaveDialog() {}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End initializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/** The optional listener for the application. */
	protected DialogInterface.OnClickListener mDialogListener = null;
	
	private EditText mDialogInput = null;
	
	private View mSubmitPositive = null;
	private View mSubmitNegative = null;
	
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SimpleDarkDialog);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//inflate from activity to ensure the dialog is styled correctly.
		View rootView = View.inflate(getActivity(), R.layout.dialog_simple_save, null); 
		
		mDialogInput = (EditText) rootView.findViewById(R.id.ibs_dialog_input);
		
		Bundle args = getArguments() ;
		
		if (args != null) {			
			((TextView) rootView.findViewById(R.id.ibs_dialog_text_message))
				.setText(args.getString(ARG_MESSAGE));
			
			String input = args.getString(KEY_DIALOG_INPUT);
			
			boolean showButtons = args.getBoolean(ARG_SHARE_AND_DELETE);
			
			Utility.setOnClickAndReturnView(rootView, R.id.ibs_dialog_button_delete, this)
					.setVisibility(showButtons ? View.VISIBLE : View.GONE);
			
			Utility.setOnClickAndReturnView(rootView, R.id.ibs_dialog_button_share, this)
					.setVisibility(showButtons ? View.VISIBLE : View.GONE);
			
			if (input != null) {
				mDialogInput.setText(input);
			}
		}
				
		mSubmitPositive = Utility.setOnClickAndReturnView(rootView, R.id.ibs_dialog_button_positive, this);
		mSubmitNegative = Utility.setOnClickAndReturnView(rootView, R.id.ibs_dialog_button_negative, this);
		
		enableInputs(true);
				
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		Fragment target = getTargetFragment();
		
		if (target == null) { // no one is listening? Try the activity 
			try {
				mDialogListener = (DialogInterface.OnClickListener) getActivity();
				
			} catch (ClassCastException e) {
				//nothing to do.
			}
		} 
	}
	
	
	/**
	 * WARNING! You will need to re-attach an manage this on rotations.
	 * @param listener
	 */
	public void setDialogOnClickListener(DialogInterface.OnClickListener listener) {
		this.mDialogListener = listener;
	}
	
	/** @return The current text input. */
	public String getInputText() {
		if (mDialogInput != null){
			return mDialogInput.getText().toString();
		}
		return "";
	}
	
	/** Sets errors and enables the dialog inputs.
	 *  @param error Sets the input error. */
	public void setInputError(String error) {
		mDialogInput.setError(error);
		enableInputs(true);
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Used to enabled and disable all text & button inputs for regular mode. */
	private void enableInputs(boolean enabled) {
		if (mDialogInput == null) {
			return;
		}
		mDialogInput.setEnabled(enabled);
		mSubmitPositive.setEnabled(enabled);
		mSubmitNegative.setEnabled(enabled);
	}

	
	/** Checks the target fragment and listener to see if anyone is listening, if so sends. */
	private void tryToSendMessage(int which) {
		final Fragment target = getTargetFragment();		
		if (mDialogListener != null &&  target == null){
			//only if we have a listener and target fragment.
			mDialogListener.onClick(getDialog(), which);	
			
		} else if(target != null) {
			Intent intent = new Intent();
			intent.putExtra(KEY_DIALOG_INPUT, mDialogInput.getText().toString());
			target.onActivityResult(getTargetRequestCode(), 
					which, intent);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Implemented Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@Override
	public void onClick(View v) {
		
		int which = 0;
		switch (v.getId()) {
		case R.id.ibs_dialog_button_positive:
			which = DialogInterface.BUTTON_POSITIVE;
			//dismiss(); let client handle it
			enableInputs(false);
			break;		
		case R.id.ibs_dialog_button_negative:
			which = DialogInterface.BUTTON_NEGATIVE;
			dismiss();
			break;
			
		case R.id.ibs_dialog_button_share:
			which = DIALOG_SHARE_ACTION;
			enableInputs(false);
			break;
			
		case R.id.ibs_dialog_button_delete:			
			which = DIALOG_DELETE_ACTION; 
			break;
		}
		
		tryToSendMessage(which);
	}

	
	
	@Override
	public void onCancel(DialogInterface dialog) {	
		super.onCancel(dialog);
		tryToSendMessage(DialogInterface.BUTTON_NEGATIVE);
	}
	
}
