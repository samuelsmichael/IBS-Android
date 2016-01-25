package com.inductivebiblestudyapp.ui.actionwrappers;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.loaders.ImageUpdateAsyncLoader;
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.data.model.googleimages.GoogleImageResponse.GoogleImageResult;
import com.inductivebiblestudyapp.ui.activities.GoogleImageSearchActivity;
import com.inductivebiblestudyapp.ui.adapters.ImageFetchRecyclerAdapter;
import com.inductivebiblestudyapp.ui.adapters.ImageFetchRecyclerAdapter.OnImageItemClickListener;
import com.inductivebiblestudyapp.util.ImagePickerUtil;
import com.inductivebiblestudyapp.util.Utility;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Simple action wrapper used to consolidate the actions for creating an image with the layout 
 * <code>viewstub_add_edit_image.xml</code> via {@link R.layout.viewstub_add_edit_image}.
 * We suggest calling {@link #destroy()} in onDestroyView() to clean up lingering bitmaps, etc.
 * 
 * Used to contain and reuse the methods for image creation & editing. 
 * @author Jason Jenkins
 * @version 0.6.7-20150910
 *
 */
public class EditImageActionWrapper implements OnClickListener, 
	ImageFetchRecyclerAdapter.OnImageItemClickListener, OnEditorActionListener {
	/** Class name for debugging purposes. */
	final static private String LOGTAG = EditImageActionWrapper.class
			.getSimpleName();
	
	
	public static final int REQUEST_IMAGE_BROWSING = 0x202;	
	public static final int REQUEST_GOOGLE_IMAGE_RESULT = 0x203;	
	
	private static final int REQUEST_EDIT_LOADER = 0;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// end constants
	////////////////////////////////////////////////////////////////////////////////////////////////

	private final FragmentActivity mActivity;
	
	private final OnActionListener mListener;
	
	private final EditText mEditImageName;
	
	private final RecyclerView mLibraryView;
	private final EditText mGoogleImageSearch;	
	private final View mGalleryButton;
	
	private final View mSaveButton;
	private final View mCancelButton;
	
	private final ImageFetchRecyclerAdapter mLibraryAdapter;
	
	private final String mAccessToken;
	private final ImageLoader mImageLoader;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End final members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private ProgressDialog mSavingDialog = null;
	
	private String mImagePath = null;
	
	private ImageItem mImageItem = null;	
	
	private OnImageItemClickListener mImageClickListener = null;
	
	
	/** Set in {@link #onActivityResult(int, int, Intent)} & <code>null</code>ed in {@link #loadImageFromPath()} */
	private GoogleImageResult mCurrGoogleResult = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Wraps actions for portability.
	 * @param activity
	 * @param rootView
	 * @param imageNameInput
	 * @param saveButton Overrides the onclick of this view. Use {@link OnActionListener#onSave(ImageItem)}
	 * @param cancelButton  Overrides the onclick of this view. Use {@link OnActionListener#onCancel()}
	 * @param listener
	 */
	public EditImageActionWrapper(FragmentActivity activity, View rootView, EditText imageNameInput,
			View saveButton, View cancelButton, OnActionListener listener) {
		
		mActivity = activity;		
		mListener = listener;
		mAccessToken = new CurrentUser(getActivity()).getIBSAccessToken();
		mImageLoader = ImageLoader.getInstance();
		
		mLibraryAdapter = new ImageFetchRecyclerAdapter(getActivity(), true);
		mLibraryAdapter.setOnImageItemClickListener(this);
		mLibraryView = (RecyclerView) rootView.findViewById(R.id.ibs_addedit_image_library_recycleview);
        mLibraryView.setHasFixedSize(true);       

        // use a horizontal linear layout manager
        LinearLayoutManager imgContainer  = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        
        mLibraryView.setLayoutManager(imgContainer);        
        mLibraryView.setAdapter(mLibraryAdapter);
        mLibraryView.setItemAnimator(new DefaultItemAnimator());
        
        mEditImageName = imageNameInput;

		mGoogleImageSearch = (EditText) rootView.findViewById(R.id.ibs_addedit_input_search_google_imags);
		mGoogleImageSearch.setOnEditorActionListener(this);
		
		mGalleryButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_addedit_button_upload_from_gallery, this);
		
		mSaveButton = saveButton;
		mCancelButton = cancelButton;

		mSaveButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
		
		checkAndShowSavingDialog(false);
	}
	
	/** @param path The image path to set. */
	public void setImagePath(String path) {
		this.mImagePath = path;
	}
	
	/** @return The path of the image to be saved. */
	public String getImagePath() {
		return this.mImagePath;
	}
	
	/** @param image The image to edit. Can be <code>null</code> */
	public void setImageItem(ImageItem image) {
		this.mImageItem = image;
		this.mImagePath = image.getUrl();
	}
	
	/** Attempts to load image, returns <code>false</code> if no path or image item is set. 
	 * @see #setImageItem(ImageItem)
	 * @see #setImagePath(String) */
	public boolean loadImage() {
		boolean pathFound = mImagePath != null; // if path is set, image item implicitly set
		loadImageFromPath();
		return pathFound;
	}
	
	/** @param listener The listern for when one clicks on the recycler view images */
	public void setImageClickListener(OnImageItemClickListener listener) {
		this.mImageClickListener = listener;
	}
	
	/** Cleans up anything that needs to be cleaned up, such as bitmaps. */
	public void destroy() {
		mLibraryAdapter.clear();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// public methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Sets the image path (if any) from the gallery picker activity. */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_IMAGE_BROWSING: {
			if (getActivity() != null && data != null) {
				final String path = ImagePickerUtil.getPathFromCameraData(getActivity(), data);
				Log.d(LOGTAG, "Image path: " + path);
				mImagePath = path;
				
				loadImageFromPath();
			}
		}
			break;
			
		case REQUEST_GOOGLE_IMAGE_RESULT: 		
			if (resultCode == Activity.RESULT_OK && data != null) {			
				mCurrGoogleResult = data.getParcelableExtra(GoogleImageSearchActivity.EXTRA_IMAGE_RESULT);
				final String path = mCurrGoogleResult.getUnescapedUrl();
				checkAndUpdateImageName(mCurrGoogleResult.getTitleNoFormatting());
				Log.d(LOGTAG, "Google image path: " + path);
				mImagePath = path;
				
				loadImageFromPath();
			}
		break;

		default:
			break;
		}
	}
	
	
	/** Enables or disables all views. */
	public void enableViews(boolean enabled) {
		mSaveButton.setEnabled(enabled);
		mCancelButton.setEnabled(enabled);
					
		mLibraryView.setEnabled(enabled);
		mGoogleImageSearch.setEnabled(enabled);
		mGalleryButton.setEnabled(enabled);
		
		mEditImageName.setEnabled(enabled);
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
	private String getString(int id) {
		return mActivity.getString(id);
	}
	/** Convenience for migration and habit. */
	private Resources getResources() {
		return mActivity.getResources();
	}
	
	/** Convenience for migration and habit. */
	private LoaderManager getLoaderManager() {
		return mActivity.getSupportLoaderManager();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Checks and sees if the fragment is updating. If so, show dialog. 
	 * Otherwise dismiss (if possible). Be cautious with this method around loaders.
	 * @param force Force show the dialog.
	 */
	private void checkAndShowSavingDialog(boolean force) {
		boolean show = force;		
		if (Utility.checkIfLoading(getLoaderManager(), REQUEST_EDIT_LOADER)) {
			//reattach the loader if loader exists
			getLoaderManager().initLoader(REQUEST_EDIT_LOADER, null, mEditImageCallbacks);
			show = true;
		}
		
		if (mSavingDialog == null && show) {
			mSavingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.ibs_text_saving));			
		} else if (mSavingDialog != null) {
			mSavingDialog.dismiss();
			mSavingDialog = null;
		}
	}
	
	/** Loads image from path if not <code>null</code>. */
	private void loadImageFromPath() {
		
		String path = mImagePath;
		if (mImagePath == null) {
			mListener.onNoImage();
			return;
		} else if (!mImagePath.contains("://")) {
			path = "file://"+mImagePath;
		}
		
		mImageLoader.loadImage(path, new ImageLoadingListener() {
			volatile boolean isLoadingComplete = false;
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				if (imageUri.startsWith("http")) {
					//loading.show();
					new Handler().postDelayed(new Runnable() {						
						@Override
						public void run() {
							if (!isLoadingComplete) {
								mListener.onImageLoadStart();
							}
						}
					}, 100); 
				}
			}
			
			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				// if we failed during a google fetch, it's possible google's source image 
				// is gone, but the thumbnail remains
				if (mCurrGoogleResult != null) {
					Log.w(LOGTAG, "Source image not found, trying thumbnail");
					mImagePath = mCurrGoogleResult.getThumbnailUrl();
					mCurrGoogleResult = null; //null to prevent looping
					
					AppCache.clearImageFromCache(mImagePath); //removes the image from cache					
					loadImageFromPath(); //reload
					return;
				}
				Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotLoadImage));
				mImagePath = null;
				mListener.onNoImage();
			}
			
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				isLoadingComplete = true;
				mListener.onImageLoaded(imageUri, loadedImage);
			}
			
			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				isLoadingComplete = true;
				mListener.onNoImage();
			}
		});
	}
	
	/** Updates the image name if there is no name currently. */
	private void checkAndUpdateImageName(String name) {
		if (mEditImageName.getText().toString().isEmpty()) {
			mEditImageName.setText(name);
		}
	}

	/** Performs the save of the given image and image name. Whether add or edit. */
	private void saveSelectedImage() {		
		mEditImageName.setError(null);
		final String imageName = mEditImageName.getText().toString();
		
		if (imageName.isEmpty()) {
			mEditImageName.setError(getString(R.string.ibs_error_cannotBeEmpty));
			return;
		} else if (mImagePath == null || mImagePath.isEmpty()) {
			Utility.toastMessage(mActivity, getString(R.string.ibs_error_imagePathMustBeSet));
			return;
		}
		
		enableViews(false);
		
		String extension = "png";
		if (mImagePath.lastIndexOf(".") > 0) {
			extension = mImagePath.substring(mImagePath.lastIndexOf(".")+1); //.jpg -> jpg
			if (extension.length() > 4 || !extension.matches("[jpg|png|jpeg]")) { 
				//most file types are at most 4 (jpeg) 
				extension = "png"; //we'll pretend its a png?
			} 
		}	
		
		Bundle args = new Bundle();
		args.putString(ImageUpdateAsyncLoader.KEY_IMAGE_NAME, imageName.trim());
		args.putString(ImageUpdateAsyncLoader.KEY_FILE_EXTENSION, extension);
		args.putString(ImageUpdateAsyncLoader.KEY_IMAGE_PATH, mImagePath);
		
		if (mImageItem != null){ //only if editing
			args.putString(ImageUpdateAsyncLoader.KEY_IMAGE_ID, mImageItem.getId());
		} 
		
		checkAndShowSavingDialog(true);
		
		getLoaderManager().initLoader(REQUEST_EDIT_LOADER, args, mEditImageCallbacks);
	}
	
	
	
	
	/** The method to handle actions when changes are saved. 
	 * @param insertedId The id inserted or null */
	/*default*/ void changesSaved(String insertedId) {
		Utility.toastMessage(getActivity(), getString(R.string.ibs_text_changesSaved));
		mLibraryAdapter.forceReload();
		
		if (insertedId == null && mImageItem != null) { //if during edit
			insertedId = mImageItem.getId();  
		}
		
		if (insertedId != null) {
			RestClient.getInstance().getImageService()
				.get(mAccessToken, insertedId, 
						new Callback<ImageItem>() {
							
							@Override
							public void success(ImageItem item, Response arg1) {
								mListener.onSave(item);
								if (item == null) {
									Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
								} else {
									mImageItem = item;
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
	//// Implemented interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private LoaderManager.LoaderCallbacks<UpdateResult> mEditImageCallbacks = 
			new LoaderManager.LoaderCallbacks<UpdateResult>() {					
				@Override
				public void onLoaderReset(Loader<UpdateResult> arg0) {}
				
				@Override
				public void onLoadFinished(Loader<UpdateResult> arg0, final UpdateResult result) {
					getLoaderManager().destroyLoader(arg0.getId());
					checkAndShowSavingDialog(false);
					
					new Handler().post(new Runnable() {
						
						@Override
						public void run() {
							if (result != null) { 
								if (result.isSuccessful()) {
									changesSaved(result.getInsertId());
									return;
								}
							} 
							
							Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
							enableViews(true);
						}
					});			
					
				}
				
				@Override
				public Loader<UpdateResult> onCreateLoader(int arg0, Bundle args) {
					if (args == null) {
						return null;
					}
					
					return new ImageUpdateAsyncLoader(getActivity(), args);
				}
			};


	@Override
	public void onImageItemClick(ImageItem item) {
		checkAndUpdateImageName(item.getName());
		mImagePath = item.getUrl();
		loadImageFromPath();
		
		if (mImageClickListener != null) {
			mImageClickListener.onImageItemClick(item);
		}
	}


	@Override
	public void onClick(View v) {
		if (mSaveButton.equals(v)) {
			saveSelectedImage();
			
		} else if (mCancelButton.equals(v)) {
			mListener.onCancel();
		} else if (mGalleryButton.equals(v)) {
			ImagePickerUtil.launchPicker(getActivity(), REQUEST_IMAGE_BROWSING);
		}
		
	}
	


	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			final String searchTerm =   mGoogleImageSearch.getText().toString().trim();
			Log.d(LOGTAG, "searching: " + searchTerm);
			Intent search = new Intent(getActivity(), GoogleImageSearchActivity.class);
			search.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			search.putExtra(GoogleImageSearchActivity.EXTRA_SEARCH_TERM, searchTerm);
			
			getActivity().startActivityForResult(search, REQUEST_GOOGLE_IMAGE_RESULT);
			return true;
		}
		return false;
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public interface
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.3.0-20150810 */
	public static interface OnActionListener {
		/** @param item The image item just created or edited. 
		 * In unusual cases, it may be <code>null</code>. */
		public void onSave(ImageItem item);
		public void onCancel();
		public void enableViews(boolean enabled);
		
		/** When there is no image path loaded.
		 * Either due to none-set or none available. */
		public void onNoImage();
		/** When image starts loading. */
		public void onImageLoadStart();
		/** When the set image has loaded.  */
		public void onImageLoaded(String imageUri, Bitmap loadedImage);
	}



}