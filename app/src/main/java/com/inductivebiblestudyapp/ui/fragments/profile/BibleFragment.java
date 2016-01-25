package com.inductivebiblestudyapp.ui.fragments.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse.Book;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.BibleBookListFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.BibleChapterListFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.BibleChapterViewFragment;

/**
 * A simple {@link Fragment} subclass.
 * @author Jason Jenkins
 * @version 0.14.1-20150831
 */
public class BibleFragment extends BibleParentFragment implements BibleBookListFragment.OnInteractionListener,
	BibleChapterListFragment.OnInteractionListener {
	
	final static private String CLASS_NAME = BibleFragment.class.getSimpleName();
	
	private static final String TAG_BOOK_LIST_FRAG = CLASS_NAME + ".TAG_BOOK_LIST_FRAG";
	private static final String TAG_CHAPTER_LIST_FRAG = CLASS_NAME + ".TAG_CHAPTER_LIST_FRAG";

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////

	public BibleFragment() {
		// Required empty public constructor
	}
	
	@Override
	protected View onInflateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_bible, container, false);
	}
	

	@Override
	protected void addStartingFragment() {
		getChildFragmentManager()
        .beginTransaction()
        .addToBackStack(TAG_BOOK_LIST_FRAG) //required for onConsumeBackButton() to work
        .add(R.id.ibs_bible_container, BibleBookListFragment.newInstance(), TAG_BOOK_LIST_FRAG)
        .commit();

		mLastTag = TAG_BOOK_LIST_FRAG;	
	}
	
	@Override
	protected int getLayoutReplaceId() {
		return R.id.ibs_bible_container;
	}
	
	
	@Override
	protected String getLoadStub() {
		return getString(R.string.ibs_config_loadStub_bible);
	}
	
	@Override
	protected void clearOnTranslationUpdate() {
		AppCache.clearBibleResponses();		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// BibleBookList listeners start here
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onBibleBookSelect(Book bookSelected) {		
		pushFragment(BibleChapterListFragment.newInstance(bookSelected), TAG_CHAPTER_LIST_FRAG);
	}
	
	@Override
	public void onBibleChapterSelect(String chapterId) {
		BibleChapterViewFragment frag = 
				BibleChapterViewFragment.newInstance(
						getString(R.string.ibs_config_loadStub_bible), 
						chapterId, 
						null, 
						BibleChapterViewFragment.TYPE_NONE, true);
		pushFragment(frag, TAG_CHAPTER_VIEW_FRAG);
	}
	
	@Override
	public void onBibleVerseSelect(String chapterId, String verseId) {
		BibleChapterViewFragment frag = 
				BibleChapterViewFragment.newInstance(
						getString(R.string.ibs_config_loadStub_bible), 
						chapterId, 
						verseId, 
						BibleChapterViewFragment.TYPE_VERSE, true);
		pushFragment(frag, TAG_CHAPTER_VIEW_FRAG);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// BibleBookList listeners end here
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/* (non-Javadoc)
	 * @see com.inductivebiblestudyapp.ui.fragments.profile.bible.BibleChapterListFragment.OnInteractionListener#onChapterSelect(com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter)
	 */
	@Override
	public void onChapterSelect(Chapter chapter) {		
		pushFragment(BibleChapterViewFragment.newInstance(
				getString(R.string.ibs_config_loadStub_bible), chapter), 
				TAG_CHAPTER_VIEW_FRAG);
	}	
}
