package com.example.lmbase;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

	private GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}


	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(55.752554, 37.618923))
				.zoom(10)
				.bearing(0)
				.tilt(20)
				.build();
		CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
		mMap.animateCamera(cameraUpdate);

		mMap.addMarker(new MarkerOptions()
				.position(new LatLng(55.775043, 37.629152))
				.title("15 кв, по 3 позиции!"));

		mMap.addMarker(new MarkerOptions()
				.position(new LatLng(55.774444, 37.641755))
				.title("1203-1266 кв, по 3 позиции!!"));

		mMap.addMarker(new MarkerOptions()
				.position(new LatLng(55.823665, 37.658606))
				.title("287 кв, не ездить!"));

		mMap.addMarker(new MarkerOptions()
				.position(new LatLng(55.829505, 37.596218))
				.title("78 кв, по 3 позиции!"));

		mMap.addMarker(new MarkerOptions()
				.position(new LatLng(55.755860, 37.403027))
				.title("176 кв, по 3 позиции!"));

		mMap.addMarker(new MarkerOptions()
				.position(new LatLng(55.741313, 37.417692))
				.title("144 кв, по 3 позиции!"));
	}
}
