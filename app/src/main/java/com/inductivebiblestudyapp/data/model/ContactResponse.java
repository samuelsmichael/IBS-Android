package com.inductivebiblestudyapp.data.model;

import java.util.List;

/**
 * 
 * @author Jason Jenkins
 * @version 0.2.0-20150902
 */
public class ContactResponse {	
	
	/*
	 * Sample output 1:
	 * 
	 *{
  "success": false,
  
  "name": [
    "The name field is required."
  ],
  "email": [
    "The email field is required."
  ],
  "phone": [
    "The phone field is required."
  ],
  "message": [
    "The message field is required."
  ]
}


Sample output 2:
{ "success": true }
	 * 
	 */

	private boolean success;
	
	/** The name error response (if any) */
	private List<String> name;
	/** The email error response (if any) */
	private List<String> email;
	/** The phone error response (if any) */
	private List<String> phone;
	/** The message error response (if any) */
	private List<String> message;
	
	public boolean isSuccessful() {
		return success;
	}
	
	/**  @return The name error message, if any, or a blank string.
	 */
	public String getNameErrorMessage() {
		return Utility.listToString(name);
	}

	
	/**  @return The email error message, if any, or a blank string.
	 */
	public String getEmailErrorMessage() {
		return Utility.listToString(email);
	}
	
	/**  @return The phone error message, if any, or a blank string.
	 */
	public String getPhoneErrorMessage() {
		return Utility.listToString(phone);
	}
	
	/**  @return The message error message, if any, or a blank string.
	 */
	public String getMessageErrorMessage() {
		return Utility.listToString(message);
	}
	
	@Override
	public String toString() {
		return super.toString() + 
				"[success: " + success + ", name:" +  name + ", email: "+ email+ 
				", phone: "+ phone+ ", message: "+ message+"]";
	}
	
}
