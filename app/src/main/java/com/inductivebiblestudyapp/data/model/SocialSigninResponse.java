package com.inductivebiblestudyapp.data.model;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author Jason Jenkins
 * @version 0.2.0-20150617
 */
public class SocialSigninResponse {	
	
	/*
	 * Sample output:
	 *
output 1:
{
  "social_user_id": [
    "The social user id field is required."
  ],
  "type": [
    "The type field is required."
  ]
}
output 2:
{
  "data": {
    "member_id": 83,
    "first_name": "",
    "last_name": "",
    "email": "",
    "password": "",
    "date": "0000-00-00 00:00:00",
    "isactive": 0,
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
    "updated_at": "-0001-11-30 00:00:00",
    "company": "",
    "activation_key": "",
    "type": "facebook",
    "social_user_id": 0,
    "access_token": "2a937a3e4655e5b170f13c618cd2e93b"
  },
  "success": true,
  "msg": "Succesfull login"
}

	 * 
	 */

	
	/** The social user id error response (if any) */
	@SerializedName("social_user_id")
	private List<String> userId;
	/** The type response (if any) */	
	private List<String> type;
	
	/** The user data. */
	@SerializedName("data")
	private UserData data;
	
	
	/**  @return The user id error message, if any, or a blank string.
	 */
	public String getUserIdErrorMessage() {
		return Utility.listToString(userId);
	}	
	
	
	/**  @return The type error message, if any, or a blank string.
	 */
	public String getTypeErrorMessage() {
		return Utility.listToString(type);
	}
	
	
	/**
	 * @return The {@link UserData} if set or null.
	 */
	public UserData getData() {
		return data;
	}
	
	@Override
	public String toString() {
		return super.toString() + 
				"[userId: " + userId + ", type"+ type +", data: " + data +"]";
	}
	
	public static class SocialSigninDeserializer implements JsonDeserializer<SocialSigninResponse> {
		@Override
		public SocialSigninResponse deserialize(JsonElement je, Type type,
				JsonDeserializationContext jdc) throws JsonParseException {
			JsonElement data = je.getAsJsonObject().get("data");
			
			if (data == null) { //data is not always supplied
				data = je;
			} 
			//using new instance to avoid infinite recursion
			return new Gson().fromJson(data, SocialSigninResponse.class);
		}
	}
	
}
