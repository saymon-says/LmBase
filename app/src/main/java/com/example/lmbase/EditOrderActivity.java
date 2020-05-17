package com.example.lmbase;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
import java.util.Objects;

public class EditOrderActivity extends AppCompatActivity {


	private EditText priceOrder, bayoutOrder;
	private TextView numberOrder;
	private RadioGroup radioGroupOrder, radioGroupDelivery;
	private String currentUserId;
	private String numberOrderStr, priceOrderStr, bayoutOrderStr, resultPoint;
	private String resultDelivery, typeDelivery, typeOrder;
	private Integer deliveryVariant = 0;
	private float resultPercentOrder;

	private DatabaseReference editOrderRef, ordersRef;
	private String currentDateOrderList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_order);

		Calendar calendarDate = Calendar.getInstance();
		@SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
		currentDateOrderList = currentDate.format(calendarDate.getTime());

		numberOrder = findViewById(R.id.number_order);
		priceOrder = findViewById(R.id.price_order);
		bayoutOrder = findViewById(R.id.bayout_order);
		radioGroupOrder = findViewById(R.id.group_radio_type_order);
		radioGroupDelivery = findViewById(R.id.group_radio_type_delivery);
		Button editButton = findViewById(R.id.edit_button);
		ImageButton fullButton = findViewById(R.id.full_buying_btn);

		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
		String orderKey = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("orderKey")).toString();
		ordersRef = FirebaseDatabase.getInstance().getReference()
				.child("Order List").child(currentUserId);
		editOrderRef = FirebaseDatabase.getInstance().getReference()
				.child("Order List").child(currentUserId).child(currentDateOrderList).child(orderKey);

		UploadInfoOrders();

		RadioButton usuallyOrder = findViewById(R.id.usually_order);
		RadioButton sddOrder = findViewById(R.id.sdd_order);
		usuallyOrder.setOnClickListener(clickTypeOrder);
		sddOrder.setOnClickListener(clickTypeOrder);

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

		fullButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bayoutOrder.setText(priceOrder.getText().toString());
			}
		});

		editButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((priceOrder.length() == 0) || (bayoutOrder.length() == 0)) {
					Toast.makeText(EditOrderActivity.this, "Проверь данные!", Toast.LENGTH_LONG).show();
				} else if (Integer.parseInt(bayoutOrder.getText().toString()) > Integer.parseInt(priceOrder.getText().toString())) {
					Toast.makeText(EditOrderActivity.this, "Космический выкуп!", Toast.LENGTH_LONG).show();
				} else {
					switch (deliveryVariant) {
						case 0:
							calculateResultPoint();
							break;
						case 1:
							calculateResultPointPartner();
							break;
						case 2:
							if (!bayoutOrder.getText().toString().equals("0")) {
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


	private void UploadInfoOrders() {
		editOrderRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					String orderNumber = Objects.requireNonNull(dataSnapshot.child("numberOrder").getValue()).toString();
					String orderPrice = Objects.requireNonNull(dataSnapshot.child("priceOrder").getValue()).toString();
					numberOrder.setText(orderNumber);
					priceOrder.setText(orderPrice);
					switch (Objects.requireNonNull(dataSnapshot.child("typeDelivery").getValue()).toString()) {
						case "usually":
							radioGroupDelivery.check(R.id.usually_delivery);
							deliveryVariant = 0;
							typeDelivery = "usually";
							break;
						case "partner":
							radioGroupDelivery.check(R.id.partner_delivery);
							deliveryVariant = 1;
							typeDelivery = "partner";
							break;
						case "economy":
							radioGroupDelivery.check(R.id.econom_delivery);
							deliveryVariant = 2;
							typeDelivery = "economy";
							break;

					}
					switch (Objects.requireNonNull(dataSnapshot.child("typeOrder").getValue()).toString()) {
						case "usually":
							radioGroupOrder.check(R.id.usually_order);
							typeOrder = "usually";
							setUpDelivery();
							break;
						case "sdd":
							radioGroupOrder.check(R.id.sdd_order);
							typeOrder = "sdd";
							resultDelivery = "7";
							break;
					}
				} else {
					Toast.makeText(EditOrderActivity.this, "Sorry..", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void setUpDelivery() {
		editOrderRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					int countOfOrders = Integer.parseInt(Objects.requireNonNull(dataSnapshot.child("counter").getValue()).toString());
					if (0 <= countOfOrders && countOfOrders < 15) {
						resultDelivery = "3";
					} else if (15 <= countOfOrders && countOfOrders < 29) {
						resultDelivery = "4";
					} else {
						resultDelivery = "7";
					}
				} else {
					resultDelivery = "3";
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void calculateResultPointPartner() {

		priceOrderStr = priceOrder.getText().toString();
		bayoutOrderStr = bayoutOrder.getText().toString();

		resultPercentOrder = Float.parseFloat(bayoutOrderStr) / Float.parseFloat(priceOrderStr) * 100;
		if (0 <= resultPercentOrder && resultPercentOrder < 30) {
			resultPoint = "3";
		} else if (30 <= resultPercentOrder && resultPercentOrder <= 100) {
			resultPoint = "5";
		} else {
			resultPoint = "0";
			Toast.makeText(this, "Парнер то нулевой..", Toast.LENGTH_LONG).show();
		}
		addOrderInOrderList();
	}

	private void calculateResultPoint() {

		priceOrderStr = priceOrder.getText().toString();
		bayoutOrderStr = bayoutOrder.getText().toString();

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
			Toast.makeText(this, "Не фортануло..", Toast.LENGTH_LONG).show();
		}
		addOrderInOrderList();
	}

	private void addOrderInOrderList() {

		numberOrderStr = numberOrder.getText().toString();
		priceOrderStr = priceOrder.getText().toString();
		bayoutOrderStr = bayoutOrder.getText().toString();

		HashMap orderMap = new HashMap();
		orderMap.put("numberOrder", numberOrderStr);
		orderMap.put("priceOrder", priceOrderStr);
		orderMap.put("bayoutOrder", bayoutOrderStr);
		orderMap.put("uid", currentUserId);
		orderMap.put("date", currentDateOrderList);
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
		Intent orderListIntent = new Intent(EditOrderActivity.this, OrderListActivity.class);
		startActivity(orderListIntent);
	}

	View.OnClickListener clickTypeOrder = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			RadioButton RD = (RadioButton) v;
			switch (RD.getId()) {
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
	};
}
