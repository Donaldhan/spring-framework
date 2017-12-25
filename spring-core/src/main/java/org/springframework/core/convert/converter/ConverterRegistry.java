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

package org.springframework.core.convert.converter;

/**
 * For registering converters with a type conversion system.
 *注册转换器到类型转换系统。
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface ConverterRegistry {

	/**
	 * Add a plain converter to this registry.
	 * The convertible source/target type pair is derived from the Converter's parameterized types.
	 * 添加一个空白的转换器到注册器。可转换源、目标类型对从转换器的参数化类型中提取。
	 * @throws IllegalArgumentException if the parameterized types could not be resolved
	 */
	void addConverter(Converter<?, ?> converter);

	/**
	 * Add a plain converter to this registry.
	 * The convertible source/target type pair is specified explicitly.
	 * <p>Allows for a Converter to be reused for multiple distinct pairs without
	 * having to create a Converter class for each pair.
	 * 添加一个空白的转换器到注册器。源、目标类型对显示地添加。在不用创建转换对的情况下，可以重用转换器。
	 * @since 3.1
	 */
	<S, T> void addConverter(Class<S> sourceType, Class<T> targetType, Converter<? super S, ? extends T> converter);

	/**
	 * Add a generic converter to this registry.
	 * 添加GenericConverter到注册器
	 */
	void addConverter(GenericConverter converter);

	/**
	 * Add a ranged converter factory to this registry.
	 * The convertible source/target type pair is derived from the ConverterFactory's parameterized types.
	 * 添加转换器工厂到注册器
	 * @throws IllegalArgumentException if the parameterized types could not be resolved
	 */
	void addConverterFactory(ConverterFactory<?, ?> factory);

	/**
	 * Remove any converters from {@code sourceType} to {@code targetType}.
	 * 移除所有从源类型到目的类型的转换器。
	 * @param sourceType the source type
	 * @param targetType the target type
	 */
	void removeConvertible(Class<?> sourceType, Class<?> targetType);

}
