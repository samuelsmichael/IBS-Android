package com.inductivebiblestudyapp.ui.fragments.profile.markings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.ui.adapters.LetteringFetchRecyclerAdapter;
import com.inductivebiblestudyapp.ui.adapters.LetteringFetchRecyclerAdapter.OnAdapterStateListener;
import com.inductivebiblestudyapp.ui.adapters.LetteringFetchRecyclerAdapter.OnLetteringItemClickListener;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A simple {@link Fragment} subclass. Use the {@link MarkingsViewAllLetteringsFragment#newInstance}
 * factory method to create an instance of this fragment.
 * @version 0.4.1-20150831
 */
public class MarkingsViewAllLetteringsFragment extends Fragment implements OnClickListener, 
	OnLetteringItemClickListener, OnAdapterStateListener, TextWatcher, OnEditorActionListener,
	OnCheckedChangeListener   {
	
	final static private String CLASS_NAME = MarkingsViewAllLetteringsFragment.class
			.getSimpleName();
	
	private static final String LOGTAG = CLASS_NAME;
	
	
	/** The delay before performing filtering */
	private static final int DELAY_USER_TYPING = 200; //ms
	/** The what to post to query. */
	private static final int HANDLER_WHAT_QUERY = 0;
	
	/** The id of the default sort to reset to. */
	private static final int DEFAULT_SORT_ID = R.id.ibs_sortBy_radioOption_byName;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 */
	public static MarkingsViewAllLetteringsFragment newInstance() {
		MarkingsViewAllLetteringsFragment fragment = new MarkingsViewAllLetteringsFragment();
		return fragment;
	}

	public MarkingsViewAllLetteringsFragment() {
		// Required empty public constructor
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private EditText mSearchInput = null;
	private RadioGroup mSortByRadioGroup = null;
	private View mSortContainer = null;
		
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views
	////////////////////////////////////////////////////////////////////////////////////////////////

	private OnInteractionListener mListener =  null;
	
	private GridLayoutManager mLetteringsContainer = null;
	private LetteringFetchRecyclerAdapter mLetteringsAdapter = null;
	
	private int mSpanCount = 3; //TODO refactor 3
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View  rootView = inflater.inflate(R.layout.fragment_markings_viewall, container, false);
		

		initViews(rootView);
		if (savedInstanceState == null) { //first run
			mSortByRadioGroup.check(DEFAULT_SORT_ID);
		} else {
			//initialize the sort.
			onCheckedChanged(mSortByRadioGroup, mSortByRadioGroup.getCheckedRadioButtonId());
		}
		
		AppCache.addLetteringListUpdateListener(mLetteringsAdapter);
		// Inflate the layout for this fragment
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		AppCache.removeLetteringListUpdateListener(mLetteringsAdapter);
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
	//// Initializer methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes all views. */
	private void initViews(View rootView) {
		mSearchInput = (EditText) rootView.findViewById(R.id.ibs_custom_markings_viewall_search);
		mSearchInput.setOnEditorActionListener(this);
		mSearchInput.addTextChangedListener(this);
		
		mLetteringsAdapter = new LetteringFetchRecyclerAdapter(getActivity());
		
		RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.ibs_custom_markings_viewall_recyclerview);
		// use a grid layout manager
		mLetteringsContainer  = new GridLayoutManager(getActivity(), mSpanCount); 
        recyclerView.setLayoutManager(mLetteringsContainer);
		recyclerView.setAdapter(mLetteringsAdapter);
		//view.setOnScrollListener(mImageAdapter.getScrollListener()); //does this need to be fixed?
		
		mLetteringsAdapter.setOnLetteringItemClickListener(this);
		mLetteringsAdapter.setOnAdapterStateListener(this);
		
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_button_viewall_create, this);
		
		mSortContainer = rootView.findViewById(R.id.ibs_custom_markings_viewall_sortby_container);
		
		mSortByRadioGroup = (RadioGroup) rootView.findViewById(R.id.ibs_sortBy_radiogroup);
		mSortByRadioGroup.setOnCheckedChangeListener(this);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onClick(View v) {
		mListener.onCreateNewLettering();
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.ibs_sortBy_radioOption_byName:
			mLetteringsAdapter.setSortMode(LetteringFetchRecyclerAdapter.SORT_NAME_ASC);
			break;
			
		case R.id.ibs_sortBy_radioOption_byRecent:
			mLetteringsAdapter.setSortMode(LetteringFetchRecyclerAdapter.SORT_RECENT_ASC);
			break;

		default:
			Log.w(CLASS_NAME, "Unspecified action: " + checkedId);
			break;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see android.widget.TextView.OnEditorActionListener#onEditorAction(android.widget.TextView, int, android.view.KeyEvent)
	 */
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			final String searchTerm =   mSearchInput.getText().toString().trim();
			Log.d(LOGTAG, "searching: " + searchTerm);			
			mSearchHandler.sendEmptyMessage(HANDLER_WHAT_QUERY); //perform search
			return true;
		}
		return false;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start text watcher
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The search handler to perform filtering in. */
	private final Handler mSearchHandler = new Handler(new Handler.Callback() {		
		@Override
		public boolean handleMessage(Message msg) {
			if (mSearchInput != null) {
				String filterString = mSearchInput.getText().toString().trim();
				mLetteringsAdapter.filter(filterString);
				return true;
			}
			return false;
		}
	});
	


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.length() > 0) {
			mSortContainer.setVisibility(View.VISIBLE);
		} else {
			mSortContainer.setVisibility(View.GONE);
			mSortByRadioGroup.check(DEFAULT_SORT_ID);
		}
		mSearchHandler.removeMessages(HANDLER_WHAT_QUERY);
		mSearchHandler.sendEmptyMessageDelayed(HANDLER_WHAT_QUERY, DELAY_USER_TYPING);
	}
	

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Object listeners
	////////////////////////////////////////////////////////////////////////////////////////////////


	@Override
	public void onLetteringItemClick(LetteringItem item) {
		mListener.onLetteringClick(item);
	}
	
	@Override
	public void onStateUpdate(int state) {
		switch (state) {
		case OnAdapterStateListener.STATE_OK:
			mLetteringsContainer.setSpanCount(mSpanCount);
			break;
		case OnAdapterStateListener.STATE_EMPTY:			
		case OnAdapterStateListener.STATE_CANNOT_CONNECT:
			mLetteringsContainer.setSpanCount(1);
			break;

		}
	}		
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal interface
	////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @version 0.2.0-20150715
	 */
	public static interface OnInteractionListener {
		public void onLetteringClick(LetteringItem data);
		
		public void onCreateNewLettering();
	}

}
