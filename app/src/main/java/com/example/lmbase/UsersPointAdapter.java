package com.example.lmbase;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class UsersPointAdapter extends FragmentStateAdapter {
	public UsersPointAdapter(@NonNull FragmentActivity fragmentActivity) {
		super(fragmentActivity);
	}

	@NonNull
	@Override
	public Fragment createFragment(int position) {
		switch (position) {
			case 0:
				return new MonthPointFragment();
			case 1:
				return new TodayPointFragment();
		}
		return null;
	}

	@Override
	public int getItemCount() {
		return 2;
	}
}
