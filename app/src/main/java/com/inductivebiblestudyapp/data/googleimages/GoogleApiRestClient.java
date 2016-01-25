package com.inductivebiblestudyapp.data.googleimages;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inductivebiblestudyapp.DebugConstants;
import com.inductivebiblestudyapp.data.model.googleimages.BasicGoogleImageResponse;
import com.inductivebiblestudyapp.data.model.googleimages.CustomSearchGoogleImageResponse;
import com.inductivebiblestudyapp.data.service.googleimages.BasicGoogleImageSearchService;
import com.inductivebiblestudyapp.data.service.googleimages.CustomSearchGoogleImageSearchService;
import com.squareup.okhttp.OkHttpClient;

/**
 * The rest client that builds services to send and parse requests/responses to/from the 
 * basic, no options, google images ajax api.
 * @author Jason Jenkins
 * @version 0.4.1-20150903
 * */
public class GoogleApiRestClient {
	private static final boolean DEBUGGING = DebugConstants.DEBUG_REQUESTS;
	
	/*
	 * Note a singleton is used as advised by the library's creator:
	 *  http://stackoverflow.com/questions/20579185/is-there-a-way-to-reuse-builder-code-for-retrofit/20627010#20627010
	 */
	
	private static final BasicGoogleImageApiRestClient sBasicInstance = new BasicGoogleImageApiRestClient();	
	private static final CustomSearchGoogleImageRestClient sCustomInstance = new CustomSearchGoogleImageRestClient();
	
	/** @return The sole instance of the base google image client. */
	public static BasicGoogleImageApiRestClient getBasicImageApi() {
		return sBasicInstance;
	}
	
	
	/** @return The sole instance of the base google image client. */
	public static CustomSearchGoogleImageRestClient getCustomSearchApi() {
		return sCustomInstance;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.1.0-20150730 */
	public static class BasicGoogleImageApiRestClient {	
	
		private static final String BASE_URL = 	"https://ajax.googleapis.com/";
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// End statics
		////////////////////////////////////////////////////////////////////////////////////////////////
	
		private BasicGoogleImageSearchService googleImageSearchService;
	
	
		protected BasicGoogleImageApiRestClient() {
			Gson gson = new GsonBuilder()
						.setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
						.registerTypeHierarchyAdapter(BasicGoogleImageResponse.class, new BasicGoogleImageResponse.BasicGoogleImageSearchDeserializer())
							.create();
						
			RestAdapter restAdapter = new RestAdapter.Builder()
				.setLogLevel(DEBUGGING ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
				.setEndpoint(BASE_URL)
				.setConverter(new GsonConverter(gson))
				.setClient(new OkClient(new OkHttpClient()))
				.build();
			
			googleImageSearchService = restAdapter.create(BasicGoogleImageSearchService.class);
			
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Service getters
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		public BasicGoogleImageSearchService getGoogleImageSearchService() {
			return googleImageSearchService;
		}
	
	}
	
	/** @version 0.1.0-20150730 */
	public static class CustomSearchGoogleImageRestClient {	
	
		private static final String BASE_URL = 	"https://www.googleapis.com/";
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// End statics
		////////////////////////////////////////////////////////////////////////////////////////////////
	
		private CustomSearchGoogleImageSearchService googleImageSearchService;
	
	
		protected CustomSearchGoogleImageRestClient() {
			Gson gson = new GsonBuilder()
						.setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
						.registerTypeHierarchyAdapter(CustomSearchGoogleImageResponse.class, 
								new CustomSearchGoogleImageResponse.CustomSearchGoogleImageSearchDeserializer())
							.create();
						
			RestAdapter restAdapter = new RestAdapter.Builder()
				.setLogLevel(DEBUGGING ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
				.setEndpoint(BASE_URL)
				.setConverter(new GsonConverter(gson))
				.setClient(new OkClient(new OkHttpClient()))
				.build();
			
			googleImageSearchService = restAdapter.create(CustomSearchGoogleImageSearchService.class);
			
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Service getters
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		public CustomSearchGoogleImageSearchService getGoogleImageSearchService() {
			return googleImageSearchService;
		}
	
	}
	
}
