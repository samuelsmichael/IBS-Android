package com.inductivebiblestudyapp.data.model;

import java.lang.reflect.Type;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author Jason Jenkins
 * @version 0.2.0-20150727
 */
public class MarkingItem implements Parcelable {

	//REMEMBER TO ADD TO PARCELABLE
	
	/*
	 *
	 * Sample output 1:
	 * 
{
    "id": 1,
    "type": "lettering",
    "start": "473",
    "end": "664",
    "object_id": 19,
    "member_id": 112,
    "chapter_id": "eng-KJV:Exod.2",
    "verse_id": "0",
    "verse_range": "1",
    "marking_value": {
      "id": 19,
      "bold": 0,
      "italics": 1,
      "underline": 0,
      "strikethrough": 0,
      "double_underline": 0,
      "boxed": 0,
      "double_boxed": 0,
      "font_color": "#ff2fffff",
      "background_color": "#ff3b7500",
      "member_id": 112,
      "name": "green and blue"
    }
  },
   *
   * Sample output 2:
   * 
  {
    "id": 2,
    "type": "image",
    "start": "667",
    "end": "820",
    "object_id": 21,
    "member_id": 112,
    "chapter_id": "eng-KJV:Exod.2",
    "verse_id": "0",
    "verse_range": "2-5",
    "marking_value": {
      "id": 21,
      "name": " bug 4",
      "member_id": 112,
      "path": "uploads\/images\/55a7a7311cd53.jpeg"
    }
  },
	 * 
	 */
	public static final int TYPE_LETTERING = 0;
	public static final int TYPE_IMAGE = 1;
	
	public MarkingItem() {}
	
	String id;	
	@SerializedName("object_id")
	String objectId;
	
	int markingType = -1;
	
	@SerializedName("start")
	int startIndex;
	@SerializedName("end")
	int endIndex;
	
	@SerializedName("chapter_id")
	String chapterId;
	@SerializedName("verse_range")
	String verseRange = "";
	
	ImageItem imageItem;
	LetteringItem letteringItem;
		
	public String getId() {
		return id;
	}
	
	public String getChapterId() {
		return chapterId;
	}
	
	public int getEndIndex() {
		return endIndex;
	}
	
	public int getStartIndex() {
		return startIndex;
	}
	
	/** @return Either {@link #TYPE_IMAGE} or {@link #TYPE_LETTERING}.
	 * -1 if unset. */
	public int getMarkingType() {
		return markingType;
	}
	
	/** @return The verse range or empty if not set. */
	public String getVerseRange() {
		if (verseRange == null) {
			verseRange = "";
		}
		return verseRange;
	}
	
	/** @return The image item for this marking or <code>null</code>
	 * if not {@link #TYPE_IMAGE}
	 * @see #getMarkingType()
	 */
	public ImageItem getImageItem() {
		return imageItem;
	}
	
	/** @return The lettering item for this marking or <code>null</code>
	 * if not {@link #TYPE_LETTERING}
	 * @see #getMarkingType()
	 */
	public LetteringItem getLetteringItem() {
		return letteringItem;
	}
	
	@Override
	public String toString() {
		return super.toString() + 
				"[id: " + id + ", start: \"" + startIndex +" , end: \"" + endIndex + 
				" , markingType: \"" + (TYPE_IMAGE == markingType ? "image" : "lettering" ) + "\"]";
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Deserializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * See {@link MarkingItem} for sample output.
	 * @author Jason Jenkins
	 * @version 0.1.0-20150721
	 */	
	public static class MarkingItemDeserializer implements JsonDeserializer<MarkingItem> {
		
		private static final String TYPE_LETTERING = "lettering";
		private static final String TYPE_IMAGE = "image";
		
		  @Override
		  public MarkingItem deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
		      throws JsonParseException {
			  final JsonObject jsonObject = json.getAsJsonObject();		  
			  
			  String type = jsonObject.get("type").getAsString();
			  MarkingItem item = null;
			  
			  final String MARKING_VALUE = "marking_value";
			  if (TYPE_LETTERING.equals(type)) {
				  item = new MarkingItem();
				  item.markingType = MarkingItem.TYPE_LETTERING;
				  item.letteringItem = 
						  context.deserialize(jsonObject.get(MARKING_VALUE), LetteringItem.class);
				  
			  } else if (TYPE_IMAGE.equals(type)) {
				  item = new MarkingItem();
				  item.markingType = MarkingItem.TYPE_IMAGE;
				  item.imageItem = 
						  context.deserialize(jsonObject.get(MARKING_VALUE), ImageItem.class);
				  
			  } else {
				  throw new UnsupportedOperationException("Found unknown marking type '"+ type + "'");
			  }
			  
			  item.id = jsonObject.get("id").getAsString();
			  item.objectId = jsonObject.get("object_id").getAsString();
			  
			  item.startIndex = jsonObject.get("start").getAsInt();
			  item.endIndex = jsonObject.get("end").getAsInt();
			  
			  item.chapterId = jsonObject.get("chapter_id").getAsString(); 
			  item.verseRange = jsonObject.get("verse_range").getAsString();
			  
			   return item;
		  }
		}

	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final Parcelable.Creator<MarkingItem> CREATOR = new Parcelable.Creator<MarkingItem>() {	    
		public MarkingItem createFromParcel(Parcel in) {
	        return new MarkingItem(in);
	    }
	
	    public MarkingItem[] newArray(int size) {
	        return new MarkingItem[size];
	    }
	};
	
	public MarkingItem(Parcel src) {
		String[] values = new String[4];
		src.readStringArray(values);
		id = values[0];
		objectId = values[1];
		chapterId = values[2];
		verseRange = values[3];
		
		int[] intValues = new int[3];
		src.readIntArray(intValues);
		
		markingType = intValues[0];
		startIndex = intValues[1];
		endIndex = intValues[2];
		
		imageItem = src.readParcelable(ImageItem.class.getClassLoader());
		letteringItem = src.readParcelable(LetteringItem.class.getClassLoader());				
	}
	

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String[] values = new String[]{id, objectId, chapterId, verseRange};		
		dest.writeStringArray(values);		
		
		int[] intValues = new int[]{markingType, startIndex, endIndex};
		dest.writeIntArray(intValues);
		
		dest.writeParcelable((Parcelable) imageItem, flags);
		dest.writeParcelable((Parcelable) letteringItem, flags);
	}

	
	@Override
	public int describeContents() {
		return 0;
	}
	
}
