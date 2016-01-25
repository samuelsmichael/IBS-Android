package com.inductivebiblestudyapp.auth.twitter;


import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.util.Utility;

/**
 * Simple activity to show the twitter login for Twitter4J.
 * The flow is as follows:
 * <ol><li>Launch activity</li>
 * <li>Wait on result: {@link #RESULT_FAILED} means failure, {@link Activity#RESULT_CANCELED} means
 * closed, {@link Activity#RESULT_OK} means success and is accompanied by results</li>
 * </ol> 
 * 
 * @author Jason Jenkins
 * @version 0.3.2-20150828
 */
public class TwitterLoginActivity extends AppCompatActivity {
	final static private String CLASS_NAME = TwitterLoginActivity.class
			.getSimpleName();
	private static final String LOGTAG = CLASS_NAME;
	

	private static final int REQUEST_MODE_TWITTER = 0x104;
	
	
	public static final String EXTRA_URL =  CLASS_NAME + ".EXTRA_URL";
	
	/** String. */
	public static final String RESULT_EXTRA_ACCESS_TOKEN =  CLASS_NAME + ".RESULT_EXTRA_ACCESS_TOKEN";
	/** String. */
	public static final String RESULT_EXTRA_TOKEN_SECRET =  CLASS_NAME + ".RESULT_EXTRA_TOKEN_SECRET";
	/** Long. */
	public static final String RESULT_EXTRA_USER_ID =  CLASS_NAME + ".RESULT_EXTRA_USER_ID";
	/** String. */
	public static final String RESULT_EXTRA_SCREEN_NAME =  CLASS_NAME + ".RESULT_EXTRA_SCREEN_NAME";

	
	public static final int RESULT_FAILED = 2;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////

	private static final Object sLock = new Object();
	/** safe if application context is used. */
	private static Twitter sTwitterInstance = null;

	/** @returns The sole instance of the twitter sign in for the app. */
	public static Twitter getTwitterInstance(Context context) {
		synchronized (sLock) {
			if (sTwitterInstance == null) {
				newTwitterInstance(context);				
			}
		}		
		return sTwitterInstance;
	}
	
	private static Twitter newTwitterInstance(Context context) {
		synchronized (sLock) {
			context = context.getApplicationContext(); //ensure everyone uses app context
			
			final ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(context.getString(R.string.twitter_consumer_key));
			builder.setOAuthConsumerSecret(context.getString(R.string.twitter_consumer_secret));
			
			final Configuration configuration = builder.build();
			
			final TwitterFactory factory = new TwitterFactory(configuration);
			sTwitterInstance = factory.getInstance();
		}		
		return sTwitterInstance;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	private WebView mWebView = null;
	
	private RequestToken mTwitterRequestToken = null;
	
	
	private ProgressDialog mLoadingDialog = null;
	
	private String mVerifier = "";
	
	private boolean mTwitterValid = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_twitter_login);
		
		getTwitter();
		
		final ProgressBar progress = (ProgressBar) findViewById(R.id.twitter_progress_bar);
		progress.setVisibility(View.GONE);
		
		mWebView = (WebView) findViewById(R.id.twitter_webview);
		
		final String url = this.getIntent().getStringExtra(EXTRA_URL);
		
		if (checkedForAccessToken() && mVerifier != null && !mVerifier.isEmpty()){
			loadAccessToken();
		} else if (null == url || Utility.checkIfLoading(getSupportLoaderManager(), REQUEST_MODE_TWITTER)) {
			//we are waiting for a token.
			mLoadingDialog = ProgressDialog.show(this, "", getString(R.string.ibs_text_loading));

			mWebView.setVisibility(View.GONE);
			//AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, getString(R.string.twitter_oauth_verifier));
			getSupportLoaderManager().initLoader(REQUEST_MODE_TWITTER, null, mTwitterRequestTokenCallbacks);
			
		} else {
			//we are loading twitter
			loadTwitter(url);
		}
	}

	@Override
	public void finish() {
		if (!mTwitterValid) {
			synchronized (sLock) { //invalidate twitter
				sTwitterInstance = null;
			}
		}
		super.finish();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private Twitter getTwitter() {
		return getTwitterInstance(this);
	}
	
	/** Checks for access token, if found uses it. */
	//network on main thread exception
	/*packgage*/ boolean checkedForAccessToken() {
		try {
			AccessToken accessToken = getTwitter().getOAuthAccessToken();
			if (accessToken != null) {
				unpackAccessTokenAndFinish(accessToken);
				return true;
			}
		} catch (TwitterException e) {} catch (IllegalStateException e) {}
		catch (NetworkOnMainThreadException e){}
		return false;
	}

	/*package*/ void loadTwitter(final String url) {
		mWebView.setWebViewClient(new TwitterWebViewClient());
		mWebView.loadUrl(url);
		mWebView.setVisibility(View.VISIBLE);
	}
	
	/*package*/ void loadAccessToken() {
		new TwitterOAuthTask().execute(new String[]{mVerifier});
	}
	
	/*package*/ void unpackAccessTokenAndFinish(AccessToken accessToken) {
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
			mLoadingDialog = null;
		}
		Intent resultIntent = new Intent();
		
		resultIntent.putExtra(RESULT_EXTRA_ACCESS_TOKEN, accessToken.getToken());
		resultIntent.putExtra(RESULT_EXTRA_TOKEN_SECRET, accessToken.getTokenSecret());
		resultIntent.putExtra(RESULT_EXTRA_USER_ID, accessToken.getUserId());
		resultIntent.putExtra(RESULT_EXTRA_SCREEN_NAME, accessToken.getScreenName());
		setResult(RESULT_OK, resultIntent);;
		mTwitterValid = true;
		finish();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////

	class TwitterWebViewClient extends WebViewClient {
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			Log.d(CLASS_NAME, "url:" +url);
			if (url.contains(getResources().getString(R.string.twitter_callback))) {
				if (url.contains("denied")) {
					finish(); //we are not authorized
					return true;
				}
				Uri uri = Uri.parse(url);
				
				/* Sending results back */
				mVerifier = uri.getQueryParameter(getString(R.string.twitter_oauth_verifier));
				loadAccessToken();				
				return true;
			}
			return false;
		}
	}
	
	class TwitterOAuthTask extends AsyncTask<String, Void, AccessToken> {
		
		@Override
		protected AccessToken doInBackground(String... params) {
			String verifier = params[0];
			try {
				return getTwitter().getOAuthAccessToken(mTwitterRequestToken, verifier);
			} catch (TwitterException e) {
				Log.e(LOGTAG, "Twitter Login Failed: " +e);
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(AccessToken accessToken) {
			if (accessToken == null) {
				//TODO double check the webview for errors
				setResult(RESULT_FAILED);
				finish();
				
			} else {
				unpackAccessTokenAndFinish(accessToken);
			}
		}


	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	private LoaderManager.LoaderCallbacks<RequestToken> mTwitterRequestTokenCallbacks =
			new LoaderManager.LoaderCallbacks<RequestToken>() {
				
				@Override
				public void onLoaderReset(Loader<RequestToken> arg0) {}
				
				@Override
				public void onLoadFinished(Loader<RequestToken> loader, RequestToken token) {
					final int id = loader.getId();
					
					getSupportLoaderManager().destroyLoader(id); //easy tracking
					
					if (mLoadingDialog != null) {
						mLoadingDialog.dismiss();
					}
					
					if (token != null) {
						mTwitterRequestToken = token;						
						
						loadTwitter(mTwitterRequestToken.getAuthenticationURL());							
						
					} else {
						Log.e(LOGTAG, "Something went wrong with token loading");
						setResult(RESULT_FAILED);
						finish();
					}
				}
				
				
				@Override
				public Loader<RequestToken> onCreateLoader(int id, Bundle args) {
					return new TwitterRequestTokenAsyncLoader(
							TwitterLoginActivity.this, 
							getTwitter());
				}
			};
	
}
