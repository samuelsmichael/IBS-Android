package com.inductivebiblestudyapp.data.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.SocialSigninResponse;
import com.inductivebiblestudyapp.data.service.SocialSigninService;

/**
 * Loader to fetch/parse email signing api using {@link AsyncTaskLoader}. 
 * @author Jason Jenkins
 * @version 0.2.2-20150827
 */
public class SocialSigninAsyncLoader extends AbstractFetchAsyncLoader<SocialSigninResponse> {
	@SuppressWarnings("unused")
	final static private String LOGTAG = SocialSigninAsyncLoader.class
			.getSimpleName();
	
	private final SocialSigninService mService;
	
	private final String mUserId;
	private final String mType;
	private final String mAccessToken;
	
	private String mImageUrl = null;	
	
	public SocialSigninAsyncLoader(Context context, String type, String userId, String serverAccessToken, String imageUrl) {
		this(context, type, userId, serverAccessToken);
		this.mImageUrl = imageUrl;
	}
	
	public SocialSigninAsyncLoader(Context context, String type, String userId, String serverAccessToken) {
		super(context);
		
		if (userId == null || type == null) {
			throw new NullPointerException("Cannot have null inputs: " + 
					"[userId :" + userId + ", type: "+ type+"]");
		}
		
		this.mService = RestClient.getInstance().getSocialSigninService();
		
		this.mType = type;
		this.mUserId = userId;
		this.mAccessToken = serverAccessToken;
	}

	@Override
	protected SocialSigninResponse fetchResult() {
		SocialSigninResponse result = null;
		
		if (TextUtils.isEmpty(mImageUrl)) {
			mImageUrl = null;
		}
		
		result =  mService.sendSigninCredentials(mUserId, mType, mAccessToken, mImageUrl);
		
		if (result == null) { //we did not fail, but there was no message
			return new SocialSigninResponse();
		}
		return result;
	}
	

}
