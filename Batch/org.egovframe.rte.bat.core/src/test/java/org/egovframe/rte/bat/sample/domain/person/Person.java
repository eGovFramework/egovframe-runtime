package org.egovframe.rte.bat.sample.domain.person;

import org.egovframe.rte.bat.sample.domain.order.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * Person 엔티티
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 * 2012.06.27  배치실행개발팀     최초 생성
 * </pre>
 * @since 2012.06.27
 */
public class Person {

    private String title = "";

    private String firstName = "";

    private String last_name = "";

    private int age = 0;

    private Address address = new Address();

    private List<Child> children = new ArrayList<Child>();

    public Person() {
        children.add(new Child());
        children.add(new Child());
    }

    /**
     * @return the address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * @return the age
     */
    public int getAge() {
        return age;
    }

    /**
     * @param age the age to set
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the children
     */
    public List<Child> getChildren() {
        return children;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(List<Child> children) {
        this.children = children;
    }

    /**
     * Intentionally non-standard method name for testing purposes
     *
     * @return the last_name
     */
    public String getLast_name() {
        return last_name;
    }

    /**
     * Intentionally non-standard method name for testing purposes
     *
     * @param last_name the last_name to set
     */
    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    /**
     * @return the person_title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the person title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Person [address=" + address + ", age=" + age + ", children=" + children + ", firstName=" + firstName + ", last_name=" + last_name + ", title=" + title + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + age;
        result = prime * result + ((children == null) ? 0 : children.hashCode());
        result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + ((last_name == null) ? 0 : last_name.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        Person other = (Person) obj;

        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address)) {
            return false;
        }

        if (age != other.age)
            return false;

        if (children == null) {
            if (other.children != null)
                return false;
        } else if (!children.equals(other.children)) {
            return false;
        }

        if (firstName == null) {
            if (other.firstName != null)
                return false;
        } else if (!firstName.equals(other.firstName)) {
            return false;
        }

        if (last_name == null) {
            if (other.last_name != null)
                return false;
        } else if (!last_name.equals(other.last_name)) {
            return false;
        }

        if (title == null) {
            return other.title == null;
        } else return title.equals(other.title);
    }

}
