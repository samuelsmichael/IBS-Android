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
public interface ChapterThemeEditService {
	
	/**
	 * Asynchronous. Creates a new theme on the given chapter.
	 * @param accessToken The IBS access token for current user.
	 * @param chapterId The chapterId id to attach the new theme to.
	 * @param name The initial name of the theme.
	 * @param callback
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "theme")
	public void create(@Field("access_token") String accessToken, 
			@Field("chapter_id") String chapterId, @Field("name")String name,
			Callback<UpdateResult> callback
			);
	
	/**
	 * Asynchronous. Edits a chapter theme.
	 * @param accessToken The IBS access token for current user.
	 * @param themeId The id of the theme to edit.
	 * @param name The text to set the theme text to.
	 * @param callback
	 */
	@FormUrlEncoded
	@PUT(ApiConstants.PATH + "theme/{theme_id}")
	public void edit(@Field("access_token") String accessToken, 
			@Path("theme_id") String themeId, @Field("name")String name,
			Callback<UpdateResult> callback
			);
	
	/**
	 * Asynchronous. Deletes an existing theme.
	 * @param accessToken The IBS access token for the current user.
	 * @param themeId The id of the theme to delete.
	 * @param callback
	 */
	@DELETE(ApiConstants.PATH + "theme/{theme_id}")
	public void delete(@Query("access_token") String accessToken, 
			@Path("theme_id") String themeId, Callback<UpdateResult> callback
			);
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Synchronous. Creates a new theme on the given chapter.
	 * @param accessToken The IBS access token for current user.
	 * @param chapterId The chapterId id to attach the new theme to.
	 * @param name The initial name of the theme.
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "theme")
	public UpdateResult create(@Field("access_token") String accessToken, 
			@Field("chapter_id") String chapterId, @Field("name")String name
			);
	
	/**
	 * Synchronous. Edits a chapter theme.
	 * @param accessToken The IBS access token for current user.
	 * @param themeId The id of the theme to edit.
	 * @param name The text to set the theme text to.
	 */
	@FormUrlEncoded
	@PUT(ApiConstants.PATH + "theme/{theme_id}")
	public UpdateResult edit(@Field("access_token") String accessToken, 
			@Path("theme_id") String themeId, @Field("name")String name
			);
	
	/**
	 * Synchronous. Deletes an existing theme.
	 * @param accessToken The IBS access token for the current user.
	 * @param themeId The id of the theme to delete.
	 */
	@DELETE(ApiConstants.PATH + "theme/{theme_id}")
	public UpdateResult delete(@Query("access_token") String accessToken, 
			@Path("theme_id") String themeId);	
	
}
