package com.inductivebiblestudyapp.ui.fragments;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/** A common group of methods for common interactions. 
 * @version 0.2.0-20150828
 * */
abstract public class ParentFragment extends Fragment {

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Fragment frag = getCurrentChild();
		if (frag != null) { 
			frag.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	/** Clears backstack from the given tag, forward. Essentially convenience method for 
	 * popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	 * @param tag
	 * @return <code>true</code> if successful, <code>false</code> otherwise.
	 */
	protected boolean popBackStackInclusive(String tag) {
		final FragmentManager fm = getChildFragmentManager();
		Fragment frag = getChildFragmentManager().findFragmentByTag(tag);
		try {
			if (frag != null) {
				fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				return true;
			}
		} catch (Exception e){}
		return false;
	}
	
	/** Returns the currently displayed fragment. */
	abstract protected Fragment getCurrentChild(); 
}
