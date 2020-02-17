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
package egovframework.rte.itl.integration.message.typed;

import java.util.ListIterator;

import egovframework.rte.itl.integration.type.ListType;

/**
 * 연계 서비스의 표준 메시지 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지 Instance를 위한 Class이다. TypedList의
 * ListIterator class이다.
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
final class TypedListIterator implements ListIterator<Object> {
	/** type */
	private ListType type;

	/** inner */
	private ListIterator<Object> inner;

	/**
	 * <code>list</code>에 대한 TypedListIterator를 생성한다.
	 * 
	 * @param list
	 *            list
	 */
	protected TypedListIterator(final ListType type, final ListIterator<Object> inner) {
		super();
		this.type = type;
		this.inner = inner;
	}

	public void add(Object o) {
		inner.add(type.getElementType().convertToTypedObject(o));
	}

	public boolean hasNext() {
		return inner.hasNext();
	}

	public boolean hasPrevious() {
		return inner.hasPrevious();
	}

	public Object next() {
		return inner.next();
	}

	public int nextIndex() {
		return inner.nextIndex();
	}

	public Object previous() {
		return inner.previous();
	}

	public int previousIndex() {
		return inner.previousIndex();
	}

	public void remove() {
		inner.remove();
	}

	public void set(Object o) {
		inner.set(type.getElementType().convertToTypedObject(o));
	}

}
