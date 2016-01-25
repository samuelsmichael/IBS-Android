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
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.data.model.ImageListResponse;
import com.inductivebiblestudyapp.data.model.UpdateResult;

/**
 * 
 * @author Jason Jenkins
 * @version 0.5.0-20150723
 */
public interface ImageService {
	
	/*
	 * Why are we using base64 images? Because the web dev says so.
	 * Aren't those very inefficient and problematic on Android. 
	 * 
	 * Yes. Yes they are. 
	 */
	
	/**
	 * Asynchronous. Creates a new user image.
	 * @param accessToken The IBS access token for the current user.
	 * @param name The name of the image.
	 * @param base64Image The image in base 64 
	 * @param extension the file extension .jpg .png
	 * @param callback
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "image?user_type=2")
	public void create(@Field("access_token") String accessToken, 
			@Field("name") String name, @Field("data") String base64Image, 
			@Field("extension")String extension, Callback<UpdateResult> callback);
	
	/**
	 * Asynchronous. Edits existing image
	 * @param accessToken The IBS access token for the current user.
	 * @param imageId The id of the image to edit.
	 * @param name The name of the image.
	 * @param base64Image The image in base 64 
	 * @param extension the file extension .jpg .png
	 * @param callback
	 */
	@FormUrlEncoded
	@PUT(ApiConstants.PATH + "image/{image_id}?user_type=2")
	public void edit(@Field("access_token") String accessToken, 
			@Path("image_id") String imageId,
			@Field("name") String name, @Field("data") String base64Image, 
			@Field("extension")String extension, Callback<UpdateResult> callback);
		
	/**
	 * Asynchronous. Deletes existing image. Do not confuse with removing a marking.
	 * @param accessToken The IBS access token for the current user.
	 * @param imageId The id of the image to remove.
	 * @param callback
	 */
	@DELETE(ApiConstants.PATH + "image/{image_id}?user_type=2")
	public void delete(@Query("access_token") String accessToken, 
			@Path("image_id") String imageId, 
			Callback<UpdateResult> callback);
	
	/**
	 * Asynchronous. Lists user created images.
	 * @param accessToken The IBS access token for the current user.
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "image")
	public void list(@Query("access_token") String accessToken,  
			Callback<ImageListResponse> callback);
	/**
	 * Asynchronous. Returns a single user created image.
	 * @param accessToken The IBS access token for the current user.
	 * @param imageId The id of the image to return.
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "image/{image_id}")
	public void get(@Query("access_token") String accessToken,  
			@Path("image_id") String imageId, 
			Callback<ImageItem> callback);
	
	/**
	 * Asynchronous. Returns the list of library/admin images.
	 * @param accessToken The IBS access token for the current user.
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "imagelibrary")
	public void listLibrary(@Query("access_token") String accessToken,  
			Callback<ImageListResponse> callback);

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Synchronous. Creates a new user image.
	 * @param accessToken The IBS access token for the current user.
	 * @param name The name of the image.
	 * @param base64Image The image in base 64 
	 * @param extension the file extension .jpg .png
	 */
	@FormUrlEncoded
	@POST(ApiConstants.PATH + "image?user_type=2")
	public UpdateResult create(@Field("access_token") String accessToken, 
			@Field("name") String name, @Field("data") String base64Image, 
			@Field("extension")String extension);
	
	/**
	 * Synchronous. Edits existing image
	 * @param accessToken The IBS access token for the current user.
	 * @param imageId The id of the image to edit.
	 * @param name The name of the image.
	 * @param base64Image The image in base 64 
	 * @param extension the file extension .jpg .png
	 */
	@FormUrlEncoded
	@PUT(ApiConstants.PATH + "image/{image_id}?user_type=2")
	public UpdateResult edit(@Field("access_token") String accessToken, 
			@Path("image_id") String imageId,
			@Field("name") String name, @Field("data") String base64Image, 
			@Field("extension")String extension);
		
	/**
	 * Synchronous. Deletes existing image. Do not confuse with removing a marking.
	 * @param accessToken The IBS access token for the current user.
	 * @param imageId The id of the image to remove.
	 */
	@DELETE(ApiConstants.PATH + "image/{image_id}?user_type=2")
	public UpdateResult delete(@Query("access_token") String accessToken, 
			@Path("image_id") String imageId);
	
	/**
	 * Synchronous. Lists user created images.
	 * @param accessToken The IBS access token for the current user
	 */
	@GET(ApiConstants.PATH + "image")
	public ImageListResponse list(@Query("access_token") String accessToken);
	
	/**
	 * Synchronous. Returns a single user created image.
	 * @param accessToken The IBS access token for the current user.
	 * @param imageId The id of the image to return.
	 */
	@GET(ApiConstants.PATH + "image/{image_id}")
	public ImageItem get(@Query("access_token") String accessToken,  
			@Path("image_id") String imageId);
	
	/**
	 * Synchronous. Returns the list of library/admin images.
	 * @param accessToken The IBS access token for the current user.
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "imagelibrary")
	public ImageListResponse listLibrary(@Query("access_token") String accessToken);
}
