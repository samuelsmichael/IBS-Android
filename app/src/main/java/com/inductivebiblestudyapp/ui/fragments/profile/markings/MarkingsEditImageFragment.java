package com.inductivebiblestudyapp.ui.fragments.profile.markings;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.ui.OnBackButtonListener;
import com.inductivebiblestudyapp.ui.actionwrappers.EditImageActionWrapper;
import com.inductivebiblestudyapp.ui.dialogs.SimpleYesNoDialog;
import com.inductivebiblestudyapp.ui.viewstub.ViewStubManager;
import com.inductivebiblestudyapp.util.BitmapRecycler;
import com.inductivebiblestudyapp.util.DialogStateHolder;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A simple {@link Fragment} subclass. Use the {@link MarkingsEditImageFragment#newInstance}
 * factory method to create an instance of this fragment.
 * 
 * Requires parent to consider it to for {@link OnBackButtonListener}.
 * @version 0.5.7-20150831
 */
public class MarkingsEditImageFragment extends Fragment implements OnClickListener, 
	OnBackButtonListener, LoaderManager.LoaderCallbacks<ContentResponse>, 
	EditImageActionWrapper.OnActionListener {
	
	final static private String CLASS_NAME = MarkingsEditImageFragment.class
			.getSimpleName();
	
	private static final String LOGTAG = CLASS_NAME;
	
	private static final String ARG_IMAGE_ITEM = CLASS_NAME + ".ARG_IMAGE_ITEM";
	
	private static final String TAG_REMOVE_DIALOG = CLASS_NAME + ".TAG_REMOVE_DIALOG";
	
	
	private static final String KEY_EDIT_MODE = CLASS_NAME + ".KEY_EDIT_MODE";
	
	/** Bundle key: string. The current path of the selected image. */
	private static final String KEY_IMAGE_PATH = CLASS_NAME + ".KEY_IMAGE_PATH";
	
	private static final String KEY_DIALOG_STATE = CLASS_NAME + ".KEY_DIALOG_STATE";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start request codes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static final int REQUEST_DELETE_DIALOG = 1;
	
	private static final int REQUEST_REMOVE_DIALOG_LOADER = 0;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start state codes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static final int STATE_CREATE_NEW = 0;
	private static final int STATE_EDIT_EXISTING = 1;
	private static final int STATE_VIEW_EXISTING = 2;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * @param editMode Whether to start in edit mode. <code>true</code> for edit.
	 */
	public static MarkingsEditImageFragment newInstance() {
		MarkingsEditImageFragment fragment = new MarkingsEditImageFragment();
		return fragment;
	}
	

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * @param imageItem The image item to start editing.
	 */
	public static MarkingsEditImageFragment newInstance(ImageItem imageItem) {
		MarkingsEditImageFragment fragment = new MarkingsEditImageFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_IMAGE_ITEM, imageItem);
		fragment.setArguments(args);
		return fragment;
	}


	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public MarkingsEditImageFragment() {
		// Required empty public constructor
	}
	
	private View mDisplayView = null;
	private View mEditView = null;
	
	private ImageView mDisplayImage = null;
	private ImageView mEditImage = null;
	
	private View mEditProgressView = null;
	
	private EditText mEditImageName = null;
	private TextView mDisplayImageName = null;
	
	private View mSaveButton = null;
	private View mCancelButton = null;
	private View mEditButton = null;
	private View mDeleteButton = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private final BitmapRecycler mBitmapRecycler = new BitmapRecycler();
	
	/** Used to contain and reuse the methods for image creation, editing and deletion. */
	private EditImageActionWrapper mImageActionWrapper = null;
	
	/** Can either be a file or url path. */
	private String mImagePath = null;
	
	private DialogStateHolder mDialogState = new DialogStateHolder();	
	
	private OnInteractionListener mListener = null;
	
	private ImageItem mImageItem = null;
	
	
	/** The current view state of the fragment. states are:
	 * {@link #STATE_CREATE_NEW}, {@link #STATE_EDIT_EXISTING}, {@link #STATE_VIEW_EXISTING}.
	 * Default: {@value #STATE_VIEW_EXISTING}.
	 */
	private int mEditState = STATE_VIEW_EXISTING;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_EDIT_MODE, mEditState);
		outState.putString(KEY_IMAGE_PATH, mImagePath);
		outState.putParcelable(ARG_IMAGE_ITEM, mImageItem);
		outState.putParcelable(KEY_DIALOG_STATE, mDialogState);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) { //restore as early as possible
			mEditState = savedInstanceState.getInt(KEY_EDIT_MODE);
			mImagePath = savedInstanceState.getString(KEY_IMAGE_PATH);			
			mImageItem = savedInstanceState.getParcelable(ARG_IMAGE_ITEM);
			mDialogState = savedInstanceState.getParcelable(KEY_DIALOG_STATE);			
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View  rootView = inflater.inflate(R.layout.fragment_markings_edit_view_image, container, false);
				
		initViews(rootView);  
		
		mImageActionWrapper = 
				new EditImageActionWrapper(getActivity(), mEditView, mEditImageName, mSaveButton, mCancelButton, this);
				
		TextView mEditTitle = (TextView) mEditView.findViewById(R.id.ibs_markings_editimg_title);
		
		String editTitle = getString(R.string.ibs_title_add_image);
		String name = "";
		Bundle args = getArguments();
		
		if (savedInstanceState == null && args != null){
			mImageItem = args.getParcelable(ARG_IMAGE_ITEM);
			mEditState = STATE_VIEW_EXISTING; //unless otherwise stated, we are viewing an item
			
		} else {
			mEditState = STATE_CREATE_NEW;
		}
		
		if (mImageItem != null) {
			if (mImagePath == null) {
				mImagePath = mImageItem.getUrl();
			}
			name = mImageItem.getName();
			editTitle = getString(R.string.ibs_title_edit_image);
			
			mImageActionWrapper.setImageItem(mImageItem);
		}
		if (!TextUtils.isEmpty(mImagePath)) {
			mImageActionWrapper.setImagePath(mImagePath);
		}

		mEditTitle.setText(editTitle);
		mDisplayImageName.setText(name);
		if (mEditState == STATE_VIEW_EXISTING) { //if we are editing, we may have a new name
			mEditImageName.setText(name);
		}
		
		mImageActionWrapper.loadImage();
		forceEditMode(mEditState);		

		if (!mDialogState.fetched) {
			getLoaderManager().initLoader(REQUEST_REMOVE_DIALOG_LOADER, null, this);
		}
		
		// Inflate the layout for this fragment
		return rootView;
	}

	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mImageActionWrapper != null) {
			mImageActionWrapper.destroy();
		}
		mBitmapRecycler.recycleAll();
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		try {
			
			mListener = (OnInteractionListener) getParentFragment();
		} catch (ClassCastException e) {
			Log.e(getTag(), "Parent fragment must implement " + OnInteractionListener.class.getName());
			throw e;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {	
		super.onActivityResult(requestCode, resultCode, data);
		final String accessToken = new CurrentUser(getActivity()).getIBSAccessToken();
		
		switch (requestCode) {
		case REQUEST_DELETE_DIALOG:
			
			if (DialogInterface.BUTTON_POSITIVE == resultCode && mImageItem != null) {
				RestClient.getInstance().getImageService().delete(accessToken, mImageItem.getId(),
						new Callback<UpdateResult>() {
							
							@Override
							public void success(UpdateResult arg0, Response arg1) {
								if (arg0 != null && arg0.isSuccessful()) {
									AppCache.setImageListResponse(null); //invalidate cache
									Utility.toastMessage(getActivity(), getString(R.string.ibs_text_deleteSuccess_image));
									mListener.onImageRequestPop();
								} else {
									Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
								}
								dismissDialog(TAG_REMOVE_DIALOG);
							}
							
							@Override
							public void failure(RetrofitError arg0) {
								Log.d(LOGTAG, "Failed for reasons");
								Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
								dismissDialog(TAG_REMOVE_DIALOG);
							}
						}
				);
			} else {
				dismissDialog(TAG_REMOVE_DIALOG);
			}
			break;
			
		case EditImageActionWrapper.REQUEST_IMAGE_BROWSING:
		case EditImageActionWrapper.REQUEST_GOOGLE_IMAGE_RESULT:
			if (Activity.RESULT_OK == resultCode) {
				mImageActionWrapper.onActivityResult(requestCode, resultCode, data);
			}
			break;

		default:
			break;
		}
	}


	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Causes an immediate (non-fade) view switch.
	 * @param edit <code>true</code> to force edit mode, <code>false</code> for display.
	 */
	public void forceEditMode(int edit) {
		this.mEditState = edit;
		if (mEditView == null || mDisplayView == null) {
			return; //we cannot switch if we have nothing yet to switch.
		}
		if (isInEditMode()) {
			ViewStubManager.switchVisible(mEditView, mDisplayView);
		} else {
			ViewStubManager.switchVisible(mDisplayView, mEditView);
		}
		
	}
	
	
	public boolean isInEditMode() {
		return STATE_VIEW_EXISTING != mEditState;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Initializing methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes the views. */
	private void initViews(View rootView) {
		// we are pressed for time, no controllers for these sub views.
		mDisplayView = ((ViewStub) rootView.findViewById(R.id.viewstub_markings_display_image)).inflate();
		mEditView = ((ViewStub) rootView.findViewById(R.id.viewstub_markings_edit_image)).inflate();
				
		mDisplayImage = (ImageView) mDisplayView.findViewById(R.id.ibs_markings_displayimg_preview);
		mEditImage = (ImageView) mEditView.findViewById(R.id.ibs_markings_editimg_preview);
		

		mDisplayImageName = (TextView) mDisplayView.findViewById(R.id.ibs_markings_displayimg_label_img_name);
		mEditImageName = (EditText) mEditView.findViewById(R.id.ibs_markings_editimg_input_img_name);
		
		mEditProgressView = Utility.getProgressView(rootView);
		
		initInputs(rootView);
		
		enableViews(true);
	}


	/** Initializes the button and click inputs. */
	private void initInputs(View rootView) {
		mEditButton = Utility.setOnClickAndReturnView(mDisplayView, R.id.ibs_markings_displayimg_button_edit, this);
		mDeleteButton = Utility.setOnClickAndReturnView(mDisplayView, R.id.ibs_markings_displayimg_button_delete, this);
		
		mSaveButton = Utility.setOnClickAndReturnView(mEditView, R.id.ibs_markings_editimg_button_save, this);
		mCancelButton = Utility.setOnClickAndReturnView(mEditView, R.id.ibs_markings_editimg_button_cancel, this);		
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/** Dismisses a given dialog, if found. */
	private void dismissDialog(String tag) {
		DialogFragment dialog = (DialogFragment) getFragmentManager().findFragmentByTag(tag);
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	
	
	/** Attempts to find and set the dialog message, while caching it on this fragment.
	 * @param requestId The dialog  */
	private void getAndSetConfirmMessage(ContentResponse data) {
			
		String message = getString(R.string.ibs_error_cannotLoadContent);
		if (data != null) {
			message = data.getContent();
			mDialogState.fetched = true;
		} 

		mDialogState.message = message;
		
		Fragment frag = getFragmentManager().findFragmentByTag(TAG_REMOVE_DIALOG);
		if (frag != null && frag instanceof SimpleYesNoDialog) {
			((SimpleYesNoDialog) frag).updateContent(message);
		}		
	}

	/** Cancels edit state and resumes view. */
	private void cancelEdit() {		
		if (mEditImageName == null) {
			return; //prevent nulls
		}
		mEditState = STATE_VIEW_EXISTING;
		
		if (mImageItem != null) { //if there was an image item
			mImagePath = mImageItem.getUrl();
			mImageActionWrapper.setImagePath(mImagePath);
			
			mImageActionWrapper.loadImage();
			
			mEditImageName.setText(mImageItem.getName());
			mDisplayImageName.setText(mImageItem.getName());
		
		}		

		ViewStubManager.switchFadeViews(mDisplayView, mEditView);
		enableViews(true);
	}
	


	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners start here
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case REQUEST_REMOVE_DIALOG_LOADER:
			return new SimpleContentAsyncLoader(
					getActivity(), 
					getString(R.string.ibs_config_load_removeCustomMarking));
		default:
			throw new UnsupportedOperationException("Id is not recognized? " + id);
		}
		
	}
	
	@Override
	public void onLoaderReset(Loader<ContentResponse> loader) {
		// nothing to do yet.
		
	}
	
	 @Override
	public void onLoadFinished(Loader<ContentResponse> loader,
			ContentResponse data) {
		 final int id = loader.getId();
		 
		 //makes it easier to tell if we are still loading
		 getLoaderManager().destroyLoader(id); 
		 switch (id) {
		 case REQUEST_REMOVE_DIALOG_LOADER:
			 getAndSetConfirmMessage(data);
		 }
		
	}
	 
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End loaders
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean onConsumeBackButton() {
		switch (mEditState) {
		
		case STATE_EDIT_EXISTING:
			cancelEdit();
			return true;
		
		//if creating new, viewing, or anything else, just pop
		case STATE_CREATE_NEW:
		case STATE_VIEW_EXISTING:
		default:
			return false;
		}
	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibs_markings_displayimg_button_edit:
			ViewStubManager.switchFadeViews(mEditView, mDisplayView);
			mEditState = STATE_EDIT_EXISTING; //if we clicked the "Edit" button we were probably viewing
			mImageActionWrapper.enableViews(true);
			break;
			
		case R.id.ibs_markings_displayimg_button_delete:
			DialogFragment dialog = 
				SimpleYesNoDialog.newInstance(mDialogState.message, true);
			dialog.setTargetFragment(this, REQUEST_DELETE_DIALOG);
			dialog.show(getFragmentManager(), TAG_REMOVE_DIALOG);
			break;
		
		
		default:
			break;
		}
		
	}
	

	@Override
	public void onSave(ImageItem item) { 
		if (item != null) {
			mImageItem = item;
			cancelEdit(); //this will reset the mode to view existing using the current image item
			
		} else {
			mListener.onImageRequestPop();
		}
	}


	@Override
	public void onCancel() {
		cancelEdit();
		mListener.onImageRequestPop();
	}


	@Override
	public void enableViews(boolean enabled) {
		if (mEditView != null) {
			mSaveButton.setEnabled(enabled);
			mCancelButton.setEnabled(enabled);
			
			mEditButton.setEnabled(enabled);
			mDeleteButton.setEnabled(enabled);
						
			mEditImageName.setEnabled(enabled);
		}
	}
	

	@Override
	@SuppressWarnings("deprecation")
	public void onNoImage() {
		if (mEditImage != null) {
			//do not recycle resources
			mEditImage.setVisibility(View.VISIBLE);
			mEditProgressView.setVisibility(View.GONE);
			mEditImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_no_image));			
		}
	}

	/** Used in {@link #onImageLoadStart()} and {@link #onImageLoaded(String, Bitmap)} */
	private boolean mOnImageLoadStart = false;
	
	@Override
	public void onImageLoadStart() {
		if (mEditImage != null) {
			mOnImageLoadStart = true;
			Utility.switchFadeProgressViews(mEditProgressView, mEditImage, true);
		}
	}

	@Override
	public void onImageLoaded(String imageUri, Bitmap loadedImage) {
		//the bitmap will be applied twice, so add twice
		mBitmapRecycler.addBitmap(loadedImage); 
		mBitmapRecycler.addBitmap(loadedImage); 
		
		if (mEditImage != null && mDisplayImage != null) {
			String name = mEditImageName.getText().toString();
			
			mBitmapRecycler.recycle(mDisplayImage); //only recycle non-resource bitmaps
			mBitmapRecycler.recycle(mEditImage); //only recycle non-resource bitmaps
			
			if (mOnImageLoadStart) {
				Utility.switchFadeProgressViews(mEditProgressView, mEditImage, false);
				mOnImageLoadStart = false;
			}
			
			mDisplayImage.setImageBitmap(loadedImage);
			mDisplayImage.setContentDescription(name);
			
			mEditImage.setImageBitmap(loadedImage);
			mEditImage.setContentDescription(name);

			mImagePath = mImageActionWrapper.getImagePath();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Interaction interface
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @version 0.2.0-20150715
	 */
	public static interface OnInteractionListener {
		/** The fragment's request to pop itself. */
		public void onImageRequestPop();
	}




}
