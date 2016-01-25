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
 * @version 0.1.0-20150710
 */
public class ImageListResponse implements Parcelable {
	
	public ImageListResponse() {}
	
	/*
	 *
	 * Sample output:
	 * 
[
  {
    "id": 3,
    "name": "Test name",
    "member_id": 112,
    "path": "uploads\/images\/55a00d08ed4e7.png"
  },
  {
    "id": 4,
    "name": "Test name",
    "member_id": 112,
    "path": "uploads\/images\/55a00e404c989.jpg"
  }

 ...
 ]
	 * 
	 */
	
	//REMEMBER TO ADD TO PARCELABLE
	
	/*default*/ ImageItem[] mImageItems = new ImageItem[0];
	
	
	public ImageItem[] getImages() {
		return mImageItems;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Serializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class ImageListDeserializer implements JsonDeserializer<ImageListResponse> {
	  @Override
	  public ImageListResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
	      throws JsonParseException {
		  final JsonArray jsonArray = json.getAsJsonArray();
	
		  final ImageListResponse response = new ImageListResponse();
		  
		  response.mImageItems = context.deserialize(jsonArray, ImageItem[].class);		  
		  
		  return response;
	  }
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Parcelable.Creator<ImageListResponse> CREATOR = new Parcelable.Creator<ImageListResponse>() {
	    public ImageListResponse createFromParcel(Parcel in) {
	        return new ImageListResponse(in);
	    }
	
	    public ImageListResponse[] newArray(int size) {
	        return new ImageListResponse[size];
	    }
	};
	
	public ImageListResponse(Parcel src) {
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
