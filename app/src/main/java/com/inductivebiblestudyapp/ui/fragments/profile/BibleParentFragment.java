package com.inductivebiblestudyapp.ui.fragments.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.auth.CurrentUser.OnTranslationUpdateListener;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseDetailsResponse.StrongsNumberEntry;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse.Verse;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.CrossReference;
import com.inductivebiblestudyapp.ui.OnBackButtonListener;
import com.inductivebiblestudyapp.ui.OnTabResetListener;
import com.inductivebiblestudyapp.ui.OnUpgradeRequestListener;
import com.inductivebiblestudyapp.ui.fragments.ParentFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.BibleChapterViewFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.BibleVerseDetailsFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.wordstudy.WordStudyFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.notes.NotesCrossReferenceDefintionFragment;
import com.inductivebiblestudyapp.util.Utility;

/**
 * This view is used by parents that must display/manage the bible interactions.
 *  
 * @author Jason Jenkins
 * @version 0.4.0-20150921
 */
abstract class BibleParentFragment extends ParentFragment implements 
	BibleChapterViewFragment.OnInteractionListener,
	BibleVerseDetailsFragment.OnInteractionListener, 
	WordStudyFragment.OnInteractionListener,
	OnBackButtonListener, OnTabResetListener,
	OnTranslationUpdateListener {
	
	final static private String CLASS_NAME = BibleParentFragment.class.getSimpleName();
	
	
	
	protected static final String TAG_CHAPTER_VIEW_FRAG = CLASS_NAME + ".TAG_CHAPTER_VIEW_FRAG";
	protected static final String TAG_DEFINTION_FRAG = CLASS_NAME + ".TAG_DEFINTION_FRAG";
	protected static final String TAG_CROSS_REFERENCES_FRAG = CLASS_NAME + ".TAG_CROSS_REFERENCES_FRAG";
	
	protected static final String TAG_VERSE_VIEW_FRAG = CLASS_NAME + ".TAG_VERSE_VIEW_FRAG";
	
	protected static final String TAG_STRONGS_NUMBER = CLASS_NAME + ".TAG_STRONGS_NUMBER";
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End tags
	////////////////////////////////////////////////////////////////////////////////////////////////

	protected static final String KEY_LAST_VISIBLE_STUB = CLASS_NAME + ".KEY_VISIBLE_STUB";

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////

	public BibleParentFragment() {
		// Required empty public constructor
	}
	
	protected String mLastTag = "";
	
	protected CurrentUser mCurrentUser = null;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		outState.putString(KEY_LAST_VISIBLE_STUB, mLastTag);		
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = onInflateView(inflater, container, savedInstanceState);
		
		if (savedInstanceState == null) {
			addStartingFragment();		
		} else {
			mLastTag = savedInstanceState.getString(KEY_LAST_VISIBLE_STUB, mLastTag);
					
			FragmentManager fm = getChildFragmentManager();
			Fragment frag = getChildFragmentManager().findFragmentByTag(mLastTag);
			fm.beginTransaction().attach(frag).commit();
		}
		mCurrentUser = new CurrentUser(getActivity());
		mCurrentUser.setOnTranslationUpdateListener(this);
		
		return rootView;
	}
	

	@Override
	protected Fragment getCurrentChild() {
		return getChildFragmentManager().findFragmentByTag(mLastTag);
	}
	
	/** Adds new fragment to the view, using {@link #getLayoutReplaceId()} */
	protected void pushFragment(Fragment frag, String tag) {
		Utility.pushFragments(getLayoutReplaceId(), getChildFragmentManager(), frag, tag);
		mLastTag = tag;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Abstract methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The inflated view to replace views in */
	abstract protected View onInflateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState);
	
	/** Adds the starting fragment. */
	abstract protected void addStartingFragment();
	
	/** Gets load stub to pass onto {@link #onVerseCrossReference(String)} for chapter loading. */
	abstract protected String getLoadStub();
	
	/** The layout id used in {@link #pushFragment(Fragment, String)} to replace the view. */
	abstract protected int getLayoutReplaceId();
	
	/** The values to reset on translation updates. */
	abstract protected void clearOnTranslationUpdate();
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Resets the fragment stack state to start. */
	private void resetFragmentToStart() {
		clearOnTranslationUpdate();
		
		getChildFragmentManager()
			.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); //clear backstack
		
		/*
		 * One cannot simply stay at the same fragment as the fragment will not know what
		 * id's to use for book, chapter, and verse fetching as they change based on version.
		 */
		
		//re-add
		addStartingFragment();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean onConsumeTabReset() {
		final int stackCount = getChildFragmentManager().getBackStackEntryCount();
		if (stackCount > 1) {
			resetFragmentToStart();
		}
		return false;
	}
	
	@Override
	public boolean onConsumeBackButton() {
		final int stackCount = getChildFragmentManager().getBackStackEntryCount();
		
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

	
	/* (non-Javadoc)
	 * @see com.inductivebiblestudyapp.ui.fragments.profile.bible.BibleChapterViewFragment.OnInteractionListener#onVerseDetails(com.inductivebiblestudyapp.data.model.BibleVerseResponse.Verse)
	 */
	@Override
	public void onVerseDetails(Verse verse) {
		final boolean upgradeComplete = UpgradeBillingManager.isUpgradeComplete(getActivity()); 
		if (upgradeComplete) {
			pushFragment(BibleVerseDetailsFragment.newInstance(verse), TAG_VERSE_VIEW_FRAG);
		} else {
			((OnUpgradeRequestListener) getActivity()).requestUpgrade();
		}
	}

	@Override
	public void onVerseCrossReference(String word) {
		pushFragment(NotesCrossReferenceDefintionFragment.newInstance(true), TAG_CROSS_REFERENCES_FRAG);
	}
	
	@Override
	public void onVerseDefinition(String word) {
		pushFragment(NotesCrossReferenceDefintionFragment.newInstance(false), TAG_DEFINTION_FRAG);
	}
	
	@Override
	public void onVerseStrongsNumber(StrongsNumberEntry strongsNumber) {
		pushFragment(WordStudyFragment.newInstance(strongsNumber), TAG_STRONGS_NUMBER);
	}
	
	@Override
	public void onWordStudyVerseCrossReference(CrossReference crossReference) {
		//first pop everything
		//popBackStackInclusive(TAG_CHAPTER_VIEW_FRAG); 
		
		//add our fragment.
		pushFragment(BibleChapterViewFragment.newInstance(
				getLoadStub(), 
				crossReference.getChapterId(),
				crossReference.getVerseId(),
				BibleChapterViewFragment.TYPE_VERSE), TAG_CHAPTER_VIEW_FRAG);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Translation update listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@Override
	public void onTranslationUpdate() {
		resetFragmentToStart();
	}
}
