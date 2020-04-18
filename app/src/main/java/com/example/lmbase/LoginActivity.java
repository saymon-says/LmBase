package com.example.lmbase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

	private Button loginButton;
	private EditText emailLoginUser, passwordLoginUser;
	private TextView newAccountLink;

	public LoginActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		loginButton = findViewById(R.id.button_login);
		emailLoginUser = findViewById(R.id.email_login);
		passwordLoginUser = findViewById(R.id.password_login);
		newAccountLink = findViewById(R.id.add_account);

		newAccountLink.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SendUserToRegisterActivity();
			}
		});
	}

	private void SendUserToRegisterActivity() {
		Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
		startActivity(registerIntent);
	}
}
