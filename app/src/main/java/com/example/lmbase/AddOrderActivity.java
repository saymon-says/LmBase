package com.example.lmbase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

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
	private String numberOrderStr, priceOrderStr, bayoutOrderStr, resultPoint;
	private double resultPercentOrder;

	private DatabaseReference ordersRef;
	private FirebaseAuth mAuth;
	private String currentDateOrderList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_order);

		Calendar calendarDate = Calendar.getInstance();
		SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
		currentDateOrderList = currentDate.format(calendarDate.getTime());

		numberOrder = findViewById(R.id.number_order);
		priceOrder = findViewById(R.id.price_order);
		bayouOrder = findViewById(R.id.bayout_order);
		addButton = findViewById(R.id.add_button);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();
		ordersRef = FirebaseDatabase.getInstance().getReference().child("Order List").child(currentUserId);

		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if((numberOrder.length() == 0) || (priceOrder.length() == 0) || (bayouOrder.length() == 0)) {
					Toast.makeText(AddOrderActivity.this, "Проверь данные!", Toast.LENGTH_LONG).show();
				} else if (Integer.parseInt(bayouOrder.getText().toString()) > Integer.parseInt(priceOrder.getText().toString())) {
					Toast.makeText(AddOrderActivity.this, "Космический выкуп!", Toast.LENGTH_LONG).show();
				} else {
					calculateResultPoint();
				}
			}
		});
	}

	private void calculateResultPoint() {

		numberOrderStr = numberOrder.getText().toString();
		priceOrderStr = priceOrder.getText().toString();
		bayoutOrderStr = bayouOrder.getText().toString();

		resultPercentOrder = Math.floor(Integer.valueOf(bayoutOrderStr) * 100 / Integer.valueOf(priceOrderStr));
		if(0 < resultPercentOrder && resultPercentOrder < 20) {
			resultPoint = "2";
		} else if(20 <= resultPercentOrder && resultPercentOrder < 30) {
			resultPoint = "4";
		} else if(30 <= resultPercentOrder && resultPercentOrder < 40) {
			resultPoint = "5";
		} else if(40 <= resultPercentOrder && resultPercentOrder < 50) {
			resultPoint = "6";
		} else if(50 <= resultPercentOrder && resultPercentOrder < 60) {
			resultPoint = "7";
		} else if(60 <= resultPercentOrder && resultPercentOrder < 70) {
			resultPoint = "8";
		} else if(70 <= resultPercentOrder && resultPercentOrder <= 100) {
			resultPoint = "9";
		} else {
			resultPoint = "0";
			Toast.makeText(this, "Какая-то хрень!", Toast.LENGTH_LONG).show();
		}
				addOrderInOrderList();
	}

	private void addOrderInOrderList() {

		HashMap orderMap = new HashMap();
		orderMap.put("numberOrder", numberOrderStr);
		orderMap.put("priceOrder", priceOrderStr);
		orderMap.put("bayoutOrder", bayoutOrderStr);
		orderMap.put("uid", currentUserId);
		orderMap.put("date", currentDateOrderList);
		orderMap.put("point", resultPoint);
		ordersRef.child(currentDateOrderList).child(numberOrderStr).updateChildren(orderMap).addOnSuccessListener(new OnSuccessListener() {
			@Override
			public void onSuccess(Object o) {
				SendUserToOrderListAсtivity();
			}
		});
	}

	private void SendUserToOrderListAсtivity() {
		Intent orderListIntent = new Intent(AddOrderActivity.this, OrderListActivity.class);
		startActivity(orderListIntent);
	}
}
