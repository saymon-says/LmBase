package com.example.lmbase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddOrderActivity extends AppCompatActivity {

	private EditText numberOrder, priceOrder, bayouOrder;
	private Button addButton;
	private String currentUserId;
	private String numberOrderInt, priceOrderInt, bayoutOrderInt;

	private DatabaseReference ordersRef;
	private FirebaseAuth mAuth;
	private String currentDateOrderList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_order);

		Calendar calendarDate = Calendar.getInstance();
		SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
		currentDateOrderList = currentDate.format(calendarDate.getTime());

		numberOrder = findViewById(R.id.number_order);
		priceOrder = findViewById(R.id.price_order);
		bayouOrder = findViewById(R.id.bayout_order);
		addButton = findViewById(R.id.add_button);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();
		ordersRef = FirebaseDatabase.getInstance().getReference().child("Order List").child(currentDateOrderList);

		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addOrderInOrderList();
			}
		});
	}

	private void addOrderInOrderList() {

		numberOrderInt = numberOrder.getText().toString();
		priceOrderInt = priceOrder.getText().toString();
		bayoutOrderInt = bayouOrder.getText().toString();

		HashMap orderMap = new HashMap();
		orderMap.put("numberOrder", numberOrderInt);
		orderMap.put("priceOrder", priceOrderInt);
		orderMap.put("bayoutOrder", bayoutOrderInt);
		orderMap.put("uid", currentUserId);
		orderMap.put("date", currentDateOrderList);
		ordersRef.child(currentUserId).child(numberOrderInt).updateChildren(orderMap).addOnSuccessListener(new OnSuccessListener() {
			@Override
			public void onSuccess(Object o) {
				SendUserToOrderListAtivity();
				Toast.makeText(AddOrderActivity.this, "Заказ добавлен в список", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void SendUserToOrderListAtivity() {
		Intent orderListIntent = new Intent(AddOrderActivity.this, OrderListActivity.class);
		startActivity(orderListIntent);
	}
}
