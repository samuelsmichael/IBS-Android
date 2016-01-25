package com.inductivebiblestudyapp.data.model;

import java.lang.reflect.Type;
import java.util.Arrays;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
* The profile data
* @author Jason Jenkins
* @version 0.3.0-20150826
*/
public class ProfileResponse implements Parcelable {
	final static private String CLASS_NAME = ProfileResponse.class
			.getSimpleName();
	private static final String KEY_USERDATA = CLASS_NAME + ".KEY_USERDATA";
	private static final String KEY_VERSIONS = CLASS_NAME + ".KEY_VERSIONS";
	
	/*
	 * Sample good output:
	 * 
{
  "user_data": [
    {
      "member_id": 82,
      "first_name": "first",
      "last_name": "last",
      "email": "",
      "password": "",
      "date": "0000-00-00 00:00:00",
      "isactive": 0,
      "ban": 0,
      "address": "address",
      "city": "city",
      "state": "state",
      "zip": "000000",
      "country": "",
      "profile_image": "",
      "sex": "",
      "birth_date": "0000-00-00",
      "registration_date": "0000-00-00",
      "bio": "",
      "created_at": "-0001-11-30 00:00:00",
      "updated_at": "2015-06-19 00:00:00",
      "company": "",
      "activation_key": "",
      "type": "twitter",
      "social_user_id": 343,
      "access_token": "...",
      "bible_version": "431"
    }
  ],
  "versions": [
    {
      "version_id": "401",
      "name": "Amplified Bible"
    },
    {
      "version_id": "431",
      "name": "King James Version"
    },
    {
      "version_id": "81",
      "name": "The Message"
    },
    {
      "version_id": "317",
      "name": "New American Standard Bible"
    }
  ]
}

Sample bad output:
{"data":"","success":false,"msg":"Wrong access_token"}

	 */
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// REMEMBER: Add new members to the parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@SerializedName("user_data")
	UserData[] userData = new UserData[0];
	
	Version[] versions = new Version[0];
	
	public ProfileResponse() {}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Accessors
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @return <code>true</code> when successful, <code>false</code> when failed. */
	public boolean isSuccessful() {
		return userData != null && versions != null;
	}
	
	/** @return The user data or <code>null</code> 
	 */
	public UserData getUserData() {
		if (userData != null && userData.length > 0) {
			return userData[0];
		}
		return null;
	}
	
	/** @return The offered bible versions (KJV, NASB, etc) or <code>null</code>. */
	public Version[] getVersions() {
		return versions;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Accessors
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	@Override
	public String toString() {
		return super.toString() + 
				"[ userdata: " + userData + " versions:"  + versions +  "]";
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Parcelable implementation
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle data = new Bundle();
		data.putParcelableArray(KEY_USERDATA, userData);
		data.putParcelableArray(KEY_VERSIONS, versions);
	
		dest.writeBundle(data);		
	}
	
	public ProfileResponse(Parcel src) {
		Bundle data = src.readBundle();
		Parcelable[] pArray = data.getParcelableArray(KEY_USERDATA);
		userData = Arrays.copyOf(pArray, pArray.length, UserData[].class);
		pArray =  data.getParcelableArray(KEY_VERSIONS);
		versions = Arrays.copyOf(pArray, pArray.length, Version[].class);
	}
	

	public static final Parcelable.Creator<ProfileResponse> CREATOR = new Parcelable.Creator<ProfileResponse>() {
        public ProfileResponse createFromParcel(Parcel in) {
            return new ProfileResponse(in); 
        }

        public ProfileResponse[] newArray(int size) {
            return new ProfileResponse[size];
        }
    };
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
    /** @version 0.1.0-20150716 */
	public static class Version implements Parcelable {
		@SerializedName("version_id")
		String versionId;
		String name;
		
		public String getVersionId() {
			return versionId;
		}
		
		public String getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return super.toString() + "[id: " +versionId + ", name: " + name + "]";
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Parcelable
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			String[] sVals = new String[]{
					versionId, name
			};
			
			dest.writeStringArray(sVals); 			
		}
		
		public Version(Parcel src) {
			String[] sValues = new String[2];
			src.readStringArray(sValues);			
			
			versionId = sValues[0];
			name = sValues[1];
		}
		
		public static final Parcelable.Creator<Version> CREATOR = new Parcelable.Creator<Version>() {
	        public Version createFromParcel(Parcel in) {
	            return new Version(in); 
	        }

	        public Version[] newArray(int size) {
	            return new Version[size];
	        }
	    };
	}
	
	public static class ProfileDeserializer implements JsonDeserializer<ProfileResponse> {

		  @Override
		  public ProfileResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
		      throws JsonParseException {
			  final JsonObject jsonObject = json.getAsJsonObject();
	
			  final ProfileResponse response = new ProfileResponse();
			  
			  response.userData = context.deserialize(jsonObject.get("user_data"), UserData[].class);
			  response.versions = context.deserialize(jsonObject.get("versions"), Version[].class);	
			  
			  if (response.userData != null && response.userData.length > 0) {
				  final String versionId = response.userData[0].getBibleVersionId();
				  
				  for (Version version : response.versions) {
					  if (versionId.equals(version.getVersionId())) {
						  //if the version is found, set and break;
						  response.userData[0].setBibleVersionName(version.getName());
						  break;
					  }
				  } 
			  }
			  
			  
			  return response;
		  }
	}
}
