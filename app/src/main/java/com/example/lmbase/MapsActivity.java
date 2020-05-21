package com.example.lmbase;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.lmbase.Model.ClusterMarker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

	private static final String TAG = "Map of Trouble Client";

	private static final int DEFAULT_ZOOM = 10;
	private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
	// Keys for storing activity state.
	private static final String KEY_CAMERA_POSITION = "camera_position";
	private static final String KEY_LOCATION = "location";
	// A default location (Moscow, Russia) and default zoom to use when location permission is
	// not granted.
	private final LatLng mDefaultLocation = new LatLng(55.751590, 37.617832);
	private GoogleMap mMap;
	private String finalAddress, finalRefinement, finalComment, clientComment;
	private EditText addressClient, commentClient, refinementClient;
	private DatabaseReference troubleClient;
	private String currentUserId;
	private Button addMarker;
	private Double clientLatitude, clientLongitude;
	private ClusterManager<ClusterMarker> markerClusterManager;
	private MyClusterManagerRenderer mClusterManagerRenderer;
	// The entry point to the Fused Location Provider.
	private FusedLocationProviderClient mFusedLocationProviderClient;
	private boolean mLocationPermissionGranted;
	// The geographical location where the device is currently located. That is, the last-known
	// location retrieved by the Fused Location Provider.
	private Location mLastKnownLocation;
	private Map<ClusterMarker, Object> client = new HashMap<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);

		addMarker = findViewById(R.id.add_marker);
		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
		troubleClient = FirebaseDatabase.getInstance().getReference().child("Trouble Client List");

		// Retrieve location and camera position from saved instance state.
		if (savedInstanceState != null) {
			mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
			CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
		}

		// Construct a FusedLocationProviderClient.
		mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		assert mapFragment != null;
		mapFragment.getMapAsync(this);
	}

	/**
	 * Saves the state of the map when the activity is paused.
	 */
	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		if (mMap != null) {
			outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
			outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
			super.onSaveInstanceState(outState);
		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		markerClusterManager = new ClusterManager<>(getApplicationContext(), mMap);

		mMap.setOnCameraIdleListener(markerClusterManager);
		mMap.setOnMarkerClickListener(markerClusterManager);

		markerClusterManager.getMarkerCollection().setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
			@Override
			public View getInfoWindow(Marker marker) {
				View info = getLayoutInflater().inflate(R.layout.info_window_marker,
						(FrameLayout) findViewById(R.id.map), false);

				TextView address = info.findViewById(R.id.address_client);
				address.setText(marker.getTitle());

				TextView comment = info.findViewById(R.id.client_comment);
				comment.setText(marker.getSnippet());

				return info;
			}

			@Override
			public View getInfoContents(Marker marker) {
				return null;
			}
		});

//		markerClusterManager.getMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//			@Override
//			public void onInfoWindowClick(Marker marker) {
//				clientComment = marker.getSnippet();
//				ShowPopUpUserAddedClient();
////				Log.d(TAG, "click Info window: " + marker);
//			}
//		});


		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(mDefaultLocation)
				.zoom(DEFAULT_ZOOM)
				.build();
		CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
		mMap.animateCamera(cameraUpdate);

		addMarker.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowAddProblemClientMarkerPopUp();
			}
		});

		ShowProblemClientMarker();

		// Prompt the user for permission.
		getLocationPermission();

		// Turn on the My Location layer and the related control on the map.
		updateLocationUI();

		// Get the current location of the device and set the position of the map.
		getDeviceLocation();
	}

	private void ShowAddProblemClientMarkerPopUp() {

		final AlertDialog.Builder client = new AlertDialog.Builder(this);
		@SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.add_marker_popup, null);
		addressClient = mView.findViewById(R.id.client_address);
		refinementClient = mView.findViewById(R.id.client_address_refinement);
		commentClient = mView.findViewById(R.id.client_comment);
		Button addMarker = mView.findViewById(R.id.accept_btn);
		client.setView(mView);

		final AlertDialog alertDialog = client.create();
		alertDialog.setCanceledOnTouchOutside(true);
		Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


		addMarker.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finalAddress = addressClient.getText().toString();
				finalComment = commentClient.getText().toString();
				finalRefinement = refinementClient.getText().toString();

				if (finalAddress.length() != 0 && finalComment.length() != 0 && finalRefinement.length() != 0) {
					UpdateDataBaseTroubleClient();
					Toast.makeText(MapsActivity.this, "Добавлено", Toast.LENGTH_SHORT).show();
					alertDialog.dismiss();
				} else {
					Toast.makeText(MapsActivity.this, "Проверь данные!", Toast.LENGTH_SHORT).show();
				}
			}
		});
		alertDialog.show();

	}

	private void UpdateDataBaseTroubleClient() {
		Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
		List<Address> list = new ArrayList<>();
		try {
			list = geocoder.getFromLocationName(finalAddress, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (list.size() > 0) {
			clientLongitude = list.get(0).getLongitude();
			clientLatitude = list.get(0).getLatitude();
		}
		HashMap statMap = new HashMap();
		statMap.put("clientLatitude", clientLatitude);
		statMap.put("clientLongitude", clientLongitude);
		statMap.put("clientAddress", finalAddress);
		statMap.put("clientComment", finalComment);
		statMap.put("clientRefinement", finalRefinement);
		statMap.put("UID", currentUserId);
		troubleClient.child(finalAddress).updateChildren(statMap).addOnSuccessListener(new OnSuccessListener() {
			@Override
			public void onSuccess(Object o) {
			}
		});
	}

	private void ShowProblemClientMarker() {
		troubleClient.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					for (DataSnapshot ds : dataSnapshot.getChildren()) {
						Map<Marker, Object> map = (Map<Marker, Object>) ds.getValue();
						assert map != null;
//						Object clientLat = map.get("clientLatitude");
//						Object clientLong = map.get("clientLongitude");
//						Object titleClient = map.get("clientAddress");
//						Object refinementInfo = map.get("clientRefinement");
//						Object infoClient = map.get("clientComment");

//						double Lat = (double) clientLat;
//						double Lg = (double) clientLong;

//						String title = String.valueOf(titleClient);
//						String snipped = String.valueOf(infoClient);
//						String refinement = String.valueOf(refinementInfo);
//						String snippet = refinement + "\n" + snipped;
						ClusterMarker newMarker = new ClusterMarker(
								(Double) map.get("clientLatitude"),
								(Double) map.get("clientLongitude"),
								(String) map.get("clientAddress"),
								map.get("clientRefinement") + "\n" + map.get("clientComment")
						);
						markerClusterManager.addItem(newMarker);
						client.put(newMarker, newMarker);
					}
//					Log.d(TAG, "markers: " + client);
					markerClusterManager.setAnimation(true);
					markerClusterManager.cluster();
				} else {
					Toast.makeText(MapsActivity.this, "Все клиенты молодцы!", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void getDeviceLocation() {
		/*
		 * Get the best and most recent location of the device, which may be null in rare
		 * cases when a location is not available.
		 */
		try {
			if (mLocationPermissionGranted) {
				Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
				locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
					@Override
					public void onComplete(@NonNull Task<Location> task) {
						if (task.isSuccessful()) {
							// Set the map's camera position to the current location of the device.
							mLastKnownLocation = task.getResult();
							if (mLastKnownLocation != null) {
								mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
										new LatLng(mLastKnownLocation.getLatitude(),
												mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
							}
						} else {
							mMap.moveCamera(CameraUpdateFactory
									.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
							mMap.getUiSettings().setMyLocationButtonEnabled(false);
						}
					}
				});
			}
		} catch (SecurityException e) {
			Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
		}
	}

	/**
	 * Prompts the user for permission to use the device location.
	 */
	private void getLocationPermission() {
		/*
		 * Request location permission, so that we can get the location of the
		 * device. The result of the permission request is handled by a callback,
		 * onRequestPermissionsResult.
		 */
		if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
				android.Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			mLocationPermissionGranted = true;
		} else {
			ActivityCompat.requestPermissions(this,
					new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
					PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
		}
	}

	/**
	 * Handles the result of the request for location permissions.
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		mLocationPermissionGranted = false;
		if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
			if (grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				mLocationPermissionGranted = true;
			}
		}
		updateLocationUI();
	}

	/**
	 * Updates the map's UI settings based on whether the user has granted location permission.
	 */
	private void updateLocationUI() {
		if (mMap == null) {
			return;
		}
		try {
			if (mLocationPermissionGranted) {
				mMap.setMyLocationEnabled(true);
				mMap.getUiSettings().setMyLocationButtonEnabled(true);
			} else {
				mMap.setMyLocationEnabled(false);
				mMap.getUiSettings().setMyLocationButtonEnabled(false);
				mLastKnownLocation = null;
				getLocationPermission();
			}
		} catch (SecurityException e) {
			Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
		}
	}


//	private void ShowPopUpUserAddedClient() {
//
//		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//		assert inflater != null;
//		View popupView = inflater.inflate(R.layout.dialog_user_added_client, null);
//		TextView comment = popupView.findViewById(R.id.client_refinement);
//		comment.setText(clientComment);
//
//		// create the popup window
//		int width = LinearLayout.LayoutParams.WRAP_CONTENT;
//		int height = LinearLayout.LayoutParams.WRAP_CONTENT;
//
//		boolean focusable = true; // lets taps outside the popup also dismiss it
//		final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
//
//		// show the popup window
//		// which view you pass in doesn't matter, it is only used for the window tolken
//		popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 30);
//	}
}
