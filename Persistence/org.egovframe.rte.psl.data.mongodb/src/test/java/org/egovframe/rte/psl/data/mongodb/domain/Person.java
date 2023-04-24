package org.egovframe.rte.psl.data.mongodb.domain;

import org.springframework.data.annotation.Id;

public class Person {

	@Id
	private String id;

	private String name;

	private int age;

	private Address address;

	private double[] location;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public double[] getLocation() {
		return location;
	}

	public void setLocation(double[] location) {
		this.location = location;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			if (this.id != null && (this.id.equals(((Person)obj).id))) {
				return true;
			}
		}
		return false;
	}

}
