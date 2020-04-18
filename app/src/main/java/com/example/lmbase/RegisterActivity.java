package com.example.lmbase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

	private EditText emailRegister, passwordRegister, confirmPasswordRegister;
	private Button buttonRegister;
	private ProgressDialog loadingDialog;

	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		mAuth = FirebaseAuth.getInstance();

		emailRegister = findViewById(R.id.email_register);
		passwordRegister = findViewById(R.id.password_register);
		confirmPasswordRegister = findViewById(R.id.confirm_password_register);
		buttonRegister = findViewById(R.id.button_register);
		loadingDialog = new ProgressDialog(this);

		buttonRegister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CreateNewUserAccount();
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

	private void SendUserToMainActivity() {
		Intent mainActivity = new Intent(RegisterActivity.this, MainActivity.class);
		startActivity(mainActivity);
	}

	private void CreateNewUserAccount() {

		String confirmPasswordRegisterUser = confirmPasswordRegister.getText().toString();
		String passwordRegisterUser = passwordRegister.getText().toString();
		String emailRegisterUser = emailRegister.getText().toString();

		if(emailRegisterUser.length() < 3) {
			Toast.makeText(this, "Email слишком короткий", Toast.LENGTH_LONG).show();
		}
		else if(passwordRegisterUser.length() < 6) {
			Toast.makeText(this, "Пароль слишком короткий", Toast.LENGTH_LONG).show();
		}
		else if(confirmPasswordRegisterUser.length() < 6) {
			Toast.makeText(this, "Пароль слишком короткий", Toast.LENGTH_LONG).show();
		}
		else if(!passwordRegisterUser.equals(confirmPasswordRegisterUser)) {
			Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_LONG).show();
		}
		else {

			loadingDialog.setTitle("Аккаунт создается..");
			loadingDialog.setMessage("Падажжиии...");
			loadingDialog.show();
			loadingDialog.setCanceledOnTouchOutside(true);

			mAuth.createUserWithEmailAndPassword(emailRegisterUser, passwordRegisterUser)
					.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {

							if(task.isSuccessful()) {
								Toast.makeText(RegisterActivity.this, "Аккаунт создан", Toast.LENGTH_LONG).show();
								loadingDialog.dismiss();
								SendUserToSetupActivity();
							} else {
								String message = task.getException().toString();
								Toast.makeText(RegisterActivity.this, "Ошибка" + message, Toast.LENGTH_LONG).show();
								loadingDialog.dismiss();
							}
						}
					});
		}
	}
	private void SendUserToSetupActivity() {
		Intent setupActivity = new Intent(RegisterActivity.this, SetupActivity.class);
		startActivity(setupActivity);
		finish();
	}
}
