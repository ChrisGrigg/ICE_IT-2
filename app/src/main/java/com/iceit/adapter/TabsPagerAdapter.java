package com.iceit.adapter;

import com.iceit.ActivationFragment;
import com.iceit.ContactsFragment;
import com.iceit.ProfileFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
		case 0:
			// Top Rated fragment activity
			return new ProfileFragment();
		case 1:
			// Games fragment activity
			return new ContactsFragment();
		case 2:
			// Movies fragment activity
			return new ActivationFragment();
		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 3;
	}

}
