package com.inductivebiblestudyapp.data.model.bible;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
* The Bible verse details data.
* @author Jason Jenkins
* @version 0.8.2-20150823
*/
public class BibleVerseDetailsResponse {
	
	/*
	 *
	 * Sample output:
	 * 
	 * 
{
  "strongs_text": [
    {
      "book_number": 2,
      "chapter_number": 2,
      "verse_number": 1,
      "verse": "And there <gr str=\"1980\">went <\/gr>a <gr str=\"376\">man <\/gr>of the <gr str=\"4480 1004\">house <\/gr>of <gr str=\"3878\">Levi<\/gr>, and <gr str=\"3947\">took <\/gr><STYLE css=\"color:#808080;font-style:italic\"> to <\/STYLE><STYLE css=\"color:#808080;font-style:italic\"> wife <\/STYLE> a <gr str=\"*853\"><\/gr><gr str=\"1323\">daughter <\/gr>of <gr str=\"3878\">Levi<\/gr>.",
      "strongs": [
        {
          "strongs_concordance_id": 1316,
          "language": "H",
          "number": 1323,
          "text": "daughter"
        },
        {
          "strongs_concordance_id": 378,
          "language": "H",
          "number": 376,
          "text": "man"
        },
        ...
      ]
    }
  ]
}
	 * 
	 */
	
	@SerializedName("strongs_text")
	StrongsVerseData[] strongsVerseData = new StrongsVerseData[0];
	
	
	public StrongsVerseData[] getStrongsVerseData() {
		return strongsVerseData;
	}
	
	@Override
	public String toString() {
		return  super.toString() + "[" +
				"strongsVerseData: " + Arrays.toString(strongsVerseData) + "]";
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.1.0-20150819 */
	public static class StrongsVerseData {
		@SerializedName("verse_number")
		String verseNumber;
		@SerializedName("verse")
	    String strongsKjvText; 
        StrongsNumberEntry[] strongs = new StrongsNumberEntry[0];
        
        public String getVerseNumber() {
			return verseNumber;
		}
        
        public StrongsNumberEntry[] getStrongs() {
    		return strongs;
    	}
    	
    	public String getStrongsKjvText() {
    		return strongsKjvText;
    	}
        
        @Override
    	public String toString() {
    		return  super.toString() + "[" +
    				"verseNumber: " + verseNumber +
    				", strongs length: " + strongs.length + 
    				", strongsKjvText: " + strongsKjvText + "]";
    		
    	}
        
	}
	

	
	/** @version 0.5.0-20150828 */
	public static class StrongsNumberEntry implements Parcelable {
		/*
		 * {
		 * 
		 *  "strongs_concordance_id": 0, 
            "language": "H", //or "G"
            "number": "1980",
            "text": "went"
          }
		 */

		@SerializedName("strongs_concordance_id")
		String strongsConcordanceId;
		String language;
		
		String number;
		String text;
		
		String[] numbers = new String[0];
		 
		/** Default is null, manually set. */
		StrongsNumberEntry mNextEntry = null;
		
		public StrongsNumberEntry() {}

		
		/** Can return <code>null</code> */
		public String getStrongsConcordanceId() {
			return strongsConcordanceId;
		}
		
		public String getNumber() {
			return number;
		}
	
		
		/** Empty if unset */ 
		public String getLanguage() {
			if (language == null) {
				return "";
			}
			return language;
		}
		
		public String getText() {
			return text;
		}
		
		/** @returns The next linked entry if one exists, if not, return <code>null</code> */
		public StrongsNumberEntry getNextEntry() {
			return mNextEntry;
		}
		
		@Override
		public String toString() {
			return super.toString() + "[" + language + number + "," + text + "]";
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof StrongsNumberEntry == false) {
				return false;
			}
			
			final StrongsNumberEntry compare = (StrongsNumberEntry) o;
			if (!text.equals(compare.text)) { //compare texts
				return false;
			};
			
			if (!getNumber().equals(compare.getNumber())) {
				return false;
			}
			
			return true;
		}
		/** Splits the numbers supplied in kjv-text. */
		protected static String[] splitNumbers(String numbers) {
			//remove any * and returns an array of numbers
			return numbers
					.replaceAll(",", "") //remove all commas
					.replaceAll("[^0-9\\s]", "") //replace all non-number, non-whitespace
					.replaceAll("\\s+", ",") //replace whitespace with commas
					.split(",");
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Start parcelable
		////////////////////////////////////////////////////////////////////////////////////////////////

		public static final Parcelable.Creator<StrongsNumberEntry> CREATOR = new Parcelable.Creator<StrongsNumberEntry>() {
		    public StrongsNumberEntry createFromParcel(Parcel in) {
		        return new StrongsNumberEntry(in);
		    }
		
		    public StrongsNumberEntry[] newArray(int size) {
		        return new StrongsNumberEntry[size];
		    }
		};
		
		public StrongsNumberEntry(Parcel src) {
			//do in alphabetical order
			strongsConcordanceId = src.readString();		
			language = src.readString();
			
			number = src.readString();
			text = src.readString();
			
		}
		

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(strongsConcordanceId);		
			dest.writeString(language);
			
			dest.writeString(number);
			dest.writeString(text);
		}

		
		@Override
		public int describeContents() {
			return 0;
		}
			
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Serializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.2.1-20150823 */
	public static class BibleVerseDetailsDeserializer implements JsonDeserializer<BibleVerseDetailsResponse> {
		/** Class name for debugging purposes. */
		final static private String CLASS_NAME = BibleVerseDetailsResponse.BibleVerseDetailsDeserializer.class
				.getSimpleName();
	  @Override
	  public BibleVerseDetailsResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
			  throws JsonParseException {
		  	//new GSON to prevent stackoverflow
		  	final BibleVerseDetailsResponse response = new Gson().fromJson(json, BibleVerseDetailsResponse.class);
	
		  	//extra deserializing 
		  	DocumentBuilder mDocumentBuilder;
		  	try {
				mDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				Log.e(CLASS_NAME, "Cannot load xml");
				mDocumentBuilder = null;
				return response;
			}
		  	
		  	StrongsVerseData[] verseData = response.getStrongsVerseData();
		  	
		  	for (StrongsVerseData strongsVerseData : verseData) {
				try {
					processVerseData(mDocumentBuilder, strongsVerseData);
				} catch (SAXException e) {
					Log.e(CLASS_NAME, "Cannot parse xml");
					e.printStackTrace();
				} catch (IOException e) {
					Log.e(CLASS_NAME, "Cannot read xml");
					e.printStackTrace();
				}			  	
			}
			
	        
		  	return response;
	  	}

		private void processVerseData(DocumentBuilder mDocumentBuilder,
				StrongsVerseData strongsVerseData) throws SAXException, IOException {
			final String parseText = "<!DOCTYPE html><body>" + //wrap to parse
					strongsVerseData.getStrongsKjvText() + "</body>";					
			
			final StrongsNumberEntry[] originalArray = strongsVerseData.getStrongs();
			final List<String> numberList = new ArrayList<String>();
			
			final int SIZE2 = originalArray.length;
			for (int index = 0; index < SIZE2; index++) {
				numberList.add(originalArray[index].getNumber());
			}
			
			//this will be the new list added
			List<StrongsNumberEntry> newList = new ArrayList<BibleVerseDetailsResponse.StrongsNumberEntry>();
	
			InputStream in = new ByteArrayInputStream(parseText.getBytes(Charset.forName("UTF-8")));
			Document doc = mDocumentBuilder.parse(in);
			doc.getDocumentElement().normalize();
			
			NodeList nodes = doc.getElementsByTagName("gr"); //all strongs words.
	      
			final int SIZE = nodes.getLength();		      	
			for (int index = 0; index < SIZE; index++) { //check every word			
				final Node node = (Element) nodes.item(index);
				
				processNumbers(originalArray, numberList, newList, node);
			}
			
			strongsVerseData.strongs = new StrongsNumberEntry[newList.size()];
			newList.toArray(strongsVerseData.strongs);
		}
	
		private void processNumbers(final StrongsNumberEntry[] originalArray,
				final List<String> numberList, List<StrongsNumberEntry> newList,
				final Node node) {
			

			final String[] numbers = //all strongs number for a given word
					StrongsNumberEntry.splitNumbers( ((Element) node).getAttribute("str") );
						
			StrongsNumberEntry prevEntry = null;
			for (String strongNumber : numbers) {
				int numIndex = numberList.indexOf(strongNumber);
				
				if (numIndex > -1) { //if this strong's number is found, 
					if (prevEntry == null) { //add if this is the words first entry
						newList.add(originalArray[numIndex]);
						
					} else if (!prevEntry.equals(originalArray[numIndex])){ 
						//link the next entry if not
						prevEntry.mNextEntry = originalArray[numIndex];
					}	        				
					prevEntry = originalArray[numIndex];					
				} else {
					//if not found, create our own blank entry.
					StrongsNumberEntry newEntry = new StrongsNumberEntry();
					newEntry.language = originalArray.length > 0 ? originalArray[0].getLanguage() : ""; //we guess it's the same
					newEntry.text = node.getTextContent(); //get text
					newEntry.number = strongNumber;
					if (newEntry.text == null) {
						newEntry.text = "";
					}
					newList.add(newEntry);
				}
			}
			
		}
	}
}