package com.example.lmbase;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

	private EditText userAlias, userFullname, workShift, reitUser;
	private CircleImageView userpic;
	private ProgressDialog progressDialog;

	private DatabaseReference userRef;
	private String currentUserId;
	private StorageReference userpicRef;

	final static int gallery_pic = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);

		userAlias = findViewById(R.id.user_alias);
		userFullname = findViewById(R.id.user_fullname);
		workShift = findViewById(R.id.settings_workshift_count);
		reitUser = findViewById(R.id.settings_reit);
		Button saveButton = findViewById(R.id.button_save_setup);
		userpic = findViewById(R.id.userpic);
		progressDialog = new ProgressDialog(this);

		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();
		userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
		userpicRef = FirebaseStorage.getInstance().getReference().child("User Pic");

		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowExistsUserPic();
			}
		});

		userpic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent galleryIntent = new Intent();
				galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
				galleryIntent.setType("image/*");
				startActivityForResult(galleryIntent, gallery_pic);
			}
		});

		userRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(dataSnapshot.exists()) {
					if(dataSnapshot.hasChild("userpic")) {
						String image = dataSnapshot.child("userpic").getValue().toString();
						Picasso.get().load(image).placeholder(R.drawable.anonymous).into(userpic);
					}
				} else {
					Toast.makeText(SetupActivity.this, "Сначала выбери аватар", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

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
		if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
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
						if(task.isSuccessful()) {
							filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
								@Override
								public void onSuccess(Uri uri) {
									final String downloadUrl = uri.toString();
									userRef.child("userpic").setValue(downloadUrl)
											.addOnCompleteListener(new OnCompleteListener<Void>() {
												@Override
												public void onComplete(@NonNull Task<Void> task) {
													if (task.isSuccessful()) {
														Toast.makeText(SetupActivity.this, "Фото загружено", Toast.LENGTH_SHORT).show();
														progressDialog.dismiss();
													} else {
														String message = task.getException().toString();
														Toast.makeText(SetupActivity.this, "Ошибка" + message, Toast.LENGTH_LONG).show();
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

	private void SaveSetupInfoUser() {

		String userAliasSetup = userAlias.getText().toString();
		String userFullnameSetup = userFullname.getText().toString();
		String userWorkshiftCounts = workShift.getText().toString();
		String userReitCounts = reitUser.getText().toString();

		if(userAliasSetup.length() < 3) {
			Toast.makeText(this, "Псевдоним слишком короткий", Toast.LENGTH_SHORT).show();
		} else if (userFullnameSetup.length() < 8) {
			Toast.makeText(this, "Коротковато Ф.И.О.", Toast.LENGTH_SHORT).show();
		} else if (userWorkshiftCounts.length() == 0) {
			Toast.makeText(this, "В среднем в месяце 15 смен", Toast.LENGTH_SHORT).show();
		} else  if (userReitCounts.length() == 0) {
			Toast.makeText(this, "В начале рейтинг 1 подойдет", Toast.LENGTH_SHORT).show();
		} else {

			int userWorkShiftCountsInt = Integer.parseInt(userWorkshiftCounts);
			double userReitCountsDouble = Double.parseDouble(userReitCounts);

			progressDialog.setTitle("Сохраняемся..");
			progressDialog.setMessage("Падажжиии...Ща все будет!");
			progressDialog.show();
			progressDialog.setCanceledOnTouchOutside(true);

			HashMap usersMap = new HashMap();
			usersMap.put("alias", userAliasSetup);
			usersMap.put("fullname", userFullnameSetup);
			usersMap.put("workshift", userWorkShiftCountsInt);
			usersMap.put("reit", userReitCountsDouble);
			userRef.updateChildren(usersMap)
					.addOnCompleteListener(new OnCompleteListener() {
						@Override
						public void onComplete(@NonNull Task task) {
							if(task.isSuccessful()) {
								Toast.makeText(SetupActivity.this, "Полетели!", Toast.LENGTH_SHORT).show();
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

	private void ShowExistsUserPic() {
		userRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(dataSnapshot.exists()) {
					if(dataSnapshot.hasChild("userpic")) {
						String image = dataSnapshot.child("userpic").getValue().toString();
						Picasso.get().load(image).placeholder(R.drawable.anonymous).into(userpic);
						SaveSetupInfoUser();
					}
				} else {
					Toast.makeText(SetupActivity.this, "Сначала выбери аватар", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}


	private void SendUserToMainActivity() {
		Intent mainActivity = new Intent(SetupActivity.this, MainActivity.class);
		startActivity(mainActivity);
	}
}
