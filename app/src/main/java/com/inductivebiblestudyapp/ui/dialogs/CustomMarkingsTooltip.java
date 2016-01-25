package com.inductivebiblestudyapp.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager;
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.ui.OnUpgradeRequestListener;
import com.inductivebiblestudyapp.ui.adapters.ImageFetchRecyclerAdapter;
import com.inductivebiblestudyapp.ui.adapters.ImageFetchRecyclerAdapter.OnImageItemClickListener;
import com.inductivebiblestudyapp.ui.adapters.LetteringFetchRecyclerAdapter;
import com.inductivebiblestudyapp.ui.adapters.LetteringFetchRecyclerAdapter.OnAdapterStateListener;
import com.inductivebiblestudyapp.ui.adapters.LetteringFetchRecyclerAdapter.OnLetteringItemClickListener;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A somewhat complex Custom Markings tool tip to toggle views, show controls, launch dialogs
 *  and try to point at text.
 * 
 * @author Jason Jenkins
 * @version 0.7.0-20150921
 * 
 *  */
public class CustomMarkingsTooltip extends SimpleTooltipDialog implements OnClickListener, 
	OnImageItemClickListener, OnLetteringItemClickListener, OnCheckedChangeListener {
	
	final static private String CLASS_NAME = CustomMarkingsTooltip.class
			.getSimpleName();
	
	/** Argument key: Int[]. The {width, height} of the view in pixels, whose width to use to determine the location of the arrow. */
	private static final String ARG_ANCHOR_VIEW_DIMEN = CLASS_NAME + ".ARG_ANCHOR_VIEW_DIMEN";
	
	/** Argument key: Int[]. The location of the view in pixels, whose width to use to determine the location of the arrow. */
	private static final String ARG_ANCHOR_LOCATION = CLASS_NAME + ".ARG_ANCHOR_LOCATION";
	
	
	private static final String TAG_LETTERING_DIALOG = CLASS_NAME + ".TAG_LETTERING_DIALOG";
	
	private static int REQUEST_IMAGE_DIALOG_ACTIVITY = 0x200;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public CustomMarkingsTooltip() {}
	
	public static CustomMarkingsTooltip newInstance(TextView anchor) {
		CustomMarkingsTooltip tooltip = new CustomMarkingsTooltip();
		Bundle args = new Bundle();
		
		//see: http://stackoverflow.com/questions/5044342/how-to-get-cursor-position-x-y-in-edittext-android
		
		int pos = anchor.getSelectionStart();
		Layout layout = anchor.getLayout();
		int line = layout.getLineForOffset(pos);
		int baseline = layout.getLineBaseline(line);
		int ascent = layout.getLineAscent(line);
		float x = layout.getPrimaryHorizontal(pos);
		float y = baseline + ascent;
		
 		
 		int[] anchorLocation = new int[2];
		anchor.getLocationOnScreen(anchorLocation);
		
		anchorLocation[0] += x;
		anchorLocation[1] += y;
		
		int[] anchorDimens = {
				1,
				anchor.getLineHeight()
		};
 		
		
		args.putIntArray(ARG_ANCHOR_LOCATION, anchorLocation);
		args.putIntArray(ARG_ANCHOR_VIEW_DIMEN, anchorDimens);
		
		tooltip.setArguments(args);
		return tooltip;
	}
	
	public static CustomMarkingsTooltip newInstance(View anchor) {
		CustomMarkingsTooltip tooltip = new CustomMarkingsTooltip();
		Bundle args = new Bundle();
		
		int[] anchorLocation = new int[2];
		anchor.getLocationOnScreen(anchorLocation);
		
		int[] anchorDimens = {
				anchor.getWidth(),
				anchor.getHeight()
		};
		args.putIntArray(ARG_ANCHOR_LOCATION, anchorLocation);
		args.putIntArray(ARG_ANCHOR_VIEW_DIMEN, anchorDimens);
		
		tooltip.setArguments(args);
		return tooltip;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End Factory methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private View mExistingContainer = null;
	private View mCreateContainer = null;
	
	private RecyclerView mImageRecycleView = null;
	private RecyclerView mLetteringRecycleView = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// end views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private ImageFetchRecyclerAdapter mImageAdapter = null;
	private LetteringFetchRecyclerAdapter mLetteringAdapter = null; 
	
	private int mImageViewHeight = 0;
	private int mLetteringViewHeight = 0;
	
	private int[] anchorDimens = new int[]{};
	private int[] anchorLocation = new int[]{};
	private boolean centered = false;
	
	private OnMarkingSetListener mListener = null;
	
	/** Set in {@link #queueForDismiss()} & 
	 * used by {@link #onActivityCreated(Bundle)} to queue the dialog for dismissal */
	private boolean mDismissThisDialog = false;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.dialog_custom_markings_tooltip, container,
				false);
		
		Bundle args = getArguments() ;
				
		Dialog dialog = getDialog(); 
		
		if (args != null) {
			
			anchorDimens = args.getIntArray(ARG_ANCHOR_VIEW_DIMEN);
			anchorLocation = args.getIntArray(ARG_ANCHOR_LOCATION);
			
		}
		mDownArrow = rootView.findViewById(R.id.dialog_tooltip_arrowDown);
		mUpArrow = rootView.findViewById(R.id.dialog_tooltip_arrowUp);
		
		mExistingContainer = rootView.findViewById(R.id.dialog_customMarking_tooltip_existing_container);
		mCreateContainer = rootView.findViewById(R.id.dialog_customMarking_tooltip_create_container);
		
		initLetteringView(rootView);        
        initImageView(rootView);
		
		Utility.setOnClickAndReturnView(rootView, R.id.dialog_customMarking_button_create_image, this);
		Utility.setOnClickAndReturnView(rootView, R.id.dialog_customMarking_button_create_lettering, this);
		
		RadioGroup sortBy = (RadioGroup) rootView.findViewById(R.id.ibs_sortBy_radiogroup);
		sortBy.setOnCheckedChangeListener(this);
		onCheckedChanged(sortBy, sortBy.getCheckedRadioButtonId());
		
		findAndSetListener(rootView, R.id.dialog_customMarking_tooltip_button_existing);
		findAndSetListener(rootView, R.id.dialog_customMarking_tooltip_button_create);
		
		//Default configuration as requested; always show existing container on load
		toggleVisibility(mExistingContainer, mCreateContainer);
		
		//adjust views, just in time.
		adjustView(dialog, rootView, anchorLocation, anchorDimens, centered);
		
		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mImageAdapter.clear();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		CreateMarkingDialog dialog2 = (CreateMarkingDialog) getFragmentManager().findFragmentByTag(TAG_LETTERING_DIALOG);
		if (dialog2 != null) {
			dialog2.setOnLetteringCreateListener(mLetteringCreateListener);
		}
	}
	
	@Override
	public void dismiss() {
		super.dismiss();
		mDismissThisDialog = false;
		/*
		 * We may have an activity without a parent dialog, we're not sure.
		 */		
		try {
			DialogFragment dialog  = (DialogFragment) getFragmentManager().findFragmentByTag(TAG_LETTERING_DIALOG);
			if (dialog != null) {
				dialog.dismiss(); //so we don't try to track without the parent
			}
			
		} catch (Exception e) {
			Log.e(CLASS_NAME, "Unexpected exception: " + e);
		}
	}
	
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		if (mDismissThisDialog) {
			try {
				dismiss();
			} catch (Exception e) { //extra redundant safety
				Log.d(CLASS_NAME, "Still failed to dismiss, waiting a little longer: " + e);
				new Handler().postDelayed(new Runnable() {					
					@Override
					public void run() {
						dismiss(); //if this fails, we really need to know it failed.
					}
				}, 100);
			}
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQUEST_IMAGE_DIALOG_ACTIVITY) {
			switch (resultCode) {
			case Activity.RESULT_OK:
				Bundle extras = data.getExtras();
				ImageItem item = extras.getParcelable(CreateImageDialogActivity.EXTRA_RESULT_IMAGE);
				onImageItemClick(item);
				break;

			default:
				queueForDismiss();
				break;
			}
		}
	}
	
	/**
	 * WARNING! You will need to re-attach an manage this on rotations.
	 * @param listener
	 */
	public void setOnMarkingSetListener(OnMarkingSetListener mListener) {
		this.mListener = mListener;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper & Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes the image horizontal view. */
	private void initImageView(View rootView) {
		mImageRecycleView = (RecyclerView) rootView.findViewById(R.id.dialog_customMarking_tooltip_image_library_recyclerView);
        mImageRecycleView.setHasFixedSize(true);       
        mImageViewHeight = mImageRecycleView.getLayoutParams().height;

        // use a horizontal linear layout manager
        LinearLayoutManager imgContainer  = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);        
        mImageRecycleView.setLayoutManager(imgContainer);
        mImageRecycleView.setItemAnimator(new DefaultItemAnimator());

		mImageAdapter = new ImageFetchRecyclerAdapter(getActivity());
        mImageRecycleView.setAdapter(mImageAdapter);        

		mImageAdapter.setOnImageItemClickListener(this);
		mImageAdapter.setOnAdapterStateListener(mImageStateListener);
	}

	/** Initializes the lettering horizontal view. */
	private void initLetteringView(View rootView) {
		mLetteringRecycleView = (RecyclerView) rootView.findViewById(R.id.dialog_customMarking_tooltip_lettering_library_recyclerView);
        mLetteringRecycleView.setHasFixedSize(true);
        mLetteringViewHeight = mLetteringRecycleView.getLayoutParams().height; 
        
        // use a horizontal linear layout manager
        LinearLayoutManager markingsContainer  = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mLetteringRecycleView.setLayoutManager(markingsContainer);
        mLetteringRecycleView.setItemAnimator(new DefaultItemAnimator());
        
        mLetteringAdapter = new LetteringFetchRecyclerAdapter(getActivity());
        mLetteringRecycleView.setAdapter(mLetteringAdapter);
        
        mLetteringAdapter.setOnLetteringItemClickListener(this);
        mLetteringAdapter.setOnAdapterStateListener(mLetteringStateListener);
	}
	
	/*
	 * Why use this in favour of expandable views? Namely because it came together 
	 * when 3 views were being toggled between. The 3rd view is gone (as is its arg)
	 * so it could be transitioned, but why bother?
	 */
	
	/** Toggles the visibility of the first view while hiding the second. */ 
	private static void toggleVisibility(View toggle, View hide) {
		boolean toggleVisible = toggle.getVisibility() == View.VISIBLE;
		toggle.setVisibility(toggleVisible ? View.GONE : View.VISIBLE);
		hide.setVisibility(View.GONE);		
	}
	
	/** Attempts to dismiss the dialog immediately, and if that fails
	 * queues dismiss() for {@link #onActivityCreated(Bundle)} */
	private void queueForDismiss() {
		try {
			dismiss(); //attempt to dismiss. 
		} catch (Exception e) {
			Log.d(CLASS_NAME, "Failed to dismiss, waiting for activity: " + e);
			mDismissThisDialog = true;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if (mListener != null) {
			mListener.onTooltipCancel();
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.ibs_sortBy_radioOption_byName:
			mImageAdapter.setSortMode(ImageFetchRecyclerAdapter.SORT_NAME_ASC);
			mLetteringAdapter.setSortMode(LetteringFetchRecyclerAdapter.SORT_NAME_ASC);
			break;
			
		case R.id.ibs_sortBy_radioOption_byRecent:
			mImageAdapter.setSortMode(ImageFetchRecyclerAdapter.SORT_RECENT_ASC);
			mLetteringAdapter.setSortMode(LetteringFetchRecyclerAdapter.SORT_RECENT_ASC);
			break;

		default:
			Log.w(CLASS_NAME, "Unspecified action: " + checkedId);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.dialog_customMarking_button_create_image:
		
		final boolean upgradeComplete = UpgradeBillingManager.isUpgradeComplete(getActivity()); 
		if (upgradeComplete) { 
			
			Intent createImageIntent = new Intent(getActivity(), CreateImageDialogActivity.class);
			getActivity().startActivityForResult(createImageIntent, REQUEST_IMAGE_DIALOG_ACTIVITY);
			
		} else {
			((OnUpgradeRequestListener) getActivity()).requestUpgrade();
		}
			break;
			
		case R.id.dialog_customMarking_button_create_lettering:
			CreateMarkingDialog letteringDialog = CreateMarkingDialog.newInstance();
			letteringDialog.setOnLetteringCreateListener(mLetteringCreateListener);
			letteringDialog.show(getFragmentManager(), TAG_LETTERING_DIALOG);
			break;
		
		case R.id.dialog_customMarking_tooltip_button_existing:
			toggleVisibility(mExistingContainer, mCreateContainer);
			break;

		case R.id.dialog_customMarking_tooltip_button_create:
			toggleVisibility(mCreateContainer, mExistingContainer);
			break;
			
		default:
			break;
		}
		
		adjustView(getDialog(), getView(), anchorLocation, anchorDimens, centered);
	}

	private CreateMarkingDialog.OnLetteringCreateListener mLetteringCreateListener = new CreateMarkingDialog.OnLetteringCreateListener() {
		
		@Override
		public void onLetterngItemCreate(LetteringItem item) {
			DialogFragment dialog = (DialogFragment) getFragmentManager().findFragmentByTag(TAG_LETTERING_DIALOG);
			if (dialog != null) {
				dialog.dismiss(); 
			}
			onLetteringItemClick(item);
		}
		
		@Override
		public void onCancel() {
			dismiss();
		}
	};
	
	

	@Override
	public void onLetteringItemClick(LetteringItem item) {
		Log.d(CLASS_NAME, "Lettering item: " + item);
		if (mListener == null) {
			Log.w(getTag(), "Cannot find listener");
		}
		mListener.onLetteringSet(item);
		dismiss();
	}

	@Override
	public void onImageItemClick(ImageItem item) {
		Log.d(CLASS_NAME, "Image item: " + item);
		
		final boolean upgradeComplete = UpgradeBillingManager.isUpgradeComplete(getActivity()); 
		if (upgradeComplete) {
			
			if (mListener == null) {
				Log.w(getTag(), "Cannot find listener");
			}
			mListener.onImageSet(item);
			queueForDismiss();
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					dismiss();
				}
			}, 50);
		
		} else {
			((OnUpgradeRequestListener) getActivity()).requestUpgrade();
		}
	}

	
	private LetteringFetchRecyclerAdapter.OnAdapterStateListener mLetteringStateListener =
			new LetteringFetchRecyclerAdapter.OnAdapterStateListener() {
				
				@Override
				public void onStateUpdate(int state) {
					switch (state) {
					case OnAdapterStateListener.STATE_OK:
						mLetteringRecycleView.getLayoutParams().height = mLetteringViewHeight;
						break;
					case OnAdapterStateListener.STATE_EMPTY:			
					case OnAdapterStateListener.STATE_CANNOT_CONNECT:
						mLetteringRecycleView.getLayoutParams().height = mLetteringViewHeight/2;
						break;

					}
				}
			};
			
	private ImageFetchRecyclerAdapter.OnAdapterStateListener mImageStateListener =
			new ImageFetchRecyclerAdapter.OnAdapterStateListener() {
				
				@Override
				public void onStateUpdate(int state) {
					switch (state) {
					case OnAdapterStateListener.STATE_OK:
						mImageRecycleView.getLayoutParams().height = mImageViewHeight;
						break;
					case OnAdapterStateListener.STATE_EMPTY:			
					case OnAdapterStateListener.STATE_CANNOT_CONNECT:
						mImageRecycleView.getLayoutParams().height = mImageViewHeight/2;
						break;

					}
				}
			};

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.2.0-20150806 */
	public static interface OnMarkingSetListener {
		public void onLetteringSet(LetteringItem item);
		public void onImageSet(ImageItem item);
		public void onTooltipCancel();
	}

}
