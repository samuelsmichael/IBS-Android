package com.inductivebiblestudyapp.data.model.bible.wordstudy;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/** @version 0.1.0-20150824 */
public class CrossReferenceResponse {
	
	/*
	 * Sample output:
	 * [
  {
    "kjv_translation_id": 11,
    "parent_book_name": "Ezra",
    "parent_book_abbr": "Ezra",
    "chapter_number": 4,
    "verse_number": 15,
    "title": "fathers",
    "strongs_concordance_id": 2,
    "number_of_occurrences": 3,
    "verse": "<p class=\"p\"><sup id=\"Ezra.4.15\" class=\"v\">15<\/sup>that search may be made in the book of the records of thy fathers: so shalt thou find in the book of the records, and know that this city <span class=\"add\">is<\/span> a rebellious city, and hurtful unto kings and provinces, and that they have moved sedition within the same of old time: for which cause was this city destroyed.<\/p>",
    "chapter_id": "eng-KJV:Ezra.4",
    "verse_id": "eng-KJV:Ezra.4.15"
  },
  {
    "kjv_translation_id": 11,
    "parent_book_name": "Ezra",
    "parent_book_abbr": "Ezra",
    "chapter_number": 5,
    "verse_number": 12,
    "title": "fathers",
  ...
  },
...
]
	 */
	
	CrossReference[] crossReferences = new CrossReference[0];
	
	public CrossReference[] getCrossReferences() {
		return crossReferences;
	}
	
	public static class CrossReferenceDeserializer implements JsonDeserializer<CrossReferenceResponse> {
		  @Override
		  public CrossReferenceResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
		      throws JsonParseException {
			  final JsonArray jsonArray = json.getAsJsonArray();
		
			  final CrossReferenceResponse response = new CrossReferenceResponse();
			  
			  response.crossReferences = context.deserialize(jsonArray, CrossReference[].class);		  
			  
			  return response;
		  }
		}
}
