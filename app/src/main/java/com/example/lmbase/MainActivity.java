package com.example.lmbase;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

	//	private final static String WORKSHEET_OPEN = "1";
	private DrawerLayout drawerLayout;
	private NavigationView navigationView;
	private CircleImageView navUserpic;
	private TextView navUserName, ordersCount, deliveryCount, buyoutCount,
			pointCount, fifteenExactTimeToday, sixtyExactTimeToday, upFineToday;
	private ImageButton settingProfile;
	private EditText fifteenExactTime, sixtyExactTime, fines, benetonCount, workshiftAdd;
	private FirebaseAuth mAuth;
	private DatabaseReference pointerRef;
	private DatabaseReference usersRef;
	private DatabaseReference statisticRef, ordersCountRef;
	private String currentUserId, currentDateOrderList, openDateWorkShift;
	private Integer fifteenTime = 0;
	private Integer sixtyTime = 0;
	private Integer finesToday = 0;
	private Integer resultBuyout = 0, countOfOrders = 1, resultDelivery = 0;
	private Integer benetonCounts = 0;
	private Integer addedWorkshifts = 0;
	private double pointFinesToday = 0.0, resultPoint = 0.0;
//	private String WORKSHEET_OPEN_TODAY = "0";
//	private String WORKSHEET_OPEN_DATE_TODAY = "0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

		Calendar calendarDate = Calendar.getInstance();
		@SuppressLint("SimpleDateFormat") final SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
		currentDateOrderList = currentDate.format(calendarDate.getTime());

		pointerRef = FirebaseDatabase.getInstance().getReference().child("Pointers List");
		usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
		ordersCountRef = FirebaseDatabase.getInstance().getReference().child("Order List").child(currentUserId).child(currentDateOrderList);
		statisticRef = FirebaseDatabase.getInstance().getReference().child("Statistic List").child(currentUserId);

		drawerLayout = findViewById(R.id.drawer_layout);
		navigationView = findViewById(R.id.nav);
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
		settingProfile = navView.findViewById(R.id.nav_setting_profile);

		Toolbar mToolbar = findViewById(R.id.main_page_toolbar);
		setSupportActionBar(mToolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle("Статистика за день");
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawerLayout, mToolbar, R.string.open_navigation, R.string.close_navigation);
		drawerLayout.addDrawerListener(toggle);
		toggle.syncState();

		pointersToday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (WORKSHEET_OPEN_TODAY.equals("1") && WORKSHEET_OPEN_DATE_TODAY.equals("1")) {
				Intent sendOrderList = new Intent(MainActivity.this, OrderListActivity.class);
				startActivity(sendOrderList);
//				} else if (WORKSHEET_OPEN_TODAY.equals("1") && WORKSHEET_OPEN_DATE_TODAY.equals("0")){
//					Toast.makeText(MainActivity.this, "Закрой старую смену!", Toast.LENGTH_SHORT).show();
//				}
			}
		});

//		ShowExistsWorkShift();

		exactTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (WORKSHEET_OPEN_TODAY.equals("1")) {
				ShowDialogExactTime();
//				} else {
//					Toast.makeText(MainActivity.this, "Смена закрыта!", Toast.LENGTH_SHORT).show();
//				}
			}
		});

		fineToday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (WORKSHEET_OPEN_TODAY.equals("1")) {
				ShowDialogFine();
//				} else {
//					Toast.makeText(MainActivity.this, "Смена закрыта!", Toast.LENGTH_SHORT).show();
//				}
			}
		});

		settingProfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SendUserToSettingsActivity();
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

//	private void ShowExistsWorkShift() {
//		usersRef.addValueEventListener(new ValueEventListener() {
//			@Override
//			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//				if (dataSnapshot.child(currentUserId).child("workshift_exist").exists()) {
//					String existWorkShift = Objects.requireNonNull(dataSnapshot.child(currentUserId)
//							.child("workshift_exist").getValue()).toString();
//					openDateWorkShift = Objects.requireNonNull(dataSnapshot.child(currentUserId)
//							.child("dateOpenWorkShift").getValue()).toString();
//					if (existWorkShift.equals(WORKSHEET_OPEN) && openDateWorkShift.equals(currentDateOrderList)) {
//						WORKSHEET_OPEN_TODAY = "1";
//						WORKSHEET_OPEN_DATE_TODAY = "1";
//						navigationView.getMenu().findItem(R.id.nav_add_workshift).setTitle("Закрыть смену");
//					} else if (existWorkShift.equals(WORKSHEET_OPEN)) {
//						WORKSHEET_OPEN_TODAY = "1";
//						WORKSHEET_OPEN_DATE_TODAY = "0";
//						navigationView.getMenu().findItem(R.id.nav_add_workshift).setTitle("Закрыть смену");
//						Toast.makeText(MainActivity.this, "Сначала закрой старую смену!", Toast.LENGTH_SHORT).show();
//					}
//				} else {
//					WORKSHEET_OPEN_TODAY = "0";
//					WORKSHEET_OPEN_DATE_TODAY = "0";
//					navigationView.getMenu().findItem(R.id.nav_add_workshift).setTitle("Открыть смену");
//				}
//			}
//
//			@Override
//			public void onCancelled(@NonNull DatabaseError databaseError) {
//
//			}
//		});
//
//	}

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

//	@Override
//	protected void onResume() {
//		super.onResume();
//		ShowAllDataOpenedWorkShift();
//	}

//	private void ShowAllDataOpenedWorkShift() {
//		statisticRef.addValueEventListener(new ValueEventListener() {
//			@SuppressLint("SetTextI18n")
//			@Override
//			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//				if (dataSnapshot.child(currentDateOrderList).exists()) {
//					fifteenTime = Integer.valueOf(Objects.requireNonNull(dataSnapshot.child(currentDateOrderList)
//							.child("15").getValue()).toString());
//					fifteenExactTimeToday.setText(fifteenTime + "");
//
//					sixtyTime = Integer.valueOf(Objects.requireNonNull(dataSnapshot.child(currentDateOrderList)
//							.child("60").getValue()).toString());
//					sixtyExactTimeToday.setText(sixtyTime + "");
//
//					finesToday = Integer.valueOf(Objects.requireNonNull(dataSnapshot.child(currentDateOrderList)
//							.child("fines").getValue()).toString());
//					upFineToday.setText(finesToday + "");
//				} else {
//					fifteenExactTimeToday.setText("0");
//					sixtyExactTimeToday.setText("0");
//					upFineToday.setText("0");
//				}
//			}
//
//			@Override
//			public void onCancelled(@NonNull DatabaseError databaseError) {
//
//			}
//		});
//
//		ordersCountRef.addValueEventListener(new ValueEventListener() {
//			@SuppressLint("SetTextI18n")
//			@Override
//			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//				if (dataSnapshot.exists()) {
//					for (DataSnapshot ds : dataSnapshot.getChildren()) {
//						Map<String, Object> map = (Map<String, Object>) ds.getValue();
//						assert map != null;
//						Object delivery = map.get("delivery");
//						int dValue = Integer.parseInt(String.valueOf(delivery));
//						Object buyout = map.get("point");
//						int bValue = Integer.parseInt(String.valueOf(buyout));
//						resultDelivery += dValue;
//						resultBuyout += bValue;
//						deliveryCount.setText(String.valueOf(resultDelivery));
//						buyoutCount.setText(String.valueOf(resultBuyout));
//					}
//					pointFinesToday = (2.5 - finesToday) * 7;
//					resultPoint = resultBuyout + resultDelivery + pointFinesToday;
//					pointCount.setText(String.valueOf(resultPoint));
//
//					countOfOrders = Math.toIntExact(dataSnapshot.getChildrenCount());
//					ordersCount.setText(countOfOrders + "");
//				} else {
//					ordersCount.setText("0");
//					pointCount.setText("0");
//					buyoutCount.setText("0");
//					deliveryCount.setText("0");
//				}
//			}
//
//			@Override
//			public void onCancelled(@NonNull DatabaseError databaseError) {
//
//			}
//		});
//	}

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
//				switch (WORKSHEET_OPEN_TODAY) {
//					case "0":
//						ShowDialogAddWorkShift();
//						break;
//					case "1":
//						ShowDialogCancelWorkShift();
//						break;
//				}
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
//				switch (WORKSHEET_OPEN_TODAY) {
//					case "0":
//						Toast.makeText(this, "Пока нечего обновлять", Toast.LENGTH_SHORT).show();
//						break;
//					case "1":
				UpdateDataBaseStatistics();
//				UpdateDataBaseWorkShift();
				UpdateDataBaseUsers();
				SendUserToMainActivity();
				Toast.makeText(this, "Обновлено", Toast.LENGTH_SHORT).show();
//						break;
//				}
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

//	private void ShowDialogCancelWorkShift() {
//		final AlertDialog.Builder cancel = new AlertDialog.Builder(this);
//		@SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.dialog_cancel_workshift, null);
//
//		Button cancelWorkShift = mView.findViewById(R.id.accept_button);
//		Button cancelDialog = mView.findViewById(R.id.cancel_button);
//		cancel.setView(mView);
//
//		final AlertDialog alertDialog = cancel.create();
//		alertDialog.setCanceledOnTouchOutside(false);
//		Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//		cancelDialog.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Toast.makeText(MainActivity.this, "Да! Поработай еще немного!", Toast.LENGTH_SHORT).show();
//				alertDialog.dismiss();
//			}
//		});
//
//		cancelWorkShift.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
////				UpdateDataBaseStatistics();
////				UpdateDataBaseUsers();
//				UpdateDataBaseWorkShift();
//				HashMap statMap = new HashMap();
//				statMap.put("workshift_exist", "0");
//				statMap.put("resultPointToday", 0);
//				usersRef.child(currentUserId).updateChildren(statMap);
//				alertDialog.dismiss();
//			}
//		});
//
//		alertDialog.show();
//	}

//	private void ShowDialogAddWorkShift() {
//		final AlertDialog.Builder add = new AlertDialog.Builder(this);
//		@SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.dialog_add_workshift, null);
//
//		Button addWorkshift = mView.findViewById(R.id.accept_button);
//		Button cancelDialog = mView.findViewById(R.id.cancel_button);
//		add.setView(mView);
//
//		final AlertDialog alertDialog = add.create();
//		alertDialog.setCanceledOnTouchOutside(false);
//		Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//		cancelDialog.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Toast.makeText(MainActivity.this, "Да ничего страшного, возвращайся!", Toast.LENGTH_SHORT).show();
//				alertDialog.dismiss();
//			}
//		});
//
//		addWorkshift.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				HashMap statMap = new HashMap();
//				statMap.put("workshift_exist", "1");
//				statMap.put("dateOpenWorkShift", currentDateOrderList);
//				usersRef.child(currentUserId).updateChildren(statMap);
//				Toast.makeText(MainActivity.this, "Поехали!", Toast.LENGTH_SHORT).show();
//				alertDialog.dismiss();
//			}
//		});
//
//		alertDialog.show();
//	}

	private void SendUserToMapsActivity() {
		Intent mapsIntent = new Intent(this, MapsActivity.class);
		startActivity(mapsIntent);
	}

	private void UpdateDataBaseUsers() {
		statisticRef.addValueEventListener(new ValueEventListener() {
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
					statMap.put("currentDateOrderList", currentDateOrderList);
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

//	private void UpdateDataBaseWorkShift() {
//
//		String resultPointToday = pointCount.getText().toString();
//		HashMap workshiftMap = new HashMap();
//		workshiftMap.put("date", currentDateOrderList);
//		workshiftMap.put("resultPoint", resultPointToday);
//		pointerRef.child(currentUserId).child(currentDateOrderList).updateChildren(workshiftMap);
//	}

	private void SendUserToWorkShiftActivity() {
		Intent workshiftIntent = new Intent(this, WorkShiftListActivity.class);
		startActivity(workshiftIntent);
	}

	private void SendUserToUserListActivity() {
		Intent orderListIntent = new Intent(this, UserListActivity.class);
		startActivity(orderListIntent);
	}

	private void SendUserToOrderList() {
//		if (WORKSHEET_OPEN_TODAY.equals("1")) {
		Intent orderListIntent = new Intent(this, OrderListActivity.class);
		startActivity(orderListIntent);
//		} else {
//			Toast.makeText(MainActivity.this, "Смена закрыта!", Toast.LENGTH_SHORT).show();
//		}
	}
}
