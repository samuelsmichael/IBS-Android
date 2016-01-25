package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.POST;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.EmailSignupResponse;

/** @version 0.2.0-20150710 */
public interface EmailSignupService {
	
	/**
	 * Returns signup response asynchronously. 
	 * @param email The email to sign up with
	 * @param password The password to sign up with
	 * @param callback
	 */
	// ApiConstants.PATH + "signup?email=email&password=password "
	@POST(ApiConstants.PATH + "signup")
	public void sendSignupCredentials(@Query("email") String email, 
			@Query("password") String password, Callback<EmailSignupResponse> callback);
	
	/**
	 * Return the signup response synchronously. 
	 * @param email The email to sign up with
	 * @param password The password to sign up with
	 * @return
	 */
	@POST(ApiConstants.PATH + "signup")
	public EmailSignupResponse sendSignupCredentials(@Query("email") String email, 
			@Query("password") String password);
	

}
