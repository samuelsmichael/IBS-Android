package com.inductivebiblestudyapp.data.model.bible;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse.Book;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse.Verse;

/**
* The Bible search response data.
* @author Jason Jenkins
* @version 0.3.0-20150831
*/
public class BibleSearchResponse {
	
	/*
	 *
	 * Sample output:
	 * 
{
  "books": [
    {
      "book_id": "eng-KJV:Num",
      "name": "Numbers",
      "abbr": "Num",
      "version_id": "431"
    },
 ...
  ],
  "verses": [
    {
      "id": "eng-KJV:Num.23.1",
      "auditid": "0",
      "book": 4,
      "chapter": 23,
      "verse": 1,
      "lastverse": "1",
      "osis_end": "eng-KJV:Num.23.1",
      "label": "",
	...
    },
    ....
  ],
  "chapters": [
    {
      "id": "eng-KJV:Num.23",
      "chapter": 23,
      "parent_book_id": "eng-KJV:Num",
      "parent_book": {
        "book_id": "eng-KJV:Num",
        "name": "Numbers",
        "abbr": "Num",
        "version_id": "431"
      }
    },
    ...
  ]
}
	 * 
	 */
	
	//List<Book> books = new ArrayList<BibleResponse.Book>();
	//List<Chapter> chapters = new ArrayList<BibleChapterResponse.Chapter>();
	List<Verse> verses = new ArrayList<BibleVerseResponse.Verse>();
	
	@Deprecated
	public List<Book> getBooks() {
		return new ArrayList<BibleResponse.Book>();
	}
	
	@Deprecated
	public List<Chapter> getChapters() {
		return new ArrayList<BibleChapterResponse.Chapter>();
	}
	
	public List<Verse> getVerses() {
		if (verses == null) {
			verses = new ArrayList<BibleVerseResponse.Verse>();
		}
		return verses;
	}		
	
	@Override
	public String toString() {
		return super.toString() + "[" +
				"verses: " + (verses == null ? null : verses.size()) +
				"]";
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Deserializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class BibleSearchDeserializer implements JsonDeserializer<BibleSearchResponse> {
		  @Override
		  public BibleSearchResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
		      throws JsonParseException {
			  final JsonObject jsonObject = json.getAsJsonObject();
		
			  final BibleSearchResponse response = new BibleSearchResponse();
			  			 
			  JsonArray verses = jsonObject.get("verses").getAsJsonArray();
			  for (JsonElement verse : verses) {
				  response.verses.add((Verse) context.deserialize(verse.getAsJsonObject(), Verse.class));
			  } 
			  
			  return response;
		  }
	}
	
}