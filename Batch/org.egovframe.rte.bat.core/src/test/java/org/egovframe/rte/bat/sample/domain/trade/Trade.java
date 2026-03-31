package org.egovframe.rte.bat.sample.domain.trade;

import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Trade 엔티티
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
public class Trade implements Serializable {

    private static final long serialVersionUID = -5557049363045084134L;

    @Size(max = 12, message = "ISIN too long")
    private String isin = "";
    private long quantity = 0;
    private BigDecimal price = new BigDecimal(0);
    private String customer = "";
    private Long id;
    private long version = 0;

    public Trade() {
    }

    public Trade(String isin, long quantity, BigDecimal price, String customer) {
        this.isin = isin;
        this.quantity = quantity;
        this.price = price;
        this.customer = customer;
    }

    public Trade(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    @Override
    public String toString() {
        return "Trade: [isin=" + this.isin + ",quantity=" + this.quantity + ",price=" + this.price + ",customer=" + this.customer + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Trade t)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        return isin.equals(t.getIsin()) && quantity == t.getQuantity() && price.equals(t.getPrice()) && customer.equals(t.getCustomer());
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
