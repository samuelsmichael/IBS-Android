package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseDetailsResponse;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.CrossReferenceResponse;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.StrongsResponse;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.StrongsSearchResponse;

/**
 * 
 * @author Jason Jenkins
 * @version 0.4.1-20150824
 */
public interface WordStudyService {
	
	/**
	 * Asynchronous. Searches Strong's for similar words (not used.)
	 * @param accessToken The IBS access token for the current user.
	 * @param query The search word to check with.
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "searchStrongs")
	public void searchStrongs(@Query("access_token") String accessToken,
			@Query("q") String query,
			Callback<StrongsSearchResponse> callback);
	
	/**
	 * Asynchronous. Returns the Strong's definition and cross reference 
	 * word list for a given work. 
	 * @param accessToken The IBS access token for current user
	 * @param strongsNumber The Strong's number to look up (half the primary key).
	 * Found in {@link BibleVerseDetailsResponse}.
	 * @param language The language (the second half); either G or H
	 * @param strongsId <b>Not used</b>
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "strongs")
	public void getStrongs(
			@Query("access_token") String accessToken,
			@Query("strongs_number") String strongsNumber,
			@Query("language") String language, 
			@Query("strongs_concordance_id") String strongsId,
			Callback<StrongsResponse> callback);
	
	/**
	 * Asynchronous. Returns the Cross reference locations for a given id/word.
	 * @param accessToken The IBS access token for current user
	 * @param kjvTranslationId The translation id of the word to look up. 
	 * Found in {@link StrongsResponse}
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "crossreferences")
	public void getCrossReferences(
			@Query("access_token") String accessToken,
			@Query("kjv_translation_id") String kjvTranslationId,
			Callback<CrossReferenceResponse> callback);
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Synchronous. Searches Strong's for similar words (not used.)
	 * @param accessToken The IBS access token for the current user.
	 * @param query The search word to check with.
	 */
	@GET(ApiConstants.PATH + "searchStrongs")
	public StrongsSearchResponse searchStrongs(@Query("access_token") String accessToken,
			@Query("q") String query);
	
	/**
	 * Synchronous. Returns the Strong's definition and cross reference 
	 * word list for a given work. 
	 * @param accessToken The IBS access token for current user
	 * @param strongsNumber The Strong's number to look up (half the primary key).
	 * Found in {@link BibleVerseDetailsResponse}.
	 * @param language The language (the second half); either G or H
	 * @param strongsId <b>Not used</b>
	 */
	@GET(ApiConstants.PATH + "strongs")
	public StrongsResponse getStrongs(
			@Query("access_token") String accessToken,
			@Query("strongs_number") String strongsNumber,
			@Query("language") String language, 
			@Query("strongs_concordance_id") String strongsId);
	
	/**
	 * Synchronous. Returns the Cross reference locations for a given id/word.
	 * @param accessToken The IBS access token for current user
	 * @param kjvTranslationId The translation id of the word to look up. 
	 * Found in {@link StrongsResponse}
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "crossreferences")
	public CrossReferenceResponse getCrossReferences(
			@Query("access_token") String accessToken,
			@Query("kjv_translation_id") String kjvTranslationId);

}
