package com.iceit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ActivationFragment extends Fragment {

    private String storedFullName;
    private String storedContactsName;
    private String storedContactsNumber;

    //TODO: loading of this Fragment needs to be right
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_activation, container, false);

        getDBData();

		// TODO: check if all data exists, if none exists don't allow
		return rootView;
	}

    private void getDBData() {
        SharedPreferences fullNameFile = getActivity().getSharedPreferences(ProfileFragment.PROFILE_FILE, 0);
        storedFullName = fullNameFile.getString("fullName", "");

        SharedPreferences contactsFile = getActivity().getSharedPreferences(ContactsFragment.CONTACTS_FILE, 0);
        storedContactsName = contactsFile.getString("contactName", "");
        storedContactsNumber = contactsFile.getString("contactNumber", "");
    }
}
