package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.POST;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.ContentResponse;

/** @version 0.1.0-20150710 */
public interface SimpleContentService {
	
	/**
	 * Returns content response asynchronously.
	 * @param name The page content to return  
	 * @param callback
	 */
	// ApiConstants.PATH + "content?name=name"
	@POST(ApiConstants.PATH + "content")
	public void getContent(@Query("name") String name, Callback<ContentResponse> callback);
	
	/**
	 * Return the content response synchronously.
	 * @param name The page content to return.
	 * @return
	 */
	@POST(ApiConstants.PATH + "content")
	public ContentResponse getContent(@Query("name") String name);
	

}
