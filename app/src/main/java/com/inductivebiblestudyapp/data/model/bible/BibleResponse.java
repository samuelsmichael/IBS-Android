package com.inductivebiblestudyapp.data.model.bible;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
* The Bible list books data.
* @author Jason Jenkins
* @version 0.2.0-20150622
*/
public class BibleResponse {
	final static private String LOGTAG = BibleResponse.class
			.getSimpleName();
	
	private static final String TESTAMENT_OLD = "OT";
	private static final String TESTAMENT_NEW = "NT";
	
	/*
	 *
	 * Sample output:
	 * 
[
  {
    "book_id": "eng-AMP:Gen",
    "version_id": "401",
    "name": "Genesis",
    "abbr": "Gen",
    "ord": "1",
    "book_group_id": "0",
    "testament": "NT",
    "id": "eng-AMP:Gen",
    "osis_end": "eng-AMP:Gen.50.26",
    "parent_version_name": "Amplified Bible",
    "parent_version_path": "\/versions\/eng-AMP",
    "parent_version_id": "eng-AMP",
    "next_book_id": "eng-AMP:Exod",
    "next_book_id_path": "\/books\/eng-AMP:Exod",
    "next_book_id_name": "Exodus",
    "next_book_id_id": "eng-AMP:Exod",
    "completed": 1
  },
  ...
  ]
	 * 
	 */
	
	private List<Book> oldTestament = new ArrayList<BibleResponse.Book>();
	private List<Book> newTestament = new ArrayList<BibleResponse.Book>();
	
	public List<Book> getOldTestament() {
		return oldTestament;
	}
	
	public List<Book> getNewTestament() {
		return newTestament;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class Section {
		String name;
		List<Book> books = new ArrayList<BibleResponse.Book>();
	}
	
	/** @version 0.2.0-20150806 */
	public static class Book implements IBibleSearchItem {
		@SerializedName("book_id")
		String bookId;
		String name;
		String testament;
		
		public String getBookId() {
			return bookId;
		}
		
		public String getName() {
			return name;
		}
		
		public String getTestament() {
			return testament;
		}
		
		@Override
		public String getSearchResultName() {
			return name;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Serializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class BibleDeserializer implements JsonDeserializer<BibleResponse> {
	  @Override
	  public BibleResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
	      throws JsonParseException {
		  final JsonArray jsonArray = json.getAsJsonArray();
	
		  final BibleResponse response = new BibleResponse();
		  
		  final int SIZE = jsonArray.size();
		  for (int index = 0; index < SIZE; index++) {
			  Book book = context.deserialize(jsonArray.get(index), Book.class);
			  
			  if (TESTAMENT_OLD.equalsIgnoreCase(book.testament)) {
				  response.oldTestament.add(book);
			  } else if (TESTAMENT_NEW.equalsIgnoreCase(book.testament)) {
				  response.newTestament.add(book);
			  } else {
				  Log.w(LOGTAG, "Unknown testament '" + book.testament + "' at index " + index);
			  }
		  }	 
		  
		  return response;
	  }
	}
	
}