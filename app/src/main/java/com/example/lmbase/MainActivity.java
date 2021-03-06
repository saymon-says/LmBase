package com.example.lmbase;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity";
	private DrawerLayout drawerLayout;
	private CircleImageView navUserpic;
	private TextView navUserName, ordersCount, deliveryCount, buyoutCount,
			pointCount, fifteenExactTimeToday, sixtyExactTimeToday, upFineToday,
			mWorkShiftTextView, mUsersTextView, mClientMapsTextView;
	private EditText fifteenExactTime, sixtyExactTime, fines, benetonCount, workshiftAdd;
	private FirebaseAuth mAuth;
	private DatabaseReference pointerRef;
	private DatabaseReference usersRef;
	private DatabaseReference statisticRef;
	private String currentUserId, currentDateOrderList;
	private Integer fifteenTime = 0;
	private Integer sixtyTime = 0;
	private Integer finesToday = 0;
	private Integer resultBuyout = 0, countOfOrders = 1, resultDelivery = 0, countWorkShifts = 0, countUsers = 0, countClient = 0;
	private Integer benetonCounts = 0;
	private Integer addedWorkshifts = 0;
	private double pointFinesToday = 0.0, resultPoint = 0.0;
	private int currentMonth, currentYear;
	private String firstDateStart, firstDateEnd, secondDateStart, secondDateEnd;
	private String date1, date2;
	private String firstMonthStart, firstMonthEnd, secondMonthEnd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

		Calendar calendarDate = Calendar.getInstance();
		currentMonth = calendarDate.get(Calendar.MONTH);
		currentYear = calendarDate.get(Calendar.YEAR);
		@SuppressLint("SimpleDateFormat") final SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
		currentDateOrderList = currentDate.format(calendarDate.getTime());

		pointerRef = FirebaseDatabase.getInstance().getReference().child("Pointers List");
		usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
		DatabaseReference troubleClientRef = FirebaseDatabase.getInstance().getReference().child("Trouble Client List");
		DatabaseReference ordersCountRef = FirebaseDatabase.getInstance().getReference().child("Order List").child(currentUserId).child(currentDateOrderList);
		statisticRef = FirebaseDatabase.getInstance().getReference().child("Statistic List").child(currentUserId);

		drawerLayout = findViewById(R.id.drawer_layout);
		NavigationView navigationView = findViewById(R.id.nav);
		mWorkShiftTextView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
				findItem(R.id.nav_for_workshift));
		mClientMapsTextView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
				findItem(R.id.nav_for_google_maps));
		mUsersTextView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
				findItem(R.id.nav_for_list));

		ordersCount = findViewById(R.id.text_orders_count);
		deliveryCount = findViewById(R.id.text_delivery_count);
		buyoutCount = findViewById(R.id.text_bayout_count);
		pointCount = findViewById(R.id.text_point_count);
		CardView pointersToday = findViewById(R.id.pointers_today);
		CardView exactTime = findViewById(R.id.exacts_time_today);
		CardView fineToday = findViewById(R.id.fines_today);
		fifteenExactTimeToday = findViewById(R.id.text_exact_time_fifteen_today);
		sixtyExactTimeToday = findViewById(R.id.text_exact_time_sixty_today);
		upFineToday = findViewById(R.id.text_fine_today);

		View navView = navigationView.inflateHeaderView(R.layout.header_nav);
		navUserName = navView.findViewById(R.id.nav_username);
		navUserpic = navView.findViewById(R.id.profile_image);
		ImageButton settingProfile = navView.findViewById(R.id.nav_setting_profile);

		Toolbar mToolbar = findViewById(R.id.main_page_toolbar);
		setSupportActionBar(mToolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle("Статистика за день");
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawerLayout, mToolbar, R.string.open_navigation, R.string.close_navigation);
		drawerLayout.addDrawerListener(toggle);
		toggle.syncState();

		try {
			CreateDateRange();
		} catch (ParseException e) {
			e.printStackTrace();
		}

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

		fineToday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowDialogFine();
			}
		});

		settingProfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SendUserToSettingsActivity();
			}
		});

		pointerRef.child(currentUserId).orderByKey().startAt(date1).endAt(date2).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					countWorkShifts = Math.toIntExact(dataSnapshot.getChildrenCount());
				}
				initializeCountDrawerWS();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		statisticRef.addValueEventListener(new ValueEventListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.child(currentDateOrderList).exists()) {
					fifteenTime = Integer.valueOf(Objects.requireNonNull(dataSnapshot.child(currentDateOrderList)
							.child("15").getValue()).toString());
					fifteenExactTimeToday.setText(fifteenTime + "");

					sixtyTime = Integer.valueOf(Objects.requireNonNull(dataSnapshot.child(currentDateOrderList)
							.child("60").getValue()).toString());
					sixtyExactTimeToday.setText(sixtyTime + "");

					finesToday = Integer.valueOf(Objects.requireNonNull(dataSnapshot.child(currentDateOrderList)
							.child("fines").getValue()).toString());
					upFineToday.setText(finesToday + "");
				} else {
					fifteenExactTimeToday.setText("0");
					sixtyExactTimeToday.setText("0");
					upFineToday.setText("0");
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
						assert map != null;
						Object delivery = map.get("delivery");
						int dValue = Integer.parseInt(String.valueOf(delivery));
						Object buyout = map.get("point");
						int bValue = Integer.parseInt(String.valueOf(buyout));
						resultDelivery += dValue;
						resultBuyout += bValue;
						deliveryCount.setText(String.valueOf(resultDelivery));
						buyoutCount.setText(String.valueOf(resultBuyout));
					}
					pointFinesToday = (2.5 - finesToday) * 7;
					resultPoint = resultBuyout + resultDelivery + pointFinesToday;
					pointCount.setText(String.valueOf(resultPoint));

					countOfOrders = Math.toIntExact(dataSnapshot.getChildrenCount());
					ordersCount.setText(countOfOrders + "");
				} else {
					ordersCount.setText("0");
					pointCount.setText("0");
					buyoutCount.setText("0");
					deliveryCount.setText("0");
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		troubleClientRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					countClient = Math.toIntExact(dataSnapshot.getChildrenCount());
				}
				initializeCountDrawerCli();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		usersRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					countUsers = Math.toIntExact(dataSnapshot.getChildrenCount());
				}
				initializeCountDrawerUs();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					if (dataSnapshot.hasChild("beneton")) {
						benetonCounts = Integer.valueOf(Objects.requireNonNull(dataSnapshot.child("beneton").getValue()).toString());
					}
					if (dataSnapshot.hasChild("addedWorkshift")) {
						addedWorkshifts = Integer.valueOf(Objects.requireNonNull(dataSnapshot.child("addedWorkshift").getValue()).toString());
					}
					if (dataSnapshot.hasChild("fullname")) {
						String fullname = Objects.requireNonNull(dataSnapshot.child("fullname").getValue()).toString();
						navUserName.setText(fullname);
					} else {
						Toast.makeText(MainActivity.this, "Нужно добавить ФИО", Toast.LENGTH_LONG).show();
					}
					if (dataSnapshot.hasChild("userpic")) {
						String userPic = Objects.requireNonNull(dataSnapshot.child("userpic").getValue()).toString();
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


		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				UserMenuSelector(item);
				return false;
			}
		});
	}

	@SuppressLint("SetTextI18n")
	private void initializeCountDrawerCli() {
		mClientMapsTextView.setGravity(Gravity.CENTER_VERTICAL);
		mClientMapsTextView.setTypeface(null, Typeface.BOLD);
		mClientMapsTextView.setTextSize(14);
		mClientMapsTextView.setText(countClient + "");
	}

	@SuppressLint("SetTextI18n")
	private void initializeCountDrawerUs() {
		mUsersTextView.setGravity(Gravity.CENTER_VERTICAL);
		mUsersTextView.setTypeface(null, Typeface.BOLD);
		mUsersTextView.setTextSize(14);
		mUsersTextView.setText(countUsers + "");
	}

	@SuppressLint("SetTextI18n")
	private void initializeCountDrawerWS() {
		mWorkShiftTextView.setGravity(Gravity.CENTER_VERTICAL);
		mWorkShiftTextView.setTypeface(null, Typeface.BOLD);
		mWorkShiftTextView.setTextSize(14);
		mWorkShiftTextView.setText(countWorkShifts + "");
	}

	private void CreateDateRange() throws ParseException {

		@SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");

		if (0 < currentMonth && currentMonth < 10) { //1
			firstMonthStart = "0" + currentMonth; //02
			firstMonthEnd = "0" + (currentMonth + 1);//03
			secondMonthEnd = "0" + (currentMonth + 2); //04
		} else if (currentMonth == 0) { //0
			firstMonthStart = "12"; //12
			firstMonthEnd = "0" + (currentMonth + 1);//01
			secondMonthEnd = "0" + (currentMonth + 2); //02
		} else if (currentMonth == 11) {//11
			firstMonthStart = currentMonth + ""; //11
			firstMonthEnd = "" + (currentMonth + 1);//12
			secondMonthEnd = "01"; //01
		} else { //10
			firstMonthStart = "" + currentMonth; //10
			firstMonthEnd = "" + (currentMonth + 1);//03
			secondMonthEnd = "" + (currentMonth + 2); //04
		}

		firstDateStart = currentYear + "-" + firstMonthStart + "-" + 26; //2020-4-26
		firstDateEnd = currentYear + "-" + firstMonthEnd + "-" + 25; //2020-5-25
		secondDateStart = currentYear + "-" + firstMonthEnd + "-" + 26; //2020-5-26
		secondDateEnd = currentYear + "-" + secondMonthEnd + "-" + 25; //2020-6-25

		Date firstDateEndRange = currentDate.parse(firstDateEnd);
		Date currentDateWorkShift = currentDate.parse(currentDateOrderList); //2020-05-24

		if (Objects.requireNonNull(firstDateEndRange).before(currentDateWorkShift)
				|| firstDateEndRange.equals(currentDateWorkShift)) {
			date1 = secondDateStart;
			date2 = secondDateEnd;
		} else {
			date1 = firstDateStart;
			date2 = firstDateEnd;
		}
	}

	private void ShowDialogFine() {
		final AlertDialog.Builder fine = new AlertDialog.Builder(this);
		@SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.dialog_fine, null);

		fines = mView.findViewById(R.id.text_fine_today);
		fines.setText(String.valueOf(finesToday));
		Button addFines = mView.findViewById(R.id.add_fines);
		Button cancelDialog = mView.findViewById(R.id.cancel_button);
		fine.setView(mView);

		final AlertDialog alertDialog = fine.create();
		alertDialog.setCanceledOnTouchOutside(false);
		Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

		cancelDialog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});

		addFines.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (fines.length() == 0) {
					finesToday = 0;
				} else {
					finesToday = Integer.valueOf(fines.getText().toString());
				}
				UpdateDataBaseStatistics();
				SendUserToMainActivity();
				alertDialog.dismiss();
			}
		});

		alertDialog.show();

	}

	private void ShowDialogExactTime() {
		final AlertDialog.Builder exactTime = new AlertDialog.Builder(this);
		@SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.dialog_exact_time, null);

		fifteenExactTime = mView.findViewById(R.id.exact_time_fifteen);
		sixtyExactTime = mView.findViewById(R.id.exact_time_sixty);
		fifteenExactTime.setText(String.valueOf(fifteenTime));
		sixtyExactTime.setText(String.valueOf(sixtyTime));

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
				UpdateDataBaseStatistics();
				alertDialog.dismiss();
			}
		});
		alertDialog.show();
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
		final String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
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
				break;

			case R.id.nav_for_list:
				SendUserToUserListActivity();
				break;

			case R.id.nav_for_workshift:
				SendUserToWorkShiftActivity();
				break;

			case R.id.nav_statistics:
				SendUserToStatisticsActivity();
				break;
			case R.id.nav_update:
				UpdateDataBaseStatistics();
				UpdateDataBaseUsers();
				SendUserToMainActivity();
				Toast.makeText(this, "Обновлено", Toast.LENGTH_SHORT).show();
				break;

			case R.id.nav_for_google_maps:
				SendUserToMapsActivity();
				break;

			case R.id.nav_settings:
				ShowDialogSurchargesUser();
				break;

			case R.id.nav_logout:
				mAuth.signOut();
				SendUserToLoginActivity();
				break;
		}
	}

	@SuppressLint("SetTextI18n")
	private void ShowDialogSurchargesUser() {
		final AlertDialog.Builder surcharges = new AlertDialog.Builder(this);
		@SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.dialog_surcharges_user, null);

		benetonCount = mView.findViewById(R.id.beneton_counts);
		workshiftAdd = mView.findViewById(R.id.workshift_added);
		benetonCount.setText(benetonCounts + "");
		workshiftAdd.setText(addedWorkshifts + "");

		Button addSurcharges = mView.findViewById(R.id.accept);
		Button cancelDialog = mView.findViewById(R.id.cancel);
		surcharges.setView(mView);

		final AlertDialog alertDialog = surcharges.create();
		alertDialog.setCanceledOnTouchOutside(false);
		Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

		cancelDialog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "Бывает", Toast.LENGTH_SHORT).show();
				alertDialog.dismiss();
			}
		});

		addSurcharges.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				benetonCounts = Integer.parseInt(benetonCount.getText().toString());
				addedWorkshifts = Integer.valueOf(workshiftAdd.getText().toString());
				UpdateDataBaseUsers();
				Toast.makeText(MainActivity.this, "Казна пополняется, милорд", Toast.LENGTH_SHORT).show();
				alertDialog.dismiss();
			}
		});

		alertDialog.show();
	}

	private void SendUserToMapsActivity() {
		Intent mapsIntent = new Intent(this, MapsActivity.class);
		startActivity(mapsIntent);
	}

	private void UpdateDataBaseUsers() {
		statisticRef.orderByKey().startAt(date1).endAt(date2).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					double resultPointMonth = 0;
					double resultPointToday = Double.parseDouble(Objects.requireNonNull(dataSnapshot.child(currentDateOrderList)
							.child("resultPoint").getValue()).toString());
					for (DataSnapshot ds : dataSnapshot.getChildren()) {
						Map<String, Object> map = (Map<String, Object>) ds.getValue();
						assert map != null;
						Object resultPoint = map.get("resultPoint");
						double rValue = Double.parseDouble(String.valueOf(resultPoint));
						resultPointMonth += rValue;
					}
					HashMap statMap = new HashMap();
					statMap.put("resultMonthPoint", resultPointMonth);
					statMap.put("resultPointToday", resultPointToday);
					statMap.put("beneton", benetonCounts);
					statMap.put("addedWorkshift", addedWorkshifts);
					usersRef.child(currentUserId).updateChildren(statMap);
				} else {
					Toast.makeText(MainActivity.this, "Пока нет ниче", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}

	private void SendUserToMainActivity() {
		Intent mainActivity = new Intent(this, MainActivity.class);
		startActivity(mainActivity);
	}

	private void UpdateDataBaseStatistics() {
		HashMap statMap = new HashMap();
		statMap.put("resultPoint", resultPoint);
		statMap.put("resultBuyout", resultBuyout);
		statMap.put("countOfOrders", countOfOrders);
		statMap.put("15", fifteenTime);
		statMap.put("60", sixtyTime);
		statMap.put("fines", finesToday);
		statisticRef.child(currentDateOrderList).updateChildren(statMap);

		double resultPointToday = Double.parseDouble(pointCount.getText().toString());
		HashMap workshiftMap = new HashMap();
		workshiftMap.put("date", currentDateOrderList);
		workshiftMap.put("resultPoint", resultPointToday);
		pointerRef.child(currentUserId).child(currentDateOrderList).updateChildren(workshiftMap);
	}

	private void SendUserToStatisticsActivity() {
		Intent sendToStatistic = new Intent(this, StatisticsActivity.class);
		startActivity(sendToStatistic);
	}

	private void SendUserToSettingsActivity() {
		Intent sendToSettings = new Intent(this, SettingsActivity.class);
		startActivity(sendToSettings);
	}

	private void SendUserToWorkShiftActivity() {
		Intent workshiftIntent = new Intent(this, WorkShiftListActivity.class);
		startActivity(workshiftIntent);
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
