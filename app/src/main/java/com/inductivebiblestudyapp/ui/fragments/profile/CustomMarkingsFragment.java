package com.inductivebiblestudyapp.ui.fragments.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.ui.OnBackButtonListener;
import com.inductivebiblestudyapp.ui.fragments.ParentFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.markings.MarkingsEditImageFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.markings.MarkingsEditLetteringFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.markings.MarkingsSummaryFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.markings.MarkingsViewAllImagesFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.markings.MarkingsViewAllLetteringsFragment;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A simple {@link Fragment} subclass. 
 * @version 0.4.0-20150804
 * 
 */
public class CustomMarkingsFragment extends ParentFragment implements OnBackButtonListener,
	MarkingsSummaryFragment.OnInteractionListener,
	MarkingsViewAllImagesFragment.OnInteractionListener,
	MarkingsViewAllLetteringsFragment.OnInteractionListener,
	MarkingsEditImageFragment.OnInteractionListener,
	MarkingsEditLetteringFragment.OnInteractionListener {
	
	final static private String CLASS_NAME = CustomMarkingsFragment.class
			.getSimpleName();
	private static final String TAG_SUMMARY_FRAG = CLASS_NAME + ".TAG_SUMMARY_FRAG";
	private static final String TAG_VIEWALL_IMAGES_FRAG = CLASS_NAME + ".TAG_VIEWALL_IMAGES_FRAG";
	private static final String TAG_VIEWALL_LETTERINGS_FRAG = CLASS_NAME + ".TAG_VIEWALL_LETTERINGS_FRAG";
	
	private static final String TAG_EDIT_IMAGE_FRAG = CLASS_NAME + ".TAG_EDIT_IMAGE_FRAG";
	
	private static final String TAG_EDIT_LETTERING_FRAG = CLASS_NAME + ".TAG_EDIT_LETTERING_FRAG";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End tags
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private static final String KEY_CURRENT_FRAG = CLASS_NAME + ".KEY_CURRENT_FRAG";
	
	
	public CustomMarkingsFragment() {
		// Required empty public constructor
	}
	
	private String mLastTag = null;
	

	
	@Override
	public void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		outState.putString(KEY_CURRENT_FRAG, mLastTag);	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_custom_markings, container,
				false);
		
		if (savedInstanceState == null) {
			getChildFragmentManager()
	            .beginTransaction()	            
				.addToBackStack(TAG_SUMMARY_FRAG) //required for onConsumeBackButton() to work
	            .add(R.id.ibs_markings_container, MarkingsSummaryFragment.newInstance(), TAG_SUMMARY_FRAG)
	            .commit();
			mLastTag = TAG_SUMMARY_FRAG;			
		} else {
			mLastTag = savedInstanceState.getString(KEY_CURRENT_FRAG, mLastTag);
					
			FragmentManager fm = getChildFragmentManager();
			Fragment frag = getChildFragmentManager().findFragmentByTag(mLastTag);
			fm.beginTransaction().attach(frag).commit();
		}
		
		
		// Inflate the layout for this fragment
		return rootView;
	}
	
	@Override
	protected Fragment getCurrentChild() {
		return getChildFragmentManager().findFragmentByTag(mLastTag);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void pushFragment(Fragment frag, String tag) {
		Utility.pushFragments(R.id.ibs_markings_container, getChildFragmentManager(), frag, tag);
		mLastTag = tag;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Implemented Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean onConsumeBackButton() {
		final int stackCount = getChildFragmentManager().getBackStackEntryCount();
		
		Fragment current = getChildFragmentManager().findFragmentByTag(mLastTag);
		if (current != null && current instanceof OnBackButtonListener) {
			//if our current fragment is back button listener, check what it wants to do
			if (((OnBackButtonListener) current).onConsumeBackButton()) {
				return true; //it was consumed
			}
			//our child fragment did not consume
		}
		
		if (stackCount > 1) {
			//get the previous item
			BackStackEntry entry = getChildFragmentManager().getBackStackEntryAt(stackCount -2);
			mLastTag = entry.getName();
			getChildFragmentManager().popBackStack();
			return true;
		} else {
			return false;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start summary listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onSummaryImageClick(ImageItem data) {
		pushFragment(MarkingsEditImageFragment.newInstance(data), TAG_EDIT_IMAGE_FRAG);
	}
	

	@Override
	public void onSummaryCreateNewImage() {
		pushFragment(MarkingsEditImageFragment.newInstance(), TAG_EDIT_IMAGE_FRAG);
	}
	

	@Override
	public void onViewAllImages() {
		pushFragment(MarkingsViewAllImagesFragment.newInstance(), TAG_VIEWALL_IMAGES_FRAG);
	}


	@Override
	public void onViewAllLetterings() {
		pushFragment(MarkingsViewAllLetteringsFragment.newInstance(), TAG_VIEWALL_LETTERINGS_FRAG);
	}


	@Override
	public void onSummaryLetteringClick(LetteringItem data) {
		pushFragment(MarkingsEditLetteringFragment.newInstance(data), TAG_EDIT_LETTERING_FRAG);	
	}


	@Override
	public void onSummaryCreateNewLettering() {
		pushFragment(MarkingsEditLetteringFragment.newInstance(), TAG_EDIT_LETTERING_FRAG);	
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start view all listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
		
	@Override
	public void onImageClick(ImageItem data) {
		pushFragment(MarkingsEditImageFragment.newInstance(data), TAG_EDIT_IMAGE_FRAG);
	}
	
	@Override
	public void onCreateNewImage() {
		pushFragment(MarkingsEditImageFragment.newInstance(), TAG_EDIT_IMAGE_FRAG);
	}
	
	
	@Override
	public void onLetteringClick(LetteringItem data) {
		pushFragment(MarkingsEditLetteringFragment.newInstance(data), TAG_EDIT_LETTERING_FRAG);				
	}
	
	@Override
	public void onCreateNewLettering() {
		pushFragment(MarkingsEditLetteringFragment.newInstance(), TAG_EDIT_LETTERING_FRAG);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start delete listeners
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onImageRequestPop() {
		if (TAG_EDIT_IMAGE_FRAG.equals(mLastTag)) {
			onConsumeBackButton(); //lazy way to pop
		}
	}

	@Override
	public void onLetteringPopRequest() {
		if (TAG_EDIT_LETTERING_FRAG.equals(mLastTag)) {
			onConsumeBackButton(); //lazy way to pop
		}
	}	
	
}
