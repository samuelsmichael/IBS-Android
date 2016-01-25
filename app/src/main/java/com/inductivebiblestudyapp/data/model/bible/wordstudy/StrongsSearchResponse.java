package com.inductivebiblestudyapp.data.model.bible.wordstudy;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
* The response for strong's definition.
* @author Jason Jenkins
* @version 0.1.0-20150818
*/
public class StrongsSearchResponse {

	
	/*
	 *
	 * Sample output:
	 * 
[
    {
        "strongs_concordance_id": 1,
        "strongs_number": 1,
        "text": "beginning",
        "num_of_kjv_occurences": 1214,
        "parent_book_name": "Genesis",
        "parent_book_abbr": "Gen", //see api/bible response
        "parent_chapter_number": 1,
        "verse_number": 1
        
    },

    ...
]

	 * 
	 */
	

	StrongSearchResult[] results = new StrongSearchResult[0];
	
	public StrongSearchResult[] getResults() {
		return results;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal clases
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.1.0-20150818 */
	public static class StrongSearchResult {
		@SerializedName("strongs_concordance_id")
		String strongsId;
		@SerializedName("strongs_number")
		String strongsNumber;	
		
		String text;
		
		@SerializedName("parent_book_name")
		String parentBookName;
		@SerializedName("parent_book_abbr")
		String parentBookAbbr;
		@SerializedName("parent_chapter_number")
		String parentChapterNumber;
		@SerializedName("verse_number")
		String verseNumber;
		
		public String getStrongsNumber() {
			return strongsNumber;
		}
		public String getStrongsId() {
			return strongsId;
		}
		
		public String getParentBookName() {
			return parentBookName;
		}
		public String getParentBookShort() {
			return parentBookAbbr;
		}
		
		public String getParentChapterNumber() {
			return parentChapterNumber;
		}
		
		public String getVerseNumber() {
			return verseNumber;
		}
		
		public String getText() {
			return text;
		}
		
		@Override
		public String toString() {
			return super.toString() + "[" +
					"strong's: " + strongsNumber +
					". " + text +
					" --> " +parentBookName + " " + 
					parentChapterNumber + ":" + verseNumber +
					"]";
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Deserializers
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static class StrongsSearchDeserializer implements JsonDeserializer<StrongsSearchResponse> {
		  @Override
		  public StrongsSearchResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
		      throws JsonParseException {
			  final JsonArray jsonArray = json.getAsJsonArray();
				
			  final StrongsSearchResponse response = new StrongsSearchResponse();
			  
			  response.results = context.deserialize(jsonArray, StrongSearchResult[].class);		  
			  
			  return response;
		  }
	}
	
	
}