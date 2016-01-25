package com.inductivebiblestudyapp.data.model;

import java.lang.reflect.Type;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * 
 * @author Jason Jenkins
 * @version 0.1.0-20150714
 */
public class LetteringListResponse implements Parcelable {
	
	public LetteringListResponse() {}
	
	/*
	 *
	 * Sample output:
	 * 
[
  {
    "id": 2,
    "bold": 1,
    "italics": 0,
    "underline": 0,
    "strikethrough": 0,
    "double_underline": 0,
    "boxed": 0,
    "double_boxed": 0,
    "font_color": "#ffffff",
    "background_color": "#000000",
    "member_id": 112,
    "name": "test 2"
  },
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
    "name": "test 2 edit"
  }

 ...
 ]
	 * 
	 */
	
	//REMEMBER TO ADD TO PARCELABLE
	
	/*default*/ LetteringItem[] mLetteringItems = new LetteringItem[0];
	
	
	public LetteringItem[] getLetterings() {
		return mLetteringItems;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Serializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class LetteringListDeserializer implements JsonDeserializer<LetteringListResponse> {
	  @Override
	  public LetteringListResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
	      throws JsonParseException {
		  final JsonArray jsonArray = json.getAsJsonArray();
	
		  final LetteringListResponse response = new LetteringListResponse();
		  
		  response.mLetteringItems = context.deserialize(jsonArray, LetteringItem[].class);		  
		  
		  return response;
	  }
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Parcelable.Creator<LetteringListResponse> CREATOR = new Parcelable.Creator<LetteringListResponse>() {
	    public LetteringListResponse createFromParcel(Parcel in) {
	        return new LetteringListResponse(in);
	    }
	
	    public LetteringListResponse[] newArray(int size) {
	        return new LetteringListResponse[size];
	    }
	};
	
	public LetteringListResponse(Parcel src) {
		String[] values = new String[3];
		src.readStringArray(values);
		
	}
	

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String[] values = new String[]{};
		
		dest.writeStringArray(values);		
	}

	
	@Override
	public int describeContents() {
		return 0;
	}
	
}
