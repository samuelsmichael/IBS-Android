package com.inductivebiblestudyapp.data.model.bible.wordstudy;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
* The response for strong's definition.
* @author Jason Jenkins
* @version 0.1.1-20150820
*/
public class StrongsDefinition implements Parcelable {

	public static final String LANGUAGE_HEBREW = "H";
	public static final String LANGUAGE_GREEK = "G";
	
	//REMEMBER TO ADD TO PARCELABLE
	
	/*
	 *
	 * Sample output:
	 * 
{
  "number": 450,
  "language": "H",
  "orig_word": "[dyla",
  "word_orig": "from (0410) and (03045)",
  "translit": "'Elyada`",
  "tdnt": "None",
  "phonetic": "el-yaw-daw'",
  "part_of_speech": "Proper Name Masculine",
  "st_def": "from \u00ab0410\u00bb and \u00ab03045\u00bb; God (is) knowing; Eljada, the name of two Israelites and of an Aramaean leader:--Eliada.",
  "IPD_def": "Eliadah or Eliada = \"God knows\"<OL TYPE=\"1\"><LI>a son of David<LI>a Benjamite warrior chief<LI>an Aramean, the father of an enemy of Solomon<\/OL>"
}

	 * 
	 */
	
	public StrongsDefinition() {}	
	
	String number;
	String language;
	@SerializedName("orig_word")
	String originalWord;
	@SerializedName("word_orig")
	String wordOrigin;
	@SerializedName("translit")
	String transliteration;
	
	String phonetic;
	@SerializedName("part_of_speech")
	String partOfSpeech;
	@SerializedName("st_def")
	String strongsDefinition;
	
	@SerializedName("IPD_def")
	String ipdDefinition;
	/** Added for robustness; not actually expected */
	@SerializedName("ipd_def")
	String ipdDefinition2;
	
	/** Additional references such as:
	 * - Theological Dictionary of the New Testament (TDNT) 
	 * - Theological Wordbook of the Old Testament (TWOT)
	 */
	String tdnt;
	
	
	public String getNumber() {
		return number;
	}
	
	/** See {@link #LANGUAGE_GREEK} & {@link #LANGUAGE_HEBREW} */
	public String getLanguage() {
		return language;
	}
	
	public String getOriginalWord() {
		return originalWord;
	}
	
	public String getPartOfSpeech() {
		return partOfSpeech;
	}
	
	public String getPhonetic() {
		return phonetic;
	}
	
	public String getTransliteration() {
		return transliteration;
	}
	
	public String getWordOrigin() {
		return wordOrigin;
	}
	
	/** Outputs html */
	public String getStrongsDefinition() {
		return strongsDefinition;
	}
	
	/** Outputs html */
	public String getIpdDefinition() {
		if (TextUtils.isEmpty(ipdDefinition)){
			//if empty, try fallback
			return ipdDefinition2;
		}
		return ipdDefinition;
	}
	
	/** Returns additional references such as: 
	 * - Theological Dictionary of the New Testament (TDNT) 
	 * - Theological Wordbook of the Old Testament (TWOT)
	 * 
	 */
	public String getAdditionalReferences() {
		return tdnt;
	}
	
	
	@Override
	public String toString() {
		return super.toString() + "[" +
				"strong's: " + number +
				". " + language +
				" -- " +transliteration +
				", wordOrigin: " + wordOrigin +
				"]";
	}


	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start parcelable
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Parcelable.Creator<StrongsDefinition> CREATOR = new Parcelable.Creator<StrongsDefinition>() {
	    public StrongsDefinition createFromParcel(Parcel in) {
	        return new StrongsDefinition(in);
	    }
	
	    public StrongsDefinition[] newArray(int size) {
	        return new StrongsDefinition[size];
	    }
	};
	
	public StrongsDefinition(Parcel src) {
		//do in alphabetical order
		originalWord = src.readString();
		ipdDefinition = src.readString();
		ipdDefinition2 = src.readString();		
		language = src.readString();
		
		number = src.readString();
		partOfSpeech = src.readString();
		phonetic = src.readString();
		strongsDefinition = src.readString();
		tdnt = src.readString();
		
		transliteration = src.readString();
		wordOrigin = src.readString();
		
	}
	

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(originalWord);
		dest.writeString(ipdDefinition);
		dest.writeString(ipdDefinition2);		
		dest.writeString(language);
		
		dest.writeString(number);
		dest.writeString(partOfSpeech);
		dest.writeString(phonetic);
		dest.writeString(strongsDefinition);
		dest.writeString(tdnt);
		
		dest.writeString(transliteration);
		dest.writeString(wordOrigin);
	}

	
	@Override
	public int describeContents() {
		return 0;
	}
		
	
	
}