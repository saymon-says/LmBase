package com.example.lmbase;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmbase.Model.WorkShifts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class WorkShiftListActivity extends AppCompatActivity {

	private RecyclerView listWorkshifts;
	private String currentUserId, currentDateOrderList;

	private FirebaseAuth mAuth;
	private DatabaseReference workshiftRef;
	private Toolbar mToolbar;
	private int currentMonth, currentYear;
	private String firstDateStart, firstDateEnd, secondDateStart, secondDateEnd;
	private String dateWorkshift, date1, date2;
	private String firstMonthStart, firstMonthEnd, secondMonthEnd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_work_shift_list);

		listWorkshifts = findViewById(R.id.list_of_workshifts);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		linearLayoutManager.setReverseLayout(false);
		linearLayoutManager.setStackFromEnd(true);
		listWorkshifts.setLayoutManager(linearLayoutManager);

		Calendar calendarDate = Calendar.getInstance();
		currentMonth = calendarDate.get(Calendar.MONTH);
		currentYear = calendarDate.get(Calendar.YEAR);
		@SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("MM-yyyy");
		@SuppressLint("SimpleDateFormat") SimpleDateFormat currentDateWorkShift = new SimpleDateFormat("yyyy-MM-dd");
		currentDateOrderList = currentDate.format(calendarDate.getTime());
		dateWorkshift = currentDateWorkShift.format(calendarDate.getTime());

		mToolbar = findViewById(R.id.workshift_page_toolbar);
		setSupportActionBar(mToolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle("Смены за " + currentDateOrderList);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
		workshiftRef = FirebaseDatabase.getInstance().getReference().child("Pointers List").child(currentUserId);
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
		Date currentDateWorkShift = currentDate.parse(dateWorkshift); //2020-05-24

		if (Objects.requireNonNull(firstDateEndRange).before(currentDateWorkShift)
				|| firstDateEndRange.equals(currentDateWorkShift)) {
			date1 = secondDateStart;
			date2 = secondDateEnd;
		} else {
			date1 = firstDateStart;
			date2 = firstDateEnd;
		}
		DisplayAllWorkShifts();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			CreateDateRange();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void DisplayAllWorkShifts() {

		Query query = workshiftRef.orderByChild("date").startAt(date1).endAt(date2);
		FirebaseRecyclerOptions<WorkShifts> options = new FirebaseRecyclerOptions.Builder<WorkShifts>()
				.setQuery(query, WorkShifts.class)
				.build();

		FirebaseRecyclerAdapter<WorkShifts, WorkShiftListActivity.WorkShiftsViewHolder> adapter =
				new FirebaseRecyclerAdapter<WorkShifts, WorkShiftListActivity.WorkShiftsViewHolder>(options) {
					@SuppressLint("SetTextI18n")
					@Override
					protected void onBindViewHolder(@NonNull WorkShiftListActivity.WorkShiftsViewHolder holder, int position, @NonNull WorkShifts model) {
						holder.date.setText("Дата " + model.getDate());
						holder.resultPoint.setText("Результат: " + model.getResultPoint());
					}

					@NonNull
					@Override
					public WorkShiftsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
						View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workshift_item, parent, false);
						return new WorkShiftsViewHolder(view);
					}
				};
		listWorkshifts.setAdapter(adapter);
		adapter.startListening();
	}

	public static class WorkShiftsViewHolder extends RecyclerView.ViewHolder {
		TextView date, resultPoint;

		WorkShiftsViewHolder(@NonNull View itemView) {
			super(itemView);
			date = itemView.findViewById(R.id.text_date_workshift);
			resultPoint = itemView.findViewById(R.id.text_resultPoint_workshift);
		}
	}

}
