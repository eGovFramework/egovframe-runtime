package org.egovframe.rte.psl.data.jpa.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@IdClass(UserPK.class)
public class User {

	private static final long serialVersionUID = 1088577580599262864L;
	
	@Id
	@GeneratedValue(strategy =  GenerationType.AUTO)
	private Long id;
	
	@Column(unique = true)
	private String username;
	private String firstname;
	private String lastname;
	private Date startDate;
	private int age;
	
	private boolean active;
	
	public Long getId() 
	{
		return id;
	}

	public void setId(Long id) 
	{
		this.id = id;
	}
	 
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
}
