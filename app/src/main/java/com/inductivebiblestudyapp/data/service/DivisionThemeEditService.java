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
 * @version 0.2.2-20150710
 */
public interface DivisionThemeEditService {
	
	/**
	 * Asynchronous. Creates a new division theme on the given verse.
	 * @param accessToken The IBS access token for current user.
	 * @param verseId The verse id to attach the new division theme to.
	 * @param name The initial name of the division theme.
	 * @param callback
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "divisiontheme")
	public void create(@Field("access_token") String accessToken, 
			@Field("verse_id") String verseId, @Field("name")String name,
			Callback<UpdateResult> callback
			);
	
	/**
	 * Asynchronous. Edits an division theme.
	 * @param accessToken The IBS access token for current user.
	 * @param divThemeId The id of the division theme to edit.
	 * @param name The text to set the division theme text to.
	 * @param callback
	 */
	@FormUrlEncoded
	@PUT(ApiConstants.PATH + "divisiontheme/{division_theme_id}")
	public void edit(@Field("access_token") String accessToken, 
			@Path("division_theme_id") String divThemeId, @Field("name")String name,
			Callback<UpdateResult> callback
			);
	
	/**
	 * Asynchronous. Deletes an existing division theme.
	 * @param accessToken The IBS access token for the current user.
	 * @param divThemeId The id of the division theme to delete.
	 * @param callback
	 */
	@DELETE(ApiConstants.PATH + "divisiontheme/{division_theme_id}")
	public void delete(@Query("access_token") String accessToken, 
			@Path("division_theme_id") String divThemeId,
			Callback<UpdateResult> callback
			);

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// 
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Synchronous. Creates a new division theme on the given verse.
	 * @param accessToken The IBS access token for current user.
	 * @param verseId The verse id to attach the new division theme to.
	 * @param name The initial name of the division theme.
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "divisiontheme")
	public UpdateResult create(@Field("access_token") String accessToken, 
			@Field("verse_id") String verseId, @Field("name")String name
			);

	/**
	 * Synchronous. Edits an division theme.
	 * @param accessToken The IBS access token for current user.
	 * @param divThemeId The id of the division theme to edit.
	 * @param name The text to set the division theme text to.
	 */
	@FormUrlEncoded
	@PUT(ApiConstants.PATH + "divisiontheme/{division_theme_id}")
	public UpdateResult edit(@Field("access_token") String accessToken, 
			@Path("division_theme_id") String divThemeId, @Field("name")String name
			);
	
	/**
	 * Synchronous. Deletes an existing division theme.
	 * @param accessToken The IBS access token for the current user.
	 * @param divThemeId The id of the division theme to delete.
	 */
	@DELETE(ApiConstants.PATH + "divisiontheme/{division_theme_id}")
	public UpdateResult delete(@Query("access_token") String accessToken, 
			@Path("division_theme_id") String divThemeId);
}
