package com.inductivebiblestudyapp.data.service.googleimages;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.model.googleimages.CustomSearchGoogleImageResponse;

/** @version 0.1.1-20150730 */
public interface CustomSearchGoogleImageSearchService {
	//Reference:
	// https://developers.google.com/custom-search/json-api/v1/reference/cse/list?hl=en
	
	public static final String SEARCH_CLIPART_PATH = 
			"/customsearch/v1?key=" + CustomSearchGoogleImageResponse.API_KEY + 
			"&cx=" + CustomSearchGoogleImageResponse.SEARCH_ENGINE_ID  + 
			"&num=" + CustomSearchGoogleImageResponse.RESULT_SIZE + 
			"&safe=high&imgType=clipart&searchType=image";
	/**
	 * Asynchronous. Searches for ONLY clipart images
	 * @param query The image phrase to search for.
	 * @param start The starting element. Max as of writing is 56; use the pages response as a guide.
	 * @param callback
	 */
	// customsearch/v1?key=API_KEY&cx=SEARCH_ENGINE_ID&safe=high&type=clipart&searchType=image&q=test&start=11
	@Headers(""+CustomSearchGoogleImageResponse.API_HEADER)
	@GET(SEARCH_CLIPART_PATH)
	public void searchClipArt(@Query("q") String query, @Query("start") String start, 
			Callback<CustomSearchGoogleImageResponse> callback);
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Synchronous. Searches for ONLY clipart images
	 * @param query The image phrase to search for.
	 * @param start The starting element. Max as of writing is 56; use the pages response as a guide.
	 * @return The image response
	 */
	// customsearch/v1?key=API_KEY&cx=SEARCH_ENGINE_ID&safe=high&type=clipart&searchType=image&q=test&start=11
	@Headers(""+CustomSearchGoogleImageResponse.API_HEADER)
	@GET(SEARCH_CLIPART_PATH)
	public CustomSearchGoogleImageResponse searchClipArt(@Query("q") String query, @Query("start") String start);
	

}
