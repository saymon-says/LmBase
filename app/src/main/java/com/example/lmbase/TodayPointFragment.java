package com.example.lmbase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lmbase.Model.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodayPointFragment extends Fragment {

	private DatabaseReference usersRef;
	private RecyclerView userList;

	public TodayPointFragment() {
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_today_point, container, false);
		userList = v.findViewById(R.id.recycler_today);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
		linearLayoutManager.setReverseLayout(true);
		linearLayoutManager.setStackFromEnd(true);
		userList.setLayoutManager(linearLayoutManager);
		return v;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
	}

	@Override
	public void onStart() {
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
				holder.todayPoint.setText(String.valueOf(model.getResultPointToday()));
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

		TextView username, userfullname, todayPoint;
		CircleImageView userpic;

		public UsersViewHolder(@NonNull View itemView) {
			super(itemView);
			username = itemView.findViewById(R.id.username_list);
			userfullname = itemView.findViewById(R.id.user_fullname_list);
			userpic = itemView.findViewById(R.id.userpic_list);
			todayPoint = itemView.findViewById(R.id.result_point);
		}
	}
}