package com.example.lmbase;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmbase.Model.Orders;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class OrderListActivity extends AppCompatActivity {

	private RecyclerView listOrders;
	private String currentUserId, currentDateOrderList;
	private FloatingActionButton addBtn;

	private FirebaseAuth mAuth;
	private DatabaseReference ordersRef;
	private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_list);

		addBtn = findViewById(R.id.add_order);
		listOrders = findViewById(R.id.list_of_orders);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		linearLayoutManager.setReverseLayout(false);
		linearLayoutManager.setStackFromEnd(true);
		listOrders.setLayoutManager(linearLayoutManager);

		Calendar calendarDate = Calendar.getInstance();
		SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
		currentDateOrderList = currentDate.format(calendarDate.getTime());

		mToolbar = findViewById(R.id.orders_page_toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle("Заказы на: " + currentDateOrderList);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();
		ordersRef = FirebaseDatabase.getInstance().getReference().child("Order List").child(currentUserId).child(currentDateOrderList);

		addBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SendUserToAddOrderActivity();
			}
		});
	}

	private void SendUserToAddOrderActivity() {
		Intent orderListIntent = new Intent(OrderListActivity.this, AddOrderActivity.class);
		startActivity(orderListIntent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		DisplayAllUserOrders();
	}

	private void DisplayAllUserOrders() {

		Query counterRef = ordersRef.orderByChild("counter");
		FirebaseRecyclerOptions<Orders> options = new FirebaseRecyclerOptions.Builder<Orders>()
				.setQuery(counterRef, Orders.class)
				.build();

		FirebaseRecyclerAdapter<Orders, OrdersViewHolder> adapter = new FirebaseRecyclerAdapter<Orders, OrdersViewHolder>(options) {
			@SuppressLint("SetTextI18n")
			@Override
			protected void onBindViewHolder(@NonNull OrdersViewHolder holder, int position, @NonNull Orders model) {

				final String orderKey = getRef(position).getKey();

				holder.numberOrder.setText("Заказ: " + model.getNumberOrder());
				holder.priceOrder.setText("Стоимость: " + model.getPriceOrder());
				holder.bayoutOrder.setText("Выкуп: " + model.getBayoutOrder());
				holder.pointOrder.setText("БВ :" + model.getPoint());
				holder.deliveryOrder.setText("БД :" + model.getDelivery());
				switch (model.getTypeDelivery()) {
					case "usually":
						holder.orderImage.setImageResource(R.drawable.lamoda_order);
						break;
					case "partner":
						holder.orderImage.setImageResource(R.drawable.partner_order);
						break;
					case "economy":
						holder.orderImage.setImageResource(R.drawable.economy_order);
						break;
				}

				holder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent editOrderIntent = new Intent(OrderListActivity.this, EditOrderActivity.class);
						editOrderIntent.putExtra("orderKey", orderKey);
						startActivity(editOrderIntent);

					}
				});
			}

			@NonNull
			@Override
			public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list, parent, false);
				return new OrdersViewHolder(view);
			}
		};
		listOrders.setAdapter(adapter);
		adapter.startListening();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	public static class OrdersViewHolder extends RecyclerView.ViewHolder {
		TextView numberOrder, priceOrder, bayoutOrder, pointOrder, deliveryOrder;
		ImageView orderImage;

		OrdersViewHolder(@NonNull View itemView) {
			super(itemView);
			numberOrder = itemView.findViewById(R.id.number_order);
			priceOrder = itemView.findViewById(R.id.price_order);
			bayoutOrder = itemView.findViewById(R.id.bayout_order);
			pointOrder = itemView.findViewById(R.id.point_order);
			deliveryOrder = itemView.findViewById(R.id.delivery_order);
			orderImage = itemView.findViewById(R.id.image_order);
		}
	}
}
