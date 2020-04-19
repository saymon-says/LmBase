package com.example.lmbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class OrderListActivity extends AppCompatActivity {

	private TextView DateWorkShift, CountPointer;
	private RecyclerView listOrders;

	private DatabaseReference ordersRef;
	private FirebaseAuth mAuth;
	String currentUserId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_list);

		DateWorkShift = findViewById(R.id.date_workshift);
		CountPointer = findViewById(R.id.count_pointer);
		listOrders = findViewById(R.id.list_of_orders);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		linearLayoutManager.setReverseLayout(true);
		linearLayoutManager.setStackFromEnd(true);
		listOrders.setLayoutManager(linearLayoutManager);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();
		ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
	}

	@Override
	protected void onStart() {
		super.onStart();

		DisplayAllUserOrders();
	}

	private void DisplayAllUserOrders() {

		FirebaseRecyclerOptions<Orders> options = new FirebaseRecyclerOptions.Builder<Orders>()
				.setQuery(ordersRef, Orders.class)
				.build();

		FirebaseRecyclerAdapter<Orders, OrdersViewHolder> adapter = new FirebaseRecyclerAdapter<Orders, OrdersViewHolder>(options) {
			@Override
			protected void onBindViewHolder(@NonNull OrdersViewHolder holder, int position, @NonNull Orders model) {
				holder.bayout.setText(model.getBayout());
			}

			@NonNull
			@Override
			public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list, parent, false);
				OrdersViewHolder ordersHolder = new OrdersViewHolder(view);
				return ordersHolder;
			}
		};
		listOrders.setAdapter(adapter);
		adapter.startListening();
	}

	public static class OrdersViewHolder extends RecyclerView.ViewHolder {
		TextView bayout;

		public OrdersViewHolder(@NonNull View itemView) {
			super(itemView);

			bayout = itemView.findViewById(R.id.bayot);
		}
	}

}
