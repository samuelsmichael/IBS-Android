package com.inductivebiblestudyapp.ui.style.span;

/**
 * Used to inform an whether or not they are enabled. based on their listener.
 * @author Jason Jenkins
 * @version 1.0.0-20150726
 */
public interface ObjectEnabledCallback {
	/** @return <code>true</code> if span is enabled, <code>false</code> otherwise. */
	public boolean isObjectEnabled();
}
