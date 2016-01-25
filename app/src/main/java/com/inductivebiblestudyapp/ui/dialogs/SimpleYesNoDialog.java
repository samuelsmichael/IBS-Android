package com.inductivebiblestudyapp.ui.dialogs;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.util.Utility;

/**
 * Creates a simple dialog with a yes and no button.
 * Note: The following events are available: 
 * <ul>
 * <li>Yes == {@link DialogInterface#BUTTON_POSITIVE}</li>
 * <li>No/Cancel/Dismiss == {@link DialogInterface#BUTTON_NEGATIVE}</li>
 * </li>
 * Fragments can either setTarget (and use onActivityResult) or Activities can implement 
 * {@link DialogInterface#OnClickListener}. 
 * Creates a 250dp wide dialog by default.
 * 
 * @author Jason Jenkins
 * @version 0.4.0-20150831
 */
public class SimpleYesNoDialog extends DialogFragment implements OnClickListener {
	final static private String CLASS_NAME = SimpleYesNoDialog.class
			.getSimpleName();
	
	
	private static final String ARG_MESSAGE = CLASS_NAME + ".ARG_MESSAGE";
	private static final String ARG_LOCK_ON_SUBMIT = CLASS_NAME + ".ARG_LOCK_ON_SUBMIT";
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @param message The message for the dialog.
	 * @param lockOnSubmit if <code>true</code> locks the dialog inputs and does not dismiss
	 * for either yes or no. If <code>false</code>, the dialog simply dismisses.
	 */
	public static SimpleYesNoDialog newInstance(String message, boolean lockOnSubmit) {
		SimpleYesNoDialog simpleDialog = new SimpleYesNoDialog(); 
		Bundle args = new Bundle();
				
		args.putString(ARG_MESSAGE, message);
		args.putBoolean(ARG_LOCK_ON_SUBMIT, lockOnSubmit);
		
		simpleDialog.setArguments(args);
		return simpleDialog;
	}
	
	/**
	 * @param message The message for the dialog.
	 */
	public static SimpleYesNoDialog newInstance(String message) {
		return newInstance(message, false);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End initializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The optional listener for the application. */
	private DialogInterface.OnClickListener mDialogListener = null;
	
	private TextView mContentView = null;
	
	private View mProgressView = null;
	private View mContainerView = null;
	
	private View mSubmitPositive = null;
	private View mSubmitNegative = null;
	
	private String mContent = null;	
	private boolean mLockOnSubmit = false;
	
	
	
	public SimpleYesNoDialog() {}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString(ARG_MESSAGE, mContent);
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SimpleDarkDialog);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//inflate from activity to ensure the dialog is styled correctly.
		View rootView = View.inflate(getActivity(), R.layout.dialog_simple_yes_no, null); 
				
		
		Bundle args = getArguments() ;		
		
		mContentView = (TextView) rootView.findViewById(R.id.ibs_dialog_text_message);
		
		mContainerView = mContentView;		
		mProgressView = Utility.getProgressView(rootView);
		
		if (savedInstanceState != null) {
			mContent = savedInstanceState.getString(ARG_MESSAGE);
		} else if (args != null) {			
			mContent = args.getString(ARG_MESSAGE);		
			mLockOnSubmit = args.getBoolean(ARG_LOCK_ON_SUBMIT);
		}
		if (mProgressView != null && mContent != null) {
			mProgressView.setVisibility(View.GONE);
		}
		
		checkAndSetText();
		
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
	 * Attempts to update title & message.
	 * @param title The title to set
	 * @param message The message to set.
	 */
	public void updateContent(String message) {
		mContent = message;
		checkAndSetText();		
	}
	
	/**
	 * WARNING! You will need to re-attach an manage this on rotations.
	 * @param listener
	 */
	public void setDialogOnClickListener(DialogInterface.OnClickListener listener) {
		this.mDialogListener = listener;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void enableInputs(boolean enabled) {
		if (mSubmitPositive != null) {
			mSubmitNegative.setEnabled(enabled);
			mSubmitPositive.setEnabled(enabled);
		}
	}
	
	/** Sets text, and checks to see if all text is loaded. If not, shows progress bars. */
	private void checkAndSetText() {
		
		if (mContent != null ) { 
			mContentView.setText(mContent);			
			return;
		}
		
		if (mProgressView == null || mContainerView == null) {
			//if either view is null, we cannot animate them.
			return;
		}

		//makes the spinner spin, only if necessary.
		if (mContent == null ) {
			Utility.switchFadeProgressViews(mProgressView, mContainerView, true); 
		} else {
			Utility.switchFadeProgressViews(mProgressView, mContainerView, false); 
		}
	}
	
	/** Checks the target fragment and listener to see if anyone is listening, if so sends. */
	private void tryToSendMessage(int which) {
		final Fragment target = getTargetFragment();		
		if (mDialogListener != null &&  target == null){
			//only if we have a listener and target fragment.
			mDialogListener.onClick(getDialog(), which);			
			return;
		} else if(target != null) {
			target.onActivityResult(getTargetRequestCode(), 
					which, null);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Implemented Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@Override
	public void onClick(View v) {
		if (mLockOnSubmit) {
			enableInputs(false);
		} else {
			dismiss();
		}
		
		int which = 0;
		switch (v.getId()) {
		case R.id.ibs_dialog_button_positive:
			which = DialogInterface.BUTTON_POSITIVE;			
			break;		
		case R.id.ibs_dialog_button_negative:
			which = DialogInterface.BUTTON_NEGATIVE;
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
