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

import com.example.lmbase.Class.WorkShifts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WorkShiftListActivity extends AppCompatActivity {

	private RecyclerView listWorkshifts;
	private String currentUserId, currentDateOrderList;

	private FirebaseAuth mAuth;
	private DatabaseReference workshiftRef;
	private Toolbar mToolbar;

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
		SimpleDateFormat currentDate = new SimpleDateFormat("MM-yyyy");
		currentDateOrderList = currentDate.format(calendarDate.getTime());

		mToolbar = findViewById(R.id.workshift_page_toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle("Смены за " + currentDateOrderList);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();
		workshiftRef = FirebaseDatabase.getInstance().getReference().child("Pointers List").child(currentUserId);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		DisplayAllWorkShifts();
	}

	private void DisplayAllWorkShifts() {

		FirebaseRecyclerOptions<WorkShifts> options = new FirebaseRecyclerOptions.Builder<WorkShifts>()
				.setQuery(workshiftRef, WorkShifts.class)
				.build();

		FirebaseRecyclerAdapter<WorkShifts, WorkShiftListActivity.WorkShiftsViewHolder> adapter =
				new FirebaseRecyclerAdapter<WorkShifts, WorkShiftListActivity.WorkShiftsViewHolder>(options) {
					@SuppressLint("SetTextI18n")
					@Override
					protected void onBindViewHolder(@NonNull WorkShiftListActivity.WorkShiftsViewHolder holder, int position, @NonNull WorkShifts model) {
						holder.date.setText("Date " + model.getDate());
						holder.resultPoint.setText("Result: " + model.getResultPoint());
					}

					@NonNull
					@Override
					public WorkShiftsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
						View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workshift_item, parent, false);
						WorkShiftListActivity.WorkShiftsViewHolder workshiftsHolder = new WorkShiftListActivity.WorkShiftsViewHolder(view);
						return workshiftsHolder;
					}
				};
		listWorkshifts.setAdapter(adapter);
		adapter.startListening();
	}

	public static class WorkShiftsViewHolder extends RecyclerView.ViewHolder {
		TextView date, resultPoint;

		public WorkShiftsViewHolder(@NonNull View itemView) {
			super(itemView);
			date = itemView.findViewById(R.id.text_date_workshift);
			resultPoint = itemView.findViewById(R.id.text_resultPoint_workshift);
		}
	}

}
