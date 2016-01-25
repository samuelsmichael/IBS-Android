package com.inductivebiblestudyapp.ui.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.auth.SigninHeadlessFragment;
import com.inductivebiblestudyapp.data.loaders.ForgotPasswordAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.data.model.EmailSigninResponse;
import com.inductivebiblestudyapp.data.model.ForgotPasswordResponse;
import com.inductivebiblestudyapp.data.model.SocialSigninResponse;
import com.inductivebiblestudyapp.ui.actionwrappers.FooterViewActionWrapper;
import com.inductivebiblestudyapp.ui.activities.ProfileActivity;
import com.inductivebiblestudyapp.ui.activities.SetupAccountActivity;
import com.inductivebiblestudyapp.ui.activities.SignUpEmailActivity;
import com.inductivebiblestudyapp.ui.dialogs.EmailPasswordRecoveryTooltip;
import com.inductivebiblestudyapp.ui.dialogs.EmailSigninTooltip;
import com.inductivebiblestudyapp.ui.dialogs.MessageToolTip;
import com.inductivebiblestudyapp.ui.dialogs.UpdateDialogMessage;
import com.inductivebiblestudyapp.util.DialogStateHolder;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link SignUpSignInFragment#newInstance} factory method to create an instance
 * of this fragment.
 * 
 * @author Jason Jenkins
 * @version 0.15.5-20150909
 */
public class SignUpSignInFragment extends Fragment implements OnClickListener, 
	LoaderManager.LoaderCallbacks<ContentResponse>, SigninHeadlessFragment.OnSigninResultListener {
	
	final static private String CLASS_NAME = SignUpSignInFragment.class.getSimpleName();
	final static private String LOGTAG = CLASS_NAME;
	
	/** Bundle key: Int. The signin */
	public static final String KEY_MODE = CLASS_NAME + ".KEY_MODE";
	
	/** Bundle key: String. The sign in text content. */
	private static final String KEY_SIGNIN_CONTENT = CLASS_NAME + ".KEY_SIGNIN_CONTENT";
	/** Bundle key: String. The sign up text content. */
	private static final String KEY_SIGNUP_CONTENT = CLASS_NAME + ".KEY_SIGNUP_CONTENT";
	
	/** Bundle key: SparseArray (SparseArray<DialogStateHolder>). The request to content pairing of data.
	 * See {@link #mRequestCodeToDialogContentMap} */
	private static final String KEY_DIALOG_STATE = CLASS_NAME + ".KEY_DIALOG_STATE";
	
	/** Bundle key: Integer. Completed signup. */
	private static final String KEY_SIGN_UP_COMPLETE = CLASS_NAME + ".KEY_SIGN_UP_COMPLETE";
	
	public static final int VALUE_SIGN_IN_MODE = 0;	
	public static final int VALUE_SIGN_UP_MODE = 1;
	
	private static final String TAG_EMAIL_SIGNIN = CLASS_NAME + ".TAG_EMAIL_SIGNIN";
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End public constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final int REQUEST_EMAIL_SIGN_UP = 0;	
	public static final int REQUEST_CONFIRM_ALL = 1; //this is temporary	
	
	public static final int REQUEST_EMAIL_SIGN_IN = 2;	
	
	public static final int REQUEST_FACEBOOK_SIGN_UP = 3;
	public static final int REQUEST_TWITTER_SIGN_UP = 4;
	public static final int REQUEST_LINKEDIN_SIGN_UP = 5;
	public static final int REQUEST_GOOGLE_PLUS_SIGN_UP = 6;
	
	public static final int REQUEST_CONFIRM_EMAIL_SIGNIN = 7; 	

	//used exclusively for loaders
	public static final int REQUEST_SIGNIN_LOADER = 8;
	public static final int REQUEST_SIGNUP_LOADER = 9;
	
	//Used for both activities AND loaders. See: mRequestTagMap	
	public static final int REQUEST_EMAIL_RECOVERY_QUERY = 10;
	
	public static final int REQUEST_CONFIRM_FACEBOOK_SIGNIN = 12;
	public static final int REQUEST_CONFIRM_TWITTER_SIGNIN = 13;
	public static final int REQUEST_CONFIRM_LINKEDIN_SIGNIN = 14;
	public static final int REQUEST_CONFIRM_GOOGLE_PLUS_SIGNIN = 15;
	
	public static final int REQUEST_CONFIRM_EMAIL_RECOVER = 16;
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** Do not change directly; use {@link #setFragmentMode(int)} for flow purposes. */
	private int mFragmentMode = 0;
	
	/** A map of: RequestCode -> PageRequest name.	 */
	private final SparseArray<String> mRequestCodeToPageRequestMap = new SparseArray<String>();
	// Using HashMaps as opposed to SpareArray because easier to store/restore in onSaveInstanceState

	/** Keep a local cache of them for when we need to use/reuse them.
	 * Key:  activity/loader request keys, Value: dialog state holder */
	private SparseArray<DialogStateHolder> mRequestCodeToDialogStateMap =  new SparseArray<DialogStateHolder>();
	
	
	/** Reserved for the activity to listen on. */
	private OnClickListener mFooterListener = null;
	
	private View mSocialEmail = null;
	private View mSocialFacebook = null; 
	private View mSocialGooglePlus = null;
	private View mSocialLinkedIn = null;
	private View mSocialTwitter = null;
	
	private TextView mContentText = null;
	
	private View mProgressView = null;
	
	
	private String mSigninContent = null;
	private String mSignupContent = null;
		
	private int mSignupComplete = -1;
	
	public static SignUpSignInFragment newInstance(int mode) {
		SignUpSignInFragment fragment = new SignUpSignInFragment();
		Bundle args = new Bundle();
		args.putInt(KEY_MODE, mode);
		fragment.setArguments(args);
		return fragment;
	}

	public SignUpSignInFragment() {
		// Required empty public constructor
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_MODE, mFragmentMode);
		outState.putString(KEY_SIGNIN_CONTENT, mSigninContent);
		outState.putString(KEY_SIGNUP_CONTENT, mSignupContent);
		
		outState.putSparseParcelableArray(KEY_DIALOG_STATE, mRequestCodeToDialogStateMap);
		outState.putInt(KEY_SIGN_UP_COMPLETE, mSignupComplete);
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		populateMaps(); //populate first then restore if possible
		
		if (savedInstanceState != null) {
			mRequestCodeToDialogStateMap = savedInstanceState.getSparseParcelableArray(KEY_DIALOG_STATE);
			mSignupComplete = savedInstanceState.getInt(KEY_SIGN_UP_COMPLETE);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_signup_signin,
				container, false);
		
		SigninHeadlessFragment frag = getSigninFrag();
		frag.setTarget(this);
		frag.setAutoSignin(false);
		
		mProgressView = Utility.getProgressView(rootView);
		
		mContentText = (TextView) rootView.findViewById(R.id.ibs_signupsignin_text_content);
		
		mSocialEmail = Utility.setOnClickAndReturnView(rootView, R.id.ibs_social_button_email, this);
		mSocialFacebook = Utility.setOnClickAndReturnView(rootView,R.id.ibs_social_button_facebook, this);
		mSocialGooglePlus = Utility.setOnClickAndReturnView(rootView,R.id.ibs_social_button_google_plus, this);
		mSocialLinkedIn = Utility.setOnClickAndReturnView(rootView,R.id.ibs_social_button_linkedin, this);
		mSocialTwitter = Utility.setOnClickAndReturnView(rootView,R.id.ibs_social_button_twitter, this);
		
		if (savedInstanceState != null){			
			mSigninContent = savedInstanceState.getString(KEY_SIGNIN_CONTENT);
			mSignupContent = savedInstanceState.getString(KEY_SIGNUP_CONTENT);			

			setFragmentMode(savedInstanceState.getInt(KEY_MODE));
			
		} else if (getArguments() != null) {
			setFragmentMode(getArguments().getInt(KEY_MODE));
		}
		
		
		FooterViewActionWrapper.newInstance(getActivity(), rootView, false);
		
		return rootView;
	}
	
	@Override //quick and easy interaction listener; not the right way but it works.
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		try {
			mFooterListener = (OnClickListener) getActivity();
			
		} catch (ClassCastException notImplemented) {
			Log.w(LOGTAG, "Activity must implement 'OnClickListener'");
			throw notImplemented;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {	
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_EMAIL_SIGN_UP:
				processEmailSignupResult(resultCode);					
				break;
				
			case REQUEST_CONFIRM_EMAIL_SIGNIN: 
				if (resultCode == DialogInterface.BUTTON_POSITIVE) {
					launchEmailSignin(mSocialEmail);
				}
				break;
				
			case REQUEST_EMAIL_SIGN_IN:
				processEmailSignin(resultCode, data);
				break;
				
			case REQUEST_EMAIL_RECOVERY_QUERY:
				if (resultCode == EmailPasswordRecoveryTooltip.RESULT_SUBMIT) {
					Bundle args = new Bundle();
					args.putString(EmailPasswordRecoveryTooltip.KEY_EMAIL, 
							data.getStringExtra(EmailPasswordRecoveryTooltip.KEY_EMAIL));
					
					getLoaderManager().initLoader(REQUEST_EMAIL_RECOVERY_QUERY, args, mForgotPasswordLoaderCallbacks);
				}
				break;
				
				

			case REQUEST_CONFIRM_EMAIL_RECOVER:
			case REQUEST_CONFIRM_FACEBOOK_SIGNIN:
			case REQUEST_CONFIRM_GOOGLE_PLUS_SIGNIN:
			case REQUEST_CONFIRM_LINKEDIN_SIGNIN:
			case REQUEST_CONFIRM_TWITTER_SIGNIN:
			case REQUEST_CONFIRM_ALL:
				if (DialogInterface.BUTTON_POSITIVE == resultCode) {
					SigninHeadlessFragment.get(getActivity()).setAutoSignin(true);
					launchNextActivity();
					
				}
				break;
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helpers start here
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @return The headless fragment. If the fragment is not initialized, intializes it. */
	private SigninHeadlessFragment getSigninFrag() {
		SigninHeadlessFragment frag = SigninHeadlessFragment.get(getActivity());
		return frag;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Simple Helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void populateMaps() {
		mRequestCodeToPageRequestMap.put(REQUEST_EMAIL_RECOVERY_QUERY, getString(R.string.ibs_config_load_emailRecoveryQuery));
		mRequestCodeToPageRequestMap.put(REQUEST_CONFIRM_EMAIL_RECOVER, getString(R.string.ibs_config_load_emailRecoveryConfirm));
		
		mRequestCodeToPageRequestMap.put(REQUEST_CONFIRM_EMAIL_SIGNIN, getString(R.string.ibs_config_load_emailSigninConfirm));
		mRequestCodeToPageRequestMap.put(REQUEST_CONFIRM_FACEBOOK_SIGNIN, getString(R.string.ibs_config_load_facebookSigninConfirm));
		mRequestCodeToPageRequestMap.put(REQUEST_CONFIRM_TWITTER_SIGNIN, getString(R.string.ibs_config_load_twitterSigninConfirm));
		mRequestCodeToPageRequestMap.put(REQUEST_CONFIRM_LINKEDIN_SIGNIN, getString(R.string.ibs_config_load_linkedInSigninConfirm));
		mRequestCodeToPageRequestMap.put(REQUEST_CONFIRM_GOOGLE_PLUS_SIGNIN, getString(R.string.ibs_config_load_googlePlusSigninConfirm));
		
		
		mRequestCodeToDialogStateMap.put(REQUEST_EMAIL_RECOVERY_QUERY, new DialogStateHolder());
		mRequestCodeToDialogStateMap.put(REQUEST_CONFIRM_EMAIL_RECOVER, new DialogStateHolder());
		
		mRequestCodeToDialogStateMap.put(REQUEST_CONFIRM_EMAIL_SIGNIN, new DialogStateHolder());
		mRequestCodeToDialogStateMap.put(REQUEST_CONFIRM_FACEBOOK_SIGNIN, new DialogStateHolder());
		mRequestCodeToDialogStateMap.put(REQUEST_CONFIRM_TWITTER_SIGNIN, new DialogStateHolder());
		mRequestCodeToDialogStateMap.put(REQUEST_CONFIRM_LINKEDIN_SIGNIN, new DialogStateHolder());
		mRequestCodeToDialogStateMap.put(REQUEST_CONFIRM_GOOGLE_PLUS_SIGNIN, new DialogStateHolder());
	}
	
	/** Mutator to provide easy access to mode changes. */
	private void setFragmentMode(int mode) {
		mFragmentMode = mode;
		checkLoadContentText();
	}
	

	private boolean isSignInMode(){
		return mFragmentMode == VALUE_SIGN_IN_MODE;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper Checkers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Checks to see if there is a non-null result at given it. If there is null
	 * launch the loader.
	 * @param id
	 */
	private void checkAndLaunchLoader(int id) {
		if (mRequestCodeToDialogStateMap.get(id).fetched == false) {
			getLoaderManager().initLoader(id, null, this);
		}
	}
	
	
	/** Starts the relevant loader, based on the current mode & if the data has not been
	 * already pre-loaded */
	private void checkLoadContentText() {
		if (isSignInMode()) {
			if (mSigninContent == null) {
				getLoaderManager().initLoader(REQUEST_SIGNIN_LOADER, null, this);				
			} else {
				mContentText.setText(mSigninContent);
				checkIfLoading(false);
			}
		} else {
			if (mSignupContent == null) {
				getLoaderManager().initLoader(REQUEST_SIGNUP_LOADER, null, this);
			} else {
				mContentText.setText(mSignupContent);
				checkIfLoading(false);
			}
		}
	}
	
	/**
	 * Performs view safety checks, then animates views (if forced) or checks whether to
	 * animate views based on loader state.
	 * @param force
	 */
	private void checkIfLoading(boolean force) {
		if (mProgressView == null || mContentText == null) {
			//if either view is null, we cannot animate them.
			return;
		}
		if (force) { //makes the spinner spin, if necessary.
			Utility.switchFadeProgressViews(mProgressView, mContentText, true);
		} else {
			Utility.switchIfLoading(
					getLoaderManager(), 
					isSignInMode() ? REQUEST_SIGNIN_LOADER : REQUEST_SIGNUP_LOADER, 
					mProgressView, mContentText);
		}
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helpers - Process & parse responses
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Processes email sign in. */
	private void processEmailSignin(int resultCode, Intent data) {
		switch (resultCode) {
		case EmailSigninTooltip.RESULT_SUBMIT: 
			Bundle args =new Bundle();
			args.putString(SigninHeadlessFragment.KEY_EMAIL, 
					data.getStringExtra(EmailSigninTooltip.KEY_EMAIL));
			args.putString(SigninHeadlessFragment.KEY_PASSWORD, 
					data.getStringExtra(EmailSigninTooltip.KEY_PASSWORD));
			
			getSigninFrag().launchEmailSignin(args);
			
			break;

		case EmailSigninTooltip.RESULT_FORGOT_PASSWORD: 
			launchEmailForgotPass(mSocialEmail, data.getStringExtra(EmailSigninTooltip.KEY_EMAIL));
			
			//fall through
		case EmailSigninTooltip.RESULT_CANCEL:
			findAndDismissDialog(TAG_EMAIL_SIGNIN);
			break;
		}
	}
	
	/** Attempts to find and set the dialog message, while caching it on this fragment.
	 * @param requestId The dialog  */
	private void getAndSetMessage(int requestId, ContentResponse data) {
		
		String message = getString(R.string.ibs_error_cannotLoadContent);
		if (data != null) {
			message = data.getContent();
			mRequestCodeToDialogStateMap.get(requestId).fetched = true;
		} else {
			mRequestCodeToDialogStateMap.get(requestId).fetched = false;
		}
		
		mRequestCodeToDialogStateMap.get(requestId).message = message;
		
		Fragment frag = getFragmentManager().findFragmentByTag(generateTag(requestId));
		if (frag != null && frag instanceof UpdateDialogMessage) {
			((UpdateDialogMessage) frag).updateMessage(message);
		}		
	}
	
	/**
	 * Processes the email result from {@link SignUpEmailActivity}. 
	 * @param resultCode
	 */
	private void processEmailSignupResult(int resultCode) {
		switch (resultCode) {
		case SignUpEmailActivity.RESULT_OK:
			checkAndLaunchLoader(REQUEST_CONFIRM_EMAIL_SIGNIN);
			setFragmentMode(VALUE_SIGN_IN_MODE);			
			launchTooltipWithMessage(R.layout.dialog_signup_confirm, mSocialEmail, 
					REQUEST_CONFIRM_EMAIL_SIGNIN);
			break;
			
		case SignUpEmailActivity.RESULT_FACEBOOK_SIGN_UP:
			onClick(mSocialFacebook); //fake clicks for simplicity
			break;
			
		case SignUpEmailActivity.RESULT_GOOGLE_PLUS_SIGN_UP:
			onClick(mSocialGooglePlus);
			break;
			
		case SignUpEmailActivity.RESULT_LINKEDIN_SIGN_UP:
			onClick(mSocialLinkedIn);
			break;
			
		case SignUpEmailActivity.RESULT_TWITTER_SIGN_UP:
			onClick(mSocialTwitter);
			break;
			
		default:
			//nothing else
		}
	}
	

	/**
	 * Parses the content response and determines, based id and contents, 
	 * how to set the members and output
	 * @param data
	 * @param id The request id.
	 */
	private void parseSignupSigninResponse(ContentResponse data, int id) {
		if (data == null) {			
			mContentText.setText(R.string.ibs_error_cannotLoadContent);
		} else {
			switch (id) {
			case REQUEST_SIGNIN_LOADER:
				mSigninContent = data.getContent();
				break;
			case REQUEST_SIGNUP_LOADER:
				mSignupContent = data.getContent();
				break;
			}
			mContentText.setText(data.getContent());
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helpers - Dialogs
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Launches the email sign in with email & password. */
	private void launchEmailSignin(View v) {
		checkAndLaunchLoader(REQUEST_EMAIL_RECOVERY_QUERY);		
		showDialog(EmailSigninTooltip.newInstance(v), TAG_EMAIL_SIGNIN, REQUEST_EMAIL_SIGN_IN);
	}
	
	/** Launches the email forgot, with email & restore. */
	private void launchEmailForgotPass(View v, String email) {
		String message = mRequestCodeToDialogStateMap.get(REQUEST_EMAIL_RECOVERY_QUERY).message;
		String tag = generateTag(REQUEST_EMAIL_RECOVERY_QUERY);
		checkAndLaunchLoader(REQUEST_CONFIRM_EMAIL_RECOVER);		
		showDialog(EmailPasswordRecoveryTooltip.newInstance(v, email, message), tag, REQUEST_EMAIL_RECOVERY_QUERY);
	}
	
	/** @return A class unique tag for the dialog, based on the request. */
	private String generateTag(int requestId) {
		return CLASS_NAME + "." + mRequestCodeToPageRequestMap.get(requestId);
	}
	
	/**
	 * Creates and launches dialog.
	 * @param layoutId Child view to use. 
	 * @param anchor The visible anchor view to measure against.
	 * @param message The message to give the dialog 
	 * @param requestCode The request.
	 */
	private void launchTooltip(View anchor, String message) {
		String tag = anchor.toString()+message;
		new MessageToolTip.Builder(message, anchor).build()
			.show(getFragmentManager(), tag);
	}
	
	/**
	 * Creates and launches dialog.
	 * @param layoutId Child view to use. 
	 * @param anchor The visible anchor view to measure against.
	 * @param message The message to give the dialog 
	 * @param requestCode The request.
	 */
	private void launchTooltipWithMessage(View anchor, int requestCode) {
		String tag = generateTag(requestCode);
		String message = mRequestCodeToDialogStateMap.get(requestCode).message;
		showDialog(new MessageToolTip.Builder(message, anchor).build(), tag, requestCode);
	}
	
	/**
	 * Creates and launches dialog.
	 * @param layoutId Child view to use. 
	 * @param anchor The visible anchor view to measure against.
	 * @param message The message to give the dialog 
	 * @param requestCode The request.
	 */
	private void launchTooltipWithMessage(int layoutId, View anchor, int requestCode) {
		String tag = generateTag(requestCode);
		String message = mRequestCodeToDialogStateMap.get(requestCode).message;
		showDialog(new MessageToolTip.Builder(layoutId, message, anchor).build(), tag, requestCode);
	}

	
	/**
	 * Sets a target fragment for the given dialog, and shows the dialog.
	 * @param dialog
	 * @param tag
	 * @param requestCode
	 */
	private void showDialog(DialogFragment dialog, String tag, int requestCode) {
		dialog.setTargetFragment(this, requestCode);
		dialog.show(getFragmentManager(), tag);
	}

	/** @return The dialog or <code>null</code> if not found. */
	private DialogFragment findDialog(String tag) {
		Fragment frag = getFragmentManager().findFragmentByTag(tag);
		if (frag == null) {
			return null;
		}
		if (frag instanceof DialogFragment) {
			return ((DialogFragment) frag);			
		} else {
			Log.w(LOGTAG, "That's not a dialog. Did you make a mistake?" + frag);
		}
		return null;
	}
	
	
	/** Finds the dialog and attempts to dismiss it if found. */
	private void findAndDismissDialog(String tag) {
		DialogFragment dialog = findDialog(tag);
		if (dialog != null) {
			dialog.dismiss();
		}
		
	}
	
	/** Cheks to see if setup has been done before. If so, skip it. If not
	 * launch it.
	 */
	private void launchNextActivity() {
		if (CurrentUser.hasCompletedSetup(getActivity())) {
			startActivity(new Intent(getActivity(), ProfileActivity.class));
		} else {
			startActivity(new Intent(getActivity(), SetupAccountActivity.class));
		}
		getActivity().finish();
	}
	
	private SignUpSignInFragment self() {
		if (getFragmentManager() != null && getTag() != null) {
			SignUpSignInFragment self = (SignUpSignInFragment) getFragmentManager().findFragmentByTag(getTag());
			if (self != null) {
				return self;
			}
		}
		return this;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onSocialSigninError(final int requestCode, final String message) {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				switch(requestCode) {				
				case SigninHeadlessFragment.REQUEST_MODE_GOOGLE:
					self().launchTooltip(mSocialGooglePlus, message);
					break;
					
				case SigninHeadlessFragment.REQUEST_MODE_FACEBOOK:
					self().launchTooltip(mSocialFacebook, message);
					break;
					
				case SigninHeadlessFragment.REQUEST_MODE_LINKEDIN:
					self().launchTooltip(mSocialLinkedIn, message);
					break;
					
				case SigninHeadlessFragment.REQUEST_MODE_TWITTER:
					self().launchTooltip(mSocialTwitter, message);
					break;
					
				default:
					Log.w(LOGTAG, "Ooops! Forgot to implement the requestCode:" + requestCode);
				}
			}
		});
	}
	
	@Override
	public void onSocialSiginResponse(final int requestCode, final SocialSigninResponse response) {
		
		new Handler().post(new Runnable() {
			
			@Override
			public void run() {						
				switch(requestCode) {
				
				case SigninHeadlessFragment.REQUEST_MODE_GOOGLE:
					self().launchTooltipWithMessage(R.layout.dialog_signup_confirm, mSocialGooglePlus, 
							REQUEST_CONFIRM_GOOGLE_PLUS_SIGNIN);
					self().mSignupComplete = REQUEST_CONFIRM_GOOGLE_PLUS_SIGNIN;
					break;
					
				case SigninHeadlessFragment.REQUEST_MODE_FACEBOOK:
					self().launchTooltipWithMessage(R.layout.dialog_signup_confirm, mSocialFacebook, 
							REQUEST_CONFIRM_FACEBOOK_SIGNIN); 
					self().mSignupComplete = REQUEST_CONFIRM_FACEBOOK_SIGNIN;
					break;
					
				case SigninHeadlessFragment.REQUEST_MODE_LINKEDIN:
					self().launchTooltipWithMessage(R.layout.dialog_signup_confirm, mSocialLinkedIn, 
							REQUEST_CONFIRM_LINKEDIN_SIGNIN);
					self().mSignupComplete = REQUEST_CONFIRM_LINKEDIN_SIGNIN;
					break;
					
				case SigninHeadlessFragment.REQUEST_MODE_TWITTER:
					self().launchTooltipWithMessage(R.layout.dialog_signup_confirm, mSocialTwitter, 
							REQUEST_CONFIRM_TWITTER_SIGNIN);
					self().mSignupComplete = REQUEST_CONFIRM_TWITTER_SIGNIN;
					break;
					
				default:
					Log.w(LOGTAG, "Ooops! Forgot to implement the requestCode:" + requestCode);
				}		
			}
		});		
	}

	@Override
	public void onEmailSigninResponse(final EmailSigninResponse response) {
		if (response != null) { //success (probably)
			final String emailError = response.getEmailErrorMessage();
			final String passwordError = response.getPasswordErrorMessage();
			
			final String invalid = self().getString(R.string.ibs_error_passwordOrEmailInvalid);
			
			final EmailSigninTooltip dialog = (EmailSigninTooltip) self().findDialog(TAG_EMAIL_SIGNIN);
			if (dialog != null) {
				//if there are no errors, dismiss and launch.
				new Handler().post(new Runnable() {										
					@Override
					public void run() {
						if (dialog.setErrors(emailError, passwordError)) {	//if no supplied errors
							if (!response.isSuccessful()) { //but we are still unsuccessful
								dialog.setErrors("", invalid); //probably invalid password
							} else {
									dialog.dismiss();
									self().launchNextActivity();
							}
						}
					}
				});
			}						
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Loaders
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private LoaderManager.LoaderCallbacks<ForgotPasswordResponse> mForgotPasswordLoaderCallbacks =
			new LoaderManager.LoaderCallbacks<ForgotPasswordResponse>() {
				
				@Override
				public void onLoaderReset(Loader<ForgotPasswordResponse> arg0) {}
				
				@Override
				public void onLoadFinished(Loader<ForgotPasswordResponse> loader,
						final ForgotPasswordResponse response) {
					getLoaderManager().destroyLoader(loader.getId());
					
					new Handler().post(new Runnable() {
						
						@Override
						public void run() {
							final DialogFragment dialog = findDialog(generateTag(REQUEST_EMAIL_RECOVERY_QUERY));
							
							if (response == null && mSocialEmail != null) {
								String message = getString(R.string.ibs_error_cannotConnect);
								if (dialog != null) {
									((EmailPasswordRecoveryTooltip) dialog).setErrors(message);
								} else {
									launchTooltip(mSocialEmail, message);
								}
								return;
							}
							
							String errors = response.getEmailErrorMessage();
							
							if (mSocialEmail != null && response.isSuccessful() && errors.isEmpty()) {
								launchTooltipWithMessage(mSocialEmail, REQUEST_CONFIRM_EMAIL_RECOVER);
								if (dialog != null){
									dialog.dismiss();
								}
							} else if (dialog != null) {
								((EmailPasswordRecoveryTooltip) dialog).setErrors(errors);
							}
						}
					});
					
				}
				
				
				@Override
				public Loader<ForgotPasswordResponse> onCreateLoader(int id, Bundle args) {
					return new ForgotPasswordAsyncLoader(
							getActivity(), 
							args.getString(EmailPasswordRecoveryTooltip.KEY_EMAIL));
				}
			};
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start contents loaders
	////////////////////////////////////////////////////////////////////////////////////////////////
			
	@Override
	public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case REQUEST_SIGNIN_LOADER:
			checkIfLoading(true);			
			return new SimpleContentAsyncLoader(getActivity(), getString(R.string.ibs_config_load_signin));
						
		case REQUEST_SIGNUP_LOADER:
			checkIfLoading(true);		
			return new SimpleContentAsyncLoader(getActivity(), getString(R.string.ibs_config_load_signup));

		case REQUEST_EMAIL_RECOVERY_QUERY:
		case REQUEST_CONFIRM_EMAIL_RECOVER:
		case REQUEST_CONFIRM_FACEBOOK_SIGNIN:
		case REQUEST_CONFIRM_GOOGLE_PLUS_SIGNIN:
		case REQUEST_CONFIRM_LINKEDIN_SIGNIN:
		case REQUEST_CONFIRM_TWITTER_SIGNIN:
		case REQUEST_CONFIRM_EMAIL_SIGNIN:
			return new SimpleContentAsyncLoader(getActivity(), mRequestCodeToPageRequestMap.get(id));
			
		default:
			throw new UnsupportedOperationException("Loader is not specified; check your request code");
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
		 case REQUEST_SIGNIN_LOADER:
		 case REQUEST_SIGNUP_LOADER:
			 parseSignupSigninResponse(data, id);
			 break;
			 
		case REQUEST_EMAIL_RECOVERY_QUERY:
		case REQUEST_CONFIRM_EMAIL_RECOVER:
		case REQUEST_CONFIRM_FACEBOOK_SIGNIN:
		case REQUEST_CONFIRM_GOOGLE_PLUS_SIGNIN:
		case REQUEST_CONFIRM_LINKEDIN_SIGNIN:
		case REQUEST_CONFIRM_TWITTER_SIGNIN:
		case REQUEST_CONFIRM_EMAIL_SIGNIN:
			getAndSetMessage(id, data);
			break;
			
		default:
			Log.w(LOGTAG, "Wait, what is this id? We were not expecting this id!: " + loader.getId());
			break;
		}
		  
		checkIfLoading(false);
	}
	 
	////////////////////////////////////////////////////////////////////////////////////////////////
	////  End loader listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onClick(View v) {
			
		switch (v.getId()) {
		
		case R.id.ibs_social_button_email:
			if (isSignInMode()) {
				launchEmailSignin(v);
				
			} else {
				Intent signUp = new Intent(getActivity(), SignUpEmailActivity.class);
				startActivityForResult(signUp, REQUEST_EMAIL_SIGN_UP); 
			}
			
			break;
		case R.id.ibs_social_button_facebook:
			if (REQUEST_CONFIRM_FACEBOOK_SIGNIN == mSignupComplete) {
				launchTooltipWithMessage(R.layout.dialog_signup_confirm, mSocialFacebook, 
						REQUEST_CONFIRM_FACEBOOK_SIGNIN);
				return;
			}
			getSigninFrag().launchRequest(SigninHeadlessFragment.REQUEST_MODE_FACEBOOK);
			checkAndLaunchLoader(REQUEST_CONFIRM_FACEBOOK_SIGNIN); //pre-emptive fetching
			break;
			
		case R.id.ibs_social_button_google_plus:
			if (REQUEST_CONFIRM_GOOGLE_PLUS_SIGNIN == mSignupComplete) {
				launchTooltipWithMessage(R.layout.dialog_signup_confirm, mSocialGooglePlus, 
						REQUEST_CONFIRM_GOOGLE_PLUS_SIGNIN);
				return;
			}
			getSigninFrag().launchRequest(SigninHeadlessFragment.REQUEST_MODE_GOOGLE);
			checkAndLaunchLoader(REQUEST_CONFIRM_GOOGLE_PLUS_SIGNIN);
			break;
			
		case R.id.ibs_social_button_linkedin:
			if (REQUEST_CONFIRM_LINKEDIN_SIGNIN == mSignupComplete) {
				launchTooltipWithMessage(R.layout.dialog_signup_confirm, mSocialLinkedIn, 
						REQUEST_CONFIRM_LINKEDIN_SIGNIN);
				return;
			}
			getSigninFrag().launchRequest(SigninHeadlessFragment.REQUEST_MODE_LINKEDIN);
			checkAndLaunchLoader(REQUEST_CONFIRM_LINKEDIN_SIGNIN);
			break;
			
		case R.id.ibs_social_button_twitter:
			if (REQUEST_CONFIRM_TWITTER_SIGNIN == mSignupComplete) {
				launchTooltipWithMessage(R.layout.dialog_signup_confirm, mSocialTwitter, 
						REQUEST_CONFIRM_TWITTER_SIGNIN);
				return;
			}
			getSigninFrag().launchRequest(SigninHeadlessFragment.REQUEST_MODE_TWITTER);
			checkAndLaunchLoader(REQUEST_CONFIRM_TWITTER_SIGNIN);
			break;
			
		default: //fall through to main
			mFooterListener.onClick(v);
			break;
		}
	}

}
