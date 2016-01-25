package com.inductivebiblestudyapp;

/** @version 0.3.0-20150924 */
public class DebugConstants {
	//TODO set values true for testing, false for production
	public final static boolean DEBUG_REQUESTS = false;
	public final static boolean DEBUG_BILLING = false;
	public final static boolean DEBUG_LICENSE_KEY = false;
	
	/** Remote error reporting. */
	public final static boolean REMOTE_ERROR_REPORTING = false; 
	/** Includes user specific, sensitive data. Always set false for production. 
	 * @see #REMOTE_ERROR_REPORTING */
	public final static boolean REMOTE_PDETAIL_REPORTING= false;
	/** Used to force a simple image search & thus not use up the quota. 
	 * Set false before release. */
	public final static boolean FORCE_SIMPLE_IMAGE_SEARCH = false;
}
