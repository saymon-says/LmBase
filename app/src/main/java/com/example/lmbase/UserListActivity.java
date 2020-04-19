package com.example.lmbase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListActivity extends AppCompatActivity {

	private RecyclerView userList;
	private Toolbar mToolbar;
	private DatabaseReference usersRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_list);

		userList = findViewById(R.id.user_list);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		linearLayoutManager.setReverseLayout(true);
		linearLayoutManager.setStackFromEnd(true);
		userList.setLayoutManager(linearLayoutManager);

		usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

		mToolbar = findViewById(R.id.users_page_toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle("Список торговых представителей");
	}

	@Override
	protected void onStart() {
		super.onStart();
		DisplayAllUsers();
	}

	private void DisplayAllUsers() {
		FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
				.setQuery(usersRef, Users.class)
				.build();

		FirebaseRecyclerAdapter<Users, UsersViewHolder> adapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
			@Override
			protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {
				holder.username.setText(model.getAlias());
				holder.userfullname.setText(model.getFullname());
				Picasso.get().load(model.getUserpic()).into(holder.userpic);
			}

			@NonNull
			@Override
			public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list, parent, false);
				UsersViewHolder usersViewHolder = new UsersViewHolder(view);
				return usersViewHolder;
			}
		};
		userList.setAdapter(adapter);
		adapter.startListening();
	}

	public static class UsersViewHolder extends RecyclerView.ViewHolder {

		TextView username, userfullname;
		CircleImageView userpic;

		public UsersViewHolder(@NonNull View itemView) {
			super(itemView);
			username = itemView.findViewById(R.id.username_list);
			userfullname = itemView.findViewById(R.id.user_fullname_list);
			userpic = itemView.findViewById(R.id.userpic_list);
		}
	}
}
