package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.EmailConfirmResponse;

/**
 * 
 * @author Jason Jenkins
 * @version 0.2.0-20150805
 */
public interface EmailSendService {
	
	/**
	 * Asynchronous. Sends an email with text & an optional image to list of recipients
	 * @param accessToken The IBS access token for current user.
	 * @param addresses The list of email addresses, separated by commas.
	 * @param message The message to send to the recipients
	 * @param imageId Optional (<code>null</code> if not used), the user uploaded
	 * image to send with the message
	 * @param callback
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "share-email")
	public void send(@Field("access_token") String accessToken, 
			@Field("users") String addresses, @Field("data")String message,
			@Field("image_id") String imageId,
			Callback<EmailConfirmResponse> callback
			);

	
	/**
	 * Synchronous. Sends an email with text & an optional image to list of recipients
	 * @param accessToken The IBS access token for current user.
	 * @param addresses The list of email addresses, separated by commas.
	 * @param message The message to send to the recipients
	 * @param imageId Optional (<code>null</code> if not used), the user uploaded
	 * image to send with the message */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "share-email")
	public EmailConfirmResponse send(@Field("access_token") String accessToken, 
			@Field("users") String addresses, @Field("data")String message, 
			@Field("image_id") String imageId);

	
}
