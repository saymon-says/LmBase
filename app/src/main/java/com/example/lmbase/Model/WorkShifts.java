package com.example.lmbase.Model;

public class WorkShifts {

	String date;
	Double resultPoint;

	public WorkShifts(String date, Double resultPoint) {
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

	public Double getResultPoint() {
		return resultPoint;
	}

	public void setResultPoint(Double resultPoint) {
		this.resultPoint = resultPoint;
	}
}
