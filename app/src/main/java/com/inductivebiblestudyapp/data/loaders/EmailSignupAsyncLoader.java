package com.inductivebiblestudyapp.data.loaders;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.EmailSignupResponse;
import com.inductivebiblestudyapp.data.service.EmailSignupService;

/**
 * Loader to fetch/parse email signup api using {@link AsyncTaskLoader}. 
 * @author Jason Jenkins
 * @version 0.1.0-20150615
 */
public class EmailSignupAsyncLoader extends AbstractFetchAsyncLoader<EmailSignupResponse> {

	private final EmailSignupService mService;
	
	private final String mPassword;
	private final String mEmail;
	
	public EmailSignupAsyncLoader(Context context,String email, String password) {
		super(context);
		
		if (password == null || email == null) {
			throw new NullPointerException("Cannot have null inputs: " + 
					"[password null:" + (password == null) + ", email: "+ email+"]");
		}
		
		this.mService = RestClient.getInstance().getEmailSignupService();
		
		this.mEmail = email.trim();
		this.mPassword = password;
	}

	@Override
	protected EmailSignupResponse fetchResult() {
		EmailSignupResponse result = (EmailSignupResponse) mService.sendSignupCredentials(mEmail, mPassword);
		if (result == null) { //we did not fail, but there was no message
			return new EmailSignupResponse();
		}
		return result;
	}
	

}
