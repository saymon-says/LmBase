package com.example.lmbase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddOrderActivity extends AppCompatActivity {

	private EditText numberOrder, priceOrder, bayouOrder;
	private Button addButton;

	private DatabaseReference ordersRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_order);

		numberOrder = findViewById(R.id.number_order);
		priceOrder = findViewById(R.id.price_order);
		bayouOrder = findViewById(R.id.bayout_order);
		addButton = findViewById(R.id.add_button);

		ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");

		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addOrderInOrderList();
			}
		});
	}

	private void addOrderInOrderList() {

	}
}
