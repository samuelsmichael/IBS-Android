package com.inductivebiblestudyapp.ui;

/**
 * An interface to define a consistent interaction across objects.
 * @author Jason Jenkins
 * @version 0.1.0-20150609
 */
public interface OnBackButtonListener {
	/**
	 * Work to be done after a back button press can be done here.
	 * @return <code>false</code> when the listener is not using the back button,
	 * <code>true</code> when the back button is consumed.
	 */
	public boolean onConsumeBackButton();
}
