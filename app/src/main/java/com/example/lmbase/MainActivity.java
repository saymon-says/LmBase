package com.example.lmbase;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

	private DrawerLayout drawerLayout;
	private NavigationView navigationView;
	private Toolbar mToolbar;
	private CircleImageView navUserpic;
	private TextView navUserName, ordersCount, deliveryCount, bayoutCount, pointCount, fifteenExactTimeToday, sixtyExactTimeToday;
	private CardView pointersToday, exactTime;
	private EditText fifteenExactTime, sixtyExactTime;

	private FirebaseAuth mAuth;
	private DatabaseReference pointerRef, usersRef, ordersCountRef, statisticRef;
	private String currentUserId, currentDateOrderList;
	private Integer resultPoint = 0, fifteenTime = 0, sixtyTime = 0;
	private Integer resultBuyout = 0, countOfOrders = 0, resultDelivery = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();

		Calendar calendarDate = Calendar.getInstance();
		final SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
		currentDateOrderList = currentDate.format(calendarDate.getTime());

		pointerRef = FirebaseDatabase.getInstance().getReference().child("Pointers List");
		usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
		ordersCountRef = FirebaseDatabase.getInstance().getReference().child("Order List").child(currentUserId).child(currentDateOrderList);
		statisticRef = FirebaseDatabase.getInstance().getReference().child("Statistic List").child(currentUserId);

		drawerLayout = findViewById(R.id.drawer_layout);
		navigationView = findViewById(R.id.nav);
		ordersCount = findViewById(R.id.orders_count);
		deliveryCount = findViewById(R.id.delivery_count);
		bayoutCount = findViewById(R.id.bayout_count);
		pointCount = findViewById(R.id.point_count);
		pointersToday = findViewById(R.id.pointers_today);
		exactTime = findViewById(R.id.exacts_time_today);
		fifteenExactTimeToday = findViewById(R.id.exact_time_fifteen_today);
		sixtyExactTimeToday = findViewById(R.id.exact_time_sixty_today);

		View navView = navigationView.inflateHeaderView(R.layout.header_nav);
		navUserName = navView.findViewById(R.id.nav_username);
		navUserpic = navView.findViewById(R.id.profile_image);

		mToolbar = findViewById(R.id.main_page_toolbar);
		setSupportActionBar(mToolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle("Статистика за день");
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawerLayout, mToolbar, R.string.open_navigation, R.string.close_navigation);
		drawerLayout.addDrawerListener(toggle);
		toggle.syncState();

		pointersToday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent sendOrderList = new Intent(MainActivity.this, OrderListActivity.class);
				startActivity(sendOrderList);
			}
		});

		exactTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowDialogExactTime();
			}
		});

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
			@SuppressLint("SetTextI18n")
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					for (DataSnapshot ds : dataSnapshot.getChildren()) {
						Map<String, Object> map = (Map<String, Object>) ds.getValue();
						Object delivery = map.get("delivery");
						int dValue = Integer.parseInt(String.valueOf(delivery));
						Object buyout = map.get("point");
						int bValue = Integer.parseInt(String.valueOf(buyout));
						resultDelivery += dValue;
						resultBuyout += bValue;
						deliveryCount.setText(String.valueOf(resultDelivery));
						bayoutCount.setText(String.valueOf(resultBuyout));
					}
					resultPoint = resultBuyout + resultDelivery;
					pointCount.setText(String.valueOf(resultPoint));

					countOfOrders = Math.toIntExact(dataSnapshot.getChildrenCount());
					ordersCount.setText(countOfOrders + "");
				} else {
					Toast.makeText(MainActivity.this, "Пока по нулям..", Toast.LENGTH_SHORT).show();
					ordersCount.setText("0");
					pointCount.setText("0");
					bayoutCount.setText("0");
					deliveryCount.setText("0");
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		statisticRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					fifteenExactTimeToday.setText(Objects.requireNonNull(dataSnapshot.child(currentDateOrderList)
							.child("15").getValue()).toString());
					sixtyExactTimeToday.setText(Objects.requireNonNull(dataSnapshot.child(currentDateOrderList)
							.child("60").getValue()).toString());
				} else {
					fifteenExactTimeToday.setText("0");
					sixtyExactTimeToday.setText("0");
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

	private void ShowDialogExactTime() {
		final AlertDialog.Builder exactTime = new AlertDialog.Builder(this);
		View mView = getLayoutInflater().inflate(R.layout.dialog_exact_time, null);

		fifteenExactTime = mView.findViewById(R.id.exact_time_fifteen);
		sixtyExactTime = mView.findViewById(R.id.exact_time_sixty);

		Button addExactTime = mView.findViewById(R.id.add_exact_time);
		Button cancelDialog = mView.findViewById(R.id.cancel_button);
		exactTime.setView(mView);

		final AlertDialog alertDialog = exactTime.create();
		alertDialog.setCanceledOnTouchOutside(false);
		Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


		cancelDialog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});

		addExactTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UpdateDataBaseExactTime();
				alertDialog.dismiss();
			}
		});
		alertDialog.show();
	}

	private void UpdateDataBaseExactTime() {

		if (fifteenExactTime.length() == 0) {
			fifteenTime = 0;
		} else {
			fifteenTime = Integer.valueOf(fifteenExactTime.getText().toString());
		}
		if (sixtyExactTime.length() == 0) {
			sixtyTime = 0;
		} else {
			sixtyTime = Integer.valueOf(sixtyExactTime.getText().toString());
		}

		HashMap statMap = new HashMap();
		statMap.put("15", fifteenTime);
		statMap.put("60", sixtyTime);
		statisticRef.child(currentDateOrderList).updateChildren(statMap).addOnSuccessListener(new OnSuccessListener() {
			@Override
			public void onSuccess(Object o) {
				Toast.makeText(MainActivity.this, "Готово..", Toast.LENGTH_SHORT).show();

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
				Toast.makeText(this, "Поехали...", Toast.LENGTH_SHORT).show();
				break;

			case R.id.nav_for_list:
				SendUserToUserListActivity();
				break;

			case R.id.nav_for_workshift:
				UpdateDataBaseWorkShift();
				break;

			case R.id.nav_statistics:
				SendUserToStatisticsActivity();
				break;
			case R.id.nav_update:
				UpdateDataBaseStatistics();
				break;

			case R.id.nav_settings:
				SendUserToSettingsActivity();
				break;

			case R.id.nav_logout:
				mAuth.signOut();
				SendUserToLoginActivity();
				break;
		}
	}

	private void UpdateDataBaseStatistics() {

		HashMap statMap = new HashMap();
		statMap.put("resultPoint", resultPoint);
		statMap.put("resultBuyout", resultBuyout);
		statMap.put("countOfOrders", countOfOrders);
		statisticRef.child(currentDateOrderList).updateChildren(statMap).addOnSuccessListener(new OnSuccessListener() {
			@Override
			public void onSuccess(Object o) {
				Toast.makeText(MainActivity.this, "Обновились", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void SendUserToStatisticsActivity() {
		Intent sendToStatistic = new Intent(this, StatisticsActivity.class);
		startActivity(sendToStatistic);
	}

	private void SendUserToSettingsActivity() {
		Intent sendToSettings = new Intent(this, SettingsActivity.class);
		startActivity(sendToSettings);
	}

	private void UpdateDataBaseWorkShift() {

		String resultPoint = pointCount.getText().toString();
		HashMap workshiftMap = new HashMap();
		workshiftMap.put("date", currentDateOrderList);
		workshiftMap.put("resultPoint", resultPoint);
		pointerRef.child(currentUserId).child(currentDateOrderList).updateChildren(workshiftMap).addOnSuccessListener(new OnSuccessListener() {
			@Override
			public void onSuccess(Object o) {
				SendUserToWorkshiftActivity();
			}
		});
	}

	private void SendUserToWorkshiftActivity() {
		Intent workshiiftIntent = new Intent(this, WorkShiftListActivity.class);
		startActivity(workshiiftIntent);
	}

	private void SendUserToUserListActivity() {
		Intent orderListIntent = new Intent(this, UserListActivity.class);
		startActivity(orderListIntent);
	}

	private void SendUserToOrderList() {
		Intent orderListIntent = new Intent(this, OrderListActivity.class);
		startActivity(orderListIntent);
	}
}
