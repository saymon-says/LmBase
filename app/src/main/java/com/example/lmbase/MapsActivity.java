package com.example.lmbase;

import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

	private GoogleMap mMap;
	private CameraPosition mCameraPosition;
	private Button addMarker;
	private String finalAddress, finalRefinement, finalComment;
	private EditText addressClient, commentClient, refinementClient;
	private DatabaseReference troubleClient;
	private String currentUserId;
	private FirebaseAuth mAuth;
	private Double clientLatitude, clientLongitude;

	// The entry point to the Fused Location Provider.
	private FusedLocationProviderClient mFusedLocationProviderClient;

	// A default location (Moscow, Russia) and default zoom to use when location permission is
	// not granted.
	private final LatLng mDefaultLocation = new LatLng(55.752554, 37.618923);
	private static final int DEFAULT_ZOOM = 10;
	private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
	private boolean mLocationPermissionGranted;

	// The geographical location where the device is currently located. That is, the last-known
	// location retrieved by the Fused Location Provider.
	private Location mLastKnownLocation;

	// Keys for storing activity state.
	private static final String KEY_CAMERA_POSITION = "camera_position";
	private static final String KEY_LOCATION = "location";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);

		addMarker = findViewById(R.id.add_marker);
		mAuth = FirebaseAuth.getInstance();
		currentUserId = mAuth.getCurrentUser().getUid();
		troubleClient = FirebaseDatabase.getInstance().getReference().child("Trouble Client List");

		// Retrieve location and camera position from saved instance state.
		if (savedInstanceState != null) {
			mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
			mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
		}

		// Construct a FusedLocationProviderClient.
		mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	/**
	 * Saves the state of the map when the activity is paused.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mMap != null) {
			outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
			outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
			super.onSaveInstanceState(outState);
		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		addMarker.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowAddProblemClientMarkerPopUp();
			}
		});

		ShowProblemClientMarker();

		// Use a custom info window adapter to handle multiple lines of text in the
		// info window contents.
		mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

			@Override
			// Return null here, so that getInfoContents() is called next.
			public View getInfoWindow(Marker arg0) {
				return null;
			}

			@Override
			public View getInfoContents(Marker marker) {
				// Inflate the layouts for the info window, title and snippet.
				View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
						(FrameLayout) findViewById(R.id.map), false);

				TextView title = infoWindow.findViewById(R.id.title);
				title.setText(marker.getTitle());

				TextView snippet = infoWindow.findViewById(R.id.snippet);
				snippet.setText(marker.getSnippet());

				return infoWindow;
			}
		});

		// Prompt the user for permission.
		getLocationPermission();

		// Turn on the My Location layer and the related control on the map.
		updateLocationUI();

		// Get the current location of the device and set the position of the map.
		getDeviceLocation();
	}

	private void ShowAddProblemClientMarkerPopUp() {

		final AlertDialog.Builder client = new AlertDialog.Builder(this);
		View mView = getLayoutInflater().inflate(R.layout.add_marker_popup, null);
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
		mMap.addMarker(new MarkerOptions()
				.position(new LatLng(55.775043, 37.629152))
				.title("15 кв, по 3 позиции!")).setSnippet("Выдавать по 3 позиции! Потому что меряет пол часа 3 вещи");
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
			Log.e("Exception: %s", e.getMessage());
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
		switch (requestCode) {
			case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					mLocationPermissionGranted = true;
				}
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
			Log.e("Exception: %s", e.getMessage());
		}
	}

//
//		mMap.addMarker(new MarkerOptions()
//				.position(new LatLng(55.774444, 37.641755))
//				.title("1203-1266 кв, по 3 позиции!!"));
//
//		mMap.addMarker(new MarkerOptions()
//				.position(new LatLng(55.823665, 37.658606))
//				.title("287 кв, не ездить!"));
//
//		mMap.addMarker(new MarkerOptions()
//				.position(new LatLng(55.829505, 37.596218))
//				.title("78 кв, по 3 позиции!"));
//
//		mMap.addMarker(new MarkerOptions()
//				.position(new LatLng(55.755860, 37.403027))
//				.title("176 кв, по 3 позиции!"));
//
//		mMap.addMarker(new MarkerOptions()
//				.position(new LatLng(55.741313, 37.417692))
//				.title("144 кв")).setSnippet("Выдавать только по 3 позиции");
//	}
}
