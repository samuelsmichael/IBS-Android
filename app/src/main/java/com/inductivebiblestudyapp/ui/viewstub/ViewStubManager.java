package com.inductivebiblestudyapp.ui.viewstub;

import java.util.HashMap;
import java.util.Map.Entry;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.util.Log;
import android.view.View;

/**
 * A state manager to load, fade and swap the list of {@link ViewStubWrapper}s. This is in lieu
 * of child fragment managers. Allows only one view at a time to be visible.
 * @author Jason Jenkins
 * @version 0.2.1-20150607
 */
public class ViewStubManager {
	final static private String LOGTAG = ViewStubManager.class.getSimpleName();
	
	private static final int FADE_DURATION = 200; //ms		
	
	private final HashMap<String, ViewStubWrapper> mStubMap = new HashMap<String, ViewStubWrapper>();
	
	private String mVisibleStub  = "";
	
	/**
	 * View stub manager. 
	 */
	public ViewStubManager() {
	}
	
	/**
	 * Adds the views to the list to manage.
	 * @param stubTag 
	 * @param viewStubWrapper The wrapper to add and manage.
	 */
	public void add(String stubTag, ViewStubWrapper viewStubWrapper) {
		mStubMap.put(stubTag, viewStubWrapper);
	}
	
	/** @return The stub wrapper or <code>null</code>. */
	public ViewStubWrapper getStubWrapper(String key) {
		return mStubMap.get(key);
	}
	
	/** Returns the tag of the visible stub. */
	public String getLastVisibleStub() {
		return mVisibleStub;
	}
	
	/** Hides all inflated views immediately. */
	public void hideAll() {
		for (Entry<String, ViewStubWrapper> entry : mStubMap.entrySet()) {
			ViewStubWrapper wrapper = entry.getValue();
			if (wrapper.isInflated()) {
				wrapper.getStub().setVisibility(View.GONE);
			}
		}
	}
	
	/**
	 * Sets the previously visible view (by this manager) to invisible and sets the given
	 * take as visible.
	 * @param stubTag
	 */
	public void makeVisible(String stubTag){
		if (!mVisibleStub.isEmpty()) {
			mStubMap.get(mVisibleStub).getStub().setVisibility(View.GONE);
		}
		mStubMap.get(stubTag).getStub().setVisibility(View.VISIBLE);
		mVisibleStub = stubTag;
	}
	
	public void fadeInView(String stubTag) {
		View hide = null;
		if (!mVisibleStub.isEmpty()) {
			hide = mStubMap.get(mVisibleStub).getStub();
		}
		switchFadeViews(mStubMap.get(stubTag).getStub(), hide);
		mVisibleStub = stubTag;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Some elements may not want all the work the viewstubmanager provides, so 
	 * easy switching for them.
	 * @param show
	 * @param hide
	 */
	public static void switchVisible(View show, View hide) {
		hide.setVisibility(View.GONE);
		show.setVisibility(View.VISIBLE);
		show.setAlpha(1.0f); //we can't very well see a transparent view can we?
	}
	
	/**
	 * Takes the two views, shows the first one via fade, hides the second via fade.
	 * @param show The view to show
	 * @param hide The view to hide (can be null)
	 */
	public static void switchFadeViews(final View show, final View hide) {
		if (show.equals(hide)) {
			Log.w(LOGTAG, "These are the same views; you can't switch visibility of the same view!");
			return;
		}
		
		show.animate()
        	.alpha(1.0f)
        	.setDuration(FADE_DURATION)
			.setListener(new AnimatorListenerAdapter() {
			    @Override
	            public void onAnimationEnd(Animator animation) {
	                super.onAnimationEnd(animation);
	                show.clearAnimation();
	        }
	    });
		show.setVisibility(View.VISIBLE); //we can't animate what we can't see
		
		if (hide == null) {
			return;
		}
		
		hide.animate()
        	.alpha(0.0f)
        	.setDuration(FADE_DURATION)
        	.setListener(new AnimatorListenerAdapter() {
			    @Override
	            public void onAnimationEnd(Animator animation) {
	                super.onAnimationEnd(animation);
	                hide.setVisibility(View.GONE);
	                hide.clearAnimation();
            }
        });
		hide.setVisibility(View.VISIBLE); //we can't animate what we can't see
	}
}
