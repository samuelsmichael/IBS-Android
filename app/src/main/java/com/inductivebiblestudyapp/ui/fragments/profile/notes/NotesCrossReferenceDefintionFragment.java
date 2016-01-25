package com.inductivebiblestudyapp.ui.fragments.profile.notes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.ui.adapters.BasicTextAdapter;

/**
 * A simple {@link Fragment} subclass. Use the {@link NotesCrossReferenceDefintionFragment#newInstance}
 * factory method to create an instance of this fragment.
 * @author Jason Jenkins
 * @version 0.1.0-20150617
 */
public class NotesCrossReferenceDefintionFragment extends Fragment {
	final static private String CLASS_NAME = NotesCrossReferenceDefintionFragment.class
			.getSimpleName();

	private static final String ARG_CROSS_REFERENCE_MODE = CLASS_NAME + ".ARG_CROSS_REFERENCE_MODE";
	
	//This class may be removed if no longer needed (which I believe is the case)
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 */
	public static NotesCrossReferenceDefintionFragment newInstance(boolean crossReferenceMode) {
		NotesCrossReferenceDefintionFragment fragment = new NotesCrossReferenceDefintionFragment();
		Bundle args = new Bundle();
		args.putBoolean(ARG_CROSS_REFERENCE_MODE, crossReferenceMode);
		fragment.setArguments(args);
		return fragment;
	}

	public NotesCrossReferenceDefintionFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View  rootView = inflater.inflate(R.layout.fragment_notes_view_definition, container, false);
		Bundle args = getArguments();
		if (args != null) {
			if (args.getBoolean(ARG_CROSS_REFERENCE_MODE)) {
				((TextView) rootView.findViewById(R.id.ibs_notes_defintion_title))
					.setText(R.string.ibs_label_crossReferences);
			}			
		}
		
		String[] defintions = new String[10];
		for (int index = 0; index < 10; index++){
			defintions[index] = getString(R.string.ibs_lorem_ipsum);
		}
		
		ListView list = (ListView) rootView.findViewById(R.id.ibs_studyNotes_definition_listview);		
		list.setAdapter(new BasicTextAdapter(getActivity(), R.layout.list_item_text, defintions));
		
		// Inflate the layout for this fragment
		return rootView;
	}

}
