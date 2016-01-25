package com.inductivebiblestudyapp.data.model;

import java.util.List;

/**
 * 
 * @author Jason Jenkins
 * @version 0.1.0-20150616
 */
class Utility {
	/**
	 * Takes a list and converts it into rows in a single string, separated by 
	 * a new line.
	 * @param list The list or <code>null</code>
	 * @return The list as a string or an empty string if input is null
	 */
	public static String listToString(List<String> list) {
		StringBuilder result = new StringBuilder();
		if (list != null) {
			int count = 0;
			for (String string : list) {
				if (count > 0) {
					result.append("\\n"); //new line between errors
				}
				result.append(string);
				count++;
			}			
		}
		return result.toString();
	}
	
}
