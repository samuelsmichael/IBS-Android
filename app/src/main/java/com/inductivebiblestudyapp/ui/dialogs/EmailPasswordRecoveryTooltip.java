package com.inductivebiblestudyapp.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.util.Utility;

/**
 *  
 * Based on {@link MessageToolTip} & {@link EmailSigninTooltip}.
 * 
 * Fragments can either setTarget (and use onActivityResult) or Activities can implement 
 * {@link OnClickListener}
 * 
 * <br />
 * 
 * Creates a 250dp wide dialog by default.
 * 
 * @author Jason Jenkins
 * @version 0.1.0-201506019
 */
public class EmailPasswordRecoveryTooltip extends SimpleTooltipDialog implements OnClickListener, UpdateDialogMessage {
	
	final static private String CLASS_NAME = EmailPasswordRecoveryTooltip.class
			.getSimpleName();
	
	/** Argument key: Int[]. The {width, height} of the view in pixels, whose width to use to determine the location of the arrow. */
	private static final String ARG_ANCHOR_VIEW_DIMEN = CLASS_NAME + ".ARG_ANCHOR_VIEW_DIMEN";
	
	/** Argument key: Int[]. The location of the view in pixels, whose width to use to determine the location of the arrow. */
	private static final String ARG_ANCHOR_LOCATION = CLASS_NAME + ".ARG_ANCHOR_LOCATION";

	/** Argument key: Boolean. true for centered, false for not.  */
	private static final String ARG_DIALOG_CENTERED = CLASS_NAME + ".ARG_DIALOG_CENTERED";
	
	/** Argument key: String. The string to set as message. */
	private static final String ARG_MESSAGE = CLASS_NAME + ".ARG_MESSAGE";
	
	public static final String KEY_EMAIL = CLASS_NAME  + ".KEY_EMAIL";
	
	/** Accompanied by data containing email.. */
	public static final int RESULT_SUBMIT = 0;
	public static final int RESULT_CANCEL = 2;	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static EmailPasswordRecoveryTooltip newInstance(View anchor, String email, String message) {
		EmailPasswordRecoveryTooltip tooltip = new EmailPasswordRecoveryTooltip();
		Bundle args = new Bundle();
		int[] anchorLocation = new int[2];
		anchor.getLocationOnScreen(anchorLocation);
		
		int[] anchorDimens = {
				anchor.getWidth(),
				anchor.getHeight()
		};
		
		args.putBoolean(ARG_DIALOG_CENTERED, true);
		
		args.putIntArray(ARG_ANCHOR_LOCATION, anchorLocation);
		args.putIntArray(ARG_ANCHOR_VIEW_DIMEN, anchorDimens);
		
		args.putString(ARG_MESSAGE, message);
		args.putString(KEY_EMAIL, email);
		
		tooltip.setArguments(args);
		return tooltip;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods here
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public EmailPasswordRecoveryTooltip() {
		super();
	}
	
	/** The optional listener for the application. */
	private OnClickListener mDialogListener = null;
	

	private EditText mEt_email = null;
	
	private View mSubmitButton = null;
	
	private View mContainerView = null;
	private View mProgressView = null;
	
	private TextView mMessageView = null;
	
	private int[] mAnchorDimens = new int[]{};
	private int[] mAnchorLocation = new int[]{};
	private boolean mCentered = false;
	
	private String mMessage = null;

	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = null;
		
		Bundle args = getArguments() ;
		Dialog dialog = getDialog();
		
		mAnchorDimens = new int[]{};
		mAnchorLocation = new int[]{};
		mCentered = false;
		
		if (args != null) {			
			mAnchorDimens = args.getIntArray(ARG_ANCHOR_VIEW_DIMEN);
			mAnchorLocation = args.getIntArray(ARG_ANCHOR_LOCATION);
			mCentered = args.getBoolean(ARG_DIALOG_CENTERED);

			if (mMessage == null) {
				mMessage = args.getString(ARG_MESSAGE);
			}
		}
		
		
		rootView = setupRootView(inflater, container, args);
		
		//adjust views, just in time.
		adjustView(dialog, rootView, mAnchorLocation, mAnchorDimens, mCentered);
		
		return rootView;
	}

	@Override
	public void onPause() {
		super.onPause();
		dismiss();		
	}
	
	/**
	 * Sets the error message if any
	 * @param emailError The email error to set
	 * @param passwordError The password error to set
	 * @return <code>true</code> if both error messages are empty.
	 */
	public boolean setErrors(String emailError) {
		if (mEt_email != null && !emailError.isEmpty()) {
			mEt_email.setError(emailError);			
		}

		//if both are empty, return true
		boolean noErrors = emailError.isEmpty();
		setEnabled(!noErrors);
		return noErrors;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		Fragment target = getTargetFragment();
		
		if (target == null) { // no one is listening? Try the activity 
			try {
				mDialogListener = (OnClickListener) getActivity();
				
			} catch (ClassCastException e) {
				//nothing to do.
			}
		} 
	}
	
	
	/**
	 * Attempts to update the <code>android.R.string.text1</code> message
	 * @param message The message to set.
	 */
	@Override
	public void updateMessage(String message) {
		mMessage = message;
		setMessageTextIfFound(message);
		checkIfWaiting();

		final View rootView = getView();
		final Dialog dialog = getDialog();
		
		if (rootView != null) {
			rootView.invalidate();
			
			rootView.post(new Runnable() {
				//TODO remove adjustment flicker
				@Override
				public void run() {
					adjustView(dialog, rootView, mAnchorLocation, mAnchorDimens, mCentered);
					rootView.invalidate();
				}
			});
		}		
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void setEnabled(boolean enabled) {
		if (mEt_email != null  && mSubmitButton != null) {
			mEt_email.setEnabled(enabled);
			mSubmitButton.setEnabled(enabled);
		}
	}

	private View setupRootView(LayoutInflater inflater, ViewGroup container, Bundle args) {
		View rootView;
		rootView = inflater.inflate(R.layout.dialog_email_forgot_pass, container,
				false);

		mDownArrow = rootView.findViewById(R.id.dialog_tooltip_arrowDown);
		mUpArrow = rootView.findViewById(R.id.dialog_tooltip_arrowUp);
		
		mContainerView = rootView.findViewById(R.id.dialog_emailForgot_container);
		mProgressView = Utility.getProgressView(rootView);
		
		mMessageView = (TextView) rootView.findViewById(R.id.dialog_emailForgot_text);
	
		mEt_email = (EditText) rootView.findViewById(R.id.dialog_emailForgot_email);
		if (args != null) {
			mEt_email.setText(args.getString(KEY_EMAIL));
		}
		
		mSubmitButton = Utility.setOnClickAndReturnView(rootView, R.id.dialog_emailForgot_submit, this);
		
		//if message is pre-set
		if (mMessage != null  && mMessageView != null && mProgressView != null){
			mMessageView.setVisibility(View.VISIBLE);
			mProgressView.setVisibility(View.GONE);		
		} else {
			checkIfWaiting();
		}
		
		setMessageTextIfFound(mMessage);
				
		return rootView;
	}
    
	
	/**
	 * Sets the message text.
	 * @param message
	 * @return <code>true</code> if the content has changed, <code>false</code> otherwise.
	 */
	private boolean setMessageTextIfFound(String message) {
		if (mMessage != null && mMessageView != null) {
			boolean contentChanged = !mMessageView.getText().toString().equals(message);
			mMessageView.setText(message);
			mMessageView.invalidate();
			return contentChanged; 
		}
		return false;
	}

	
	/**
	 * Performs view safety checks, then animates views (if forced) or checks whether to
	 * animate views based on loader state.
	 * @param force
	 */
	private void checkIfWaiting() {
		if (mProgressView == null || mContainerView == null) {
			//if either view is null, we cannot animate them.
			return;
		}

		//makes the spinner spin, only if necessary.
		if (mMessage == null) {
			Utility.switchFadeProgressViews(mProgressView, mContainerView, true); 
		} else {
			Utility.switchFadeProgressViews(mProgressView, mContainerView, false); 
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Implemented Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onClick(View v) {
		//dismiss();
		final int id = v.getId();
		final Fragment target = getTargetFragment();	
		
		final String email = mEt_email.getText().toString();
		mEt_email.setError(null);	
		
		if (mDialogListener != null &&  target == null) {
			switch (id) {
			case R.id.dialog_emailForgot_submit:
				setEnabled(false);
				mDialogListener.onSubmit(email);
				break;
				
			}
		} else if(target != null) {
			final int requestCode = getTargetRequestCode();			
			
			Intent result = new Intent();
			result.putExtra(KEY_EMAIL, email);
			
			switch (id) {			
			case R.id.dialog_emailForgot_submit:			
				target.onActivityResult(requestCode, RESULT_SUBMIT, result);
				break;
			}
		}
		
	}

		
		
	@Override
	public void onCancel(DialogInterface dialog) {	
		super.onCancel(dialog);
		
		final Fragment target = getTargetFragment();	
		
		if (mDialogListener != null &&  target == null){
			mDialogListener.onCancel();
		} else if(target != null) {
			target.onActivityResult(getTargetRequestCode(), 
					RESULT_CANCEL, null);
		}
	}
	
	public static interface OnClickListener {
		public void onSubmit(String email);
		public void onCancel();
	}
	
}