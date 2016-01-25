package com.inductivebiblestudyapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author Jason Jenkins
 * @version 0.1.0-20150625
 */
public class Note implements Parcelable {

	//REMEMBER TO ADD TO PARCELABLE
	
	@SerializedName("study_note_id")
	String noteId;
	@SerializedName("verse_id")
	String verseId;
	String text;
	
	public String getNoteId() {
		return noteId;
	}
	
	public String getVerseId() {
		return verseId;
	}
	
	public String getText() {
		return text;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>() {
	    public Note createFromParcel(Parcel in) {
	        return new Note(in);
	    }
	
	    public Note[] newArray(int size) {
	        return new Note[size];
	    }
	};
	
	public Note(Parcel src) {
		String[] values = new String[3];
		src.readStringArray(values);
		noteId = values[0];
		verseId = values[1];
		text = values[2];
	}
	

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String[] values = new String[]{noteId, verseId, text};
		
		dest.writeStringArray(values);		
	}

	
	@Override
	public int describeContents() {
		return 0;
	}
	
}
