/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package egovframework.brte.sample.common.domain.trade;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CustomerCredit 엔티티
 * @author 배치실행개발팀
 * @since 2012. 07.25
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *   
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012. 07.25  배치실행개발팀     최초 생성
 *  </pre>
 */

@Entity
@Table(name = "CUSTOMER")
public class CustomerCredit {

	// field "id"
	@Id
	private int id;

	// field "name"
	private String name;

	// field "credit"
	private BigDecimal credit;

	/**
	 * 생성자
	 */
	public CustomerCredit() {
	}

	/**
	 * 생성자
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
	 * @return
	 */
	public BigDecimal getCredit() {
		return credit;
	}

	/**
	 * id getter
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * id setter
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * credit setter
	 * @param credit
	 */
	public void setCredit(BigDecimal credit) {
		this.credit = credit;
	}

	/**
	 * name getter
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * name setter
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * credit field를 증가
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
