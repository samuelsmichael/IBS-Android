package com.inductivebiblestudyapp.data.model.bible.wordstudy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.inductivebiblestudyapp.data.model.bible.Utility;


/** 
 * The response for a cross reference.
* @author Jason Jenkins
 * @version 0.3.3-20150827 */
public class CrossReference implements Parcelable {
	//REMEMBER TO ADD TO PARCELABLE
	
	/*
	 *  {
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
  }
	 */
	
	public CrossReference() {}
		
	String title;		

	@SerializedName("parent_book_name")
	String bookName;
	@SerializedName("parent_book_abbr")
	String bookNameShort;
	@SerializedName("chapter_number")
	String chapterNumber;
	@SerializedName("verse_number")
	String verseNumber;
	
	@SerializedName("verse")
	String verseRaw;
	/** Manually set. */
	String verseText = null;
	
	@SerializedName("verse_id")
	String verseId;
	@SerializedName("chapter_id")
	String chapterId;
	
	/** Manually set. */
	String verseRange = "";
	@SerializedName("number_of_occurrences")
	String numberOfOccurences;
	
	@SerializedName("show_headings")
	boolean showHeadings = false;
	
	public String getTitle() {
		return title;
	}
	
	public String getBookName() {
		return bookName;
	}
	
	public String getBookNameShort() {
		return bookNameShort;
	}
	
	public String getChapterNumber() {
		return chapterNumber;
	}
	
	private static final Pattern REGEX_VERSE_NUM = Pattern.compile("<sup.*?>(.*?)<\\/sup>");
	
	/** @return Returns the reference from the verse text. */
	public String getVerseRange() {
		if (TextUtils.isEmpty(verseRange)) {
			Matcher matcher = REGEX_VERSE_NUM.matcher(Utility.cleanVerseHtml(verseRaw));
			if (matcher.find()) {
				verseRange = matcher.group(1);
			}
		}
		return verseRange;
	}
	
	public String getVerseNumber() {
		return verseNumber;
	}
	
	/** Returns cleaned html string, with no verse number. */
	public String getVerseTextOnly() {
		if (TextUtils.isEmpty(verseText) && verseRaw != null) {
			verseText = Utility.cleanVerseHtml(verseRaw);
		}
		if (showHeadings) {
			return REGEX_VERSE_NUM.matcher(verseText).replaceFirst("");
		}
		return REGEX_VERSE_NUM.matcher(verseText.replaceAll("<h.*h\\d?>", ""))
				.replaceFirst("");
	}
	
	/** Returns the cleaned html string, with verse number. */
	public String getVerse() {
		if (TextUtils.isEmpty(verseText) && verseRaw != null) {
			verseText = Utility.cleanVerseHtml(verseRaw);
		}
		return verseText;
	}
	
	public String getChapterId() {
		return chapterId;
	}
	
	/** The current version's verse id. */
	public String getVerseId() {
		return verseId;
	}
	
	public String getNumberOfOccurences() {
		return numberOfOccurences;
	}
	
	@Override
	public String toString() {
		return super.toString() + "[" +
				" title: " + title  + " (" + numberOfOccurences + ") " +
				", --> " + bookName + " " + chapterNumber + ":" + verseNumber + 
				" (" + verseId + ") ]";
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Parcelable.Creator<CrossReference> CREATOR = new Parcelable.Creator<CrossReference>() {
	    public CrossReference createFromParcel(Parcel in) {
	        return new CrossReference(in);
	    }
	
	    public CrossReference[] newArray(int size) {
	        return new CrossReference[size];
	    }
	};
	
	public CrossReference(Parcel src) {
		//do in alphabetical order
		this.bookName = src.readString();
		this.bookNameShort = src.readString();
		this.chapterId = src.readString();
		this.chapterNumber = src.readString();
		this.numberOfOccurences = src.readString();
		
		this.showHeadings = src.readInt() != 0;
		
		this.title = src.readString();
		this.verseId = src.readString();
		this.verseNumber = src.readString();
		this.verseRange = src.readString();
		this.verseRaw = src.readString();

		this.verseText = src.readString();
		
	}
	

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(bookName);
		dest.writeString(bookNameShort);
		dest.writeString(chapterId);
		dest.writeString(chapterNumber);
		dest.writeString(numberOfOccurences);
		
		dest.writeInt(showHeadings ? 1 : 0);
		
		dest.writeString(title);
		dest.writeString(verseId);
		dest.writeString(verseNumber);
		dest.writeString(verseRange);
		dest.writeString(verseRaw);
		
		dest.writeString(verseText);
	}

	
	@Override
	public int describeContents() {
		return 0;
	}
}
