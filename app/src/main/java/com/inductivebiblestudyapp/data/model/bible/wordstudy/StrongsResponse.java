package com.inductivebiblestudyapp.data.model.bible.wordstudy;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
* The response for strong's definition.
* @author Jason Jenkins
* @version 0.5.0-20150824
*/
public class StrongsResponse implements Parcelable {

	//REMEMBER TO ADD TO PARCELABLE
	
	/*
	 *
	 * Sample output:
	 * 
"strongs_definition": {
    "number": 1980,
    "language": "H",
    "orig_word": "$lh",
    "word_orig": "akin to (03212), a primitive root",
    "translit": "halak",
    "tdnt": "TWOT - 498",
    "phonetic": "haw-lak'",
    "part_of_speech": "Verb",
    "st_def": "akin to \u00ab03212\u00bb; a primitive root; to walk (in a great variety of applications, literally and figuratively):--(all) along, apace, behave (self), come, (on) continually, be conversant, depart, + be eased, enter, exercise (self), + follow, forth, forward, get, go (about, abroad, along, away, forward, on, out, up and down), + greater, grow, be wont to haunt, lead, march, \u00d7 more and more, move (self), needs, on, pass (away), be at the point, quite, run (along), + send, speedily, spread, still, surely, + tale-bearer, + travel(-ler), walk (abroad, on, to and fro, up and down, to places), wander, wax, (way-)faring man, \u00d7 be weak, whirl.",
    "IPD_def": "<OL TYPE=\"1\"><LI>to go, walk, come<OL TYPE=\"a\"><LI>(Qal)<OL TYPE=\"1\"><LI>to go, walk, come, depart, proceed, move, go away<LI>to die, live, manner of life (fig.)<\/OL><LI>(Piel)<OL TYPE=\"1\"><LI>to walk<LI>to walk (fig.)<\/OL><LI>(Hithpael)<OL TYPE=\"1\"><LI>to traverse<LI>to walk about<\/OL><LI>(Niphal) to lead, bring, lead away, carry, cause to walk<\/OL><\/OL>"
  },
  "cross_references": [
    {
      "kjv_translation_id": 4686,
      "parent_book_name": "Proverbs",
      "parent_book_abbr": "Prov",
      "chapter_number": 20,
      "verse_number": 19,
      "title": "about",
      "strongs_concordance_id": 1967,
      "number_of_occurrences": 2,
      "verse": "<p class=\"q1\"><sup id=\"Prov.20.19\" class=\"v\">19<\/sup>He that goeth about <span class=\"add\">as<\/span> a talebearer revealeth secrets:<\/p>\n<p class=\"q2\">Therefore meddle not with him that flattereth with his lips.<\/p>",
      "chapter_id": "eng-KJV:Prov.20",
      "verse_id": "eng-KJV:Prov.20.19"
    },
   ...
  ]
	 * 
	 */
	
	@SerializedName("cross_references")
	CrossReferenceEntry[] crossReferenceEntries = new CrossReferenceEntry[0];
	
	@SerializedName("strongs_definition")
	StrongsDefinition strongsDefinition;
	
	public StrongsDefinition getStrongsDefinition() {
		return strongsDefinition;
	}
	
	public CrossReferenceEntry[] getCrossReferenceEntries() {
		return crossReferenceEntries;
	}
	
	@Override
	public String toString() {
		return super.toString() + "[" +
				"strongsDefinition: " + strongsDefinition +
				", crossReferenceEntries: " + crossReferenceEntries.length + "]";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** 
	 * Simple entries showing the potential cross references
	 * @version 0.4.0-20150824 */
	public static class CrossReferenceEntry implements Parcelable {
		//REMEMBER TO ADD TO PARCELABLE
		/*
		 *  {
        "kjv_translation_id": 9419,
        "parent_book_name": "Exodus",
        "parent_book_abbr": "Exod", //see api/bible response,
        "chapter_number": 2,
        "verse_number": 1,
        "title": "man",
        "strongs_concordance_id": 378,
        "number_of_occurrences": 506
	    //strongs_concordance_kjv_text.verse 
		"verse": "And there <gr str="1980">went </gr>a <gr str="376"..."
		//chapter_verse.id
		"verse_id": "eng-AMP:Exod.2.1"
      },
		 */
		
		public CrossReferenceEntry() {}
		
		@SerializedName("kjv_translation_id")
		String kjvTranslationId;
		String title;		 
		@SerializedName("number_of_occurrences")
		String numberOfOccurences;
		
		public String getKjvTranslationId() {
			return kjvTranslationId;
		}
		
		public String getTitle() {
			return title;
		}
		
		
		public String getNumberOfOccurences() {
			return numberOfOccurences;
		}
		
		@Override
		public String toString() {
			return super.toString() + "[" +
					"kjvTranslationId: " + kjvTranslationId + ", " +
					" title: " + title  + " (" + numberOfOccurences + ") ]";
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Start parcelable
		////////////////////////////////////////////////////////////////////////////////////////////////

		public static final Parcelable.Creator<CrossReferenceEntry> CREATOR = new Parcelable.Creator<CrossReferenceEntry>() {
		    public CrossReferenceEntry createFromParcel(Parcel in) {
		        return new CrossReferenceEntry(in);
		    }
		
		    public CrossReferenceEntry[] newArray(int size) {
		        return new CrossReferenceEntry[size];
		    }
		};
		
		public CrossReferenceEntry(Parcel src) {
			//do in alphabetical order
			this.kjvTranslationId = src.readString();
			this.numberOfOccurences = src.readString();
			
			this.title = src.readString();
		}
		

		@Override
		public void writeToParcel(Parcel dest, int flags) {

			dest.writeString(kjvTranslationId);
			dest.writeString(numberOfOccurences);
			
			dest.writeString(title);		
		}

		
		@Override
		public int describeContents() {
			return 0;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Parcelable.Creator<StrongsResponse> CREATOR = new Parcelable.Creator<StrongsResponse>() {
	    public StrongsResponse createFromParcel(Parcel in) {
	        return new StrongsResponse(in);
	    }
	
	    public StrongsResponse[] newArray(int size) {
	        return new StrongsResponse[size];
	    }
	};
	
	public StrongsResponse(Parcel src) {
		//do in alphabetical order
		crossReferenceEntries = src.createTypedArray(CrossReferenceEntry.CREATOR);
		strongsDefinition = src.readParcelable(StrongsDefinition.class.getClassLoader());
	}
	

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedArray(crossReferenceEntries, flags);
		dest.writeParcelable(strongsDefinition, flags);
	}

	
	@Override
	public int describeContents() {
		return 0;
	}
		
	
	
}