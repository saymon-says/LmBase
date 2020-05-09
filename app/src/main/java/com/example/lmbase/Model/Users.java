package com.example.lmbase.Model;

public class Users {
	String alias, fullname, userpic, currentDateOrderList;
	Double resultPointToday, resultMonthPoint;

	public Users() {
	}

	public Users(String alias, String fullname, String userpic, String currentDateOrderList, Double resultPointToday, Double resultMonthPoint) {
		this.alias = alias;
		this.fullname = fullname;
		this.userpic = userpic;
		this.currentDateOrderList = currentDateOrderList;
		this.resultPointToday = resultPointToday;
		this.resultMonthPoint = resultMonthPoint;
	}

	public String getCurrentDateOrderList() {
		return currentDateOrderList;
	}

	public void setCurrentDateOrderList(String currentDateOrderList) {
		this.currentDateOrderList = currentDateOrderList;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getUserpic() {
		return userpic;
	}

	public void setUserpic(String userpic) {
		this.userpic = userpic;
	}

	public Double getResultPointToday() {
		return resultPointToday;
	}

	public void setResultPointToday(Double resultPointToday) {
		this.resultPointToday = resultPointToday;
	}

	public Double getResultMonthPoint() {
		return resultMonthPoint;
	}

	public void setResultMonthPoint(Double resultMonthPoint) {
		this.resultMonthPoint = resultMonthPoint;
	}
}
