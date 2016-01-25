package com.inductivebiblestudyapp.ui.fragments.profile.bible;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.AppCache.OnCacheUpdateListener;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse.Book;
import com.inductivebiblestudyapp.ui.adapters.BibleChapterListAdapter;

/**
 * A simple {@link Fragment} subclass. Use the {@link BibleChapterListFragment#newInstance}
 * factory method to create an instance of this fragment.
 * @version 0.3.2-20150728
 */
public class BibleChapterListFragment extends Fragment implements AdapterView.OnItemClickListener  {
	
	final static private String CLASS_NAME = BibleChapterListFragment.class
			.getSimpleName();
	
	private static final String ARG_BOOK_NAME = CLASS_NAME + ".ARG_BOOK_NAME";
	private static final String ARG_BOOK_ID = CLASS_NAME + ".ARG_BOOK_ID";
	

	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 */
	public static BibleChapterListFragment newInstance(Book book) {
		BibleChapterListFragment fragment = new BibleChapterListFragment();
		Bundle args = new Bundle();
		args.putString(ARG_BOOK_NAME, book.getName());
		args.putString(ARG_BOOK_ID, book.getBookId());
		fragment.setArguments(args);
		return fragment;
	}

	public BibleChapterListFragment() {
		// Required empty public constructor
	}
	
	private OnInteractionListener mListener = null;
		
	private BibleChapterListAdapter mChapterAdapter = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View  rootView = inflater.inflate(R.layout.fragment_bible_chapters_list, container, false);
		
		Bundle args = getArguments();
		String bookId = "";
		
		if (args != null) {
			((TextView) rootView.findViewById(R.id.ibs_bible_chapters_bookTitle))
				.setText(getString(R.string.ibs_title_bookOfFormat, args.getString(ARG_BOOK_NAME)));
			bookId = args.getString(ARG_BOOK_ID);
		}
		
		mChapterAdapter = new BibleChapterListAdapter(getActivity(), bookId);
		
		GridView textGridView = (GridView) rootView.findViewById(R.id.ibs_bible_chapters_gridview);
		textGridView.setAdapter(mChapterAdapter);
		textGridView.setOnItemClickListener(this);
		
		AppCache.addBibleVerseUpdateListener(mChapterAdapter);
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		AppCache.removeBibleVerseUpdateListener(mChapterAdapter);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		try {
			mListener = (OnInteractionListener) getParentFragment();
		} catch (ClassCastException e) {
			Log.e(getTag(), "Parent fragment must implement " + OnInteractionListener.class.getName());
			throw e;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Chapter chapter = mChapterAdapter.getItem(position);
		if (chapter != null) {
			mListener.onChapterSelect(chapter);
		}		
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal interface
	////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @version 0.2.0-20150625
	 */
	public static interface OnInteractionListener {
		/** Supplies the event that happened in the wrapper. */
		public void onChapterSelect(Chapter chapter);
	}
	

}
