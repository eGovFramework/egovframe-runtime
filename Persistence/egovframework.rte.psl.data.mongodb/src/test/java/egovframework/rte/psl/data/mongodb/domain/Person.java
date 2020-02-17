package egovframework.rte.psl.data.mongodb.domain;

import org.springframework.data.annotation.Id;

public class Person {
	@Id
	private String id;

	private String firstname;

	private String lastname;

	private Address address;
	
	private double[] location;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			
			if (this.id != null && (this.id.equals(((Person)obj).id))) {
				return true;
			}
		}
		
		return false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
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
}
