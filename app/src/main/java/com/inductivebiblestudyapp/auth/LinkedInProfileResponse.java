package com.inductivebiblestudyapp.auth;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * 
 * @version 0.3.1-20150625
 *
 */
class LinkedInProfileResponse {

	public static String QUERY = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,email-address,location:(name),picture-url)";;
	/* 
	 * 
	 * 
	 * https://developer.linkedin.com/docs/rest-api
	 * https://developer.linkedin.com/docs/fields/basic-profile
	 * https://developer.linkedin.com/docs/fields/location
	 * 
	 * Sample response:
	 *  {
		  "firstName": "Frodo",
		  "headline": "Jewelery Repossession in Middle Earth",
		  "id": "1R2RtA",
		  "lastName": "Baggins",
		  "siteStandardProfileRequest": {
		    "url": "https://www.linkedin.com/profile/view?id=..."
		  }
		  location: {
		  	name:  "San Francisco Bay Area",
		  	country: "us"
		  }
		}
	 *
	 * Although we are only requesting id & names at the moment.
	 */
	
	private String id;
	private String firstName;
	private String lastName;
	
	private String email;
	private Location location;
	
	private String pictureUrl;
	
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
	
	public String getLocation() {
		if (location == null) {
			return "";
		}
		return location.name;
	}
	
	/** The picture url or null on failure. */
	public String getPictureUrl() {
		return pictureUrl;
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
		String name = ""; 
		
		@Override
		public String toString() {
			return super.toString() + "["+name+"]";
		}
	}
	
	
	
	public static class ProfileDeserializer implements JsonDeserializer<LinkedInProfileResponse> {

		  @Override
		  public LinkedInProfileResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
		      throws JsonParseException {
			  final JsonObject jsonObject = json.getAsJsonObject();
	
			  final LinkedInProfileResponse response = new LinkedInProfileResponse();
			  
			  response.id = jsonObject.get("id").getAsString();
			  response.firstName = jsonObject.get("firstName").getAsString();
			  response.lastName = jsonObject.get("lastName").getAsString();
			  if (jsonObject.get("location") != null) {
				  response.location = context.deserialize(jsonObject.get("location"), Location.class);
			  }
			  if ( jsonObject.get("pictureUrl") != null) {
				  response.pictureUrl = jsonObject.get("pictureUrl").getAsString();
			  }
			  
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
