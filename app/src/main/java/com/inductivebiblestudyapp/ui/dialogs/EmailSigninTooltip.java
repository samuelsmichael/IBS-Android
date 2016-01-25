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

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.util.Utility;

/**
 *
 * Based on original content from {@link SimpleTooltipDialog} (0.6.0-20150611).
 * 
 * Fragments can either setTarget (and use onActivityResult) or Activities can implement 
 * {@link OnClickListener}
 * 
 * <br />
 * 
 * Creates a 250dp wide dialog by default.
 * 
 * @author Jason Jenkins
 * @version 0.2.0-201506015
 */
public class EmailSigninTooltip extends SimpleTooltipDialog implements OnClickListener {
	
	final static private String CLASS_NAME = EmailSigninTooltip.class
			.getSimpleName();
	
	/** Argument key: Int[]. The {width, height} of the view in pixels, whose width to use to determine the location of the arrow. */
	private static final String ARG_ANCHOR_VIEW_DIMEN = CLASS_NAME + ".ARG_ANCHOR_VIEW_DIMEN";
	
	/** Argument key: Int[]. The location of the view in pixels, whose width to use to determine the location of the arrow. */
	private static final String ARG_ANCHOR_LOCATION = CLASS_NAME + ".ARG_ANCHOR_LOCATION";
	

	/** Argument key: Boolean. true for centered, false for not.  */
	private static final String ARG_DIALOG_CENTERED = CLASS_NAME + ".ARG_DIALOG_CENTERED";
	
	public static final String KEY_EMAIL = CLASS_NAME  + ".KEY_EMAIL";
	public static final String KEY_PASSWORD = CLASS_NAME  + ".KEY_PASSWORD";
	
	/** Accompanied by data containing email & password. */
	public static final int RESULT_SUBMIT = 0;
	/** Accompanied by data containing email. */
	public static final int RESULT_FORGOT_PASSWORD = 1;
	public static final int RESULT_CANCEL = 2;	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static EmailSigninTooltip newInstance(View anchor) {
		EmailSigninTooltip tooltip = new EmailSigninTooltip();
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
		tooltip.setArguments(args);
		return tooltip;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods here
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public EmailSigninTooltip() {
		super();
	}
	
	/** The optional listener for the application. */
	private OnClickListener mDialogListener = null;
	

	private EditText mEt_email = null;
	private EditText mEt_password = null;
	
	private View mSiginButton = null;
	private View mForgotButton = null;
	
	private int[] mAnchorDimens = new int[]{};
	private int[] mAnchorLocation = new int[]{};
	private boolean mCentered = false;

	
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
		}
		
		
		rootView = setupRootView(inflater, container);
		
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
	public boolean setErrors(String emailError, String passwordError) {
		if (mEt_email != null && !emailError.isEmpty()) {
			mEt_email.setError(emailError);			
		}
		if (mEt_password != null && !passwordError.isEmpty()) {
			mEt_password.setError(passwordError);
		}

		//if both are empty, return true
		boolean noErrors = passwordError.isEmpty() && emailError.isEmpty();
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
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void setEnabled(boolean enabled) {
		if (mEt_email != null && mEt_password != null && mForgotButton != null && mSiginButton != null) {
			mEt_email.setEnabled(enabled);
			mEt_password.setEnabled(enabled);
			mForgotButton.setEnabled(enabled);
			mSiginButton.setEnabled(enabled);
		}
	}

	private View setupRootView(LayoutInflater inflater, ViewGroup container) {
		View rootView;
		rootView = inflater.inflate(R.layout.dialog_email_signin, container,
				false);

		mDownArrow = rootView.findViewById(R.id.dialog_tooltip_arrowDown);
		mUpArrow = rootView.findViewById(R.id.dialog_tooltip_arrowUp);
		
		mEt_email = (EditText) rootView.findViewById(R.id.dialog_emailSignin_et_email);
		mEt_password = (EditText) rootView.findViewById(R.id.dialog_emailSignin_et_password);
		
		mForgotButton = Utility.setOnClickAndReturnView(rootView, R.id.dialog_emailSignin_button_forgot, this);
		mSiginButton = Utility.setOnClickAndReturnView(rootView, R.id.dialog_emailSignin_button_signin, this);
		return rootView;
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
		final String password = mEt_password.getText().toString();
		mEt_password.setError(null);		
		
		if (mDialogListener != null &&  target == null) {
			switch (id) {
			case R.id.dialog_emailSignin_button_signin:
				setEnabled(false);
				mDialogListener.onSubmit(email, password);
				break;
				
			case R.id.dialog_emailSignin_button_forgot:
				mDialogListener.onForgot(email);			
				break;
			}
		} else if(target != null) {
			final int requestCode = getTargetRequestCode();			
			
			Intent result = new Intent();
			result.putExtra(KEY_EMAIL, email);
			
			switch (id) {
			
			case R.id.dialog_emailSignin_button_signin:
				result.putExtra(KEY_PASSWORD, password);
				target.onActivityResult(requestCode, RESULT_SUBMIT, result);			
				break;
			
			case R.id.dialog_emailSignin_button_forgot:				
				target.onActivityResult(requestCode, RESULT_FORGOT_PASSWORD, result);
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
		public void onSubmit(String email, String password);
		public void onForgot(String email);
		public void onCancel();
	}
	
}