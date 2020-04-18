package com.example.lmbase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

	private EditText userAlias, userFullname;
	private Button saveButton;
	private CircleImageView userpic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);

		userAlias = findViewById(R.id.user_alias);
		userFullname = findViewById(R.id.user_Fullname);
		saveButton = findViewById(R.id.button_save_setup);
		userpic = findViewById(R.id.userpic);
	}
}
