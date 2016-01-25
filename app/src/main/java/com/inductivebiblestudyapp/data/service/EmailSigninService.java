package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.POST;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.EmailSigninResponse;

/** @version 0.2.0-20150710 */
public interface EmailSigninService {
	
	/**
	 * Returns signin response asynchronously.
	 * @param email The email to sign in with
	 * @param password The password to sign in with
	 * @param callback
	 */
	// ApiConstants.PATH + "login?email=email&password=password "
	@POST(ApiConstants.PATH + "login")
	public void sendSigninCredentials(@Query("email") String email, 
			@Query("password") String password, Callback<EmailSigninResponse> callback);
	
	/**
	 * Return the signin response synchronously.
	 * @param email The email to sign in with
	 * @param password The password to sign in with
	 * @return
	 */
	@POST(ApiConstants.PATH + "login")
	public EmailSigninResponse sendSigninCredentials(@Query("email") String email, 
			@Query("password") String password);
	

}
