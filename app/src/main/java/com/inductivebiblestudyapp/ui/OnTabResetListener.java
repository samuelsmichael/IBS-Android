package com.inductivebiblestudyapp.ui;

/**
 * An interface to define a consistent interaction across objects.
 * @author Jason Jenkins
 * @version 0.1.0-20150921
 */
public interface OnTabResetListener {
	/**
	 * Work to be done after a tab reset request/
	 * @return <code>false</code> when the listener wants to reset the tab,
	 * <code>true</code> when tab reset is consumed.
	 */
	public boolean onConsumeTabReset();
}
