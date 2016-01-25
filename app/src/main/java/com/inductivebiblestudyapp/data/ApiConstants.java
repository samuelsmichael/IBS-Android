package com.inductivebiblestudyapp.data;

/** 
 * A list of url base constants used throughout the RestClient and web-services.
 * @author Jason Jenkins
 * @version 0.3.0-20150924 */
public class ApiConstants {
	/** The domain where the services are hosted. Must have trailing slash. */
	public static final String BASE_URL = "http://inductivebiblestudyapp.com/";
	/** The service path on the domain. Starts and ends with slash. */
	public static final String PATH = "/app/api/"; 
	/** The full image base path. Cannot start with slash. */
	public static final String IMAGE_PATH = BASE_URL + "app/";
}
