package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.StudyNotesResponse;

/** @version 0.1.0-20150722 */
public interface StudyNotesService {
	

	/**
	 * Asynchronous. Produces a list of study notes, including:
	 * notes, division themes, chapter themes, lettering markings, image markings
	 * @param accessToken The IBS access token for the current user
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "note")
	public void list(@Query("access_token") String accessToken, Callback<StudyNotesResponse> callback);
	
	/**
	 * Synchronous. Produces a list of study notes, including:
	 * notes, division themes, chapter themes, lettering markings, image markings
	 * @param accessToken The IBS access token for the current user
	 */
	@GET(ApiConstants.PATH + "note")
	public StudyNotesResponse list(@Query("access_token") String accessToken);
	

}
