package com.inductivebiblestudyapp.ui.fragments.profile;


import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.loaders.ProfileFetchAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.ProfileUpdateAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.data.model.ProfileResponse;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.data.model.ProfileResponse.Version;
import com.inductivebiblestudyapp.ui.OnBackButtonListener;
import com.inductivebiblestudyapp.ui.RestartReceiver;
import com.inductivebiblestudyapp.ui.dialogs.MessageToolTip;
import com.inductivebiblestudyapp.ui.viewstub.ViewStubManager;
import com.inductivebiblestudyapp.util.BitmapRecycler;
import com.inductivebiblestudyapp.util.DialogStateHolder;
import com.inductivebiblestudyapp.util.ImagePickerUtil;
import com.inductivebiblestudyapp.util.RecyclableImageViewAware;
import com.inductivebiblestudyapp.util.Utility;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;


/**
 * The fragment to load/edit user details.
 * @version 0.6.0-20150915
 */
public class ProfileFragment extends Fragment implements OnClickListener, OnBackButtonListener, 
	LoaderManager.LoaderCallbacks<ContentResponse> {
	
	final static private String CLASS_NAME = ProfileFragment.class
			.getSimpleName();
	final static private String LOGTAG = CLASS_NAME;
	
	/** Bundle key: Boolean. Whether or not they are in edit mode. */
	private static final String KEY_IN_EDIT_MODE = CLASS_NAME + ".KEY_IN_EDIT_MODE";	

	private static final String KEY_DIALOG_STATE = CLASS_NAME + ".KEY_DIALOG_CONTENT";
	
	private static final String KEY_IMAGE_PATH = CLASS_NAME + ".KEY_IMAGE_PATH";
	
	/** Bundle key. Parcelable array: #mTranslationsList */
	private static final String KEY_TRANSLATION_LIST = CLASS_NAME + ".KEY_TRANSLATION_LIST";
	
	private static final String TAG_CONFIRM_DIALOG = CLASS_NAME + ".TAG_CONFIRM_DIALOG";
		
	private static final int REQUEST_CONFIRM_CONTENT_LOADER = 0;
	private static final int REQUEST_UPDATE_LOADER = 1;
	private static final int REQUEST_PROFILE_LOADER = 2;
	
	private static final int REQUEST_IMAGE_GALLERY = 0x10;
	

	private static final int UPDATE_INTERVAL = 30000; //30 seconds
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public ProfileFragment() {
	}
	
	/** The display view to show data. */
	private View mDisplayView = null;
	/** The edit view to edit data. */
	private View mEditView = null;
	
	private View mDisplayImageProgress = null;
	private View mEditImageProgress = null;
	
	private TextView mTv_availableTranslations = null;
	private RadioGroup mRg_translations = null;
	
	private EditText mEt_name = null;
	private EditText mEt_email = null;
	private EditText mEt_address1 = null;
	private EditText mEt_address2 = null;
	private EditText mEt_about = null;
	
	private EditText mEt_password1 = null;
	private EditText mEt_password2 = null;
	
	private View mEditButton = null;
	private View mSaveButton = null;
	
	private View mProgressView = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// end views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private final BitmapRecycler mBitmapRecycler = new BitmapRecycler();
	
	/** Default is empty. Only not empty if the user is selecting a new image. */
	private String mImagePath = "";
	
	private ProgressDialog mSavingDialog = null;
	
	private Version[] mTranslationList = null;
	
	private CurrentUser mUser = null;
	
	private DialogStateHolder mDialogState = null;
	
	private boolean mInputHasErrors = false;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_IN_EDIT_MODE, inEditMode());
		outState.putParcelable(KEY_DIALOG_STATE, mDialogState);
		
		outState.putParcelableArray(KEY_TRANSLATION_LIST, mTranslationList);
		
		outState.putString(KEY_IMAGE_PATH, mImagePath);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_profile,
				container, false);
		
		mProgressView = rootView.findViewById(R.id.ibs_profile_progress);
		mDisplayView = ((ViewStub) rootView.findViewById(R.id.viewstub_profile_display)).inflate();
		mEditView = ((ViewStub) rootView.findViewById(R.id.viewstub_profile_edit)).inflate();	
		
		mDisplayImageProgress = mDisplayView.findViewById(R.id.ibs_profile_display_img_progessBar);
		mEditImageProgress = mEditView.findViewById(R.id.ibs_profile_edit_img_progessBar);
		
		rootView.findViewById(R.id.ibs_profile_edit_img).setOnClickListener(this);
		
		mEditButton = Utility.setOnClickAndReturnView(mDisplayView, R.id.ibs_profile_display_button_edit, this);

		mSaveButton = Utility.setOnClickAndReturnView(mEditView, R.id.ibs_profile_edit_button_save, this);
		mEditView.findViewById(R.id.ibs_profile_edit_button_cancel).setOnClickListener(this);

		boolean editMode = false;
		if (savedInstanceState != null) {
			DialogStateHolder state =
					(DialogStateHolder) savedInstanceState.getParcelable(KEY_DIALOG_STATE);
			mDialogState = state == null ? mDialogState : state;
			
			editMode = savedInstanceState.getBoolean(KEY_IN_EDIT_MODE);
			
			Parcelable[] pArray = savedInstanceState.getParcelableArray(KEY_TRANSLATION_LIST);
			if (pArray != null) {
				mTranslationList = Arrays.copyOf(pArray, pArray.length, Version[].class);
			}
			
			mImagePath = savedInstanceState.getString(KEY_IMAGE_PATH, "");
		} else {
			mDialogState = new DialogStateHolder();
		}
		
		populateContent(rootView);
		populateTranslations(); //should just give errors		
	
		
		if (editMode) {
			showHideViews(mEditView, mDisplayView);
		} else {
			showHideViews(mDisplayView, mEditView);
		}
		
		if (!mDialogState.fetched) {
			getLoaderManager().initLoader(REQUEST_CONFIRM_CONTENT_LOADER, null, this);
		}
		
		checkAndUpdate();
		checkAndShowSavingDialog(false);
		
		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mBitmapRecycler.recycleAll();
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (REQUEST_IMAGE_GALLERY == requestCode && Activity.RESULT_OK == resultCode) {
			if (getActivity() != null && data != null) {
				String path = ImagePickerUtil.getPathFromCameraData(getActivity(), data);
				Log.d(LOGTAG, "Image path: " + path);
				
				if (getView() != null) {
					populateImage(getView(), path);
				}
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/** Processes input, stores it and uploads it. 
	 * @param local <code>true</code> commits it to local values, <code>false</code>
	 * submits it online*/	
	private void processInput(boolean local) {
		mInputHasErrors = false;
		final String fullname  = checkRequiredInputForError(mEt_name);
		final String email = checkRequiredInputForError(mEt_email);
		final String address1 = getOptionalInput(mEt_address1);
		
		//"Address must be in the format of 'City, State, Zip" 
		final String address2 = checkStateCityZipForError(mEt_address2);
		final String bio = getOptionalInput(mEt_about);
		
		final int checkedId = mRg_translations.getCheckedRadioButtonId();
		
		String selectedVersionId = mUser.getTranslationId();
		String selectedVersionName = mUser.getTranslationName().trim();
		if (checkedId >= 0) {
			Version checked = mTranslationList[checkedId];
			selectedVersionId = checked.getVersionId();
			selectedVersionName = checked.getName().trim();
		}
		
		String newPassword = null;
		if (mUser.isEmailSignin()) {
			newPassword = checkPasswordForErrors();
		}
		
		if (mInputHasErrors) {
			return; //cannot proceed
		}
		
		final int spaceIndex = fullname.indexOf(" ");
		
		String first = fullname;
		String last = "";
		if (spaceIndex > 0) {
			first = fullname.substring(0, spaceIndex).trim();
			last = fullname.substring(spaceIndex).trim();
		}
		
		String addressStub = address2;
		String zip = "";
		String city = "";
		String state = "";
		for (int count = 0; count < 2; count++) {			
			int lastComma = addressStub.lastIndexOf(",");
			if (lastComma > 0){
				String value = addressStub.substring(lastComma+1);
				addressStub = addressStub.substring(0, lastComma);
				
				if (count == 0) {
					zip = value.trim();
				} else if (count == 1) {
					state = value.trim();
				}
			}
		}
		city = addressStub.trim();

		if (local) {
			mUser.setFirstName(first);
			mUser.setLastName(last);
			mUser.setEmail(email);
			mUser.setBio(bio);
			mUser.setAddress(address1);
			mUser.setCity(city);
			mUser.setState(state);
			mUser.setZip(zip);
			
			mUser.setTranslationId(selectedVersionId);
			mUser.setTranslationName(selectedVersionName);
			
			mUser.setProfileImage(mImagePath);
			
			populateText(getView());
		} else {
			if (	first.equals(mUser.getFirstName()) && last.equals(mUser.getLastName()) &&
					email.equals(mUser.getEmail()) && address1.equals(mUser.getAddress()) &&
					state.equals(mUser.getState()) && city.equals(mUser.getCity()) &&
					zip.equals(mUser.getZip()) && bio.equals(mUser.getBio()) &&
					selectedVersionId.equals(mUser.getTranslationId()) &&
					(TextUtils.isEmpty(newPassword) || !mUser.isEmailSignin()) &&
					mUser.getProfileImagePath().equals(mImagePath)) {
				new MessageToolTip.Builder(getString(R.string.ibs_text_profileNoChangesMade), mSaveButton)
				.setCentered(false)
				.build()
				.show(getFragmentManager(),TAG_CONFIRM_DIALOG);
				
				cancelEdit();
				return;
			}
			
			Bundle args = new Bundle();
			args.putString(ProfileUpdateAsyncLoader.KEY_FIRSTNAME, first);
			args.putString(ProfileUpdateAsyncLoader.KEY_LASTNAME, last);
			args.putString(ProfileUpdateAsyncLoader.KEY_EMAIL, email);
			args.putString(ProfileUpdateAsyncLoader.KEY_ADDRESS, address1);
			args.putString(ProfileUpdateAsyncLoader.KEY_STATE, state);
			args.putString(ProfileUpdateAsyncLoader.KEY_CITY, city);
			args.putString(ProfileUpdateAsyncLoader.KEY_ZIP, zip);
			args.putString(ProfileUpdateAsyncLoader.KEY_TRANSLATIONS, selectedVersionId);
			
			args.putString(ProfileUpdateAsyncLoader.KEY_USER_BIO, bio);

			args.putString(ProfileUpdateAsyncLoader.KEY_PASSWORD, newPassword);
			
			checkAndShowSavingDialog(true);
			getLoaderManager().initLoader(REQUEST_UPDATE_LOADER, args, mProfileUpdateCallbacks);
		}
				
	}
	
	/** Populates the content of the profile. */
	private void populateContent(View rootView) {
		populateText(rootView);	
		populateImage(rootView, TextUtils.isEmpty(mImagePath) ? mUser.getProfileImagePath() : mImagePath);
	}
	
	//Thought: consider updating content every 5 minutes
	/** Finds & populates the views with local data. Both inputs and output. */
	private void populateText(View rootView) {
		mUser = new CurrentUser(getActivity());
		
		final String fullname = mUser.getFirstName() + " " + mUser.getLastName();
		final String email = mUser.getEmail();
		final String address1 = mUser.getAddress();
		final String about = mUser.getBio();
		

		String address2 = mUser.getCity() + mUser.getState() + mUser.getZip();
		
		if (!address2.trim().isEmpty()) {
			address2 = mUser.getCity() + ", " + mUser.getState() + ", " + mUser.getZip();
		}
		
		Utility.setTextViewAndReturn(rootView, R.id.ibs_profile_display_name, fullname);
		Utility.setTextViewAndReturn(rootView, R.id.ibs_profile_display_email, email);
		Utility.setTextViewAndReturn(rootView, R.id.ibs_profile_display_address1, address1);
		Utility.setTextViewAndReturn(rootView, R.id.ibs_profile_display_address2, address2);
		Utility.setTextViewAndReturn(rootView, R.id.ibs_profile_display_about, mUser.getBio());
		
		Utility.setTextViewAndReturn(rootView, R.id.ibs_profile_display_selectedTranslation, 
				getString(R.string.ibs_label_translationsSelected, mUser.getTranslationName()));
		
	
		mEt_name = findAndSetEdit(rootView, R.id.ibs_profile_edit_name, fullname);
		mEt_email = findAndSetEdit(rootView, R.id.ibs_profile_edit_email, email);
		mEt_address1 = findAndSetEdit(rootView, R.id.ibs_profile_edit_address1, address1);
		mEt_address2 = findAndSetEdit(rootView, R.id.ibs_profile_edit_address2, address2);
		mEt_about = findAndSetEdit(rootView, R.id.ibs_profile_edit_about, about);
		
		View passwordLabel = rootView.findViewById(R.id.ibs_profile_edit_changePassword); 
		mEt_password1 = findAndSetEdit(rootView, R.id.ibs_profile_edit_password1, "");
		mEt_password2 = findAndSetEdit(rootView, R.id.ibs_profile_edit_password2, "");
		
		int passwordVisibility = mUser.isEmailSignin() ? View.VISIBLE : View.GONE;

		passwordLabel.setVisibility(passwordVisibility);
		mEt_password1.setVisibility(passwordVisibility);
		mEt_password2.setVisibility(passwordVisibility);

		mTv_availableTranslations = 
				(TextView) rootView.findViewById(R.id.ibs_profile_display_availableTranslations_list);
		mRg_translations = 
				(RadioGroup) rootView.findViewById(R.id.ibs_profile_edit_radiogroup_translations);
	}
	
	/** Finds and populates translations based on {@link #mTranslationList}. 
	 * If null or empty, sets error. */
	@SuppressLint("InflateParams") 
	private void populateTranslations() {
		if (mTv_availableTranslations == null || mRg_translations == null) { 
			return;
		}
		final String selectedVersion = mUser.getTranslationId();
		
		if (mTranslationList == null || mTranslationList.length == 0) {
			mTv_availableTranslations.setText(R.string.ibs_error_cannotConnect);
			
			TextView tv = new TextView(getActivity());
			tv.setText(R.string.ibs_error_cannotLoadTranslations);
			tv.setTextAppearance(getActivity(), android.R.attr.textAppearanceLarge);
			tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			tv.setGravity(Gravity.CENTER);
			mRg_translations.removeAllViews();
			mRg_translations.addView(tv);
			
		} else {
			String versionList = "";
			int position = 0;
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			mRg_translations.removeAllViews();
			
			for (Version version : mTranslationList) {
				if (!versionList.isEmpty()) {
					versionList += "\n"; //new line between lines
				}
				versionList += 
						getString(R.string.ibs_text_translationLineStub, version.getName());
				
				RadioButton cb = (RadioButton) inflater.inflate(R.layout.radiobutton_version, null);
				cb.setText(version.getName());
				cb.setTag(version.getVersionId());
				cb.setId(position++);
				
				if (version.getVersionId().equals(selectedVersion)) {
					cb.setChecked(true); //set the current selected as default
				}
													
				mRg_translations.addView(cb);
			}
			mTv_availableTranslations.setText(versionList);
		}
	}
	
	private void populateImage(View rootView, String path) {
		ImageView displayImg = (ImageView) rootView.findViewById(R.id.ibs_profile_display_img);
		ImageView editImg = (ImageView) rootView.findViewById(R.id.ibs_profile_edit_img);
		
		if (TextUtils.isEmpty(path)) {
			displayImg.setImageResource(R.drawable.ic_no_image);
			editImg.setImageResource(R.drawable.ic_no_image);
			return;
		}
		
		if (!path.contains("://")) {
			path = "file://" + path; //set to file path
		}
		
		mImagePath = path;
		
		
		ImageAware display = new RecyclableImageViewAware(displayImg, mBitmapRecycler);
		ImageAware edit = new RecyclableImageViewAware(editImg, mBitmapRecycler);
		
		ImageLoader.getInstance().displayImage(path, display, mImageLoader);
		ImageLoader.getInstance().displayImage(path, edit, mImageLoader);
	}
	
	/*package*/ static EditText findAndSetEdit(View rootView, int id, String text) {
		EditText edit = (EditText) rootView.findViewById(id);
		edit.setText(text);
		edit.setError(null);
		return edit;
	}
	
	/** Checks the password for mismatch, if the first one is empty returns <code>null</code>
	 * (to skip).
	 */
	private String checkPasswordForErrors() {
		String pass1 = mEt_password1.getText().toString();
		
		if (pass1.equals(mEt_password2.getText().toString())) {
			return pass1;
		} else {
			mInputHasErrors = true;
			mEt_password1.setError(getString(R.string.ibs_error_passwordsMustMatch));
			mEt_password2.setError(getString(R.string.ibs_error_passwordsMustMatch));
		}
		
		if (TextUtils.isEmpty(pass1)) {
			return null;
		}
		
		return "";
	}
	
	
	/** Used for state, city, state inputs. */
	private String checkStateCityZipForError(EditText input) {
		String out = input.getText().toString().trim();
		input.setError(null);
		if (out.isEmpty()) {
			return "";
		}
	    Matcher matcher =  Pattern.compile("[^,],[^,]").matcher(out);
	    int count = 0;
	    while (matcher.find()){
	    	count +=1;
	    }
	    if (count < 2) {
	    	mInputHasErrors = true;
	    	input.setError(getString(R.string.ibs_error_address2_cityStateZip));
	    }
		return out;
	}
	
	/** Used for required inputs. */
	private String checkRequiredInputForError(EditText input) {
		String out = input.getText().toString().trim();
		input.setError(null);
		if (out.isEmpty()) {
			mInputHasErrors = true;
			input.setError(getString(R.string.ibs_error_cannotBeEmpty));
		}
		return out.trim();
	}
	
	/** Used for optional inputs. */
	private String getOptionalInput(EditText input) {
		String out = input.getText().toString().trim();
		input.setError(null);
		return out;
	}
	
	/**
	 * @return <code>true</code> if in edit mode, <code>false</code> otherwise.
	 */
	private boolean inEditMode(){
		if (mEditView == null) {
			return false;
		}
		return mEditView.getVisibility() == View.VISIBLE;
	}
	
	/** Immediately shows/hides views, no animations. */
	private void showHideViews(View show, View hide) {
		ViewStubManager.switchVisible(show, hide);
	}
	
	/**
	 * Takes the two views, shows the first one via fade, hides the second via fade.
	 * @param show The view to show
	 * @param hide The view to hide
	 */
	private void switchFadeViews(final View show, final View hide) {
		ViewStubManager.switchFadeViews(show, hide);
	}
	

	private void cancelEdit() {
		mImagePath = ""; //blank out images that may be set.
		switchFadeViews(mDisplayView, mEditView);
		populateText(getView());
		populateImage(getView(), mUser.getProfileImagePath());
	}
	
	
	
	/** Attempts to find and set the dialog message, while caching it on this fragment.
	 * @param requestId The dialog  */
	private void getAndSetConfirmMessage(ContentResponse data) {
		
		String message = getString(R.string.ibs_error_cannotLoadContent);
		if (data != null) {
			message = data.getContent();
			mDialogState.fetched = true;
		} else {
			mDialogState.fetched = false;
		}
		mDialogState.message = message;
		
		Fragment frag = getFragmentManager().findFragmentByTag(TAG_CONFIRM_DIALOG);
		if (frag != null && frag instanceof MessageToolTip) {
			((MessageToolTip) frag).updateMessage(message);
		}		
	}
	
	
	/*package*/ void showImageProgress(boolean show) {
		if (mDisplayImageProgress != null && mEditImageProgress != null) {
			mDisplayImageProgress.setVisibility(show ? View.VISIBLE : View.GONE);
			mEditImageProgress.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Checks and sees if the fragment is updating. If so, show dialog. 
	 * Otherwise dismiss (if possible). Be cautious with this method around loaders.
	 * @param force Force show the dialog.
	 */
	private void checkAndShowSavingDialog(boolean force) {
		boolean show = force;		
		if (Utility.checkIfLoading(getLoaderManager(), REQUEST_UPDATE_LOADER)) {
			//reattach the loader if loader exists
			getLoaderManager().initLoader(REQUEST_UPDATE_LOADER, null, mProfileUpdateCallbacks);
			show = true;
		}
		
		if (mSavingDialog == null && show) {
			mSavingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.ibs_text_saving));			
		} else if (mSavingDialog != null) {
			mSavingDialog.dismiss();
			mSavingDialog = null;
		}
	}
	
	private void checkAndUpdate() {
		if (mEditView.getVisibility() == View.VISIBLE) {
			return; //if in edit mode skip
		}
		if ((new Date().getTime() - CurrentUser.getLastUpdateTime(getActivity())) > UPDATE_INTERVAL ||
			mTranslationList == null ){ //if no translations or out of date, update
			getLoaderManager().initLoader(REQUEST_PROFILE_LOADER, null, mProfileLoadCallback);
			checkLoading(true);
		} else {
			mProgressView.setVisibility(View.GONE);
			mDisplayView.setVisibility(View.VISIBLE);
		}
	}
	
	private void checkLoading(boolean force) {
		final boolean loading = Utility.checkIfLoading(getLoaderManager(), REQUEST_PROFILE_LOADER);
		Utility.checkIfLoading(getLoaderManager(), REQUEST_PROFILE_LOADER, mProgressView, mDisplayView, force);
		
		if (mEditButton != null) {
			mEditButton.setEnabled(!loading);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////

		
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private LoaderManager.LoaderCallbacks<ProfileResponse> mProfileLoadCallback = 
			new LoaderManager.LoaderCallbacks<ProfileResponse>() {
		@Override
		public Loader<ProfileResponse> onCreateLoader(int arg0, Bundle arg1) {
			return new ProfileFetchAsyncLoader(getActivity());
		}
		
		@Override
		public void onLoaderReset(Loader<ProfileResponse> arg0) {}
		
		@Override
		public void onLoadFinished(Loader<ProfileResponse> loader,
				ProfileResponse profileResponse) {
			getLoaderManager().destroyLoader(loader.getId());
			checkLoading(false); //beware of infinite loops!
			
			if (profileResponse != null) {
				if (!profileResponse.isSuccessful()) { 
					//response was not successful, thus clear preferences and return to top.
					// WARNING: Beware of loops here
					RestartReceiver.sendBroadcast(getActivity());
					return;
				}				
				
				CurrentUser.updateUserData(getActivity(), profileResponse.getUserData());
				CurrentUser.setLastUpdateNow(getActivity());
								
				if (getView() != null) {
					mImagePath = ""; //clear any local image loaded
					populateContent(getView());
					mTranslationList = profileResponse.getVersions();
				}
			} 
			populateTranslations();
		}
	};
	
	private LoaderManager.LoaderCallbacks<UpdateResult> mProfileUpdateCallbacks = 
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
								final String errorMessage = result.getMessage();
								
								if (result.isSuccessful()) {
									Log.d(LOGTAG, "Success!");
									new MessageToolTip.Builder(mDialogState.message, mSaveButton)
										.setCentered(false)
										.build()
										.show(getFragmentManager(),TAG_CONFIRM_DIALOG);
									processInput(true);
									
									mSaveButton.postDelayed(new Runnable() {								
										@Override
										public void run() {
											switchFadeViews(mDisplayView, mEditView);
										}
									}, 150);
									
								} else if (errorMessage != null && 
										errorMessage.toLowerCase(Locale.US).contains("email") || 
										errorMessage.toLowerCase(Locale.US).contains("e-mail") ){
									mEt_email.setError(errorMessage);
								} else {
									new MessageToolTip.Builder(""+result.getMessage(), mSaveButton)
									.setCentered(false)
									.build()
									.show(getFragmentManager(),TAG_CONFIRM_DIALOG);
								}
							} else {
								new MessageToolTip.Builder(getString(R.string.ibs_error_cannotConnect), mSaveButton)
									.setCentered(false)
									.build()
									.show(getFragmentManager(),TAG_CONFIRM_DIALOG);
								//cancelEdit();
							}
						}
					});			
					
				}
				
				@Override
				public Loader<UpdateResult> onCreateLoader(int arg0, Bundle arg1) {
					if (arg1 == null) {
						return null;
					}
						
					if (TextUtils.isEmpty(mImagePath)) {
						return new ProfileUpdateAsyncLoader(getActivity(), arg1);
					} else {
						return new ProfileUpdateAsyncLoader(getActivity(), arg1, mImagePath);
					}
				}
			};
	
	@Override
	public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case REQUEST_CONFIRM_CONTENT_LOADER:
			return new SimpleContentAsyncLoader(getActivity(), getString(R.string.ibs_config_load_profileConfirm));
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
		 case REQUEST_CONFIRM_CONTENT_LOADER:
			 getAndSetConfirmMessage(data);
		 }
		
	}
	 
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End Loaders
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private ImageLoadingListener mImageLoader = new ImageLoadingListener() {
		
		@Override
		public void onLoadingStarted(String imageUri, View view) {
			//show progress
			showImageProgress(true);
		}
		
		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			showImageProgress(false);
		}
		
		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			showImageProgress(false);
		}
		
		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			showImageProgress(false);
		}
	};
	
	@Override
	public boolean onConsumeBackButton() {
		if (inEditMode()) {
			cancelEdit();
			return true;
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibs_profile_edit_img:
			ImagePickerUtil.launchPicker(getActivity(), REQUEST_IMAGE_GALLERY);
			break;
		
		case R.id.ibs_profile_display_button_edit: 
			switchFadeViews(mEditView, mDisplayView);
			break;
			
		case R.id.ibs_profile_edit_button_save:
			processInput(false);			
			
			break;
		case R.id.ibs_profile_edit_button_cancel: 
			cancelEdit();
			break;
		}		
	}
}
