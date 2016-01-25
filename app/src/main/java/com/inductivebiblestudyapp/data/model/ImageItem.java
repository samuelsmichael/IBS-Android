package com.inductivebiblestudyapp.data.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.inductivebiblestudyapp.data.ApiConstants;

/**
 * 
 * @author Jason Jenkins
 * @version 0.2.0-20150729
 */
public class ImageItem implements Parcelable {
	final static private String CLASS_NAME = LetteringItem.class
			.getSimpleName();
	static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

	//REMEMBER TO ADD TO PARCELABLE
	
	/*
	 *
	 * Sample output 1:
	 * 
{
   "id": 3,
   "name": "Test name",
   "member_id": 112,
   "path": "uploads\/images\/55a00d08ed4e7.png"
   "last_used": "2015-07-29 00:26",
    ...
  }
  
  Sample output 2:
  
    {
    "id": 2,
	...
    "last_used": null,
    ...
  }
	 * 
	 */
	
	private String id;
	private String name;
	private String path;
	
	/** Parsed into #dateTime when fetched. */
	@SerializedName("last_used")
	String lastUsed = "";
	private long lastUsedTime = 0;
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	

	public String getUrl() {
		if (path.startsWith("\\/")) {
			path = path.replaceFirst("\\/", "");
		}
		return ApiConstants.IMAGE_PATH + Uri.decode(path);
	}
	
	/** @return The (UTC) time since epoch of when last used, or 0
	 * if never used */
	public long getLastUsedTime() {
		if (lastUsedTime > 0) {
			return lastUsedTime; //return as quickly as possible
		} else if (lastUsed != null && !lastUsed.isEmpty()) {
			try {
				lastUsedTime = DATE_FORMAT.parse(lastUsed).getTime();
			} catch (ParseException e) {
				Log.w(CLASS_NAME, "\""+lastUsed+"\" could not parse correctly", e);
				lastUsed = ""; //avoid repeated parses
			}
		}
		return lastUsedTime;
	}
	
	@Override
	public String toString() {
		return super.toString() + "[id: " + id + ", name: \"" + name + 
								"\", last_used: " + lastUsed + "]";
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Parcelable.Creator<ImageItem> CREATOR = new Parcelable.Creator<ImageItem>() {
	    public ImageItem createFromParcel(Parcel in) {
	        return new ImageItem(in);
	    }
	
	    public ImageItem[] newArray(int size) {
	        return new ImageItem[size];
	    }
	};
	
	public ImageItem(Parcel src) {
		String[] values = new String[4];
		src.readStringArray(values);
		id = values[0];
		name = values[1];
		path = values[2];
		lastUsed = values[3];
	}
	

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String[] values = new String[]{id, name, path, lastUsed};		
		dest.writeStringArray(values);		
	}

	
	@Override
	public int describeContents() {
		return 0;
	}
	
}
