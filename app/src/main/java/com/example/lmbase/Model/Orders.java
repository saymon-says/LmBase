package com.example.lmbase.Model;

public class Orders {
	String numberOrder, priceOrder, bayoutOrder, uid, point, delivery;

	public Orders(String numberOrder, String priceOrder, String bayoutOrder, String uid, String point, String delivery) {
		this.numberOrder = numberOrder;
		this.priceOrder = priceOrder;
		this.bayoutOrder = bayoutOrder;
		this.uid = uid;
		this.point = point;
		this.delivery = delivery;
	}

	public Orders() {
	}

	public String getDelivery() {
		return delivery;
	}

	public void setDelivery(String delivery) {
		this.delivery = delivery;
	}

	public String getPoint() {
		return point;
	}

	public void setPoint(String point) {
		this.point = point;
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
