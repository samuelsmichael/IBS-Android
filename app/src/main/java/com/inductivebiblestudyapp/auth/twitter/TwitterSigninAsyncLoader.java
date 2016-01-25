package com.inductivebiblestudyapp.auth.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.util.Log;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.loaders.AbstractFetchAsyncLoader;
import com.inductivebiblestudyapp.data.model.SocialSigninResponse;
import com.inductivebiblestudyapp.data.service.SocialSigninService;

/**
 * Loader to complete Twitter signin. Requires the google to be signed in.
 * @author Jason Jenkins
 * @version 0.2.0-20150623
 */
public class TwitterSigninAsyncLoader extends AbstractFetchAsyncLoader<SocialSigninResponse> {
	final static private String LOGTAG = TwitterSigninAsyncLoader.class
			.getSimpleName();

	private final SocialSigninService mService;
	
	private final String mUserId;
	
	private final String mServerToken;
	
	private final Twitter mTwitter;
	
	
	public TwitterSigninAsyncLoader(Context context, Twitter twitter, String serverToken, String userId) {
		super(context);
		this.mService = RestClient.getInstance().getSocialSigninService();
		this.mUserId = userId;
		this.mServerToken = serverToken;
		this.mTwitter = twitter;
	}

	@Override
	protected SocialSigninResponse fetchResult() {
		SocialSigninResponse result = null; 
		
	    try {
	    	User twitterUser = mTwitter.showUser(Long.parseLong(mUserId));
			
			CurrentUser user = new CurrentUser(getContext());
			
			final String fullname = twitterUser.getName().trim();
			String first = fullname;
			String last = "";
			
			final int spaceIndex = fullname.indexOf(" ");
			if (spaceIndex > 0) {
				first = fullname.substring(0, spaceIndex);
				last = fullname.substring(spaceIndex);
			}
			
			user.setFirstName(first);
			user.setLastName(last);
			user.setCity(twitterUser.getLocation());
			

			String imageUrl = twitterUser.getProfileImageURL(); 

			
			Log.d(LOGTAG, "Twitter fullname: " + fullname);
			
			result = mService.sendSigninCredentials(mUserId, getContext().getString(R.string.ibs_config_value_signin_twitter), mServerToken, imageUrl);
			
		} catch (TwitterException e) {
			e.printStackTrace();
		} 
		
		if (result == null) { //we did not fail, but there was no message
			return new SocialSigninResponse();
		}
		return result;
	}

	
	

}
