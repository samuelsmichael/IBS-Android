package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.UpdateResult;

/**
 * 
 * @author Jason Jenkins
 * @version 0.5.0-20150727
 */
public interface MarkingService {
	
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_LETTERING = "lettering";
	
	/**
	 * Asynchronous. Creates a marking (image, lettering or otherwise).
	 * @param accessToken The IBS access token for the current user.
	 * @param type The type of marking supported types are image and marking.
	 * @param typeId The id of the given type, as given by their object (image id & lettering id).
	 * @param startIndex The starting index of the marking, relative to the chapter start.
	 * @param endIndex The ending index of the marking, relative to the chapter start.
	 * @param chapterId The id of the chapter to apply the marking to. 
	 * @param verseRange The visual verse range, used for output.
	 * @param callback
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "markings")
	public void create(@Field("access_token") String accessToken, 
			@Field("type") String type,  @Field("object_id") String typeId, 
			@Field("start") int startIndex, @Field("end") int endIndex,
			@Field("chapter_id")String chapterId, @Field("verse_range")String verseRange,
			Callback<UpdateResult> callback
			);
	
	/**
	 * Asynchronous. Creates an image marking.
	 * @param accessToken The IBS access token for the current user.
	 * @param typeId The id of the image.
	 * @param startIndex The starting index of the marking, relative to the chapter start.
	 * @param endIndex The ending index of the marking, relative to the chapter start.
	 * @param chapterId The id of the chapter to apply the marking to. 
	 * @param verseRange The visual verse range, used for output.
	 * @param callback
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "markings?type=" + TYPE_IMAGE)
	public void createImageMarking(@Field("access_token") String accessToken, 
			@Field("object_id") String typeId, 
			@Field("start") int startIndex, @Field("end") int endIndex,
			@Field("chapter_id")String chapterId, @Field("verse_range")String verseRange,
			Callback<UpdateResult> callback
			);
	
	/**
	 * Asynchronous. Creates a lettering marking.
	 * @param accessToken The IBS access token for the current user.
	 * @param typeId The id of the lettering.
	 * @param startIndex The starting index of the marking, relative to the chapter start.
	 * @param endIndex The ending index of the marking, relative to the chapter start.
	 * @param chapterId The id of the chapter to apply the marking to. 
	 * @param verseRange The visual verse range, used for output.
	 * @param callback
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "markings?type=" + TYPE_LETTERING)
	public void createLetteringMarking(@Field("access_token") String accessToken, 
			@Field("object_id") String typeId, 
			@Field("start") int startIndex, @Field("end") int endIndex,
			@Field("chapter_id")String chapterId, @Field("verse_range")String verseRange,
			Callback<UpdateResult> callback
			);	
	
	/**
	 * Asynchronous. Deletes a marking (image, lettering or otherwise) applied to a selection.
	 * @param accessToken The IBS access token for the current user
	 * @param markingId The id of the marking. Do not confuse with the image or lettering id.
	 * @param callback
	 */
	@DELETE(ApiConstants.PATH + "markings/{object_id}")
	public void delete(@Query("access_token") String accessToken, 
			@Path("object_id") String markingId, 
			Callback<UpdateResult> callback);
	
	/* NOTE: there is a get method but it used & unimplemeneted */
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// 
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Synchronous. Creates a marking (image, lettering or otherwise).
	 * @param accessToken The IBS access token for the current user.
	 * @param type The type of marking supported types are image and marking.
	 * @param typeId The id of the given type, as given by their object (image id & lettering id).
	 * @param startIndex The starting index of the marking, relative to the chapter start.
	 * @param endIndex The ending index of the marking, relative to the chapter start.
	 * @param chapterId The id of the chapter to apply the marking to. 
	 * @param verseRange The visual verse range, used for output */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "markings")
	public UpdateResult create(@Field("access_token") String accessToken, 
			@Field("type") String type,  @Field("object_id") String typeId, 
			@Field("start") int startIndex, @Field("end") int endIndex,
			 @Field("chapter_id")String chapterId, @Field("verse_range")String verseRange
			);
	
	/**
	 * Synchronous. Creates an image marking.
	 * @param accessToken The IBS access token for the current user.
	 * @param typeId The id of the image.
	 * @param startIndex The starting index of the marking, relative to the chapter start.
	 * @param endIndex The ending index of the marking, relative to the chapter start.
	 * @param chapterId The id of the chapter to apply the marking to. 
	 * @param verseRange The visual verse range, used for output.
	 */ 
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "markings?type=" + TYPE_IMAGE)
	public UpdateResult createImageMarking(@Field("access_token") String accessToken, 
			@Field("object_id") String typeId, 
			@Field("start") int startIndex, @Field("end") int endIndex,
			@Field("chapter_id")String chapterId, @Field("verse_range")String verseRange
			);
	
	/**
	 * Synchronous. Creates a lettering marking.
	 * @param accessToken The IBS access token for the current user.
	 * @param typeId The id of the lettering.
	 * @param startIndex The starting index of the marking, relative to the chapter start.
	 * @param endIndex The ending index of the marking, relative to the chapter start.
	 * @param chapterId The id of the chapter to apply the marking to. 
	 * @param verseRange The visual verse range, used for output.
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "markings?type=" + TYPE_LETTERING)
	public UpdateResult createLetteringMarking(@Field("access_token") String accessToken, 
			@Field("object_id") String typeId, 
			@Field("start") int startIndex, @Field("end") int endIndex,
			@Field("chapter_id")String chapterId, @Field("verse_range")String verseRange
			);	

	/**
	 * Synchronous. Deletes a marking (image, lettering or otherwise) applied to a selection.
	 * @param accessToken The IBS access token for the current user
	 * @param markingId The id of the marking. Do not confuse with the image or lettering id. */
	@DELETE(ApiConstants.PATH + "markings/{object_id}")
	public UpdateResult delete(@Query("access_token") String accessToken, 
			@Path("object_id") String markingId);
	
	
}
