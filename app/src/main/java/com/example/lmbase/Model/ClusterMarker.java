package com.example.lmbase.Model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {

	private LatLng position;
	private String title;
	private String snippet;

	public ClusterMarker(double lat, double lng, String title, String snippet) {
		position = new LatLng(lat, lng);
		this.title = title;
		this.snippet = snippet;
	}

	public ClusterMarker() {
	}

	@NonNull
	@Override
	public LatLng getPosition() {
		return position;
	}

	public void setPosition(LatLng position) {
		this.position = position;
	}

	@Nullable
	@Override
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Nullable
	@Override
	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

}
