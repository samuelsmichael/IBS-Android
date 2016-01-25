package com.inductivebiblestudyapp.data.model;

import java.lang.reflect.Type;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * 
 * @author Jason Jenkins
 * @version 0.1.1-20150723
 */
public class StudyNotesResponse implements Parcelable {
	
	public StudyNotesResponse() {}
	
	/*
	 *
	 * Sample output:
	 * 
{
  ...
  
  "notes": [
    {
      "study_note_id": 17,
      "verse_id": "eng-MSG:Exod.2.4",
      "member_id": 112,
      "text": "note 2 change",
      "parent_verse": {
        "id": "eng-MSG:Exod.2.4",
        "verse": 4,
        "parent_chapter_id": "eng-MSG:Exod.2",
        "parent_chapter": {
          "id": "eng-MSG:Exod.2",
          "chapter": "2",
          "parent_book_id": "eng-MSG:Exod",
          "parent_book": {
            "book_id": "eng-MSG:Exod",
            "name": "Exodus"
          }
        }
      }
    },
    ...
    ],
    
    "themes": [
    {
      "id": 2,
      "chapter_id": "eng-MSG:Exod.1",
      "member_id": 112,
      "name": "chapter b",
      "parent_chapter": {
        "id": "eng-MSG:Exod.1",
        "chapter": "1",
        "parent_book_id": "eng-MSG:Exod",
        "parent_book": {
          "book_id": "eng-MSG:Exod",
          "name": "Exodus"
        }
      }
    },
    ...
    ],
    
      "divisionthemes": [
    {
      "id": 1,
      "verse_id": "eng-AMP:Exod.2.1",
      "member_id": 112,
      "name": "Test",
      "parent_verse": {
        "id": "eng-AMP:Exod.2.1",
        "verse": 1,
        "parent_chapter_id": "eng-AMP:Exod.2",
        "parent_chapter": {
          "id": "eng-AMP:Exod.2",
          "chapter": "2",
          "parent_book_id": "eng-AMP:Exod",
          "parent_book": {
            "book_id": "eng-AMP:Exod",
            "name": "Exodus"
          }
        }
      }
    },
    ...
    ],
    
    "markings": [
    {
      "id": 1,
      "type": "lettering",
      "start": "473",
      "end": "664",
      "object_id": 19,
      "member_id": 112,
      "chapter_id": "eng-KJV:Exod.2",
      "verse_id": "0",
      "verse_range": "",
      "created_at": "2015-07-21 08:51:16",
      "parent_chapter": {
        "id": "eng-KJV:Exod.2",
        "chapter": "2",
        "parent_book_id": "eng-KJV:Exod",
        "parent_book": {
          "book_id": "eng-KJV:Exod",
          "name": "Exodus"
        }
      }
    },
    {
      "id": 2,
      "type": "image",
      "start": "667",
      "end": "820",
      "object_id": 21,
      "member_id": 112,
      "chapter_id": "eng-KJV:Exod.2",
      "verse_id": "0",
      "verse_range": "",
      "created_at": "2015-07-21 08:51:16",
      "parent_chapter": {
        "id": "eng-KJV:Exod.2",
        "chapter": "2",
        "parent_book_id": "eng-KJV:Exod",
        "parent_book": {
          "book_id": "eng-KJV:Exod",
          "name": "Exodus"
        }
      }
    },
    ...
    ]
}
	 * 
	 */
	
	//REMEMBER TO ADD TO PARCELABLE
	
	/*default*/ StudyNoteItem[] items = new StudyNoteItem[0];
	
	
	public StudyNoteItem[] getStudyNotes() {
		return items;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.2.0-20150727 */
	public static class StudyNoteItem {
		/** Default value, only given if unset. */
		public static final int TYPE_UNKNOWN = -1;
		
		public static final int TYPE_CHAPTER_THEME = 0;
		public static final int TYPE_NOTE = 1;
		public static final int TYPE_DIV_THEME = 2;
		public static final int TYPE_MARKING_IMAGE = 3;
		public static final int TYPE_MARKING_LETTERING = 4;
		
		
		String itemId;
		int itemType = TYPE_UNKNOWN;
		
		String parentBookId;
		String parentBookName;
		
		String parentChapterId;
		String parentChapterNumber;
		
		String verseRange = "";
		
		@Override
		public String toString() {
			return super.toString() +
					"[itemId: " + itemId + ", type: + " + itemType + " -> " + 
					parentBookName + " " + parentChapterNumber + ":" + verseRange + 
					"]";
		}
		
		/** @return The id of the underlying item. */
		public String getItemId() {
			return itemId;
		}
		
		/** @return Representation of type, see {@link StudyNoteItem}
		 * constants for possible options. 
		 */
		public int getItemType() {
			return itemType;
		}
		
		public String getParentBookName() {
			return parentBookName;
		}
		public String getParentChapterNumber() {
			return parentChapterNumber;
		}
		
		public String getParentChapterId() {
			return parentChapterId;
		}
		
		/** @return The verse range or an empty string. */
		public String getVerseRange() {
			if (verseRange == null) {
				return "";
			}
			return verseRange;
		}
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Serializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.1.0-20150722 */
	public static class StudyNotesDeserializer implements JsonDeserializer<StudyNotesResponse> {
		@Override
		public StudyNotesResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
			throws JsonParseException {
				final JsonObject jsonObject = json.getAsJsonObject();	
				
				ArrayList<StudyNoteItem> noteList = new ArrayList<StudyNoteItem>();
				
				parseNotes(jsonObject, noteList);			
				parseDivThemes(jsonObject, noteList);			
				parseChapterThemes(jsonObject, noteList);	
				parseMarkings(jsonObject, noteList);
				
				final StudyNotesResponse response = new StudyNotesResponse();
				response.items = new StudyNoteItem[noteList.size()]; 
				noteList.toArray(response.items);
				
				return response;
			}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Utility methods
		////////////////////////////////////////////////////////////////////////////////////////////////
	
		
		private static void parseNotes(final JsonObject jsonObject,
				ArrayList<StudyNoteItem> noteList) {
			JsonElement notes = jsonObject.get("notes");
			if (notes.isJsonArray()) {
				JsonArray array = notes.getAsJsonArray();
				final int SIZE = array.size();
				
				for (int index = 0; index < SIZE; index++) {
					JsonObject root = array.get(index).getAsJsonObject();
					StudyNoteItem noteItem = new StudyNoteItem();
					
					noteItem.itemType = StudyNoteItem.TYPE_NOTE;					
					noteItem.itemId = root.get("study_note_id").getAsString();					
					parseParentVerse(root, noteItem);
					
					noteList.add(noteItem);
				}				
			}
		}
		
	
		private static void parseDivThemes(final JsonObject jsonObject,
				ArrayList<StudyNoteItem> noteList) {
			JsonElement divThemes = jsonObject.get("divisionthemes");
			if (divThemes.isJsonArray()) {
				JsonArray array = divThemes.getAsJsonArray();
				final int SIZE = array.size();
				
				for (int index = 0; index < SIZE; index++) {
					JsonObject root = array.get(index).getAsJsonObject();
					StudyNoteItem noteItem = new StudyNoteItem();
					
					noteItem.itemType = StudyNoteItem.TYPE_DIV_THEME;					
					noteItem.itemId = root.get("id").getAsString();					
					parseParentVerse(root, noteItem);
					
					noteList.add(noteItem);
				}
			}
		}
		
		private static void parseChapterThemes(final JsonObject jsonObject,
				ArrayList<StudyNoteItem> noteList) {
			JsonElement themes = jsonObject.get("themes");
			if (themes.isJsonArray()) {
				JsonArray array = themes.getAsJsonArray();
				final int SIZE = array.size();
				
				for (int index = 0; index < SIZE; index++) {
					JsonObject root = array.get(index).getAsJsonObject();
					StudyNoteItem noteItem = new StudyNoteItem();
					
					noteItem.itemType = StudyNoteItem.TYPE_CHAPTER_THEME;					
					noteItem.itemId = root.get("id").getAsString();					
					parseParentChapter(root, noteItem);
					
					noteList.add(noteItem);
				}
			}
		}
		
		private static void parseMarkings(final JsonObject jsonObject,
				ArrayList<StudyNoteItem> noteList) {
			JsonElement markings = jsonObject.get("markings");
			if (markings.isJsonArray()) {
				JsonArray array = markings.getAsJsonArray();
				final int SIZE = array.size();
				
				for (int index = 0; index < SIZE; index++) {
					JsonObject root = array.get(index).getAsJsonObject();
					StudyNoteItem noteItem = new StudyNoteItem();
					
					final String type = root.get("type").getAsString();
					if (type.equals("lettering")) {
						noteItem.itemType = StudyNoteItem.TYPE_MARKING_LETTERING;
					} else if (type.equals("image")) {
						noteItem.itemType = StudyNoteItem.TYPE_MARKING_IMAGE;
					} else {
						Log.w(	StudyNotesDeserializer.class.getSimpleName(), 
								"Unexpected type \"" + type + "\"");
						noteItem.itemType = StudyNoteItem.TYPE_UNKNOWN;
					}
										
					noteItem.itemId = root.get("id").getAsString();
					noteItem.verseRange = root.get("verse_range").getAsString();
					parseParentChapter(root, noteItem);
					
					noteList.add(noteItem);
				}
			}
		}



		/** Parses the sub-element "parent_verse" 
		 * @param root The root of json containing "parent_verse"
		 * @param noteItem The return item
		 */
		private static void parseParentVerse(JsonObject root, StudyNoteItem noteItem) {
			JsonObject verse = root.get("parent_verse").getAsJsonObject();
			noteItem.verseRange = verse.get("verse").getAsString();
			
			parseParentChapter(verse, noteItem);
		}

		/** Parses the sub-element "parent_chapter" 
		 * @param root The root json containing "parent_chapter"
		 * @param noteItem The return item
		 */
		private static void parseParentChapter(JsonObject root, StudyNoteItem noteItem) {
			JsonObject chapter = root.get("parent_chapter").getAsJsonObject();
			noteItem.parentChapterId = chapter.get("id").getAsString();
			noteItem.parentChapterNumber = chapter.get("chapter").getAsString();
			
			JsonObject book = chapter.get("parent_book").getAsJsonObject();
			noteItem.parentBookId = book.get("book_id").getAsString();
			noteItem.parentBookName = book.get("name").getAsString();
		}
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Parcelable.Creator<StudyNotesResponse> CREATOR = new Parcelable.Creator<StudyNotesResponse>() {
	    public StudyNotesResponse createFromParcel(Parcel in) {
	        return new StudyNotesResponse(in);
	    }
	
	    public StudyNotesResponse[] newArray(int size) {
	        return new StudyNotesResponse[size];
	    }
	};
	
	public StudyNotesResponse(Parcel src) {
		String[] values = new String[0];
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
