package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.POST;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.ForgotPasswordResponse;

/** @version 0.2.0-20150710 */
public interface ForgotPasswordService {
	
	/**
	 * Asynchronous. Request a password reset for a given email account.
	 * @param email The email address to request password reset.
	 * @param callback
	 */
	// ApiConstants.PATH + "login?email=email&password=password "
	@POST(ApiConstants.PATH + "forgot-password")
	public void requestPasswordRecovery(@Query("email") String email, Callback<ForgotPasswordResponse> callback);
	
	/**
	 * Synchronous. Request a password reset for a given email account.
	 * @param email The email address to request password reset.
	 */
	@POST(ApiConstants.PATH + "forgot-password")
	public ForgotPasswordResponse requestPasswordRecovery(@Query("email") String email);
	

}
