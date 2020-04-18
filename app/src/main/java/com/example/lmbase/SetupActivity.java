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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

	private EditText userAlias, userFullname;
	private Button saveButton;
	private CircleImageView userpic;
	private ProgressDialog progressDialog;

	private FirebaseAuth mAuth;
	private DatabaseReference userRef;
	private String currentUserId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);

		userAlias = findViewById(R.id.user_alias);
		userFullname = findViewById(R.id.user_Fullname);
		saveButton = findViewById(R.id.button_save_setup);
		userpic = findViewById(R.id.userpic);
		progressDialog = new ProgressDialog(this);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();
		userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SaveSetupInfoUser();
			}
		});
	}

	private void SaveSetupInfoUser() {
		String userAliasSetup = userAlias.getText().toString();
		String userFullnameSetup = userFullname.getText().toString();

		if(userAliasSetup.length() < 3) {
			Toast.makeText(this, "Псевдоним слишком короткий", Toast.LENGTH_LONG).show();
		} else if (userFullnameSetup.length() < 8) {
			Toast.makeText(this, "Коротковато Ф.И.О.", Toast.LENGTH_LONG).show();
		} else {
			progressDialog.setTitle("Сохраняемся..");
			progressDialog.setMessage("Падажжиии...");
			progressDialog.show();
			progressDialog.setCanceledOnTouchOutside(true);

			HashMap usersMap = new HashMap();
			usersMap.put("alias", userAliasSetup);
			usersMap.put("fullname", userFullnameSetup);
			userRef.updateChildren(usersMap)
					.addOnCompleteListener(new OnCompleteListener() {
				@Override
				public void onComplete(@NonNull Task task) {
					if(task.isSuccessful()) {
						Toast.makeText(SetupActivity.this, "Полетели!", Toast.LENGTH_LONG).show();
						SendUserToMainActivity();
						progressDialog.dismiss();
					} else {
						String message = task.getException().toString();
						Toast.makeText(SetupActivity.this, "Ошибка" + message, Toast.LENGTH_LONG).show();
						progressDialog.dismiss();
					}
				}
			});
		}
	}

	private void SendUserToMainActivity() {
		Intent mainActivity = new Intent(SetupActivity.this, MainActivity.class);
		startActivity(mainActivity);
	}
}
