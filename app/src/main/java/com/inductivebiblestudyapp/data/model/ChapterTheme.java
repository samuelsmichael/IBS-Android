package com.inductivebiblestudyapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author Jason Jenkins
 * @version 0.1.1-20150626
 */
public class ChapterTheme implements Parcelable {

	//REMEMBER TO ADD TO PARCELABLE
	
	@SerializedName("id")
	String chapterThemeId;
	@SerializedName("chapter_id")
	String chapterId;
	String name;
	
	public String getChapterThemeId() {
		return chapterThemeId;
	}
	
	public String getChapterId() {
		return chapterId;
	}
	
	public String getText() {
		return name;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Parcelable.Creator<ChapterTheme> CREATOR = new Parcelable.Creator<ChapterTheme>() {
	    public ChapterTheme createFromParcel(Parcel in) {
	        return new ChapterTheme(in);
	    }
	
	    public ChapterTheme[] newArray(int size) {
	        return new ChapterTheme[size];
	    }
	};
	
	public ChapterTheme(Parcel src) {
		String[] values = new String[3];
		src.readStringArray(values);
		chapterThemeId = values[0];
		chapterId = values[1];
		name = values[2];
	}
	

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String[] values = new String[]{chapterThemeId, chapterId, name};
		
		dest.writeStringArray(values);		
	}

	
	@Override
	public int describeContents() {
		return 0;
	}
	
}
