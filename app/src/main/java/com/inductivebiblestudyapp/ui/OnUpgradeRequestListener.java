package com.inductivebiblestudyapp.ui;

/**
 * An interface to define a consistent interaction across objects
 * for upgrades.
 * @author Jason Jenkins
 * @version 0.1.0-20150921
 */
public interface OnUpgradeRequestListener {
	/** Requests that the upgrade process be started. */
	public void requestUpgrade();
}
