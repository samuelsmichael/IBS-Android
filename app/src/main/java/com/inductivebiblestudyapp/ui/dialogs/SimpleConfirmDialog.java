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
 * Creates a simple dialog with a confirmation button.
 * Note: The following events are available: 
 * <ul>
 * <li>Confirm == {@link DialogInterface#BUTTON_POSITIVE}</li>
 * <li>Cancel/Dismiss == {@link DialogInterface#BUTTON_NEGATIVE}</li>
 * </li>
 * Fragments can either setTarget (and use onActivityResult) or Activities can implement 
 * {@link OnClickListener} or listen via onActivityResult. 
 * Creates a 250dp wide dialog by default.
 * 
 * @author Jason Jenkins
 * @version 0.4.1-20150814
 */
public class SimpleConfirmDialog extends DialogFragment implements OnClickListener {
	final static private String CLASS_NAME = SimpleConfirmDialog.class
			.getSimpleName();
	
	
	/* Argument key: Int. The layout id to use for the child content. */
	//private static final String ARG_LAYOUT_ID = CLASS_NAME + ".ARG_LAYOUT_ID";
	/** Argument key: String. The dialog title. */
	private static final String ARG_TITLE = CLASS_NAME + ".ARG_TITLE";
	
	/** Argument key: String. The dialog content. */
	private static final String ARG_CONTENT = CLASS_NAME + ".ARG_CONTENT";
	
	private static final String KEY_REQUEST_CODE = CLASS_NAME + ".KEY_REQUEST_CODE";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The optional listener for the application. */
	private OnClickListener mDialogListener = null;	
	
	/** 
	 * Note that if the title is <code>null</code> a progress bar will be used until it is updated.
	 * @param title The title to give the window. 
	 * @deprecated Use {@link #newInstance(String, String)} instead
	 */
	@Deprecated
	public static SimpleConfirmDialog newInstance(String title) {
		SimpleConfirmDialog simpleDialog = new SimpleConfirmDialog(); 
		Bundle args = new Bundle();
				
		args.putString(ARG_TITLE, title);
		args.putString(ARG_CONTENT, "");
		
		simpleDialog.setArguments(args);
		return simpleDialog;
	}
	
	/** 
	 * Note that if either title or content is <code>null</code> a progress bar will be used until it is updated.
	 * @param title The title to give the window. 
	 * @param content The content of the window
	 */
	public static SimpleConfirmDialog newInstance(String title, String content) {
		SimpleConfirmDialog simpleDialog = new SimpleConfirmDialog(); 
		Bundle args = new Bundle();
				
		args.putString(ARG_TITLE, title);
		args.putString(ARG_CONTENT, content);
		
		simpleDialog.setArguments(args);
		return simpleDialog;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public SimpleConfirmDialog() {}
	
	private TextView mTitleView =  null;
	private TextView mContentView = null;
	
	private View mProgressView = null;
	private View mContainerView = null;
	
	private String mTitle = null;
	private String mContent = null;
	
	private int mRequestCode = -1;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ARG_TITLE, mTitle);
		outState.putString(ARG_CONTENT, mContent);
		outState.putInt(KEY_REQUEST_CODE, mRequestCode);
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SimpleConfirmDialog);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//inflate from activity to ensure the dialog is styled correctly.
		View rootView = View.inflate(getActivity(), R.layout.dialog_simple_confirm, null); 
				
		mTitleView = (TextView) rootView.findViewById(R.id.ibs_dialog_text_title);
		mContentView = (TextView) rootView.findViewById(R.id.ibs_dialog_text_content);
		
		mContainerView = rootView.findViewById(R.id.ibs_dialog_container);
		mProgressView = Utility.getProgressView(rootView);
		
		Bundle args = getArguments() ;
				
		if (savedInstanceState != null){
			mTitle = savedInstanceState.getString(ARG_TITLE);
			mContent = savedInstanceState.getString(ARG_CONTENT);
			
			mRequestCode = savedInstanceState.getInt(KEY_REQUEST_CODE);
		} else if (args != null) {			
			mTitle = args.getString(ARG_TITLE);
			mContent = args.getString(ARG_CONTENT);
		}
		if (mProgressView != null && mContent != null) {
			mProgressView.setVisibility(View.GONE);
		}
		
		checkAndSetText();
		
		findAndSetListener(rootView, R.id.ibs_dialog_button_confirm);
				
		return rootView;
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
	 * Attempts to update title & message.
	 * @param title The title to set
	 * @param message The message to set.
	 */
	public void updateContent(String title, String message) {
		mTitle = title;
		mContent = message;
		checkAndSetText();		
	}
	
	public void setRequestCode(int requestCode) {
		mRequestCode = requestCode;
		
		if (getTargetFragment() != null) {
			setTargetFragment(getTargetFragment(), mRequestCode);
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Sets text, and checks to see if all text is loaded. If not, shows progress bars. */
	private void checkAndSetText() {
		
		if (mTitle != null) {
			mTitleView.setText(mTitle);
		}
		if (mContent != null) {
			mContentView.setText(mContent);			
		}
		
		if (mProgressView == null || mContainerView == null) {
			//if either view is null, we cannot animate them.
			return;
		}

		//makes the spinner spin, only if necessary.
		if (mContent == null || mTitle == null) {
			Utility.switchFadeProgressViews(mProgressView, mContainerView, true); 
		} else {
			Utility.switchFadeProgressViews(mProgressView, mContainerView, false); 
		}
	}
	
	/** Finds the given view and sets the click listener if found. */ 
	private void findAndSetListener(View rootView, int id) {
		View view = rootView.findViewById(id);
		if (view != null) {
			view.setOnClickListener(this);
		}
	}
	
	/** Checks the target fragment and listener to see if anyone is listening, if so sends. */
	private void tryToSendMessage(int which) {
		final Fragment target = getTargetFragment();		
		if (mDialogListener != null &&  target == null){
			//only if we have a listener and target fragment.
			mDialogListener.onClick(getDialog(), which, mRequestCode);			
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
		dismiss();
		
		int which = 0;
		switch (v.getId()) {
		case R.id.ibs_dialog_button_confirm:
			which = DialogInterface.BUTTON_POSITIVE;			
			break;		
		}
		
		tryToSendMessage(which);
	}

	
	
	@Override
	public void onCancel(DialogInterface dialog) {	
		super.onCancel(dialog);
		tryToSendMessage(DialogInterface.BUTTON_NEGATIVE);
	}
	
	public static interface OnClickListener {
		public void onClick(DialogInterface dialog, int which, int requestCode);
	}
	
}
