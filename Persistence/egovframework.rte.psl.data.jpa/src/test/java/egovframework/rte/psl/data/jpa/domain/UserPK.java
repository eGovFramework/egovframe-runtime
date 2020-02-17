package egovframework.rte.psl.data.jpa.domain;

import java.io.Serializable;

public class UserPK implements Serializable {
	private static final long serialVersionUID = 1L;	
	
	private Long id;
	
//	public int hashCode(){return 0;};
//	public boolean equals(Object anObject) {return true;};
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
    	  
//    	 if (this == obj)
//             return true;
//         if (obj == null)
//             return false;
//         if (getClass() != obj.getClass())
//             return false;
//         
//         UserPK other = (UserPK) obj;
//         if (userId == null) {
//             if (other.userId != null)
//                 return false;
//         } else if (!userId.equals(other.userId))
//             return false;
//         return true;
    }


}
