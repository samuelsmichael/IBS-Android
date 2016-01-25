package com.inductivebiblestudyapp.ui.viewstub;

import java.util.ArrayList;
import java.util.List;

/**
 * A built on {@link ViewStubManager}, a state manager to manage the pushing/popping of 
 * view stubs to a stack.
 * @author Jason Jenkins
 * @version 0.2.1-20150607
 */
public class ViewStubStackManager {

	private final List<String> mBackStack = new ArrayList<String>();
	
	private final ViewStubManager mManager = new ViewStubManager();
	
	private String mLastTag = "";
	
	public ViewStubStackManager(){}
	
	public ViewStubStackManager(List<String> startingStack, String lastTag) {
		mBackStack.addAll(startingStack);
		this.mLastTag = lastTag;		
	}
	
	/**
	 * Returns the last tag added, regardless if part of the stack or not.
	 * @return The last tag or an empty string.
	 */
	public String getLastTag(){
		return mLastTag;
	}
	
	/** 
	 * @return The current backstack
	 */
	public ArrayList<String> getBackStack() {
		return (ArrayList<String>) mBackStack;
	}
	
	/**
	 * Adds stubs to manager for easier loading.
	 * @param viewStubWrapper
	 * @param tag
	 */
	public void preload(String tag, ViewStubWrapper viewStubWrapper) {
		mManager.add(tag, viewStubWrapper);
	}
	
	/**
	 * Reloads the last visible tag.
	 */
	public void reload(){
		mManager.makeVisible(mLastTag);
	}
	
	/**
	 * Convenience method for {@link #push(String, boolean, boolean)};
	 * calls with all bools set <code>true</code>
	 * @param tag
	 * @return
	 */
	public boolean push(String tag) {
		return push(tag, true, true);
	}
	
	/**
	 * Finds, loads and animates a wrapper.
	 * @param tag
	 * @param animate
	 * @param addToBackstack <code>true</code> to add to backstack, <code>false</code> to not.
	 * @return
	 * @throws NullPointerException if the tag was not preloaded
	 */
	public boolean push(String tag, boolean animate, boolean addToBackstack) {
		if (animate) {
			mManager.fadeInView(tag);
		} else {
			mManager.makeVisible(tag);
		}
		
		if (addToBackstack && !mLastTag.isEmpty()) { 
			//if back stack, add the previous tag if existing
			mBackStack.add(mLastTag);			
		}

		mLastTag = tag;
		
		return true;
	}

	/** Convenience method; calls {@link #pop(boolean)} set <code>true</code> */
	public ViewStubWrapper pop() {
		return pop(true);
	}
	
	/**
	 * Attempts to pop the current screen and load the previous tag. 
	 * @param animate
	 * @return the stub removed or <code>null</code> if there is nothing to pop.
	 */
	public ViewStubWrapper pop(boolean animate) {
		if (mBackStack.isEmpty()) {
			return null;
		}
		
		ViewStubWrapper wrapper = mManager.getStubWrapper(mLastTag);
		
		//set new last tag
		mLastTag = mBackStack.remove(mBackStack.size() - 1);
		
		if (animate) {
			mManager.fadeInView(mLastTag);
		} else {
			mManager.makeVisible(mLastTag);
		}
		return wrapper;
	}
}
