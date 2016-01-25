package com.inductivebiblestudyapp.data.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

/**
 * @author Jason Jenkins
 * @version 0.2.1-20150910
 */
public class LetteringItem implements Parcelable {
	final static private String CLASS_NAME = LetteringItem.class
			.getSimpleName();
	static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

	//REMEMBER TO ADD TO PARCELABLE
	
	/*
	 *
	 * Sample output 1:
	 * 
  {
    "id": 1,
    "bold": 1,
    "italics": 0,
    "underline": 0,
    "strikethrough": 0,
    "double_underline": 0,
    "boxed": 0,
    "double_boxed": 0,
    "font_color": "#000000",
    "background_color": "#ffffff",
    "member_id": 112,
    "name": "test 2 edit",
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
	
	String id;
	String name;
	
	int bold;
	int italics;
	int strikethrough; 
	int underline; 		
	@SerializedName("double_underline")
	int doubleUnderline; 	
	int boxed;
	@SerializedName("double_boxed")
	int doubleBoxed;
	
	@SerializedName("font_color")
	String fontColor;
	@SerializedName("background_color")
	String backgroundColor;
	
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

	public boolean getBold() {
		return bold != 0;
	}
	
	public boolean getItalics() {
		return italics != 0;
	}
	
	public boolean getBoxed() {
		return boxed != 0;
	}
	
	public boolean getDoubleBoxed() {
		return doubleBoxed != 0;
	}
	public boolean getDoubleUnderline() {
		return doubleUnderline != 0;
	}
	
	public boolean getStrikethrough() {
		return strikethrough != 0;
	}
	public boolean getUnderline() {
		return underline != 0;
	}
	
	/** @return The background color or blank if not a hex value. */
	public String getBackgroundColor() {
		return fixAndReturnColor(backgroundColor);
	}

	
	/** @return The text color or blank if not a hex value. */
	public String getFontColor() {
		return fixAndReturnColor(fontColor);
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
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	/** Attempts to fix the color and returns it, if not a hex value (e.g. #ff00112233)
	 * returns blank
	 * @param color
	 */
	private static String fixAndReturnColor(String color) {
		color = color.trim();
		if (color.startsWith("#")) {
			if (color.length() == 7) { //if only equal to 7 vs. 9
				color = color.replaceAll("#", "#ff");
			}
			if (color.length() == 9) {
				return color;
			}
		}
		return ""; 
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Parcelable.Creator<LetteringItem> CREATOR = new Parcelable.Creator<LetteringItem>() {
	    public LetteringItem createFromParcel(Parcel in) {
	        return new LetteringItem(in);
	    }
	
	    public LetteringItem[] newArray(int size) {
	        return new LetteringItem[size];
	    }
	};
	
	public LetteringItem(Parcel src) {
		String[] values = new String[4];
		src.readStringArray(values);
		id = values[0];
		name = values[1];
		backgroundColor = values[2];
		fontColor = values[3];
		lastUsed = values[4];
		
		int[] vals = new int[7];
		
		bold = vals[0];
		italics = vals[1];
		strikethrough = vals[2];
		underline = vals[3];
		doubleUnderline = vals[4];
		boxed = vals[5];
		doubleBoxed = vals[6];
	}
	

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String[] values = new String[]{id, name, backgroundColor, fontColor, lastUsed};		
		dest.writeStringArray(values);	
		
		int[] vals = new int[]{
			bold, italics, strikethrough, underline, 
			doubleUnderline, boxed, doubleBoxed	
		};
		dest.writeIntArray(vals);
	}

	
	@Override
	public int describeContents() {
		return 0;
	}
	
}
