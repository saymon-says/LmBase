package com.example.lmbase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddOrderActivity extends AppCompatActivity {

	private EditText numberOrder, priceOrder, bayouOrder;
	private Button addButton;
	private RadioGroup radioGroupOrder, radioGroupDelivery;
	private String currentUserId;
	private String numberOrderStr, priceOrderStr, bayoutOrderStr, resultPoint;
	private String resultDelivery, typeOrder, typeDelivery;
	private Integer deliveryVariant = 0;
	private long counterOrders = 0;
	private float resultPercentOrder;

	private DatabaseReference ordersRef, ordersCountRef;
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
		radioGroupOrder = findViewById(R.id.group_radio_type_order);
		radioGroupDelivery = findViewById(R.id.group_radio_type_delivery);
		addButton = findViewById(R.id.add_button);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();
		ordersRef = FirebaseDatabase.getInstance().getReference().child("Order List").child(currentUserId);
		ordersCountRef = FirebaseDatabase.getInstance().getReference().child("Order List").child(currentUserId).child(currentDateOrderList);

		if (radioGroupOrder.getCheckedRadioButtonId() == R.id.usually_order) {
			typeOrder = "usually";
			setUpDelivery();
		}
		radioGroupOrder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.usually_order:
						typeOrder = "usually";
						setUpDelivery();
						break;
					case R.id.sdd_order:
						typeOrder = "sdd";
						resultDelivery = "7";
						break;
				}
			}
		});

		if(radioGroupDelivery.getCheckedRadioButtonId() == R.id.usually_delivery){
			deliveryVariant = 0;
			typeDelivery = "usually";
		}
		radioGroupDelivery.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.usually_delivery:
						deliveryVariant = 0;
						typeDelivery = "usually";
						break;
					case R.id.partner_delivery:
						deliveryVariant = 1;
						typeDelivery = "partner";
						break;
					case R.id.econom_delivery:
						deliveryVariant = 2;
						typeDelivery = "economy";
						break;
				}
			}
		});

		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((numberOrder.length() == 0) || (priceOrder.length() == 0) || (bayouOrder.length() == 0)) {
					Toast.makeText(AddOrderActivity.this, "Проверь данные!", Toast.LENGTH_LONG).show();
				} else if (Integer.parseInt(bayouOrder.getText().toString()) > Integer.parseInt(priceOrder.getText().toString())) {
					Toast.makeText(AddOrderActivity.this, "Космический выкуп!", Toast.LENGTH_LONG).show();
				} else {
					switch (deliveryVariant) {
						case 0:
							calculateResultPoint();
							break;
						case 1:
							calculateResultPointPartner();
							break;
						case 2:
							if (!bayouOrder.getText().toString().equals("0")) {
								resultPoint = "3";
							} else {
								resultPoint = "0";
							}
							addOrderInOrderList();
							break;
					}
				}
			}
		});
	}

	private void setUpDelivery() {
		ordersCountRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					counterOrders = dataSnapshot.getChildrenCount();
					int countOfOrders = (int) dataSnapshot.getChildrenCount();
					if (0 < countOfOrders && countOfOrders < 15) {
						resultDelivery = "3";
					} else if (15 <= countOfOrders && countOfOrders < 29) {
						resultDelivery = "4";
					} else {
						resultDelivery = "7";
					}
				} else {
					counterOrders = 0;
					resultDelivery = "3";
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void calculateResultPointPartner() {

		numberOrderStr = numberOrder.getText().toString();
		priceOrderStr = priceOrder.getText().toString();
		bayoutOrderStr = bayouOrder.getText().toString();

		resultPercentOrder = Float.parseFloat(bayoutOrderStr) / Float.parseFloat(priceOrderStr) * 100;
		if (0 < resultPercentOrder && resultPercentOrder < 30) {
			resultPoint = "3";
		} else if (30 <= resultPercentOrder && resultPercentOrder <= 100) {
			resultPoint = "5";
		} else {
			resultPoint = "0";
			Toast.makeText(this, "Какая-то хрень!", Toast.LENGTH_LONG).show();
		}
		addOrderInOrderList();
	}

	private void calculateResultPoint() {

		numberOrderStr = numberOrder.getText().toString();
		priceOrderStr = priceOrder.getText().toString();
		bayoutOrderStr = bayouOrder.getText().toString();

		resultPercentOrder = Float.parseFloat(bayoutOrderStr) / Float.parseFloat(priceOrderStr) * 100;
		if (0 < resultPercentOrder && resultPercentOrder < 20) {
			resultPoint = "2";
		} else if (20 <= resultPercentOrder && resultPercentOrder < 30) {
			resultPoint = "4";
		} else if (30 <= resultPercentOrder && resultPercentOrder < 40) {
			resultPoint = "5";
		} else if (40 <= resultPercentOrder && resultPercentOrder < 50) {
			resultPoint = "6";
		} else if (50 <= resultPercentOrder && resultPercentOrder < 60) {
			resultPoint = "7";
		} else if (60 <= resultPercentOrder && resultPercentOrder < 70) {
			resultPoint = "8";
		} else if (70 <= resultPercentOrder && resultPercentOrder <= 100) {
			resultPoint = "9";
		} else {
			resultPoint = "0";
			Toast.makeText(this, "Какая-то хрень!", Toast.LENGTH_LONG).show();
		}
		addOrderInOrderList();
	}

	private void addOrderInOrderList() {

		numberOrderStr = numberOrder.getText().toString();
		priceOrderStr = priceOrder.getText().toString();
		bayoutOrderStr = bayouOrder.getText().toString();

		HashMap orderMap = new HashMap();
		orderMap.put("numberOrder", numberOrderStr);
		orderMap.put("priceOrder", priceOrderStr);
		orderMap.put("bayoutOrder", bayoutOrderStr);
		orderMap.put("uid", currentUserId);
		orderMap.put("date", currentDateOrderList);
		orderMap.put("counter", counterOrders);
		orderMap.put("point", resultPoint);
		orderMap.put("delivery", resultDelivery);
		orderMap.put("typeOrder", typeOrder);
		orderMap.put("typeDelivery", typeDelivery);
		ordersRef.child(currentDateOrderList).child(numberOrderStr).updateChildren(orderMap).addOnSuccessListener(new OnSuccessListener() {
			@Override
			public void onSuccess(Object o) {
				SendUserToOrderListActivity();
			}
		});
	}

	private void SendUserToOrderListActivity() {
		Intent orderListIntent = new Intent(AddOrderActivity.this, OrderListActivity.class);
		startActivity(orderListIntent);
	}
}
