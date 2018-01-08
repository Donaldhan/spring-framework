/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.core.env;

import org.springframework.util.ObjectUtils;

/**
 * A {@link PropertySource} implementation capable of interrogating its
 * underlying source object to enumerate all possible property name/value
 * pairs. Exposes the {@link #getPropertyNames()} method to allow callers
 * to introspect available properties without having to access the underlying
 * source object. This also facilitates a more efficient implementation of
 * {@link #containsProperty(String)}, in that it can call {@link #getPropertyNames()}
 * and iterate through the returned array rather than attempting a call to
 * {@link #getProperty(String)} which may be more expensive. Implementations may
 * consider caching the result of {@link #getPropertyNames()} to fully exploit this
 * performance opportunity.
 * EnumerablePropertySource为属性源的一种实现，可以检索底层数据源，并枚举属性值对。
 * {@link #getPropertyNames()}暴露方法，允许调用者在不访问底层属性源的情况下，检查给定的属性是否存在。
 * 提供了是否包含属性源的有效实现，调用{@link #getPropertyNames()返回name集，迭代检查，而不是尝试调用
 * 代价昂贵的{@link #getProperty(String)}方法。具体的实现可以考虑缓存属性源name集，进一步优化性能。
 * <p>Most framework-provided {@code PropertySource} implementations are enumerable;
 * a counter-example would be {@code JndiPropertySource} where, due to the
 * nature of JNDI it is not possible to determine all possible property names at
 * any given time; rather it is only possible to try to access a property
 * (via {@link #getProperty(String)}) in order to evaluate whether it is present
 * or not.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
public abstract class EnumerablePropertySource<T> extends PropertySource<T> {

	public EnumerablePropertySource(String name, T source) {
		super(name, source);
	}

	protected EnumerablePropertySource(String name) {
		super(name);
	}


	/**
	 * Return whether this {@code PropertySource} contains a property with the given name.
	 * <p>This implementation checks for the presence of the given name within the
	 * {@link #getPropertyNames()} array.
	 * 判断当前属性源是否包含跟定name对应的属性。当前实现仅仅检查属性name是否在属性源的name集中。
	 * @param name the name of the property to find
	 */
	@Override
	public boolean containsProperty(String name) {
		return ObjectUtils.containsElement(getPropertyNames(), name);
	}

	/**
	 * Return the names of all properties contained by the
	 * {@linkplain #getSource() source} object (never {@code null}).
	 * 返回属性源中的所有属性name
	 */
	public abstract String[] getPropertyNames();

}
