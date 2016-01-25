package com.inductivebiblestudyapp.data.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.ProfileResponse;
import com.inductivebiblestudyapp.data.service.ProfileFetchService;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Loader to update profile api using {@link AsyncTaskLoader}. 
 * @author Jason Jenkins
 * @version 0.1.3-20150826
 */
public class ProfileFetchAsyncLoader extends AbstractFetchAsyncLoader<ProfileResponse> {
	/** Class name for debugging purposes. */
	@SuppressWarnings("unused")
	final static private String CLASS_NAME = ProfileFetchAsyncLoader.class
			.getSimpleName();
	
	private final ProfileFetchService mService;
	private final String mAccessToken;
	
	public ProfileFetchAsyncLoader(Context context) {
		super(context);	
		CurrentUser user = new CurrentUser(context);
		mAccessToken = user.getIBSAccessToken();
		
		this.mService = RestClient.getInstance().getProfileFetchService();
	}

	@Override
	protected ProfileResponse fetchResult() {
		ProfileResponse result = mService.getProfile(mAccessToken);
		if (result != null && result.isSuccessful()) {
			//precache the image
			ImageLoader.getInstance().loadImageSync(result.getUserData().getProfileImage());	
		}
		return result;
	}	
}
