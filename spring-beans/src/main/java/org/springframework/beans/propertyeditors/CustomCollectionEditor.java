/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Property editor for Collections, converting any source Collection
 * to a given target Collection type.
 * 集合类属性编辑器，转换任何源集合到给定的目标集合类型。
 * <p>By default registered for Set, SortedSet and List,
 * to automatically convert any given Collection to one of those
 * target types if the type does not match the target property.
 * 如果类型不匹配目标属性，将使用默认的Set, SortedSet and List属性编辑器，将给定的集合转换为
 * 目标集合。
 * @author Juergen Hoeller
 * @since 1.1.3
 * @see java.util.Collection
 * @see java.util.Set
 * @see java.util.SortedSet
 * @see java.util.List
 */
public class CustomCollectionEditor extends PropertyEditorSupport {

	@SuppressWarnings("rawtypes")
	private final Class<? extends Collection> collectionType;

	private final boolean nullAsEmptyCollection;


	/**
	 * Create a new CustomCollectionEditor for the given target type,
	 * keeping an incoming {@code null} as-is.
	 * @param collectionType the target type, which needs to be a
	 * sub-interface of Collection or a concrete Collection class
	 * @see java.util.Collection
	 * @see java.util.ArrayList
	 * @see java.util.TreeSet
	 * @see java.util.LinkedHashSet
	 */
	@SuppressWarnings("rawtypes")
	public CustomCollectionEditor(Class<? extends Collection> collectionType) {
		this(collectionType, false);
	}

	/**
	 * Create a new CustomCollectionEditor for the given target type.
	 * <p>If the incoming value is of the given type, it will be used as-is.
	 * If it is a different Collection type or an array, it will be converted
	 * to a default implementation of the given Collection type.
	 * If the value is anything else, a target Collection with that single
	 * value will be created.
	 * <p>The default Collection implementations are: ArrayList for List,
	 * TreeSet for SortedSet, and LinkedHashSet for Set.
	 * @param collectionType the target type, which needs to be a
	 * sub-interface of Collection or a concrete Collection class
	 * @param nullAsEmptyCollection whether to convert an incoming {@code null}
	 * value to an empty Collection (of the appropriate type)
	 * @see java.util.Collection
	 * @see java.util.ArrayList
	 * @see java.util.TreeSet
	 * @see java.util.LinkedHashSet
	 */
	@SuppressWarnings("rawtypes")
	public CustomCollectionEditor(Class<? extends Collection> collectionType, boolean nullAsEmptyCollection) {
		if (collectionType == null) {
			throw new IllegalArgumentException("Collection type is required");
		}
		if (!Collection.class.isAssignableFrom(collectionType)) {
			throw new IllegalArgumentException(
					"Collection type [" + collectionType.getName() + "] does not implement [java.util.Collection]");
		}
		this.collectionType = collectionType;
		this.nullAsEmptyCollection = nullAsEmptyCollection;
	}


	/**
	 * Convert the given text value to a Collection with a single element.
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		setValue(text);
	}

	/**
	 * Convert the given value to a Collection of the target type.
	 * 转换给定的值为给定的目标集合
	 */
	@Override
	public void setValue(Object value) {
		if (value == null && this.nullAsEmptyCollection) {//空集合
			super.setValue(createCollection(this.collectionType, 0));
		}
		else if (value == null || (this.collectionType.isInstance(value) && !alwaysCreateNewCollection())) {
			// Use the source value as-is, as it matches the target type.
			super.setValue(value);
		}
		else if (value instanceof Collection) {//集合类型
			// Convert Collection elements.
			Collection<?> source = (Collection<?>) value;
			Collection<Object> target = createCollection(this.collectionType, source.size());
			for (Object elem : source) {
				target.add(convertElement(elem));
			}
			super.setValue(target);
		}
		else if (value.getClass().isArray()) {//数组类型
			// Convert array elements to Collection elements.
			int length = Array.getLength(value);
			Collection<Object> target = createCollection(this.collectionType, length);
			for (int i = 0; i < length; i++) {
				target.add(convertElement(Array.get(value, i)));
			}
			super.setValue(target);
		}
		else {
			// A plain value: convert it to a Collection with a single element.
			Collection<Object> target = createCollection(this.collectionType, 1);
			target.add(convertElement(value));
			super.setValue(target);
		}
	}

	/**
	 * Create a Collection of the given type, with the given
	 * initial capacity (if supported by the Collection type).
	 * 创建给定类型的集合，并初始化容量
	 * @param collectionType a sub-interface of Collection
	 * @param initialCapacity the initial capacity
	 * @return the new Collection instance
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Collection<Object> createCollection(Class<? extends Collection> collectionType, int initialCapacity) {
		if (!collectionType.isInterface()) {//非接口，则直接创建
			try {
				return collectionType.newInstance();
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException(
						"Could not instantiate collection class: " + collectionType.getName(), ex);
			}
		}
		else if (List.class == collectionType) {//List
			return new ArrayList<Object>(initialCapacity);
		}
		else if (SortedSet.class == collectionType) {//Set
			return new TreeSet<Object>();
		}
		else {
			return new LinkedHashSet<Object>(initialCapacity);//LinkedHashSet
		}
	}

	/**
	 * Return whether to always create a new Collection,
	 * even if the type of the passed-in Collection already matches.
	 * 返回是否总是创建新的集合，即使传过去的集合类型已经匹配。
	 * <p>Default is "false"; can be overridden to enforce creation of a
	 * new Collection, for example to convert elements in any case.
	 * 默认为false，可以强制创建一个新的集合，比如转换集合元素。
	 * @see #convertElement
	 */
	protected boolean alwaysCreateNewCollection() {
		return false;
	}

	/**
	 * Hook to convert each encountered Collection/array element.
	 * 转换集合元素的Hook。
	 * The default implementation simply returns the passed-in element as-is.
	 * 默认的实现，简单地返回原始元素。
	 * <p>Can be overridden to perform conversion of certain elements,
	 * for example String to Integer if a String array comes in and
	 * should be converted to a Set of Integer objects.
	 * 子列可以重写，执行确定类型元素的转换，比如，如果转换String数组为Integer数组。
	 * <p>Only called if actually creating a new Collection!
	 * This is by default not the case if the type of the passed-in Collection
	 * already matches. Override {@link #alwaysCreateNewCollection()} to
	 * enforce creating a new Collection in every case.
	 * 在调用此方法之前，实际上目标集合已经创建。如果目标集合类型已经匹配，不在上述范围之内。可以通过重写
	 * {@link #alwaysCreateNewCollection()}方法，来保证在任何情况下始终创建一个集合。
	 * @param element the source element
	 * @return the element to be used in the target Collection
	 * @see #alwaysCreateNewCollection()
	 */
	protected Object convertElement(Object element) {
		return element;
	}


	/**
	 * This implementation returns {@code null} to indicate that
	 * there is no appropriate text representation.
	 */
	@Override
	public String getAsText() {
		return null;
	}

}
