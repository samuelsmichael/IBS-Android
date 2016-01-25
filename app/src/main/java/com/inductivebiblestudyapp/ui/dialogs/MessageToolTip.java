package com.inductivebiblestudyapp.ui.dialogs;

import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.util.Utility;

/**
 *
 * Based on original content from {@link SimpleTooltipDialog} (0.6.0-20150611).
 * 
 * Note: To use buttons with this, use pairs:
 * <ul>
 * <li><code>android.R.id.button1</code> == {@link DialogInterface#BUTTON_POSITIVE}</li>
 * <li><code>android.R.id.button2</code> == {@link DialogInterface#BUTTON_NEGATIVE} (or "Cancel")</li>
 * <li><code>android.R.id.button3</code> ==  {@link DialogInterface#BUTTON_NEUTRAL}</li>
 * </ul>
 * which will be pushed through {@link #onClick(View)}. 
 * Fragments can either setTarget (and use onActivityResult) or Activities can implement 
 * {@link DialogInterface#OnClickListener}
 * 
 * <br />
 * 
 * Creates a 250dp wide dialog by default.
 * 
 * @author Jason Jenkins
 * @version 0.3.3-20150619
 */
public class MessageToolTip extends SimpleTooltipDialog implements OnClickListener, UpdateDialogMessage {
	
	final static private String CLASS_NAME = MessageToolTip.class
			.getSimpleName();
	
	/** Argument key: Int. The layout id to use for the child content. 
	 * May be replaced with {@link #ARG_MESSAGE} */
	private static final String ARG_LAYOUT_ID = CLASS_NAME + ".ARG_LAYOUT_ID";
	
	/** Argument key: Int[]. The {width, height} of the view in pixels, whose width to use to determine the location of the arrow. */
	private static final String ARG_ANCHOR_VIEW_DIMEN = CLASS_NAME + ".ARG_ANCHOR_VIEW_DIMEN";
	
	/** Argument key: Int[]. The location of the view in pixels, whose width to use to determine the location of the arrow. */
	private static final String ARG_ANCHOR_LOCATION = CLASS_NAME + ".ARG_ANCHOR_LOCATION";
	
	/** Argument key: Boolean. true for centered, false for not.  */
	private static final String ARG_DIALOG_CENTERED = CLASS_NAME + ".centered";
	
	/** Argument key: Long (Optional). The width in dp to render the dialog. */
	private static final String ARG_DIALOG_WIDTH_DP = CLASS_NAME + ".ARG_DIALOG_WIDTH_DP";
	
	/** Argument key: Int (Optional). The string to build instead of a layout resource. 
	 * {@link #ARG_LAYOUT_ID} */
	private static final String ARG_MESSAGE = CLASS_NAME + ".ARG_MESSAGE";
	
	/** Argument key: Int (Optional). The page loader ID to transfer to the dialog.
	 * Content will be populated into android.R.id.text1. */
	@Deprecated
	private static final String ARG_PAGE_LOADER = CLASS_NAME + ".ARG_PAGE_LOADER";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End arg constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Builder used to build the simple tool tip.
	 * @author Jason Jenkins
	 * @version 0.2.0-20150611
	 */
	static public class Builder {
		final private Bundle args = new Bundle();
		
		private void extractDimenArgs(View anchor) {
			int[] anchorLocation = new int[2];
			anchor.getLocationOnScreen(anchorLocation);
			
			int[] anchorDimens = {
					anchor.getWidth(),
					anchor.getHeight()
			};
			args.putIntArray(ARG_ANCHOR_LOCATION, anchorLocation);
			args.putIntArray(ARG_ANCHOR_VIEW_DIMEN, anchorDimens);
		}
		
		/** To build the dialog using a layout & custom message. 
		 * The id of the custom message must be <code>android.R.id.text1</code>
		 * @param layoutId The layout id to insert within the white dialog box.
		 * @param anchor The visible view to create a tool tip for.
		 */
		public Builder(int layoutId, String message, View anchor) {			
			extractDimenArgs(anchor);
			
			args.putInt(ARG_LAYOUT_ID, layoutId);
			args.putString(ARG_MESSAGE, message);
			args.putBoolean(ARG_DIALOG_CENTERED, true);
		}
		
		
		/** To build the dialog using a layout. 
		 * @param layoutId The layout id to insert within the white dialog box.
		 * @param anchor The visible view to create a tool tip for.
		 */
		public Builder(int layoutId, View anchor) {			
			extractDimenArgs(anchor);
			
			args.putInt(ARG_LAYOUT_ID, layoutId);	
			args.putBoolean(ARG_DIALOG_CENTERED, true);
		}
		
		/** To build the dialog using a string. 
		 * @param message The message to insert into the dialog
		 * @param anchor The visible view to create a tool tip for.
		 */
		public Builder(String message, View anchor) {
			extractDimenArgs(anchor);
			
			args.putString(ARG_MESSAGE, message);
			args.putBoolean(ARG_DIALOG_CENTERED, true);
		}
		
		/**
		 * Removed for simpler approach.
		 */
		@Deprecated
		public Builder setPageLoader(String page) {
			args.putString(ARG_PAGE_LOADER, page);
			return this;
		}
		
		/**
		 * @param centered <code>true</code> to center the window, 
		 * <code>false</code> to move it. Note centering may have unintended effects.
		 */
		public Builder setCentered(boolean centered) {
			args.putBoolean(ARG_DIALOG_CENTERED, centered);
			return this;
		}
		
		/**
		 * @param dpWidth the option to specify the window size.
		 */
		public Builder setDpWidth(int width) {
			args.putLong(ARG_DIALOG_WIDTH_DP, width);
			return this;
		}
		
		/** Builds the tooltip. */
		public MessageToolTip build(){
			MessageToolTip toolTip = new MessageToolTip();

			toolTip.setArguments(args);
			return toolTip;
		}
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods here
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public MessageToolTip() {
		super();
	}
	
	/** The optional listener for the application. */
	private DialogInterface.OnClickListener mDialogListener = null;
	
	/** The page message. */
	private String mMessage = null;
	
	private TextView mMessageView = null;
	
	private View mProgressView = null;
	private View mContainerView = null;
	
	private int[] mAnchorDimens = new int[]{};
	private int[] mAnchorLocation = new int[]{};
	private boolean mCentered = false;

	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ARG_MESSAGE, mMessage);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null){
			mMessage = savedInstanceState.getString(ARG_MESSAGE);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = null;
		
		Bundle args = getArguments() ;
		
		
		Dialog dialog = getDialog(); 
		
		if (args != null) {			
			rootView = preProcessArgs(inflater, container, args);
			
			mAnchorDimens = args.getIntArray(ARG_ANCHOR_VIEW_DIMEN);
			mAnchorLocation = args.getIntArray(ARG_ANCHOR_LOCATION);
			mCentered = args.getBoolean(ARG_DIALOG_CENTERED);
			
			if (args.containsKey(ARG_DIALOG_WIDTH_DP)) {
				double dp = args.getLong(ARG_DIALOG_WIDTH_DP, -1);
				
				WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
				wmlp.width =  dpToPx(getResources(), dp);
				
			}
		}

		mDownArrow = rootView.findViewById(R.id.dialog_tooltip_arrowDown);
		mUpArrow = rootView.findViewById(R.id.dialog_tooltip_arrowUp);
		
		findAndSetListener(rootView, android.R.id.button1);
		findAndSetListener(rootView, android.R.id.button2);
		findAndSetListener(rootView, android.R.id.button3);
		
		//adjust views, just in time.
		adjustView(dialog, rootView, mAnchorLocation, mAnchorDimens, mCentered);
		
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
	
	@Override
	public void onPause() {
		super.onPause();
		dismiss();
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
	
	/** Initializing helper to build the view logic. */
	private View preProcessArgs(LayoutInflater inflater,
			ViewGroup container, Bundle args) {
		View rootView;
		int layoutId = args.getInt(ARG_LAYOUT_ID, -1);
		
		if (mMessage == null) {
			mMessage = args.getString(ARG_MESSAGE);
		}
		
		if (layoutId > 0) {
			rootView = inflater.inflate(R.layout.dialog_simple_tooltip_custom, container,
						false);
			mContainerView = View.inflate(getActivity(), layoutId, null);
			((FrameLayout) rootView.findViewById(R.id.dialog_tooltip_content)).addView(mContainerView);

			mMessageView = (TextView) rootView.findViewById(android.R.id.text1);
			
		} else {
			rootView = inflater.inflate(R.layout.dialog_simple_tooltip, container,
					false);
			mMessageView = (TextView) rootView.findViewById(android.R.id.text1);
			mContainerView = mMessageView;
		}
		
		mProgressView = Utility.getProgressView(rootView);
		
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
    
	/** Checks the target fragment and listener to see if anyone is listening, if so sends. */
    protected void tryToSendMessage(int which) {
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
		dismiss();
		
		int which = 0;
		switch (v.getId()) {
		case android.R.id.button1:
			which = DialogInterface.BUTTON_POSITIVE;			
			break;
		case android.R.id.button2:
			which = DialogInterface.BUTTON_NEGATIVE;			
			break;
		case android.R.id.button3:
			which = DialogInterface.BUTTON_NEUTRAL;			
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