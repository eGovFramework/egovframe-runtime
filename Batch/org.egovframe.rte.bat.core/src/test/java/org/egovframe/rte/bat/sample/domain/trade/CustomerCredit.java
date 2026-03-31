package org.egovframe.rte.bat.sample.domain.trade;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.math.BigDecimal;

/**
 * CustomerCredit 엔티티
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012. 07.25  배치실행개발팀     최초 생성
 *  </pre>
 * @since 2012. 07.25
 */
@Entity
@Table(name = "CUSTOMER")
@XmlRootElement(name = "customer")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomerCredit {

    @Id
    @XmlElement
    private int id;

    @XmlElement
    private String name;

    @XmlElement
    private BigDecimal credit;

    /**
     * 생성자
     */
    public CustomerCredit() {
    }

    /**
     * 생성자
     *
     * @param id
     * @param name
     * @param credit
     */
    public CustomerCredit(int id, String name, BigDecimal credit) {
        this.id = id;
        this.name = name;
        this.credit = credit;
    }

    /**
     * field 내용을 String으로 return
     */
    public String toString() {
        return "CustomerCredit [id=" + id + ",name=" + name + ", credit=" + credit + "]";
    }

    /**
     * credit getter
     *
     * @return
     */
    public BigDecimal getCredit() {
        return credit;
    }

    /**
     * credit setter
     *
     * @param credit
     */
    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    /**
     * id getter
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * id setter
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * name getter
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * name setter
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * credit field를 증가
     *
     * @param sum credit에 더하는 수
     * @return credit이 증가된 새로운 객체
     */
    public CustomerCredit increaseCreditBy(BigDecimal sum) {
        CustomerCredit newCredit = new CustomerCredit();
        newCredit.credit = this.credit.add(sum);
        newCredit.name = this.name;
        newCredit.id = this.id;
        return newCredit;
    }

    /**
     * Object가 CustomerCredit인지 판단
     */
    public boolean equals(Object o) {
        return (o instanceof CustomerCredit) && ((CustomerCredit) o).id == id;
    }

    /**
     * id
     */
    public int hashCode() {
        return id;
    }

}
