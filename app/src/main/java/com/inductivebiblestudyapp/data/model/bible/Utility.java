package com.inductivebiblestudyapp.data.model.bible;

/** Used within the model 
 * @version 0.1.0-20150824 */
public class Utility {

	public static String cleanVerseHtml(String string) {
		return string.replaceAll("\\\\n", "") //remove "new lines"; you're either using html or not
		  	.replaceAll("\\n", "") 				  	
		  	.replaceAll("\\\\", "") //remove escapes
		  	.replaceAll("&quot;", "\"") //use actual quotes so html ids render correctly
	  		.replaceAll("&gt;", ">") //same with < and >
	  		.replaceAll("&lt;", "<");
	}
}
