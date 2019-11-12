package com.datastax.alexott.demos.product;

import com.datastax.driver.mapping.annotations.UDT;

@UDT(keyspace = "test", name = "information")
public class Information {
	String info1;
	String info2;
	
	public String getInfo1() {
		return info1;
	}
	public void setInfo1(String info1) {
		this.info1 = info1;
	}
	public String getInfo2() {
		return info2;
	}
	public void setInfo2(String info2) {
		this.info2 = info2;
	}
	@Override
	public String toString() {
		return "Information [info1=" + info1 + ", info2=" + info2 + "]";
	}
	
}
