package org.egovframe.rte.psl.data.jpa.domain;

import java.io.Serializable;

public class UserPK implements Serializable {
	private static final long serialVersionUID = 1L;	
	
	private Long id;

	public UserPK(){}
	
	public UserPK(Long userId) 
	{
		this.id = userId;
	}
	
	public Long getId() 
	{
		return id;
	}

	public void setId(Long id) 
	{
		this.id = id;
	}

    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object obj) {
    	  return true;
    }

}
