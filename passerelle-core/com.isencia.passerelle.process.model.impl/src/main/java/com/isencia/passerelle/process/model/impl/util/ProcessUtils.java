package com.isencia.passerelle.process.model.impl.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;

public class ProcessUtils {

	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> emptyIterator() {
		return (Iterator<T>) EmptyIterator.EMPTY_ITERATOR;
	}

	private static class EmptyIterator<E> implements Iterator<E> {
		@SuppressWarnings({"unchecked","rawtypes"})
		static final EmptyIterator<Object> EMPTY_ITERATOR = new EmptyIterator();

		public boolean hasNext() {
			return false;
		}

		public E next() {
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new IllegalStateException();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> ListIterator<T> emptyListIterator() {
		return (ListIterator<T>) EmptyListIterator.EMPTY_ITERATOR;
	}

	private static class EmptyListIterator<E> extends EmptyIterator<E> implements ListIterator<E> {
		@SuppressWarnings({"unchecked","rawtypes"})
		static final EmptyListIterator<Object> EMPTY_ITERATOR = new EmptyListIterator();

		public boolean hasPrevious() {
			return false;
		}

		public E previous() {
			throw new NoSuchElementException();
		}

		public int nextIndex() {
			return 0;
		}

		public int previousIndex() {
			return -1;
		}

		public void set(E e) {
			throw new IllegalStateException();
		}

		public void add(E e) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * @serial include
	 */
	private static class EmptyList<E> extends AbstractList<E> implements RandomAccess, Serializable, Cloneable {
		private static final long serialVersionUID = 8842843931221139166L;

		// Preserves singleton property
		@Override
		protected Object clone() throws CloneNotSupportedException {
			return EMPTY_LIST;
		}

		public boolean contains(Object obj) {
			return false;
		}

		public boolean containsAll(Collection<?> c) {
			return c.isEmpty();
		}

		public boolean equals(Object o) {
			return (o instanceof List) && ((List<?>) o).isEmpty();
		}

		public E get(int index) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}

		public int hashCode() {
			return 1;
		}

		public boolean isEmpty() {
			return true;
		}

		public Iterator<E> iterator() {
			return emptyIterator();
		}

		public ListIterator<E> listIterator() {
			return emptyListIterator();
		}

		// Preserves singleton property
		private Object readResolve() {
			return EMPTY_LIST;
		}

		public int size() {
			return 0;
		}

		public Object[] toArray() {
			return new Object[0];
		}

		public <T> T[] toArray(T[] a) {
			if (a.length > 0)
				a[0] = null;
			return a;
		}
	}

	private static class EmptyMap<K, V> extends AbstractMap<K, V> implements Serializable, Cloneable {
		private static final long serialVersionUID = 6428348081105594320L;

		@Override
		protected Object clone() throws CloneNotSupportedException {
			return EMPTY_MAP;
		}

		public boolean containsKey(Object key) {
			return false;
		}

		public boolean containsValue(Object value) {
			return false;
		}

		public Set<Map.Entry<K, V>> entrySet() {
			return emptySet();
		}

		public boolean equals(Object o) {
			return (o instanceof Map) && ((Map<?, ?>) o).isEmpty();
		}

		public V get(Object key) {
			return null;
		}

		public int hashCode() {
			return 0;
		}

		public boolean isEmpty() {
			return true;
		}

		public Set<K> keySet() {
			return emptySet();
		}

		// Preserves singleton property
		private Object readResolve() {
			return EMPTY_MAP;
		}

		public int size() {
			return 0;
		}

		public Collection<V> values() {
			return emptySet();
		}
	}

	private static class EmptySet<E> extends AbstractSet<E> implements Serializable, Cloneable {
		private static final long serialVersionUID = 1582296315990362920L;

		// Preserves singleton property
		@Override
		protected Object clone() throws CloneNotSupportedException {
			return EMPTY_SET;
		}

		public boolean contains(Object obj) {
			return false;
		}

		public boolean containsAll(Collection<?> c) {
			return c.isEmpty();
		}

		public boolean isEmpty() {
			return true;
		}

		public Iterator<E> iterator() {
			return emptyIterator();
		}

		// Preserves singleton property
		private Object readResolve() {
			return EMPTY_SET;
		}

		public int size() {
			return 0;
		}

		public Object[] toArray() {
			return new Object[0];
		}

		public <T> T[] toArray(T[] a) {
			if (a.length > 0)
				a[0] = null;
			return a;
		}
	}

	@SuppressWarnings("rawtypes")
	public static final Set EMPTY_SET = new EmptySet();
	@SuppressWarnings("rawtypes")
	public static final List EMPTY_LIST = new EmptyList();
	@SuppressWarnings("rawtypes")
	public static final Map EMPTY_MAP = new EmptyMap();

	@SuppressWarnings("unchecked")
	public static final <T> List<T> emptyList() {
		return (List<T>) EMPTY_LIST;
	}

	@SuppressWarnings("unchecked")
	public static final <K, V> Map<K, V> emptyMap() {
		return (Map<K, V>) EMPTY_MAP;
	}

	@SuppressWarnings("unchecked")
	public static final <T> Set<T> emptySet() {
		return (Set<T>) EMPTY_SET;
	}

	/**
	 * determines if a set has been initialized. Sets in process entities are by
	 * default initialized as Collections.EMPTY_SET. This method determines if
	 * the set has really been initialized, either through a load from the
	 * database or an add.
	 * 
	 * @param set
	 * @return true or false
	 */
	public static boolean isInitialized(Collection<?> set) {
		return (set != null && set != EMPTY_SET);
	}

	/**
	 * determines if a list has been initialized. Lists in process entities are
	 * by default initialized as Collections.EMPTY_LIST. This method determines
	 * if the list has really been initialized, either through a load from the
	 * database or an add.
	 * 
	 * @param list
	 * @return true or false
	 */
	public static boolean isInitialized(List<?> list) {
		return (list != null && list != EMPTY_LIST);
	}

	/**
	 * determines if a map has been initialized. Maps in process entities are by
	 * default initialized as Collections.EMPTY_MAP. This method determines if
	 * the map has really been initialized, either through a load from the
	 * database or a put.
	 * 
	 * @param map
	 * @return true or false
	 */
	public static boolean isInitialized(Map<?, ?> map) {
		return (map != null && map != EMPTY_MAP);
	}
}
