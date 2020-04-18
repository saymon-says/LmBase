package com.example.lmbase;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav);
        View navView = navigationView.inflateHeaderView(R.layout.heade_nav);
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Домашняя страница");

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSElector(item);
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null) {
            SendUserToLoginActivity();
        } else {
        	CheckUserInBase();
		}
    }

	private void CheckUserInBase() {
    	final String currentUserId = mAuth.getCurrentUser().getUid();
    	usersRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(!dataSnapshot.hasChild(currentUserId)) {
					SendUserToSetupActivity();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void SendUserToSetupActivity() {
		Intent setupActivity = new Intent(MainActivity.this, SetupActivity.class);
		startActivity(setupActivity);
		finish();
	}

	private void SendUserToLoginActivity() {

        Intent loginActivity = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginActivity);
        finish();
    }

    private void UserMenuSElector(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                Toast.makeText(this, "Домой", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_tor_list:
                Toast.makeText(this, "Список торговых", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_static:
                Toast.makeText(this, "Статистика", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }
}
