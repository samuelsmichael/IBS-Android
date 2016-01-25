package com.inductivebiblestudyapp.data.model;

import java.util.List;

/**
 * 
 * @author Jason Jenkins
 * @version 0.1.3-20150902
 */
public class ForgotPasswordResponse {	
	
	/*
	 * Sample output:
	 *
output 1: 
{
  "email": [
    "The email field is required."
  ],
}

output 2:
{
  "data": "",
  "success": false,
  "msg": "Wrong email"
}


	 * 
	 */

	/** The email error response (if any) */
	private List<String> email;
	/** The success of the forgot request. */
	private boolean success = true;
	/** The error message given when the email is invalid. */
	private String msg;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Accessors
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**  @return The email error message, if any, or a blank string.
	 */
	public String getEmailErrorMessage() {
		if (!success) {
			return msg;
		}
		return Utility.listToString(email);
	}
	
	/** @return <code>true</code> when successful, <code>false</code> when failed. */
	public boolean isSuccessful() {
		return success || (email == null || email.isEmpty()); 
	}
	
	
	
	@Override
	public String toString() {		
		return super.toString() + 
				"[email errors: "+ email+ ", success: "+success+ "']";
	}	
	
		
}
