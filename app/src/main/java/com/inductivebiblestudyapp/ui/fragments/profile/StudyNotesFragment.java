package com.inductivebiblestudyapp.ui.fragments.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.StudyNotesResponse.StudyNoteItem;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.BibleChapterViewFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.notes.NotesListFragment;

/**
 * A simple {@link Fragment} subclass.
 * @author Jason Jenkins
 * @version 0.8.1-20150831
 */
public class StudyNotesFragment extends BibleParentFragment implements 
	NotesListFragment.OnInteractionListener {
	
	final static private String CLASS_NAME = StudyNotesFragment.class.getSimpleName();
	
	private static final String TAG_NOTES_LIST_FRAG = CLASS_NAME + ".TAG_NOTES_LIST_FRAG";
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public StudyNotesFragment() {
		// Required empty public constructor		
	}
	

	@Override
	protected View onInflateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_study_notes, container, false);
	}
	
	@Override
	protected int getLayoutReplaceId() {
		return R.id.ibs_notes_container;
	}
		
	@Override
	protected void addStartingFragment() {
		getChildFragmentManager()
        .beginTransaction()
        .addToBackStack(TAG_NOTES_LIST_FRAG) //required for onConsumeBackButton() to work
        .add(R.id.ibs_notes_container, NotesListFragment.newInstance(), TAG_NOTES_LIST_FRAG)
        .commit();
		mLastTag = TAG_NOTES_LIST_FRAG;		
	}	

	@Override
	protected String getLoadStub() {
		return getString(R.string.ibs_config_loadStub_notes);
	}
	
	@Override
	protected void clearOnTranslationUpdate() {
		AppCache.setsStudyNotesReponse(null);		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see com.inductivebiblestudyapp.ui.fragments.profile.notes.NotesListFragment.OnInteractionListener#onNoteClick(com.inductivebiblestudyapp.data.model.StudyNotesResponse.StudyNoteItem)
	 */
	@Override
	public void onNoteClick(StudyNoteItem noteItem) {
		int type = BibleChapterViewFragment.TYPE_CHAPTER_THEME;
		
		switch (noteItem.getItemType()) {
		case StudyNoteItem.TYPE_NOTE:
			type = BibleChapterViewFragment.TYPE_NOTE;
			break;
		case StudyNoteItem.TYPE_DIV_THEME:
			type = BibleChapterViewFragment.TYPE_DIV_THEME;
			break;
		case StudyNoteItem.TYPE_MARKING_LETTERING:
		case StudyNoteItem.TYPE_MARKING_IMAGE:
			type = BibleChapterViewFragment.TYPE_MARKING;
			break;
			
		case StudyNoteItem.TYPE_CHAPTER_THEME:
		default:
			break;
		}
		
		pushFragment(	BibleChapterViewFragment.newInstance(
							getString(R.string.ibs_config_loadStub_notes), 
							noteItem.getParentChapterId(),
							noteItem.getItemId(), 
							type),
						TAG_CHAPTER_VIEW_FRAG);
						
	}


}
