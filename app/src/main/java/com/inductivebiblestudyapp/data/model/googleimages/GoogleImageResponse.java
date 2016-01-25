package com.inductivebiblestudyapp.data.model.googleimages;

import android.os.Parcelable;


/**
 * The interface defining the basic structure needed for a google image search
 * response, including a list of results & the ability to fetch the next page.
 * @author Jason Jenkins
 * @version 1.1.0-20150730
 */
public interface GoogleImageResponse {	
	
	/** @return The zero-indexed page number. */
	public int getCurrentPageIndex();
	
	/** @return The current start. */
	public int getStart();
	
	/** @return The next page of the response or <code>null</code> if no page
	 * is available after the current one.
	 */
	public Page getNextPage();
	
	/** 
	 * @return An array of {@link GoogleImageResult}s
	 */
	public GoogleImageResult[] getResults(); 
	

	/** @return A number of estimated results (should be parsable) */
	public String getEstimatedResultCount(); 	
	
	/** @return <code>true</code> when request is successful, <code>false</code> when it fails. */
	public boolean isSuccessful();	
	
	/** An interfaces of how what all Google Image Results must return at minimum.
	 * 
	 * @author Jason Jenkins
	 * @version 1.0.0-20150730
	 */
	public static interface GoogleImageResult extends Parcelable {

		/** @return The image title with the search term highlight via <b>bold</b> tags. */
		public String getTitle();
		
		/** @return The image title with no formatting. */
		public String getTitleNoFormatting();
		
		/** @return The unescaped  url of the image. */
		public String getUnescapedUrl();
		
		/** @return The google thumbail url. Remember, this will not end in a file extension */
		public String getThumbnailUrl();
		
	}
	
	/** 
	 * The minimum items required for a page result.
	 * @version 0.1.0-20150730 */
	public static interface Page {
		
		/** @return The starting image of the page. Can be parsed to int. */
		public String getStart();
		
		/** @return The one-indexed page number. Can be parsed to int. */
		public String getLabel();
		
		/** @return The start (0 based) index or -1 on failure. */
		public int getStartAsInt();
		
		/** @return The one-indexed page number or -1 on failure. */
		public int getLabelAsInt(); 
		
	}

	
}
