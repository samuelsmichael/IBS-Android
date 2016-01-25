package com.inductivebiblestudyapp.data.model;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/** @version 0.3.0-20150715
 */
public class UpdateResult {
	
	/*
	 * Output 1:
{
  "data": "",
  "success": true,
  "msg": "Lettering created"
}
	 * 
	 * 
	 * Output 2:
{
  "data": {
    "insert_id": 14
  },
  "success": true,
  "msg": "Lettering created"
}
	 */
	boolean success = true;
	@SerializedName("insert_id")
	String insertId = null;
	
	String msg; 
	
	/** @return The insert id if found, or <code>null</code> if not. */
	public String getInsertId() {
		return insertId;
	}
	
	/** @return The message, if any, or <code>null</code> */
	public String getMessage() {
		return msg;
	}
	
	public boolean isSuccessful() {
		return success;
	}
	
	public static class UpdateResultDeserializer implements JsonDeserializer<UpdateResult> {
		@Override
		public UpdateResult deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			JsonObject content = json.getAsJsonObject();
			UpdateResult response = new Gson().fromJson(json, UpdateResult.class);			
			JsonElement data = content.get("data");
			
			if (data.isJsonObject()) { //data is not always supplied as object/string
				JsonElement id = data.getAsJsonObject().get("insert_id");
				if (id != null && !id.isJsonNull()) {
					response.insertId = id.getAsString();
				}
			} 
			return response;
		}
	}
}
