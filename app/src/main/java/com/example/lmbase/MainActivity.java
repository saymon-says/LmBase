package com.example.lmbase;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

	private DrawerLayout drawerLayout;
	private NavigationView navigationView;
	private Toolbar mToolbar;
	private CircleImageView navUserpic;
	private TextView navUserName, ordersCount, workshiftCount;

	private FirebaseAuth mAuth;
	private DatabaseReference usersRef, ordersRef, ordersCountRef, workshiftCountRef;
	private String currentUserId, currentDateOrderList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();

		Calendar calendarDate = Calendar.getInstance();
		SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
		currentDateOrderList = currentDate.format(calendarDate.getTime());

		usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
		ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders List");
		ordersCountRef = FirebaseDatabase.getInstance().getReference().child("Order List").child(currentUserId).child(currentDateOrderList);
		workshiftCountRef = FirebaseDatabase.getInstance().getReference().child("Order List").child(currentUserId);

		drawerLayout = findViewById(R.id.drawer_layout);
		navigationView = findViewById(R.id.nav);
		ordersCount = findViewById(R.id.orders_count);
		workshiftCount = findViewById(R.id.workshift_count);

		View navView = navigationView.inflateHeaderView(R.layout.heade_nav);
		navUserName = navView.findViewById(R.id.nav_username);
		navUserpic = navView.findViewById(R.id.profile_image);

		mToolbar = findViewById(R.id.main_page_toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle("Домашняя страница");
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawerLayout, mToolbar, R.string.open_navigation, R.string.close_navigation);
		drawerLayout.addDrawerListener(toggle);
		toggle.syncState();

		usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					if (dataSnapshot.hasChild("fullname")) {
						String fullname = dataSnapshot.child("fullname").getValue().toString();
						navUserName.setText(fullname);
					} else {
						Toast.makeText(MainActivity.this, "Нужно добавить ФИО", Toast.LENGTH_LONG).show();
					}
					if (dataSnapshot.hasChild("userpic")) {
						String userPic = dataSnapshot.child("userpic").getValue().toString();
						Picasso.get().load(userPic).placeholder(R.drawable.anonymous).into(navUserpic);
					} else {
						Toast.makeText(MainActivity.this, "Добавь аватар", Toast.LENGTH_LONG).show();
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		ordersCountRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					String countOfOrders = String.valueOf(dataSnapshot.getChildrenCount());
					ordersCount.setText(countOfOrders);
				} else {
					Toast.makeText(MainActivity.this, "Заказов еще нет", Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		workshiftCountRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					String countOfWorkShift = String.valueOf(dataSnapshot.getChildrenCount());
					workshiftCount.setText(countOfWorkShift);
				} else {
					Toast.makeText(MainActivity.this, "В этом месяце нет смен", Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				UserMenuSelector(item);
				return false;
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		FirebaseUser currentUser = mAuth.getCurrentUser();
		if (currentUser == null) {
			SendUserToLoginActivity();
		} else {
			CheckUserInBase();
		}
	}

	@Override
	public void onBackPressed() {
		if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
			drawerLayout.closeDrawer(GravityCompat.START);
		} else {
		}
	}

	private void CheckUserInBase() {
		final String currentUserId = mAuth.getCurrentUser().getUid();
		usersRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (!dataSnapshot.hasChild(currentUserId)) {
					SendUserToSetupActivity();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void SendUserToSetupActivity() {
		Intent setupActivity = new Intent(MainActivity.this, SetupActivity.class);
		startActivity(setupActivity);
		finish();
	}

	private void SendUserToLoginActivity() {

		Intent loginActivity = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(loginActivity);
		finish();
	}

	private void UserMenuSelector(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.nav_add_workshift:
				SendUserToOrderList();
				Toast.makeText(this, "Поехали...", Toast.LENGTH_LONG).show();
				break;

			case R.id.nav_tor_list:
				SendUserToUserListActivity();
				Toast.makeText(this, "Список торговых", Toast.LENGTH_LONG).show();
				break;

			case R.id.nav_static:
				Toast.makeText(this, "Статистика", Toast.LENGTH_LONG).show();
				break;

			case R.id.nav_logout:
				mAuth.signOut();
				SendUserToLoginActivity();
				break;
		}
	}

	private void SendUserToUserListActivity() {
		Intent orderListIntent = new Intent(MainActivity.this, UserListActivity.class);
		startActivity(orderListIntent);
	}

	private void SendUserToOrderList() {
		Intent orderListIntent = new Intent(MainActivity.this, OrderListActivity.class);
		startActivity(orderListIntent);
	}
}
