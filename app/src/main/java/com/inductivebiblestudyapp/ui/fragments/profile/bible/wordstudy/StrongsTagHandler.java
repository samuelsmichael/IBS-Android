package com.inductivebiblestudyapp.ui.fragments.profile.bible.wordstudy;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html.TagHandler;

import com.inductivebiblestudyapp.util.RomanNumberal;

/** Handles unsupported tags.
 * @version 0.3.1-20150823 */
class StrongsTagHandler implements TagHandler {
	final List<TagLevel> tagLevels = new ArrayList<TagLevel>();

	@Override
	public void handleTag(boolean opening, String tag, Editable output,
			XMLReader xmlReader) {
		 if (tag.equals("ul")) {
			 if (opening) {
				 tagLevels.add(new TagLevel(tag, tagLevels.size()));
			 } else {
				 popCurrentLevel();
			 }
		 } else if (tag.equals("ol")) {
			if (opening) {
				 tagLevels.add(new TagLevel(tag, tagLevels.size()));
			 } else {
				 popCurrentLevel();
			 }
		 }
		 
		 TagLevel curr = getCurrentLevel();
		 if (curr == null) {
			 return;
		 }
		 
	 	if (tag.equals("li")) {
	        if (curr.parent.equals("ul")) {
	           if (curr.first) {
	                output.append("\n" + curr.tabs + "•");
	                curr.first=  false;
	            } else {
	            	curr.first = true;
	            }
	            
	        } else {
	        	if (curr.first) {
	                output.append("\n" + curr.tabs + curr.getNumber());
	                curr.first = false;
	                curr.count++;
	            } else {
	            	curr.first = true;
	            }
	        }   
	    }
	}
	
	
	private TagLevel getCurrentLevel() {
		if (tagLevels.size() == 0) {
			return null;
		}
		return tagLevels.get(tagLevels.size() -1 );
	}
	
	private TagLevel popCurrentLevel() {
		if (tagLevels.size() == 0) {
			return null;
		}
		return tagLevels.remove(tagLevels.size() -1 );
	}
	
	private static class TagLevel {
		boolean first = true;
		/** 1-indexed value. */
		int count = 1;
		
		final String tabs;			
		final String parent;
		
		final int level;
		
		public TagLevel(String tag, int level) {
			this.parent = tag;
			this.level = level;
			StringBuilder builder = new StringBuilder();
			for (int count = 0; count <= level; count++) {
				builder.append("\t");
			}
			this.tabs = builder.toString();
		}
		
		public String getNumber() {
			switch (level % 3) {
			case 0: //0, 3
				return String.valueOf(count) + ". ";
			case 1: //1, 4
				return getLetter(count) +". ";
			default: //2, 5
				return RomanNumberal.toRomanLower(count) +". ";
			}
		}
		

		final static String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		@SuppressWarnings("unused")
		final static String alphabet = "abcdefghijklmnopqrstuvwxyz";
		static String getLetter(int count) {
			count = (count - 1) % ALPHABET.length();
			return ""+ALPHABET.charAt(count);
		}
	}
}
