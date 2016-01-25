package com.inductivebiblestudyapp.data.model;

import java.util.List;

/**
 * 
 * @author Jason Jenkins
 * @version 0.2.0-20150617
 */
public class EmailSignupResponse {	
	
	/*
	 * Sample output:
	 *
output 1:
{
  "email": [
    "The email field is required."
  ],
  "password": [
    "The password field is required."
  ]
}

output 2:
{
  "data": "",
  "success": false,
  "msg": "User already exists"
}

	 * 
	 */
	
	/** The password error response (if any) */
	private List<String> password;
	/** The email error response (if any) */
	private List<String> email;
	/** The success of the signup request. */
	private boolean success = true;
	
	/**  @return The name error message, if any, or a blank string.
	 */
	public String getPasswordErrorMessage() {
		return Utility.listToString(password);
	}

	
	/**  @return The email error message, if any, or a blank string.
	 */
	public String getEmailErrorMessage() {
		return Utility.listToString(email);
	}
	
	/** @return <code>true</code> when successful, <code>false</code> when failed. */
	public boolean isSuccessful() {
		return success;
	}
	
	@Override
	public String toString() {
		return super.toString() + 
				"[password errors: "+ password  + ", email: "+ email+", success: "+success+"]";
	}	
	
}
