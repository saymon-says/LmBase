package com.example.lmbase.Model;

public class WorkShifts {

	String date, resultPoint;

	public WorkShifts(String date, String resultPoint) {
		this.date = date;
		this.resultPoint = resultPoint;
	}

	public WorkShifts() {

	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getResultPoint() {
		return resultPoint;
	}

	public void setResultPoint(String resultPoint) {
		this.resultPoint = resultPoint;
	}
}
