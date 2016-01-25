package com.inductivebiblestudyapp.data.model.bible;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.inductivebiblestudyapp.data.model.ChapterTheme;
import com.inductivebiblestudyapp.data.model.DivisionTheme;
import com.inductivebiblestudyapp.data.model.MarkingItem;
import com.inductivebiblestudyapp.data.model.Note;

/**
* The Bible list verse data.
* @author Jason Jenkins
* @version 0.4.4-20150827
*/
public class BibleVerseResponse {
	
	/*
	 *
	 * Sample output:
	 * 
	 * 
{
  "theme": [
    
  ],
  "markings": [
    {
      "id": 362,
      "type": "lettering",
      "start": "0",
      "end": "71",
      "object_id": 47,
      "member_id": 112,
      "chapter_id": "eng-KJV:Matt.3",
      "verse_range": "1",
      "created_at": "2015-08-14 08:57:50",
      "marking_value": {
        "id": 47,
        "bold": 0,
        "italics": 0,
        "underline": 0,
        "strikethrough": 1,
        "double_underline": 0,
        "boxed": 0,
        "double_boxed": 0,
        "font_color": "#ff00beff",
        "background_color": "#ff007718",
        "member_id": 112,
        "name": "long long way to Bah Sing Sae",
        "last_used": "2015-08-24 19:25",
        "user_type": 2
      }
    }
  ],
  "data": [
    {
      "id": "eng-KJV:Matt.3.1",
      "auditid": "0",
      "book": 40,
      "chapter": 3,
      "verse": 1,
      "lastverse": "1",
      "osis_end": "eng-KJV:Matt.3.1",
      "label": "",
      "reference": "Matthew 3:1",
      "text": "<h3 class=\"s1\">His Baptism and Temptation<\/h3>\n<h4 class=\"s2\">John the Baptist<\/h4>\n<p class=\"p\"><sup id=\"Matt.3.1\" class=\"v\">1<\/sup>In those days came John the Baptist, preaching in the wilderness of Jud\u00e6a,<\/p>",
      "parent_chapter_id": "eng-KJV:Matt.3",
      "parent_chapter_path": "\/chapters\/eng-KJV:Matt.3",
      "parent_chapter_name": "Matthew 3",
      "next_verse_id": "eng-KJV:Matt.3.2",
      "next_verse_path": "\/verses\/eng-KJV:Matt.3.2",
      "next_verse_name": "Matthew 3:2",
      "previous_verse_id": "eng-KJV:Matt.2.23",
      "previous_verse_path": "\/verses\/eng-KJV:Matt.2.23",
      "previous_verse_name": "Matthew 2:23",
      "copyright": "<p>King James Version 1611 (Authorized Version). Copyright status: UK English with BFBS additions \u00a9 2011 British and Foreign Bible Society; Crown Copyright in UK<\/p>",
      "footnotes": "",
      "crossreferences": "",
      "parent_chapter": {
        "id": "eng-KJV:Matt.3",
        "chapter": 3,
        "parent_book_id": "eng-KJV:Matt",
        "parent_book": {
          "book_id": "eng-KJV:Matt",
          "name": "Matthew",
          "abbr": "Matt",
          "version_id": "431"
        }
      },
      "note": null,
      "division_theme": null
    },
    {
      "id": "eng-KJV:Matt.3.2",
      "auditid": "0",
      "book": 40,
      "chapter": 3,
      "verse": 2,
      "lastverse": "2",
      "osis_end": "eng-KJV:Matt.3.2",
      "label": "",
      "reference": "Matthew 3:2",
      "text": "<p class=\"p\"><sup id=\"Matt.3.2\" class=\"v\">2<\/sup>and saying, Repent ye: for the kingdom of heaven is at hand.<\/p>",
      "parent_chapter_id": "eng-KJV:Matt.3",
      "parent_chapter_path": "\/chapters\/eng-KJV:Matt.3",
      "parent_chapter_name": "Matthew 3",
      "next_verse_id": "eng-KJV:Matt.3.3",
      "next_verse_path": "\/verses\/eng-KJV:Matt.3.3",
      "next_verse_name": "Matthew 3:3",
      "previous_verse_id": "eng-KJV:Matt.3.1",
      "previous_verse_path": "\/verses\/eng-KJV:Matt.3.1",
      "previous_verse_name": "Matthew 3:1",
      "copyright": "<p>King James Version 1611 (Authorized Version). Copyright status: UK English with BFBS additions \u00a9 2011 British and Foreign Bible Society; Crown Copyright in UK<\/p>",
      "footnotes": "",
      "crossreferences": "",
      "parent_chapter": {
        "id": "eng-KJV:Matt.3",
        "chapter": 3,
        "parent_book_id": "eng-KJV:Matt",
        "parent_book": {
          "book_id": "eng-KJV:Matt",
          "name": "Matthew",
          "abbr": "Matt",
          "version_id": "431"
        }
      },
      "note": null,
      "division_theme": null
    },
    ...
    ]
    }
	 * 
	 */
	
	@SerializedName("theme")
	ChapterTheme chapterTheme;
	
	MarkingItem[] markings = new MarkingItem[0];
	
	Verse[] verses = new Verse[0];
	
	/** @return chapter theme or <code>null</code>. */
	public ChapterTheme getChapterTheme() {
		return chapterTheme;
	}
	
	public Verse[] getVerses() {
		return verses;
	}
	
	/** @return The list of markings. */
	public MarkingItem[] getMarkings() {
		return markings;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** @version 0.7.1-20150910 */
	public static class Verse implements Parcelable, IBibleSearchItem {
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// REMEMBER: Add to parcelable
		////////////////////////////////////////////////////////////////////////////////////////////////
		@SerializedName("id")
		String verseId;
		@SerializedName("verse")
		String number;
		@SerializedName("parent_chapter_name")
		String parentChapter;
		@SerializedName("parent_chapter_id")
		String parentChapterId;
		
		String text;
		String rawText;
		
		String reference;
		
		Note note;
		@SerializedName("division_theme")
		DivisionTheme divisionTheme;
		
		@SerializedName("show_headings")
		boolean showHeadings = false;
		
		public void setNote(Note note) {
			this.note = note;
		}

		public boolean hasDivisionTheme() {
			return (divisionTheme != null && divisionTheme.getDivisionThemeId() != null);
		}
		/** @return <code>true</code> if verse has not, <code>false</code> otherwise. */
		public boolean hasNote(){
			return (note != null && note.getNoteId() != null);
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Start accessors
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		public boolean isShowingHeadings() {
			return showHeadings;
		}
		
		public String getVerseId() {
			return verseId;
		}
		
		/** @return The full clean html text. Note this will contain the verse number. */
		public String getText() {
			return text;
		}
		
		/** @return The raw text string given in the response. */
		public String getRawText() {
			return rawText;
		}
		
		/** @return The cleaned text, giving valid html with removed superfluous content.
		 * Such as verse numbers & (if {@link #isShowingHeadings()} is false) headings */
		public String getVerseOnlyText() {
			String result = "";
			if (text != null) { 
				//remove verse number
				result = text.replaceAll("<sup.*sup>", "");
				if (!showHeadings) {
					result = result.replaceAll("<h.*h\\d?>", "");
				}
			} 
			return result;
		}
		
		public String getParentChapterId() {
			return parentChapterId;
		}
		
		public String getParentChapter() {
			return parentChapter;
		}
		
		public String getNumber() {
			return number;
		}
		
		/** @return The note or <code>null</code>. */
		public Note getNote() {
			return note;
		}
		
		/** @return The division theme or <code>null</code>. */
		public DivisionTheme getDivisionTheme() {
			return divisionTheme;
		}
		
		@Override
		public String getSearchResultName() {
			return reference;
		}
		
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Start parcelable
		////////////////////////////////////////////////////////////////////////////////////////////////

		public static final Parcelable.Creator<Verse> CREATOR = new Parcelable.Creator<Verse>() {
		    public Verse createFromParcel(Parcel in) {
		        return new Verse(in);
		    }
		
		    public Verse[] newArray(int size) {
		        return new Verse[size];
		    }
		};
		
		public Verse(Parcel src) {
			String[] values = new String[7];
			src.readStringArray(values);
			
			verseId = values[0];
			number = values[1];
			parentChapter = values[2];	
			parentChapterId = values[3];
			text = values[4];
			rawText = values[5];
			reference = values[6];
			
			note = src.readParcelable(Note.class.getClassLoader());
			divisionTheme = src.readParcelable(DivisionTheme.class.getClassLoader());
			
			showHeadings = src.readInt() != 0; 
		}
		

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			String[] values = new String[]{
				verseId,
				number, 
				parentChapter,
				parentChapterId,
				text, 
				rawText,
				reference
			};
			
			dest.writeStringArray(values);
			dest.writeParcelable(note, flags);
			dest.writeParcelable(divisionTheme, flags);
			
			dest.writeInt(showHeadings ? 1 : 0);
		}

		
		@Override
		public int describeContents() {
			return 0;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Serializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.2.1-20150824 */
	public static class BibleVerseDeserializer implements JsonDeserializer<BibleVerseResponse> {
		private static final Pattern REGEX_VERSE_NUM = Pattern.compile("<sup.*?>(.*?)<\\/sup>");
		
	  @Override
	  public BibleVerseResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
	      throws JsonParseException {
		  final JsonObject jsonObject = json.getAsJsonObject();		  
		  final JsonArray data = jsonObject.get("data").getAsJsonArray();
	
		  final BibleVerseResponse response = new BibleVerseResponse();
		  
		  JsonElement theme = jsonObject.get("theme");
		  if (theme.isJsonObject()) {
			  response.chapterTheme = context.deserialize(theme, ChapterTheme.class);
		  }
		  
		  JsonElement markings = jsonObject.get("markings");
		  if (markings.isJsonArray()) {			  
			  response.markings = context.deserialize(markings, MarkingItem[].class);
		  }
		  
		  final int SIZE = data.size();
		  List<Verse> verses = new ArrayList<Verse>();
		  
		  for (int index = 0; index < SIZE; index++) {
			  Verse verse = context.deserialize(data.get(index), Verse.class);
			  verse.note = context.deserialize(data.get(index).getAsJsonObject().get("note"), Note.class);
			  verse.divisionTheme = context.deserialize(data.get(index).getAsJsonObject().get("division_theme"), DivisionTheme.class);
			  
			  verse.rawText =  verse.text;
			  //clean text
			  verse.text = Utility.cleanVerseHtml(verse.rawText);
			  
			  //extract verse numbers,
			  Matcher matcher = REGEX_VERSE_NUM.matcher(verse.text);
			  if (matcher.find()) {//if found
				  verse.number = matcher.group(1); //replace with the braced contents
			  } 
			  verses.add(verse);
		  }

		  response.verses = new Verse[verses.size()];
		  verses.toArray(response.verses);
		  
		  return response;
	  }
	}
	
	
}