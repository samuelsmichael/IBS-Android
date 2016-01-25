package com.inductivebiblestudyapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * 
 * @author Jason Jenkins
 * @version 0.1.0-20150731
 */
public class EmailConfirmResponse implements IContentResponse, Parcelable {	
	
	/*
	 * Sample output:
	 * 
	 * {
  "data": "",
  "success": true,
  "msg": {
    "title": "share_with_email",
    "description": "",
    "keywords": "",
    "content_id": 218,
    "name": "share_with_email",
    "data": "share_with_email:lorem ipsum lorem ipsum lorem ipsum\n\t  \t\t lorem ipsum\n\t    \tlorem ipsum\n\t     lorem ipsum lorem ipsum lorem ipsum\n\t      lorem ipsum lorem ipsum lorem ipsum lorem ipsum",
    "type": "data",
    "tiny_enabled": 0,
    "page_name": "",
    "video": "",
    "updated_at": "-0001-11-30 00:00:00",
    "created_at": "-0001-11-30 00:00:00"
  }
}
	 * 
	 */
	
	public EmailConfirmResponse() {}

	boolean success = true;	
	Message msg;
	String data;
	
	protected static class Message {
		/** inner data of response. */
		String data; 
		String title;
		@Override
		public String toString() {
			return super.toString() + "[title: "+ title + ", data:" + data + "]";
		}
	}
	

	/** @return <code>true</code> if successful, <code>false</code> otherwise. */
	public boolean isSuccessful() {
		return success;
	}
	
	/** @return Message content, blank if not found */
	@Override
	public String getContent() {
		if (msg == null) {
			return "";
		}
		return msg.data;
	}
	
	/** @return Message title, blank if not found */
	@Override
	public String getTitle(){
		if (msg == null) {
			return "";
		}
		return msg.title;
	}
	
	
	@Override
	public String toString() {
		return super.toString() + 
				"[success: ["+success+"], + message:" + msg +"]";
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Parcellable starts here
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final Parcelable.Creator<EmailConfirmResponse> CREATOR = new Parcelable.Creator<EmailConfirmResponse>() {
	    public EmailConfirmResponse createFromParcel(Parcel in) {
	        return new EmailConfirmResponse(in);
	    }
	
	    public EmailConfirmResponse[] newArray(int size) {
	        return new EmailConfirmResponse[size];
	    }
	};

	public EmailConfirmResponse(Parcel src) {
		success = src.readInt() != 0;
		
		msg = new Message();
		msg.title = src.readString();
		msg.data = src.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(success ? 1 : 0);
		dest.writeString(msg == null ? "" : msg.title);
		dest.writeString(msg == null ? "" : msg.data);		
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	
}
