package org.egovframe.rte.bat.sample.domain.order;

/**
 * Address 엔티티
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
public class Address {

    public static final String LINE_ID_BILLING_ADDR = "BAD";

    public static final String LINE_ID_SHIPPING_ADDR = "SAD";

    private String addressee;

    private String addrLine1;

    private String addrLine2;

    private String city;

    private String zipCode;

    private String state;

    private String country;

    public String getAddrLine1() {
        return addrLine1;
    }

    public void setAddrLine1(String addrLine1) {
        this.addrLine1 = addrLine1;
    }

    public String getAddrLine2() {
        return addrLine2;
    }

    public void setAddrLine2(String addrLine2) {
        this.addrLine2 = addrLine2;
    }

    public String getAddressee() {
        return addressee;
    }

    public void setAddressee(String addressee) {
        this.addressee = addressee;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Override
    public String toString() {
        return "Address [addressee=" + addressee + ", city=" + city + ", country=" + country + ", state=" + state + ", zipCode=" + zipCode + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((addressee == null) ? 0 : addressee.hashCode());
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        result = prime * result + ((zipCode == null) ? 0 : zipCode.hashCode());
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

        Address other = (Address) obj;

        if (addressee == null) {
            if (other.addressee != null)
                return false;
        } else if (!addressee.equals(other.addressee)) {
            return false;
        }

        if (country == null) {
            if (other.country != null)
                return false;
        } else if (!country.equals(other.country)) {
            return false;
        }

        if (zipCode == null) {
            return other.zipCode == null;
        } else return zipCode.equals(other.zipCode);
    }

}
