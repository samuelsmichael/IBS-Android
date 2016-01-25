package com.inductivebiblestudyapp.data.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.ForgotPasswordResponse;
import com.inductivebiblestudyapp.data.service.ForgotPasswordService;

/**
 * Loader to fetch/parse forgot password api using {@link AsyncTaskLoader}. 
 * @author Jason Jenkins
 * @version 0.1.0-20150619
 */
public class ForgotPasswordAsyncLoader extends AbstractFetchAsyncLoader<ForgotPasswordResponse> {

	private final ForgotPasswordService mService;
	
	private final String mEmail;
	
	public ForgotPasswordAsyncLoader(Context context, String email) {
		super(context);
		
		if (email == null) {
			throw new NullPointerException("Cannot have null inputs, email: "+ email+"]");
		}
		
		this.mService = RestClient.getInstance().getForgotPasswordService();
		
		this.mEmail = email.trim();
	}

	@Override
	protected ForgotPasswordResponse fetchResult() {
		ForgotPasswordResponse result = (ForgotPasswordResponse) mService.requestPasswordRecovery(mEmail);
		return result;
	}
	

}
