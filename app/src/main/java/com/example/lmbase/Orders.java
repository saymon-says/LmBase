package com.example.lmbase;

public class Orders {
	String numberOrder, priceOrder, bayoutOrder, uid;

	public Orders(String numberOrder, String priceOrder, String bayoutOrder, String uid) {
		this.numberOrder = numberOrder;
		this.priceOrder = priceOrder;
		this.bayoutOrder = bayoutOrder;
		this.uid = uid;
	}

	public Orders() {
	}

	public String getNumberOrder() {
		return numberOrder;
	}

	public void setNumberOrder(String numberOrder) {
		this.numberOrder = numberOrder;
	}

	public String getPriceOrder() {
		return priceOrder;
	}

	public void setPriceOrder(String priceOrder) {
		this.priceOrder = priceOrder;
	}

	public String getBayoutOrder() {
		return bayoutOrder;
	}

	public void setBayoutOrder(String bayoutOrder) {
		this.bayoutOrder = bayoutOrder;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
