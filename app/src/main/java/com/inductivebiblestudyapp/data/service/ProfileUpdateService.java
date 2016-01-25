package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.UpdateResult;

/**
 * 
 * @author Jason Jenkins
 * @version 0.5.0-20150910
 */
public interface ProfileUpdateService {
	

	/**
	 * Asynchronous. Updates the current user profile.
	 * @param accessToken The IBS access token for the current user.
	 * @param firstName The user's first name.
	 * @param lastName The user's surname.
	 * @param email Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param address Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param city Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param state Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param zip Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param bibleVersion
	 * @param bio Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param base64Image Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param password Optional (should be <code>null</code> if skipped, blank to set blank). 
	 * The password to update.
	 * @param callback
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "profile")
	public void sendProfileUpdate(@Field("access_token") String accessToken, 
			@Field("first_name") String firstName, @Field("last_name") String lastName,
			@Field("email") String email,
			@Field("address") String address, @Field("city") String city, 
			@Field("state") String state, @Field("zip") String zip, 
			@Field("bible_version") String bibleVersion, 
			@Field("bio") String bio, @Field("profile_image") String base64Image,
			@Field("password") String password,
			Callback<UpdateResult> callback
			);
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// 
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Synchronous. Updates the current user profile.
	 * @param accessToken The IBS access token for the current user.
	 * @param firstName The user's first name.
	 * @param lastName The user's surname.
	 * @param email Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param address Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param city Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param state Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param zip Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param bibleVersion
	 * @param bio Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param base64Image Optional (should be <code>null</code> if skipped, blank to set blank).
	 * @param password Optional (should be <code>null</code> if skipped, blank to set blank). 
	 * The password to update.
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "profile")
	public UpdateResult sendProfileUpdate(@Field("access_token") String accessToken, 
			@Field("first_name") String firstName, @Field("last_name") String lastName,
			@Field("email") String email,
			@Field("address") String address, @Field("city") String city, 
			@Field("state") String state, @Field("zip") String zip, 
			@Field("bible_version") String bibleVersion,
			@Field("bio") String bio, @Field("profile_image") String base64Image,
			@Field("password") String password
			);
	

}
