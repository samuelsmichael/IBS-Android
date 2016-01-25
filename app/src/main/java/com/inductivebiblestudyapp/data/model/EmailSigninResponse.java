package com.inductivebiblestudyapp.data.model;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * 
 * @author Jason Jenkins
 * @version 0.2.2-20150622
 */
public class EmailSigninResponse {	
	
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
  "msg": "Wrong email or password"
}

output 3:
{
  "data": {
    "member_id": 81,
    "first_name": "",
    "last_name": "",
    "email": "name@example.com",
    "date": "0000-00-00 00:00:00",
    "isactive": 1,
    "ban": 0,
    "address": "",
    "city": "",
    "state": "",
    "zip": "",
    "country": "",
    "profile_image": "",
    "sex": "",
    "birth_date": "0000-00-00",
    "registration_date": "0000-00-00",
    "bio": "",
    "created_at": "-0001-11-30 00:00:00",
    "updated_at": "2015-06-17 00:00:00",
    "company": "",
    "activation_key": "47b51266eb44e7ca0ca9415607a05b7f",
    "type": "email",
    "social_user_id": 0,
    "access_token": "130ad65088a29f83648ea207fc33b18f"
  },
  "success": true,
  "msg": "User Fetched"
}


	 * 
	 */

	
	/** The password error response (if any) */
	private List<String> password;
	/** The email error response (if any) */
	private List<String> email;
	/** The success of the signin request. */
	private boolean success = true;
	
	//worth noting: http://www.codedisqus.com/CNVkPjjeWq/retrofit-gson-converter-for-nested-json-with-different-objects.html
	private Object data;
	
	/** Where we will actually store data. */
	private UserData mUserData = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Accessors
	////////////////////////////////////////////////////////////////////////////////////////////////
	
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
	
	/**
	 * @return The {@link UserData} if set or null.
	 */
	public UserData getData() {  
		return mUserData;
	}
	
	@Override
	public String toString() {		
		return super.toString() + 
				"[password errors: "+ password + ", email: "+ email+
				", success: "+success+", data: '"+data+"', " +
				"userData: '" +getData()+(mUserData == null ? "null" : mUserData.toString()) + "']";
	}	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public static class EmailSigninDeserializer implements JsonDeserializer<EmailSigninResponse> {
		@Override
		public EmailSigninResponse deserialize(JsonElement je, Type type,
				JsonDeserializationContext jdc) throws JsonParseException {
			JsonObject content = je.getAsJsonObject();
			EmailSigninResponse response = new Gson().fromJson(je, EmailSigninResponse.class);			
			JsonElement data = content.get("data");
			
			if (!(response.data instanceof String)) { //data is not always supplied as object/string
				response.mUserData = new Gson().fromJson(data, UserData.class);
			} 
			return response;
		}
	}
	
	
}
