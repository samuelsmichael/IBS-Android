package com.inductivebiblestudyapp.data.loaders;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.EmailSigninResponse;
import com.inductivebiblestudyapp.data.service.EmailSigninService;

/**
 * Loader to fetch/parse email signing api using {@link AsyncTaskLoader}. 
 * @author Jason Jenkins
 * @version 0.2.0-20150615
 */
public class EmailSigninAsyncLoader extends AbstractFetchAsyncLoader<EmailSigninResponse> {

	private final EmailSigninService mService;
	
	private final String mPassword;
	private final String mEmail;
	
	public EmailSigninAsyncLoader(Context context,String email, String password) {
		super(context);
		
		if (password == null || email == null) {
			throw new NullPointerException("Cannot have null inputs: " + 
					"[password null:" + (password == null) + ", email: "+ email+"]");
		}
		
		this.mService = RestClient.getInstance().getEmailSigninService();
		
		this.mEmail = email.trim();
		this.mPassword = password;
	}

	@Override
	protected EmailSigninResponse fetchResult() {
		EmailSigninResponse result = (EmailSigninResponse) mService.sendSigninCredentials(mEmail, mPassword);
		if (result == null) { //we did not fail, but there was no message
			return new EmailSigninResponse();
		}
		return result;
	}
	

}
