package com.example.lmbase;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

public class StatisticsActivity extends AppCompatActivity {

	private DatabaseReference fullMonthRef, usersRef;
	private String currentDateOrderList;
	private TextView cashToday, monthCash, monthCashVariable, monthRating, monthCashTime, monthCashSurcharge;
	private Float tax = 0.87f, pointValue = 13.5f;
	private Integer countOfOrders, workShiftValue, workShiftCounts;
	private double reitUserMonth;


	@SuppressLint("RestrictedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statistics);

		Calendar calendarDate = Calendar.getInstance();
		@SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
		currentDateOrderList = currentDate.format(calendarDate.getTime());

		Toolbar mToolbar = findViewById(R.id.statistics_navbar);
		setSupportActionBar(mToolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle("Статистика за месяц");
		getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
		fullMonthRef = FirebaseDatabase.getInstance().getReference().child("Statistic List").child(currentUserId);
		usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

		monthCash = findViewById(R.id.month_cash);
		CardView month = findViewById(R.id.month_money);
		cashToday = findViewById(R.id.cash_today);
		monthCashVariable = findViewById(R.id.month_cash_variable);
		CardView monthVariable = findViewById(R.id.month_money_variable);
		monthRating = findViewById(R.id.month_rating_tb);
		monthCashTime = findViewById(R.id.month_cash_time);
		monthCashSurcharge = findViewById(R.id.month_cash_surcharges);

		month.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowInfoMonthCash();
			}
		});
		monthVariable.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowInfoMonthVariableCash();
			}
		});

		usersRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					reitUserMonth = Double.parseDouble(Objects.requireNonNull(dataSnapshot.child("reit").getValue()).toString());
					workShiftCounts = Integer.parseInt(Objects.requireNonNull(dataSnapshot.child("workshift").getValue()).toString());
					workShiftValue = 6000 / workShiftCounts;
				} else {
					Toast.makeText(StatisticsActivity.this, "Nothing yet..", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		fullMonthRef.addValueEventListener(new ValueEventListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

				if (dataSnapshot.exists()) {
					countOfOrders = Math.toIntExact(dataSnapshot.getChildrenCount());
					if (dataSnapshot.child(currentDateOrderList).exists()) {
						int exactFifteen = Integer.parseInt(Objects.requireNonNull(dataSnapshot.child(currentDateOrderList).child("15").getValue()).toString());
						int exactSixty = Integer.parseInt(Objects.requireNonNull(dataSnapshot.child(currentDateOrderList).child("60").getValue()).toString());
						int resultExact = exactFifteen * 100 + exactSixty * 20;
						double aa = Double.parseDouble(Objects.requireNonNull(dataSnapshot.child(currentDateOrderList).child("resultPoint").getValue()).toString());
						double resultA = (aa * reitUserMonth * pointValue + workShiftValue + resultExact) * tax;
						double newDouble = new BigDecimal(resultA).setScale(2, RoundingMode.UP).doubleValue();
						cashToday.setText(newDouble + " руб");
					} else {
						cashToday.setText("0 руб");
					}
					double resultPointMonth = 0;
					int resultCountOrders = 0;
					int resultBuyoutOrders = 0;
					int resultFifteenExact = 0;
					int resultSixtyExact = 0;
					for (DataSnapshot ds : dataSnapshot.getChildren()) {
						Map<String, Object> map = (Map<String, Object>) ds.getValue();
						Object resultPoint = map.get("resultPoint");
						Object resultCount = map.get("countOfOrders");
						Object result = map.get("resultBuyout");
						Object resultFifteen = map.get("15");
						Object resultSixty = map.get("60");
						double rValue = Double.parseDouble(String.valueOf(resultPoint));
						int oValue = Integer.parseInt(String.valueOf(resultCount));
						int bValue = Integer.parseInt(String.valueOf(result));
						int fValue = Integer.parseInt(String.valueOf(resultFifteen));
						int sValue = Integer.parseInt(String.valueOf(resultSixty));
						resultPointMonth += rValue;
						resultCountOrders += oValue;
						resultBuyoutOrders += bValue;
						resultFifteenExact += fValue;
						resultSixtyExact += sValue;
					}

					double resultCashFifteensTime = resultFifteenExact * 100;
					double resultCashSixtyTime = resultSixtyExact * 20;
					double newDoubleResultCashSixtyTime = new BigDecimal(resultCashSixtyTime)
							.setScale(2, RoundingMode.UP).doubleValue();
					double newDoubleResultCashFifteensTime = new BigDecimal(resultCashFifteensTime)
							.setScale(2, RoundingMode.UP).doubleValue();
					double resultExactTime = newDoubleResultCashFifteensTime + newDoubleResultCashSixtyTime;
					monthCashTime.setText(resultExactTime + " руб");


					double a = (resultPointMonth * reitUserMonth * pointValue + workShiftValue * countOfOrders - 2000 + resultExactTime) * tax;
					double newDoubleA = new BigDecimal(a).setScale(2, RoundingMode.UP).doubleValue();
					monthCash.setText(newDoubleA + " руб");

					double b = (double) resultBuyoutOrders / resultCountOrders;
					double newDoubleB = new BigDecimal(b).setScale(5, RoundingMode.UP).doubleValue();
					monthRating.setText(newDoubleB + "");

					double resultMonthCash = ((resultPointMonth * reitUserMonth) / countOfOrders * pointValue * workShiftCounts + 6000) * tax;
					double newDoubleResultMonthCash = new BigDecimal(resultMonthCash).setScale(2, RoundingMode.UP).doubleValue();
					monthCashVariable.setText((newDoubleResultMonthCash) + " руб");
				} else {
					monthCash.setText("0 руб");
					monthCashVariable.setText("0 руб");
					monthRating.setText("0");
					monthCashTime.setText("0 руб");
					cashToday.setText("0 руб");
					monthCashSurcharge.setText("0 руб");
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void ShowInfoMonthVariableCash() {
		final AlertDialog.Builder monthVariable = new AlertDialog.Builder(this);
		@SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.variable_pop_up, null);
		Button cancelDialog = mView.findViewById(R.id.accept_btn);
		monthVariable.setView(mView);

		final AlertDialog alertDialog = monthVariable.create();
		alertDialog.setCanceledOnTouchOutside(false);
		Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

		cancelDialog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});
		alertDialog.show();

	}

	private void ShowInfoMonthCash() {
		final AlertDialog.Builder month = new AlertDialog.Builder(this);
		@SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.month_pop_up, null);
		Button cancelDialog = mView.findViewById(R.id.accept_btn);
		month.setView(mView);

		final AlertDialog alertDialog = month.create();
		alertDialog.setCanceledOnTouchOutside(false);
		Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

		cancelDialog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});
		alertDialog.show();

	}
}
