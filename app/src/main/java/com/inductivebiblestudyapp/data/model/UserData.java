package com.inductivebiblestudyapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
* The user data
* @author Jason Jenkins
* @version 0.5.0-20150716
*/
public class UserData implements Parcelable {
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// REMEMBER: Add new members to the parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@SerializedName("member_id")
	private Integer memberId  = -1;

	@SerializedName("social_user_id")
	private String socialUserId;
	
	@SerializedName("access_token")
	private String accessToken;
	
	@SerializedName("profile_image")
	private String profileImage;
	
	private Integer isactive = -1;		
	private Integer ban = -1;
	
	@SerializedName("first_name")
	private String firstName;
	@SerializedName("last_name")
	private String lastName;
	
	private String email;		
	private String address;
	private String city;
	private String zip;
	private String state;
	private String bio;
	
	@SerializedName("activation_key")
	private String activationKey;
	
	private String type;
	
	@SerializedName("bible_version")
	private String bibleVersionId;
	
	/** Supplied manually via profile response. */
	private String bibleVersionName = "";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Mutators
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @param bibleVersionName The version name as found in the response. */
	public void setBibleVersionName(String bibleVersionName) {
		this.bibleVersionName = bibleVersionName;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Accessors
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/** @return <code>true</code> when the user has completed setup, <code>false</code> otherwise. */
	public boolean hasUserSetup() {
		return !(firstName.isEmpty() || lastName.isEmpty());
	}	
	
	public String getProfileImage() {
		return profileImage;
	}

	public String getType() {
		return type;
	}
	
	public int getMemberId() {
		return memberId;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public boolean isActive() {
		return isactive == 1;
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
	
	public String getAddress() {
		return address;
	}
	
	public String getCity() {
		return city;
	}
	
	public String getState() {
		return state;
	}
	
	public String getZip() {
		return zip;
	}
	
	
	public String getBio() {
		return bio;
	}
	
	/** @return The select bible version id. */
	public String getBibleVersionId() {
		return bibleVersionId;
	}
	
	/** @return The bible version name or blank if unset. */
	public String getBibleVersionName() {
		return bibleVersionName;
	}
	
	@Override
	public String toString() {
		return super.toString() + 
				"[ member: "+ memberId + " first:" + firstName + ", last:  " + lastName + "]";
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
		int[] intVals = new int[]{
				memberId,
				isactive,
				ban
		};
		
		String[] sVals = new String[]{
			accessToken,	
			firstName,
			lastName,		
			email,	
			address,
			city,
			zip,
			state,
			bio,
			activationKey,		
			type,
			socialUserId,
			profileImage
		};
		
		dest.writeIntArray(intVals);
		dest.writeStringArray(sVals);
		
	}
	
	public UserData(Parcel src) {
		int[] intVals = new int[3];
		String[] sValues = new String[12];
		
		src.readIntArray(intVals);
		src.readStringArray(sValues);
		
		memberId = intVals[0];
		isactive = intVals[2];
		ban = intVals[3];
		
		accessToken = sValues[0];
		firstName = sValues[1];
		lastName = sValues[2];		
		email = sValues[3];		
		address = sValues[4];
		city = sValues[5];
		zip = sValues[6];
		state = sValues[7];
		bio = sValues[8];
		activationKey = sValues[9];		
		type  = sValues[10];
		socialUserId = sValues[11];
		profileImage = sValues[12];
	}
	
	public static final Parcelable.Creator<UserData> CREATOR = new Parcelable.Creator<UserData>() {
        public UserData createFromParcel(Parcel in) {
            return new UserData(in); 
        }

        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };
	
}
