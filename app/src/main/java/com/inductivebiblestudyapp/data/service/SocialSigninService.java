package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.SocialSigninResponse;

/**
 * 
 * @author Jason Jenkins
 * @version 0.3.2-20150710
 */
public interface SocialSigninService {
	

	/** Returns signin response asynchronously.
	 * 
	 * @param userId The social user id
	 * @param type The type of sign in. Expects: facebook, google, linkedin, twitter
	 * @param serverAccessToken The server access token for the social sign in
	 * @param profileImageUrl Optional (should be <code>null</code> if skipped).
	 * @param callback
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "social-login")
	public void sendSigninCredentials(@Field("social_user_id") String userId,
			 @Field("type")String type,  @Field("access_token")String serverAccessToken,
			@Field("profile_image") String profileImage,
			Callback<SocialSigninResponse> callback);
	
	
	/**
	 * Return the sign-in response synchronously.
	 * @param userId The social user id
	 * @param type The type of sign in. Expects: facebook, google, linkedin, twitter
	 * @param serverAccessToken The server access token for the social sign in
	 * @param profileImageUrl Optional (should be <code>null</code> if skipped).
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "social-login")
	public SocialSigninResponse sendSigninCredentials(@Field("social_user_id") String userId,
			@Field("type")String type, @Field("access_token")String accessToken, 
			@Field("profile_image") String profileImageUrl);
	

}
