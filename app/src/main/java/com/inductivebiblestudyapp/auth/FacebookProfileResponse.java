package com.inductivebiblestudyapp.auth;



import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * 
 * @version 0.2.0-20150620
 *
 */
class FacebookProfileResponse {

	/* 
	 * https://developers.facebook.com/docs/android/graph
	 * https://developers.facebook.com/docs/facebook-login/permissions/v2.3#reference-public_profile
	 * https://developers.facebook.com/docs/facebook-login/permissions/v2.3#reference-user_location
	 * 
	 * Sample response:
	 *  {
  "id": "12345678", 
  "birthday": "1/1/1950", 
  "first_name": "Chris", 
  "gender": "male", 
  "last_name": "Colm", 
  "link": "http://www.facebook.com/12345678", 
  "location": {
    "id": "110843418940484", 
    "name": "Seattle, Washington"
  }, 
  "locale": "en_US", 
  "name": "Chris Colm", 
  "timezone": -8, 
  "updated_time": "2010-01-01T16:40:43+0000", 
  "verified": true
}
	 *
	 * Although we are only requesting id at the moment.
	 */
	
	private String id;
	@SerializedName("first_name")
	private String firstName = "";
	@SerializedName("last_name")
	private String lastName = "";
	
	private String email;
	
	
	private Location location = new Location();

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Accessors
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public String getId() {
		return id;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public String getEmail() {
		return email;
	}
	
	public Location getLocation() {
		return location;
	}
	
	/** Not yet functional.
	 * Attempts to extract the state from location
	 * @return The state or an empty string. */
	public String getState() {
		final String location = this.location.name;
		final int start = location.indexOf(",");
		
		if (start < 0) {
			return "";
		}
		return location.substring(start).trim();
	}
	
	/** Not yet functional. 
	 * Attempts to extract the citiy from location
	 * @return The city or an empty string. */
	public String getCity() {
		final String location = this.location.name;
		final int end = location.indexOf(",");
		
		if (end < 0) {
			return "";
		}
		return location.substring(0, end).trim();
	}
	
	@Override
	public String toString() {
		return super.toString() + 
				"[ id:" + id + ", firstName: " +firstName + ", last: " +lastName + 
				", email: " + email + ", location: " +location +" ]"; 
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	static public class Location {
		private String name = ""; 
		
		@Override
		public String toString() {
			return super.toString() + "["+name+"]";
		}
	}
	
	public static class FacebookResponseDeserializer implements JsonDeserializer<FacebookProfileResponse> {

		  @Override
		  public FacebookProfileResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
		      throws JsonParseException {
			  final JsonObject jsonObject = json.getAsJsonObject();
	
			  final FacebookProfileResponse response = new FacebookProfileResponse();
			  
			  response.id = jsonObject.get("id").getAsString();
			  response.firstName = jsonObject.get("first_name").getAsString();
			  response.lastName = jsonObject.get("last_name").getAsString();
			  response.location = context.deserialize(jsonObject.get("location"), Location.class);
			  
			  return response;
		  }
		}
	
	public static class LocationDeserializer implements JsonDeserializer<Location> {
		@Override
		public Location deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context)
			throws JsonParseException {
				final JsonObject jsonObject = json.getAsJsonObject();
			
				final Location location = new Location();
				location.name = jsonObject.get("name").getAsString();
				return location;
			}
	}
	
}
