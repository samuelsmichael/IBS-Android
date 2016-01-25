package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.UpdateResult;

/**
 * 
 * @author Jason Jenkins
 * @version 0.3.2-20150710
 */
public interface NoteEditService {
	
	
	/**
	 * Asynchronous. Creates a new note on the given verse.
	 * @param accessToken The IBS access token for current user.
	 * @param verseId The verse id to attach the new note to.
	 * @param text The initial text body of the given note.
	 * @param callback
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "note")
	public void create(@Field("access_token") String accessToken, 
			@Field("verse_id") String verseId, @Field("text")String text,
			Callback<UpdateResult> callback
			);
	
	/**
	 * Asynchronous. Edits an existing note.
	 * @param accessToken The IBS access token for current user.
	 * @param noteId The id of the note to edit.
	 * @param text The text to set the note body to.
	 * @param callback
	 */
	@FormUrlEncoded
	@PUT(ApiConstants.PATH + "note/{note_id}")
	public void edit(@Field("access_token") String accessToken, 
			@Path("note_id") String noteId, @Field("text")String text,
			Callback<UpdateResult> callback
			);
	
	/**
	 * Asynchronous. Deletes an existing note.
	 * @param accessToken The IBS access token for the current user.
	 * @param noteId The id of the note to delete.
	 * @param callback
	 */
	@DELETE(ApiConstants.PATH + "note/{note_id}")
	public void delete(@Query("access_token") String accessToken, 
			@Path("note_id") String noteId, 
			Callback<UpdateResult> callback
			);
	
	/* NOTE: There is a "GET" method but it has not been implemented. */
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// 
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Synchronous. Creates a new note on the given verse.
	 * @param accessToken The IBS access token for current user.
	 * @param verseId The verse id to attach the new note to.
	 * @param text The initial text body of the given note.
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "note")
	public UpdateResult create(@Field("access_token") String accessToken, 
			@Field("verse_id") String verseId, @Field("text")String text
			);
	
	/**
	 * Synchronous. Edits an existing note.
	 * @param accessToken The IBS access token for current user.
	 * @param noteId The id of the note to edit.
	 * @param text The text to set the note body to.
	 */
	@FormUrlEncoded
	@PUT(ApiConstants.PATH + "note/{note_id}")
	public UpdateResult edit(@Field("access_token") String accessToken, 
			@Path("note_id") String noteId, @Field("text")String text
			);
	
	/**
	 * Synchronous. Deletes an existing note.
	 * @param accessToken The IBS access token for the current user.
	 * @param noteId The id of the note to delete.
	 */
	@DELETE(ApiConstants.PATH + "note/{note_id}")
	public UpdateResult delete(@Query("access_token") String accessToken, 
			@Field("note_id") String noteId);
}
