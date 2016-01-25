package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.data.model.LetteringListResponse;
import com.inductivebiblestudyapp.data.model.UpdateResult;

/**
 * Use for font styles 
 * @author Jason Jenkins
 * @version 0.3.0-20150723
 */
public interface LetteringService {
	
	/**
	 * Asynchronous. Creates a new user lettering.
	 * @param accessToken The IBS access token for the current user.
	 * @param name The name of the lettering.
	 * @param bold 1 for true, 0 for false.
	 * @param italics 1 for true, 0 for false.
	 * @param underline 1 for true, 0 for false.
	 * @param strikethrough 1 for true, 0 for false.
	 * @param doubleUnderline 1 for true, 0 for false.
	 * @param boxed 1 for true, 0 for false.
	 * @param doubleBoxed 1 for true, 0 for false.
	 * @param fontColor The hex font color. 0 for "ignore".
	 * @param backgroundColor The hex font color. 0 for "ignore".
	 * @param callback
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "lettering")
	public void create(@Field("access_token") String accessToken, 
			@Field("name") String name, 
			@Field("bold") int bold, 	
			@Field("italics") int italics, 	
			@Field("underline") int underline, 	
			@Field("strikethrough") int strikethrough, 	
			@Field("double_underline") int doubleUnderline, 	
			@Field("boxed") int boxed, 	
			@Field("double_boxed") int doubleBoxed, 	
			@Field("font_color") String fontColor, 	
			@Field("background_color") String backgroundColor,
			Callback<UpdateResult> callback);
	
	/**
	 * Asynchronous. Edits an existing user lettering.
	 * @param accessToken The IBS access token for the current user. 
	 * @param letteringId The id of the existing lettering.
	 * @param name The name of the lettering.
	 * @param bold 1 for true, 0 for false.
	 * @param italics 1 for true, 0 for false.
	 * @param underline 1 for true, 0 for false.
	 * @param strikethrough 1 for true, 0 for false.
	 * @param doubleUnderline 1 for true, 0 for false.
	 * @param boxed 1 for true, 0 for false.
	 * @param doubleBoxed 1 for true, 0 for false.
	 * @param fontColor The hex font color. 0 for "ignore".
	 * @param backgroundColor The hex font color. 0 for "ignore".
	 * @param callback
	 */
	@FormUrlEncoded
	@PUT(ApiConstants.PATH + "lettering/{lettering_id}")
	public void edit(@Field("access_token") String accessToken, 
			@Path("lettering_id") String letteringId,
			@Field("name") String name, 
			@Field("bold") int bold, 	
			@Field("italics") int italics, 	
			@Field("underline") int underline, 	
			@Field("strikethrough") int strikethrough, 	
			@Field("double_underline") int doubleUnderline, 	
			@Field("boxed") int boxed, 	
			@Field("double_boxed") int doubleBoxed, 	
			@Field("font_color") String fontColor, 	
			@Field("background_color") String backgroundColor,
			Callback<UpdateResult> callback);
		
	/**
	 * Asynchronous. Deletes an existing user lettering. Do not confuse with deleting markings.
	 * @param accessToken The IBS access token for the current user. 
	 * @param letteringId The id of the existing lettering.
	 * @param callback
	 */
	@DELETE(ApiConstants.PATH + "lettering/{lettering_id}")
	public void delete(@Query("access_token") String accessToken, 
			@Path("lettering_id") String letteringId,
			Callback<UpdateResult> callback);
	
	
	/**
	 * Asynchronous. Lists all user letterings. 
	 * @param accessToken The IBS access token for the current user. 
	 * @param callback */
	@GET(ApiConstants.PATH + "lettering")
	public void list(@Query("access_token") String accessToken,  
			Callback<LetteringListResponse> callback);
	
	/**
	 * Asynchronous.Returns a single user lettering. 
	 * @param accessToken The IBS access token for the current user. 
	 * @param letteringId The id of the lettering to retrieve
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "lettering/{lettering_id}")
	public void get(@Query("access_token") String accessToken,  
			@Path("lettering_id") String letteringId,
			Callback<LetteringItem> callback);
	/**
	 * Asynchronous. Lists all the library/admin letterings.
	 * @param accessToken The IBS access token for the current user.
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "letteringlibrary")
	public void listLibrary(@Query("access_token") String accessToken,  
			Callback<LetteringListResponse> callback);

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Synchronous. Creates a new user lettering.
	 * @param accessToken The IBS access token for the current user.
	 * @param name The name of the lettering.
	 * @param bold 1 for true, 0 for false.
	 * @param italics 1 for true, 0 for false.
	 * @param underline 1 for true, 0 for false.
	 * @param strikethrough 1 for true, 0 for false.
	 * @param doubleUnderline 1 for true, 0 for false.
	 * @param boxed 1 for true, 0 for false.
	 * @param doubleBoxed 1 for true, 0 for false.
	 * @param fontColor The hex font color. 0 for "ignore".
	 * @param backgroundColor The hex font color. 0 for "ignore".
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "lettering")
	public UpdateResult create(@Field("access_token") String accessToken, 
			@Field("name") String name, 
			@Field("bold") int bold, 	
			@Field("italics") int italics, 	
			@Field("underline") int underline, 	
			@Field("strikethrough") int strikethrough, 	
			@Field("double_underline") int doubleUnderline, 	
			@Field("boxed") int boxed, 	
			@Field("double_boxed") int doubleBoxed, 	
			@Field("font_color") String fontColor, 	
			@Field("background_color") String backgroundColor);
	
	/**
	 * Synchronous. Edits an existing user lettering.
	 * @param accessToken The IBS access token for the current user. 
	 * @param letteringId The id of the existing lettering.
	 * @param name The name of the lettering.
	 * @param bold 1 for true, 0 for false.
	 * @param italics 1 for true, 0 for false.
	 * @param underline 1 for true, 0 for false.
	 * @param strikethrough 1 for true, 0 for false.
	 * @param doubleUnderline 1 for true, 0 for false.
	 * @param boxed 1 for true, 0 for false.
	 * @param doubleBoxed 1 for true, 0 for false.
	 * @param fontColor The hex font color. 0 for "ignore".
	 * @param backgroundColor The hex font color. 0 for "ignore".
	 */
	@FormUrlEncoded
	@PUT(ApiConstants.PATH + "lettering/{lettering_id}")
	public UpdateResult edit(@Field("access_token") String accessToken, 
			@Path("lettering_id") String letteringId,
			@Field("name") String name, 
			@Field("bold") int bold, 	
			@Field("italics") int italics, 	
			@Field("underline") int underline, 	
			@Field("strikethrough") int strikethrough, 	
			@Field("double_underline") int doubleUnderline, 	
			@Field("boxed") int boxed, 	
			@Field("double_boxed") int doubleBoxed, 	
			@Field("font_color") String fontColor, 	
			@Field("background_color") String backgroundColor);
	
	/**
	 * Synchronous. Deletes an existing user lettering. Do not confuse with deleting markings.
	 * @param accessToken The IBS access token for the current user. 
	 * @param letteringId The id of the existing lettering.
	 */
	@DELETE(ApiConstants.PATH + "lettering/{lettering_id}")
	public UpdateResult delete(@Query("access_token") String accessToken, 
			@Path("lettering_id") String letteringId);
	
	/**
	 * Synchronous. Lists all user letterings. 
	 * @param accessToken The IBS access token for the current user.  */
	@GET(ApiConstants.PATH + "lettering")
	public LetteringListResponse list(@Query("access_token") String accessToken);
	
	/**
	 * Synchronous. Returns a single user lettering. 
	 * @param accessToken The IBS access token for the current user. 
	 * @param letteringId The id of the lettering to retrieve */
	@GET(ApiConstants.PATH + "lettering/{lettering_id}")
	public LetteringItem get(@Query("access_token") String accessToken,  
			@Path("lettering_id") String letteringId);
	
	/**
	 * Synchronous. Lists all the library/admin letterings.
	 * @param accessToken The IBS access token for the current user. */
	@GET(ApiConstants.PATH + "letteringlibrary")
	public LetteringListResponse listLibrary(@Query("access_token") String accessToken);
}
