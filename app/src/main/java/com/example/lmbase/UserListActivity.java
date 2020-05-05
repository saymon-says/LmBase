package com.example.lmbase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

public class UserListActivity extends AppCompatActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_list);

		Toolbar mToolbar = findViewById(R.id.users_page_toolbar);
		setSupportActionBar(mToolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle("Список торговых представителей");

		ViewPager2 viewPager2 = findViewById(R.id.view_pager);
		viewPager2.setAdapter(new UsersPointAdapter(this));

		TabLayout tabLayout = findViewById(R.id.table_layout);
		TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
				tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
			@Override
			public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
				switch (position) {
					case 0:
						tab.setText("Месяц");
						break;
					case 1:
						tab.setText("День");
						break;
				}
			}
		}
		);
		tabLayoutMediator.attach();
	}
}