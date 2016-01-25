package com.inductivebiblestudyapp.data.model;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * 
 * @author Jason Jenkins
 * @version 0.2.1-20150731
 */
public class ContentResponse implements IContentResponse {	
	
	/*
	 * Sample output:
	 * 
	 * {
  "data": {
    "title": "welcome",
    "description": "",
    "keywords": "",
    "content_id": 142,
    "name": "welcome",
    "data": "welcome:lorem ipsum lorem ipsum lorem ipsum\n\t  \t\t lorem ipsum\n\t    \tlorem ipsum\n\t     lorem ipsum lorem ipsum lorem ipsum\n\t      lorem ipsum lorem ipsum lorem ipsum lorem ipsum",
    "type": "data",
    "tiny_enabled": 0,
    "page_name": "",
    "video": "",
    "updated_at": "-0001-11-30 00:00:00",
    "created_at": "-0001-11-30 00:00:00"
  },
  "success": true,
  "msg": "Content page fetched sucessfully"
}
	 * 
	 */

	
	/** Outer data of response. */
	private Data data;
	
	protected static class Data {
		/** inner data of response. */
		String data; 
		String title;
		@Override
		public String toString() {
			return super.toString() + "[title: "+ title + ", data:" + data + "]";
		}
	}
	
	@Override
	public String getContent() {
		if (data == null) {
			return "";
		}
		return data.data;
	}
	
	@Override
	public String getTitle(){
		if (data == null) {
			return "";
		}
		return data.title;
	}
	
	
	@Override
	public String toString() {
		return super.toString() + "[content:" + data.data + ", title: "+ data.title+"]";
	}

	
	public static class ContentDeserializer implements JsonDeserializer<ContentResponse> {
		@Override
		public ContentResponse deserialize(JsonElement je, Type type,
				JsonDeserializationContext jdc) throws JsonParseException {
			JsonElement data = je.getAsJsonObject().get("data");
			
			//using new instance to avoid infinite recursion
			return new Gson().fromJson(data, ContentResponse.class);
		}
	}

	
}
