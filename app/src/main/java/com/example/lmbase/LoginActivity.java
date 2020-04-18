package com.example.lmbase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

	private Button loginButton;
	private EditText emailLoginUser, passwordLoginUser;
	private TextView newAccountLink;
	private FirebaseAuth mAuth;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mAuth = FirebaseAuth.getInstance();

		loginButton = findViewById(R.id.button_login);
		emailLoginUser = findViewById(R.id.email_login);
		passwordLoginUser = findViewById(R.id.password_login);
		newAccountLink = findViewById(R.id.add_account);
		progressDialog = new ProgressDialog(this);

		newAccountLink.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SendUserToRegisterActivity();
			}
		});

		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AcceptUserLogin();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		FirebaseUser currentUser = mAuth.getCurrentUser();
		if (currentUser != null) {
			SendUserToMainActivity();
		}
	}

	private void AcceptUserLogin() {

		String emailUser = emailLoginUser.getText().toString();
		String passwordUser = passwordLoginUser.getText().toString();

		if(emailUser.length() < 3) {
			Toast.makeText(this, "Email слишком короткий", Toast.LENGTH_LONG).show();
		} else if (passwordUser.length() < 6) {
			Toast.makeText(this, "Пароль слишком короткий", Toast.LENGTH_LONG).show();
		} else {

			progressDialog.setTitle("Авторизуемся..");
			progressDialog.setMessage("Падажжиии...");
			progressDialog.show();
			progressDialog.setCanceledOnTouchOutside(true);

			mAuth.signInWithEmailAndPassword(emailUser, passwordUser)
					.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {
							if(task.isSuccessful()) {
								Toast.makeText(LoginActivity.this, "Полетели!", Toast.LENGTH_LONG).show();
								SendUserToMainActivity();
								progressDialog.dismiss();
							} else {
								String message = task.getException().toString();
								Toast.makeText(LoginActivity.this, "Ошибка" + message, Toast.LENGTH_LONG).show();
								progressDialog.dismiss();
							}
						}
					});
		}
	}

	private void SendUserToMainActivity() {
		Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(mainActivity);
	}

	private void SendUserToRegisterActivity() {
		Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
		startActivity(registerIntent);
	}
}
