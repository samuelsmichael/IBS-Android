package com.inductivebiblestudyapp.auth;

import java.io.IOException;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.loaders.AbstractFetchAsyncLoader;
import com.inductivebiblestudyapp.data.model.SocialSigninResponse;
import com.inductivebiblestudyapp.data.service.SocialSigninService;

/**
 * Loader to complete Google signin. Requires the google to be signed in.
 * @author Jason Jenkins
 * @version 0.3.1-20150624
 */
class GoogleSigninAsyncLoader extends AbstractFetchAsyncLoader<SocialSigninResponse> {
	final static private String LOGTAG = GoogleSigninAsyncLoader.class
			.getSimpleName();

	private final SocialSigninService mService;
		
	private final String mAccountName;
	
	private String mImageUrl = null;
	
	
	public GoogleSigninAsyncLoader(Context context, String accountName, String pictureUrl) {
		this(context, accountName);
		this.mImageUrl = pictureUrl;
	}
	
	public GoogleSigninAsyncLoader(Context context, String accountName) {
		super(context);
		this.mService = RestClient.getInstance().getSocialSigninService();
		this.mAccountName = accountName;
	}

	@Override
	protected SocialSigninResponse fetchResult() {
		SocialSigninResponse result = null; 
		
	    try {
	    	
	    	Account account = new Account(mAccountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
	        String scopes =  "oauth2:profile email";

	        
	    	//FIX ME see: https://developers.google.com/identity/sign-in/android/backend-auth
	        
			String accountId = GoogleAuthUtil.getAccountId(getContext(), mAccountName);
			Log.d(LOGTAG, "Account id: " + accountId); 

			String serverToken = GoogleAuthUtil.getToken(getContext(), account, scopes);
			
			String type = getContext().getString(R.string.ibs_config_value_signin_google);
			
			result =  mService.sendSigninCredentials(accountId, type, serverToken, mImageUrl);
			
		} catch (GoogleAuthException e) {
			//e.printStackTrace();
			Log.e(LOGTAG, "Failed to authorize: ", e);
			//TODO error reporting
		} catch (IOException e) {
			//e.printStackTrace();
			Log.e(LOGTAG, "Failed to connect: ", e);
		}
		
		if (result == null) { //we did not fail, but there was no message
			return new SocialSigninResponse();
		}
		return result;
	}
	

}
