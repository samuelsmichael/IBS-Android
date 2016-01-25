package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.POST;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.ContactResponse;
import com.inductivebiblestudyapp.data.model.ContentResponse;

/** @version 0.2.0-20150710 */
public interface ContactService {
	
	/**
	 * Returns contact response asynchronously.
	 * @param name The users name
	 * @param email The email address to contact them at
	 * @param phone The phone number to reach them at
	 * @param message The message to send
	 * @param callback
	 */
	// ApiConstants.PATH + "contact?name=name&email=email&phone=phone&message=message"
	@POST(ApiConstants.PATH + "contact")
	public void sendConactMessage(@Query("name") String name, @Query("email") String email, 
			@Query("phone") String phone, @Query("message") String message, Callback<ContentResponse> callback);
	
	/**
	 * Return the contact response synchronously.
	 * @param name The users name
	 * @param email The email address to contact them at
	 * @param phone The phone number to reach them at
	 * @param message The message to send
	 * @return
	 */
	@POST(ApiConstants.PATH + "contact")
	public ContactResponse sendConactMessage(@Query("name") String name,
			@Query("email") String email, @Query("phone") String phone, 
			@Query("message") String message);
	

}
