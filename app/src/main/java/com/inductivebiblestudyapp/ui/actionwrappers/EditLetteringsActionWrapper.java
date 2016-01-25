package com.inductivebiblestudyapp.ui.actionwrappers;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.data.service.LetteringService;
import com.inductivebiblestudyapp.ui.adapters.CustomMarkingButtonAdapter;
import com.inductivebiblestudyapp.ui.adapters.LetteringFetchRecyclerAdapter;
import com.inductivebiblestudyapp.ui.adapters.LetteringFetchRecyclerAdapter.OnLetteringItemClickListener;
import com.inductivebiblestudyapp.ui.dialogs.ColorPickerDialog;
import com.inductivebiblestudyapp.util.Utility;

/**
* Action wrapper used to consolidate the actions for creating a lettering with 
* a grid view & the {@link CustomMarkingButtonAdapter}.
* 
* Used to contain and reuse the methods for lettering creation & editing. 
* @author Jason Jenkins
* @version 0.3.1-20150806
*
*/
public class EditLetteringsActionWrapper implements OnClickListener, OnItemClickListener, 
	OnLetteringItemClickListener {
	
	final static private String CLASS_NAME = EditLetteringsActionWrapper.class
			.getSimpleName();	
	private static final String LOGTAG = CLASS_NAME;
	

	public static final String TAG_COLORPICKER_HIGHLIGHT = CLASS_NAME + ".TAG_COLORPICKER_HIGHLIGHT";
	public static final String TAG_COLORPICKER_TEXT = CLASS_NAME + ".TAG_COLORPICKER_TEXT";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private final FragmentActivity mActivity;

	private final OnActionListener mListener;
	

	private final RecyclerView mMarkingLibaryView; 
	private final GridView mMarkingsGridView;
	private final EditText mLetteringName;
	
	private final View mSaveButton;
	private final View mCancelButton;
	
	private final String mAccessToken;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End final members
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Can be <code>null</code> */
	private LetteringItem mLetteringItem = null;
	
	private int mBold = 0;
	private int mBox = 0;
	private int mDoubleBox = 0;
	private int mItalics = 0;
	private int mStrike = 0;
	private int mUnderline = 0;
	private int mDoubleUnderline = 0;
	private String mTextColor = "0";
	private String mBackgroundColor = "0";
	
	public EditLetteringsActionWrapper(FragmentActivity activity, RecyclerView libraryRecyclerView, 
			GridView buttonGridView, EditText letteringNameInput, 
			View saveButton, View cancelButton, OnActionListener listener) {
		mActivity = activity;
		
		mMarkingLibaryView = libraryRecyclerView;
		mMarkingLibaryView.setHasFixedSize(true);
        
        // use a horizontal linear layout manager
        LinearLayoutManager markingsContainer  = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mMarkingLibaryView.setLayoutManager(markingsContainer);
        
        LetteringFetchRecyclerAdapter mLibraryAdapter = new LetteringFetchRecyclerAdapter(getActivity(), true);
        mLibraryAdapter.setOnLetteringItemClickListener(this);
        mMarkingLibaryView.setAdapter(mLibraryAdapter);
		
		mMarkingsGridView = buttonGridView;
		mMarkingsGridView.setAdapter(new CustomMarkingButtonAdapter(activity));
		mMarkingsGridView.setOnItemClickListener(this);
		
		mLetteringName = letteringNameInput;
		
		mSaveButton = saveButton;
		mSaveButton.setOnClickListener(this);
		mCancelButton = cancelButton;
		mCancelButton.setOnClickListener(this);
		
		mListener = listener;
		
		mAccessToken = new CurrentUser(mActivity).getIBSAccessToken();
		
		reattachColorDialogs();
	}
	
	/** @param letteringItem The lettering to edit. Can be <code>null</code> */
	public void setLetteringItem(LetteringItem letteringItem) {
		this.mLetteringItem = letteringItem;
		populateLettering(letteringItem);
	}
	
	/** @param letteringItem The lettering to copy but NOT edit.  */
	public void copyLetteringItem(LetteringItem letteringItem) {
		populateLettering(letteringItem);
	}
	
	/** Enables or disables all views. */
	public void enableViews(boolean enabled) {
		mSaveButton.setEnabled(enabled);
		mCancelButton.setEnabled(enabled);
		
		mMarkingsGridView.setEnabled(enabled);
		mLetteringName.setEnabled(enabled);
		
		mListener.enableViews(enabled);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// wrapper methods
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Convenience for migration and habit. */
	private Activity getActivity() {
		return mActivity;
	}
	
	/** Convenience for migration and habit. */
	private String getString(int resId) {
		return mActivity.getString(resId);
	}
	
	/** Convenience for migration and habit. */
	private FragmentManager getFragmentManager() {
		return mActivity.getSupportFragmentManager();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Re-attaches any color dialogs and resets their listeners.
	 */
	private void reattachColorDialogs() {
		ColorPickerDialog textPicker = 
				(ColorPickerDialog) getFragmentManager().findFragmentByTag(TAG_COLORPICKER_TEXT);
		if (textPicker != null) {
			textPicker.setOnColorPickedListener(mTextColorListener);
					
		}		
		
		ColorPickerDialog highlightPicker = 
				(ColorPickerDialog) getFragmentManager().findFragmentByTag(TAG_COLORPICKER_HIGHLIGHT);
		if (highlightPicker != null) {
			highlightPicker.setOnColorPickedListener(mHighlightColorListener);
					
		}		
	}
	
	/** Converts boolean to 1 or 0. */
	private static int toInt(final boolean isChecked) {
		return isChecked ? 1 : 0;
	}
	
	/** Populates the lettering grid view based on the lettering item. */
	private void populateLettering(LetteringItem lettering) {
		if (lettering == null) {
			return;
		}
		
		if (mLetteringName.getText().toString().isEmpty()) {
			mLetteringName.setText(lettering.getName());
		}

		mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_BOLD, lettering.getBold());
		mBold = toInt(lettering.getBold());
		mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_ITALICS, lettering.getItalics());
		mItalics = toInt(lettering.getItalics());
		mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_STRIKE, lettering.getStrikethrough());
		mStrike = toInt(lettering.getStrikethrough());
		
		mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_UNDERLINE, lettering.getUnderline());
		mUnderline = toInt(lettering.getUnderline());
		mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_UNDERLINE_DOUBLE, lettering.getDoubleUnderline());
		mDoubleUnderline = toInt(lettering.getDoubleUnderline());
		mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_BOX, lettering.getBoxed());
		mBox = toInt(lettering.getBoxed());
		mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_BOX_DOUBLE, lettering.getDoubleBoxed());
		mDoubleBox = toInt(lettering.getDoubleBoxed());
		
		if (!lettering.getFontColor().isEmpty()) {
			mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_COLOR, true);
			mTextColor = lettering.getFontColor();			
		} else {
			mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_COLOR, false);
		}
		
		if (!lettering.getBackgroundColor().isEmpty()) {
			mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_HIGHLIGHT, true);
			mBackgroundColor = lettering.getBackgroundColor();			
		} else {
			mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_HIGHLIGHT, false);
		}
	}
	
	/** Performs the save of the given lettering. Whether add or edit. */
	private void saveLettering() {		
		mLetteringName.setError(null);
		final String letteringName = mLetteringName.getText().toString().trim();
		
		if (letteringName.isEmpty()) {
			mLetteringName.setError(getString(R.string.ibs_error_cannotBeEmpty));
			return;
		} 
		
		enableViews(false);
		
		final String accessToken = 	mAccessToken;
		final LetteringService service = RestClient.getInstance().getLetteringService();
		
		if (mLetteringItem == null){ //only if creating new 
			service.create(
					accessToken, 
					letteringName, 
					mBold, mItalics, mUnderline, mStrike, 
					mDoubleUnderline, mBox, mDoubleBox, mTextColor, mBackgroundColor, 
					mLetteringCallback);
		} else {
			service.edit(	
					accessToken,
					mLetteringItem.getId(),
					letteringName, 
					mBold, mItalics, mUnderline, mStrike, 
					mDoubleUnderline, mBox, mDoubleBox, mTextColor, mBackgroundColor, 
					mLetteringCallback);
		}
	}
	
	/** The method to handle actions when changes are saved.
	 * @param letteringId The created id, may be <code>null</code> */
	/*default*/ void changesSaved(String letteringId) {
		Utility.toastMessage(getActivity(), getString(R.string.ibs_text_changesSaved));
		AppCache.setLetteringListResponse(null); //invalidate cache
		
		if (letteringId == null && mLetteringItem != null) {
			letteringId = mLetteringItem.getId();
		}
		
		if (letteringId != null) {
			RestClient.getInstance().getLetteringService()
				.get(mAccessToken, letteringId, 
					new Callback<LetteringItem>() {
							@Override
							public void success(LetteringItem item, Response arg1) {
								mListener.onSave(item);
								if (item == null) {
									Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
								} else {
									mLetteringItem = item;
								}
							}
							
							@Override
							public void failure(RetrofitError arg0) {
								mListener.onSave(null);
								Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
							}
						});
		} else {
			Log.w(LOGTAG, "Unusual behaviour; we're not sure what we created here");
			mListener.onSave(null);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private Callback<UpdateResult> mLetteringCallback = new Callback<UpdateResult>() {
		
		@Override
		public void success(UpdateResult result, Response arg1) {
			enableViews(true);
			if (result == null) {
				Log.d(LOGTAG, "Failed with null");

				Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
				return;
			}
			Log.d(LOGTAG, "success: " + result.isSuccessful());
			
			changesSaved(result.getInsertId());
		}

		
		@Override
		public void failure(RetrofitError arg0) {
			Log.d(LOGTAG, "Failed.");	
			
			Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
			enableViews(true);
		}
	};
	
	private ColorPickerDialog.OnColorPickedListener mHighlightColorListener = 
			new ColorPickerDialog.OnColorPickedListener() {
		
		@Override
		public void onColorPicked(int color) {
			mBackgroundColor = "#"+Integer.toHexString(color);
		}
		
		@Override
		public void onCancel(){			
			mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_HIGHLIGHT, false);
		}
	};
	
	private ColorPickerDialog.OnColorPickedListener mTextColorListener = 
			new ColorPickerDialog.OnColorPickedListener() {
		
		@Override
		public void onColorPicked(int color) {
			mTextColor = "#"+Integer.toHexString(color);
		}
		
		@Override
		public void onCancel(){
			mMarkingsGridView.setItemChecked(CustomMarkingButtonAdapter.ID_COLOR, false);
		}
	};
	
	@Override
	public void onClick(View v) {
		if (mSaveButton.equals(v)) {
			saveLettering();
			
		} else if (mCancelButton.equals(v)) {
			enableViews(true);
			mListener.onCancel();
		}
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final boolean isChecked = mMarkingsGridView.isItemChecked(position); 
		
		switch (position) {
		case CustomMarkingButtonAdapter.ID_BOLD:
			mBold = toInt(isChecked);
			break;
		case CustomMarkingButtonAdapter.ID_BOX:
			mBox = toInt(isChecked);
			break;
		case CustomMarkingButtonAdapter.ID_BOX_DOUBLE:
			mDoubleBox = toInt(isChecked);
			break;
		case CustomMarkingButtonAdapter.ID_ITALICS:
			mItalics = toInt(isChecked);
			break;
		case CustomMarkingButtonAdapter.ID_STRIKE:
			mStrike = toInt(isChecked);
			break;
		case CustomMarkingButtonAdapter.ID_UNDERLINE:
			mUnderline = toInt(isChecked);
			break;
		case CustomMarkingButtonAdapter.ID_UNDERLINE_DOUBLE:
			mDoubleUnderline = toInt(isChecked);
			break;
		
		case CustomMarkingButtonAdapter.ID_COLOR:
			if (isChecked) {
				ColorPickerDialog textDialog = 
						ColorPickerDialog.newInstance(getString(R.string.ibs_title_pickTextColor));
				textDialog.setOnColorPickedListener(mTextColorListener);
				textDialog.show(getFragmentManager(), TAG_COLORPICKER_TEXT);

				return;
			} else {
				mTextColor = "0";
			}
			break;
			
			
		case CustomMarkingButtonAdapter.ID_HIGHLIGHT:
			if (isChecked) {
				ColorPickerDialog highlightDialog = 
						ColorPickerDialog.newInstance(getString(R.string.ibs_title_pickHighlightColor));
				highlightDialog.setOnColorPickedListener(mHighlightColorListener);
				highlightDialog.show(getFragmentManager(), TAG_COLORPICKER_HIGHLIGHT);

				return;
				
			} else {
				mBackgroundColor = "0";
			}			
			
		default:
			Log.d(CLASS_NAME, "Oops! We forgot to check this position:" + position);
			break;
		}		

	}
	

	@Override
	public void onLetteringItemClick(LetteringItem item) {
		copyLetteringItem(item);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public interface
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.2.0-20150715 */
	public static interface OnActionListener {
		/** @param item The lettering being save. May be <code>null</code>
		 * in unusual circumstances
		 */
		public void onSave(LetteringItem item);
		public void onCancel();
		public void enableViews(boolean enabled);
	}

	
}
