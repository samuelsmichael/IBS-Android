package com.inductivebiblestudyapp.data.service.googleimages;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.model.googleimages.BasicGoogleImageResponse;

/** @version 0.2.0-20150730 */
public interface BasicGoogleImageSearchService {
	
	
	/**
	 * Asynchronous query.
	 * @param query The image phrase to search for.
	 * @param start The starting element. Max as of writing is 56; use the pages response as a guide.
	 * @param callback
	 */
	// ajax/services/search/images?v=1.0&rsz=8&start=0&q=test
	@GET("/ajax/services/search/images?v=1.0&rsz=" + BasicGoogleImageResponse.RESULT_SIZE)
	public void search(@Query("q") String query, @Query("start") String start, 
			Callback<BasicGoogleImageResponse> callback);
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Synchronous query.
	 * @param query The image phrase to search for.
	 * @param start The starting element. Max as of writing is 56; use the pages response as a guide.
	 * @return The image response
	 */
	// ajax/services/search/images?v=1.0&rsz=8&start=0&q=test
	@GET("/ajax/services/search/images?v=1.0&rsz=" + BasicGoogleImageResponse.RESULT_SIZE)
	public BasicGoogleImageResponse search(@Query("q") String query, @Query("start") String start);
	

}
