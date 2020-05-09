package com.example.lmbase;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

	private Toolbar mToolbar;
	private EditText userName, userFullName, workShift, reitUser;
	private Button editBtn;
	private CircleImageView userpic;
	private DatabaseReference settingsRef;
	private StorageReference userpicRef;
	private ProgressDialog progressDialog;
	private FirebaseAuth mAuth;
	private String currentUserId;
	final static int gallery_pic = 1;

	@SuppressLint("RestrictedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		progressDialog = new ProgressDialog(this);


		mToolbar = findViewById(R.id.settings_toolbar);
		setSupportActionBar(mToolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle("Редактирование профиля");
		getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

		userName = findViewById(R.id.settings_username);
		userFullName = findViewById(R.id.settings_fullname);
		workShift = findViewById(R.id.settings_workshift_count);
		reitUser = findViewById(R.id.settings_reit);
		editBtn = findViewById(R.id.upload_profile);
		userpic = findViewById(R.id.settings_userpic);

		mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();
		settingsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
		userpicRef = FirebaseStorage.getInstance().getReference().child("User Pic");


		userpic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent galleryIntent = new Intent();
				galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
				galleryIntent.setType("image/*");
				startActivityForResult(galleryIntent, gallery_pic);
			}
		});

		settingsRef.addValueEventListener(new ValueEventListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					userName.setText(Objects.requireNonNull(dataSnapshot.child("alias").getValue()).toString());
					userFullName.setText(Objects.requireNonNull(dataSnapshot.child("fullname").getValue()).toString());
					String image = Objects.requireNonNull(dataSnapshot.child("userpic").getValue()).toString();
					Picasso.get().load(image).placeholder(R.drawable.anonymous).into(userpic);
//					if (dataSnapshot.child("workshift").exists()) {
					workShift.setText(Objects.requireNonNull(dataSnapshot.child("workshift").getValue()).toString());
					reitUser.setText(dataSnapshot.child("reit").getValue().toString());
//					} else {
//						workShift.setText(15 +"");
//					}
				} else {
					Toast.makeText(SettingsActivity.this, "Nothing yet..", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		editBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditUserProfile();
			}
		});


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == gallery_pic && resultCode == RESULT_OK && data != null) {
			Uri imageUri = data.getData();
			CropImage.activity()
					.setGuidelines(CropImageView.Guidelines.ON)
					.setAspectRatio(1, 1)
					.start(this);
		}
		if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
			CropImage.ActivityResult result = CropImage.getActivityResult(data);
			if (resultCode == RESULT_OK) {

				progressDialog.setTitle("Сохраняемся..");
				progressDialog.setMessage("Падажжиии...");
				progressDialog.show();
				progressDialog.setCanceledOnTouchOutside(true);

				Uri resultUri = result.getUri();
				final StorageReference filePath = userpicRef.child(currentUserId + ".jpg");

				filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
					@Override
					public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
						if (task.isSuccessful()) {
							filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
								@Override
								public void onSuccess(Uri uri) {
									final String downloadUrl = uri.toString();
									settingsRef.child("userpic").setValue(downloadUrl)
											.addOnCompleteListener(new OnCompleteListener<Void>() {
												@Override
												public void onComplete(@NonNull Task<Void> task) {
													if (task.isSuccessful()) {
														Toast.makeText(SettingsActivity.this, "Фото загружено", Toast.LENGTH_LONG).show();
														progressDialog.dismiss();
													} else {
														String message = task.getException().toString();
														Toast.makeText(SettingsActivity.this, "Ошибка" + message, Toast.LENGTH_LONG).show();
														progressDialog.dismiss();
													}
												}
											});
								}
							});
						}
					}
				});
			} else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
				Exception error = result.getError();
				progressDialog.dismiss();
			}
		}
	}

	private void EditUserProfile() {
		String userAlias = userName.getText().toString();
		String userFullname = userFullName.getText().toString();
		int userWorkshiftCounts = Integer.parseInt(workShift.getText().toString());
		double userReitCounts = Double.parseDouble(reitUser.getText().toString());

		if (userAlias.length() < 3) {
			Toast.makeText(this, "Псевдоним слишком короткий", Toast.LENGTH_LONG).show();
		} else if (userFullname.length() < 8) {
			Toast.makeText(this, "Коротковато Ф.И.О.", Toast.LENGTH_LONG).show();
		} else if (userWorkshiftCounts == 0) {
			Toast.makeText(this, "Проверь смены!", Toast.LENGTH_SHORT).show();
		} else  if (userReitCounts == 0) {
			Toast.makeText(this, "Рейтинг маловат", Toast.LENGTH_SHORT).show();
		} else {
			progressDialog.setTitle("Сохраняемся..");
			progressDialog.setMessage("Падажжиии...Ща все будет");
			progressDialog.show();
			progressDialog.setCanceledOnTouchOutside(true);

			HashMap usersMap = new HashMap();
			usersMap.put("alias", userAlias);
			usersMap.put("fullname", userFullname);
			usersMap.put("workshift", userWorkshiftCounts);
			usersMap.put("reit", userReitCounts);
			settingsRef.updateChildren(usersMap)
					.addOnCompleteListener(new OnCompleteListener() {
						@Override
						public void onComplete(@NonNull Task task) {
							if (task.isSuccessful()) {
								Toast.makeText(SettingsActivity.this, "Полетели!", Toast.LENGTH_LONG).show();
								SendUserToMainActivity();
								progressDialog.dismiss();
							} else {
								String message = task.getException().toString();
								Toast.makeText(SettingsActivity.this, "Ошибка" + message, Toast.LENGTH_LONG).show();
								progressDialog.dismiss();
							}
						}
					});
		}
	}

	private void SendUserToMainActivity() {
		Intent mainActivity = new Intent(SettingsActivity.this, MainActivity.class);
		startActivity(mainActivity);
	}
}
