package com.inductivebiblestudyapp.auth;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.Name;
import com.google.android.gms.plus.model.people.Person.PlacesLived;
import com.google.gson.GsonBuilder;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.twitter.TwitterLoginActivity;
import com.inductivebiblestudyapp.auth.twitter.TwitterSigninAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.EmailSigninAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.SocialSigninAsyncLoader;
import com.inductivebiblestudyapp.data.model.EmailSigninResponse;
import com.inductivebiblestudyapp.data.model.SocialSigninResponse;
import com.inductivebiblestudyapp.util.PreferenceUtil;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;

/*
 * To any future developers out there this should *really* be split
 * into about 5 classes. Single responsibility principle and all that. 
 * 
 * Perhaps put into a service.
 */
/**
 * Created to avoid repeating repetitive connection actions.
 * Initialize in onCreate. Call #onStart() and #onStop() in their respective methods.
 * Must be used in combination with {@link SigninCompatActivity}.
 * Must call {@link #setTarget(OnSigninResultListener)}.
 * 
 * @author Jason Jenkins
 * @version 0.9.6-20150915
 */
public class SigninHeadlessFragment extends Fragment
	implements ConnectionCallbacks, OnConnectionFailedListener {
	
	final static private String CLASS_NAME = SigninHeadlessFragment.class
			.getSimpleName();
	private static String LOGTAG = CLASS_NAME;
	
	@SuppressWarnings("unused")
	private static boolean DEBUG = true;
	
	//remember to check validateRequestMode(int)
	private static final int REQUEST_MODE_EMAIL = 0x100; //this has its own method
	
	public static final int REQUEST_MODE_GOOGLE = 0x101;
	public static final int REQUEST_MODE_FACEBOOK = 0x102;
	public static final int REQUEST_MODE_LINKEDIN = 0x103;
	public static final int REQUEST_MODE_TWITTER = 0x104;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End int constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final String KEY_EMAIL = CLASS_NAME + ".KEY_EMAIL";
	public static final String KEY_PASSWORD = CLASS_NAME + ".KEY_PASSWORD";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End public constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static final String TAG = SigninHeadlessFragment.class.getName();
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static SigninHeadlessFragment newInstance() {
		return new SigninHeadlessFragment();
	}
	
	public static SigninHeadlessFragment get(FragmentActivity activity) {
		final FragmentManager manager = activity.getSupportFragmentManager();		
		SigninHeadlessFragment frag = (SigninHeadlessFragment) manager.findFragmentByTag(TAG);
		
		if (frag == null) {
			frag = SigninHeadlessFragment.newInstance();
			manager.beginTransaction()
				.add(frag, TAG)
				.commit();
		} 
		return frag;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	//default for faster accessing 
	OnSigninResultListener mOnResultsListener = null; 
	
	private Context mAppContext = null;
	
	private final SparseArray<String> mPrefSigninTypes = new SparseArray<String>();
	
	private boolean mAutoSignin = true;
	
	/** The request code and mode to use for the fragment. */
	private int mRequestCodeMode = -1;
	/** The picture to attempt to fetch. Default value is null */
	private String mPictureUrl = null;
	
	private CallbackManager mFacebookCallbackManager = null;
	boolean mFacebookIntentInProgress = false;
	
	private GoogleApiClient mGoogleApiClient = null;
	boolean mGoogleIntentInProgress = false;
	boolean mGoogleUserInitiated = false;
	
	private boolean mLinkedInIntentInProgess = false;

	private Twitter mTwitter = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		setRetainInstance(true); //BEWARE OF CONTEXT!
		
		mAppContext = getActivity().getApplicationContext(); //somewhat safe
		
		mPrefSigninTypes.append(REQUEST_MODE_EMAIL, getString(R.string.ibs_pref_VALUE_SIGNIN_EMAIL));
		mPrefSigninTypes.append(REQUEST_MODE_FACEBOOK, getString(R.string.ibs_pref_VALUE_SIGNIN_FACEBOOK));
		mPrefSigninTypes.append(REQUEST_MODE_GOOGLE, getString(R.string.ibs_pref_VALUE_SIGNIN_GOOGLE));
		mPrefSigninTypes.append(REQUEST_MODE_LINKEDIN, getString(R.string.ibs_pref_VALUE_SIGNIN_LINKEDIN));
		mPrefSigninTypes.append(REQUEST_MODE_TWITTER, getString(R.string.ibs_pref_VALUE_SIGNIN_TWITTER));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//headless
		return null;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		//connect(mRequestCodeMode);	
	}
	
	@Override
	public void onStop() {
		super.onStop();
		//disconnect(mRequestCodeMode);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mGoogleApiClient = null;
		mFacebookCallbackManager = null;
	}
		
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		try {
			SigninCompatActivity activity = (SigninCompatActivity) getActivity();
			
			activity.onCheckIfSignedIn();
			
		} catch (ClassCastException e) {
			Log.d(LOGTAG, "Parent activity must extend: " + 
					SigninCompatActivity.class.getSimpleName());
			throw e;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		if (REQUEST_MODE_TWITTER == requestCode) {
			twitterOnActivityResult(requestCode, resultCode, data);
		}
		
	}
	
	@Override
	public void setTargetFragment(Fragment fragment, int requestCode) {
		super.setTargetFragment(fragment, requestCode);
		
		mOnResultsListener = (OnSigninResultListener) fragment;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Additional methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public boolean isAutoSignin() {
		return mAutoSignin;
	}
	
	public void setAutoSignin(boolean autoSignin) {
		this.mAutoSignin = autoSignin;
	}
		
	/**
	 * Whether or not the fragment consumes the activity result. 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 * @return <code>true</code> to consume, false to not.
	 */
	public boolean consumeActivityResult(int requestCode, int resultCode, Intent data) {
		//Linkedin has no way to set request codes, apparently?
		if (mLinkedInIntentInProgess) {
			LISessionManager.getInstance(mAppContext)
						.onActivityResult(getActivity(), requestCode, resultCode, data);
			mLinkedInIntentInProgess = false;
		}
		if (mFacebookIntentInProgress) {
			if (mFacebookCallbackManager != null) {			
				mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
			}
			mFacebookIntentInProgress = false;
		}
		
		switch (requestCode) {
		case REQUEST_MODE_GOOGLE:
			mGoogleIntentInProgress = false;
			
			if (resultCode != Activity.RESULT_OK) {
				mGoogleUserInitiated = false;				
			}
			
			if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
			      mGoogleApiClient.reconnect();
		    }

			break;
			
		case REQUEST_MODE_FACEBOOK:			
			break;			
		case REQUEST_MODE_LINKEDIN:
			break;
			
		case REQUEST_MODE_TWITTER:
			twitterOnActivityResult(requestCode, resultCode, data);
			break;

		default:
			return false;
		}
		return true;
	}
	

	public void setTarget(OnSigninResultListener listener) {
		mOnResultsListener = listener;
	}
	
	/**
	 * Clears any previous signins & launches the given signin request. 
	 * @param requestCode
	 */
	public void launchRequest(final int requestCode) {
		validateRequestMode(requestCode);
		this.mRequestCodeMode = requestCode;
		clearSigninData();
		connect(requestCode);
	}
	
	/**
	 * Launches the email sign in request.
	 * @param args The sign in args with the username & password set.
	 */
	public void launchEmailSignin(Bundle args) {
		if (args.containsKey(KEY_EMAIL) && args.containsKey(KEY_PASSWORD)) {
			mRequestCodeMode=REQUEST_MODE_EMAIL;
			getLoaderManager().restartLoader(0, args, mEmailSignInLoaderCallbacks);
		} else {
			throw new IllegalArgumentException("Must supply email & password in arg");
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static void validateRequestMode(int requestMode) {
		if (	requestMode != REQUEST_MODE_EMAIL  && requestMode != REQUEST_MODE_GOOGLE &&
				requestMode != REQUEST_MODE_FACEBOOK && requestMode != REQUEST_MODE_LINKEDIN && 
				requestMode != REQUEST_MODE_TWITTER ) {
			throw new IllegalArgumentException("Unsupported mode requested:" + requestMode);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void connect(final int requestCode) {
		mRequestCodeMode = requestCode;
		switch (requestCode) {
		case REQUEST_MODE_GOOGLE:
			mGoogleUserInitiated = true;
			connectGoogle();
			break;
			
		case REQUEST_MODE_FACEBOOK:
			connectFacebook();
			break;
			
		case REQUEST_MODE_LINKEDIN:        	
			connectLinkedIn(null);
			break;
			
		case REQUEST_MODE_TWITTER:
			connectTwitter();
			break;
			
		default:
			break;
		}
	}
	
	private void disconnect(final int mode) {
		switch (mode) {
		case REQUEST_MODE_GOOGLE:
			if (mGoogleApiClient != null) {
				mGoogleApiClient.disconnect();
			}
			break;

		default:
			break;
		}
	}
	
	/** Sets the login type based on the request code. */
	private void setLoginType(final int requestCode) {
		Editor edit = PreferenceUtil.getPreferences(mAppContext).edit();
		edit.putString(
				getString(R.string.ibs_pref_KEY_SIGNIN_TYPE), 
				mPrefSigninTypes.get(requestCode));
		edit.commit();
	}
	
	/** Sets the login userid. */
	private void setUserId(String userId) {
		Editor edit = PreferenceUtil.getPreferences(mAppContext).edit();
		edit.putString(
				getString(R.string.ibs_pref_KEY_USER_SOCIAL_ID), 
				userId);
		edit.commit();
	}
	
	private String getAccessToken() {
		return PreferenceUtil.getPreferences(mAppContext)
				.getString(getString(R.string.ibs_pref_KEY_SIGNIN_ACCESS_TOKEN), "");
	}

	private void storeAccessToken(String accessToken) {
		PreferenceUtil.getPreferences(mAppContext).edit()
			.putString(getString(R.string.ibs_pref_KEY_SIGNIN_ACCESS_TOKEN), accessToken)
			.commit();
	}
	
	private void clearSigninData() {
		Editor edit = PreferenceUtil.getPreferences(mAppContext).edit();
		edit.putString(
				getString(R.string.ibs_pref_KEY_USER_SOCIAL_ID), 
				"");
		edit.putString(getString(R.string.ibs_pref_KEY_SIGNIN_ACCESS_TOKEN), "");
		edit.putString(getString(R.string.ibs_pref_KEY_SIGNIN_ACCESS_TOKEN_SECRET), "");
		edit.commit();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Connect helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void connectGoogle() {
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(mAppContext)
				.addConnectionCallbacks(this)
	         	.addOnConnectionFailedListener(this)
	         	.addApi(Plus.API)
	         	.addScope(new Scope(Scopes.PLUS_LOGIN))
	         	.addScope(new Scope("profile"))
	         	.addScope(new Scope("email"))
	         	.build();
		}
		
     	mGoogleApiClient.connect();
	}
	
	private void connectFacebook() {
		final Collection<String> permissions = Arrays.asList("public_profile", "email");
		FacebookSdk.sdkInitialize(mAppContext);
		 
		 if (mFacebookCallbackManager == null) {
			 mFacebookCallbackManager = CallbackManager.Factory.create();
			 //mFacebookLoginButton.setReadPermissions("user_friends");  
		 }		 
		 LoginManager.getInstance().registerCallback(mFacebookCallbackManager, mFacebookCallback);
		 
		 boolean prevSignIn = PreferenceUtil.getSigninType(mAppContext).equals(getString(R.string.ibs_pref_VALUE_SIGNIN_FACEBOOK));
		 if (prevSignIn && !TextUtils.isEmpty(getAccessToken())) {
			 String accessToken = getAccessToken();
			 String applicationId = getString(R.string.facebook_app_id);
			 String userId = PreferenceUtil.getUserId(mAppContext);
			 
			 com.facebook.AccessToken token = new com.facebook.AccessToken(accessToken, applicationId, userId, permissions, null, null, null, null);
			 com.facebook.AccessToken.setCurrentAccessToken(token);
			 
			 //token.getUserId(); 
			 //Log.d(LOGTAG, "Facebook id: " + token.getUserId() );		 

			 mFacebookIntentInProgress = true;
			 LoginManager.getInstance().logInWithReadPermissions(getActivity(), permissions);
		 } else {	
			 mFacebookIntentInProgress = true;
			 LoginManager.getInstance().logInWithReadPermissions(getActivity(), permissions);
		 }
	}
	
	private void connectLinkedIn(String accessToken) {
		// Build the list of member required permissions         
        com.linkedin.platform.utils.Scope scope = com.linkedin.platform.utils.Scope.build(
        		com.linkedin.platform.utils.Scope.R_BASICPROFILE, 
        		com.linkedin.platform.utils.Scope.W_SHARE);
        
        if (accessToken == null) {
	        mLinkedInIntentInProgess = true;
	        //get and launch session 
	        LISessionManager.getInstance(mAppContext)
	        					.init(getActivity(), scope, mLinkedinAuthListener, true);
        } else {        	
        	//first use token to connect
        	LISessionManager.getInstance(mAppContext)
        		.init(com.linkedin.platform.AccessToken.buildAccessToken(accessToken));
        	//then fetch
        	fetchLinkedInProfile();
        }
            
	}
	
	private void connectTwitter() {
		
		mTwitter = TwitterLoginActivity.getTwitterInstance(mAppContext);
	
		if (PreferenceUtil.getSigninType(mAppContext).equals(getString(R.string.ibs_pref_VALUE_SIGNIN_TWITTER))){
			String token = getAccessToken();
			String secret = PreferenceUtil.getPreferences(mAppContext)
					.getString(getString(R.string.ibs_pref_KEY_SIGNIN_ACCESS_TOKEN_SECRET), null);
			AccessToken accessToken = new AccessToken(token, secret);
			mTwitter.setOAuthAccessToken(accessToken);
			
			try {
				twitterProcessUserId(accessToken.getUserId(), token);
				return;
			} catch (Exception e) {
				Log.e(LOGTAG, "Twitter Login Failed" + e);
			}
		}
	
		
		final Intent intent = new Intent(mAppContext, TwitterLoginActivity.class);
		startActivityForResult(intent, REQUEST_MODE_TWITTER);
		
	 
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Clear connection helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void clearGoogle() {
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.clearDefaultAccountAndReconnect();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Google listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
	  if (!mGoogleIntentInProgress){
		  if (mGoogleUserInitiated && result.hasResolution()) {
		    try {
		      mGoogleIntentInProgress = true;
		      result.startResolutionForResult(getActivity(), REQUEST_MODE_GOOGLE);
		      
		    } catch (SendIntentException e) {
		      // The intent was canceled before it was sent.  Return to the default
		      // state and attempt to connect to get an updated ConnectionResult.
		      mGoogleIntentInProgress = false;
		      mGoogleApiClient.connect();
		    }
		  }
	  } else {
		  //the intent *is* in progress yet we still failed.
		  mGoogleIntentInProgress = false;
		  mOnResultsListener.onSocialSigninError(REQUEST_MODE_GOOGLE, 
					getString(R.string.ibs_error_cannotConnect));
	  }
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// send token to api
		mGoogleIntentInProgress = false;
		mGoogleUserInitiated = false;
	    
		Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
		if (currentPerson != null) {
            CurrentUser user = new CurrentUser(getActivity());
                        
            final Name name = currentPerson.getName();
            if (name != null) {
                user.setFirstName(name.getGivenName());
                user.setLastName(name.getFamilyName());
            }
            
            //Consider: https://code.google.com/p/google-plus-platform/issues/detail?id=620            
            String location = currentPerson.getCurrentLocation();
            if (location == null || location.isEmpty()) {
            	List<PlacesLived> places = currentPerson.getPlacesLived();
            	if (places != null && !places.isEmpty()) {
            		location = places.get(0).getValue();
            	}
            }
            user.setCity(location);    
            user.setEmail(Plus.AccountApi.getAccountName(mGoogleApiClient));
            
            if (currentPerson.getImage() != null) {
            	mPictureUrl = currentPerson.getImage().getUrl();
            }
		}
		
		getLoaderManager().restartLoader(REQUEST_MODE_GOOGLE, null, mSocialSignInCallbacks);
		setLoginType(REQUEST_MODE_GOOGLE);
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Facebook listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
		@Override
        public void onSuccess(LoginResult loginResult) {
			com.facebook.AccessToken accessToken = loginResult.getAccessToken(); 
			storeAccessToken(accessToken.getToken());
			
			Log.d(LOGTAG, "Facebook id: " + accessToken.getUserId());
			setUserId( accessToken.getUserId());
			
			GraphRequest request = GraphRequest.newMeRequest(accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {

                        	final GsonBuilder gsonBuilder = new GsonBuilder();
                        	
                        	if (object != null) {
	                        	//add deserializers here
	                        	FacebookProfileResponse profile = gsonBuilder.create()
	                        				.fromJson(object.toString(), FacebookProfileResponse.class);
	                        	
	                        	if (profile != null) {
		                        	Log.d(LOGTAG, "Facebook JSON: "  + object.toString() );
		                			Log.d(LOGTAG, "Facebook: "  + profile.toString() );
		                			
		                			CurrentUser user = new CurrentUser(getActivity());
		                			user.setFirstName(profile.getFirstName());
		                			user.setLastName(profile.getLastName());
		                			user.setEmail(profile.getEmail());                			
	                        	}
                        	}
                			
                			getLoaderManager().restartLoader(REQUEST_MODE_FACEBOOK, null, mSocialSignInCallbacks);
                        }
                    });
            Bundle parameters = new Bundle();
            //Thought: request location after gaining facebook approval.
            //See: https://developers.facebook.com/docs/facebook-login/permissions/v2.3#reference-user_location
            parameters.putString("fields", "id,first_name,last_name,email");
            request.setParameters(parameters);
            request.executeAsync();        
            setLoginType(REQUEST_MODE_FACEBOOK);	
        }

        @Override
        public void onCancel() {
        	//nothing to do?
        }

		@Override
		public void onError(FacebookException error) {
			mOnResultsListener.onSocialSigninError(REQUEST_MODE_FACEBOOK, 
					getString(R.string.ibs_error_cannotConnect));
		}
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// LinkedIn listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	//actual profile fetching
	private ApiListener mLinkedInAPIListener = new ApiListener() {
		
		@Override
		public void onApiSuccess(ApiResponse apiResponse) {
			
			//Gson to the rescue!
			GsonBuilder gsonBuilder = new GsonBuilder();
		    gsonBuilder.registerTypeAdapter(LinkedInProfileResponse.class, 
		    		new LinkedInProfileResponse.ProfileDeserializer());
		    gsonBuilder.registerTypeAdapter(LinkedInProfileResponse.Location.class, 
		    		new LinkedInProfileResponse.LocationDeserializer());
		    
		    if (apiResponse != null) {

				Log.d(LOGTAG, "LinkedIn Json: " + apiResponse.getResponseDataAsJson().toString());
				
				LinkedInProfileResponse profile = gsonBuilder.create().fromJson(
						apiResponse.getResponseDataAsJson().toString(), 
						LinkedInProfileResponse.class);
				
				if (profile != null) {					
					Log.d(LOGTAG, "LinkedIn response: " + profile.toString());
					setUserId(profile.getId());
					
					CurrentUser user = new CurrentUser(getActivity());
					user.setFirstName(profile.getFirstName());
					user.setLastName(profile.getLastName());
					user.setCity(profile.getLocation()); 
					user.setEmail(profile.getEmail());
					
					mPictureUrl = profile.getPictureUrl();
				}
				
				getLoaderManager().restartLoader(REQUEST_MODE_LINKEDIN, null, mSocialSignInCallbacks);
		    } else {
		    	mOnResultsListener.onSocialSigninError(REQUEST_MODE_LINKEDIN, 
						getString(R.string.ibs_error_cannotConnect));
		    }
		}
		
		@Override
		public void onApiError(LIApiError LIApiError) {
			mOnResultsListener.onSocialSigninError(REQUEST_MODE_LINKEDIN, 
					getString(R.string.ibs_error_cannotConnect));
		}
	};
	
	//Initial authorization
	private AuthListener mLinkedinAuthListener = new AuthListener() {
		
		@Override
		public void onAuthSuccess() {
			
			Log.d(LOGTAG, "LinkedIn onAuthSuccess ");
			
			String accessToken = LISessionManager.getInstance(mAppContext)
							.getSession().getAccessToken().toString();
			
			storeAccessToken(accessToken);			
			
			fetchLinkedInProfile();
			
			setLoginType(REQUEST_MODE_LINKEDIN);			
		}
		
		@Override
		public void onAuthError(LIAuthError error) {
			mOnResultsListener.onSocialSigninError(REQUEST_MODE_LINKEDIN, 
					getString(R.string.ibs_error_cannotConnect));
		}
	};
	


	/** Performs APIHelper linkedin request. */
	private void fetchLinkedInProfile() {
		//https://developer.linkedin.com/docs/android-sdk 
		//Oh, linkedin, your documentation is always so... broken?
		
		APIHelper apiHelper = APIHelper.getInstance(mAppContext);
		apiHelper.getRequest(mAppContext, LinkedInProfileResponse.QUERY, mLinkedInAPIListener);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Twitter methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void twitterOnActivityResult(int requestCode, int resultCode, Intent data) {
		//FIXME the app crashes in this method for unknown reasons
		if (resultCode == TwitterLoginActivity.RESULT_OK) {
			Bundle extras = data.getExtras();
			final String accessToken = extras.getString(TwitterLoginActivity.RESULT_EXTRA_ACCESS_TOKEN);
			final String tokenSecret = extras.getString(TwitterLoginActivity.RESULT_EXTRA_TOKEN_SECRET);
			final long userId = extras.getLong(TwitterLoginActivity.RESULT_EXTRA_USER_ID);
			final String screenName = extras.getString(TwitterLoginActivity.RESULT_EXTRA_SCREEN_NAME);
			
			storeAccessToken(accessToken);
			
			
			PreferenceUtil.getPreferences(mAppContext).edit()
				.putString(getString(R.string.ibs_pref_KEY_SIGNIN_ACCESS_TOKEN_SECRET), tokenSecret)
				.commit();
		
			setLoginType(REQUEST_MODE_TWITTER);	
			
			twitterProcessUserId(userId, accessToken);
				
		} else if (requestCode == TwitterLoginActivity.RESULT_FAILED) {
			mOnResultsListener.onSocialSigninError(REQUEST_MODE_TWITTER, 
					getString(R.string.ibs_error_cannotConnect));
			
		}
	}

	private long twitterProcessUserId(long userId, String accessToken){
				
		setUserId(""+userId);
		Log.d(LOGTAG, "Twitter id: " + userId);
		//saveTwitterInfo(accessToken);		

		Bundle args = new Bundle();
		args.putString(TwitterLoginActivity.RESULT_EXTRA_ACCESS_TOKEN, accessToken);
		//always send the access token
		getLoaderManager().restartLoader(REQUEST_MODE_TWITTER, args, mSocialSignInCallbacks);
		
		return userId; //FIXME the error is possibly here.
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Loaders
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private LoaderManager.LoaderCallbacks<EmailSigninResponse> mEmailSignInLoaderCallbacks =
			new LoaderManager.LoaderCallbacks<EmailSigninResponse>() {
				
				@Override
				public void onLoaderReset(Loader<EmailSigninResponse> arg0) {}
				
				@Override
				public void onLoadFinished(Loader<EmailSigninResponse> arg0,
						EmailSigninResponse response) {

					Log.d(LOGTAG, "Response: " + response);
					if (response != null && response.getData() != null) {
						setLoginType(REQUEST_MODE_EMAIL);
						CurrentUser.updateUserData(mAppContext, response.getData());
					}
					mOnResultsListener.onEmailSigninResponse(response);
				}
				
				
				@Override
				public Loader<EmailSigninResponse> onCreateLoader(int id, Bundle args) {
					return new EmailSigninAsyncLoader(
							mAppContext, 
							args.getString(KEY_EMAIL), 
							args.getString(KEY_PASSWORD));
				}
			};
			
			
		private LoaderManager.LoaderCallbacks<SocialSigninResponse> mSocialSignInCallbacks =
				new LoaderManager.LoaderCallbacks<SocialSigninResponse>() {
					
					@Override
					public void onLoaderReset(Loader<SocialSigninResponse> arg0) {}
					
					@Override
					public void onLoadFinished(Loader<SocialSigninResponse> loader,
							SocialSigninResponse response) {
						final int id = loader.getId();

						Log.d(LOGTAG, "Response: " + response);
						
						if (response == null) {
							if (mOnResultsListener != null) {								
								mOnResultsListener.onSocialSigninError(id, 
										getString(R.string.ibs_error_cannotConnect));
								return; //end early
							}
						} 
						
						if (response.getData() != null) {
							CurrentUser.updateUserData(mAppContext, response.getData());
						}

						if (mOnResultsListener != null) {								
							mOnResultsListener.onSocialSiginResponse(id, response);
						}
						
						switch (id) {
						case REQUEST_MODE_GOOGLE:							
							break;

						default:
							break;
						}					
					}
					
					
					@Override
					public Loader<SocialSigninResponse> onCreateLoader(int id, Bundle args) {
						//final String profileId = args.getString(KEY_PROFILE_ID);
						final String userId = PreferenceUtil.getUserId(mAppContext);
						
						switch (id) {
						case REQUEST_MODE_GOOGLE:
							String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);			
							if (mPictureUrl == null) {
								return new GoogleSigninAsyncLoader(mAppContext, accountName);
							} //else
							return new GoogleSigninAsyncLoader(mAppContext, accountName, mPictureUrl);
							
						case REQUEST_MODE_FACEBOOK:
							String fbAccessToken = PreferenceUtil.getPreferences(mAppContext)
								.getString(getString(R.string.ibs_pref_KEY_SIGNIN_ACCESS_TOKEN), "");
							
							return new SocialSigninAsyncLoader(mAppContext, getString(R.string.ibs_config_value_signin_facebook), userId, fbAccessToken);
							
						case REQUEST_MODE_LINKEDIN:
							return new SocialSigninAsyncLoader(mAppContext, getString(R.string.ibs_config_value_signin_linkedin), userId, null, mPictureUrl);
							
						case REQUEST_MODE_TWITTER:
							String twitAccessToken = args.getString(TwitterLoginActivity.RESULT_EXTRA_ACCESS_TOKEN);
							return new TwitterSigninAsyncLoader(mAppContext, mTwitter, twitAccessToken, userId);
							
						default:
							return null;
						}
						
					}
				};
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Interaction interface
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * @version 0.2.0-20150618
	 */
	public static interface OnSigninResultListener {
		public void onSocialSiginResponse(int requestCode, SocialSigninResponse response);
		
		/** @param message Message for what went wrong. */
		public void onSocialSigninError(int requestCode, String message);
		
		public void onEmailSigninResponse(EmailSigninResponse response);		
	}
	
}
