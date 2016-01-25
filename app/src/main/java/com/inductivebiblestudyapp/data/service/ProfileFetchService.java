package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.ProfileResponse;

/**
 * 
 * @author Jason Jenkins
 * @version 0.1.1-20150710
 */
public interface ProfileFetchService {
	
	/**
	 * Asynchronous. Returns the current user profile.
	 * @param accessToken The IBS access token for the current user. 
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "profile")
	public void getProfile(@Query("access_token") String accessToken, Callback<ProfileResponse> callback
			);
	
	/**
	 * Synchronous. Returns the current user profile.
	 * @param accessToken The IBS access token for the current user. */
	@GET(ApiConstants.PATH + "profile")
	public ProfileResponse getProfile(@Query("access_token") String accessToken);
	

}
