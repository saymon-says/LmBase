package com.example.lmbase;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

	private Toolbar mToolbar;
	private DatabaseReference fullMonthRef, ordersCountRef;
	private FirebaseAuth mAuth;
	private String currentUserId, currentDateOrderList;
	private TextView cashToday, monthCash, monthCashVariable, monthRating;
	private Float tax = 0.87f, pointValue = 13.5f;
	private Integer countOfOrders;


	@SuppressLint("RestrictedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statistics);

		Calendar calendarDate = Calendar.getInstance();
		SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
		currentDateOrderList = currentDate.format(calendarDate.getTime());

		mToolbar = findViewById(R.id.statistics_navbar);
		setSupportActionBar(mToolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle("Статистика");
		getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();
		ordersCountRef = FirebaseDatabase.getInstance().getReference().child("Pointers List").child(currentUserId).child(currentDateOrderList);
		fullMonthRef = FirebaseDatabase.getInstance().getReference().child("Statistic List").child(currentUserId);

		monthCash = findViewById(R.id.month_cash);
		cashToday = findViewById(R.id.cash_today);
		monthCashVariable = findViewById(R.id.month_cash_variable);
		monthRating = findViewById(R.id.month_rating_tb);


		ordersCountRef.addValueEventListener(new ValueEventListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					Integer a = Integer.valueOf(dataSnapshot.child("resultPoint").getValue().toString());
					double result = (a * pointValue + 400) * tax;
					double newDouble = new BigDecimal(result).setScale(2, RoundingMode.UP).doubleValue();
					cashToday.setText(newDouble + " руб");
				} else {
					Toast.makeText(StatisticsActivity.this, "Сорян..", Toast.LENGTH_SHORT).show();
					cashToday.setText("Ничего пока");
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
				countOfOrders = Math.toIntExact(dataSnapshot.getChildrenCount());
				if (dataSnapshot.exists()) {
					int resultPointMonth = 0;
					int resultCountOrders = 0;
					int resultBuyoutOrders = 0;
					for (DataSnapshot ds : dataSnapshot.getChildren()) {
						Map<String, Object> map = (Map<String, Object>) ds.getValue();
						Object resultPoint = map.get("resultPoint");
						Object resultCount = map.get("countOfOrders");
						Object result = map.get("resultBuyout");
						int rValue = Integer.parseInt(String.valueOf(resultPoint));
						int oValue = Integer.parseInt(String.valueOf(resultCount));
						int bValue = Integer.parseInt(String.valueOf(result));
						resultPointMonth += rValue;
						resultCountOrders += oValue;
						resultBuyoutOrders += bValue;
					}
					double a = (resultPointMonth * pointValue + 400 * countOfOrders - 2000) * tax;
					double newDoubleA = new BigDecimal(a).setScale(2, RoundingMode.UP).doubleValue();
					monthCash.setText(newDoubleA + " руб");

					double b = (double) resultBuyoutOrders / resultCountOrders;
					double newDoubleB = new BigDecimal(b).setScale(5, RoundingMode.UP).doubleValue();
					monthRating.setText(newDoubleB + "");

					double resultMonthCash = (resultPointMonth / countOfOrders * pointValue * 15 + 6000) * tax;
					double newDoubleResultMonthCash = new BigDecimal(resultMonthCash).setScale(2, RoundingMode.UP).doubleValue();
					monthCashVariable.setText((newDoubleResultMonthCash) + " руб");
				} else {
					monthCash.setText("0");
					monthCashVariable.setText("0");
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});


	}
}
