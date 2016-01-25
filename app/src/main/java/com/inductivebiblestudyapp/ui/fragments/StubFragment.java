package com.inductivebiblestudyapp.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;

/**
 * A simple {@link Fragment} subclass. Use the {@link StubFragment#newInstance}
 * factory method to create an instance of this fragment.
 * 
 */
public class StubFragment extends Fragment {
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";

	

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param param1
	 *            Parameter 1.
	 * @param param2
	 *            Parameter 2.
	 * @return A new instance of fragment StubFragment.
	 */
	public static StubFragment newInstance(String param1) {
		StubFragment fragment = new StubFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		fragment.setArguments(args);
		return fragment;
	}

	public StubFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View  rootView = inflater.inflate(R.layout.fragment_stub, container, false);
		if (getArguments() != null) {
			((TextView) rootView.findViewById(R.id.container))
				.setText(getArguments().getString(ARG_PARAM1));
		}
		// Inflate the layout for this fragment
		return rootView;
	}

}
