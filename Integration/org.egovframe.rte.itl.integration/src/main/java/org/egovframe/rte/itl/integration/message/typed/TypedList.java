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
package org.egovframe.rte.itl.integration.message.typed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.egovframe.rte.itl.integration.type.ListType;
import org.egovframe.rte.itl.integration.type.Type;

import org.springframework.util.CollectionUtils;

/**
 * 연계 서비스의 표준 메시지 구현 클래스.
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지 Instance를 위한 Class이다. List Type에 해당하는 List
 * Instance를 나타낸다.
 * </p>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	심상호				최초 생성
 * </pre>
 */
public class TypedList implements List<Object> {

	/** type */
	protected final ListType type;

	/** inner list */
	protected List<Object> inner;

	/**
	 * Argument <code>type</code>에 해당하는 TypedList Instance를 생성한다.
	 * 
	 * @param type
	 *            type
	 * @throws IllegalArgumentException
	 *             Argument <code>type</code> 값이 <code>null</code>인 경우
	 */
	public TypedList(final ListType type) {
		super();
		if (type == null) {
			throw new IllegalArgumentException();
		}
		this.type = type;
		this.inner = new ArrayList<Object>();
	}

	/**
	 * Argument <code>type</code>에 해당하는 TypedList Instance를 생성한다. Argument
	 * <code>collection</code> 에 있는 값을 저장한다.
	 * 
	 * @param type
	 *            type
	 * @param collection
	 *            collection
	 * @throws IllegalArgumentException
	 *             1. Argument <code>type</code> 값이 <code>null</code>인 경우 2.
	 *             Argument <code>collection</code> 값이 null인 경우
	 */
	public TypedList(final ListType type, final Collection<? extends Object> collection) {
		this(type);
		if (collection == null) {
			throw new IllegalArgumentException();
		}
		addAll(collection);
	}

	/**
	 * Argument <code>type</code>에 해당하는 TypedList Instance를 생성한다. Argument
	 * <code>array</code>에 있는 값을 저장한다.
	 * 
	 * @param type
	 *            type
	 * @param array
	 *            array
	 * @throws IllegalArgumentException
	 *             1. Argument <code>type</code> 값이 <code>null</code>인 경우 2.
	 *             Argument <code>array</code> 값이 <code>null</code> 이거나 array가
	 *             아닌 경우
	 */
	@SuppressWarnings("unchecked")
	public TypedList(final ListType type, final Object array) {
		this(type);
		if (array == null || array.getClass().isArray() == false) {
			throw new IllegalArgumentException();
		}
		addAll(CollectionUtils.arrayToList(array));
	}

	/**
	 * Type을 읽어온다.
	 * 
	 * @return type
	 */
	public ListType getType() {
		return type;
	}

	/**
	 * List Element의 Type을 읽어온다.
	 * 
	 * @return element type
	 */
	public Type getElementType() {
		return type.getElementType();
	}

	/**
	 * Argument <code>value</code>를 TypedList에 담을 수 있는 형태로 변환한다.
	 * 
	 * @param value
	 *            value
	 * @return 변환된 value
	 */
	protected Object convertToTypedObject(final Object value) {
		return getElementType().convertToTypedObject(value);
	}

	/**
	 * Argument <code>c</code>에 담겨있는 값들을 TypedList에 담을 수 있는 형태로 변환한다.
	 * 
	 * @param c
	 *            값을 담고 있는 Collection 객체
	 * @return 변환된 값을 담고 있는 Collection 객체
	 */
	protected Collection<Object> convertToTypedObjects(final Collection<? extends Object> c) {
		if (c == null) {
			return null;
		}
		Collection<Object> newCollection = new ArrayList<Object>();
		for (Object object : c) {
			newCollection.add(convertToTypedObject(object));
		}
		return newCollection;
	}

	public void add(int index, Object element) {
		inner.add(index, convertToTypedObject(element));
	}

	public boolean add(Object o) {
		return inner.add(convertToTypedObject(o));
	}

	public boolean addAll(Collection<? extends Object> c) {
		return inner.addAll(convertToTypedObjects(c));
	}

	public boolean addAll(int index, Collection<? extends Object> c) {
		return inner.addAll(index, convertToTypedObjects(c));
	}

	public void clear() {
		inner.clear();
	}

	public boolean contains(Object o) {
		return inner.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return inner.containsAll(c);
	}

	public Object get(int index) {
		return inner.get(index);
	}

	public int indexOf(Object o) {
		return inner.indexOf(o);
	}

	public boolean isEmpty() {
		return inner.isEmpty();
	}

	public Iterator<Object> iterator() {
		return inner.iterator();
	}

	public int lastIndexOf(Object o) {
		return inner.lastIndexOf(o);
	}

	public ListIterator<Object> listIterator() {
		return new TypedListIterator(type, inner.listIterator());
	}

	public ListIterator<Object> listIterator(int index) {
		return new TypedListIterator(type, inner.listIterator(index));
	}

	public Object remove(int index) {
		return inner.remove(index);
	}

	public boolean remove(Object o) {
		return inner.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return inner.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return inner.retainAll(c);
	}

	public Object set(int index, Object element) {
		return inner.set(index, convertToTypedObject(element));
	}

	public int size() {
		return inner.size();
	}

	public List<Object> subList(int fromIndex, int toIndex) {
		TypedList subList = new TypedList(this.type);
		subList.inner = inner.subList(fromIndex, toIndex);
		return subList;
	}

	public Object[] toArray() {
		return inner.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return inner.toArray(a);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inner == null) ? 0 : inner.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TypedList other = (TypedList) obj;
		if (inner == null) {
			if (other.inner != null) {
				return false;
			}
		} else if (!inner.equals(other.inner)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

}
