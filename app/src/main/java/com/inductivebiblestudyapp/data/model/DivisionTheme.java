package com.inductivebiblestudyapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author Jason Jenkins
 * @version 0.1.0-20150626
 */
public class DivisionTheme implements Parcelable {

	//REMEMBER TO ADD TO PARCELABLE
	
	@SerializedName("id")
	String divisionId;
	@SerializedName("verse_id")
	String verseId;
	String name;
	
	public String getDivisionThemeId() {
		return divisionId;
	}
	
	public String getVerseId() {
		return verseId;
	}
	
	public String getText() {
		return name;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Parcelable.Creator<DivisionTheme> CREATOR = new Parcelable.Creator<DivisionTheme>() {
	    public DivisionTheme createFromParcel(Parcel in) {
	        return new DivisionTheme(in);
	    }
	
	    public DivisionTheme[] newArray(int size) {
	        return new DivisionTheme[size];
	    }
	};
	
	public DivisionTheme(Parcel src) {
		String[] values = new String[3];
		src.readStringArray(values);
		divisionId = values[0];
		verseId = values[1];
		name = values[2];
	}
	

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String[] values = new String[]{divisionId, verseId, name};
		
		dest.writeStringArray(values);		
	}

	
	@Override
	public int describeContents() {
		return 0;
	}
	
}
