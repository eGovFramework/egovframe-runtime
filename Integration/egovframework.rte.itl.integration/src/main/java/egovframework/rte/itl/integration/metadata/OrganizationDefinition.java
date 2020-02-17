/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package egovframework.rte.itl.integration.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import egovframework.rte.itl.integration.util.Validatable;

import org.springframework.util.StringUtils;

/**
 * 연계 서비스 메타 데이터 중 '기관' 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 메타 데이터 중 '기관'를 나타내는 class이다.
 * </p>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.06.01  심상호           최초 생성
 * 
 * </pre>
 */
public class OrganizationDefinition implements Validatable {
	/** 기관 ID */
	private String id;

	/** 기관명 */
	private String name;

	/** 소속 시스템 */
	private Map<String, SystemDefinition> systems = new HashMap<String, SystemDefinition>();

	/** valid */
	private boolean valid = false;

	/** statucChanged flag */
	private AtomicBoolean statusChanged = new AtomicBoolean(false);

	/**
	 * Default Constructor
	 */
	public OrganizationDefinition() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            기관 ID
	 * @param name
	 *            기관명
	 */
	public OrganizationDefinition(String id, String name) {
		super();
		this.id = id;
		this.name = name;
		this.statusChanged.set(true);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            기관 ID
	 * @param name
	 *            기관명
	 * @param systems
	 *            소속 시스템
	 */
	public OrganizationDefinition(String id, String name,
			Map<String, SystemDefinition> systems) {
		super();
		this.id = id;
		this.name = name;
		this.systems = systems;
		this.statusChanged.set(true);
	}

	/**
	 * 기관 ID
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * 기관 ID
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
		this.statusChanged.set(true);
	}

	/**
	 * 기관명
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 기관명
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
		this.statusChanged.set(true);
	}

	/**
	 * 소속시스템
	 * 
	 * @return the systems
	 */
	public Map<String, SystemDefinition> getSystems() {
		return systems;
	}

	/**
	 * 소속시스템
	 * 
	 * @param systems
	 *            the systems to set
	 */
	public void setSystems(Map<String, SystemDefinition> systems) {
		this.systems = systems;
		this.statusChanged.set(true);
	}

	/**
	 * SystemDefinition을 읽어온다.
	 * 
	 * @param systemId
	 *            systemId
	 * @return SystemDefinition
	 */
	public SystemDefinition getSystemDefinition(String systemId) {
		return systems.get(systemId);
	}

	public boolean isValid() {
		if (statusChanged.getAndSet(false)) {
			valid = (StringUtils.hasText(id) && StringUtils.hasText(name) && systems != null);
			if (systems != null) {
				for (SystemDefinition system : systems.values()) {
					valid = valid && system.isValid();
				}
			}
		}
		return valid;
	}
//	CHECKSTYLE:OFF
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getName()).append(" {").append("\n\tid = ")
				.append(StringUtils.quote(id)).append("\n\tname = ")
				.append(StringUtils.quote(name));
		if (systems == null) {
			sb.append("\n\tsystems = null");
		} else {
			sb.append("\n\tsystems = {");
			for (Entry<String, SystemDefinition> entry : systems.entrySet()) {
				sb.append("\n\t\t<key = ")
						.append(StringUtils.quote(entry.getKey()))
						.append(", value = ")
						.append(entry.getValue() == null ? "" : "\n")
						.append(entry.getValue()).append(">");
			}
			sb.append("\n\t}");
		}
		sb.append("\n}");
		return sb.toString();
	}
//	CHECKSTYLE:ON
}
