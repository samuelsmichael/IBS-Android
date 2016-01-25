package com.inductivebiblestudyapp.data.model.bible;

import java.lang.reflect.Type;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.inductivebiblestudyapp.data.model.ChapterTheme;

/**
* The Bible list chapter data.
* @author Jason Jenkins
* @version 0.2.4-20150803
*/
public class BibleChapterResponse {
	
	/*
	 *
	 * Sample output:
	 * 
[
  {
    "id": "eng-NASB:Rev.1",
    "auditid": "0",
    "label": "",
    "chapter": "1",
    "osis_end": "eng-NASB:Rev.1.20",
    "parent_book_id": "eng-NASB:Rev",
    "parent_book_path": "\/books\/eng-NASB:Rev",
    "parent_book_name": "Revelation",
    "next_chapter_id": "eng-NASB:Rev.2",
    "next_chapter_path": "\/chapters\/eng-NASB:Rev.2",
    "next_chapter_name": "Revelation 2",
    "previous_chapter_id": "eng-NASB:Jude.1",
    "previous_chapter_path": "\/chapters\/eng-NASB:Jude.1",
    "previous_chapter_name": "Jude 1",
    "copyright": "\n      New American Standard Bible, Copyright  1960,1962,1963,1968,1971,1972,1973,1975,1977,1995 by The Lockman Foundation.  Used by permission.\n    ",
    "completed": 1,
    "theme": null
  },
  {
    "id": "eng-NASB:Rev.2",
...
    "theme": { ... } //see ChapterTheme
  },
  ...
  ]
	 * 
	 */
	
	
	Chapter[] chapters = new Chapter[0];
	
	public Chapter[] getChapters() {
		return chapters;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** @version 0.6.0-20150806 */
	public static class Chapter implements Parcelable, IBibleSearchItem {
		
		public Chapter() {}
		
		String copyright;
		
		@SerializedName("id")
		String chapterId;
		@SerializedName("chapter")
		String number;
		
		@SerializedName("parent_book_name")
		String parentBook;
		@SerializedName("parent_book_id")
		String parentBookId;
		
		@SerializedName("previous_chapter_name")
		String previousChapterName;
		@SerializedName("previous_chapter_id")
		String previousChapterId;
		
		@SerializedName("next_chapter_name")
		String nextChapterName;
		@SerializedName("next_chapter_id")
		String nextChapterId;
		
		ChapterTheme theme;
		
		@SerializedName("parent_book")
		Book book;
		
		public String getChapterId() {
			return chapterId;
		}
		
		/** @return The abbreviated book name . */
		public String getParentBookShort() {
			if (book != null) {
				return book.abbr;
			}
			return "";
		}
		
		public String getCopyright() {
			return copyright;
		}
		
		/** @return The parent book and number, separated by a space. */
		public String getName() {
			return parentBook + " " + number;
		}
		
		public String getNumber() {
			return number;
		}
				
		public String getParentBookId() {
			return parentBookId;
		}
		
		public String getParentBook() {
			return parentBook;
		}
		
		/** @return The next chapter's name or empty if not found (e.g.
		 * no next chapter.)
		 */
		public String getNextChapterName() {
			if (nextChapterName == null) {
				return "";
			}
			return nextChapterName;
		}
		
		/** @return The next chapter's id or empty if not found (e.g.
		 * no next chapter.)
		 */
		public String getNextChapterId() {
			if (nextChapterId == null) {
				return "";
			}
			return nextChapterId;
		}
		
		/** @return The previous chapter's name or empty if not found (e.g.
		 * no previous chapter.)
		 */
		public String getPreviousChapterName() {
			if (previousChapterName == null) {
				return "";
			}
			return previousChapterName;
		}
		
		/** @return The previous chapter's id or empty if not found (e.g.
		 * no previous chapter.)
		 */
		public String getPreviousChapterId() {
			if (previousChapterId == null) {
				return "";
			}
			return previousChapterId;
		}
		
		/** @return The chapter theme, if set or <code>null</code>. */
		public ChapterTheme getChapterTheme() {
			return theme;
		}
		
		@Override
		public String getSearchResultName() {
			return getName();
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Internal class
		////////////////////////////////////////////////////////////////////////////////////////////////
		/** Class for the sole purpose of fetching the book abbreviation. */
		private static class Book {
			String abbr;
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Start parcelable
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		public static final Parcelable.Creator<Chapter> CREATOR = new Parcelable.Creator<Chapter>() {	    
			public Chapter createFromParcel(Parcel in) {
		        return new Chapter(in);
		    }
		
		    public Chapter[] newArray(int size) {
		        return new Chapter[size];
		    }
		};
		
		public Chapter(Parcel src) {
			String[] values = new String[10];
			src.readStringArray(values);
			
			chapterId = values[0];
			number = values[1];
			
			book = new Book();			
			book.abbr = values[2];
			
			parentBook = values[3];
			parentBookId = values[4];
			
			previousChapterName = values[5];
			previousChapterId = values[6];
			
			nextChapterName = values[7];
			nextChapterId = values[8];
			
			copyright = values[9];				
			
			theme = src.readParcelable(ChapterTheme.class.getClassLoader());				
		}
		

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			String[] values = new String[]{
				chapterId,
				number,
				
				book != null ? book.abbr : "",
				parentBook,
				parentBookId,
				
				previousChapterName,
				previousChapterId,
				
				nextChapterName,
				nextChapterId,
				
				copyright
			};
			dest.writeStringArray(values);		
			
			
			dest.writeParcelable((Parcelable) theme, flags);
		}

		
		@Override
		public int describeContents() {
			return 0;
		}		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Serializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class BibleChapterDeserializer implements JsonDeserializer<BibleChapterResponse> {
	  @Override
	  public BibleChapterResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
	      throws JsonParseException {
		  final JsonArray jsonArray = json.getAsJsonArray();
	
		  final BibleChapterResponse response = new BibleChapterResponse();
		  
		  response.chapters = context.deserialize(jsonArray, Chapter[].class);		  
		  
		  return response;
	  }
	}
}