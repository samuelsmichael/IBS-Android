package com.inductivebiblestudyapp.data.model;

/**
 * Common interface so that classes which are all content responses but
 * different forms can be used the same way.
 * @version 1.0.0-20150731
 *
 */
public interface IContentResponse {
	/** @return Message content, blank if not found */
	public String getContent();
	/** @return Message title, blank if not found */
	public String getTitle();
}
